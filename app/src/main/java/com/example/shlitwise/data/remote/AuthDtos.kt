package com.example.shlitwise.data.remote

data class SignUpRequestDto(
    val fullName: String,
    val email: String,
    val password: String,
    val phoneNumber: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class UpdateAccountRequestDto(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String?
)

data class ParticipantLookupRequestDto(
    val value: String
)

data class ExpenseParticipantRequestDto(
    val userId: Long,
    val displayName: String
)

data class SaveExpenseRequestDto(
    val createdByUserId: Long,
    val description: String,
    val amount: Double,
    val participants: List<ExpenseParticipantRequestDto>,
    val paidByUserId: Long?,
    val paidByDisplayName: String,
    val splitType: String,
    val singleParticipantSplitOption: String?
)

data class ExpenseResponseDto(
    val id: Long,
    val createdByUserId: Long,
    val description: String,
    val amount: Double,
    val paidByUserId: Long?,
    val paidByDisplayName: String,
    val splitType: String,
    val singleParticipantSplitOption: String?,
    val participants: List<ExpenseParticipantRequestDto>
)

data class ActivityExpenseResponseDto(
    val id: Long,
    val description: String,
    val amount: Double,
    val paidByUserId: Long?,
    val paidByDisplayName: String,
    val splitType: String,
    val singleParticipantSplitOption: String?,
    val participants: List<ExpenseParticipantRequestDto>,
    val createdAt: Long
)

data class FriendBalanceResponseDto(
    val friendUserId: Long,
    val friendDisplayName: String,
    val balanceAmount: Double,
    val balanceState: String
)

data class UserDto(
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String
)

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)