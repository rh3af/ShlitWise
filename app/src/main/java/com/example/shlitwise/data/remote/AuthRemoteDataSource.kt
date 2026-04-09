package com.example.shlitwise.data.remote

import com.example.shlitwise.model.AuthResult
import com.example.shlitwise.model.User

class AuthRemoteDataSource {

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): AuthResult {
        val result = AuthApiService.signUp(
            SignUpRequestDto(
                fullName = fullName,
                email = email,
                password = password,
                phoneNumber = phoneNumber
            )
        )

        return result.fold(
            onSuccess = { response ->
                AuthResult.Success(
                    user = User(
                        id = response.user.id,
                        fullName = response.user.fullName,
                        email = response.user.email,
                        phoneNumber = response.user.phoneNumber
                    ),
                    token = response.token
                )
            },
            onFailure = { error ->
                AuthResult.Error(error.message ?: "Unable to sign up")
            }
        )
    }

    fun signIn(
        email: String,
        password: String
    ): AuthResult {
        val result = AuthApiService.login(
            LoginRequestDto(
                email = email,
                password = password
            )
        )

        return result.fold(
            onSuccess = { response ->
                AuthResult.Success(
                    user = User(
                        id = response.user.id,
                        fullName = response.user.fullName,
                        email = response.user.email,
                        phoneNumber = response.user.phoneNumber
                    ),
                    token = response.token
                )
            },
            onFailure = { error ->
                AuthResult.Error(error.message ?: "Unable to log in")
            }
        )
    }

    fun updateAccount(
        userId: Long,
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String?
    ): Result<User> {
        val result = AuthApiService.updateAccount(
            userId = userId,
            request = UpdateAccountRequestDto(
                fullName = fullName,
                email = email,
                phoneNumber = phoneNumber,
                password = password
            )
        )

        return result.fold(
            onSuccess = { response ->
                Result.success(
                    User(
                        id = response.id,
                        fullName = response.fullName,
                        email = response.email,
                        phoneNumber = response.phoneNumber
                    )
                )
            },
            onFailure = { error ->
                Result.failure(Exception(error.message ?: "Unable to update account"))
            }
        )
    }

    fun lookupParticipant(value: String): Result<User> {
        val result = AuthApiService.lookupParticipant(
            ParticipantLookupRequestDto(value = value)
        )

        return result.fold(
            onSuccess = { response ->
                Result.success(
                    User(
                        id = response.id,
                        fullName = response.fullName,
                        email = response.email,
                        phoneNumber = response.phoneNumber
                    )
                )
            },
            onFailure = { error ->
                Result.failure(Exception(error.message ?: "Unable to lookup participant"))
            }
        )
    }
}