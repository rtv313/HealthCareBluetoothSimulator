package com.raul_t.myapplication.presentation.bluetoothClient.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.R
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Composable
fun PinEntryDialog(
    errorMessage: String?,
    onPinEntered: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Enter PIN Required") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "This device requires a 4-digit PIN code to establish a secure data stream connection.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pinText,
                    onValueChange = { if (it.length <= 4) pinText = it },
                    label = { Text("PIN Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPinEntered(pinText) },
                enabled = pinText.length == 4
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.start),
                    color = if (pinText.length == 4) SuccessGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
            }
        }
    )
}
