package de.poljansek.budgetbook.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RefreshOnEnter(refresh: () -> Unit) {
    LaunchedEffect(Unit) { refresh() }
}
