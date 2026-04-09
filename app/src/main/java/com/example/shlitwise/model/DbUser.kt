package com.example.shlitwise.model

data class DbUser(
    val id: Long,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val phoneNumber: String
)