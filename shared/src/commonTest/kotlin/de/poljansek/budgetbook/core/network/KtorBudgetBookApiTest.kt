package de.poljansek.budgetbook.core.network

import de.poljansek.budgetbook.core.network.dto.ExpenseDtoCreate
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
    fun getOverviewParsesYearlyList() = runTest {
        val engine = MockEngine {
            respond(
                content = """
                    [{
                      "year": 2026,
                      "expensesSum": 100,
                      "incomesSum": 200,
                      "sum": 100,
                      "currency": "EUR",
                      "monthlyOverviews": [
                        {"month": 1, "expenses": 100, "incomes": 200, "sum": 100, "currency": "EUR"}
                      ]
                    }]
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val api = api(engine)
        val overview = api.getOverview()
        assertEquals(2026, overview.single().year)
        assertEquals(100, overview.single().monthlyOverviews.single().expenses)
    }

    @Test
    fun createExpenseSendsCents() = runTest {
        val engine = MockEngine {
            respond(
                content = """
                    {
                      "id": "abc",
                      "version": 0,
                      "date": "2026-03-15",
                      "description": "Einkauf",
                      "category": "Lebensmittel",
                      "amount": 4250,
                      "currency": "EUR"
                    }
                """.trimIndent(),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val created = api(engine).createExpense(
            ExpenseDtoCreate(
                date = "2026-03-15",
                description = "Einkauf",
                category = "Lebensmittel",
                amount = 4250,
            )
        )
        assertEquals("abc", created.id)
        assertEquals(4250, created.amount)
    }

    @Test
    fun updateExpenseMapsConflict() = runTest {
        val engine = MockEngine {
            respond("version mismatch", status = HttpStatusCode.Conflict)
        }
        assertFailsWith<ConflictException> {
            api(engine).updateExpense(
                "abc",
                de.poljansek.budgetbook.core.network.dto.ExpenseDto(
                    id = "abc",
                    version = 0,
                    date = "2026-03-15",
                    description = "Einkauf",
                    category = "Lebensmittel",
                    amount = 4250,
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
