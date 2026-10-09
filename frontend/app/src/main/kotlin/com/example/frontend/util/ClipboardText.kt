package com.example.frontend.util

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

/**
 * Plain-text helpers over the suspend [Clipboard] API.
 *
 * [Clipboard] deals in [ClipEntry], which wraps Android's [ClipData] and can
 * hold several items of any type. Everything this app copies is a single short
 * string, so these two functions keep that plumbing out of the screens.
 */
suspend fun Clipboard.setPlainText(label: String, text: String) {
    setClipEntry(ClipEntry(ClipData.newPlainText(label, text)))
}

/** Returns the first text item on the clipboard, or null if there isn't one. */
suspend fun Clipboard.getPlainText(): String? {
    val clipData = getClipEntry()?.clipData ?: return null
    if (clipData.itemCount == 0) return null
    return clipData.getItemAt(0).text?.toString()
}
