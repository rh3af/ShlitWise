package com.example.shlitwise.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shlitwise.data.AuthRepository
import com.example.shlitwise.data.remote.FriendBalanceResponseDto
import com.example.shlitwise.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FriendsScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    currentUser: User?
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var friendBalances by remember { mutableStateOf<List<FriendBalanceResponseDto>>(emptyList()) }

    LaunchedEffect(currentUser?.id) {
        if (currentUser == null) {
            isLoading = false
            errorMessage = "You must be logged in to view friends"
            friendBalances = emptyList()
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null

        val result = withContext(Dispatchers.IO) {
            repository.getFriendBalances(currentUser.id)
        }

        result.fold(
            onSuccess = { items ->
                friendBalances = items
                errorMessage = null
            },
            onFailure = { error ->
                friendBalances = emptyList()
                errorMessage = error.message ?: "Unable to load friend balances"
            }
        )

        isLoading = false
    }

    when {
        isLoading -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Loading friends...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        !errorMessage.isNullOrBlank() -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Friends",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        friendBalances.isEmpty() -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Friends",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Add friends and expenses",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Friends",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(friendBalances, key = { it.friendUserId }) { item ->
                    FriendBalanceCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun FriendBalanceCard(item: FriendBalanceResponseDto) {
    val balanceText: String
    val balanceColor: Color

    when (item.balanceState) {
        "YOU_OWE" -> {
            balanceText = "You owe $${"%.2f".format(item.balanceAmount)}"
            balanceColor = Color.Red
        }

        "THEY_OWE_YOU" -> {
            balanceText = "Owes you $${"%.2f".format(item.balanceAmount)}"
            balanceColor = Color(0xFF1B8A3B)
        }

        else -> {
            balanceText = "Balanced"
            balanceColor = Color.Black
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.friendDisplayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = balanceText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = balanceColor
            )
        }
    }
}