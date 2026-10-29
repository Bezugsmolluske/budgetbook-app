package de.poljansek.budgetbook.feature.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.money.formatMoney
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.SummaryBucketDto
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.RefreshOnEnter
import de.poljansek.budgetbook.core.ui.SimpleDropdown
import de.poljansek.budgetbook.core.ui.monthLabel
import org.koin.compose.viewmodel.koinViewModel

private val ExpenseSelected = Color(0xFFC62828)
private val IncomeSelected = Color(0xFF2E7D32)
private val IncomeLight = Color(0xFF2E7D32)
private val ExpenseLight = Color(0xFFC62828)
private val IncomeDark = Color(0xFFA5D6A7)
private val ExpenseDark = Color(0xFFEF9A9A)
private val YAxisWidth = 104.dp
private val PlotHeight = 200.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::refresh)
    val kindCategories = state.categories.filter { it.type == state.kind }
    val selectedName = kindCategories.firstOrNull { it.id == state.categoryId }?.name
        ?: kindCategories.firstOrNull()?.name.orEmpty()
    val amountColor = amountColor(state.kind)

    Scaffold(topBar = { TopAppBar(title = { Text("Statistik") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = state.kind == BookType.EXPENSE,
                    onClick = { viewModel.setKind(BookType.EXPENSE) },
                    label = { Text("Ausgaben") },
                    colors = typeChipColors(ExpenseSelected),
                )
                FilterChip(
                    selected = state.kind == BookType.INCOME,
                    onClick = { viewModel.setKind(BookType.INCOME) },
                    label = { Text("Einkommen") },
                    colors = typeChipColors(IncomeSelected),
                )
                if (kindCategories.isNotEmpty()) {
                    SimpleDropdown(
                        label = "Kategorie",
                        options = kindCategories.map { it.name },
                        selected = selectedName,
                        onSelected = viewModel::setCategoryName,
                        modifier = Modifier.width(240.dp),
                    )
                }
            }
            when {
                state.loading -> LoadingState()
                state.error != null -> ErrorState(state.error ?: "Fehler", onRetry = viewModel::refresh)
                else -> {
                    val statistic = state.statistic
                    if (statistic == null) {
                        Text("Keine Daten", modifier = Modifier.padding(top = 16.dp))
                    } else {
                        val total = if (state.kind == BookType.EXPENSE) statistic.expensesTotal else statistic.incomesTotal
                        val buckets = statistic.buckets
                        Text(
                            "Durchschnitt ${formatMoney(statistic.mean, statistic.currency)}",
                            color = amountColor,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            "Gesamt ${formatMoney(total, statistic.currency)} · ${buckets.size} Monate",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                        if (buckets.isNotEmpty()) {
                            MonthlyBarChart(
                                buckets = buckets,
                                kind = state.kind,
                                currency = statistic.currency,
                                barColor = amountColor,
                            )
                        }
                        val listState = rememberLazyListState()
                        Box(Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp)) {
                            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                                items(buckets, key = { it.period }) { month ->
                                    val amount = if (state.kind == BookType.EXPENSE) month.expenses else month.incomes
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(formatYearMonth(month.period), modifier = Modifier.weight(1f))
                                        Text(formatMoney(amount, statistic.currency), color = amountColor)
                                    }
                                    HorizontalDivider()
                                }
                            }
                            VerticalScrollbar(
                                adapter = rememberScrollbarAdapter(listState),
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                style = ScrollbarStyle(
                                    minimalHeight = 24.dp,
                                    thickness = 8.dp,
                                    shape = RoundedCornerShape(4.dp),
                                    hoverDurationMillis = 300,
                                    unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyBarChart(
    buckets: List<SummaryBucketDto>,
    kind: BookType,
    currency: String,
    barColor: Color,
) {
    val values = buckets.map { if (kind == BookType.EXPENSE) it.expenses else it.incomes }
    val max = (values.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall
    val years = buckets.groupBy { it.period.take(4) }
    Column(Modifier.fillMaxWidth()) {
        Text("Betrag", color = labelColor, style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth().height(PlotHeight).padding(top = 4.dp)) {
            Column(
                Modifier.width(YAxisWidth).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                AxisAmount(max, currency, labelColor)
                AxisAmount(max / 2, currency, labelColor)
                AxisAmount(0L, currency, labelColor)
            }
            Canvas(Modifier.weight(1f).fillMaxHeight()) {
                listOf(0f, 0.5f, 1f).forEach { fraction ->
                    val y = size.height * (1f - fraction)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                    )
                }
                val slot = size.width / values.size
                values.forEachIndexed { index, value ->
                    val barHeight = (value.toFloat() / max) * size.height
                    val barWidth = (slot - 1f).coerceAtLeast(1f)
                    drawRect(
                        color = barColor,
                        topLeft = Offset(index * slot, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Spacer(Modifier.width(YAxisWidth))
            Row(Modifier.weight(1f)) {
                years.forEach { (year, months) ->
                    Text(
                        year,
                        color = labelColor,
                        style = labelStyle,
                        modifier = Modifier.weight(months.size.toFloat()),
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(YAxisWidth))
            Text("Monat", color = labelColor, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AxisAmount(cents: Long, currency: String, color: Color) {
    Text(
        formatMoney(cents, currency),
        color = color,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun typeChipColors(selected: Color) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = selected,
    selectedLabelColor = Color.White,
)

@Composable
private fun amountColor(kind: BookType): Color {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return when (kind) {
        BookType.EXPENSE -> if (dark) ExpenseDark else ExpenseLight
        BookType.INCOME -> if (dark) IncomeDark else IncomeLight
    }
}

private fun formatYearMonth(period: String): String {
    val parts = period.split("-")
    val month = parts.getOrNull(1)?.toIntOrNull() ?: return period
    val year = parts.getOrNull(0) ?: return period
    return "${monthLabel(month)} $year"
}
