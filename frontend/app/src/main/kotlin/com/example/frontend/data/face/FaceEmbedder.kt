package com.example.frontend.data.face

import android.content.Context
import android.graphics.Bitmap
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
        val faces = createFaceDetector(accurate = true).use { it.detect(photo) }
        val face = faces.singleOrNull() ?: throw FaceEmbeddingException.NoFace()
        return withContext(Dispatchers.Default) {
            val crop = photo.cropAround(face.boundingBox)
            lock.withLock { run(interpreter(), crop) }
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
        face.scale(width, height).getPixels(pixels, 0, width, 0, 0, width, height)

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
        return output[0].l2Normalized()
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

private fun FloatArray.l2Normalized(): DoubleArray {
    val norm = sqrt(sumOf { it.toDouble() * it })
    return DoubleArray(size) { if (norm == 0.0) 0.0 else this[it] / norm }
}
