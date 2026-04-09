package com.example.shlitwise.data

import android.util.Patterns
import com.example.shlitwise.model.AuthResult
import com.example.shlitwise.model.User
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(
    private val dbHelper: ShlitWiseDbHelper,
    private val sessionManager: SessionManager
) : AuthApi {

    override fun signUp(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): AuthResult {
        val normalizedFullName = fullName.trim()
        val normalizedEmail = email.trim().lowercase()
        val normalizedPhoneNumber = phoneNumber.trim()

        if (normalizedFullName.isEmpty()) {
            return AuthResult.Error("Full name is required")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return AuthResult.Error("Enter a valid email address")
        }

        if (!isStrongPassword(password)) {
            return AuthResult.Error(
                "Password must be at least 8 characters and include uppercase, lowercase, and a digit"
            )
        }

        if (!isValidPhoneNumber(normalizedPhoneNumber)) {
            return AuthResult.Error("Enter a valid phone number with at least 10 digits")
        }

        if (dbHelper.getUserByEmail(normalizedEmail) != null) {
            return AuthResult.Error("An account already exists for this email")
        }

        val userId = dbHelper.insertUser(
            fullName = normalizedFullName,
            email = normalizedEmail,
            passwordHash = hashPassword(password),
            phoneNumber = normalizedPhoneNumber
        )

        if (userId == -1L) {
            return AuthResult.Error("Unable to create account right now")
        }

        val user = User(
            id = userId,
            fullName = normalizedFullName,
            email = normalizedEmail,
            phoneNumber = normalizedPhoneNumber
        )

        val token = generateToken()
        sessionManager.saveSession(user, token)

        return AuthResult.Success(user, token)
    }

    override fun signIn(
        email: String,
        password: String
    ): AuthResult {
        val normalizedEmail = email.trim().lowercase()

        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return AuthResult.Error("Enter a valid email address")
        }

        if (password.isBlank()) {
            return AuthResult.Error("Password is required")
        }

        val dbUser = dbHelper.getUserByEmail(normalizedEmail)
            ?: return AuthResult.Error("No account found for this email")

        if (dbUser.passwordHash != hashPassword(password)) {
            return AuthResult.Error("Invalid email or password")
        }

        val user = User(
            id = dbUser.id,
            fullName = dbUser.fullName,
            email = dbUser.email,
            phoneNumber = dbUser.phoneNumber
        )

        val token = generateToken()
        sessionManager.saveSession(user, token)

        return AuthResult.Success(user, token)
    }

    fun getCurrentUser(): User? = sessionManager.getCurrentUser()

    fun signOut() {
        sessionManager.clearSession()
    }

    private fun isStrongPassword(password: String): Boolean {
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        return hasMinLength && hasUppercase && hasLowercase && hasDigit
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.length >= 10 && phoneNumber.all { it.isDigit() }
    }

    private fun generateToken(): String = UUID.randomUUID().toString()

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}