package com.example.shlitwise.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shlitwise.data.AuthRepository
import com.example.shlitwise.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditAccountScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    currentUser: User?,
    onBackClick: () -> Unit,
    onSaveClick: (User) -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var phoneNumber by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val trimmedFullName = fullName.trim()
    val trimmedEmail = email.trim()
    val trimmedPhone = phoneNumber.trim()

    val isNameValid = trimmedFullName.isNotEmpty()
    val isEmailValid = trimmedEmail.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
    val isPhoneValid = trimmedPhone.length >= 10 && trimmedPhone.all { it.isDigit() }
    val isPasswordValid = password.isBlank() || (
            password.length >= 8 &&
                    password.any { it.isUpperCase() } &&
                    password.any { it.isLowerCase() } &&
                    password.any { it.isDigit() }
            )

    val isSaveEnabled = isNameValid && isEmailValid && isPhoneValid && isPasswordValid && !isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Edit Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                errorMessage = null
            },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = fullName.isNotBlank() && !isNameValid
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = email.isNotBlank() && !isEmailValid
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
                errorMessage = null
            },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            isError = phoneNumber.isNotBlank() && !isPhoneValid
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = password.isNotBlank() && !isPasswordValid
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Leave password blank if you do not want to change it.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (currentUser == null) {
                    errorMessage = "No user found"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        repository.updateCurrentUser(
                            user = currentUser.copy(
                                fullName = trimmedFullName,
                                email = trimmedEmail,
                                phoneNumber = trimmedPhone
                            ),
                            newPassword = password
                        )
                    }

                    isLoading = false

                    result.fold(
                        onSuccess = { updatedUser ->
                            onSaveClick(updatedUser)
                        },
                        onFailure = { error ->
                            errorMessage = error.message ?: "Unable to update account"
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isSaveEnabled
        ) {
            Text(if (isLoading) "Saving changes..." else "Save Changes")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Back")
        }
    }
}