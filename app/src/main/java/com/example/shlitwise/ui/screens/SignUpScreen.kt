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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shlitwise.data.AuthRepository
import com.example.shlitwise.model.AuthResult
import com.example.shlitwise.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SignUpScreen(
    repository: AuthRepository,
    onBackClick: () -> Unit,
    onSignUpSuccess: (User) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val trimmedFullName = fullName.trim()
    val trimmedEmail = email.trim()
    val trimmedPhoneNumber = phoneNumber.trim()

    val isEmailValid = trimmedEmail.isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()

    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }

    val isPasswordStrong = hasMinLength && hasUppercase && hasLowercase && hasDigit
    val doPasswordsMatch = confirmPassword.isNotBlank() && password == confirmPassword
    val isPhoneValid = trimmedPhoneNumber.length >= 10 && trimmedPhoneNumber.all { it.isDigit() }
    val isFullNameValid = trimmedFullName.isNotEmpty()

    val fullNameValidationMessage = when {
        fullName.isBlank() -> null
        !isFullNameValid -> "Full name is required"
        else -> null
    }

    val emailValidationMessage = when {
        email.isBlank() -> null
        !isEmailValid -> "Enter a valid email address"
        else -> null
    }

    val passwordValidationMessage = when {
        password.isBlank() -> null
        !hasMinLength -> "Password must be at least 8 characters"
        !hasUppercase -> "Password must contain at least 1 uppercase letter"
        !hasLowercase -> "Password must contain at least 1 lowercase letter"
        !hasDigit -> "Password must contain at least 1 digit"
        else -> null
    }

    val confirmPasswordValidationMessage = when {
        confirmPassword.isBlank() -> null
        !doPasswordsMatch -> "Passwords do not match"
        else -> null
    }

    val phoneValidationMessage = when {
        phoneNumber.isBlank() -> null
        !isPhoneValid -> "Enter a valid phone number with at least 10 digits"
        else -> null
    }

    val isSignUpEnabled = isFullNameValid &&
            isEmailValid &&
            isPasswordStrong &&
            doPasswordsMatch &&
            isPhoneValid &&
            !isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign Up",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                errorMessage = null
            },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = fullNameValidationMessage != null
        )

        if (fullNameValidationMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = fullNameValidationMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = emailValidationMessage != null
        )

        if (emailValidationMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = emailValidationMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = passwordValidationMessage != null
        )

        if (passwordValidationMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = passwordValidationMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = confirmPasswordValidationMessage != null
        )

        if (confirmPasswordValidationMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = confirmPasswordValidationMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
                errorMessage = null
            },
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            isError = phoneValidationMessage != null
        )

        if (phoneValidationMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phoneValidationMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

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
                isLoading = true
                errorMessage = null

                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        repository.signUp(
                            fullName = trimmedFullName,
                            email = trimmedEmail,
                            password = password,
                            phoneNumber = trimmedPhoneNumber
                        )
                    }

                    isLoading = false

                    when (result) {
                        is AuthResult.Success -> onSignUpSuccess(result.user)
                        is AuthResult.Error -> errorMessage = result.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isSignUpEnabled
        ) {
            Text(if (isLoading) "Creating account..." else "Done")
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