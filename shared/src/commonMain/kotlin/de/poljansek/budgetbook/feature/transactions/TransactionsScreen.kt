package de.poljansek.budgetbook.feature.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.files.pickBackupFile
import de.poljansek.budgetbook.core.money.formatMoney
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.network.dto.TransactionDto
import de.poljansek.budgetbook.core.ui.ConfirmDialog
import de.poljansek.budgetbook.core.ui.DateRangeField
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.MonthYearSelector
import de.poljansek.budgetbook.core.ui.RefreshOnEnter
import de.poljansek.budgetbook.core.ui.SimpleDropdown
import de.poljansek.budgetbook.core.ui.formatIsoDate
import de.poljansek.budgetbook.feature.importing.ImportDialog
import de.poljansek.budgetbook.feature.importing.ImportedBooking
import de.poljansek.budgetbook.feature.importing.parseMlpCsv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onAdd: (BookType) -> Unit,
    onEdit: (String) -> Unit,
    viewModel: TransactionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::refresh)
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<TransactionDto?>(null) }
    var importBookings by remember { mutableStateOf<List<ImportedBooking>?>(null) }
    var importError by remember { mutableStateOf<String?>(null) }
    val selectedCategory = state.categories.firstOrNull { it.id == state.categoryId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buchungen") },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val file = pickBackupFile("csv") ?: return@launch
                                runCatching {
                                    withContext(Dispatchers.Default) { parseMlpCsv(file.bytes) }
                                }.onSuccess { rows ->
                                    importError = null
                                    importBookings = rows
                                }.onFailure { error ->
                                    importError = error.message ?: "Die Datei konnte nicht gelesen werden."
                                }
                            }
                        },
                    ) { Text("Importieren") }
                },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddBookingButton(
                    label = "Einkommen",
                    containerColor = IncomeButton,
                    onClick = { onAdd(BookType.INCOME) },
                )
                AddBookingButton(
                    label = "Ausgabe",
                    containerColor = ExpenseButton,
                    onClick = { onAdd(BookType.EXPENSE) },
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
        ) {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.mode == TransactionFilterMode.MonthYear,
                        onClick = { viewModel.setMode(TransactionFilterMode.MonthYear) },
                        label = { Text("Monat") },
                    )
                    FilterChip(
                        selected = state.mode == TransactionFilterMode.DateRange,
                        onClick = { viewModel.setMode(TransactionFilterMode.DateRange) },
                        label = { Text("Zeitraum") },
                    )
                }
                if (state.mode == TransactionFilterMode.MonthYear) {
                    MonthYearSelector(
                        month = state.month,
                        year = state.year,
                        onMonthChange = viewModel::setMonth,
                        onYearChange = viewModel::setYear,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                } else {
                    DateRangeField(
                        startDate = state.startDate,
                        endDate = state.endDate,
                        onRangeSelected = viewModel::setDateRange,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    )
                }
                importError?.let { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                SimpleDropdown(
                    label = "Kategorie",
                    options = listOf<CategoryDto?>(null) + state.categories,
                    selected = selectedCategory,
                    onSelected = viewModel::setCategory,
                    display = { category ->
                        category?.let { "${it.name} (${it.type.displayName()})" } ?: ALL_CATEGORIES_LABEL
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
            when {
                state.loading -> LoadingState()
                state.error != null -> ErrorState(state.error ?: "Fehler", onRetry = viewModel::refresh)
                else -> {
                    val data = state.data
                    val sum = data?.sum ?: 0L
                    val colors = amountColors()
                    Text(
                        text = "Summe: ${formatMoney(sum, data?.currency ?: "EUR")}",
                        color = if (sum >= 0) colors.income else colors.expense,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 72.dp),
                    ) {
                        items(data?.items.orEmpty(), key = { it.id ?: it.hashCode().toString() }) { item ->
                            TransactionRow(
                                item = item,
                                onClick = { item.id?.let(onEdit) },
                                onDelete = { pendingDelete = item },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { item ->
        ConfirmDialog(
            title = "Buchung löschen?",
            message = "${item.description} (${item.signedAmountLabel()})",
            onConfirm = {
                item.id?.let(viewModel::delete)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    importBookings?.let { bookings ->
        ImportDialog(
            bookings = bookings,
            onDismiss = { savedCount ->
                importBookings = null
                if (savedCount > 0) viewModel.refresh()
            },
        )
    }
}

@Composable
private fun AddBookingButton(
    label: String,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
        ),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun TransactionRow(
    item: TransactionDto,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = amountColors()
    val amountColor = when (item.type) {
        BookType.INCOME -> colors.income
        BookType.EXPENSE -> colors.expense
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.description, fontWeight = FontWeight.Medium)
            Text(
                "${formatIsoDate(item.date)} · ${item.category.name} · ${item.type.displayName()}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            item.signedAmountLabel(),
            color = amountColor,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Löschen")
        }
    }
}

internal fun BookType.displayName(): String = when (this) {
    BookType.EXPENSE -> "Ausgabe"
    BookType.INCOME -> "Einkommen"
}

private val IncomeLight = Color(0xFF2E7D32)
private val ExpenseLight = Color(0xFFC62828)
private val IncomeDark = Color(0xFFA5D6A7)
private val ExpenseDark = Color(0xFFEF9A9A)
private val IncomeButton = Color(0xFF2E7D32)
private val ExpenseButton = Color(0xFFC62828)

private data class AmountColors(val income: Color, val expense: Color)

@Composable
private fun amountColors(): AmountColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return if (dark) AmountColors(IncomeDark, ExpenseDark) else AmountColors(IncomeLight, ExpenseLight)
}

private fun TransactionDto.signedAmountLabel(): String {
    val formatted = formatMoney(amount, currency)
    return when (type) {
        BookType.INCOME -> "+$formatted"
        BookType.EXPENSE -> "−$formatted"
    }
}
