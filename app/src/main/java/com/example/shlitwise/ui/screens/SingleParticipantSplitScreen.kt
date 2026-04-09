package com.example.shlitwise.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shlitwise.model.SingleParticipantSplitOption
import com.example.shlitwise.model.toDisplayText

@Composable
fun SingleParticipantSplitScreen(
    otherUserName: String,
    selectedOption: SingleParticipantSplitOption,
    onOptionSelected: (SingleParticipantSplitOption) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler {
        onBackClick()
    }

    val options = listOf(
        SingleParticipantSplitOption.YOU_PAID_SPLIT_EQUALLY,
        SingleParticipantSplitOption.YOU_ARE_OWED_FULL_AMOUNT,
        SingleParticipantSplitOption.OTHER_PAID_SPLIT_EQUALLY,
        SingleParticipantSplitOption.OTHER_IS_OWED_FULL_AMOUNT
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("← Back")
            }

            Text(
                text = "How Was this Expense Split",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        options.forEach { option ->
            val isSelected = option == selectedOption

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = option.toDisplayText(otherUserName),
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSelected) {
                        Button(
                            onClick = { onOptionSelected(option) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Selected")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onOptionSelected(option) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Choose")
                        }
                    }
                }
            }
        }
    }
}