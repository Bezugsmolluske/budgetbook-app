package de.poljansek.budgetbook.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
enum class BookType { EXPENSE, INCOME }

@Serializable
data class CategoryRefDto(
    val id: String,
    val name: String,
    val type: BookType,
)

@Serializable
data class TransactionDto(
    val id: String? = null,
    val version: Long? = null,
    val date: String,
    val description: String,
    val amount: Long,
    val currency: String = "EUR",
    val type: BookType,
    val category: CategoryRefDto,
    val createdAt: String? = null,
    val lastEditedAt: String? = null,
)

@Serializable
data class TransactionWriteDto(
    val id: String? = null,
    val version: Long? = null,
    val date: String,
    val description: String,
    val amount: Long,
    val currency: String = "EUR",
    val type: BookType,
    val categoryId: String,
)

@Serializable
data class TransactionsDto(
    val sum: Long,
    val currency: String = "EUR",
    val items: List<TransactionDto> = emptyList(),
)

@Serializable
data class CategoryDto(
    val id: String? = null,
    val version: Long? = null,
    val name: String,
    val type: BookType,
    val createdAt: String? = null,
    val lastEditedAt: String? = null,
)

@Serializable
data class CategoryDtoCreate(
    val name: String,
    val type: BookType,
)

@Serializable
data class SummaryBucketDto(
    val period: String,
    val expenses: Long,
    val incomes: Long,
    val net: Long,
)

@Serializable
data class TransactionSummaryDto(
    val from: String,
    val to: String,
    val currency: String = "EUR",
    val expensesTotal: Long,
    val incomesTotal: Long,
    val net: Long,
    val mean: Long,
    val buckets: List<SummaryBucketDto> = emptyList(),
)

@Serializable
data class BackupRestoreDto(
    val filename: String? = null,
)

@Serializable
data class ErrorDto(
    val code: String,
    val message: String,
)
