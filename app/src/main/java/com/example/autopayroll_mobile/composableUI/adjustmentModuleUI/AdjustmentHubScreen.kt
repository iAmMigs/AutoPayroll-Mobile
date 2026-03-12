package com.example.autopayroll_mobile.composableUI.adjustmentModuleUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.composableUI.StatusChip
import com.example.autopayroll_mobile.composableUI.TutorialOverlay
import com.example.autopayroll_mobile.composableUI.tutorialTarget
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentModuleUiState
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentRequest
import com.example.autopayroll_mobile.utils.TutorialManager
import com.example.autopayroll_mobile.utils.TutorialStep
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel
import com.google.gson.Gson
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val WebBackground = Color(0xFFF8F9FA)
private val WebSurface = Color.White
private val WebBorderColor = Color(0xFFE2E8F0)
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF64748B)
private val AccentYellow = Color(0xFFFFC107)
private val IconBgYellow = Color(0xFFFFF7ED)
private val IconYellow = Color(0xFFF59E0B)
private val IconBgBlue = Color(0xFFEFF6FF)
private val IconBlue = Color(0xFF3B82F6)
private val IconBgGreen = Color(0xFFDCFCE7)
private val IconGreen = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentHubScreen(
    uiState: AdjustmentModuleUiState,
    viewModel: AdjustmentModuleViewModel,
    onNavigateToFiling: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val isTutorialActive by TutorialManager.isTutorialActive.collectAsState()
    val currentStep by TutorialManager.currentStep.collectAsState()

    LaunchedEffect(currentStep) {
        if (isTutorialActive && currentStep == TutorialStep.NAVIGATE_TO_ADJUSTMENT) {
            TutorialManager.nextStep(TutorialStep.ADJUSTMENT_OVERVIEW)
        }
    }

    val activeState = remember(isTutorialActive, uiState) {
        if (isTutorialActive) {
            // We use an expanded JSON to guarantee we hit the exact @SerializedName keys your app uses
            val mockJson1 = """{
                "id": "1", "adjustment_id": "1", "employee_id": "EMP", "reference_id": "ADJ-001",
                "type": "Overtime", "adjustment_type": "Overtime", "adjustmentType": "Overtime",
                "date": "2026-03-12T10:00:00Z", "date_submitted": "2026-03-12T10:00:00Z", "created_at": "2026-03-12T10:00:00Z", "start_date": "2026-03-12",
                "status": "Pending", "remarks": "Tutorial data", "reason": "Tutorial data", "description": "Tutorial"
            }"""

            val mockJson2 = """{
                "id": "2", "adjustment_id": "2", "employee_id": "EMP", "reference_id": "ADJ-002",
                "type": "Missed Punch", "adjustment_type": "Missed Punch", "adjustmentType": "Missed Punch",
                "date": "2026-03-10T10:00:00Z", "date_submitted": "2026-03-10T10:00:00Z", "created_at": "2026-03-10T10:00:00Z", "start_date": "2026-03-10",
                "status": "Approved", "remarks": "Tutorial data", "reason": "Tutorial data", "description": "Tutorial"
            }"""

            val mockRequests = try { listOf(
                Gson().fromJson(mockJson1, AdjustmentRequest::class.java),
                Gson().fromJson(mockJson2, AdjustmentRequest::class.java)
            ) } catch(e:Exception) { emptyList() }

            uiState.copy(isLoading = false, adjustmentRequests = mockRequests)
        } else uiState
    }

    val pendingCount = activeState.adjustmentRequests.count { it.status.equals("Pending", ignoreCase = true) }
    val approvedCount = activeState.adjustmentRequests.count { it.status.equals("Approved", ignoreCase = true) }

    val filteredList = remember(activeState.adjustmentRequests, activeState.filterStatus) {
        if (activeState.filterStatus == "All") activeState.adjustmentRequests
        else activeState.adjustmentRequests.filter { it.status.equals(activeState.filterStatus, ignoreCase = true) }
    }

    var statsRect by remember { mutableStateOf<Rect?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Payroll Credit Adjustment", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextHeader) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WebBackground, titleContentColor = TextHeader)
                )
            },
            containerColor = WebBackground,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { if (!isTutorialActive) onNavigateToFiling() },
                    containerColor = AccentYellow,
                    contentColor = TextHeader,
                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("File Request", fontWeight = FontWeight.Bold)
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().tutorialTarget(isActive = currentStep == TutorialStep.ADJUSTMENT_STATS) { statsRect = it },
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatsCard(label = "PENDING", value = pendingCount.toString(), icon = Icons.Default.HourglassEmpty, iconTint = IconYellow, modifier = Modifier.weight(1f))
                            StatsCard(label = "APPROVED", value = approvedCount.toString(), icon = Icons.Default.Check, iconTint = IconGreen, modifier = Modifier.weight(1f))
                        }
                    }

                    item {
                        Column {
                            HorizontalDivider(color = WebBorderColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Adjustment History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHeader)
                                FilterChipButton(activeState.filterStatus, { if (!isTutorialActive) viewModel.onFilterChanged(it) })
                            }
                        }
                    }

                    if (activeState.isLoading && !isTutorialActive) {
                        item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = TextHeader) } }
                    } else if (filteredList.isEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Inbox, null, tint = Color(0xFFE2E8F0), modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No available data", color = TextBody, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(filteredList, key = { it.id }) { request ->
                            HistoryItemCard(request = request, onClick = { if (!isTutorialActive) onNavigateToDetail(request.id) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }

        // OVERLAYS
        if (isTutorialActive) {
            when (currentStep) {
                TutorialStep.ADJUSTMENT_OVERVIEW -> {
                    TutorialOverlay(title = "Adjustment Module", description = "Finally, this is where you can file for missing punches, overtime, and schedule adjustments.", onNext = { TutorialManager.nextStep(TutorialStep.ADJUSTMENT_STATS) })
                }
                TutorialStep.ADJUSTMENT_STATS -> {
                    TutorialOverlay(title = "Track Adjustments", description = "Monitor the approval status of your filed adjustments directly from here.", targetRect = statsRect, onNext = { TutorialManager.nextStep(TutorialStep.TUTORIAL_END) }, onBack = { TutorialManager.nextStep(TutorialStep.ADJUSTMENT_OVERVIEW) })
                }
                TutorialStep.TUTORIAL_END -> {
                    // CONCLUDE TUTORIAL
                    TutorialOverlay(title = "You're All Set!", description = "That concludes the tutorial. You are now ready to use AutoPayroll! Click 'Finish' below to unlock the app.", onNext = { TutorialManager.endTutorial() }, onBack = { TutorialManager.nextStep(TutorialStep.ADJUSTMENT_STATS) })
                }
                else -> {}
            }
        }
    }
}

