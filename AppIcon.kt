package com.linea.launcher.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.painter.BitmapPainter

fun Drawable.toBitmap(size: Int = 128): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    val w = if (intrinsicWidth > 0) intrinsicWidth else size
    val h = if (intrinsicHeight > 0) intrinsicHeight else size
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bmp
}

@Composable
fun rememberIconPainter(icon: Drawable?): Painter? {
    icon ?: return null
    return remember(icon) { BitmapPainter(icon.toBitmap().asImageBitmap()) }
}

@Composable
fun AppIconImage(icon: Drawable?, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val painter = rememberIconPainter(icon) ?: return
    Image(painter = painter, contentDescription = contentDescription, modifier = modifier)
}
