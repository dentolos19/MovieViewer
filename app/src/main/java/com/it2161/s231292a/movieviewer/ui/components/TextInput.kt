package com.it2161.s231292a.movieviewer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TextInput(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String = "",
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    errorText: String? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            isError = errorText != null,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            minLines = minLines,
            maxLines = maxLines,
            readOnly = readOnly,
            trailingIcon = trailingIcon
        )

        ErrorText(text = errorText)
    }
}
