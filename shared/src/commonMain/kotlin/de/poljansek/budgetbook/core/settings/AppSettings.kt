package de.poljansek.budgetbook.core.settings

import com.russhwolf.settings.Settings
import de.poljansek.budgetbook.core.config.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemePreference {
    System,
    Light,
    Dark,
}

class AppSettings(private val settings: Settings) {
    private val _theme = MutableStateFlow(readTheme())
    val theme: StateFlow<ThemePreference> = _theme.asStateFlow()

    private val _baseUrlOverride = MutableStateFlow(settings.getStringOrNull(KEY_BASE_URL))
    val baseUrlOverride: StateFlow<String?> = _baseUrlOverride.asStateFlow()

    fun resolvedBaseUrl(): String {
        val override = _baseUrlOverride.value?.trim()?.trimEnd('/')
        if (!override.isNullOrEmpty()) return override
        return AppConfig.flavorDefaultBaseUrl()
    }

    fun setTheme(preference: ThemePreference) {
        settings.putString(KEY_THEME, preference.name)
        _theme.value = preference
    }

    fun setBaseUrlOverride(url: String?) {
        val cleaned = url?.trim()?.trimEnd('/').orEmpty()
        if (cleaned.isEmpty()) {
            settings.remove(KEY_BASE_URL)
            _baseUrlOverride.value = null
        } else {
            settings.putString(KEY_BASE_URL, cleaned)
            _baseUrlOverride.value = cleaned
        }
    }

    private fun readTheme(): ThemePreference {
        val raw = settings.getStringOrNull(KEY_THEME) ?: return ThemePreference.System
        return ThemePreference.entries.find { it.name == raw } ?: ThemePreference.System
    }

    private companion object {
        const val KEY_THEME = "theme_preference"
        const val KEY_BASE_URL = "base_url_override"
    }
}
