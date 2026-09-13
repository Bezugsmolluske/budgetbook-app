package de.poljansek.budgetbook.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.dto.YearlyOverviewDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OverviewUiState(
    val loading: Boolean = true,
    val years: List<YearlyOverviewDto> = emptyList(),
    val error: String? = null,
)

class OverviewViewModel(
    private val api: BudgetBookApi,
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState())
    val state: StateFlow<OverviewUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { api.getOverview() }
                .onSuccess { _state.value = OverviewUiState(loading = false, years = it) }
                .onFailure { _state.value = OverviewUiState(loading = false, error = it.message ?: "Fehler") }
        }
    }
}
