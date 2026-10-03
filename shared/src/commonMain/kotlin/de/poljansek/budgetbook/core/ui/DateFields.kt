package de.poljansek.budgetbook.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate

private const val MILLIS_PER_DAY = 86_400_000L

// DatePicker state uses UTC midnight millis.
internal fun isoToUtcMillis(iso: String): Long? =
    runCatching { LocalDate.parse(iso).toEpochDays() * MILLIS_PER_DAY }.getOrNull()

internal fun utcMillisToIso(millis: Long): String =
    LocalDate.fromEpochDays((millis / MILLIS_PER_DAY).toInt()).toString()

@Composable
private fun PickerTriggerField(
    value: String,
    label: String,
    iconDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = iconDescription) },
            modifier = Modifier.focusProperties { canFocus = false }.fillMaxWidth(),
        )
        // Read-only text fields swallow taps, so an overlay opens the picker.
        // The field is not a tab stop; a chosen date focuses the next input instead.
        Box(
            Modifier
                .matchParentSize()
                .focusProperties { canFocus = false }
                .clickable(enabled = enabled, onClick = onClick),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    date: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Datum",
    enabled: Boolean = true,
) {
    var open by remember { mutableStateOf(false) }

    PickerTriggerField(
        value = if (date.isBlank()) "" else formatIsoDate(date),
        label = label,
        iconDescription = "Datum wählen",
        onClick = { open = true },
        modifier = modifier,
        enabled = enabled,
    )

    if (open) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = isoToUtcMillis(date))
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateSelected(utcMillisToIso(it)) }
                        open = false
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) { Text("Übernehmen") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Abbrechen") }
            },
        ) {
            DatePicker(
                state = pickerState,
                title = { Text("Datum wählen", Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DateRangeField(
    startDate: String,
    endDate: String,
    onRangeSelected: (start: String, end: String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Zeitraum",
) {
    var open by remember { mutableStateOf(false) }

    PickerTriggerField(
        value = "${formatIsoDate(startDate)} – ${formatIsoDate(endDate)}",
        label = label,
        iconDescription = "Zeitraum wählen",
        onClick = { open = true },
        modifier = modifier,
    )

    if (open) {
        // DateRangePicker only scrolls through months. The single DatePicker has the
        // month and year menu, so the range is edited one bound at a time with that picker.
        var editingStart by remember { mutableStateOf(true) }
        var startMillis by remember { mutableStateOf(isoToUtcMillis(startDate)) }
        var endMillis by remember { mutableStateOf(isoToUtcMillis(endDate)) }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = startMillis,
            initialDisplayedMonthMillis = startMillis,
        )

        fun rememberSelection() {
            pickerState.selectedDateMillis?.let { selected ->
                if (editingStart) startMillis = selected else endMillis = selected
            }
        }

        fun showBound(isStart: Boolean) {
            if (isStart == editingStart) return
            rememberSelection()
            editingStart = isStart
            val millis = if (isStart) startMillis else endMillis
            pickerState.selectedDateMillis = millis
            if (millis != null) pickerState.displayedMonthMillis = millis
        }

        val activeMillis = pickerState.selectedDateMillis
        val shownStart = if (editingStart) activeMillis ?: startMillis else startMillis
        val shownEnd = if (editingStart) endMillis else activeMillis ?: endMillis

        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        rememberSelection()
                        val start = startMillis
                        val end = endMillis
                        if (start != null && end != null) {
                            val (from, to) = if (start <= end) start to end else end to start
                            onRangeSelected(utcMillisToIso(from), utcMillisToIso(to))
                        }
                        open = false
                    },
                    enabled = shownStart != null && shownEnd != null,
                ) { Text("Übernehmen") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Abbrechen") }
            },
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    FlowRow(
                        Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = editingStart,
                            onClick = { showBound(true) },
                            label = { Text("Von ${shownStart.toDisplayDate()}") },
                        )
                        FilterChip(
                            selected = !editingStart,
                            onClick = { showBound(false) },
                            label = { Text("Bis ${shownEnd.toDisplayDate()}") },
                        )
                    }
                },
            )
        }
    }
}

private fun Long?.toDisplayDate(): String =
    this?.let { formatIsoDate(utcMillisToIso(it)) } ?: "…"
