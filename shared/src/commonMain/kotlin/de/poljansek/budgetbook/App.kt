package de.poljansek.budgetbook

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.poljansek.budgetbook.app.theme.BudgetBookTheme
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.settings.AppSettings
import de.poljansek.budgetbook.feature.categories.CategoriesScreen
import de.poljansek.budgetbook.feature.overview.OverviewScreen
import de.poljansek.budgetbook.feature.settings.SettingsScreen
import de.poljansek.budgetbook.feature.statistics.StatisticsScreen
import de.poljansek.budgetbook.feature.transactions.TransactionEditorScreen
import de.poljansek.budgetbook.feature.transactions.TransactionsScreen
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable data object OverviewRoute
@Serializable data object TransactionsRoute
@Serializable data object CategoriesRoute
@Serializable data object StatisticsRoute
@Serializable data object SettingsRoute
@Serializable data class TransactionEditorRoute(val id: String = "", val type: String = "")

private data class TopLevelDestination(
    val route: Any,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(OverviewRoute, "Übersicht", Icons.Filled.Home),
    TopLevelDestination(TransactionsRoute, "Buchungen", Icons.Filled.AddCircle),
    TopLevelDestination(CategoriesRoute, "Kategorien", Icons.AutoMirrored.Filled.List),
    TopLevelDestination(StatisticsRoute, "Statistik", Icons.Filled.DateRange),
    TopLevelDestination(SettingsRoute, "Einstellungen", Icons.Filled.Settings),
)

@Composable
fun App() {
    val settings = koinInject<AppSettings>()
    val theme by settings.theme.collectAsStateWithLifecycle()
    BudgetBookTheme(theme) {
        Surface(Modifier.fillMaxSize()) {
            BudgetBookNav()
        }
    }
}

@Composable
private fun BudgetBookNav(
    navController: NavHostController = rememberNavController(),
) {
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val isEditor = destination?.hasRoute<TransactionEditorRoute>() == true
    val widthDp = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    val useRail = widthDp >= 600.dp && !isEditor

    Scaffold(
        bottomBar = {
            if (!useRail && !isEditor) {
                NavigationBar {
                    topLevelDestinations.forEach { item ->
                        NavigationBarItem(
                            selected = destination.isSelected(item),
                            onClick = { navController.navigateTopLevel(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            alwaysShowLabel = false,
                        )
                    }
                }
            }
        }
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (useRail) {
                NavigationRail {
                    topLevelDestinations.forEach { item ->
                        NavigationRailItem(
                            selected = destination.isSelected(item),
                            onClick = { navController.navigateTopLevel(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
            NavHost(
                navController = navController,
                startDestination = OverviewRoute,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable<OverviewRoute> { OverviewScreen() }
                composable<TransactionsRoute> {
                    TransactionsScreen(
                        onAdd = { type ->
                            navController.navigate(TransactionEditorRoute(type = type.name))
                        },
                        onEdit = { navController.navigate(TransactionEditorRoute(id = it)) },
                    )
                }
                composable<CategoriesRoute> { CategoriesScreen() }
                composable<StatisticsRoute> { StatisticsScreen() }
                composable<SettingsRoute> { SettingsScreen() }
                composable<TransactionEditorRoute> { entry ->
                    val route = entry.toRoute<TransactionEditorRoute>()
                    TransactionEditorScreen(
                        type = route.type.takeIf { it.isNotBlank() }?.let { BookType.valueOf(it) },
                        transactionId = route.id,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

private fun androidx.navigation.NavDestination?.isSelected(item: TopLevelDestination): Boolean {
    val destination = this ?: return false
    return when (item.route) {
        OverviewRoute -> destination.hasRoute<OverviewRoute>()
        TransactionsRoute -> destination.hasRoute<TransactionsRoute>()
        CategoriesRoute -> destination.hasRoute<CategoriesRoute>()
        StatisticsRoute -> destination.hasRoute<StatisticsRoute>()
        SettingsRoute -> destination.hasRoute<SettingsRoute>()
        else -> false
    }
}

private fun NavHostController.navigateTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
