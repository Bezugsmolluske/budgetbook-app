package de.poljansek.budgetbook.core.network

import de.poljansek.budgetbook.core.network.dto.BackupDto
import de.poljansek.budgetbook.core.network.dto.CategoryDtoCreate
import de.poljansek.budgetbook.core.network.dto.ExpenseCategoryDto
import de.poljansek.budgetbook.core.network.dto.ExpenseDto
import de.poljansek.budgetbook.core.network.dto.ExpenseDtoCreate
import de.poljansek.budgetbook.core.network.dto.ExpenseStatisticDto
import de.poljansek.budgetbook.core.network.dto.ExpensesDto
import de.poljansek.budgetbook.core.network.dto.IncomeCategoryDto
import de.poljansek.budgetbook.core.network.dto.IncomeDto
import de.poljansek.budgetbook.core.network.dto.IncomeDtoCreate
import de.poljansek.budgetbook.core.network.dto.IncomesDto
import de.poljansek.budgetbook.core.network.dto.YearlyOverviewDto

interface BudgetBookApi {
    suspend fun getOverview(years: List<Int>? = null): List<YearlyOverviewDto>

    suspend fun getExpenses(
        month: Int? = null,
        year: Int? = null,
        category: String = "Alle",
        startDate: String? = null,
        endDate: String? = null,
    ): ExpensesDto

    suspend fun getExpense(id: String): ExpenseDto
    suspend fun createExpense(body: ExpenseDtoCreate): ExpenseDto
    suspend fun updateExpense(id: String, body: ExpenseDto): ExpenseDto
    suspend fun deleteExpense(id: String)

    suspend fun getIncomes(
        month: Int? = null,
        year: Int? = null,
        category: String = "Alle",
    ): IncomesDto

    suspend fun getIncome(id: String): IncomeDto
    suspend fun createIncome(body: IncomeDtoCreate): IncomeDto
    suspend fun updateIncome(id: String, body: IncomeDto): IncomeDto
    suspend fun deleteIncome(id: String)

    suspend fun getExpenseCategories(): List<ExpenseCategoryDto>
    suspend fun createExpenseCategory(body: CategoryDtoCreate): ExpenseCategoryDto
    suspend fun updateExpenseCategory(id: String, body: ExpenseCategoryDto): ExpenseCategoryDto
    suspend fun deleteExpenseCategory(id: String)

    suspend fun getIncomeCategories(): List<IncomeCategoryDto>
    suspend fun createIncomeCategory(body: CategoryDtoCreate): IncomeCategoryDto
    suspend fun updateIncomeCategory(id: String, body: IncomeCategoryDto): IncomeCategoryDto
    suspend fun deleteIncomeCategory(id: String)

    suspend fun getExpenseStatistics(category: String): ExpenseStatisticDto

    suspend fun downloadJsonBackup(): ByteArray
    suspend fun downloadCsvBackup(): ByteArray
    suspend fun restoreJsonBackup(bytes: ByteArray, fileName: String): String
    suspend fun restoreCsvBackup(bytes: ByteArray, fileName: String): String
}
