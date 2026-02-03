@file:OptIn(ExperimentalMaterial3Api::class)

package com.it2161.s231292a.movieviewer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.*

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
}

