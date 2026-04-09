package com.example.shlitwise.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shlitwise.model.User

@Composable
fun ParticipantEntryField(
    participants: List<User>,
    currentInput: String,
    isLoading: Boolean,
    onInputChange: (String) -> Unit,
    onAddEntry: () -> Unit,
    onRemoveEntry: (User) -> Unit,
    errorMessage: String?
) {
    if (participants.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            participants.forEach { participant ->
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = if (participant.fullName.isNotBlank()) {
                                participant.fullName
                            } else {
                                participant.email
                            }
                        )
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = { onRemoveEntry(participant) },
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text("x")
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors()
                )
            }
        }
    }

    OutlinedTextField(
        value = currentInput,
        onValueChange = onInputChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("With you and") },
        placeholder = { Text("Enter names, emails or phone #s") },
        singleLine = true,
        isError = errorMessage != null
    )

    if (errorMessage != null) {
        Text(
            text = errorMessage,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    Button(
        onClick = onAddEntry,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        enabled = currentInput.trim().isNotEmpty() && !isLoading
    ) {
        Text(if (isLoading) "Checking..." else "Add Participant")
    }
}