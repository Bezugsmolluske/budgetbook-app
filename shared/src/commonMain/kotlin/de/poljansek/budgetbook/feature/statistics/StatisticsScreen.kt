package de.poljansek.budgetbook.feature.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.money.formatMoney
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.SimpleDropdown
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val barColor = MaterialTheme.colorScheme.primary
    val kindCategories = state.categories.filter { it.type == state.kind }
    val selectedName = kindCategories.firstOrNull { it.id == state.categoryId }?.name
        ?: kindCategories.firstOrNull()?.name.orEmpty()

    Scaffold(topBar = { TopAppBar(title = { Text("Statistik") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(
                Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.kind == BookType.EXPENSE,
                    onClick = { viewModel.setKind(BookType.EXPENSE) },
                    label = { Text("Ausgaben") },
                )
                FilterChip(
                    selected = state.kind == BookType.INCOME,
                    onClick = { viewModel.setKind(BookType.INCOME) },
                    label = { Text("Einkommen") },
                )
            }
            if (kindCategories.isNotEmpty()) {
                SimpleDropdown(
                    label = "Kategorie",
                    options = kindCategories.map { it.name },
                    selected = selectedName,
                    onSelected = viewModel::setCategoryName,
                )
            }
            when {
                state.loading -> LoadingState()
                state.error != null -> ErrorState(state.error ?: "Fehler", onRetry = viewModel::refresh)
                else -> {
                    val statistic = state.statistic
                    if (statistic == null) {
                        Text("Keine Daten")
                    } else {
                        val total = if (state.kind == BookType.EXPENSE) statistic.expensesTotal else statistic.incomesTotal
                        Text(
                            "Durchschnitt: ${formatMoney(statistic.mean, statistic.currency)}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                        Text(
                            "Gesamt: ${formatMoney(total, statistic.currency)} · ${statistic.buckets.size} Monate",
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                        val values = statistic.buckets.map {
                            if (state.kind == BookType.EXPENSE) it.expenses else it.incomes
                        }
                        if (values.isNotEmpty()) {
                            Canvas(Modifier.fillMaxWidth().height(180.dp).padding(bottom = 16.dp)) {
                                val max = (values.maxOrNull() ?: 1L).coerceAtLeast(1L).toFloat()
                                val barWidth = size.width / values.size
                                values.forEachIndexed { index, value ->
                                    val height = (value.toFloat() / max) * size.height
                                    drawRect(
                                        color = barColor,
                                        topLeft = Offset(index * barWidth + 2f, size.height - height),
                                        size = Size((barWidth - 4f).coerceAtLeast(1f), height),
                                    )
                                }
                            }
                        }
                        LazyColumn {
                            items(statistic.buckets) { month ->
                                val amount = if (state.kind == BookType.EXPENSE) month.expenses else month.incomes
                                Text(
                                    "${month.period}    ${formatMoney(amount, statistic.currency)}",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}
