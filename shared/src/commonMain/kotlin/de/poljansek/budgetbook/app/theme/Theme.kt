package de.poljansek.budgetbook.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.poljansek.budgetbook.core.settings.ThemePreference

private val TealSeed = Color(0xFF00897B)
private val LightColors = lightColorScheme(
    primary = TealSeed,
    tertiary = Color(0xFF2E7D32),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    tertiary = Color(0xFFA5D6A7),
)

@Composable
fun BudgetBookTheme(
    preference: ThemePreference,
    content: @Composable () -> Unit,
) {
    val dark = when (preference) {
        ThemePreference.System -> isSystemInDarkTheme()
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}
