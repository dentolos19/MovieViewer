package com.it2161.s231292a.movieviewer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerInput(
    label: String,
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    showPicker: Boolean,
    onShowPicker: () -> Unit,
    onDismiss: () -> Unit,
    errorText: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val displayText = selectedDate?.let { dateFormatter.format(Date(it)) } ?: ""

    Column(modifier = modifier) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text("Select date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onShowPicker() },
            readOnly = true,
            enabled = enabled,
            isError = errorText != null,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "Select date",
                    modifier = Modifier.clickable(enabled = enabled) { onShowPicker() }
                )
            }
        )

        ErrorText(text = errorText)
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

