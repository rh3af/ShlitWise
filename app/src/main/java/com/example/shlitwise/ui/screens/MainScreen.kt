package com.example.shlitwise.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shlitwise.ui.theme.ShlitWiseTheme

@Composable
fun MainScreen(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ShlitWise",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Split expenses with friends",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign In with Google")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ShlitWiseTheme {
        MainScreen(
            onSignUpClick = {},
            onSignInClick = {},
            onGoogleSignInClick = {}
        )
    }
}