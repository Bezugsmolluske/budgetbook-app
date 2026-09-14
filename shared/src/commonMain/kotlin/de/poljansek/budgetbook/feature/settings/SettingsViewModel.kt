package de.poljansek.budgetbook.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.config.AppConfig
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.settings.AppSettings
import de.poljansek.budgetbook.core.settings.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrlDraft: String = "",
    val message: String? = null,
    val busy: Boolean = false,
)

class SettingsViewModel(
    private val appSettings: AppSettings,
    private val api: BudgetBookApi,
) : ViewModel() {
    val theme: StateFlow<ThemePreference> = appSettings.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), appSettings.theme.value)

    private val _state = MutableStateFlow(
        SettingsUiState(baseUrlDraft = appSettings.resolvedBaseUrl())
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    val flavorDefault: String = AppConfig.flavorDefaultBaseUrl()
    val flavorName: String = if (AppConfig.isProd()) "prod" else "dev"

    fun setTheme(preference: ThemePreference) {
        appSettings.setTheme(preference)
    }

    fun updateBaseUrlDraft(value: String) {
        _state.value = _state.value.copy(baseUrlDraft = value)
    }

    fun saveBaseUrl() {
        val draft = _state.value.baseUrlDraft.trim()
        appSettings.setBaseUrlOverride(draft.takeIf { it.isNotEmpty() && it != flavorDefault })
        _state.value = _state.value.copy(
            baseUrlDraft = appSettings.resolvedBaseUrl(),
            message = "API-URL gespeichert",
        )
    }

    fun resetBaseUrl() {
        appSettings.setBaseUrlOverride(null)
        _state.value = _state.value.copy(
            baseUrlDraft = appSettings.resolvedBaseUrl(),
            message = "API-URL auf Flavor-Standard zurückgesetzt",
        )
    }

    fun notice(text: String) {
        _state.value = _state.value.copy(message = text)
    }

    suspend fun exportJson(): ByteArray = api.downloadJsonBackup()
    suspend fun exportCsv(): ByteArray = api.downloadCsvBackup()

    fun restoreJson(bytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            runCatching { api.restoreJsonBackup(bytes, fileName) }
                .onSuccess { _state.value = _state.value.copy(busy = false, message = "Wiederhergestellt: ${it.filename}") }
                .onFailure { _state.value = _state.value.copy(busy = false, message = it.message) }
        }
    }

    fun restoreCsv(bytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            runCatching { api.restoreCsvBackup(bytes, fileName) }
                .onSuccess { _state.value = _state.value.copy(busy = false, message = "Wiederhergestellt: ${it.filename}") }
                .onFailure { _state.value = _state.value.copy(busy = false, message = it.message) }
        }
    }
}
