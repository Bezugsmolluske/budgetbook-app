package de.poljansek.budgetbook.feature.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.config.AppConfig
import de.poljansek.budgetbook.core.money.formatMoney
import de.poljansek.budgetbook.core.network.dto.ExpenseDto
import de.poljansek.budgetbook.core.ui.ConfirmDialog
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.MonthYearSelector
import de.poljansek.budgetbook.core.ui.SimpleDropdown
import de.poljansek.budgetbook.core.ui.formatIsoDate
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: ExpensesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<ExpenseDto?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ausgaben") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.mode == ExpenseFilterMode.MonthYear,
                    onClick = { viewModel.setMode(ExpenseFilterMode.MonthYear) },
                    label = { Text("Monat") },
                )
                FilterChip(
                    selected = state.mode == ExpenseFilterMode.DateRange,
                    onClick = { viewModel.setMode(ExpenseFilterMode.DateRange) },
                    label = { Text("Zeitraum") },
                )
            }
            if (state.mode == ExpenseFilterMode.MonthYear) {
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
            val categoryNames = listOf(AppConfig.ALL_CATEGORIES) + state.categories.map { it.category }
            SimpleDropdown(
                label = "Kategorie",
                options = categoryNames,
                selected = state.category,
                onSelected = viewModel::setCategory,
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
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(data?.expenses.orEmpty(), key = { it.id ?: it.hashCode().toString() }) { expense ->
                            ExpenseRow(
                                expense = expense,
                                onClick = { expense.id?.let(onEdit) },
                                onDelete = { pendingDelete = expense },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { expense ->
        ConfirmDialog(
            title = "Ausgabe löschen?",
            message = "${expense.description} (${formatMoney(expense.amount, expense.currency)})",
            onConfirm = {
                expense.id?.let(viewModel::delete)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun ExpenseRow(
    expense: ExpenseDto,
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
            Text(expense.description, fontWeight = FontWeight.Medium)
            Text(
                "${formatIsoDate(expense.date)} · ${expense.category}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(formatMoney(expense.amount, expense.currency), modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Löschen")
        }
    }
}
