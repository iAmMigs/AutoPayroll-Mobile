package com.example.autopayroll_mobile.composableUI

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.autopayroll_mobile.data.model.Payslip
import com.example.autopayroll_mobile.viewmodel.PayslipViewModel

// --- WEB DESIGN TOKENS ---
private val WebBackground = Color(0xFFF8F9FA)
private val WebSurface = Color.White
private val WebBorderColor = Color(0xFFE2E8F0)
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF64748B)
private val TextLabel = Color(0xFF94A3B8)

private val StatusReleasedBg = Color(0xFFDCFCE7)
private val StatusReleasedText = Color(0xFF166534)
private val StatusProcessingBg = Color(0xFFFEF3C7)
private val StatusProcessingText = Color(0xFFB45309)
private val StatusRejectedBg = Color(0xFFFEE2E2)
private val StatusRejectedText = Color(0xFF991B1B)

@Composable
fun PayslipScreen(
    viewModel: PayslipViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // AUTO-REFRESH: Update data when screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "My Payslips",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextHeader
            )
            Text(
                text = "View your payment history.", // Text slightly updated since download is gone
                fontSize = 14.sp,
                color = TextBody,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // FILTERS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FILTER BY YEAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBody,
                    letterSpacing = 1.sp
                )

                WebYearDropdown(
                    selectedYear = uiState.selectedYear,
                    availableYears = uiState.availableYears,
                    onYearSelected = { viewModel.onYearSelected(it) }
                )
            }

            // LIST CONTENT
            val showEmptyState = !uiState.isLoading && (uiState.payslips.isEmpty() || uiState.listErrorMessage != null)

            if (uiState.isLoading) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(5) { PayslipItemPlaceholder() }
                }
            } else if (showEmptyState) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.listErrorMessage ?: "No available payslip",
                        color = TextLabel,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.payslips) { payslip ->
                        // UPDATED: No longer passing a click listener
                        WebPayslipCard(payslip = payslip)
                    }
                }
            }
        }
    }
}

@Composable
fun WebPayslipCard(payslip: Payslip) {
    val (bg, txt) = when (payslip.status.lowercase()) {
        "released", "paid", "completed" -> StatusReleasedBg to StatusReleasedText
        "processing", "pending" -> StatusProcessingBg to StatusProcessingText
        "rejected", "failed" -> StatusRejectedBg to StatusRejectedText
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Pay Period & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "PAY PERIOD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLabel,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = payslip.dateRange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHeader
                    )
                    Text(
                        text = payslip.year.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextBody
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(bg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = payslip.status,
                        color = txt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = WebBorderColor,
                thickness = 1.dp
            )

            // Row 2: Net Pay ONLY (Button Removed)
            Column {
                Text(
                    text = "NET PAY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLabel,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = payslip.netAmount,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHeader
                )
            }
        }
    }
}

@Composable
fun WebYearDropdown(
    selectedYear: Int,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { isExpanded = true },
            modifier = Modifier
                .width(100.dp)
                .height(36.dp),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, WebBorderColor),
            color = WebSurface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedYear.toString(),
                    fontSize = 13.sp,
                    color = TextHeader
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TextBody,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .background(WebSurface)
                .width(100.dp)
        ) {
            availableYears.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString(), fontSize = 14.sp, color = TextHeader) },
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
fun PayslipItemPlaceholder() {
    val shimmerBrush = shimmerBrush()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Box(modifier = Modifier.height(10.dp).width(60.dp).background(shimmerBrush))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.height(14.dp).width(120.dp).background(shimmerBrush))
                }
                Box(modifier = Modifier.height(20.dp).width(50.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Removed the button placeholder row
            Box(modifier = Modifier.height(18.dp).width(100.dp).background(shimmerBrush))
            Box(modifier = Modifier.height(32.dp).width(60.dp).background(shimmerBrush))
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