package de.poljansek.budgetbook.feature.incomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.config.AppConfig
import de.poljansek.budgetbook.core.money.centsToInput
import de.poljansek.budgetbook.core.money.parseMoneyToCents
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.ConflictException
import de.poljansek.budgetbook.core.network.dto.IncomeCategoryDto
import de.poljansek.budgetbook.core.network.dto.IncomeDto
import de.poljansek.budgetbook.core.network.dto.IncomeDtoCreate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IncomeEditorUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val date: String = "",
    val description: String = "",
    val category: String = "",
    val amountInput: String = "",
    val currency: String = AppConfig.DEFAULT_CURRENCY,
    val version: Long? = null,
    val categories: List<IncomeCategoryDto> = emptyList(),
    val error: String? = null,
    val conflict: Boolean = false,
    val saved: Boolean = false,
)

class IncomeEditorViewModel(
    private val incomeId: String,
    private val api: BudgetBookApi,
) : ViewModel() {
    private val isCreate: Boolean = incomeId.isBlank()
    private val _state = MutableStateFlow(IncomeEditorUiState())
    val state: StateFlow<IncomeEditorUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun updateDate(value: String) { _state.value = _state.value.copy(date = value) }
    fun updateDescription(value: String) { _state.value = _state.value.copy(description = value) }
    fun updateCategory(value: String) { _state.value = _state.value.copy(category = value) }
    fun updateAmount(value: String) { _state.value = _state.value.copy(amountInput = value) }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, conflict = false)
            runCatching {
                val categories = api.getIncomeCategories()
                val existing = if (isCreate) null else api.getIncome(incomeId)
                categories to existing
            }.onSuccess { (categories, existing) ->
                _state.value = if (existing == null) {
                    IncomeEditorUiState(
                        loading = false,
                        categories = categories,
                        category = categories.firstOrNull()?.category.orEmpty(),
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
        if (current.date.isBlank() || current.category.isBlank() || current.description.isBlank()) {
            _state.value = current.copy(error = "Bitte alle Felder ausfüllen")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(saving = true, error = null, conflict = false)
            runCatching {
                if (isCreate) {
                    api.createIncome(
                        IncomeDtoCreate(
                            date = current.date,
                            description = current.description,
                            category = current.category,
                            amount = cents,
                            currency = current.currency,
                        )
                    )
                } else {
                    api.updateIncome(
                        incomeId,
                        IncomeDto(
                            id = incomeId,
                            version = current.version,
                            date = current.date,
                            description = current.description,
                            category = current.category,
                            amount = cents,
                            currency = current.currency,
                        )
                    )
                }
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

    private fun IncomeDto.toUi(categories: List<IncomeCategoryDto>) = IncomeEditorUiState(
        loading = false,
        date = date,
        description = description,
        category = category,
        amountInput = centsToInput(amount),
        currency = currency,
        version = version,
        categories = categories,
    )
}
