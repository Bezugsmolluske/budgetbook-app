package de.poljansek.budgetbook

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.poljansek.budgetbook.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "BudgetBook",
        ) {
            App()
        }
    }
}
