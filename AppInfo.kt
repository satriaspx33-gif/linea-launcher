package com.linea.launcher.data

import android.graphics.drawable.Drawable

/** One launchable app entry. */
data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val icon: Drawable?,
) {
    /** Stable id "pkg/class" used for favorites, hidden set, folders. */
    val key: String get() = "$packageName/$className"
    val sortKey: String get() = label.trim().ifEmpty { packageName }
    val firstLetter: Char
        get() {
            val c = sortKey.firstOrNull()?.uppercaseChar() ?: '#'
            return if (c in 'A'..'Z') c else '#'
        }
}
