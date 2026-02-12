package com.example.wreck4less

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.wreck4less.ui.screens.*
import com.example.wreck4less.ui.theme.Wreck4LessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Wreck4LessTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var role by remember { mutableStateOf("customer") }
                var currentView by remember { mutableStateOf("home") }

                // Global Back Navigation
                BackHandler(enabled = isLoggedIn && currentView != "home") {
                    currentView = "home"
                }

                if (!isLoggedIn) {
                    AuthPortal(onLoginSuccess = { selectedRole ->
                        role = selectedRole
                        isLoggedIn = true
                        currentView = "home"
                    })
                } else {
                    when (role) {
                        "customer" -> CustomerSupportFlow(
                            view = currentView,
                            onNavigate = { currentView = it },
                            onLogout = { isLoggedIn = false }
                        )
                        "manager" -> ManagerDashboard(onLogout = { isLoggedIn = false })
                        "driver" -> DriverTerminal(onLogout = { isLoggedIn = false })
                        "admin" -> AdminConsole(onLogout = { isLoggedIn = false })
                    }
                }
            }
        }
    }
}