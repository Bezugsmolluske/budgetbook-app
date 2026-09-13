package de.poljansek.budgetbook.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.config.AppConfig
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.dto.ExpenseCategoryDto
import de.poljansek.budgetbook.core.network.dto.ExpensesDto
import de.poljansek.budgetbook.core.ui.yearChoices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ExpenseFilterMode { MonthYear, DateRange }

data class ExpensesUiState(
    val loading: Boolean = true,
    val data: ExpensesDto? = null,
    val categories: List<ExpenseCategoryDto> = emptyList(),
    val month: Int = 9,
    val year: Int = yearChoices().first(),
    val category: String = AppConfig.ALL_CATEGORIES,
    val mode: ExpenseFilterMode = ExpenseFilterMode.MonthYear,
    val startDate: String = "${yearChoices().first()}-01-01",
    val endDate: String = "${yearChoices().first()}-12-31",
    val error: String? = null,
)

class ExpensesViewModel(
    private val api: BudgetBookApi,
) : ViewModel() {
    private val _state = MutableStateFlow(ExpensesUiState())
    val state: StateFlow<ExpensesUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun setMonth(month: Int) {
        _state.value = _state.value.copy(month = month)
        refresh()
    }

    fun setYear(year: Int) {
        _state.value = _state.value.copy(year = year)
        refresh()
    }

    fun setCategory(category: String) {
        _state.value = _state.value.copy(category = category)
        refresh()
    }

    fun setMode(mode: ExpenseFilterMode) {
        _state.value = _state.value.copy(mode = mode)
        refresh()
    }

    fun setStartDate(value: String) {
        _state.value = _state.value.copy(startDate = value)
        refresh()
    }

    fun setEndDate(value: String) {
        _state.value = _state.value.copy(endDate = value)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(loading = true, error = null)
            runCatching {
                val categories = api.getExpenseCategories()
                val data = if (current.mode == ExpenseFilterMode.DateRange) {
                    api.getExpenses(
                        category = current.category,
                        startDate = current.startDate,
                        endDate = current.endDate,
                    )
                } else {
                    api.getExpenses(
                        month = current.month,
                        year = current.year,
                        category = current.category,
                    )
                }
                categories to data
            }.onSuccess { (categories, data) ->
                _state.value = _state.value.copy(loading = false, categories = categories, data = data)
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Fehler")
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { api.deleteExpense(id) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "Fehler") }
        }
    }
}
