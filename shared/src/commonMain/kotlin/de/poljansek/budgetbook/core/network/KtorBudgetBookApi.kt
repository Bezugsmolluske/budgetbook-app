package de.poljansek.budgetbook.core.network

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

class KtorBudgetBookApi(
    private val client: HttpClient,
    private val baseUrlProvider: () -> String,
) : BudgetBookApi {

    private fun base(): String = baseUrlProvider().trimEnd('/')

    override suspend fun getOverview(years: List<Int>?): List<YearlyOverviewDto> {
        val response = client.get("${base()}/overview") {
            years.orEmpty().forEach { parameter("years", it) }
        }
        return response.bodyOrThrow()
    }

    override suspend fun getExpenses(
        month: Int?,
        year: Int?,
        category: String,
        startDate: String?,
        endDate: String?,
    ): ExpensesDto {
        val response = client.get("${base()}/expenses") {
            if (startDate != null && endDate != null) {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            } else {
                if (month != null) parameter("month", month)
                if (year != null) parameter("year", year)
            }
            parameter("category", category)
        }
        return response.bodyOrThrow()
    }

    override suspend fun getExpense(id: String): ExpenseDto =
        client.get("${base()}/expenses/$id").bodyOrThrow()

    override suspend fun createExpense(body: ExpenseDtoCreate): ExpenseDto =
        client.post("${base()}/expenses") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun updateExpense(id: String, body: ExpenseDto): ExpenseDto =
        client.put("${base()}/expenses/$id") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun deleteExpense(id: String) {
        client.delete("${base()}/expenses/$id").ensureSuccess()
    }

    override suspend fun getIncomes(month: Int?, year: Int?, category: String): IncomesDto {
        val response = client.get("${base()}/incomes") {
            if (month != null) parameter("month", month)
            if (year != null) parameter("year", year)
            parameter("category", category)
        }
        return response.bodyOrThrow()
    }

    override suspend fun getIncome(id: String): IncomeDto =
        client.get("${base()}/incomes/$id").bodyOrThrow()

    override suspend fun createIncome(body: IncomeDtoCreate): IncomeDto =
        client.post("${base()}/incomes") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun updateIncome(id: String, body: IncomeDto): IncomeDto =
        client.put("${base()}/incomes/$id") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun deleteIncome(id: String) {
        client.delete("${base()}/incomes/$id").ensureSuccess()
    }

    override suspend fun getExpenseCategories(): List<ExpenseCategoryDto> =
        client.get("${base()}/expenseCategories").bodyOrThrow()

    override suspend fun createExpenseCategory(body: CategoryDtoCreate): ExpenseCategoryDto =
        client.post("${base()}/expenseCategories") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun updateExpenseCategory(id: String, body: ExpenseCategoryDto): ExpenseCategoryDto =
        client.put("${base()}/expenseCategories/$id") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun deleteExpenseCategory(id: String) {
        client.delete("${base()}/expenseCategories/$id").ensureSuccess()
    }

    override suspend fun getIncomeCategories(): List<IncomeCategoryDto> =
        client.get("${base()}/incomeCategories").bodyOrThrow()

    override suspend fun createIncomeCategory(body: CategoryDtoCreate): IncomeCategoryDto =
        client.post("${base()}/incomeCategories") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun updateIncomeCategory(id: String, body: IncomeCategoryDto): IncomeCategoryDto =
        client.put("${base()}/incomeCategories/$id") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyOrThrow()

    override suspend fun deleteIncomeCategory(id: String) {
        client.delete("${base()}/incomeCategories/$id").ensureSuccess()
    }

    override suspend fun getExpenseStatistics(category: String): ExpenseStatisticDto =
        client.get("${base()}/expenses/statistics") {
            parameter("category", category)
        }.bodyOrThrow()

    override suspend fun downloadJsonBackup(): ByteArray =
        client.get("${base()}/backup/json").bytesOrThrow()

    override suspend fun downloadCsvBackup(): ByteArray =
        client.get("${base()}/backup/csv").bytesOrThrow()

    override suspend fun restoreJsonBackup(bytes: ByteArray, fileName: String): String =
        uploadBackup("${base()}/backup/json", bytes, fileName, ContentType.Application.Json)

    override suspend fun restoreCsvBackup(bytes: ByteArray, fileName: String): String =
        uploadBackup("${base()}/backup/csv", bytes, fileName, ContentType.parse("text/csv"))

    private suspend fun uploadBackup(
        url: String,
        bytes: ByteArray,
        fileName: String,
        contentType: ContentType,
    ): String {
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
        )
        return response.bodyAsTextOrThrow()
    }
}

private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
    ensureSuccess()
    return body()
}

private suspend fun HttpResponse.bytesOrThrow(): ByteArray {
    ensureSuccess()
    return body()
}

private suspend fun HttpResponse.bodyAsTextOrThrow(): String {
    ensureSuccess()
    return bodyAsText()
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
    return text.ifBlank { "HTTP ${status.value}" }
}
