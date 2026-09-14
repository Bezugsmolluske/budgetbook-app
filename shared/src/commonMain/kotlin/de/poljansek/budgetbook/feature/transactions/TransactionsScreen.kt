package de.poljansek.budgetbook.feature.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.money.formatMoney
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.network.dto.TransactionDto
import de.poljansek.budgetbook.core.ui.ConfirmDialog
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.MonthYearSelector
import de.poljansek.budgetbook.core.ui.SimpleDropdown
import de.poljansek.budgetbook.core.ui.formatIsoDate
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onAdd: (BookType) -> Unit,
    onEdit: (String) -> Unit,
    viewModel: TransactionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<TransactionDto?>(null) }
    val selectedCategory = state.categories.firstOrNull { it.id == state.categoryId }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Buchungen") }) },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                ExtendedFloatingActionButton(
                    onClick = { onAdd(BookType.INCOME) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Einkommen") },
                )
                ExtendedFloatingActionButton(
                    onClick = { onAdd(BookType.EXPENSE) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Ausgabe") },
                )
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
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
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = state.startDate,
                        onValueChange = viewModel::setStartDate,
                        label = { Text("Von (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = state.endDate,
                        onValueChange = viewModel::setEndDate,
                        label = { Text("Bis (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            SimpleDropdown(
                label = "Kategorie",
                options = listOf<CategoryDto?>(null) + state.categories,
                selected = selectedCategory,
                onSelected = viewModel::setCategory,
                display = { category ->
                    category?.let { "${it.name} (${it.type.displayName()})" } ?: ALL_CATEGORIES_LABEL
                },
                modifier = Modifier.padding(top = 12.dp),
            )
            when {
                state.loading -> LoadingState()
                state.error != null -> ErrorState(state.error ?: "Fehler", onRetry = viewModel::refresh)
                else -> {
                    val data = state.data
                    Text(
                        text = "Summe: ${formatMoney(data?.sum ?: 0, data?.currency ?: "EUR")}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 160.dp),
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
}

@Composable
private fun TransactionRow(
    item: TransactionDto,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.description, fontWeight = FontWeight.Medium)
            Text(
                "${formatIsoDate(item.date)} · ${item.category.name} · ${item.type.displayName()}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(item.signedAmountLabel(), modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Löschen")
        }
    }
}

internal fun BookType.displayName(): String = when (this) {
    BookType.EXPENSE -> "Ausgabe"
    BookType.INCOME -> "Einkommen"
}

private fun TransactionDto.signedAmountLabel(): String {
    val formatted = formatMoney(amount, currency)
    return when (type) {
        BookType.INCOME -> "+$formatted"
        BookType.EXPENSE -> "−$formatted"
    }
}
