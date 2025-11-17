package com.example.autopayroll_mobile.composableUI.dashboardUI

// Import the new Coil composable
import coil.compose.AsyncImage
// --- other imports ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.viewmodel.DashboardUiState
import com.example.autopayroll_mobile.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val TextPrimary = Color(0xFF3C3C3C)
val CardSurface = Color.White // White for cards/main content area
val AppBackground = Color(0xFFEEEEEE) // Light Gray for the overall screen background / Header area
val HeaderBackground = Color(0xFFE0E0E0) // Slightly darker gray for card headers

val StatusPending = Color(0xFFFFA726)
val StatusApproved = Color(0xFF66BB6A)
val StatusRejected = Color(0xFFEF5350)
val StatusDefault = Color(0xFF9E9E9E)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // Outer box to set the overall background color to WHITE (CardSurface) and handle insets
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CardSurface)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. PROFILE HEADER SECTION (Plain Column, White BG) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurface)
                    .padding(horizontal = 20.dp) // Apply horizontal padding here
                    .padding(top = 16.dp) // Margin below the status bar
                    .padding(bottom = 16.dp), // Padding at the bottom for separation/margin
            ) {
                // REMOVED: "Employee Dashboard" Title

                ProfileHeaderContent(state = uiState)
            }

            // --- 2. CONTENT SECTION (The White BG Part) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurface)
                    .padding(horizontal = 20.dp) // Keep horizontal padding consistent
                    .padding(top = 8.dp, bottom = 20.dp)
            ) {
                AttendanceSummaryCard()
                Spacer(modifier = Modifier.height(32.dp)) // Increased spacing
                PreviewCards()
                Spacer(modifier = Modifier.height(32.dp)) // Increased spacing
                TransactionsCard(uiState = uiState)
                // Padding at the bottom for the scrolling content
                Spacer(modifier = Modifier.height(40.dp)) // Increased final padding
            }
        }
    }
}

@Composable
fun ProfileHeaderContent(state: DashboardUiState) {
    // This composable handles the content inside the top section.
    Row(
        modifier = Modifier
            .fillMaxWidth()
        // Removed horizontal padding here as it was applied to the parent Column
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = state.profilePhotoUrl,
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.profiledefault),
            error = painterResource(id = R.drawable.profiledefault),
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = state.employeeName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = state.employeeId,
                color = TextPrimary,
                fontSize = 14.sp,
            )
            Text(
                text = state.jobAndCompany,
                color = TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AttendanceSummaryCard() {
    // ... (unchanged)
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(title = "Regular", value = "0", modifier = Modifier.weight(1f))
                StatItem(title = "Overtime", value = "0", modifier = Modifier.weight(1f))
                StatItem(title = "Late", value = "0", modifier = Modifier.weight(1f))
            }
            Text(
                text = "Accumulated hours over last attendance check",
                fontSize = 12.sp,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StatItem(title: String, value: String, modifier: Modifier = Modifier) {
    // ... (unchanged)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 14.sp, color = TextPrimary)
        Text(
            text = value,
            fontSize = 28.sp,
            color = Color(0xFF3C3C3C),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PreviewCards() {
    // ... (unchanged)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PreviewCard(
            title = "Leave Balance Preview",
            value = "0",
            modifier = Modifier.weight(1f)
        )
        PreviewCard(
            title = "Absence Preview",
            value = "0",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PreviewCard(title: String, value: String, modifier: Modifier = Modifier) {
    // ... (unchanged)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp)
            Text(
                text = value,
                fontSize = 36.sp,
                color = Color(0xFF3C3C3C),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun formatDate(dateString: String): String {
    // ... (unchanged)
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        formatter.format(parser.parse(dateString) ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun TransactionsCard(uiState: DashboardUiState) {
    // ... (unchanged)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Most Recent Payslip",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeaderBackground)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(text = "Net Earning", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                    Text(text = "Pay Date", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = TextPrimary, modifier = Modifier.weight(1f))
                    Text(text = "Status", fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = TextPrimary, modifier = Modifier.weight(1f))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(134.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator()
                        }
                        uiState.recentPayslip != null -> {
                            val payslip = uiState.recentPayslip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₱${payslip.netPay}",
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = formatDate(payslip.payDate),
                                    textAlign = TextAlign.Center,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 16.sp
                                )
                                val statusText = payslip.status.replaceFirstChar { it.uppercase() }
                                val statusColor = when (payslip.status.toLowerCase(Locale.ROOT)) {
                                    "pending" -> StatusPending
                                    "released" -> StatusApproved
                                    "rejected" -> StatusRejected
                                    else -> StatusDefault
                                }
                                Text(
                                    text = statusText,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusColor)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "No payslip to show.",
                                color = Color(0xFFA0A0A0),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}