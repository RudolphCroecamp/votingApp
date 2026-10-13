package com.example.frontend.ui.Components.Camera

import android.graphics.Bitmap
import android.graphics.Paint

enum class CameraCaptureStates {
    ID_DOCUMENT,FACE
}

sealed interface CaptureState {
    data object Framing: CaptureState
    data object Capturing: CaptureState
    data class Problem (val message: String) : CaptureState
    data class Captured (val photo: Bitmap): CaptureState
    data object Uploading: CaptureState
}