package de.poljansek.budgetbook.feature.incomes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.ui.ConflictDialog
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.SimpleDropdown
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeEditorScreen(
    incomeId: String,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<IncomeEditorViewModel> { parametersOf(incomeId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (incomeId.isBlank()) "Neues Einkommen" else "Einkommen bearbeiten") },
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
            else -> Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                OutlinedTextField(
                    value = state.date,
                    onValueChange = viewModel::updateDate,
                    label = { Text("Datum (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                )
                if (state.categories.isNotEmpty()) {
                    SimpleDropdown(
                        label = "Kategorie",
                        options = state.categories.map { it.category },
                        selected = state.category.ifBlank { state.categories.first().category },
                        onSelected = viewModel::updateCategory,
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
