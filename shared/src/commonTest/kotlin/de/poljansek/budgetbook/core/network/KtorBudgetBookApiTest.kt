package de.poljansek.budgetbook.core.network

import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.TransactionWriteDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KtorBudgetBookApiTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun getSummaryParsesBuckets() = runTest {
        val engine = MockEngine {
            respond(
                content = """
                    {
                      "from": "2026-01-01",
                      "to": "2026-03-31",
                      "currency": "EUR",
                      "expensesTotal": 100,
                      "incomesTotal": 200,
                      "net": 100,
                      "mean": 33,
                      "buckets": [
                        {"period": "2026-01", "expenses": 100, "incomes": 200, "net": 100}
                      ]
                    }
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val summary = api(engine).getSummary(from = "2026-01-01", to = "2026-03-31")
        assertEquals(100, summary.net)
        assertEquals("2026-01", summary.buckets.single().period)
    }

    @Test
    fun createTransactionSendsCents() = runTest {
        val engine = MockEngine {
            respond(
                content = """
                    {
                      "id": "abc",
                      "version": 0,
                      "date": "2026-03-15",
                      "description": "Einkauf",
                      "amount": 4250,
                      "currency": "EUR",
                      "type": "EXPENSE",
                      "category": {"id": "cat-1", "name": "Lebensmittel", "type": "EXPENSE"}
                    }
                """.trimIndent(),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val created = api(engine).createTransaction(
            TransactionWriteDto(
                date = "2026-03-15",
                description = "Einkauf",
                amount = 4250,
                type = BookType.EXPENSE,
                categoryId = "cat-1",
            )
        )
        assertEquals("abc", created.id)
        assertEquals(4250, created.amount)
        assertEquals("Lebensmittel", created.category.name)
    }

    @Test
    fun updateTransactionMapsConflict() = runTest {
        val engine = MockEngine {
            respond(
                """{"code":"CONFLICT","message":"version mismatch"}""",
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        assertFailsWith<ConflictException> {
            api(engine).updateTransaction(
                "abc",
                TransactionWriteDto(
                    id = "abc",
                    version = 0,
                    date = "2026-03-15",
                    description = "Einkauf",
                    amount = 4250,
                    type = BookType.EXPENSE,
                    categoryId = "cat-1",
                )
            )
        }
    }

    private fun api(engine: MockEngine): KtorBudgetBookApi {
        val client = HttpClient(engine) {
            expectSuccess = false
            install(ContentNegotiation) { json(json) }
        }
        return KtorBudgetBookApi(client) { "http://localhost:8080" }
    }
}
