package de.poljansek.budgetbook.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.ConflictException
import de.poljansek.budgetbook.core.network.dto.CategoryDtoCreate
import de.poljansek.budgetbook.core.network.dto.ExpenseCategoryDto
import de.poljansek.budgetbook.core.network.dto.IncomeCategoryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CategoryKind { Expense, Income }

data class CategoriesUiState(
    val loading: Boolean = true,
    val expenseCategories: List<ExpenseCategoryDto> = emptyList(),
    val incomeCategories: List<IncomeCategoryDto> = emptyList(),
    val kind: CategoryKind = CategoryKind.Expense,
    val draftName: String = "",
    val editingId: String? = null,
    val editingVersion: Long? = null,
    val error: String? = null,
    val conflict: Boolean = false,
)

class CategoriesViewModel(
    private val api: BudgetBookApi,
) : ViewModel() {
    private val _state = MutableStateFlow(CategoriesUiState())
    val state: StateFlow<CategoriesUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun setKind(kind: CategoryKind) {
        _state.value = _state.value.copy(kind = kind, draftName = "", editingId = null)
    }

    fun updateDraft(value: String) {
        _state.value = _state.value.copy(draftName = value)
    }

    fun startEditExpense(category: ExpenseCategoryDto) {
        _state.value = _state.value.copy(
            kind = CategoryKind.Expense,
            draftName = category.category,
            editingId = category.id,
            editingVersion = category.version,
        )
    }

    fun startEditIncome(category: IncomeCategoryDto) {
        _state.value = _state.value.copy(
            kind = CategoryKind.Income,
            draftName = category.category,
            editingId = category.id,
            editingVersion = category.version,
        )
    }

    fun cancelEdit() {
        _state.value = _state.value.copy(draftName = "", editingId = null, editingVersion = null)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, conflict = false)
            runCatching {
                api.getExpenseCategories() to api.getIncomeCategories()
            }.onSuccess { (expenses, incomes) ->
                _state.value = _state.value.copy(
                    loading = false,
                    expenseCategories = expenses,
                    incomeCategories = incomes,
                )
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Fehler")
            }
        }
    }

    fun save() {
        val current = _state.value
        val name = current.draftName.trim()
        if (name.isEmpty()) {
            _state.value = current.copy(error = "Name darf nicht leer sein")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(error = null, conflict = false)
            runCatching {
                if (current.kind == CategoryKind.Expense) {
                    if (current.editingId == null) {
                        api.createExpenseCategory(CategoryDtoCreate(name))
                    } else {
                        api.updateExpenseCategory(
                            current.editingId,
                            ExpenseCategoryDto(
                                id = current.editingId,
                                version = current.editingVersion,
                                category = name,
                            ),
                        )
                    }
                } else {
                    if (current.editingId == null) {
                        api.createIncomeCategory(CategoryDtoCreate(name))
                    } else {
                        api.updateIncomeCategory(
                            current.editingId,
                            IncomeCategoryDto(
                                id = current.editingId,
                                version = current.editingVersion,
                                category = name,
                            ),
                        )
                    }
                }
            }.onSuccess {
                _state.value = _state.value.copy(draftName = "", editingId = null, editingVersion = null)
                refresh()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    conflict = error is ConflictException,
                    error = error.message ?: "Fehler",
                )
            }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            runCatching { api.deleteExpenseCategory(id) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "Fehler") }
        }
    }

    fun deleteIncome(id: String) {
        viewModelScope.launch {
            runCatching { api.deleteIncomeCategory(id) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "Fehler") }
        }
    }
}
