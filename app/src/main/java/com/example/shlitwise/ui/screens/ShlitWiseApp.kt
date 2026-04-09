package com.example.shlitwise.ui.screens

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.shlitwise.data.AuthRepository
import com.example.shlitwise.data.SessionManager
import com.example.shlitwise.data.remote.AuthRemoteDataSource
import com.example.shlitwise.model.User
import com.example.shlitwise.navigation.AppScreen

@Composable
fun ShlitWiseApp() {
    val context = LocalContext.current

    val repository = remember {
        AuthRepository(
            remoteDataSource = AuthRemoteDataSource(),
            sessionManager = SessionManager(context.applicationContext)
        )
    }

    val existingUser = remember { repository.getCurrentUser() }
    var currentUser by rememberSaveable { mutableStateOf(existingUser) }
    var currentScreen by rememberSaveable {
        mutableStateOf(if (existingUser != null) AppScreen.HOME else AppScreen.MAIN)
    }

    when (currentScreen) {
        AppScreen.MAIN -> MainScreen(
            onSignUpClick = { currentScreen = AppScreen.SIGN_UP },
            onSignInClick = { currentScreen = AppScreen.SIGN_IN },
            onGoogleSignInClick = {
                Toast.makeText(
                    context,
                    "Google Sign-In will be connected later",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        AppScreen.SIGN_IN -> SignInScreen(
            repository = repository,
            onBackClick = { currentScreen = AppScreen.MAIN },
            onLoginSuccess = { user: User ->
                currentUser = user
                currentScreen = AppScreen.HOME
            }
        )

        AppScreen.SIGN_UP -> SignUpScreen(
            repository = repository,
            onBackClick = { currentScreen = AppScreen.MAIN },
            onSignUpSuccess = { user: User ->
                currentUser = user
                currentScreen = AppScreen.HOME
            }
        )

        AppScreen.HOME -> HomeScreen(
            user = currentUser,
            onLogoutClick = {
                repository.signOut()
                currentUser = null
                currentScreen = AppScreen.MAIN
            }
        )
    }
}