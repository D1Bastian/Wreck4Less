package com.example.wreck4less

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.wreck4less.ui.screens.*
import com.example.wreck4less.ui.theme.Wreck4LessTheme

/**
 * MainActivity manages the navigation state for the Wreck4Less application.
 * It coordinates transitions between the Customer flow and Driver flow.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Wreck4LessTheme {
                var currentView by remember { mutableStateOf("home") }
                var role by remember { mutableStateOf("customer") } // "customer" or "driver"

                // Handle back button behavior for deep navigation
                BackHandler(enabled = currentView != "home") {
                    currentView = when (currentView) {
                        "details" -> "home"
                        "pricing" -> "details"
                        "payment" -> "pricing"
                        else -> "home"
                    }
                }

                if (role == "driver") {
                    DriverDashboard(onSwitchRole = { role = "customer"; currentView = "home" })
                } else {
                    when (currentView) {
                        "home" -> CustomerHome(
                            onStartRequest = { currentView = "details" },
                            onSwitchRole = { role = "driver" }
                        )
                        "details" -> VehicleDetailsScreen(
                            onNext = { currentView = "pricing" },
                            onBack = { currentView = "home" }
                        )
                        "pricing" -> PricingOptionsScreen(
                            onNext = { currentView = "payment" },
                            onBack = { currentView = "details" }
                        )
                        "payment" -> PaymentScreen(
                            onConfirm = { currentView = "searching" },
                            onBack = { currentView = "pricing" }
                        )
                        "searching" -> SearchingScreen(
                            onDriverAssigned = { currentView = "active_job" }
                        )
                        "active_job" -> ActiveJobScreen(
                            onComplete = { currentView = "home" }
                        )
                    }
                }
            }
        }
    }
}