// ... Keep existing StatsCard, LatestStatsCardCompact, HistoryItemCard, FilterChipButton ...

// --- UPDATED CARDS FOR FIXED LAYOUT ---

@Composable
fun StatsCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp), // Fixed height to align all cards
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextBody,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextHeader
            )
        }
    }
}

@Composable
fun LatestStatsCardCompact(
    label: String,
    request: AdjustmentRequest?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextBody,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (request != null) {
                // Shorten type name for mobile box (e.g. "Official Business" -> "Official...")
                Text(
                    text = request.type.replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHeader,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                val date = try {
                    OffsetDateTime.parse(request.dateSubmitted)
                        .format(DateTimeFormatter.ofPattern("MMM dd", Locale.US))
                } catch (e: Exception) { "" }
                Text(
                    text = date,
                    fontSize = 11.sp,
                    color = TextBody,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "-",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(request: AdjustmentRequest, onClick: () -> Unit) {
    val displayDate = remember(request.dateSubmitted) {
        try { OffsetDateTime.parse(request.dateSubmitted).format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US)) } catch (e: Exception) { request.dateSubmitted ?: "" }
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.type.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, color = TextHeader, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = TextBody, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(displayDate, color = TextBody, fontSize = 12.sp)
                }
            }
            StatusChip(status = request.status)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = WebBorderColor)
        }
    }
}

@Composable
fun FilterChipButton(currentStatus: String, onStatusSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("All", "Pending", "Approved", "Rejected")
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            border = BorderStroke(1.dp, WebBorderColor),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp), tint = TextBody)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (currentStatus == "All") "Filter" else currentStatus, color = TextBody, fontSize = 12.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(WebSurface)) {
            options.forEach { status ->
                DropdownMenuItem(text = { Text(status, color = TextHeader) }, onClick = { onStatusSelected(status); expanded = false })
            }
        }
    }
}