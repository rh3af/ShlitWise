package com.example.shlitwise.data

import com.example.shlitwise.model.AuthResult

interface AuthApi {
    fun signUp(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): AuthResult

    fun signIn(
        email: String,
        password: String
    ): AuthResult
}