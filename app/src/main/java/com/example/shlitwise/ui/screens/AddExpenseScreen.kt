package com.example.shlitwise.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shlitwise.data.AuthRepository
import com.example.shlitwise.model.SingleParticipantSplitOption
import com.example.shlitwise.model.User
import com.example.shlitwise.model.toDisplayText
import com.example.shlitwise.ui.components.ParticipantEntryField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddExpenseScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val selectedParticipants = remember { mutableStateListOf<User>() }

    var participantInput by rememberSaveable { mutableStateOf("") }
    var participantError by rememberSaveable { mutableStateOf<String?>(null) }
    var isParticipantLookupLoading by rememberSaveable { mutableStateOf(false) }

    var description by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    var showSingleParticipantSplitScreen by rememberSaveable { mutableStateOf(false) }
    var selectedSingleParticipantSplitOption by rememberSaveable {
        mutableStateOf(SingleParticipantSplitOption.YOU_PAID_SPLIT_EQUALLY)
    }

    val scope = rememberCoroutineScope()

    BackHandler {
        if (showSingleParticipantSplitScreen) {
            showSingleParticipantSplitScreen = false
        } else {
            onBackClick()
        }
    }

    if (showSingleParticipantSplitScreen && selectedParticipants.size == 1) {
        val otherUserName = selectedParticipants.first().fullName.ifBlank {
            selectedParticipants.first().email
        }

        SingleParticipantSplitScreen(
            otherUserName = otherUserName,
            selectedOption = selectedSingleParticipantSplitOption,
            onOptionSelected = { option ->
                selectedSingleParticipantSplitOption = option
            },
            onBackClick = {
                showSingleParticipantSplitScreen = false
            }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBackClick, enabled = !isParticipantLookupLoading) {
                Text("Back")
            }

            Text(
                text = "Add Expense",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onSaveClick, enabled = !isParticipantLookupLoading) {
                Text("✓")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ParticipantEntryField(
            participants = selectedParticipants,
            currentInput = participantInput,
            isLoading = isParticipantLookupLoading,
            onInputChange = {
                participantInput = it
                participantError = null
            },
            onAddEntry = {
                val trimmedValue = participantInput.trim()

                if (trimmedValue.isBlank()) {
                    participantError = "Enter an email address or phone number"
                    return@ParticipantEntryField
                }

                isParticipantLookupLoading = true
                participantError = null

                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        repository.lookupParticipant(trimmedValue)
                    }

                    isParticipantLookupLoading = false

                    result.fold(
                        onSuccess = { foundUser ->
                            val alreadyAdded = selectedParticipants.any {
                                it.id == foundUser.id ||
                                        it.email.equals(foundUser.email, ignoreCase = true) ||
                                        it.phoneNumber == foundUser.phoneNumber
                            }

                            if (alreadyAdded) {
                                participantError = "This participant is already added"
                            } else {
                                selectedParticipants.add(foundUser)
                                participantInput = ""
                                participantError = null

                                if (selectedParticipants.size > 1) {
                                    showSingleParticipantSplitScreen = false
                                }
                            }
                        },
                        onFailure = { error ->
                            participantError = error.message ?: "Unable to lookup participant"
                        }
                    )
                }
            },
            onRemoveEntry = { participant ->
                selectedParticipants.remove(participant)

                if (selectedParticipants.size != 1) {
                    showSingleParticipantSplitScreen = false
                }
            },
            errorMessage = participantError
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description") },
            placeholder = { Text("Description") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                    amount = newValue
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        if (selectedParticipants.size == 1) {
            Spacer(modifier = Modifier.height(16.dp))

            val otherUserName = selectedParticipants.first().fullName.ifBlank {
                selectedParticipants.first().email
            }

            Button(
                onClick = {
                    showSingleParticipantSplitScreen = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedSingleParticipantSplitOption.toDisplayText(otherUserName))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Amount accepts dollars with up to 2 decimal places.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}