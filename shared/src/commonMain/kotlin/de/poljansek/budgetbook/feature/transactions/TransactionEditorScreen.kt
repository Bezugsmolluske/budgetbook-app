package de.poljansek.budgetbook.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.ui.ConflictDialog
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.SimpleDropdown
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorScreen(
    type: BookType?,
    transactionId: String,
    onBack: () -> Unit,
) {
    val initialType = type ?: BookType.EXPENSE
    val viewModel = koinViewModel<TransactionEditorViewModel> { parametersOf(initialType, transactionId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isCreate = transactionId.isBlank()
    val title = when {
        isCreate && state.type == BookType.EXPENSE -> "Neue Ausgabe"
        isCreate -> "Neues Einkommen"
        else -> "Buchung bearbeiten"
    }
    val matchingCategories = remember(state.categories, state.type) {
        state.categories.filter { it.type == state.type }
    }
    val selectedName = matchingCategories.firstOrNull { it.id == state.categoryId }?.name
        ?: matchingCategories.firstOrNull()?.name.orEmpty()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null && state.categories.isEmpty() -> ErrorState(
                state.error ?: "Fehler",
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            else -> Column(
                Modifier.padding(padding).fillMaxSize().padding(16.dp),
            ) {
                Row(
                    Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.type == BookType.EXPENSE,
                        onClick = { viewModel.updateType(BookType.EXPENSE) },
                        label = { Text("Ausgaben") },
                    )
                    FilterChip(
                        selected = state.type == BookType.INCOME,
                        onClick = { viewModel.updateType(BookType.INCOME) },
                        label = { Text("Einkommen") },
                    )
                }
                OutlinedTextField(
                    value = state.date,
                    onValueChange = viewModel::updateDate,
                    label = { Text("Datum (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                )
                if (matchingCategories.isNotEmpty()) {
                    SimpleDropdown(
                        label = "Kategorie",
                        options = matchingCategories.map { it.name },
                        selected = selectedName,
                        onSelected = viewModel::updateCategoryName,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                OutlinedTextField(
                    value = state.amountInput,
                    onValueChange = viewModel::updateAmount,
                    label = { Text("Betrag") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    suffix = { Text("€") },
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Beschreibung") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                )
                if (state.error != null && !state.conflict) {
                    Text(state.error ?: "", modifier = Modifier.padding(bottom = 12.dp))
                }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.saving) "Speichern …" else "Speichern")
                }
            }
        }
    }

    if (state.conflict) {
        ConflictDialog(onReload = viewModel::load, onDismiss = viewModel::load)
    }
}
