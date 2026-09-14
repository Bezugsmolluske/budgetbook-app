package de.poljansek.budgetbook.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.network.dto.TransactionsDto
import de.poljansek.budgetbook.core.ui.monthEnd
import de.poljansek.budgetbook.core.ui.monthStart
import de.poljansek.budgetbook.core.ui.yearChoices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TransactionFilterMode { MonthYear, DateRange }

const val ALL_CATEGORIES_LABEL = "Alle"

data class TransactionsUiState(
    val loading: Boolean = true,
    val data: TransactionsDto? = null,
    val categories: List<CategoryDto> = emptyList(),
    val month: Int = 9,
    val year: Int = yearChoices().first(),
    val categoryId: String? = null,
    val mode: TransactionFilterMode = TransactionFilterMode.MonthYear,
    val startDate: String = "${yearChoices().first()}-01-01",
    val endDate: String = "${yearChoices().first()}-12-31",
    val error: String? = null,
)

class TransactionsViewModel(
    private val type: BookType,
    private val api: BudgetBookApi,
) : ViewModel() {
    private val _state = MutableStateFlow(TransactionsUiState())
    val state: StateFlow<TransactionsUiState> = _state.asStateFlow()

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

    fun setCategoryLabel(label: String) {
        val categoryId = _state.value.categories.firstOrNull { it.name == label }?.id
        _state.value = _state.value.copy(categoryId = categoryId)
        refresh()
    }

    fun setMode(mode: TransactionFilterMode) {
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
                val categories = api.getCategories(type)
                val (from, to) = if (current.mode == TransactionFilterMode.DateRange) {
                    current.startDate to current.endDate
                } else {
                    monthStart(current.year, current.month) to monthEnd(current.year, current.month)
                }
                val data = api.getTransactions(from = from, to = to, type = type, categoryId = current.categoryId)
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
            runCatching { api.deleteTransaction(id) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "Fehler") }
        }
    }
}
