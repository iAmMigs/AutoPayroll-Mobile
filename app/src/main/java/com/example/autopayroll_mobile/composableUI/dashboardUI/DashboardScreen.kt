package com.example.autopayroll_mobile.composableUI.dashboardUI

import coil.compose.AsyncImage
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.data.model.Schedule
import com.example.autopayroll_mobile.composableUI.dashboardUI.DashboardUiState
import com.example.autopayroll_mobile.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar // This import is already present in your original code

val TextPrimary = Color(0xFF3C3C3C)
val CardSurface = Color.White
val AppBackground = Color(0xFFCECECE)
val HeaderBackground = Color(0xFF9A9A9A)

val StatusPending = Color(0xFFFFA726)
val StatusApproved = Color(0xFF66BB6A)
val StatusRejected = Color(0xFFEF5350)
val StatusDefault = Color(0xFF9E9E9E)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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
            // --- 1. PROFILE HEADER SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurface)
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp)
                    .padding(bottom = 16.dp),
            ) {
                // ADDED: GreetingHeaderContent call
                GreetingHeaderContent()
                // ADDED: Spacer for separation
                Spacer(modifier = Modifier.height(16.dp)) // You can adjust this height

                ProfileHeaderContent(state = uiState) // ORIGINAL: ProfileHeaderContent remains
            }

            // --- 2. CONTENT SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurface)
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 20.dp)
            ) {
                // Pass all three hour stats from State
                AttendanceSummaryCard(
                    regularHours = uiState.lastWorkedHours,
                    overtimeHours = uiState.overtimeHours,
                    lateHours = uiState.lateHours
                )

                Spacer(modifier = Modifier.height(16.dp))

                PreviewCards(
                    leaveCredits = uiState.leaveCredits,
                    absences = uiState.absences
                )

                Spacer(modifier = Modifier.height(16.dp))
                TransactionsCard(uiState = uiState)
                Spacer(modifier = Modifier.height(16.dp))
                ScheduleCard(schedule = uiState.currentSchedule, isLoading = uiState.isLoading)
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}


@Composable
fun GreetingHeaderContent() {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val greeting = when (currentHour) {
        in 5..11 -> "Good Morning!" // 5 AM to 11:59 AM
        in 12..16 -> "Good Afternoon!" // 12 PM to 4:59 PM
        else -> "Good Evening!" // 5 PM to 4:59 AM (includes midnight to 4:59 AM)
    }

    Text(
        text = greeting,
        fontSize = 22.sp, // Matching the "Payroll Viewing" text size
        fontWeight = FontWeight.Bold,
        color = TextPrimary // Using the TextPrimary color defined in this file
    )
}

// The ProfileHeaderContent function is kept exactly as it was in your original code.
@Composable
fun ProfileHeaderContent(state: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
fun AttendanceSummaryCard(
    regularHours: String,
    overtimeHours: String,
    lateHours: String
) {
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(title = "Regular", value = regularHours, modifier = Modifier.weight(1f))
                StatItem(title = "Overtime", value = overtimeHours, modifier = Modifier.weight(1f))
                StatItem(title = "Late", value = lateHours, modifier = Modifier.weight(1f))
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

// ... [Rest of the file: PreviewCards, PreviewCard, formatDate, TransactionsCard, formatTime, ScheduleCard remain unchanged]
@Composable
fun PreviewCards(leaveCredits: String, absences: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PreviewCard(
            title = "Leave Balance Preview",
            value = leaveCredits,
            modifier = Modifier.weight(1f)
        )
        PreviewCard(
            title = "Absence Preview",
            value = absences,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PreviewCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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

fun formatTime(timeString: String?): String {
    if (timeString == null) return "--:--"
    return try {
        val parser = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        formatter.format(parser.parse(timeString) ?: Date())
    } catch (e: Exception) {
        timeString
    }
}

@Composable
fun ScheduleCard(schedule: Schedule?, isLoading: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "My Schedule",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                } else if (schedule != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Shift Start", fontSize = 14.sp, color = TextPrimary)
                            Text(
                                text = formatTime(schedule.startTime),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Shift End", fontSize = 14.sp, color = TextPrimary)
                            Text(
                                text = formatTime(schedule.endTime),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No active schedule found.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}