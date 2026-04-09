package com.example.shlitwise.data.remote

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AuthApiService {

    private const val BASE_URL = "http://192.168.2.140:8000"

    fun signUp(request: SignUpRequestDto): Result<AuthResponseDto> {
        val body = JSONObject().apply {
            put("fullName", request.fullName)
            put("email", request.email)
            put("password", request.password)
            put("phoneNumber", request.phoneNumber)
        }

        return post("/signup", body)
    }

    fun login(request: LoginRequestDto): Result<AuthResponseDto> {
        val body = JSONObject().apply {
            put("email", request.email)
            put("password", request.password)
        }

        return post("/login", body)
    }

    private fun post(path: String, jsonBody: JSONObject): Result<AuthResponseDto> {
        return try {
            val url = URL("$BASE_URL$path")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
            }

            if (responseCode in 200..299) {
                Result.success(parseAuthResponse(responseText))
            } else {
                Result.failure(Exception(parseErrorMessage(responseText)))
            }
        } catch (e: Exception) {
            Result.failure(
                Exception(
                    "Unable to connect to server. Make sure backend is running and phone is on same Wi-Fi."
                )
            )
        }
    }

    private fun parseAuthResponse(response: String): AuthResponseDto {
        val json = JSONObject(response)
        val userJson = json.getJSONObject("user")

        return AuthResponseDto(
            token = json.getString("token"),
            user = UserDto(
                id = userJson.getLong("id"),
                fullName = userJson.getString("fullName"),
                email = userJson.getString("email"),
                phoneNumber = userJson.getString("phoneNumber")
            )
        )
    }

    private fun parseErrorMessage(response: String): String {
        return try {
            val json = JSONObject(response)
            json.optString("detail", "Something went wrong")
        } catch (e: Exception) {
            "Something went wrong"
        }
    }
}