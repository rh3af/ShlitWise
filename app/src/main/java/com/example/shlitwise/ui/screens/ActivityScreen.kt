package com.example.shlitwise.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shlitwise.data.AuthRepository
import com.example.shlitwise.data.remote.ActivityExpenseResponseDto
import com.example.shlitwise.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    currentUser: User?
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var activityItems by remember { mutableStateOf<List<ActivityExpenseResponseDto>>(emptyList()) }

    LaunchedEffect(currentUser?.id) {
        if (currentUser == null) {
            isLoading = false
            errorMessage = "You must be logged in to view activity"
            activityItems = emptyList()
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null

        val result = withContext(Dispatchers.IO) {
            repository.getActivityExpenses(currentUser.id)
        }

        result.fold(
            onSuccess = { items ->
                activityItems = items
                errorMessage = null
            },
            onFailure = { error ->
                activityItems = emptyList()
                errorMessage = error.message ?: "Unable to load activity"
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
                    text = "Loading activity...",
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
                    text = "Activity",
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

        activityItems.isEmpty() -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Activity",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Add expenses to check out the activity",
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
                        text = "Activity",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(activityItems, key = { it.id }) { item ->
                    ActivityExpenseCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun ActivityExpenseCard(item: ActivityExpenseResponseDto) {
    val participantNames = item.participants.joinToString(", ") { it.displayName }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.description,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$${"%.2f".format(item.amount)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Paid by: ${item.paidByDisplayName}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Participants: $participantNames",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (item.singleParticipantSplitOption != null) {
                    "Split: ${item.singleParticipantSplitOption}"
                } else {
                    "Split: ${item.splitType}"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}