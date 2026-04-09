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