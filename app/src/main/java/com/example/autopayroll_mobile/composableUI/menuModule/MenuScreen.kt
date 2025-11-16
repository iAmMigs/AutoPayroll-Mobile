package com.example.autopayroll_mobile.composableUI.menuModule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R

// Define the colors based on the requested design (darker header, white body)
val HeaderColor = Color(0xFFF5F5F5) // Light gray for the header background
val ScreenBackgroundColor = Color.White // White for the main content area

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToLeave: () -> Unit,
    onNavigateToAdjustment: () -> Unit,
    onLogout: () -> Unit
    // Add other navigation callbacks (Dashboard, Payslip, etc.) if needed in the menu UI
) {
    Scaffold(
        topBar = {
            // APPLYING THE NEW DESIGN: TopAppBar container color set to HeaderColor
            TopAppBar(
                title = {
                    Text(
                        "Main Menu",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                modifier = Modifier.background(HeaderColor),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderColor // Set the TopAppBar background color
                )
            )
        },
        containerColor = HeaderColor // Set Scaffold container to the header color for the entire screen,
        // and let the Column handle the white content area.
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ScreenBackgroundColor) // Main content area is explicitly set to white
        ) {

            // --- Menu Items ---
            // These items benefit from being wrapped in a Card for separation/style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    MenuItem("Profile", onNavigateToProfile)
                    Divider(Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                    MenuItem("Leave Request", onNavigateToLeave)
                    Divider(Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                    MenuItem("Payroll Credit Adjustment", onNavigateToAdjustment)
                    Divider(Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                    MenuItem("Settings", {})
                    Divider(Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                    MenuItem("Help", {})
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push button to the bottom

            // --- Logout Button (Red, Bottom of Screen) ---
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), // Red color
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("LOGOUT", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun MenuItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_right_arrow), // Assuming this icon exists
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}