package com.example.wreck4less.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// --- RED & BLACK PREMIUM THEME (#FF0000) ---
val BrandRed = Color(0xFFFF0000)
val PureBlack = Color(0xFF000000)
val SurfaceZinc = Color(0xFF121212)
val CardGray = Color(0xFF1C1C1E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WreckerHeader(
    title: String,
    userName: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onMenu: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PureBlack,
            titleContentColor = Color.White
        ),
        title = {
            Column {
                Text(
                    "WRECK4LESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandRed,
                    letterSpacing = 1.sp
                )
                Text(
                    userName.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = if (showBack) onBack else onMenu) {
                Icon(
                    if (showBack) Icons.Default.ArrowBack else Icons.Default.Menu,
                    contentDescription = null,
                    tint = BrandRed
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Notifications */ }) {
                Icon(Icons.Default.Notifications, null, tint = Color.DarkGray)
            }
        }
    )
}

// --- AUTHENTICATION & ROLE SELECTION ---

@Composable
fun AuthPortal(onLoginSuccess: (String) -> Unit) {
    var step by remember { mutableStateOf("auth") } // "auth" or "role"

    if (step == "auth") {
        Column(
            modifier = Modifier.fillMaxSize().background(PureBlack).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(100.dp).background(BrandRed, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.LocalShipping, null, tint = PureBlack, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("WRECK4LESS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp, fontStyle = FontStyle.Italic)
            Text("PRIVATE FLEET SYSTEM", color = BrandRed.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(48.dp))
            OutlinedTextField(
                value = "", onValueChange = {},
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = "", onValueChange = {},
                label = { Text("Secure Password") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
            )

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { step = "role" },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
            ) {
                Text("SIGN IN", fontWeight = FontWeight.Black, color = PureBlack)
            }
            TextButton(onClick = { /* Sign Up */ }) {
                Text("NEW ACCOUNT? REGISTER HERE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        RoleSelectionScreen(onRoleSelected = onLoginSuccess)
    }
}

@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(PureBlack).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("SIGN IN AS...", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        Spacer(Modifier.height(40.dp))
        StaffPortalButton("Customer", Icons.Default.Person) { onRoleSelected("customer") }
        StaffPortalButton("Fleet Manager", Icons.Default.Shield) { onRoleSelected("manager") }
        StaffPortalButton("Fleet Driver", Icons.Default.LocalShipping) { onRoleSelected("driver") }
        StaffPortalButton("System Admin", Icons.Default.Settings) { onRoleSelected("admin") }
    }
}

@Composable
fun StaffPortalButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CardGray),
        border = BorderStroke(1.dp, BrandRed.copy(alpha = 0.2f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = BrandRed)
            Spacer(Modifier.width(16.dp))
            Text(label.uppercase(), fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray)
        }
    }
}

// --- CUSTOMER FLOW (Support Hub) ---

@Composable
fun CustomerSupportFlow(view: String, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    Scaffold(
        containerColor = PureBlack,
        topBar = {
            WreckerHeader(
                title = if (view == "home") "Support Hub" else view.uppercase(),
                userName = "John Doe",
                showBack = view != "home",
                onBack = { onNavigate("home") },
                onMenu = { /* Drawer Logic */ }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            when (view) {
                "home" -> CustomerHome(onNavigate, onLogout)
                "form" -> WreckRequestForm(onSubmit = { onNavigate("pending") })
                "gas" -> GasStationScreen()
                "emergency" -> EmergencyServicesScreen()
                "pending" -> PendingReviewScreen(onConfirmed = { onNavigate("payment") })
                "payment" -> PaymentChoiceScreen(onConfirmed = { onNavigate("tracking") })
                "tracking" -> LiveTrackingScreen()
            }
        }
    }
}

@Composable
fun CustomerHome(onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = BrandRed),
            modifier = Modifier.fillMaxWidth().height(220.dp).clickable { onNavigate("form") }
        ) {
            Column(Modifier.padding(32.dp), verticalArrangement = Arrangement.Center) {
                Text("NEED A\nWRECK?", fontSize = 42.sp, fontWeight = FontWeight.Black, color = PureBlack, lineHeight = 42.sp)
                Text("Tap for private dispatch", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureBlack.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardAction("Gas Nearby", Icons.Default.LocalGasStation, Modifier.weight(1f)) { onNavigate("gas") }
            DashboardAction("Emergency", Icons.Default.ShieldAlert, Modifier.weight(1f)) { onNavigate("emergency") }
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Default.Logout, null, tint = BrandRed)
            Spacer(Modifier.width(8.dp))
            Text("SIGN OUT", color = Color.Gray, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun RowScope.DashboardAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray),
        modifier = modifier.height(140.dp).clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = BrandRed, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun WreckRequestForm(onSubmit: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Wreck Intel", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = "", onValueChange = {}, label = { Text("Make / Model / Year") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
        )
        Spacer(Modifier.height(24.dp))
        Text("Vehicle Damage (4 Photos)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                Box(Modifier.size(70.dp).background(CardGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.DarkGray)
                }
            }
        }
        OutlinedTextField(
            value = "", onValueChange = {}, label = { Text("Damage Report Details") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed)
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(64.dp), // Adjusted height to match specs
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("SUBMIT FOR DISPATCH", fontWeight = FontWeight.Black, color = PureBlack)
        }
    }
}

