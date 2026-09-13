package de.poljansek.budgetbook.di

import com.russhwolf.settings.Settings
import de.poljansek.budgetbook.core.network.BudgetBookApi
import de.poljansek.budgetbook.core.network.KtorBudgetBookApi
import de.poljansek.budgetbook.core.settings.AppSettings
import de.poljansek.budgetbook.feature.categories.CategoriesViewModel
import de.poljansek.budgetbook.feature.expenses.ExpenseEditorViewModel
import de.poljansek.budgetbook.feature.expenses.ExpensesViewModel
import de.poljansek.budgetbook.feature.incomes.IncomeEditorViewModel
import de.poljansek.budgetbook.feature.incomes.IncomesViewModel
import de.poljansek.budgetbook.feature.overview.OverviewViewModel
import de.poljansek.budgetbook.feature.settings.SettingsViewModel
import de.poljansek.budgetbook.feature.statistics.StatisticsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }
    single {
        HttpClient {
            expectSuccess = false
            install(ContentNegotiation) {
                json(get())
            }
        }
    }
    single { AppSettings(Settings()) }
    single<BudgetBookApi> {
        KtorBudgetBookApi(get()) { get<AppSettings>().resolvedBaseUrl() }
    }

    viewModelOf(::OverviewViewModel)
    viewModelOf(::ExpensesViewModel)
    viewModelOf(::IncomesViewModel)
    viewModelOf(::CategoriesViewModel)
    viewModelOf(::StatisticsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModel { (expenseId: String) -> ExpenseEditorViewModel(expenseId, get()) }
    viewModel { (incomeId: String) -> IncomeEditorViewModel(incomeId, get()) }
}

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
