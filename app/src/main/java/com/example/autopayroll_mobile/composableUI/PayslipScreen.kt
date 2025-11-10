package com.example.autopayroll_mobile.composableUI

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.data.model.Payslip
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.PayslipUiState
import com.example.autopayroll_mobile.viewmodel.PayslipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipScreen(viewModel: PayslipViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back press */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            EmployeeHeader(uiState)
            HorizontalDivider()
            Text(
                text = "My Pay Slips",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )

            // UPDATED LAZYCOLUMN: Handles Loading, Error, and Success states
            LazyColumn {

                if (uiState.isLoading) {
                    // Show 5 placeholder cards while loading
                    items(5) {
                        PayslipItemPlaceholder()
                    }
                } else if (uiState.listErrorMessage != null) {
                    // Show the error message if loading is done AND there's an error
                    item {
                        Text(
                            text = uiState.listErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Show the real payslip items when loading is done and no error
                    items(uiState.payslips) { payslip ->
                        PayslipItem(payslip = payslip)
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeHeader(uiState: PayslipUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                fontSize = 18.sp
            )
            Text(
                text = uiState.jobAndCompany,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PayslipItem(payslip: Payslip) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            Text(
                text = payslip.status,
                color = when (payslip.status) {
                    "Processing" -> Color(0xFFFFA500) // Orange
                    "Completed" -> Color(0xFF4CAF50) // Green
                    else -> Color.Black
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// --- NEW PLACEHOLDER COMPOSABLE ---
@Composable
fun PayslipItemPlaceholder() {
    val shimmerBrush = shimmerBrush()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                // Placeholder for Date Range
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(150.dp)
                        .background(shimmerBrush)
                )
                // Placeholder for Net Amount
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(100.dp)
                        .background(shimmerBrush)
                )
            }
            // Placeholder for Status
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(80.dp)
                    .background(shimmerBrush)
            )
        }
    }
}

// --- NEW SHIMMER BRUSH COMPOSABLE ---
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