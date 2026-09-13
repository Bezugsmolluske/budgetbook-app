package de.poljansek.budgetbook.feature.incomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.config.AppConfig
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.dto.IncomeCategoryDto
import de.poljansek.budgetbook.core.network.dto.IncomesDto
import de.poljansek.budgetbook.core.ui.yearChoices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IncomesUiState(
    val loading: Boolean = true,
    val data: IncomesDto? = null,
    val categories: List<IncomeCategoryDto> = emptyList(),
    val month: Int = 9,
    val year: Int = yearChoices().first(),
    val category: String = AppConfig.ALL_CATEGORIES,
    val error: String? = null,
)

class IncomesViewModel(
    private val api: BudgetBookApi,
) : ViewModel() {
    private val _state = MutableStateFlow(IncomesUiState())
    val state: StateFlow<IncomesUiState> = _state.asStateFlow()

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

    fun refresh() {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(loading = true, error = null)
            runCatching {
                val categories = api.getIncomeCategories()
                val data = api.getIncomes(current.month, current.year, current.category)
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
            runCatching { api.deleteIncome(id) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "Fehler") }
        }
    }
}
