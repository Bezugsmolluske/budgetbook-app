package de.poljansek.budgetbook.feature.incomes

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import de.poljansek.budgetbook.core.network.dto.IncomeDto
import de.poljansek.budgetbook.core.ui.ConfirmDialog
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.MonthYearSelector
import de.poljansek.budgetbook.core.ui.SimpleDropdown
import de.poljansek.budgetbook.core.ui.formatIsoDate
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomesScreen(
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: IncomesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<IncomeDto?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Einkommen") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            MonthYearSelector(
                month = state.month,
                year = state.year,
                onMonthChange = viewModel::setMonth,
                onYearChange = viewModel::setYear,
            )
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
                        items(data?.incomes.orEmpty(), key = { it.id ?: it.hashCode().toString() }) { income ->
                            Row(
                                Modifier.fillMaxWidth().clickable { income.id?.let(onEdit) }.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(income.description, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${formatIsoDate(income.date)} · ${income.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                Text(formatMoney(income.amount, income.currency), modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = { pendingDelete = income }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { income ->
        ConfirmDialog(
            title = "Einkommen löschen?",
            message = "${income.description} (${formatMoney(income.amount, income.currency)})",
            onConfirm = {
                income.id?.let(viewModel::delete)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}
