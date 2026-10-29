package de.poljansek.budgetbook.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.poljansek.budgetbook.core.network.dto.BookType

private val ExpenseSelected = Color(0xFFC62828)
private val ExpenseSelectedText = Color.White
private val IncomeSelected = Color(0xFF2E7D32)
private val IncomeSelectedText = Color.White

@Composable
private fun typeChipColors(selected: Color, onSelected: Color) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = selected,
    selectedLabelColor = onSelected,
    disabledSelectedContainerColor = selected,
)

@Composable
fun TransactionForm(
    type: BookType,
    onTypeChange: (BookType) -> Unit,
    date: String,
    onDateSelected: (String) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    categoryNames: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateModifier: Modifier = Modifier,
    amountModifier: Modifier = Modifier,
    categoryModifier: Modifier = Modifier,
    descriptionModifier: Modifier = Modifier,
    categoryFocusRequester: FocusRequester? = null,
    categoryTabStop: Boolean = true,
    amountKeyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    amountKeyboardActions: KeyboardActions = KeyboardActions.Default,
    descriptionKeyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    descriptionKeyboardActions: KeyboardActions = KeyboardActions.Default,
    headerActions: @Composable RowScope.() -> Unit = {},
    enabled: Boolean = true,
    descriptionMaxLines: Int = Int.MAX_VALUE,
) {
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = type == BookType.EXPENSE,
                onClick = { onTypeChange(BookType.EXPENSE) },
                label = { Text("Ausgaben") },
                enabled = enabled,
                colors = typeChipColors(ExpenseSelected, ExpenseSelectedText),
                modifier = Modifier.focusProperties { canFocus = false },
            )
            FilterChip(
                selected = type == BookType.INCOME,
                onClick = { onTypeChange(BookType.INCOME) },
                label = { Text("Einkommen") },
                enabled = enabled,
                colors = typeChipColors(IncomeSelected, IncomeSelectedText),
                modifier = Modifier.focusProperties { canFocus = false },
            )
            Spacer(Modifier.weight(1f))
            headerActions()
        }
        Row(
            Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateField(
                date = date,
                onDateSelected = onDateSelected,
                modifier = Modifier.weight(1f).then(dateModifier),
                enabled = enabled,
            )
            OutlinedTextField(
                value = TextFieldValue(amountText, TextRange(amountText.length)),
                onValueChange = { onAmountChange(it.text) },
                enabled = enabled,
                label = { Text("Betrag") },
                modifier = Modifier.weight(1f).then(amountModifier),
                suffix = { Text("€") },
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                keyboardOptions = amountKeyboardOptions,
                keyboardActions = amountKeyboardActions,
                singleLine = true,
            )
        }
        if (categoryNames.isNotEmpty()) {
            SimpleDropdown(
                label = "Kategorie",
                options = categoryNames,
                selected = selectedCategory,
                onSelected = onCategorySelected,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).then(categoryModifier),
                focusRequester = categoryFocusRequester,
                tabStop = categoryTabStop,
                enabled = enabled,
                searchable = true,
            )
        }
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            enabled = enabled,
            label = { Text("Beschreibung") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).then(descriptionModifier),
            keyboardOptions = descriptionKeyboardOptions,
            keyboardActions = descriptionKeyboardActions,
            maxLines = descriptionMaxLines,
        )
    }
}
