package com.example.shlitwise.data

import android.util.Patterns
import com.example.shlitwise.data.remote.ActivityExpenseResponseDto
import com.example.shlitwise.data.remote.AuthRemoteDataSource
import com.example.shlitwise.data.remote.ExpenseResponseDto
import com.example.shlitwise.data.remote.FriendBalanceResponseDto
import com.example.shlitwise.model.AuthResult
import com.example.shlitwise.model.User

class AuthRepository(
    private val remoteDataSource: AuthRemoteDataSource,
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

        val result = remoteDataSource.signUp(
            fullName = normalizedFullName,
            email = normalizedEmail,
            password = password,
            phoneNumber = normalizedPhoneNumber
        )

        if (result is AuthResult.Success) {
            sessionManager.saveSession(result.user, result.token)
        }

        return result
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

        val result = remoteDataSource.signIn(
            email = normalizedEmail,
            password = password
        )

        if (result is AuthResult.Success) {
            sessionManager.saveSession(result.user, result.token)
        }

        return result
    }

    fun updateCurrentUser(
        user: User,
        newPassword: String?
    ): Result<User> {
        val normalizedFullName = user.fullName.trim()
        val normalizedEmail = user.email.trim().lowercase()
        val normalizedPhoneNumber = user.phoneNumber.trim()
        val normalizedPassword = newPassword?.trim().orEmpty()

        if (normalizedFullName.isEmpty()) {
            return Result.failure(Exception("Full name is required"))
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return Result.failure(Exception("Enter a valid email address"))
        }

        if (!isValidPhoneNumber(normalizedPhoneNumber)) {
            return Result.failure(Exception("Enter a valid phone number with at least 10 digits"))
        }

        if (normalizedPassword.isNotBlank() && !isStrongPassword(normalizedPassword)) {
            return Result.failure(
                Exception("Password must be at least 8 characters and include uppercase, lowercase, and a digit")
            )
        }

        val result = remoteDataSource.updateAccount(
            userId = user.id,
            fullName = normalizedFullName,
            email = normalizedEmail,
            phoneNumber = normalizedPhoneNumber,
            password = normalizedPassword.ifBlank { null }
        )

        return result.fold(
            onSuccess = { updatedUser ->
                sessionManager.updateUser(updatedUser)
                Result.success(updatedUser)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    fun lookupParticipant(value: String): Result<User> {
        val normalizedValue = value.trim()

        if (normalizedValue.isBlank()) {
            return Result.failure(Exception("Enter an email address or phone number"))
        }

        return remoteDataSource.lookupParticipant(normalizedValue)
    }

    fun saveExpense(
        createdByUserId: Long,
        description: String,
        amount: Double,
        participants: List<User>,
        paidByUserId: Long?,
        paidByDisplayName: String,
        splitType: String,
        singleParticipantSplitOption: String?
    ): Result<ExpenseResponseDto> {
        val normalizedDescription = description.trim()
        val normalizedPaidByDisplayName = paidByDisplayName.trim()

        if (createdByUserId <= 0) {
            return Result.failure(Exception("Invalid expense creator"))
        }

        if (normalizedDescription.isBlank()) {
            return Result.failure(Exception("Description is required"))
        }

        if (amount <= 0.0) {
            return Result.failure(Exception("Amount must be greater than 0"))
        }

        if (participants.isEmpty()) {
            return Result.failure(Exception("Add at least one participant"))
        }

        if (normalizedPaidByDisplayName.isBlank()) {
            return Result.failure(Exception("Paid by selection is required"))
        }

        if (splitType.isBlank()) {
            return Result.failure(Exception("Split type is required"))
        }

        return remoteDataSource.saveExpense(
            createdByUserId = createdByUserId,
            description = normalizedDescription,
            amount = amount,
            participants = participants,
            paidByUserId = paidByUserId,
            paidByDisplayName = normalizedPaidByDisplayName,
            splitType = splitType,
            singleParticipantSplitOption = singleParticipantSplitOption
        )
    }

    fun getActivityExpenses(userId: Long): Result<List<ActivityExpenseResponseDto>> {
        if (userId <= 0) {
            return Result.failure(Exception("Invalid user id"))
        }

        return remoteDataSource.getActivityExpenses(userId)
    }

    fun getFriendBalances(userId: Long): Result<List<FriendBalanceResponseDto>> {
        if (userId <= 0) {
            return Result.failure(Exception("Invalid user id"))
        }

        return remoteDataSource.getFriendBalances(userId)
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
}