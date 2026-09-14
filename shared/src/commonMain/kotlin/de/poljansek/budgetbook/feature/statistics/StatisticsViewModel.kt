package de.poljansek.budgetbook.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.network.dto.TransactionSummaryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val loading: Boolean = true,
    val categories: List<CategoryDto> = emptyList(),
    val kind: BookType = BookType.EXPENSE,
    val categoryId: String? = null,
    val statistic: TransactionSummaryDto? = null,
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

    fun setKind(kind: BookType) {
        val first = _state.value.categories.firstOrNull { it.type == kind }
        _state.value = _state.value.copy(kind = kind, categoryId = first?.id)
        refresh()
    }

    fun setCategoryName(name: String) {
        val id = _state.value.categories.firstOrNull { it.name == name && it.type == _state.value.kind }?.id
        _state.value = _state.value.copy(categoryId = id)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(loading = true, error = null)
            runCatching {
                val categories = api.getCategories()
                val selectedId = current.categoryId
                    ?: categories.firstOrNull { it.type == current.kind }?.id
                val statistic = if (selectedId == null) {
                    null
                } else {
                    api.getSummary(type = current.kind, categoryId = selectedId)
                }
                Triple(categories, selectedId, statistic)
            }.onSuccess { (categories, selectedId, statistic) ->
                _state.value = _state.value.copy(
                    loading = false,
                    categories = categories,
                    categoryId = selectedId,
                    statistic = statistic,
                )
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Fehler")
            }
        }
    }
}
