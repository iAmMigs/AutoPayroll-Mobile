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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import com.example.autopayroll_mobile.utils.TutorialManager
import com.example.autopayroll_mobile.utils.TutorialStep

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
    onViewDetails: (Payslip) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val isTutorialActive by TutorialManager.isTutorialActive.collectAsState()
    val currentStep by TutorialManager.currentStep.collectAsState()

    LaunchedEffect(currentStep) {
        if (isTutorialActive && currentStep == TutorialStep.NAVIGATE_TO_PAYSLIP) {
            TutorialManager.nextStep(TutorialStep.PAYSLIP_OVERVIEW)
        }
    }

    // DIRECT OVERRIDE
    val activePayslips = if (isTutorialActive) {
        listOf(
            Payslip(dateRange = "16-31 March", referenceId = "#PAY-2026-0001", originalPayDate = "2026-03-31", netAmount = "₱15,000.00", status = "Released", downloadPeriod = "16-31", downloadYear = 2026, downloadMonth = 3, year = 2026, employeeId = "EMP-001")
        )
    } else { uiState.payslips }

    var filterRect by remember { mutableStateOf<Rect?>(null) }
    var listRect by remember { mutableStateOf<Rect?>(null) }
    var cardRect by remember { mutableStateOf<Rect?>(null) }
    var viewBtnRect by remember { mutableStateOf<Rect?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshData() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // FIXED ALIGNMENT ISSUE: Box handles background, Column handles the Insets
    Box(modifier = Modifier.fillMaxSize().background(WebBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "My Payslips", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextHeader)
            Text(text = "View your payment history.", fontSize = 14.sp, color = TextBody, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).tutorialTarget(isActive = currentStep == TutorialStep.PAYSLIP_FILTER) { filterRect = it },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "FILTER BY YEAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextBody, letterSpacing = 1.sp)
                WebYearDropdown(selectedYear = uiState.selectedYear, availableYears = uiState.availableYears, onYearSelected = { viewModel.onYearSelected(it) })
            }

            val showEmptyState = !isTutorialActive && !uiState.isLoading && (uiState.payslips.isEmpty() || uiState.listErrorMessage != null)

            Box(modifier = Modifier.fillMaxWidth().weight(1f).tutorialTarget(isActive = currentStep == TutorialStep.PAYSLIP_LIST_AREA) { listRect = it }) {
                if (uiState.isLoading && !isTutorialActive) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { items(5) { PayslipItemPlaceholder() } }
                } else if (showEmptyState) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = uiState.listErrorMessage ?: "No available payslip", color = TextLabel, fontSize = 16.sp) }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(activePayslips) { payslip ->
                            val isFirst = payslip == activePayslips.first()
                            WebPayslipCard(
                                payslip = payslip,
                                cardModifier = if (isFirst) Modifier.tutorialTarget(isActive = currentStep == TutorialStep.PAYSLIP_CARD) { cardRect = it } else Modifier,
                                btnModifier = if (isFirst) Modifier.tutorialTarget(isActive = currentStep == TutorialStep.PAYSLIP_VIEW_BTN) { viewBtnRect = it } else Modifier,
                                onViewDetails = {
                                    if (isTutorialActive) TutorialManager.nextStep(TutorialStep.PAYSLIP_DETAIL_INFO)
                                    onViewDetails(payslip)
                                }
                            )
                        }
                    }
                }
            }
        }

        // OVERLAYS
        if (isTutorialActive) {
            when (currentStep) {
                TutorialStep.PAYSLIP_OVERVIEW -> { TutorialOverlay(title = "Payslip Module", description = "Welcome to the Payslip module! Here you can review your complete payment history.", onNext = { TutorialManager.nextStep(TutorialStep.PAYSLIP_FILTER) }) }
                TutorialStep.PAYSLIP_FILTER -> { TutorialOverlay(title = "Filter Records", description = "Use this dropdown to filter your payslips by year to easily find older records.", targetRect = filterRect, onNext = { TutorialManager.nextStep(TutorialStep.PAYSLIP_LIST_AREA) }, onBack = { TutorialManager.nextStep(TutorialStep.PAYSLIP_OVERVIEW) }) }
                TutorialStep.PAYSLIP_LIST_AREA -> { TutorialOverlay(title = "Payslip Records", description = "This area will display all your available payslips for the selected year.", targetRect = listRect, onNext = { TutorialManager.nextStep(TutorialStep.PAYSLIP_CARD) }, onBack = { TutorialManager.nextStep(TutorialStep.PAYSLIP_FILTER) }) }
                TutorialStep.PAYSLIP_CARD -> { TutorialOverlay(title = "Payslip Summary", description = "Each card shows a quick summary: the exact pay period, your total net amount, and the status.", targetRect = cardRect, customYOffset = 30.dp, onNext = { TutorialManager.nextStep(TutorialStep.PAYSLIP_VIEW_BTN) }, onBack = { TutorialManager.nextStep(TutorialStep.PAYSLIP_LIST_AREA) }) }
                TutorialStep.PAYSLIP_VIEW_BTN -> {
                    TutorialOverlay(
                        title = "View Details",
                        description = "Tap this eye button on any card to open the full detailed breakdown of your earnings and deductions.",
                        targetRect = viewBtnRect,
                        showNextButton = false, // MUST TAP THE BUTTON
                        onNext = { },
                        onBack = { TutorialManager.nextStep(TutorialStep.PAYSLIP_CARD) }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun WebPayslipCard(
    payslip: Payslip,
    onViewDetails: () -> Unit,
    cardModifier: Modifier = Modifier,
    btnModifier: Modifier = Modifier
) {
    val (bg, txt) = when (payslip.status.lowercase()) {
        "released", "paid", "completed" -> StatusReleasedBg to StatusReleasedText
        "processing", "pending" -> StatusProcessingBg to StatusProcessingText
        "rejected", "failed" -> StatusRejectedBg to StatusRejectedText
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Card(
        modifier = cardModifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(text = "PAY PERIOD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLabel, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = payslip.dateRange, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextHeader)
                    Text(text = payslip.year.toString(), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextBody)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(text = payslip.status, color = txt, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "REFERENCE ID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLabel, letterSpacing = 0.5.sp)
                    Text(text = payslip.referenceId, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextBody)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = WebBorderColor, thickness = 1.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "NET PAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLabel, letterSpacing = 0.5.sp)
                    Text(text = payslip.netAmount, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHeader)
                }

                IconButton(
                    onClick = onViewDetails,
                    modifier = btnModifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = "View Payslip Details", tint = Color(0xFF475569))
                }
            }
        }
    }
}

// Keep existing WebYearDropdown, PayslipItemPlaceholder, shimmerBrush
@Composable
fun WebYearDropdown(selectedYear: Int, availableYears: List<Int>, onYearSelected: (Int) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        Surface(onClick = { isExpanded = true }, modifier = Modifier.width(100.dp).height(36.dp), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, WebBorderColor), color = WebSurface) {
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = selectedYear.toString(), fontSize = 13.sp, color = TextHeader)
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = TextBody, modifier = Modifier.size(20.dp))
            }
        }
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }, modifier = Modifier.background(WebSurface).width(100.dp)) {
            availableYears.forEach { year -> DropdownMenuItem(text = { Text(year.toString(), fontSize = 14.sp, color = TextHeader) }, onClick = { onYearSelected(year); isExpanded = false }) }
        }
    }
}
@Composable
fun PayslipItemPlaceholder() {
    val shimmerBrush = shimmerBrush()
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = WebSurface), border = BorderStroke(1.dp, WebBorderColor), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column { Box(modifier = Modifier.height(10.dp).width(60.dp).background(shimmerBrush)); Spacer(modifier = Modifier.height(8.dp)); Box(modifier = Modifier.height(14.dp).width(120.dp).background(shimmerBrush)) }
                Box(modifier = Modifier.height(20.dp).width(50.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.height(18.dp).width(100.dp).background(shimmerBrush))
            Box(modifier = Modifier.height(32.dp).width(60.dp).background(shimmerBrush))
        }
    }
}
@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    if (!showShimmer) return Brush.linearGradient(colors = listOf(Color.LightGray.copy(alpha = 0.6f), Color.LightGray.copy(alpha = 0.6f)))
    val shimmerColors = listOf(Color.LightGray.copy(alpha = 0.6f), Color.LightGray.copy(alpha = 0.2f), Color.LightGray.copy(alpha = 0.6f))
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(initialValue = 0f, targetValue = targetValue, animationSpec = infiniteRepeatable(animation = tween(800, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "shimmer_anim")
    return Brush.linearGradient(colors = shimmerColors, start = Offset.Zero, end = Offset(x = translateAnimation.value, y = translateAnimation.value))
}