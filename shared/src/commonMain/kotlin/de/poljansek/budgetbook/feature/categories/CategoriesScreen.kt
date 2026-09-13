package de.poljansek.budgetbook.feature.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.ui.ConflictDialog
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Kategorien") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.kind == CategoryKind.Expense,
                    onClick = { viewModel.setKind(CategoryKind.Expense) },
                    label = { Text("Ausgaben") },
                )
                FilterChip(
                    selected = state.kind == CategoryKind.Income,
                    onClick = { viewModel.setKind(CategoryKind.Income) },
                    label = { Text("Einkommen") },
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.draftName,
                    onValueChange = viewModel::updateDraft,
                    label = { Text(if (state.editingId == null) "Neue Kategorie" else "Kategorie bearbeiten") },
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = viewModel::save) {
                    Text(if (state.editingId == null) "Anlegen" else "Speichern")
                }
            }
            if (state.editingId != null) {
                Button(onClick = viewModel::cancelEdit, modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("Bearbeiten abbrechen")
                }
            }
            when {
                state.loading -> LoadingState()
                state.error != null && state.expenseCategories.isEmpty() && state.incomeCategories.isEmpty() ->
                    ErrorState(state.error ?: "Fehler", onRetry = viewModel::refresh)
                else -> {
                    if (state.error != null && !state.conflict) {
                        Text(state.error ?: "", modifier = Modifier.padding(bottom = 8.dp))
                    }
                    if (state.kind == CategoryKind.Expense) {
                        LazyColumn {
                            items(state.expenseCategories, key = { it.id ?: it.category }) { category ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(category.category, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.startEditExpense(category) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten")
                                    }
                                    IconButton(onClick = { category.id?.let(viewModel::deleteExpense) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    } else {
                        LazyColumn {
                            items(state.incomeCategories, key = { it.id ?: it.category }) { category ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(category.category, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.startEditIncome(category) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten")
                                    }
                                    IconButton(onClick = { category.id?.let(viewModel::deleteIncome) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.conflict) {
        ConflictDialog(onReload = viewModel::refresh, onDismiss = viewModel::refresh)
    }
}
