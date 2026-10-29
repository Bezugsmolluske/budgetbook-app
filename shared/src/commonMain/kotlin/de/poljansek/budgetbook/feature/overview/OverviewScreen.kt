package de.poljansek.budgetbook.feature.overview

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.money.formatMoney
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.RefreshOnEnter
import de.poljansek.budgetbook.core.ui.monthLabel
import org.koin.compose.viewmodel.koinViewModel

private val IncomeLight = Color(0xFF2E7D32)
private val ExpenseLight = Color(0xFFC62828)
private val IncomeDark = Color(0xFFA5D6A7)
private val ExpenseDark = Color(0xFFEF9A9A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(viewModel: OverviewViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::refresh)
    Scaffold(
        topBar = { TopAppBar(title = { Text("Übersicht") }) },
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(
                message = state.error ?: "Fehler",
                onRetry = viewModel::refresh,
                modifier = Modifier.padding(padding),
            )
            else -> {
                val scrollState = rememberScrollState()
                Box(Modifier.padding(padding).fillMaxSize()) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (state.years.isEmpty()) {
                            Text("Keine Übersicht vorhanden.")
                        } else {
                            state.years.forEach { year ->
                                YearlyTable(year)
                            }
                        }
                    }
                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(scrollState),
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

@Composable
private fun YearlyTable(year: YearlyOverviewUi) {
    Card(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = year.year.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            HeaderRow()
            year.months.forEach { month ->
                DataRow(
                    label = monthLabel(month.month),
                    incomes = month.incomes,
                    expenses = month.expenses,
                    sum = month.sum,
                    currency = month.currency,
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            DataRow(
                label = "Gesamt",
                incomes = year.incomesSum,
                expenses = year.expensesSum,
                sum = year.sum,
                currency = year.currency,
                emphasize = true,
            )
        }
    }
}

@Composable
private fun HeaderRow() {
    val headerColor = MaterialTheme.colorScheme.onSurfaceVariant
    val headerStyle = MaterialTheme.typography.labelLarge
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Monat", Modifier.weight(1f), color = headerColor, style = headerStyle)
        Text(
            "Einkommen",
            Modifier.weight(1f),
            color = headerColor,
            style = headerStyle,
            textAlign = TextAlign.End,
        )
        Text(
            "Ausgaben",
            Modifier.weight(1f),
            color = headerColor,
            style = headerStyle,
            textAlign = TextAlign.End,
        )
        Text(
            "Summe",
            Modifier.weight(1f),
            color = headerColor,
            style = headerStyle,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun DataRow(
    label: String,
    incomes: Long,
    expenses: Long,
    sum: Long,
    currency: String,
    emphasize: Boolean = false,
) {
    val colors = amountColors()
    val weight = if (emphasize) FontWeight.Bold else FontWeight.Normal
    val sumColor = if (sum >= 0) colors.income else colors.expense
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                if (emphasize) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f), fontWeight = weight)
        Text(
            formatMoney(incomes, currency),
            Modifier.weight(1f),
            color = colors.income,
            fontWeight = weight,
            textAlign = TextAlign.End,
        )
        Text(
            formatMoney(expenses, currency),
            Modifier.weight(1f),
            color = colors.expense,
            fontWeight = weight,
            textAlign = TextAlign.End,
        )
        Text(
            formatMoney(sum, currency),
            Modifier.weight(1f),
            color = sumColor,
            fontWeight = weight,
            textAlign = TextAlign.End,
        )
    }
}

private data class AmountColors(val income: Color, val expense: Color)

@Composable
private fun amountColors(): AmountColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return if (dark) AmountColors(IncomeDark, ExpenseDark) else AmountColors(IncomeLight, ExpenseLight)
}
