package com.gtw.bambufilamentloader.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.gtw.bambufilamentloader.model.domain.DiscoveredPrinter

@Composable
fun AccessCodeInputDialogue(
    printer: DiscoveredPrinter,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val accessCode = remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(accessCode.value) },
                enabled = accessCode.value.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        title = { Text("Access Code Required") },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TextField(
                value = accessCode.value,
                onValueChange = { accessCode.value = it },
                label = { Text("For ${printer.name}") },
            )
        }
    )
}