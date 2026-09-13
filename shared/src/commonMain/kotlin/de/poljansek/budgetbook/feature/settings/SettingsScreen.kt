package de.poljansek.budgetbook.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.settings.ThemePreference
import de.poljansek.budgetbook.core.ui.ConfirmDialog
import de.poljansek.budgetbook.core.files.pickBackupFile
import de.poljansek.budgetbook.core.files.saveBackupFile
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var restoreKind by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Einstellungen") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Darstellung", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = theme == ThemePreference.System,
                    onClick = { viewModel.setTheme(ThemePreference.System) },
                    label = { Text("System") },
                )
                FilterChip(
                    selected = theme == ThemePreference.Light,
                    onClick = { viewModel.setTheme(ThemePreference.Light) },
                    label = { Text("Hell") },
                )
                FilterChip(
                    selected = theme == ThemePreference.Dark,
                    onClick = { viewModel.setTheme(ThemePreference.Dark) },
                    label = { Text("Dunkel") },
                )
            }

            Text("API", style = MaterialTheme.typography.titleMedium)
            Text("Flavor: ${viewModel.flavorName}")
            Text("Standard: ${viewModel.flavorDefault}", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = state.baseUrlDraft,
                onValueChange = viewModel::updateBaseUrlDraft,
                label = { Text("Basis-URL") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::saveBaseUrl) { Text("URL speichern") }
                OutlinedButton(onClick = viewModel::resetBaseUrl) { Text("Zurücksetzen") }
            }

            Text("Backup", style = MaterialTheme.typography.titleMedium)
            Text("Ein Restore überschreibt alle Serverdaten.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            runCatching {
                                saveBackupFile(viewModel.exportJson(), "backup", "json")
                            }
                        }
                    },
                    enabled = !state.busy,
                ) { Text("JSON exportieren") }
                Button(
                    onClick = {
                        scope.launch {
                            runCatching {
                                saveBackupFile(viewModel.exportCsv(), "backup", "csv")
                            }
                        }
                    },
                    enabled = !state.busy,
                ) { Text("CSV exportieren") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { restoreKind = "json" },
                    enabled = !state.busy,
                ) { Text("JSON wiederherstellen") }
                OutlinedButton(
                    onClick = { restoreKind = "csv" },
                    enabled = !state.busy,
                ) { Text("CSV wiederherstellen") }
            }
            state.message?.let { Text(it) }
        }
    }

    restoreKind?.let { kind ->
        ConfirmDialog(
            title = "Backup einspielen?",
            message = "Alle vorhandenen Daten auf dem Server werden gelöscht und durch das Backup ersetzt.",
            confirmLabel = "Weiter",
            onConfirm = {
                restoreKind = null
                scope.launch {
                    val picked = pickBackupFile(kind)
                    if (picked == null) {
                        viewModel.notice("Dateiauswahl ist in dieser Umgebung nicht verfügbar.")
                        return@launch
                    }
                    if (kind == "json") {
                        viewModel.restoreJson(picked.bytes, picked.name)
                    } else {
                        viewModel.restoreCsv(picked.bytes, picked.name)
                    }
                }
            },
            onDismiss = { restoreKind = null },
        )
    }
}
