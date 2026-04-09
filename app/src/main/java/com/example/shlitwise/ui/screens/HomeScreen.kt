package com.example.shlitwise.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shlitwise.model.User

@Composable
fun HomeScreen(
    user: User?,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to ShlitWise",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Logged in as ${user?.fullName ?: "User"}",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user?.email ?: "",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}