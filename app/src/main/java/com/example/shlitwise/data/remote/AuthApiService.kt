package com.example.shlitwise.data.remote

import org.json.JSONArray
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

        return postForAuth("/signup", body)
    }

    fun login(request: LoginRequestDto): Result<AuthResponseDto> {
        val body = JSONObject().apply {
            put("email", request.email)
            put("password", request.password)
        }

        return postForAuth("/login", body)
    }

    fun updateAccount(userId: Long, request: UpdateAccountRequestDto): Result<UserDto> {
        val body = JSONObject().apply {
            put("fullName", request.fullName)
            put("email", request.email)
            put("phoneNumber", request.phoneNumber)
            put("password", request.password ?: "")
        }

        return putForUser("/account/$userId", body)
    }

    fun lookupParticipant(request: ParticipantLookupRequestDto): Result<UserDto> {
        val body = JSONObject().apply {
            put("value", request.value)
        }

        return postForUser("/users/lookup", body)
    }

    fun saveExpense(request: SaveExpenseRequestDto): Result<ExpenseResponseDto> {
        val participantsArray = JSONArray().apply {
            request.participants.forEach { participant ->
                put(
                    JSONObject().apply {
                        put("userId", participant.userId)
                        put("displayName", participant.displayName)
                    }
                )
            }
        }

        val body = JSONObject().apply {
            put("createdByUserId", request.createdByUserId)
            put("description", request.description)
            put("amount", request.amount)
            put("participants", participantsArray)
            if (request.paidByUserId != null) {
                put("paidByUserId", request.paidByUserId)
            } else {
                put("paidByUserId", JSONObject.NULL)
            }
            put("paidByDisplayName", request.paidByDisplayName)
            put("splitType", request.splitType)
            if (request.singleParticipantSplitOption != null) {
                put("singleParticipantSplitOption", request.singleParticipantSplitOption)
            } else {
                put("singleParticipantSplitOption", JSONObject.NULL)
            }
        }

        return postForExpense("/expenses", body)
    }

    fun getActivityExpenses(userId: Long): Result<List<ActivityExpenseResponseDto>> {
        return getForActivity("/expenses/activity/$userId")
    }

    fun getFriendBalances(userId: Long): Result<List<FriendBalanceResponseDto>> {
        return getForFriendBalances("/friends/balances/$userId")
    }

    private fun postForAuth(path: String, jsonBody: JSONObject): Result<AuthResponseDto> {
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
            Result.failure(Exception("Unable to connect to server. Make sure backend is running and phone is on same Wi-Fi."))
        }
    }

    private fun postForUser(path: String, jsonBody: JSONObject): Result<UserDto> {
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
                Result.success(parseUserResponse(responseText))
            } else {
                Result.failure(Exception(parseErrorMessage(responseText)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unable to connect to server. Make sure backend is running and phone is on same Wi-Fi."))
        }
    }

    private fun postForExpense(path: String, jsonBody: JSONObject): Result<ExpenseResponseDto> {
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
                Result.success(parseExpenseResponse(responseText))
            } else {
                Result.failure(Exception(parseErrorMessage(responseText)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unable to connect to server. Make sure backend is running and phone is on same Wi-Fi."))
        }
    }

    private fun getForActivity(path: String): Result<List<ActivityExpenseResponseDto>> {
        return try {
            val url = URL("$BASE_URL$path")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
            }

            if (responseCode in 200..299) {
                Result.success(parseActivityExpenseList(responseText))
            } else {
                Result.failure(Exception(parseErrorMessage(responseText)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unable to connect to server. Make sure backend is running and phone is on same Wi-Fi."))
        }
    }

    private fun getForFriendBalances(path: String): Result<List<FriendBalanceResponseDto>> {
        return try {
            val url = URL("$BASE_URL$path")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
            }

            if (responseCode in 200..299) {
                Result.success(parseFriendBalanceList(responseText))
            } else {
                Result.failure(Exception(parseErrorMessage(responseText)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unable to connect to server. Make sure backend is running and phone is on same Wi-Fi."))
        }
    }

    private fun putForUser(path: String, jsonBody: JSONObject): Result<UserDto> {
        return try {
            val url = URL("$BASE_URL$path")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "PUT"
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
                Result.success(parseUserResponse(responseText))
            } else {
                Result.failure(Exception(parseErrorMessage(responseText)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unable to connect to server. Make sure backend is running and phone is on same Wi-Fi."))
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

    private fun parseUserResponse(response: String): UserDto {
        val userJson = JSONObject(response)

        return UserDto(
            id = userJson.getLong("id"),
            fullName = userJson.getString("fullName"),
            email = userJson.getString("email"),
            phoneNumber = userJson.getString("phoneNumber")
        )
    }

    private fun parseExpenseResponse(response: String): ExpenseResponseDto {
        val json = JSONObject(response)
        val participantsJson = json.getJSONArray("participants")
        val participants = mutableListOf<ExpenseParticipantRequestDto>()

        for (i in 0 until participantsJson.length()) {
            val participantJson = participantsJson.getJSONObject(i)
            participants.add(
                ExpenseParticipantRequestDto(
                    userId = participantJson.getLong("userId"),
                    displayName = participantJson.getString("displayName")
                )
            )
        }

        return ExpenseResponseDto(
            id = json.getLong("id"),
            createdByUserId = json.getLong("createdByUserId"),
            description = json.getString("description"),
            amount = json.getDouble("amount"),
            paidByUserId = if (json.isNull("paidByUserId")) null else json.getLong("paidByUserId"),
            paidByDisplayName = json.getString("paidByDisplayName"),
            splitType = json.getString("splitType"),
            singleParticipantSplitOption = if (json.isNull("singleParticipantSplitOption")) null else json.getString("singleParticipantSplitOption"),
            participants = participants
        )
    }

    private fun parseActivityExpenseList(response: String): List<ActivityExpenseResponseDto> {
        val jsonArray = JSONArray(response)
        val items = mutableListOf<ActivityExpenseResponseDto>()

        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            val participantsJson = json.getJSONArray("participants")
            val participants = mutableListOf<ExpenseParticipantRequestDto>()

            for (j in 0 until participantsJson.length()) {
                val participantJson = participantsJson.getJSONObject(j)
                participants.add(
                    ExpenseParticipantRequestDto(
                        userId = participantJson.getLong("userId"),
                        displayName = participantJson.getString("displayName")
                    )
                )
            }

            items.add(
                ActivityExpenseResponseDto(
                    id = json.getLong("id"),
                    description = json.getString("description"),
                    amount = json.getDouble("amount"),
                    paidByUserId = if (json.isNull("paidByUserId")) null else json.getLong("paidByUserId"),
                    paidByDisplayName = json.getString("paidByDisplayName"),
                    splitType = json.getString("splitType"),
                    singleParticipantSplitOption = if (json.isNull("singleParticipantSplitOption")) null else json.getString("singleParticipantSplitOption"),
                    participants = participants,
                    createdAt = json.getLong("createdAt")
                )
            )
        }

        return items
    }

    private fun parseFriendBalanceList(response: String): List<FriendBalanceResponseDto> {
        val jsonArray = JSONArray(response)
        val items = mutableListOf<FriendBalanceResponseDto>()

        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            items.add(
                FriendBalanceResponseDto(
                    friendUserId = json.getLong("friendUserId"),
                    friendDisplayName = json.getString("friendDisplayName"),
                    balanceAmount = json.getDouble("balanceAmount"),
                    balanceState = json.getString("balanceState")
                )
            )
        }

        return items
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