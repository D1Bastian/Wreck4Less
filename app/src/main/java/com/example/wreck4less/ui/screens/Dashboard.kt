package com.example.wreck4less.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- CUSTOMER SCREENS ---

@Composable
fun CustomerHome(onStartRequest: () -> Unit, onSwitchRole: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF020617))) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF0F172A))) {
            // Simulated Map View
            Text("MAP ACTIVE", Modifier.align(Alignment.Center), color = Color.Gray, fontWeight = FontWeight.Bold)

            IconButton(
                onClick = onSwitchRole,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color(0xFF1E293B), CircleShape)
            ) {
                Icon(Icons.Default.Build, contentDescription = "Driver Mode", tint = Color(0xFFF59E0B))
            }
        }
        Card(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Stuck?", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Professional recovery in minutes.", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onStartRequest,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("REQUEST RECOVERY", fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun VehicleDetailsScreen(onNext: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF020617)).padding(24.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
        Text("Vehicle Specs", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = "", onValueChange = {},
            label = { Text("Destination Address") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF59E0B),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Make") }, modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFF59E0B),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Model") }, modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFF59E0B),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
        ) {
            Text("See Pricing", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PricingOptionsScreen(onNext: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF020617)).padding(24.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
        Text("Wait Time vs Price", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))

        PricingTierItem("Priority Recovery", "$185", "5-10 min", true)
        Spacer(Modifier.height(16.dp))
        PricingTierItem("Standard Tow", "$120", "35-50 min", false)

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
        ) {
            Text("Proceed to Payment", fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun PricingTierItem(title: String, price: String, eta: String, selected: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF0F172A) else Color.Transparent),
        border = if (selected) BorderStroke(2.dp, Color(0xFFF59E0B)) else null,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text("ETA: $eta", color = Color.Gray, fontSize = 12.sp)
            }
            Text(price, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

@Composable
fun PaymentScreen(onConfirm: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF020617)).padding(24.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
        Text("Checkout", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Box(Modifier.padding(24.dp)) {
                Text("**** 4242", color = Color.White, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterStart))
                Text("VISA", color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopEnd))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("PAY & BOOK", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SearchingScreen(onDriverAssigned: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onDriverAssigned()
    }
    Box(Modifier.fillMaxSize().background(Color(0xFF020617)), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFFF59E0B))
    }
}

@Composable
fun ActiveJobScreen(onComplete: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFF020617))) {
        Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF0F172A)))
        Card(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Arriving in 6m", color = Color(0xFFF59E0B), fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text("Big Mike is on the way.", color = Color.White)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text("Finish Job") }
            }
        }
    }
}

@Composable
fun DriverDashboard(onSwitchRole: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF020617)).padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Dispatch Board", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            IconButton(onClick = onSwitchRole) { Icon(Icons.Default.Person, null, tint = Color.White) }
        }
        Spacer(Modifier.height(24.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(listOf("Tesla Model 3", "BMW X5", "Honda Civic")) { car ->
                DriverJobItem(car)
            }
        }
    }
}

@Composable
fun DriverJobItem(car: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(car, color = Color.White, fontWeight = FontWeight.Bold)
                Text("$85.00", color = Color(0xFF10B981), fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))) {
                Text("Accept")
            }
        }
    }
}
