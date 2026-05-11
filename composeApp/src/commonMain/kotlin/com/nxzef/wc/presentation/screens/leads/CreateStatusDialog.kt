package com.nxzef.wc.presentation.screens.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.nxzef.wc.presentation.components.toComposeColor
import com.nxzef.wc.shared.model.LeadStatus

val STATUS_COLORS = listOf(
    "#2196F3", "#4CAF50", "#FF9800", "#E91E63",
    "#9C27B0", "#009688", "#FF5722", "#607D8B"
)

@Composable
fun CreateStatusDialog(
    existingStatuses: List<LeadStatus>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String) -> Unit,
    onValidationError: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(STATUS_COLORS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("New Status", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Status Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Text("Pick a Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    STATUS_COLORS.forEach { hex ->
                        val c = hex.toComposeColor()
                        Box(
                            modifier = Modifier
                                .size(if (selectedColor == hex) 32.dp else 28.dp)
                                .background(c, MaterialTheme.shapes.small)
                                .then(
                                    if (selectedColor == hex)
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            MaterialTheme.shapes.small
                                        )
                                    else Modifier
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = name.trim()
                    when {
                        trimmed.isEmpty() -> onValidationError("Status name is required")
                        existingStatuses.any { it.name.equals(trimmed, ignoreCase = true) } ->
                            onValidationError("A status with this name already exists")
                        else -> onConfirm(trimmed, selectedColor)
                    }
                },
                enabled = name.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = MaterialTheme.shapes.medium) { Text("Cancel") }
        }
    )
}
