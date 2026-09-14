package de.poljansek.budgetbook.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.config.AppConfig
import de.poljansek.budgetbook.core.money.centsToInput
import de.poljansek.budgetbook.core.money.parseMoneyToCents
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.ConflictException
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.network.dto.TransactionDto
import de.poljansek.budgetbook.core.network.dto.TransactionWriteDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionEditorUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val type: BookType = BookType.EXPENSE,
    val date: String = "",
    val description: String = "",
    val categoryId: String = "",
    val amountInput: String = "",
    val currency: String = AppConfig.DEFAULT_CURRENCY,
    val version: Long? = null,
    val categories: List<CategoryDto> = emptyList(),
    val error: String? = null,
    val conflict: Boolean = false,
    val saved: Boolean = false,
)

class TransactionEditorViewModel(
    private val initialType: BookType,
    private val transactionId: String,
    private val api: BudgetBookApi,
) : ViewModel() {
    private val isCreate: Boolean = transactionId.isBlank()
    private val _state = MutableStateFlow(TransactionEditorUiState(type = initialType))
    val state: StateFlow<TransactionEditorUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun updateType(type: BookType) {
        val matching = _state.value.categories.filter { it.type == type }
        _state.value = _state.value.copy(
            type = type,
            categoryId = matching.firstOrNull()?.id.orEmpty(),
        )
    }

    fun updateDate(value: String) { _state.value = _state.value.copy(date = value) }
    fun updateDescription(value: String) { _state.value = _state.value.copy(description = value) }
    fun updateCategoryName(name: String) {
        val matching = _state.value.categories.filter { it.type == _state.value.type }
        val id = matching.firstOrNull { it.name == name }?.id.orEmpty()
        _state.value = _state.value.copy(categoryId = id)
    }
    fun updateAmount(value: String) { _state.value = _state.value.copy(amountInput = value) }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, conflict = false)
            runCatching {
                val categories = api.getCategories()
                val existing = if (isCreate) null else api.getTransaction(transactionId)
                categories to existing
            }.onSuccess { (categories, existing) ->
                _state.value = if (existing == null) {
                    TransactionEditorUiState(
                        loading = false,
                        type = initialType,
                        categories = categories,
                        categoryId = categories.firstOrNull { it.type == initialType }?.id.orEmpty(),
                    )
                } else {
                    existing.toUi(categories)
                }
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Fehler")
            }
        }
    }

    fun save() {
        val current = _state.value
        val cents = parseMoneyToCents(current.amountInput)
        if (cents == null) {
            _state.value = current.copy(error = "Ungültiger Betrag")
            return
        }
        if (current.date.isBlank() || current.categoryId.isBlank() || current.description.isBlank()) {
            _state.value = current.copy(error = "Bitte alle Felder ausfüllen")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(saving = true, error = null, conflict = false)
            val body = TransactionWriteDto(
                id = transactionId.takeIf { !isCreate },
                version = current.version,
                date = current.date,
                description = current.description,
                amount = cents,
                currency = current.currency,
                type = current.type,
                categoryId = current.categoryId,
            )
            runCatching {
                if (isCreate) api.createTransaction(body.copy(id = null, version = null))
                else api.updateTransaction(transactionId, body)
            }.onSuccess {
                _state.value = _state.value.copy(saving = false, saved = true)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    saving = false,
                    conflict = error is ConflictException,
                    error = error.message ?: "Fehler",
                )
            }
        }
    }

    private fun TransactionDto.toUi(categories: List<CategoryDto>) = TransactionEditorUiState(
        loading = false,
        type = type,
        date = date,
        description = description,
        categoryId = category.id,
        amountInput = centsToInput(amount),
        currency = currency,
        version = version,
        categories = categories,
    )
}
