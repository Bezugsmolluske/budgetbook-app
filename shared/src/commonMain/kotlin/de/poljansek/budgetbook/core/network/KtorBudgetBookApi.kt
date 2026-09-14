package de.poljansek.budgetbook.core.network

import de.poljansek.budgetbook.core.network.dto.BackupRestoreDto
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.network.dto.CategoryDtoCreate
import de.poljansek.budgetbook.core.network.dto.ErrorDto
import de.poljansek.budgetbook.core.network.dto.TransactionDto
import de.poljansek.budgetbook.core.network.dto.TransactionSummaryDto
import de.poljansek.budgetbook.core.network.dto.TransactionWriteDto
import de.poljansek.budgetbook.core.network.dto.TransactionsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class KtorBudgetBookApi(
    private val client: HttpClient,
    private val baseUrlProvider: () -> String,
) : BudgetBookApi {

    private val errorJson = Json { ignoreUnknownKeys = true }

    private fun base(): String = baseUrlProvider().trimEnd('/')

    override suspend fun getSummary(
        from: String?,
        to: String?,
        type: BookType?,
        categoryId: String?,
    ): TransactionSummaryDto {
        val response = client.get("${base()}/transactions/summary") {
            if (from != null) parameter("from", from)
            if (to != null) parameter("to", to)
            if (type != null) parameter("type", type.name)
            if (categoryId != null) parameter("categoryId", categoryId)
        }
        return response.bodyOrThrow()
    }

    override suspend fun getTransactions(
        from: String?,
        to: String?,
        type: BookType?,
        categoryId: String?,
    ): TransactionsDto {
        val response = client.get("${base()}/transactions") {
            if (from != null) parameter("from", from)
            if (to != null) parameter("to", to)
            if (type != null) parameter("type", type.name)
            if (categoryId != null) parameter("categoryId", categoryId)
        }
        return response.bodyOrThrow()
    }

    override suspend fun getTransaction(id: String): TransactionDto =
        client.get("${base()}/transactions/$id").bodyOrThrow()

    override suspend fun createTransaction(body: TransactionWriteDto): TransactionDto =
        client.post("${base()}/transactions") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun updateTransaction(id: String, body: TransactionWriteDto): TransactionDto =
        client.put("${base()}/transactions/$id") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun deleteTransaction(id: String) {
        client.delete("${base()}/transactions/$id").ensureSuccess()
    }

    override suspend fun getCategories(type: BookType?): List<CategoryDto> {
        val response = client.get("${base()}/categories") {
            if (type != null) parameter("type", type.name)
        }
        return response.bodyOrThrow()
    }

    override suspend fun createCategory(body: CategoryDtoCreate): CategoryDto =
        client.post("${base()}/categories") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun updateCategory(id: String, body: CategoryDto): CategoryDto =
        client.put("${base()}/categories/$id") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun deleteCategory(id: String) {
        client.delete("${base()}/categories/$id").ensureSuccess()
    }

    override suspend fun downloadJsonBackup(): ByteArray =
        client.get("${base()}/backup/json").bytesOrThrow()

    override suspend fun downloadCsvBackup(): ByteArray =
        client.get("${base()}/backup/csv").bytesOrThrow()

    override suspend fun restoreJsonBackup(bytes: ByteArray, fileName: String): BackupRestoreDto =
        uploadBackup("${base()}/backup/json", bytes, fileName, ContentType.Application.Json)

    override suspend fun restoreCsvBackup(bytes: ByteArray, fileName: String): BackupRestoreDto =
        uploadBackup("${base()}/backup/csv", bytes, fileName, ContentType.parse("text/csv"))

    private suspend fun uploadBackup(
        url: String,
        bytes: ByteArray,
        fileName: String,
        contentType: ContentType,
    ): BackupRestoreDto {
        val response = client.submitFormWithBinaryData(
            url = url,
            formData = formData {
                append(
                    "file",
                    bytes,
                    Headers.build {
                        append(HttpHeaders.ContentType, contentType.toString())
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    },
                )
            },
        ) {
            parameter("replace", true)
        }
        return response.bodyOrThrow()
    }

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        ensureSuccess()
        return body()
    }

    private suspend fun HttpResponse.bytesOrThrow(): ByteArray {
        ensureSuccess()
        return body()
    }

    private suspend fun HttpResponse.ensureSuccess() {
        when (status) {
            HttpStatusCode.OK, HttpStatusCode.Created, HttpStatusCode.NoContent -> Unit
            HttpStatusCode.Conflict -> throw ConflictException(safeMessage())
            else -> throw ApiException(status.value, safeMessage())
        }
    }

    private suspend fun HttpResponse.safeMessage(): String {
        val text = runCatching { bodyAsText() }.getOrNull().orEmpty().trim()
        if (text.startsWith("{")) {
            runCatching { errorJson.decodeFromString<ErrorDto>(text).message }.getOrNull()
                ?.let { return it }
        }
        return text.ifBlank { "HTTP ${status.value}" }
    }
}
