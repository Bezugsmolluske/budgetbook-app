package de.poljansek.budgetbook.feature.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.poljansek.budgetbook.core.money.formatAmountInput
import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.ui.ConflictDialog
import de.poljansek.budgetbook.core.ui.ErrorState
import de.poljansek.budgetbook.core.ui.LoadingState
import de.poljansek.budgetbook.core.ui.TransactionForm
import de.poljansek.budgetbook.core.ui.ignoreTab
import de.poljansek.budgetbook.core.ui.moveFocusOnTab
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
    val categoryFocus = remember { FocusRequester() }
    val amountFocus = remember { FocusRequester() }
    val descriptionFocus = remember { FocusRequester() }
    val saveFocus = remember { FocusRequester() }
    var pendingFocus by remember { mutableStateOf<FormFocus?>(null) }
    var categoryTabStop by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    LaunchedEffect(pendingFocus, categoryTabStop) {
        when (pendingFocus) {
            FormFocus.Category -> {
                if (!categoryTabStop) {
                    categoryTabStop = true
                } else {
                    runCatching { categoryFocus.requestFocus() }
                    pendingFocus = null
                }
            }
            FormFocus.Amount -> {
                runCatching { amountFocus.requestFocus() }
                categoryTabStop = false
                pendingFocus = null
            }
            null -> Unit
        }
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
                val amountText = formatAmountInput(state.amountInput)
                TransactionForm(
                    type = state.type,
                    onTypeChange = viewModel::updateType,
                    date = state.date,
                    onDateSelected = { date ->
                        viewModel.updateDate(date)
                        pendingFocus = if (matchingCategories.isNotEmpty()) {
                            FormFocus.Category
                        } else {
                            FormFocus.Amount
                        }
                    },
                    amountText = amountText,
                    onAmountChange = viewModel::updateAmount,
                    categoryNames = matchingCategories.map { it.name },
                    selectedCategory = selectedName,
                    onCategorySelected = { name ->
                        viewModel.updateCategoryName(name)
                        pendingFocus = FormFocus.Amount
                    },
                    description = state.description,
                    onDescriptionChange = viewModel::updateDescription,
                    dateModifier = Modifier.ignoreTab(),
                    amountModifier = Modifier
                        .focusRequester(amountFocus)
                        .onFocusChanged { if (it.isFocused) categoryTabStop = false }
                        .moveFocusOnTab(next = descriptionFocus, previous = null),
                    categoryModifier = Modifier.ignoreTab(),
                    descriptionModifier = Modifier
                        .focusRequester(descriptionFocus)
                        .moveFocusOnTab(next = saveFocus, previous = amountFocus),
                    categoryFocusRequester = categoryFocus,
                    categoryTabStop = categoryTabStop,
                    amountKeyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    amountKeyboardActions = KeyboardActions(onNext = { descriptionFocus.requestFocus() }),
                    descriptionKeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    descriptionKeyboardActions = KeyboardActions(onNext = { saveFocus.requestFocus() }),
                )
                if (state.error != null && !state.conflict) {
                    Text(state.error ?: "", modifier = Modifier.padding(bottom = 12.dp))
                }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(saveFocus)
                        .moveFocusOnTab(next = null, previous = descriptionFocus),
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

private enum class FormFocus { Category, Amount }
