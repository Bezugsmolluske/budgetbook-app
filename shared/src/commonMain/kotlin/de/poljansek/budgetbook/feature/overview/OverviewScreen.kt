package de.poljansek.budgetbook.feature.overview

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.money.formatMoney
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.monthLabel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(viewModel: OverviewViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
            else -> Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                if (state.years.isEmpty()) {
                    Text("Keine Übersicht vorhanden.")
                } else {
                    state.years.forEach { year ->
                        YearlyTable(year)
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlyTable(year: YearlyOverviewUi) {
    Text(
        text = year.year.toString(),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
    )
    Column(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        HeaderRow()
        HorizontalDivider()
        year.months.forEach { month ->
            DataRow(
                label = monthLabel(month.month),
                incomes = month.incomes,
                expenses = month.expenses,
                sum = month.sum,
                currency = month.currency,
            )
        }
        HorizontalDivider()
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

@Composable
private fun HeaderRow() {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("Monat", Modifier.width(140.dp), fontWeight = FontWeight.Bold)
        Text("Einkommen", Modifier.width(140.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
        Text("Ausgaben", Modifier.width(140.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
        Text("Summe", Modifier.width(140.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
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
    val weight = if (emphasize) FontWeight.Bold else FontWeight.Normal
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, Modifier.width(140.dp), fontWeight = weight)
        Text(formatMoney(incomes, currency), Modifier.width(140.dp), textAlign = TextAlign.End, fontWeight = weight)
        Text(formatMoney(expenses, currency), Modifier.width(140.dp), textAlign = TextAlign.End, fontWeight = weight)
        Text(formatMoney(sum, currency), Modifier.width(140.dp), textAlign = TextAlign.End, fontWeight = weight)
    }
}
