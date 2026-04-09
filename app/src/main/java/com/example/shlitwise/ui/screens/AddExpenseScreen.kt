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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shlitwise.data.AuthRepository
import com.example.shlitwise.model.PayerOption
import com.example.shlitwise.model.SingleParticipantSplitOption
import com.example.shlitwise.model.User
import com.example.shlitwise.model.buildPayerOptions
import com.example.shlitwise.model.toDisplayText
import com.example.shlitwise.ui.components.ParticipantEntryField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddExpenseScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    currentUser: User?,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val selectedParticipants = remember { mutableStateListOf<User>() }

    var participantInput by rememberSaveable { mutableStateOf("") }
    var participantError by rememberSaveable { mutableStateOf<String?>(null) }
    var isParticipantLookupLoading by rememberSaveable { mutableStateOf(false) }

    var description by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var expenseSaveError by rememberSaveable { mutableStateOf<String?>(null) }
    var isSavingExpense by rememberSaveable { mutableStateOf(false) }

    var showSingleParticipantSplitScreen by rememberSaveable { mutableStateOf(false) }
    var selectedSingleParticipantSplitOption by rememberSaveable {
        mutableStateOf(SingleParticipantSplitOption.YOU_PAID_SPLIT_EQUALLY)
    }

    var isPaidByDropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedPayerUserId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedPayerDisplayName by rememberSaveable { mutableStateOf("You") }

    val scope = rememberCoroutineScope()
    val participantCount = selectedParticipants.size
    val payerOptions: List<PayerOption> = buildPayerOptions(selectedParticipants)

    LaunchedEffect(participantCount) {
        when {
            participantCount == 0 -> {
                showSingleParticipantSplitScreen = false
                selectedSingleParticipantSplitOption =
                    SingleParticipantSplitOption.YOU_PAID_SPLIT_EQUALLY
                selectedPayerUserId = null
                selectedPayerDisplayName = "You"
                isPaidByDropdownExpanded = false
            }

            participantCount == 1 -> {
                isPaidByDropdownExpanded = false
                selectedPayerUserId = null
                selectedPayerDisplayName = "You"
            }

            participantCount >= 2 -> {
                showSingleParticipantSplitScreen = false
                val currentStillExists = payerOptions.any {
                    it.userId == selectedPayerUserId && it.displayName == selectedPayerDisplayName
                }

                if (!currentStillExists) {
                    selectedPayerUserId = null
                    selectedPayerDisplayName = "You"
                }
            }
        }
    }

    BackHandler {
        if (showSingleParticipantSplitScreen) {
            showSingleParticipantSplitScreen = false
        } else if (!isSavingExpense) {
            onBackClick()
        }
    }

    if (showSingleParticipantSplitScreen && participantCount == 1) {
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
            TextButton(
                onClick = onBackClick,
                enabled = !isParticipantLookupLoading && !isSavingExpense
            ) {
                Text("Back")
            }

            Text(
                text = "Add Expense",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    if (currentUser == null) {
                        expenseSaveError = "You must be logged in to save an expense"
                        return@TextButton
                    }

                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        expenseSaveError = "Enter a valid amount greater than 0"
                        return@TextButton
                    }

                    if (description.trim().isBlank()) {
                        expenseSaveError = "Description is required"
                        return@TextButton
                    }

                    if (selectedParticipants.isEmpty()) {
                        expenseSaveError = "Add at least one participant"
                        return@TextButton
                    }

                    val paidByUserId: Long?
                    val paidByDisplayName: String
                    val splitType = "EQUAL"
                    val singleParticipantSplitOption: String?

                    if (participantCount == 1) {
                        val otherUser = selectedParticipants.first()
                        val otherUserDisplayName = otherUser.fullName.ifBlank { otherUser.email }

                        when (selectedSingleParticipantSplitOption) {
                            SingleParticipantSplitOption.YOU_PAID_SPLIT_EQUALLY -> {
                                paidByUserId = null
                                paidByDisplayName = "You"
                            }

                            SingleParticipantSplitOption.YOU_ARE_OWED_FULL_AMOUNT -> {
                                paidByUserId = null
                                paidByDisplayName = "You"
                            }

                            SingleParticipantSplitOption.OTHER_PAID_SPLIT_EQUALLY -> {
                                paidByUserId = otherUser.id
                                paidByDisplayName = otherUserDisplayName
                            }

                            SingleParticipantSplitOption.OTHER_IS_OWED_FULL_AMOUNT -> {
                                paidByUserId = otherUser.id
                                paidByDisplayName = otherUserDisplayName
                            }
                        }

                        singleParticipantSplitOption = selectedSingleParticipantSplitOption.name
                    } else {
                        paidByUserId = selectedPayerUserId
                        paidByDisplayName = selectedPayerDisplayName
                        singleParticipantSplitOption = null
                    }

                    isSavingExpense = true
                    expenseSaveError = null

                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            repository.saveExpense(
                                createdByUserId = currentUser.id,
                                description = description,
                                amount = parsedAmount,
                                participants = selectedParticipants.toList(),
                                paidByUserId = paidByUserId,
                                paidByDisplayName = paidByDisplayName,
                                splitType = splitType,
                                singleParticipantSplitOption = singleParticipantSplitOption
                            )
                        }

                        isSavingExpense = false

                        result.fold(
                            onSuccess = {
                                onSaveClick()
                            },
                            onFailure = { error ->
                                expenseSaveError = error.message ?: "Unable to save expense"
                            }
                        )
                    }
                },
                enabled = !isParticipantLookupLoading && !isSavingExpense
            ) {
                Text(if (isSavingExpense) "Saving..." else "✓")
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
                expenseSaveError = null
            },
            onAddEntry = {
                val trimmedValue = participantInput.trim()

                if (trimmedValue.isBlank()) {
                    participantError = "Enter an email address or phone number"
                    return@ParticipantEntryField
                }

                isParticipantLookupLoading = true
                participantError = null
                expenseSaveError = null

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
            },
            errorMessage = participantError
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                expenseSaveError = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description") },
            placeholder = { Text("Description") },
            singleLine = true,
            enabled = !isSavingExpense
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                    amount = newValue
                    expenseSaveError = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSavingExpense
        )

        if (participantCount == 1) {
            Spacer(modifier = Modifier.height(16.dp))

            val otherUserName = selectedParticipants.first().fullName.ifBlank {
                selectedParticipants.first().email
            }

            Text(
                text = "Split Option",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    showSingleParticipantSplitScreen = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSavingExpense
            ) {
                Text(selectedSingleParticipantSplitOption.toDisplayText(otherUserName))
            }
        }

        if (participantCount >= 2) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Paid By",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            BoxedPaidBySelector(
                selectedPayerDisplayName = selectedPayerDisplayName,
                expanded = isPaidByDropdownExpanded,
                onExpandToggle = {
                    if (!isSavingExpense) {
                        isPaidByDropdownExpanded = !isPaidByDropdownExpanded
                    }
                },
                onDismiss = {
                    isPaidByDropdownExpanded = false
                },
                payerOptions = payerOptions,
                onPayerSelected = { option ->
                    selectedPayerUserId = option.userId
                    selectedPayerDisplayName = option.displayName
                    isPaidByDropdownExpanded = false
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Split Type",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            ) {
                Text("Split Equally")
            }
        }

        if (!expenseSaveError.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = expenseSaveError.orEmpty(),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Amount accepts dollars with up to 2 decimal places.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BoxedPaidBySelector(
    selectedPayerDisplayName: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onDismiss: () -> Unit,
    payerOptions: List<PayerOption>,
    onPayerSelected: (PayerOption) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onExpandToggle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedPayerDisplayName)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ) {
            payerOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onPayerSelected(option)
                    }
                )
            }
        }
    }
}