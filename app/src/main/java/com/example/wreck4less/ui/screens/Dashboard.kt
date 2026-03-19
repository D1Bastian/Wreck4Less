package com.example.wreck4less.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Job
import android.util.Log
import com.example.wreck4less.data.model.*
import com.example.wreck4less.data.security.SecureStorage
import com.example.wreck4less.data.repository.GasStation
import com.example.wreck4less.data.repository.JobRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

// --- RED & BLACK PREMIUM THEME (#FF0000) ---
val BrandRed = Color(0xFFFF0000)
val PureBlack = Color(0xFF000000)
val SurfaceZinc = Color(0xFF121212)
val CardGray = Color(0xFF1C1C1E)

enum class UserRole { CUSTOMER, MANAGER, DRIVER, ADMIN }

data class UserSession(
    val userId: String,
    val displayName: String,
    val role: UserRole,
    val token: String
)

data class CustomerJobState(
    val jobId: String,
    val status: String,
    val supportLine: String,
    val confirmedRate: Double,
    val driverId: String?,
    val driverLocation: DriverLocation?,
    val intel: WreckIntel
)

data class MapMarker(
    val title: String,
    val lat: Double,
    val lon: Double
)

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
                    userName.uppercase(Locale.getDefault()),
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
                    if (showBack) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
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
fun AuthPortal(
    repository: JobRepository,
    onLoginSuccess: (UserSession) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isRegister by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submitLogin() {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedName = fullName.trim()
        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Email and password are required.") }
            return
        }
        if (isRegister && trimmedName.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Full name is required.") }
            return
        }
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val response = if (isRegister) {
                    repository.register(
                        RegisterRequest(
                            email = trimmedEmail,
                            password = trimmedPassword,
                            full_name = trimmedName
                        )
                    )
                } else {
                    repository.login(
                        LoginRequest(
                            email = trimmedEmail,
                            password = trimmedPassword
                        )
                    )
                }
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val role = runCatching { UserRole.valueOf(body.role.uppercase(Locale.getDefault())) }
                        .getOrElse { UserRole.CUSTOMER }
                    val displayName = body.full_name.ifBlank { email.substringBefore("@").ifBlank { "Operator" } }
                    SecureStorage.saveSession(body.token, body.role, body.user_id, displayName)
                    onLoginSuccess(
                        UserSession(
                            userId = body.user_id,
                            displayName = displayName,
                            role = role,
                            token = body.token
                        )
                    )
                } else {
                    val bodyText = response.errorBody()?.string()?.take(240)
                    val message = if (!bodyText.isNullOrBlank()) {
                        "Unable to sign in (${response.code()}): $bodyText"
                    } else {
                        "Unable to sign in. Server error ${response.code()}."
                    }
                    errorMessage = message
                    snackbarHostState.showSnackbar(message)
                    Log.e("AuthPortal", "Login/register failed: $message")
                }
            } catch (e: Exception) {
                val message = e.localizedMessage ?: "Network error"
                errorMessage = message
                snackbarHostState.showSnackbar(message)
                Log.e("AuthPortal", "Login/register exception", e)
            }
            isLoading = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = PureBlack) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(100.dp).background(BrandRed, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.LocalShipping, null, tint = PureBlack, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("WRECK4LESS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp, fontStyle = FontStyle.Italic)
            Text(
                if (isRegister) "CREATE YOUR ACCOUNT" else "PRIVATE FLEET SYSTEM",
                color = BrandRed.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(32.dp))
            if (isRegister) {
                OutlinedTextField(
                    value = fullName, onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
                )
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Secure Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
            )
            Spacer(Modifier.height(20.dp))

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { if (!isLoading) submitLogin() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PureBlack, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isRegister) "CREATE ACCOUNT" else "SIGN IN", fontWeight = FontWeight.Black, color = PureBlack)
                }
            }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                if (isRegister) "Your account will be activated on sign in." else "Access is validated by the fleet server.",
                color = Color.Gray,
                fontSize = 11.sp
            )
            TextButton(onClick = { if (!isLoading) isRegister = !isRegister }) {
                Text(
                    if (isRegister) "ALREADY HAVE AN ACCOUNT? SIGN IN" else "NEW ACCOUNT? REGISTER HERE",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
// --- CUSTOMER FLOW (Support Hub) ---

@Composable
fun CustomerSupportFlow(
    session: UserSession,
    repository: JobRepository,
    onLogout: () -> Unit
) {
    var view by remember { mutableStateOf("home") }
    var jobState by remember { mutableStateOf<CustomerJobState?>(null) }
    var history by remember { mutableStateOf<List<JobStatusResponse>>(emptyList()) }
    var historyLoading by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var manageJob by remember { mutableStateOf<JobStatusResponse?>(null) }
    var exportCsv by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var historyJob: Job? by remember { mutableStateOf(null) }

    BackHandler(enabled = view != "home") { view = "home" }

    fun refreshHistory() {
        historyJob?.cancel()
        historyLoading = true
        historyJob = scope.launch {
            try {
                val response = repository.customerHistory()
                if (response.isSuccessful && response.body() != null) {
                    history = response.body()!!.jobs
                }
            } catch (_: Exception) {
                history = emptyList()
            }
            historyLoading = false
        }
    }

    LaunchedEffect(view) {
        if (view == "account" || view == "past" || view == "inprogress") {
            refreshHistory()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(containerColor = PureBlack) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "WRECK4LESS",
                    color = BrandRed,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    session.displayName,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(16.dp))
                DrawerItem("Account", Icons.Default.Person) {
                    view = "account"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("In Progress", Icons.Default.Timelapse) {
                    view = "inprogress"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Past Wrecks", Icons.Default.History) {
                    view = "past"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Sign Out", Icons.AutoMirrored.Filled.Logout) {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            }
        }
    ) {
        Scaffold(
            containerColor = PureBlack,
            topBar = {
                WreckerHeader(
                    title = if (view == "home") "Support Hub" else view.uppercase(Locale.getDefault()),
                    userName = session.displayName,
                    showBack = view != "home",
                    onBack = { view = "home" },
                    onMenu = { scope.launch { drawerState.open() } }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding)) {
                when (view) {
                    "home" -> CustomerHome(
                        onNavigate = { view = it },
                        onLogout = onLogout
                    )
                    "form" -> WreckRequestForm(
                        repository = repository,
                        session = session,
                        onSubmitted = { job ->
                            jobState = job
                            view = "pending"
                        }
                    )
                    "gas" -> GasStationScreen(repository = repository)
                    "emergency" -> EmergencyServicesScreen(supportLine = jobState?.supportLine ?: "1-800-WRECK-HQ")
                    "pending" -> PendingReviewScreen(
                        repository = repository,
                        jobState = jobState,
                        onApproved = { updated ->
                            jobState = updated
                            view = "payment"
                        }
                    )
                    "payment" -> PaymentChoiceScreen(
                        repository = repository,
                        jobState = jobState,
                        onConfirmed = { updated ->
                            jobState = updated
                            view = "tracking"
                        }
                    )
                    "tracking" -> LiveTrackingScreen(jobState = jobState)
                    "account" -> CustomerAccountScreen(
                        session = session,
                        history = history,
                        isLoading = historyLoading
                    )
                    "past" -> PastWrecksScreen(
                        history = history,
                        isLoading = historyLoading
                    )
                    "inprogress" -> InProgressScreen(
                        history = history,
                        isLoading = historyLoading,
                        onOpenTracking = { job ->
                            jobState = CustomerJobState(
                                jobId = job.job_id,
                                status = job.status,
                                supportLine = "1-800-WRECK-HQ",
                                confirmedRate = job.confirmed_rate,
                                driverId = job.driver_id,
                                driverLocation = job.driver_location,
                                intel = job.intel
                            )
                            view = "tracking"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerHome(onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Card(
            onClick = { onNavigate("form") },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = BrandRed),
            modifier = Modifier.fillMaxWidth().height(220.dp)
        ) {
            Column(Modifier.padding(32.dp), verticalArrangement = Arrangement.Center) {
                Text("REQUEST A\nWRECK", fontSize = 40.sp, fontWeight = FontWeight.Black, color = PureBlack, lineHeight = 42.sp)
                Text("Tap for private dispatch", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureBlack.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardAction("Gas Stations Nearby", Icons.Default.LocalGasStation, Modifier.weight(1f)) { onNavigate("gas") }
            DashboardAction("Emergency", Icons.Default.Shield, Modifier.weight(1f)) { onNavigate("emergency") }
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = BrandRed)
            Spacer(Modifier.width(8.dp))
            Text("SIGN OUT", color = Color.Gray, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun RowScope.DashboardAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray),
        modifier = modifier.height(140.dp)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = BrandRed, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
    }
}
@Composable
fun WreckRequestForm(
    repository: JobRepository,
    session: UserSession,
    onSubmitted: (CustomerJobState) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var damage by remember { mutableStateOf("") }
    var locationLabel by remember { mutableStateOf("") }
    var locationLat by remember { mutableStateOf<Double?>(null) }
    var locationLng by remember { mutableStateOf<Double?>(null) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    locationLat = location.latitude
                    locationLng = location.longitude
                    locationLabel = "${"%.5f".format(location.latitude)}, ${"%.5f".format(location.longitude)}"
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Unable to read current location.") }
                }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Location permission denied.") }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        imageUris = uris.take(3)
    }

    fun requestLocation() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    locationLat = location.latitude
                    locationLng = location.longitude
                    locationLabel = "${"%.5f".format(location.latitude)}, ${"%.5f".format(location.longitude)}"
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Unable to read current location.") }
                }
            }
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = PureBlack) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Wrecking Details", fontSize = 24.sp, fontWeight = FontWeight.Black, color = BrandRed)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = make, onValueChange = { make = it }, label = { Text("Make") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = model, onValueChange = { model = it }, label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = year, onValueChange = { year = it }, label = { Text("Year") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = damage, onValueChange = { damage = it }, label = { Text("Damage Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = locationLabel, onValueChange = { locationLabel = it }, label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { requestLocation() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CardGray)
            ) {
                Icon(Icons.Default.MyLocation, null, tint = BrandRed)
                Spacer(Modifier.width(8.dp))
                Text("USE CURRENT LOCATION", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
            Spacer(Modifier.height(20.dp))

            Text("Photos (optional)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { imagePicker.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = CardGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoLibrary, null, tint = BrandRed)
                Spacer(Modifier.width(8.dp))
                Text("ADD UP TO 3 IMAGES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            if (imageUris.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("${imageUris.size} image(s) selected", color = Color.Gray, fontSize = 11.sp)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (make.isBlank() || model.isBlank() || year.isBlank() || damage.isBlank() || locationLabel.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please complete all fields.") }
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            val submission = WreckSubmission(
                                intel = WreckIntel(
                                    make = make,
                                    model = model,
                                    year = year,
                                    damage_description = damage,
                                    image_keys = imageUris.map { it.toString() },
                                    location_label = locationLabel,
                                    location_lat = locationLat,
                                    location_lng = locationLng
                                )
                            )
                            val response = repository.submitWreck(submission)
                            if (response.isSuccessful && response.body() != null) {
                                val body = response.body()!!
                                val jobState = CustomerJobState(
                                    jobId = body.job_id,
                                    status = "PENDING_OPS",
                                    supportLine = body.support_line,
                                    confirmedRate = 0.0,
                                    driverId = null,
                                    driverLocation = null,
                                    intel = submission.intel
                                )
                                onSubmitted(jobState)
                            } else {
                                snackbarHostState.showSnackbar("Dispatch failed. Server error ${response.code()}.")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(e.localizedMessage ?: "Network error")
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PureBlack, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("SUBMIT FOR DISPATCH", fontWeight = FontWeight.Black, color = PureBlack)
                }
            }
        }
    }
}
@Composable
fun PendingReviewScreen(
    repository: JobRepository,
    jobState: CustomerJobState?,
    onApproved: (CustomerJobState) -> Unit
) {
    val context = LocalContext.current
    var secondsRemaining by remember { mutableStateOf(300) }
    var statusText by remember { mutableStateOf("Awaiting operations approval") }
    val supportLine = jobState?.supportLine ?: "1-800-WRECK-HQ"

    LaunchedEffect(jobState?.jobId) {
        if (jobState?.jobId == null) return@LaunchedEffect
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining -= 1
        }
    }

    LaunchedEffect(jobState?.jobId) {
        val jobId = jobState?.jobId ?: return@LaunchedEffect
        while (true) {
            delay(10000)
            try {
                val response = repository.getJob(jobId)
                if (response.isSuccessful && response.body() != null) {
                    val updated = response.body()!!
                    statusText = updated.status.replace("_", " ")
                    if (updated.status == "RATE_APPROVED") {
                        onApproved(jobState.copy(
                            status = updated.status,
                            confirmedRate = updated.confirmed_rate,
                            driverId = updated.driver_id,
                            driverLocation = updated.driver_location,
                            intel = updated.intel
                        ))
                        return@LaunchedEffect
                    }
                }
            } catch (_: Exception) {
                statusText = "Waiting for server"
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = BrandRed, strokeWidth = 6.dp, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(24.dp))
        Text("DISPATCH STATUS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(statusText.uppercase(Locale.getDefault()), color = BrandRed, fontWeight = FontWeight.Bold)
        Text("${secondsRemaining/60}:${(secondsRemaining%60).toString().padStart(2,'0')}", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$supportLine")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardGray),
            border = BorderStroke(1.dp, BrandRed.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Phone, null, tint = BrandRed)
            Spacer(Modifier.width(12.dp))
            Text("CALL OPERATIONS", color = Color.White, fontWeight = FontWeight.Black)
        }
        Text("We will notify you when pricing is approved.", color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun PaymentChoiceScreen(
    repository: JobRepository,
    jobState: CustomerJobState?,
    onConfirmed: (CustomerJobState) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedMethod by remember { mutableStateOf("card") }
    var isLoading by remember { mutableStateOf(false) }

    val rate = jobState?.confirmedRate ?: 0.0
    val jobId = jobState?.jobId

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = PureBlack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            Text("APPROVED QUOTE", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("$${"%.2f".format(rate)}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 48.sp)
            Spacer(Modifier.height(32.dp))
            Text("SELECT PAYMENT PROTOCOL", color = BrandRed, fontWeight = FontWeight.Black, fontSize = 10.sp)
            Spacer(Modifier.height(16.dp))

            PaymentItem("Credit / Debit", Icons.Default.CreditCard, selectedMethod == "card") { selectedMethod = "card" }
            PaymentItem("E-Transfer", Icons.Default.AccountBalance, selectedMethod == "bank_transfer") { selectedMethod = "bank_transfer" }
            PaymentItem("Cash on Delivery", Icons.Default.Payments, selectedMethod == "cash") { selectedMethod = "cash" }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    if (jobId == null) return@Button
                    isLoading = true
                    scope.launch {
                        try {
                            val response = repository.finalizePayment(
                                PaymentFinalize(job_id = jobId, method = selectedMethod, amount = rate)
                            )
                            if (response.isSuccessful) {
                                onConfirmed(jobState!!.copy(status = "TRACKING_ACTIVE"))
                            } else {
                                snackbarHostState.showSnackbar("Payment confirmation failed.")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(e.localizedMessage ?: "Network error")
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PureBlack, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("CONFIRM & DISPATCH", fontWeight = FontWeight.Black, color = PureBlack)
                }
            }
        }
    }
}

@Composable
fun PaymentItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(70.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) BrandRed else CardGray),
        border = BorderStroke(1.dp, Color.DarkGray),
        onClick = onClick
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (selected) PureBlack else BrandRed)
            Spacer(Modifier.width(16.dp))
            Text(label.uppercase(Locale.getDefault()), color = if (selected) PureBlack else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun LiveTrackingScreen(jobState: CustomerJobState?) {
    val context = LocalContext.current
    val supportLine = jobState?.supportLine ?: "1-800-WRECK-HQ"
    val customerLat = jobState?.intel?.location_lat
    val customerLng = jobState?.intel?.location_lng
    val driverLat = jobState?.driverLocation?.lat
    val driverLng = jobState?.driverLocation?.lng

    val markers = buildList {
        if (customerLat != null && customerLng != null) add(MapMarker("Your Vehicle", customerLat, customerLng))
        if (driverLat != null && driverLng != null) add(MapMarker("Driver", driverLat, driverLng))
    }

    Column(Modifier.fillMaxSize().background(PureBlack)) {
        if (markers.isNotEmpty()) {
            OsmMapView(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                markers = markers,
                center = GeoPoint(markers.first().lat, markers.first().lon)
            )
        } else {
            Box(Modifier.weight(1f).fillMaxWidth().background(CardGray), contentAlignment = Alignment.Center) {
                Text("LIVE LOCATION UNAVAILABLE", color = BrandRed, fontWeight = FontWeight.Black)
            }
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
                        Text(jobState?.driverId ?: "Driver Assigned", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("STATUS: ${jobState?.status ?: "TRACKING"}", color = BrandRed, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$supportLine")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        Icon(Icons.Default.Phone, null, tint = PureBlack)
                        Spacer(Modifier.width(8.dp))
                        Text("CALL DRIVER", color = PureBlack, fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:$supportLine")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CardGray)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Message, null, tint = BrandRed)
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
fun ManagerDashboard(repository: JobRepository, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var queue by remember { mutableStateOf<List<JobStatusResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedJob by remember { mutableStateOf<JobStatusResponse?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = repository.managerQueue()
            if (response.isSuccessful && response.body() != null) {
                queue = response.body()!!.jobs
            }
        } catch (_: Exception) {
            queue = emptyList()
        }
        isLoading = false
    }

    Scaffold(
        containerColor = PureBlack,
        topBar = { WreckerHeader("Fleet Portal", "Manager", false, {}, {}) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("JOB DISPATCH QUEUE", color = BrandRed, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val response = repository.managerQueue()
                            if (response.isSuccessful && response.body() != null) {
                                queue = response.body()!!.jobs
                            }
                        } catch (_: Exception) {
                            queue = emptyList()
                        }
                        isLoading = false
                    }
                }) {
                    Icon(Icons.Default.Refresh, null, tint = BrandRed)
                }
            }
            Spacer(Modifier.height(16.dp))
            if (isLoading) {
                LinearProgressIndicator(color = BrandRed, modifier = Modifier.fillMaxWidth())
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(queue) { job ->
                        Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
                            Column(Modifier.padding(20.dp)) {
                                Text("${job.job_id} • ${job.intel.make} ${job.intel.model}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text(job.intel.location_label, color = Color.Gray, fontSize = 12.sp)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { selectedJob = job },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                                ) {
                                    Text("ASSIGN DRIVER", color = PureBlack, fontWeight = FontWeight.Black)
                                }
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

    if (selectedJob != null) {
        AssignDriverDialog(
            job = selectedJob!!,
            onDismiss = { selectedJob = null },
            onConfirm = { driverId, rate ->
                scope.launch {
                    repository.managerDispatch(
                        ManagerConfirm(job_id = selectedJob!!.job_id, assigned_driver_id = driverId, approved_rate = rate)
                    )
                    selectedJob = null
                    val response = repository.managerQueue()
                    if (response.isSuccessful && response.body() != null) {
                        queue = response.body()!!.jobs
                    }
                }
            }
        )
    }
}

@Composable
fun AssignDriverDialog(job: JobStatusResponse, onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var selectedDriver by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("195") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Driver", color = Color.White) },
        containerColor = CardGray,
        text = {
            Column {
                Text("${job.job_id} • ${job.intel.make} ${job.intel.model}", color = Color.LightGray)
                Spacer(Modifier.height(12.dp))
                Text("Driver ID", color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = selectedDriver,
                    onValueChange = { selectedDriver = it },
                    label = { Text("Driver UUID") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        focusedLabelColor = BrandRed
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text("Approved Rate") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed, focusedLabelColor = BrandRed)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val rate = rateInput.toDoubleOrNull() ?: 0.0
                onConfirm(selectedDriver, rate)
            }, colors = ButtonDefaults.buttonColors(containerColor = BrandRed)) {
                Text("CONFIRM", color = PureBlack, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
        }
    )
}

@Composable
fun DrawerItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
        Icon(icon, null, tint = BrandRed)
        Spacer(Modifier.width(12.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CustomerAccountScreen(
    session: UserSession,
    history: List<JobStatusResponse>,
    isLoading: Boolean
) {
    var showCardDialog by remember { mutableStateOf(false) }
    val savedLast4 = SecureStorage.getCardLast4()
    val savedExpiry = SecureStorage.getCardExpiry()
    val savedName = SecureStorage.getCardName()

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("ACCOUNT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
            Column(Modifier.padding(16.dp)) {
                Text(session.displayName, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Role: ${session.role.name.lowercase(Locale.getDefault())}", color = Color.Gray, fontSize = 12.sp)
                Text("User ID: ${session.userId}", color = Color.Gray, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("PAYMENT METHOD", color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
            Column(Modifier.padding(16.dp)) {
                if (!savedLast4.isNullOrBlank()) {
                    Text("Card ending in $savedLast4", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Expiry: $savedExpiry", color = Color.Gray, fontSize = 11.sp)
                    if (!savedName.isNullOrBlank()) {
                        Text(savedName, color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    Text("No card on file", color = Color.Gray, fontSize = 11.sp)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showCardDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("ADD / UPDATE CARD", color = PureBlack, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("PAST TRANSACTIONS", color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        if (isLoading) {
            LinearProgressIndicator(color = BrandRed, modifier = Modifier.fillMaxWidth())
        } else if (history.isEmpty()) {
            Text("No previous wrecks yet.", color = Color.Gray, fontSize = 12.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history.take(10)) { job ->
                    Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(job.job_id, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(job.intel.location_label, color = Color.Gray, fontSize = 11.sp)
                            Text(job.status.replace("_", " "), color = BrandRed, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showCardDialog) {
        AddCardDialog(
            onDismiss = { showCardDialog = false },
            onSave = { last4, expiry, name ->
                SecureStorage.saveCard(last4, expiry, name)
                showCardDialog = false
            }
        )
    }
}

@Composable
fun PastWrecksScreen(history: List<JobStatusResponse>, isLoading: Boolean) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("PAST WRECKS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        if (isLoading) {
            LinearProgressIndicator(color = BrandRed, modifier = Modifier.fillMaxWidth())
            return
        }
        val completed = history.filter { it.status == "COMPLETE" }
        if (completed.isEmpty()) {
            Text("No completed requests yet.", color = Color.Gray, fontSize = 12.sp)
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(completed) { job ->
                Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(job.job_id, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${job.intel.make} ${job.intel.model} (${job.intel.year})", color = Color.Gray, fontSize = 11.sp)
                        Text(job.intel.location_label, color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InProgressScreen(
    history: List<JobStatusResponse>,
    isLoading: Boolean,
    onOpenTracking: (JobStatusResponse) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("IN PROGRESS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        if (isLoading) {
            LinearProgressIndicator(color = BrandRed, modifier = Modifier.fillMaxWidth())
            return
        }
        val active = history.firstOrNull { it.status != "COMPLETE" }
        if (active == null) {
            Text("No active dispatches right now.", color = Color.Gray, fontSize = 12.sp)
            return
        }
        Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
            Column(Modifier.padding(16.dp)) {
                Text(active.job_id, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${active.intel.make} ${active.intel.model} (${active.intel.year})", color = Color.Gray, fontSize = 11.sp)
                Text(active.status.replace("_", " "), color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onOpenTracking(active) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("OPEN TRACKING", color = PureBlack, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun AdminOverview(
    jobs: List<JobStatusResponse>,
    ongoing: List<JobStatusResponse>,
    completed: List<JobStatusResponse>,
    drivers: List<DriverRosterEntry>
) {
    Column {
        Text("Active Dispatches: ${ongoing.size}", color = BrandRed, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatCard("Total Requests", jobs.size.toString())
            AdminStatCard("Completed", completed.size.toString())
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatCard("Drivers Online", drivers.count { it.online }.toString())
            AdminStatCard("Drivers Total", drivers.size.toString())
        }
    }
}

@Composable
fun RowScope.AdminStatCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardGray),
        modifier = Modifier.weight(1f)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label.uppercase(Locale.getDefault()), color = Color.Gray, fontSize = 10.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
    }
}

@Composable
fun AdminJobsList(title: String, items: List<JobStatusResponse>, onManage: (JobStatusResponse) -> Unit) {
    Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
    Spacer(Modifier.height(8.dp))
    if (items.isEmpty()) {
        Text("No jobs found.", color = Color.Gray, fontSize = 12.sp)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { job ->
            Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
                Column(Modifier.padding(12.dp)) {
                    Text(job.job_id, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${job.intel.make} ${job.intel.model} (${job.intel.year})", color = Color.Gray, fontSize = 11.sp)
                    Text(job.intel.location_label, color = Color.Gray, fontSize = 11.sp)
                    Text(job.status.replace("_", " "), color = BrandRed, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onManage(job) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        Text("MANAGE", color = PureBlack, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDriversScreen(drivers: List<DriverRosterEntry>) {
    Text("DRIVER ROSTER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
    Spacer(Modifier.height(8.dp))
    if (drivers.isEmpty()) {
        Text("No drivers in the system.", color = Color.Gray, fontSize = 12.sp)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(drivers) { driver ->
            Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
                Column(Modifier.padding(12.dp)) {
                    Text(driver.full_name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(driver.email, color = Color.Gray, fontSize = 11.sp)
                    Text(
                        if (driver.online) "ONLINE" else "OFFLINE",
                        color = if (driver.online) BrandRed else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (driver.active_job_id != null) {
                        Text("Job: ${driver.active_job_id}", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMapScreen(jobs: List<JobStatusResponse>) {
    Text("MAP VIEW", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
    Spacer(Modifier.height(8.dp))
    val markers = jobs.mapNotNull { job ->
        val lat = job.intel.location_lat ?: return@mapNotNull null
        val lng = job.intel.location_lng ?: return@mapNotNull null
        MapMarker("${job.job_id} (${job.status})", lat, lng)
    }
    if (markers.isEmpty()) {
        Text("No geocoded jobs to display.", color = Color.Gray, fontSize = 12.sp)
        return
    }
    OsmMapView(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        markers = markers,
        center = GeoPoint(markers.first().lat, markers.first().lon)
    )
}

@Composable
fun AdminManageDialog(
    job: JobStatusResponse,
    drivers: List<DriverRosterEntry>,
    onDismiss: () -> Unit,
    onReassign: (String) -> Unit,
    onUpdateRate: (Double) -> Unit,
    onCancel: (String?) -> Unit
) {
    var selectedDriver by remember { mutableStateOf(drivers.firstOrNull()?.id.orEmpty()) }
    var rateInput by remember { mutableStateOf(job.confirmed_rate.toString()) }
    var cancelReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Job", color = Color.White) },
        containerColor = CardGray,
        text = {
            Column {
                Text(job.job_id, color = Color.White, fontWeight = FontWeight.Bold)
                Text(job.status.replace("_", " "), color = BrandRed, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                Text("Reassign Driver", color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = selectedDriver,
                    onValueChange = { selectedDriver = it },
                    label = { Text("Driver ID") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        focusedLabelColor = BrandRed
                    )
                )
                if (drivers.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Available: ${drivers.take(3).joinToString { it.full_name }}", color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text("Update Rate", color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text("Approved Rate") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        focusedLabelColor = BrandRed
                    )
                )
                Spacer(Modifier.height(12.dp))
                Text("Cancel Job", color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = cancelReason,
                    onValueChange = { cancelReason = it },
                    label = { Text("Reason (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        focusedLabelColor = BrandRed
                    )
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    val rate = rateInput.toDoubleOrNull() ?: job.confirmed_rate
                    onUpdateRate(rate)
                }) { Text("UPDATE RATE", color = BrandRed) }
                TextButton(onClick = {
                    if (selectedDriver.isNotBlank()) {
                        onReassign(selectedDriver)
                    }
                }) { Text("REASSIGN", color = BrandRed) }
                TextButton(onClick = {
                    onCancel(cancelReason.ifBlank { null })
                }) { Text("CANCEL JOB", color = Color(0xFFFF6B6B)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = Color.Gray) }
        }
    )
}

@Composable
fun AdminExportDialog(csv: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Logs", color = Color.White) },
        containerColor = CardGray,
        text = {
            Column {
                Text("CSV generated. Tap copy to clipboard.", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    csv.take(800),
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("wreck4less_export", csv))
                onDismiss()
            }) { Text("COPY CSV", color = BrandRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = Color.Gray) }
        }
    )
}

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Card", color = Color.White) },
        containerColor = CardGray,
        text = {
            Column {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it.filter { ch -> ch.isDigit() }.take(19) },
                    label = { Text("Card Number") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        focusedLabelColor = BrandRed
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = expiry,
                    onValueChange = { expiry = it.take(5) },
                    label = { Text("Expiry (MM/YY)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        focusedLabelColor = BrandRed
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name on Card") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        focusedLabelColor = BrandRed
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = cardNumber.trim()
                    if (trimmed.length >= 12) {
                        val last4 = trimmed.takeLast(4)
                        onSave(last4, expiry.trim(), name.trim())
                    }
                }
            ) { Text("SAVE", color = BrandRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
        }
    )
}

@Composable
fun DriverDashboard(session: UserSession, repository: JobRepository, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var assignment by remember { mutableStateOf<JobStatusResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = repository.driverAssignment(session.userId)
            if (response.isSuccessful && response.body() != null) {
                assignment = response.body()!!.job
            }
        } catch (_: Exception) {
            assignment = null
        }
        isLoading = false
    }

    Scaffold(
        containerColor = PureBlack,
        topBar = { WreckerHeader("Fleet Portal", session.displayName, false, {}, {}) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(24.dp)) {
            if (isLoading) {
                LinearProgressIndicator(color = BrandRed, modifier = Modifier.fillMaxWidth())
            } else if (assignment == null) {
                Text("NO ASSIGNED JOBS", color = Color.White, fontWeight = FontWeight.Black)
                Text("Stand by for dispatch.", color = Color.Gray)
            } else {
                val job = assignment!!
                Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
                    Column(Modifier.padding(24.dp)) {
                        Text("ACTIVE DISPATCH", color = BrandRed, fontWeight = FontWeight.Black)
                        Text(job.intel.location_label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Vehicle: ${job.intel.make} ${job.intel.model} (${job.intel.year})", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            repository.driverUpdate(DriverUpdateRequest(job.job_id, "EN_ROUTE"))
                                        } catch (_: Exception) {
                                            // Ignore transient network errors in driver status updates.
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("EN ROUTE", color = PureBlack, fontWeight = FontWeight.Black, fontSize = 10.sp)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            repository.driverUpdate(DriverUpdateRequest(job.job_id, "ARRIVED"))
                                        } catch (_: Exception) {
                                            // Ignore transient network errors in driver status updates.
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("ARRIVED", color = Color.White, fontSize = 10.sp)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            repository.driverUpdate(DriverUpdateRequest(job.job_id, "COMPLETE"))
                                        } catch (_: Exception) {
                                            // Ignore transient network errors in driver status updates.
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("COMPLETE", color = Color.White, fontSize = 10.sp)
                            }
                        }
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

@Composable
fun AdminConsole(repository: JobRepository, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var audit by remember { mutableStateOf<AdminAuditResponse?>(null) }
    var drivers by remember { mutableStateOf<List<DriverRosterEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var view by remember { mutableStateOf("overview") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(Unit) {
        try {
            val response = repository.adminAudit()
            if (response.isSuccessful) {
                audit = response.body()
            }
            val driverResponse = repository.adminDrivers()
            if (driverResponse.isSuccessful && driverResponse.body() != null) {
                drivers = driverResponse.body()!!.drivers
            }
        } catch (_: Exception) {
            audit = null
        }
        isLoading = false
    }

    fun refresh() {
        isLoading = true
        scope.launch {
            try {
                val response = repository.adminAudit()
                if (response.isSuccessful) {
                    audit = response.body()
                }
                val driverResponse = repository.adminDrivers()
                if (driverResponse.isSuccessful && driverResponse.body() != null) {
                    drivers = driverResponse.body()!!.drivers
                }
            } catch (_: Exception) {
                audit = null
            }
            isLoading = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(containerColor = PureBlack) {
                Spacer(Modifier.height(24.dp))
                Text("ADMIN PANEL", color = BrandRed, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(8.dp))
                DrawerItem("Overview", Icons.Default.Dashboard) {
                    view = "overview"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Ongoing Jobs", Icons.Default.Timelapse) {
                    view = "ongoing"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("All Requests", Icons.Default.List) {
                    view = "all"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Completed Jobs", Icons.Default.DoneAll) {
                    view = "completed"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Drivers Online", Icons.Default.TrendingUp) {
                    view = "drivers"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Map View", Icons.Default.Map) {
                    view = "map"
                    scope.launch { drawerState.close() }
                }
                DrawerItem("Export Logs", Icons.Default.Download) {
                    scope.launch { drawerState.close() }
                    scope.launch {
                        try {
                            val response = repository.adminExport()
                            if (response.isSuccessful && response.body() != null) {
                                exportCsv = response.body()!!.csv
                            } else {
                                snackbarHostState.showSnackbar("Export failed: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(e.localizedMessage ?: "Export failed")
                        }
                    }
                }
                DrawerItem("Sign Out", Icons.AutoMirrored.Filled.Logout) {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            }
        }
    ) {
        Scaffold(
            containerColor = PureBlack,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                WreckerHeader(
                    "Fleet Portal",
                    "Admin",
                    false,
                    {},
                    { scope.launch { drawerState.open() } }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "SYSTEM ADMINISTRATION",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, null, tint = BrandRed)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (isLoading) {
                    LinearProgressIndicator(color = BrandRed, modifier = Modifier.fillMaxWidth())
                    return@Column
                }
                if (audit == null) {
                    Text("No audit data available", color = Color.Gray)
                    return@Column
                }

                val jobs = audit!!.registry.values.toList()
                val ongoing = jobs.filter { it.status != "COMPLETE" && it.status != "CANCELLED" }
                val completed = jobs.filter { it.status == "COMPLETE" }

                when (view) {
                    "overview" -> AdminOverview(jobs = jobs, ongoing = ongoing, completed = completed, drivers = drivers)
                    "ongoing" -> AdminJobsList(title = "ONGOING JOBS", items = ongoing, onManage = { manageJob = it })
                    "all" -> AdminJobsList(title = "ALL REQUESTS", items = jobs, onManage = { manageJob = it })
                    "completed" -> AdminJobsList(title = "COMPLETED JOBS", items = completed, onManage = { manageJob = it })
                    "drivers" -> AdminDriversScreen(drivers = drivers)
                    "map" -> AdminMapScreen(jobs = ongoing)
                }

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("TERMINATE SESSION", color = PureBlack, fontWeight = FontWeight.Black)
                }
            }
        }
    }

    if (manageJob != null) {
        AdminManageDialog(
            job = manageJob!!,
            drivers = drivers,
            onDismiss = { manageJob = null },
            onReassign = { driverId ->
                scope.launch {
                    try {
                        repository.adminReassign(manageJob!!.job_id, AdminReassignRequest(driverId))
                        refresh()
                    } catch (_: Exception) {
                    }
                    manageJob = null
                }
            },
            onUpdateRate = { rate ->
                scope.launch {
                    try {
                        repository.adminUpdateRate(manageJob!!.job_id, AdminRateRequest(rate))
                        refresh()
                    } catch (_: Exception) {
                    }
                    manageJob = null
                }
            },
            onCancel = { reason ->
                scope.launch {
                    try {
                        repository.adminCancelJob(manageJob!!.job_id, AdminCancelRequest(reason))
                        refresh()
                    } catch (_: Exception) {
                    }
                    manageJob = null
                }
            }
        )
    }

    if (exportCsv != null) {
        AdminExportDialog(csv = exportCsv!!, onDismiss = { exportCsv = null })
    }
}
// --- UTILITY SCREENS ---

@Composable
fun GasStationScreen(repository: JobRepository) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var stations by remember { mutableStateOf<List<GasStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var locationLat by remember { mutableStateOf<Double?>(null) }
    var locationLng by remember { mutableStateOf<Double?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    locationLat = location.latitude
                    locationLng = location.longitude
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Unable to read current location.") }
                }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Location permission denied.") }
        }
    }

    fun requestLocation() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    locationLat = location.latitude
                    locationLng = location.longitude
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Unable to read current location.") }
                }
            }
        } else {
            permissionLauncher.launch(permission)
        }
    }

    LaunchedEffect(locationLat, locationLng) {
        val lat = locationLat
        val lng = locationLng
        if (lat != null && lng != null) {
            isLoading = true
            try {
                stations = repository.findGasStations(lat, lng, 2000)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Unable to load stations.")
            }
            isLoading = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = PureBlack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            Text("GAS STATIONS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { requestLocation() }, colors = ButtonDefaults.buttonColors(containerColor = CardGray)) {
                Icon(Icons.Default.MyLocation, null, tint = BrandRed)
                Spacer(Modifier.width(8.dp))
                Text("LOCATE NEARBY STATIONS", color = Color.White, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                LinearProgressIndicator(color = BrandRed, modifier = Modifier.fillMaxWidth())
            }

            val markers = stations.map { MapMarker(it.name, it.lat, it.lon) }
            if (markers.isNotEmpty()) {
                OsmMapView(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    markers = markers,
                    center = GeoPoint(markers.first().lat, markers.first().lon)
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stations) { station ->
                        Card(colors = CardDefaults.cardColors(containerColor = CardGray)) {
                            Column(Modifier.padding(16.dp)) {
                                Text(station.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${"%.4f".format(station.lat)}, ${"%.4f".format(station.lon)}", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else if (!isLoading) {
                Text("Request location to load stations.", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EmergencyServicesScreen(supportLine: String) {
    val context = LocalContext.current
    val emergencyContacts = listOf(
        "Emergency (911)" to "Call for immediate danger",
        "Fire (911)" to "Report fires or smoke",
        "Police (911)" to "Report crimes in progress",
        "EMS (911)" to "Medical emergencies"
    )

    Column(Modifier.fillMaxSize().background(PureBlack).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("EMERGENCY SUPPORT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Spacer(Modifier.height(16.dp))
        Text("Operations hotline", color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        Text(supportLine, color = BrandRed, fontWeight = FontWeight.Black, fontSize = 24.sp)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$supportLine")
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
        ) {
            Icon(Icons.Default.Phone, null, tint = PureBlack)
            Spacer(Modifier.width(8.dp))
            Text("CALL OPERATIONS", color = PureBlack, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(28.dp))
        Text("LOCAL EMERGENCY SERVICES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        emergencyContacts.forEach { (title, subtitle) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardGray),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(title, color = BrandRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Text(subtitle, color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = CardGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("WRECK4LESS DISPATCH", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Text("Tow & recovery support", color = Color.Gray, fontSize = 11.sp)
                Text(supportLine, color = BrandRed, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}

// --- MAP COMPOSABLE ---

@Composable
fun OsmMapView(modifier: Modifier, markers: List<MapMarker>, center: GeoPoint) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.overlays.clear()
            view.controller.setZoom(14.5)
            view.controller.setCenter(center)
            markers.forEach { marker ->
                val mapMarker = Marker(view)
                mapMarker.position = GeoPoint(marker.lat, marker.lon)
                mapMarker.title = marker.title
                view.overlays.add(mapMarker)
            }
            view.invalidate()
        }
    )
}
