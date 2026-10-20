package com.example.frontend.data.face

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.face.FaceLandmark
import android.graphics.Color
import android.graphics.Rect
import androidx.core.graphics.scale
import com.example.frontend.ui.components.Camera.createFaceDetector
import com.example.frontend.ui.components.Camera.detect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

const val FACE_MODEL_ASSET = "mobilefacenet.tflite"

private const val FACE_CROP_MARGIN = 0.15f

fun interface FaceEmbedder {
    suspend fun embed(photo: Bitmap): DoubleArray
}

sealed class FaceEmbeddingException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NoFace : FaceEmbeddingException("No single face found in the photo")
    class ModelMissing(cause: Throwable) : FaceEmbeddingException("Missing assets/$FACE_MODEL_ASSET", cause)
}

class TfliteFaceEmbedder(
    private val context: Context,
    private val modelAsset: String = FACE_MODEL_ASSET
) : FaceEmbedder {

    private val lock = Mutex()
    private var interpreter: Interpreter? = null

    override suspend fun embed(photo: Bitmap): DoubleArray {
        val faces = createFaceDetector(accurate = true, landmarks = true).use { it.detect(photo) }
        val face = faces.singleOrNull() ?: throw FaceEmbeddingException.NoFace()
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
        return withContext(Dispatchers.Default) {
            lock.withLock {
                val model = interpreter()
                val size = model.getInputTensor(0).shape().let { it[2] to it[1] } // width to height
                // Line the eyes up on the template every time, so tilt and distance stop
                // changing the embedding between registration and sign-in.
                val aligned = if (leftEye != null && rightEye != null) {
                    photo.alignedFace(leftEye, rightEye, size.first, size.second)
                } else {
                    photo.cropAround(face.boundingBox).scale(size.first, size.second)
                }
                // Averaging with the mirror image is a standard, cheap accuracy boost.
                val forward = run(model, aligned)
                val mirrored = run(model, aligned.mirrored())
                DoubleArray(forward.size) { forward[it] + mirrored[it] }.l2Normalized()
            }
        }
    }

    private fun interpreter(): Interpreter = interpreter ?: Interpreter(
        loadModel(),
        Interpreter.Options().setNumThreads(4)
    ).also { interpreter = it }

    private fun loadModel(): MappedByteBuffer = try {
        context.assets.openFd(modelAsset).use { fd ->
            FileInputStream(fd.fileDescriptor).use { stream ->
                stream.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
            }
        }
    } catch (e: FileNotFoundException) {
        throw FaceEmbeddingException.ModelMissing(e)
    }

    private fun run(model: Interpreter, face: Bitmap): DoubleArray {
        val inputShape = model.getInputTensor(0).shape()
        val batch = inputShape[0]
        val height = inputShape[1]
        val width = inputShape[2]
        val outputSize = model.getOutputTensor(0).shape().last()

        val pixels = IntArray(width * height)
        val input112 = if (face.width == width && face.height == height) face else face.scale(width, height)
        input112.getPixels(pixels, 0, width, 0, 0, width, height)

        val input = ByteBuffer.allocateDirect(batch * width * height * 3 * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
        repeat(batch) {
            pixels.forEach { pixel ->
                input.putFloat((Color.red(pixel) - 127.5f) / 128f)
                input.putFloat((Color.green(pixel) - 127.5f) / 128f)
                input.putFloat((Color.blue(pixel) - 127.5f) / 128f)
            }
        }
        input.rewind()

        val output = Array(batch) { FloatArray(outputSize) }
        model.run(input, output)
        return output[0].map { it.toDouble() }.toDoubleArray().l2Normalized()
    }
}

private fun Bitmap.cropAround(box: Rect): Bitmap {
    val marginX = (box.width() * FACE_CROP_MARGIN).toInt()
    val marginY = (box.height() * FACE_CROP_MARGIN).toInt()
    val left = (box.left - marginX).coerceIn(0, width - 1)
    val top = (box.top - marginY).coerceIn(0, height - 1)
    val right = (box.right + marginX).coerceIn(left + 1, width)
    val bottom = (box.bottom + marginY).coerceIn(top + 1, height)
    return Bitmap.createBitmap(this, left, top, right - left, bottom - top)
}

private fun DoubleArray.l2Normalized(): DoubleArray {
    val norm = sqrt(sumOf { it * it })
    return DoubleArray(size) { if (norm == 0.0) 0.0 else this[it] / norm }
}

/**
 * Where the eyes sit in the 112 x 112 ArcFace/MobileFaceNet training template.
 * Scaled to the model's real input size below.
 */
private const val TEMPLATE_SIZE = 112f
private val TEMPLATE_LEFT_EYE = PointF(38.29f, 51.70f)
private val TEMPLATE_RIGHT_EYE = PointF(73.53f, 51.50f)

/**
 * Rotates, scales and crops so the eyes land exactly on the template positions.
 * ML Kit's LEFT_EYE is the subject's left eye, which is on the right of the image.
 */
internal fun Bitmap.alignedFace(subjectLeftEye: PointF, subjectRightEye: PointF, outWidth: Int, outHeight: Int): Bitmap {
    // Image-left eye first, so the angle is measured left-to-right across the picture.
    val (imageLeft, imageRight) =
        if (subjectRightEye.x <= subjectLeftEye.x) subjectRightEye to subjectLeftEye else subjectLeftEye to subjectRightEye
    val sx = outWidth / TEMPLATE_SIZE
    val sy = outHeight / TEMPLATE_SIZE
    val targetLeft = PointF(TEMPLATE_LEFT_EYE.x * sx, TEMPLATE_LEFT_EYE.y * sy)
    val targetRight = PointF(TEMPLATE_RIGHT_EYE.x * sx, TEMPLATE_RIGHT_EYE.y * sy)

    val matrix = Matrix()
    matrix.setPolyToPoly(
        floatArrayOf(imageLeft.x, imageLeft.y, imageRight.x, imageRight.y), 0,
        floatArrayOf(targetLeft.x, targetLeft.y, targetRight.x, targetRight.y), 0,
        2 // two point pairs = rotation + uniform scale + translation, no skew
    )
    val out = createBitmap(outWidth, outHeight)
    Canvas(out).drawBitmap(this, matrix, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
    return out
}

private fun Bitmap.mirrored(): Bitmap =
    Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply { preScale(-1f, 1f) }, true)
