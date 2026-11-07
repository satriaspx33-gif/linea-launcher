package com.linea.launcher.ui.home

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LETTERS = ('A'..'Z').toList() + '#'

/**
 * Signature A-Z fast-scroll strip. Drag along it to jump to a letter.
 * [onLetter] fires with the chosen section letter; double-tap triggers [onDoubleTap]
 * (used for the quick-lock gesture).
 */
@Composable
fun AlphabetScroller(
    onLetter: (Char) -> Unit,
    modifier: Modifier = Modifier,
    onDoubleTap: (() -> Unit)? = null,
) {
    var height by remember { mutableStateOf(1f) }
    var active by remember { mutableStateOf<Char?>(null) }

    fun pick(y: Float) {
        val idx = ((y / height) * LETTERS.size).toInt().coerceIn(0, LETTERS.size - 1)
        val c = LETTERS[idx]
        if (c != active) { active = c; onLetter(c) }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(24.dp)
            .padding(vertical = 8.dp)
            .onGloballyPositioned { height = it.size.height.toFloat().coerceAtLeast(1f) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { pick(it.y) },
                    onDragEnd = { active = null },
                    onDragCancel = { active = null },
                ) { change, _ -> pick(change.position.y) }
            }
            .pointerInput(onDoubleTap) {
                androidx.compose.foundation.gestures.detectTapGestures(
                    onDoubleTap = { onDoubleTap?.invoke() },
                    onTap = { pick(it.y) },
                )
            },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LETTERS.forEach { c ->
            Text(
                text = c.toString(),
                fontSize = 11.sp,
                color = if (c == active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
