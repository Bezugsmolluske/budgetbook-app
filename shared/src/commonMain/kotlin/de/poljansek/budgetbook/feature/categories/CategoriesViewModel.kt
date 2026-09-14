package de.poljansek.budgetbook.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.ConflictException
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.network.dto.CategoryDtoCreate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CategoryKind { Expense, Income }

fun CategoryKind.toBookType(): BookType = when (this) {
    CategoryKind.Expense -> BookType.EXPENSE
    CategoryKind.Income -> BookType.INCOME
}

data class CategoriesUiState(
    val loading: Boolean = true,
    val categories: List<CategoryDto> = emptyList(),
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

    fun startEdit(category: CategoryDto) {
        _state.value = _state.value.copy(
            kind = if (category.type == BookType.EXPENSE) CategoryKind.Expense else CategoryKind.Income,
            draftName = category.name,
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
            runCatching { api.getCategories() }
                .onSuccess { categories ->
                    _state.value = _state.value.copy(loading = false, categories = categories)
                }
                .onFailure {
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
        val type = current.kind.toBookType()
        viewModelScope.launch {
            _state.value = current.copy(error = null, conflict = false)
            runCatching {
                if (current.editingId == null) {
                    api.createCategory(CategoryDtoCreate(name, type))
                } else {
                    api.updateCategory(
                        current.editingId,
                        CategoryDto(
                            id = current.editingId,
                            version = current.editingVersion,
                            name = name,
                            type = type,
                        ),
                    )
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

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { api.deleteCategory(id) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "Fehler") }
        }
    }
}
