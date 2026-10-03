package de.poljansek.budgetbook.feature.importing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.money.amountInputToCents
import de.poljansek.budgetbook.core.money.applyAmountInput
import de.poljansek.budgetbook.core.money.centsToAmountInput
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.network.dto.TransactionDto
import de.poljansek.budgetbook.core.network.dto.TransactionWriteDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

data class ImportRow(
    val key: Int,
    val date: String,
    val amount: SignedAmountInput,
    val heldType: BookType,
    val currency: String,
    val description: String,
    val source: String,
    val categoryId: String,
    val savedId: String? = null,
    val savedVersion: Long? = null,
    val editing: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
) {
    val type: BookType
        get() = amount.bookType() ?: heldType

    val locked: Boolean
        get() = savedId != null && !editing

    fun isComplete(): Boolean {
        val cents = amountInputToCents(amount.digits) ?: 0L
        return date.isNotBlank() && description.isNotBlank() && categoryId.isNotBlank() && cents > 0L
    }
}

data class ImportUiState(
    val loading: Boolean = false,
    val total: Int = 0,
    val removed: Int = 0,
    val rows: List<ImportRow> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val existing: List<ExistingBooking> = emptyList(),
    val changed: Boolean = false,
    val error: String? = null,
) {
    val saved: Int
        get() = rows.count { it.savedId != null }

    val duplicates: Int
        get() = rows.count { row ->
            val cents = amountInputToCents(row.amount.digits) ?: 0L
            matchingDuplicates(row.date, cents, row.type, existing)
                .any { it.id == null || it.id != row.savedId }
        }
}

