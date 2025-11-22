package com.example.autopayroll_mobile.composableUI

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.data.model.Payslip
import com.example.autopayroll_mobile.viewmodel.PayslipUiState
import com.example.autopayroll_mobile.viewmodel.PayslipViewModel
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipScreen(
    viewModel: PayslipViewModel,
    onBack: () -> Unit // Kept for fragment dependency, but not used in UI
) {
    val uiState by viewModel.uiState.collectAsState()

    // Outer Box/Column to handle full screen and status bar padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Custom Header Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    // FIX: Removed Back button functionality and icon.
                    // Renamed title to "Payroll Viewing".
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        // Removed clickable modifier
                    ) {
                        // Removed Icon and Spacer
                        Text(
                            "Payroll Viewing", // RENAME: Payroll Viewing
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    EmployeeHeaderContent(uiState)
                }
                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Pay Slips",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    YearFilterDropdown(
                        selectedYear = uiState.selectedYear,
                        availableYears = uiState.availableYears,
                        onYearSelected = { year ->
                            viewModel.onYearSelected(year)
                        }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    if (uiState.isLoading) {
                        items(5) {
                            PayslipItemPlaceholder()
                        }
                    } else if (uiState.listErrorMessage != null) {
                        item {
                            val isError = uiState.listErrorMessage!!.startsWith("Error")
                            val message = uiState.listErrorMessage!!

                            Text(
                                text = message,
                                color = if (isError) MaterialTheme.colorScheme.error else Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(uiState.payslips) { payslip ->
                            PayslipItem(payslip = payslip)
                        }
                    }
                    // Add bottom padding space to the list
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeHeaderContent(uiState: PayslipUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profiledefault),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = uiState.employeeName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = uiState.jobAndCompany,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearFilterDropdown(
    selectedYear: Int,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { isExpanded = true },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(selectedYear.toString())
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Select Year"
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            availableYears.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onYearSelected(year)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PayslipItem(payslip: Payslip) {
    // Define status colors locally
    val StatusPending = Color(0xFFFFA726)
    val StatusApproved = Color(0xFF66BB6A)
    val StatusRejected = Color(0xFFEF5350)
    val StatusDefault = Color(0xFF9E9E9E)

    // Determine status text and colors
    val statusText = payslip.status.replaceFirstChar { it.uppercase() }

    // Use String.lowercase()
    val statusColor = when (payslip.status.lowercase()) {
        "processing", "pending" -> StatusPending
        "completed", "released" -> StatusApproved
        "rejected" -> StatusRejected
        else -> StatusDefault
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = payslip.dateRange, fontWeight = FontWeight.Bold)
                Text(text = "Net Amount: ${payslip.netAmount}", color = Color.Gray)
            }

            // Apply colorized background chip style
            Text(
                text = statusText,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun PayslipItemPlaceholder() {
    val shimmerBrush = shimmerBrush()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(150.dp)
                        .background(shimmerBrush)
                )
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(100.dp)
                        .background(shimmerBrush)
                )
            }
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(80.dp)
                    .background(shimmerBrush)
            )
        }
    }
}

@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    if (!showShimmer) {
        return Brush.linearGradient(
            colors = listOf(Color.LightGray.copy(alpha = 0.6f), Color.LightGray.copy(alpha = 0.6f)),
        )
    }

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}