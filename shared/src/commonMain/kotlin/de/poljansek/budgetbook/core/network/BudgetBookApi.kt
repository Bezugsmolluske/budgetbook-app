package de.poljansek.budgetbook.core.network

import de.poljansek.budgetbook.core.network.dto.BackupRestoreDto
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.network.dto.CategoryDtoCreate
import de.poljansek.budgetbook.core.network.dto.TransactionDto
import de.poljansek.budgetbook.core.network.dto.TransactionSummaryDto
import de.poljansek.budgetbook.core.network.dto.TransactionWriteDto
import de.poljansek.budgetbook.core.network.dto.TransactionsDto

interface BudgetBookApi {
    suspend fun getSummary(
        from: String? = null,
        to: String? = null,
        type: BookType? = null,
        categoryId: String? = null,
    ): TransactionSummaryDto

    suspend fun getTransactions(
        from: String? = null,
        to: String? = null,
        type: BookType? = null,
        categoryId: String? = null,
    ): TransactionsDto

    suspend fun getTransaction(id: String): TransactionDto
    suspend fun createTransaction(body: TransactionWriteDto): TransactionDto
    suspend fun updateTransaction(id: String, body: TransactionWriteDto): TransactionDto
    suspend fun deleteTransaction(id: String)

    suspend fun getCategories(type: BookType? = null): List<CategoryDto>
    suspend fun createCategory(body: CategoryDtoCreate): CategoryDto
    suspend fun updateCategory(id: String, body: CategoryDto): CategoryDto
    suspend fun deleteCategory(id: String)

    suspend fun downloadJsonBackup(): ByteArray
    suspend fun downloadCsvBackup(): ByteArray
    suspend fun restoreJsonBackup(bytes: ByteArray, fileName: String): BackupRestoreDto
    suspend fun restoreCsvBackup(bytes: ByteArray, fileName: String): BackupRestoreDto
}