class ImportViewModel(
    private val api: BudgetBookApi,
) : ViewModel() {
    private val _state = MutableStateFlow(ImportUiState())
    val state: StateFlow<ImportUiState> = _state.asStateFlow()
    private var source: List<ImportedBooking> = emptyList()
    private var loadJob: Job? = null

    fun start(bookings: List<ImportedBooking>) {
        source = bookings
        loadJob?.cancel()
        _state.value = ImportUiState(loading = true, total = bookings.size, rows = bookings.toRows())
        loadJob = viewModelScope.launch {
            if (bookings.isEmpty()) {
                _state.value = _state.value.copy(loading = false, error = "Keine Buchungen in der Datei.")
                return@launch
            }
            try {
                val categories = api.getCategories()
                val from = bookings.minOf { it.date }
                val to = bookings.maxOf { it.date }
                val existing = api.getTransactions(from = from, to = to).items.map { it.toExistingBooking() }
                _state.value = _state.value.copy(
                    loading = false,
                    categories = categories,
                    existing = existing,
                    error = null,
                )
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (error: Exception) {
                _state.value = _state.value.copy(loading = false, error = error.message ?: "Fehler")
            }
        }
    }

    fun retry() {
        start(source)
    }

    fun updateDate(key: Int, date: String) = update(key) { it.copy(date = date) }

    fun updateDescription(key: Int, description: String) = update(key) { it.copy(description = description) }

    fun updateAmount(key: Int, proposed: String) = update(key) { row ->
        row.editedAmount(proposed)
    }

    fun updateType(key: Int, type: BookType) = update(key) { row ->
        row.withType(type, _state.value.categories)
    }

    fun discard(key: Int) {
        val row = _state.value.rows.firstOrNull { it.key == key } ?: return
        if (row.saving || row.savedId != null) return
        _state.value = _state.value.copy(
            rows = _state.value.rows.filter { it.key != key },
            removed = _state.value.removed + 1,
        )
    }

    fun edit(key: Int) {
        mapRow(key) { it.copy(editing = true, error = null) }
    }

    fun updateCategory(key: Int, name: String) = update(key) { row ->
        val id = _state.value.categories
            .firstOrNull { it.type == row.type && it.name == name }
            ?.id
            .orEmpty()
        row.copy(categoryId = id)
    }

    fun save(key: Int) {
        val row = _state.value.rows.firstOrNull { it.key == key } ?: return
        if (row.saving || row.locked || !row.isComplete()) return
        val categories = _state.value.categories
        viewModelScope.launch {
            mapRow(key) { it.copy(saving = true, error = null) }
            val cents = amountInputToCents(row.amount.digits) ?: 0L
            val body = TransactionWriteDto(
                id = row.savedId,
                version = row.savedVersion,
                date = row.date,
                description = row.description.trim(),
                amount = cents,
                currency = row.currency.ifBlank { "EUR" },
                type = row.type,
                categoryId = row.categoryId,
            )
            try {
                val saved = if (row.savedId == null) {
                    api.createTransaction(body.copy(id = null, version = null))
                } else {
                    api.updateTransaction(row.savedId, body)
                }
                val id = saved.id
                if (id.isNullOrBlank()) {
                    mapRow(key) { it.copy(saving = false, error = "Die Buchung wurde ohne Kennung gespeichert.") }
                    return@launch
                }
                val categoryName = categories.firstOrNull { it.id == row.categoryId }?.name.orEmpty()
                val booking = ExistingBooking(
                    date = row.date,
                    amountCents = cents,
                    type = row.type,
                    description = row.description.trim(),
                    categoryName = categoryName,
                    currency = row.currency.ifBlank { "EUR" },
                    id = id,
                )
                val latest = _state.value
                _state.value = latest.copy(
                    changed = true,
                    existing = latest.existing.filter { it.id != id } + booking,
                    rows = latest.rows.map { current ->
                        if (current.key != key) current
                        else current.copy(
                            savedId = id,
                            savedVersion = saved.version,
                            editing = false,
                            saving = false,
                            error = null,
                        )
                    },
                )
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (error: Exception) {
                mapRow(key) { it.copy(saving = false, error = error.message ?: "Fehler") }
            }
        }
    }

    fun delete(key: Int) {
        val row = _state.value.rows.firstOrNull { it.key == key } ?: return
        val id = row.savedId ?: return
        if (row.saving) return
        viewModelScope.launch {
            mapRow(key) { it.copy(saving = true, error = null) }
            try {
                api.deleteTransaction(id)
                val latest = _state.value
                _state.value = latest.copy(
                    changed = true,
                    removed = latest.removed + 1,
                    rows = latest.rows.filter { it.key != key },
                    existing = latest.existing.filter { it.id != id },
                )
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (error: Exception) {
                mapRow(key) { it.copy(saving = false, error = error.message ?: "Fehler") }
            }
        }
    }

    private fun update(key: Int, block: (ImportRow) -> ImportRow) {
        val row = _state.value.rows.firstOrNull { it.key == key } ?: return
        if (row.saving || row.locked) return
        mapRow(key) { block(it).copy(error = null) }
    }

    private fun mapRow(key: Int, block: (ImportRow) -> ImportRow) {
        _state.value = _state.value.copy(
            rows = _state.value.rows.map { row -> if (row.key == key) block(row) else row },
        )
    }
}

private fun List<ImportedBooking>.toRows(): List<ImportRow> = mapIndexed { index, booking ->
    ImportRow(
        key = index,
        date = booking.date,
        amount = SignedAmountInput(
            negative = booking.signedCents < 0,
            digits = centsToAmountInput(abs(booking.signedCents)),
        ),
        heldType = booking.type,
        currency = booking.currency,
        description = booking.description,
        source = booking.source,
        categoryId = "",
    )
}

private fun TransactionDto.toExistingBooking() = ExistingBooking(
    date = date.take(10),
    amountCents = amount,
    type = type,
    description = description,
    categoryName = category.name,
    currency = currency,
    id = id,
)

internal fun ImportRow.editedAmount(proposed: String): ImportRow {
    val digits = applyAmountInput(amount.digits, proposed)
    val cents = amountInputToCents(digits) ?: 0L
    return copy(
        amount = SignedAmountInput(
            negative = heldType == BookType.EXPENSE && cents > 0L,
            digits = digits,
        ),
    )
}

internal fun ImportRow.withType(type: BookType, categories: List<CategoryDto>): ImportRow =
    copy(
        amount = amount.copy(negative = type == BookType.EXPENSE),
        heldType = type,
        categoryId = categoryId.keptFor(type, categories),
    )

private fun String.keptFor(type: BookType, categories: List<CategoryDto>): String {
    val category = categories.firstOrNull { it.id == this } ?: return ""
    return if (category.type == type) this else ""
}
