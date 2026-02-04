package com.example.autopayroll_mobile.composableUI.dashboardUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.data.model.Schedule
import com.example.autopayroll_mobile.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- WEB DESIGN COLORS ---
val WebBackground = Color(0xFFF8F9FA)
val WebSurface = Color.White
val WebBorderColor = Color(0xFFE2E8F0)
val TextHeader = Color(0xFF1E293B)
val TextLabel = Color(0xFF64748B)
val TextBody = Color(0xFF334155)

// Status & Accents
val AccentYellow = Color(0xFFFFC107)
val AccentRed = Color(0xFFEF5350)
val StatusPaidBg = Color(0xFFDCFCE7)
val StatusPaidText = Color(0xFF166534)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            WebHeaderSection(uiState)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WebStatCard(title = "REGULAR", value = uiState.lastWorkedHours, unit = "hours", modifier = Modifier.weight(1f))
                WebStatCard(title = "OVERTIME", value = uiState.overtimeHours, unit = "hours", modifier = Modifier.weight(1f))

                // LATE logic: Ensure positive and Red color
                WebStatCard(
                    title = "LATE",
                    value = uiState.lateHours.replace("-", ""),
                    unit = "mins",
                    valueColor = AccentRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AccentStatCard(
                    title = "LEAVE BALANCE",
                    value = uiState.leaveCredits,
                    accentColor = AccentYellow,
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )
                AccentStatCard(
                    title = "ABSENCES",
                    value = uiState.absences,
                    accentColor = AccentRed,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // RESTORED: WebPayslipCard
            WebPayslipCard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            WebScheduleCard(schedule = uiState.currentSchedule, isLoading = uiState.isLoading)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ... WebHeaderSection, WebStatCard, AccentStatCard ...

@Composable
fun WebHeaderSection(state: DashboardUiState) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 5..11 -> "Good Morning,"
        in 12..16 -> "Good Afternoon,"
        else -> "Good Evening,"
    }

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
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, AccentYellow, CircleShape)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting ${state.employeeName}!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextHeader,
                lineHeight = 28.sp
            )
            Text(
                text = state.jobAndCompany,
                fontSize = 13.sp,
                color = TextLabel,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun WebStatCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextHeader
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextLabel,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = unit,
                fontSize = 10.sp,
                color = TextLabel
            )
        }
    }
}

@Composable
fun AccentStatCard(
    title: String,
    value: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLabel
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHeader
                )
            }
        }
    }
}

@Composable
fun WebPayslipCard(uiState: DashboardUiState) {
    Text(
        text = "Most Recent Payslip",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextHeader,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(text = "NET EARNING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLabel, modifier = Modifier.weight(1.2f))
                Text(text = "PAY DATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLabel, modifier = Modifier.weight(1f))
                Text(text = "STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLabel, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
            }

            Divider(color = WebBorderColor, thickness = 1.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (uiState.recentPayslip != null) {
                    val payslip = uiState.recentPayslip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₱${payslip.netPay}",
                            fontWeight = FontWeight.Bold,
                            color = TextHeader,
                            modifier = Modifier.weight(1.2f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatDate(payslip.payDate),
                            color = TextBody,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp
                        )

                        // Handle potential null status safely
                        val statusStr = payslip.status ?: "Released"
                        val isPaid = statusStr.equals("released", ignoreCase = true) || statusStr.equals("paid", ignoreCase = true)

                        Box(
                            modifier = Modifier.weight(0.8f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = if(isPaid) "Paid" else statusStr,
                                color = if(isPaid) StatusPaidText else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if(isPaid) StatusPaidBg else Color.Gray)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Text("No records found", color = TextLabel, fontSize = 14.sp)
                }
            }
            // Removed the bottom footer row/button area entirely
        }
    }
}

// ... WebScheduleCard and formatters ...
@Composable
fun WebScheduleCard(schedule: Schedule?, isLoading: Boolean) {
    Text(
        text = "My Schedule",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextHeader,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SHIFT SCHEDULE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextLabel,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else if (schedule != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Start Time
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatTimeBig(schedule.startTime),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHeader
                        )
                        Text(
                            text = formatTimeAmPm(schedule.startTime),
                            fontSize = 12.sp,
                            color = TextLabel
                        )
                    }

                    // Arrow
                    Text(
                        text = "→",
                        fontSize = 24.sp,
                        color = AccentYellow,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    // End Time
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatTimeBig(schedule.endTime),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHeader
                        )
                        Text(
                            text = formatTimeAmPm(schedule.endTime),
                            fontSize = 12.sp,
                            color = TextLabel
                        )
                    }
                }
            } else {
                Text("No active schedule", color = TextLabel)
            }
        }
    }
}

fun formatTimeBig(timeString: String?): String {
    if (timeString == null) return "--:--"
    return try {
        val parser = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("hh:mm", Locale.getDefault())
        formatter.format(parser.parse(timeString) ?: Date())
    } catch (e: Exception) { "--:--" }
}

fun formatTimeAmPm(timeString: String?): String {
    if (timeString == null) return "--"
    return try {
        val parser = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("a", Locale.getDefault())
        formatter.format(parser.parse(timeString) ?: Date())
    } catch (e: Exception) { "" }
}

fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(parser.parse(dateString) ?: Date())
    } catch (e: Exception) { dateString }
}