@Composable
fun PendingReviewScreen(onConfirmed: () -> Unit) {
    var timer by remember { mutableStateOf(300) }
    LaunchedEffect(Unit) {
        while (timer > 0) { delay(1000); timer-- }
        onConfirmed()
    }
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = BrandRed, strokeWidth = 6.dp, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(32.dp))
        Text("AWAITING CONFIRMATION", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("${timer/60}:${(timer%60).toString().padStart(2,'0')}", color = BrandRed, fontSize = 56.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = { /* Dialer */ },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardGray),
            border = BorderStroke(1.dp, BrandRed.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Phone, null, tint = BrandRed)
            Spacer(Modifier.width(12.dp))
            Text("APP STICKING? CALL OPS", color = Color.White, fontWeight = FontWeight.Black)
        }
        TextButton(onClick = onConfirmed, modifier = Modifier.padding(top = 16.dp)) {
            Text("SIMULATE APPROVAL", color = Color.DarkGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun PaymentChoiceScreen(onConfirmed: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("APPROVED QUOTE", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("$195.00", color = Color.White, fontWeight = FontWeight.Black, fontSize = 48.sp)
        Spacer(Modifier.height(32.dp))
        Text("SELECT PAYMENT PROTOCOL", color = BrandRed, fontWeight = FontWeight.Black, fontSize = 10.sp)
        Spacer(Modifier.height(16.dp))

        PaymentItem("Credit / Debit Card", Icons.Default.CreditCard)
        PaymentItem("Direct Bank Transfer", Icons.Default.AccountBalance)
        PaymentItem("Cash on Completion", Icons.Default.Payments)

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onConfirmed,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("CONFIRM & DISPATCH", fontWeight = FontWeight.Black, color = PureBlack)
        }
    }
}

@Composable
fun PaymentItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(70.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray),
        border = BorderStroke(1.dp, Color.DarkGray)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = BrandRed)
            Spacer(Modifier.width(16.dp))
            Text(label.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun LiveTrackingScreen() {
    Column(Modifier.fillMaxSize().background(PureBlack)) {
        Box(Modifier.weight(1f).fillMaxWidth().background(CardGray), contentAlignment = Alignment.Center) {
            Text("LIVE GPS DATA", color = BrandRed.copy(alpha = 0.3f), fontWeight = FontWeight.Black, fontSize = 32.sp)
            Icon(Icons.Default.LocalShipping, null, tint = BrandRed, modifier = Modifier.size(64.dp))
        }
        Card(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceZinc),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(60.dp).background(BrandRed, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = PureBlack)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Big Mike", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("ETA: 6 MIN", color = BrandRed, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = BrandRed)) {
                        Icon(Icons.Default.Phone, null, tint = PureBlack)
                        Spacer(Modifier.width(8.dp))
                        Text("CALL DRIVER", color = PureBlack, fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }
                    Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = CardGray)) {
                        Icon(Icons.Default.Message, null, tint = BrandRed)
                        Spacer(Modifier.width(8.dp))
                        Text("MESSAGE DRIVER", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// --- STAFF PERSPECTIVES (Fleet Portal) ---

@Composable
fun ManagerDashboard(onLogout: () -> Unit) {
    Scaffold(
        containerColor = PureBlack,
        topBar = { WreckerHeader("Fleet Portal", "Manager", false, {}, {}) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(24.dp)) {
            Text("JOB DISPATCH QUEUE", color = BrandRed, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listOf("WRK-7721: Tesla Model 3", "WRK-7725: BMW X5")) { job ->
                    Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(job, color = Color.White, modifier = Modifier.weight(1f))
                            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = BrandRed)) {
                                Text("ASSIGN", color = PureBlack, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CardGray)) {
                Text("SIGN OUT OF FLEET")
            }
        }
    }
}

@Composable
fun AdminConsole(onLogout: () -> Unit) {
    Scaffold(
        containerColor = PureBlack,
        topBar = { WreckerHeader("Fleet Portal", "Admin", false, {}, {}) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(24.dp)) {
            Text("SYSTEM ADMINISTRATION", color = Color.White, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(32.dp))
            StaffPortalButton("Provision Driver", Icons.Default.AddCircle) {}
            StaffPortalButton("Financial Audit", Icons.Default.BarChart) {}
            StaffPortalButton("Fleet Logs", Icons.Default.History) {}
            Spacer(Modifier.weight(1f))
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BrandRed)) {
                Text("TERMINATE SESSION", color = PureBlack, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun DriverDashboard(onLogout: () -> Unit) {
    Scaffold(
        containerColor = PureBlack,
        topBar = { WreckerHeader("Fleet Portal", "Big Mike", false, {}, {}) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(24.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
                Column(Modifier.padding(24.dp)) {
                    Text("ACTIVE DISPATCH", color = BrandRed, fontWeight = FontWeight.Black)
                    Text("72 Pine St, Financial District", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BrandRed)) {
                        Text("VIEW DAMAGE IMAGES", color = PureBlack, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CardGray)) {
                Text("GO OFFLINE")
            }
        }
    }
}

// --- UTILITY SCREENS ---

@Composable fun GasStationScreen() { Box(Modifier.fillMaxSize().background(PureBlack), contentAlignment = Alignment.Center) { Text("GAS STATIONS NEARBY", color = BrandRed, fontWeight = FontWeight.Black) } }
@Composable fun EmergencyServicesScreen() { Box(Modifier.fillMaxSize().background(PureBlack), contentAlignment = Alignment.Center) { Text("DIALING 1-800-WRECK-HQ", color = BrandRed, fontWeight = FontWeight.Black) } }