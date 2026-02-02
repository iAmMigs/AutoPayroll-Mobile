package com.example.autopayroll_mobile.composableUI.menuModule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R

// --- WEB DESIGN TOKENS (Consistent with Leave Module) ---
private val WebBackground = Color(0xFFF8F9FA) // Light Gray Background
private val WebSurface = Color.White          // White Cards/Header
private val TextHeader = Color(0xFF1E293B)    // Dark Blue-Grey Text
private val TextBody = Color(0xFF64748B)      // Softer Grey Text
private val DividerColor = Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToLeave: () -> Unit,
    onNavigateToAdjustment: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // REPLACED TEXT WITH IMAGE TITLE
                    Image(
                        painter = painterResource(id = R.drawable.autopayrolltitle),
                        contentDescription = "Auto Payroll",
                        modifier = Modifier
                            .height(40.dp) // Adjusted height for better fit
                            .fillMaxWidth(),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = WebSurface
                ),
                modifier = Modifier.shadow(elevation = 2.dp) // Subtle shadow for web-app feel
            )
        },
        containerColor = WebBackground
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp), // Outer padding
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spacer to push content down slightly
            Spacer(modifier = Modifier.height(8.dp))

            // --- Menu Card ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WebSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {

                    // Profile
                    MenuItem(
                        label = "Profile",
                        iconRes = R.drawable.ic_personal_info,
                        onClick = onNavigateToProfile
                    )
                    MenuDivider()

                    // Leave Request
                    MenuItem(
                        label = "Leave Request",
                        iconRes = R.drawable.ic_leave_policy, // Using leave policy icon
                        onClick = onNavigateToLeave
                    )
                    MenuDivider()

                    // Adjustment
                    MenuItem(
                        label = "Payroll Credit Adjustment",
                        iconRes = R.drawable.ic_payroll,
                        onClick = onNavigateToAdjustment
                    )
                    MenuDivider()

                    // Change Password
                    MenuItem(
                        label = "Reset Password",
                        iconVector = Icons.Default.LockReset, // Using Vector for generic actions
                        onClick = onNavigateToChangePassword
                    )
                    MenuDivider()

                    // Help
                    MenuItem(
                        label = "Help",
                        iconVector = Icons.AutoMirrored.Filled.HelpOutline,
                        onClick = {}, // No action yet
                        showChevron = false // Optional: hide chevron for simple actions
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Logout Button ---
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE2E2), // Light Red Background
                    contentColor = Color(0xFFDC2626)    // Dark Red Text
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "Log Out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Version Info (Optional footer)
            Text(
                text = "Version 1.0.0",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// --- Helper Composable for Menu Items ---
@Composable
fun MenuItem(
    label: String,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    onClick: () -> Unit,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Logic (Supports both Resource ID and Vector)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9)), // Light grey icon background
            contentAlignment = Alignment.Center
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = TextHeader,
                    modifier = Modifier.size(20.dp)
                )
            } else if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = TextHeader,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextHeader,
            modifier = Modifier.weight(1f)
        )

        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1), // Very light grey for chevron
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MenuDivider() {
    Divider(
        color = DividerColor,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 76.dp) // Indent divider to align with text
    )
}

// Helper shadow extension (optional, but standard in M3)
fun Modifier.shadow(elevation: androidx.compose.ui.unit.Dp): Modifier {
    return this // Placeholder if you want actual shadow logic, usually handled by Surface/Card elevation
}