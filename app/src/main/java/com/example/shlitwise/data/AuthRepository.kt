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

        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }

        if (normalizedPhoneNumber.length < 10) {
            return AuthResult.Error("Enter a valid phone number")
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

    private fun generateToken(): String = UUID.randomUUID().toString()

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}