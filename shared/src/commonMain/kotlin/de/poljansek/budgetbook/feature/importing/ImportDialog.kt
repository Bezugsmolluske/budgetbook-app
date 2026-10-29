package de.poljansek.budgetbook.feature.importing

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.money.amountInputToCents
import de.poljansek.budgetbook.core.money.formatAmountInput
import de.poljansek.budgetbook.core.money.formatMoney
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.TransactionForm
import de.poljansek.budgetbook.core.ui.formatIsoDate
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDialog(
    bookings: List<ImportedBooking>,
    onDismiss: (savedCount: Int) -> Unit,
    viewModel: ImportViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(bookings) { viewModel.start(bookings) }

    val allSettled = !state.loading && state.error == null && state.rows.all { it.savedId != null }

    fun close() {
        if (state.rows.any { it.saving }) return
        onDismiss(if (state.changed) 1 else 0)
    }

    Dialog(
        onDismissRequest = { close() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    Column {
                        TopAppBar(
                            title = { Text("Umsätze importieren") },
                            navigationIcon = {
                                IconButton(onClick = { close() }, enabled = state.rows.none { it.saving }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Schließen")
                                }
                            },
                        )
                        if (!state.loading && state.error == null) {
                            Text(
                                "Gesamt ${state.total} · Gespeichert ${state.saved} · Verworfen ${state.removed} · Dubletten ${state.duplicates}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (allSettled) {
                        ExtendedFloatingActionButton(onClick = { close() }) {
                            Text("Fertig")
                        }
                    }
                },
            ) { padding ->
                when {
                    state.loading -> LoadingState(Modifier.padding(padding))
                    state.error != null -> ErrorState(
                        message = state.error ?: "Fehler",
                        onRetry = viewModel::retry,
                        modifier = Modifier.padding(padding),
                    )
                    state.rows.isEmpty() -> Box(
                        Modifier.padding(padding).fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Keine Buchungen zum Importieren.")
                    }
                    else -> {
                        val widthDp = with(LocalDensity.current) {
                            LocalWindowInfo.current.containerSize.width.toDp()
                        }
                        val gridState = rememberLazyGridState()
                        Box(Modifier.padding(padding).fillMaxSize()) {
                            LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Fixed(if (widthDp >= TwoColumnMinWidth) 2 else 1),
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(
                                    top = 16.dp,
                                    end = 12.dp,
                                    bottom = if (allSettled) 96.dp else 24.dp,
                                ),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(state.rows, key = { it.key }) { row ->
                                    ImportRowCard(
                                        row = row,
                                        categories = state.categories,
                                        existing = state.existing,
                                        onDate = { viewModel.updateDate(row.key, it) },
                                        onAmount = { viewModel.updateAmount(row.key, it) },
                                        onType = { viewModel.updateType(row.key, it) },
                                        onDescription = { viewModel.updateDescription(row.key, it) },
                                        onCategory = { viewModel.updateCategory(row.key, it) },
                                        onDiscard = { viewModel.discard(row.key) },
                                        onSave = { viewModel.save(row.key) },
                                        onEdit = { viewModel.edit(row.key) },
                                        onDelete = { viewModel.delete(row.key) },
                                    )
                                }
                            }
                            VerticalScrollbar(
                                adapter = rememberScrollbarAdapter(gridState),
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                style = ScrollbarStyle(
                                    minimalHeight = 24.dp,
                                    thickness = 8.dp,
                                    shape = RoundedCornerShape(4.dp),
                                    hoverDurationMillis = 300,
                                    unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportRowCard(
    row: ImportRow,
    categories: List<CategoryDto>,
    existing: List<ExistingBooking>,
    onDate: (String) -> Unit,
    onAmount: (String) -> Unit,
    onType: (BookType) -> Unit,
    onDescription: (String) -> Unit,
    onCategory: (String) -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val cents = amountInputToCents(row.amount.digits) ?: 0L
    val matches = matchingDuplicates(row.date, cents, row.type, existing)
        .filter { it.id == null || it.id != row.savedId }
    val matchingCategories = categories.filter { it.type == row.type }
    val selectedName = matchingCategories.firstOrNull { it.id == row.categoryId }?.name.orEmpty()
    val saved = row.savedId != null
    val duplicate = matches.isNotEmpty()
    val highlight = duplicateHighlightColors()

    Card(
        Modifier.fillMaxWidth(),
        colors = when {
            duplicate -> CardDefaults.cardColors(
                containerColor = highlight.container,
                contentColor = highlight.text,
            )
            saved && !row.editing -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            )
            else -> CardDefaults.cardColors()
        },
    ) {
        if (duplicate) {
            Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                Text(
                    "Mögliche Dublette",
                    style = MaterialTheme.typography.titleSmall,
                    color = highlight.text,
                )
                matches.forEach { match ->
                    ExistingMatch(match, highlight.text)
                }
            }
        }
        ImportSource(
            source = row.source,
            color = if (duplicate) highlight.text else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = 16.dp,
                top = if (duplicate) 8.dp else 16.dp,
                end = 16.dp,
            ),
        )
        TransactionForm(
            type = row.type,
            onTypeChange = onType,
            date = row.date,
            onDateSelected = onDate,
            amountText = formatAmountInput(row.amount.digits),
            onAmountChange = onAmount,
            categoryNames = matchingCategories.map { it.name },
            selectedCategory = selectedName,
            onCategorySelected = onCategory,
            description = row.description,
            onDescriptionChange = onDescription,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            enabled = !row.locked,
            descriptionMaxLines = 3,
        )
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                saved && !row.editing -> {
                    Text(
                        "Gespeichert",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onEdit, enabled = !row.saving) { Text("Bearbeiten") }
                }
                saved -> {
                    TextButton(onClick = onDelete, enabled = !row.saving) { Text("Löschen") }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onSave, enabled = row.isComplete() && !row.saving) {
                        Text(if (row.saving) "Speichern …" else "Speichern")
                    }
                }
                else -> {
                    TextButton(onClick = onDiscard, enabled = !row.saving) { Text("Verwerfen") }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onSave, enabled = row.isComplete() && !row.saving) {
                        Text(if (row.saving) "Speichern …" else "Speichern")
                    }
                }
            }
        }
        row.error?.let { message ->
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun ExistingMatch(match: ExistingBooking, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Bereits vorhanden", style = MaterialTheme.typography.labelLarge)
            Text(
                match.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "${formatIsoDate(match.date)} · ${formatMoney(match.amountCents, match.currency)} · ${match.categoryName}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ImportSource(source: String, color: Color, modifier: Modifier = Modifier) {
    val lines = source.lines()
    val head = lines.firstOrNull().orEmpty()
    val rest = lines.drop(1).joinToString("\n").trim()
    var expanded by remember(source) { mutableStateOf(false) }
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                head,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.weight(1f),
            )
            if (rest.isNotEmpty()) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(if (expanded) "Weniger" else "Rohdaten")
                }
            }
        }
        if (expanded && rest.isNotEmpty()) {
            Text(rest, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

private val TwoColumnMinWidth = 960.dp
private val DuplicateHighlightLight = Color(0xFFFFE0B2)
private val DuplicateHighlightTextLight = Color(0xFF6D4C00)
private val DuplicateHighlightDark = Color(0xFF5C4300)
private val DuplicateHighlightTextDark = Color(0xFFFFE0B2)

private data class DuplicateHighlightColors(val container: Color, val text: Color)

@Composable
private fun duplicateHighlightColors(): DuplicateHighlightColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return if (dark) {
        DuplicateHighlightColors(DuplicateHighlightDark, DuplicateHighlightTextDark)
    } else {
        DuplicateHighlightColors(DuplicateHighlightLight, DuplicateHighlightTextLight)
    }
}
