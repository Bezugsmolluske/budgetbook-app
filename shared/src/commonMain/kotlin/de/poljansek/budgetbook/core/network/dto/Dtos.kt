package de.poljansek.budgetbook.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
    val id: String? = null,
    val version: Long? = null,
    val date: String,
    val description: String,
    val category: String,
    val amount: Long,
    val currency: String = "EUR",
    val createdAt: String? = null,
    val lastEditedAt: String? = null,
)

@Serializable
data class ExpenseDtoCreate(
    val date: String,
    val description: String,
    val category: String,
    val amount: Long,
    val currency: String = "EUR",
)

@Serializable
data class ExpensesDto(
    val sum: Long,
    val currency: String = "EUR",
    val expenses: List<ExpenseDto> = emptyList(),
)

@Serializable
data class IncomeDto(
    val id: String? = null,
    val version: Long? = null,
    val date: String,
    val description: String,
    val category: String,
    val amount: Long,
    val currency: String = "EUR",
    val createdAt: String? = null,
    val lastEditedAt: String? = null,
)

@Serializable
data class IncomeDtoCreate(
    val date: String,
    val description: String,
    val category: String,
    val amount: Long,
    val currency: String = "EUR",
)

@Serializable
data class IncomesDto(
    val sum: Long,
    val currency: String = "EUR",
    val incomes: List<IncomeDto> = emptyList(),
)

@Serializable
data class ExpenseCategoryDto(
    val id: String? = null,
    val version: Long? = null,
    val category: String,
    val createdAt: String? = null,
    val lastEditedAt: String? = null,
)

@Serializable
data class IncomeCategoryDto(
    val id: String? = null,
    val version: Long? = null,
    val category: String,
    val createdAt: String? = null,
    val lastEditedAt: String? = null,
)

@Serializable
data class CategoryDtoCreate(
    val category: String,
)

@Serializable
data class SumDto(
    val amount: Long,
    val currency: String = "EUR",
)

@Serializable
data class MonthlyOverviewDto(
    val month: Int,
    val expenses: Long,
    val incomes: Long,
    val sum: Long,
    val currency: String = "EUR",
)

@Serializable
data class YearlyOverviewDto(
    val year: Int,
    val expensesSum: Long,
    val incomesSum: Long,
    val sum: Long,
    val currency: String = "EUR",
    val monthlyOverviews: List<MonthlyOverviewDto> = emptyList(),
)

@Serializable
data class MonthlyExpenseDto(
    val date: String,
    val amount: Long,
    val currency: String = "EUR",
)

@Serializable
data class ExpenseStatisticDto(
    val totalSum: Long,
    val meanExpense: Long,
    val numberOfMonth: Long,
    val currency: String = "EUR",
    val monthlyExpenses: List<MonthlyExpenseDto> = emptyList(),
)

@Serializable
data class BackupDto(
    val expenseDtos: List<ExpenseDto> = emptyList(),
    val expenseCategoryDtos: List<ExpenseCategoryDto> = emptyList(),
    val incomeDtos: List<IncomeDto> = emptyList(),
    val incomeCategoryDtos: List<IncomeCategoryDto> = emptyList(),
)
