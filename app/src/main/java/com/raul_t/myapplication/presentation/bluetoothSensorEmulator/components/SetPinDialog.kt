package com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.raul_t.myapplication.R

@Composable
fun SetPinDialog(
    initialPin: String = "0000",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var pinDigits by remember { 
        mutableStateOf(
            initialPin.map { it.toString() }.take(4).let { 
                if (it.size < 4) it + List(4 - it.size) { "0" } else it
            }
        ) 
    }
    val focusRequesters = remember { List(4) { FocusRequester() } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.set_pin_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    pinDigits.forEachIndexed { index, digit ->
                        OutlinedTextField(
                            value = digit,
                            onValueChange = { newValue ->
                                if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                    val newPin = pinDigits.toMutableList()
                                    newPin[index] = if (newValue.isEmpty()) "" else newValue
                                    pinDigits = newPin
                                    
                                    // Move focus to next if digit entered
                                    if (newValue.isNotEmpty() && index < 3) {
                                        focusRequesters[index + 1].requestFocus()
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(60.dp)
                                .focusRequester(focusRequesters[index]),
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.outline,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                cursorColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(pinDigits.joinToString(""))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(text = stringResource(R.string.save))
                    }
                }
            }
        }
    }

    // Effect to focus the first field on launch
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }
}
