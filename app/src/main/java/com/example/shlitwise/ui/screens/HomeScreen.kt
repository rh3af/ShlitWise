package com.example.shlitwise.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.shlitwise.data.AuthRepository
import com.example.shlitwise.model.User
import com.example.shlitwise.navigation.HomeTab

@Composable
fun HomeScreen(
    repository: AuthRepository,
    user: User?,
    onLogoutClick: () -> Unit,
    onUserUpdated: (User) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.FRIENDS) }
    var showEditAccount by rememberSaveable { mutableStateOf(false) }
    var showAddExpense by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            val shouldShowFab =
                !showEditAccount &&
                        !showAddExpense &&
                        (selectedTab == HomeTab.FRIENDS || selectedTab == HomeTab.ACTIVITY)

            if (shouldShowFab) {
                FloatingActionButton(
                    onClick = { showAddExpense = true }
                ) {
                    Text("Add Expense")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.FRIENDS,
                    onClick = {
                        selectedTab = HomeTab.FRIENDS
                        showEditAccount = false
                        showAddExpense = false
                    },
                    icon = { Text("👥") },
                    label = { Text("Friends") }
                )

                NavigationBarItem(
                    selected = selectedTab == HomeTab.ACTIVITY,
                    onClick = {
                        selectedTab = HomeTab.ACTIVITY
                        showEditAccount = false
                        showAddExpense = false
                    },
                    icon = { Text("📋") },
                    label = { Text("Activity") }
                )

                NavigationBarItem(
                    selected = selectedTab == HomeTab.ACCOUNT,
                    onClick = {
                        selectedTab = HomeTab.ACCOUNT
                        showEditAccount = false
                        showAddExpense = false
                    },
                    icon = { Text("👤") },
                    label = { Text("Account") }
                )
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when {
            showAddExpense -> {
                AddExpenseScreen(
                    modifier = contentModifier,
                    repository = repository,
                    onBackClick = { showAddExpense = false },
                    onSaveClick = { showAddExpense = false }
                )
            }

            selectedTab == HomeTab.FRIENDS -> {
                FriendsScreen(modifier = contentModifier)
            }

            selectedTab == HomeTab.ACTIVITY -> {
                ActivityScreen(modifier = contentModifier)
            }

            selectedTab == HomeTab.ACCOUNT && showEditAccount -> {
                EditAccountScreen(
                    modifier = contentModifier,
                    repository = repository,
                    currentUser = user,
                    onBackClick = { showEditAccount = false },
                    onSaveClick = { updatedUser ->
                        onUserUpdated(updatedUser)
                        showEditAccount = false
                    }
                )
            }

            selectedTab == HomeTab.ACCOUNT -> {
                AccountScreen(
                    modifier = contentModifier,
                    user = user,
                    onEditAccountClick = { showEditAccount = true },
                    onLogoutClick = onLogoutClick
                )
            }
        }
    }
}