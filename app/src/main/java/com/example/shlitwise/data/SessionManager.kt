package com.example.shlitwise.data

import android.content.Context
import com.example.shlitwise.model.User

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("shlitwise_session", Context.MODE_PRIVATE)

    fun saveSession(user: User, token: String) {
        prefs.edit()
            .putLong("user_id", user.id)
            .putString("full_name", user.fullName)
            .putString("email", user.email)
            .putString("phone_number", user.phoneNumber)
            .putString("auth_token", token)
            .apply()
    }

    fun getCurrentUser(): User? {
        val userId = prefs.getLong("user_id", -1L)
        if (userId == -1L) return null

        return User(
            id = userId,
            fullName = prefs.getString("full_name", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            phoneNumber = prefs.getString("phone_number", "") ?: ""
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}