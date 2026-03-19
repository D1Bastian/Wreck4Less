package com.example.wreck4less

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.wreck4less.ui.screens.*
import com.example.wreck4less.ui.theme.Wreck4LessTheme
import com.example.wreck4less.data.repository.JobRepository
import com.example.wreck4less.data.security.SecureStorage
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComposeFoundationFlags.isNonComposedClickableEnabled = false
        setContent {
            Wreck4LessTheme {
                val repository = remember { JobRepository() }
                var session by remember { mutableStateOf(loadSession()) }

                val logout: () -> Unit = {
                    SecureStorage.clearSession()
                    session = null
                }

                if (session == null) {
                    AuthPortal(
                        repository = repository,
                        onLoginSuccess = { session = it }
                    )
                } else {
                    when (session!!.role) {
                        UserRole.CUSTOMER -> CustomerSupportFlow(
                            session = session!!,
                            repository = repository,
                            onLogout = logout
                        )
                        UserRole.MANAGER -> ManagerDashboard(
                            repository = repository,
                            onLogout = logout
                        )
                        UserRole.DRIVER -> DriverDashboard(
                            session = session!!,
                            repository = repository,
                            onLogout = logout
                        )
                        UserRole.ADMIN -> AdminConsole(
                            repository = repository,
                            onLogout = logout
                        )
                    }
                }
            }
        }
    }

    private fun loadSession(): UserSession? {
        val token = SecureStorage.getToken() ?: return null
        val roleValue = SecureStorage.getRole() ?: return null
        val userId = SecureStorage.getUserId() ?: return null
        val fullName = SecureStorage.getFullName() ?: "Operator"
        val role = runCatching { UserRole.valueOf(roleValue.uppercase(Locale.getDefault())) }
            .getOrDefault(UserRole.CUSTOMER)
        return UserSession(
            userId = userId,
            displayName = fullName,
            role = role,
            token = token
        )
    }
}
