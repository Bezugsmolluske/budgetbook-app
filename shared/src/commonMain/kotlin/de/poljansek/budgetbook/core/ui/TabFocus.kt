package de.poljansek.budgetbook.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

fun Modifier.moveFocusOnTab(next: FocusRequester?, previous: FocusRequester?): Modifier =
    onPreviewKeyEvent { event ->
        if (!event.isTab()) return@onPreviewKeyEvent false
        if (event.type == KeyEventType.KeyDown) {
            val target = if (event.isShiftPressed) previous else next
            target?.requestFocus()
        }
        true
    }

fun Modifier.ignoreTab(): Modifier =
    onPreviewKeyEvent { event -> event.isTab() }

private fun KeyEvent.isTab(): Boolean =
    key == Key.Tab && (type == KeyEventType.KeyDown || type == KeyEventType.KeyUp)
