package de.poljansek.budgetbook.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.dto.SummaryBucketDto
import de.poljansek.budgetbook.core.network.dto.TransactionSummaryDto
import de.poljansek.budgetbook.core.ui.monthStart
import de.poljansek.budgetbook.core.ui.yearChoices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class YearlyOverviewUi(
    val year: Int,
    val expensesSum: Long,
    val incomesSum: Long,
    val sum: Long,
    val currency: String,
    val months: List<MonthlyOverviewUi>,
)

data class MonthlyOverviewUi(
    val month: Int,
    val expenses: Long,
    val incomes: Long,
    val sum: Long,
    val currency: String,
)

data class OverviewUiState(
    val loading: Boolean = true,
    val years: List<YearlyOverviewUi> = emptyList(),
    val error: String? = null,
)

class OverviewViewModel(
    private val api: BudgetBookApi,
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState())
    val state: StateFlow<OverviewUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val years = yearChoices()
            val from = monthStart(years.last(), 1)
            val to = "${years.first()}-12-31"
            runCatching { api.getSummary(from = from, to = to).toYearly() }
                .onSuccess { _state.value = OverviewUiState(loading = false, years = it) }
                .onFailure { _state.value = OverviewUiState(loading = false, error = it.message ?: "Fehler") }
        }
    }
}

internal fun TransactionSummaryDto.toYearly(): List<YearlyOverviewUi> =
    buckets.groupBy { it.period.take(4).toInt() }
        .map { (year, monthBuckets) ->
            YearlyOverviewUi(
                year = year,
                expensesSum = monthBuckets.sumOf { it.expenses },
                incomesSum = monthBuckets.sumOf { it.incomes },
                sum = monthBuckets.sumOf { it.net },
                currency = currency,
                months = monthBuckets.sortedBy { it.period }.map { it.toMonth(currency) },
            )
        }
        .sortedByDescending { it.year }

private fun SummaryBucketDto.toMonth(currency: String) = MonthlyOverviewUi(
    month = period.takeLast(2).toInt(),
    expenses = expenses,
    incomes = incomes,
    sum = net,
    currency = currency,
)
