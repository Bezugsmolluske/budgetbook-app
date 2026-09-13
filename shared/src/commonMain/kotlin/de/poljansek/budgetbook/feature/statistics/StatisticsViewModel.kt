package de.poljansek.budgetbook.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.config.AppConfig
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.dto.ExpenseCategoryDto
import de.poljansek.budgetbook.core.network.dto.ExpenseStatisticDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val loading: Boolean = true,
    val categories: List<ExpenseCategoryDto> = emptyList(),
    val category: String = AppConfig.ALL_CATEGORIES,
    val statistic: ExpenseStatisticDto? = null,
    val error: String? = null,
)

class StatisticsViewModel(
    private val api: BudgetBookApi,
) : ViewModel() {
    private val _state = MutableStateFlow(StatisticsUiState())
    val state: StateFlow<StatisticsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun setCategory(category: String) {
        _state.value = _state.value.copy(category = category)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(loading = true, error = null)
            runCatching {
                val categories = api.getExpenseCategories()
                val statistic = api.getExpenseStatistics(current.category)
                categories to statistic
            }.onSuccess { (categories, statistic) ->
                _state.value = _state.value.copy(
                    loading = false,
                    categories = categories,
                    statistic = statistic,
                )
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Fehler")
            }
        }
    }
}
