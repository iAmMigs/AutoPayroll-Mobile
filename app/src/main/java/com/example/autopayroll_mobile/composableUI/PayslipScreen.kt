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
// import androidx.compose.material.icons.automirrored.filled.ArrowBack // No longer needed
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipScreen(
    viewModel: PayslipViewModel
    // <-- 1. REMOVED onBack PARAMETER
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll") }
                // <-- 2. REMOVED navigationIcon PARAMETER
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            EmployeeHeader(uiState)
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
                modifier = Modifier.padding(horizontal = 16.dp)
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
            }
        }
    }
}

// ... (Rest of the file is unchanged) ...

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
            Text(
                text = payslip.status,
                color = when (payslip.status.lowercase()) {
                    "processing" -> Color(0xFFFFA500) // Orange
                    "completed" -> Color(0xFF4CAF50) // Green
                    else -> Color.Black
                },
                fontWeight = FontWeight.SemiBold
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