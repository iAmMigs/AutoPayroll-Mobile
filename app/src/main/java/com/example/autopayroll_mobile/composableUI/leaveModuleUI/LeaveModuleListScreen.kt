package com.example.autopayroll_mobile.composableUI.leaveModuleUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.composableUI.StatusChip
import com.example.autopayroll_mobile.composableUI.TutorialOverlay
import com.example.autopayroll_mobile.composableUI.tutorialTarget
import com.example.autopayroll_mobile.data.model.LeaveBalance // Using proper class name now
import com.example.autopayroll_mobile.data.model.LeaveRequest
import com.example.autopayroll_mobile.utils.TutorialManager
import com.example.autopayroll_mobile.utils.TutorialStep
import com.example.autopayroll_mobile.viewmodel.LeaveModuleUiState
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel
import com.google.gson.Gson
import java.time.LocalDate

private val WebBackground = Color(0xFFF8F9FA)
private val WebSurface = Color.White
private val WebBorderColor = Color(0xFFE2E8F0)
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF64748B)
private val AccentYellow = Color(0xFFFFC107)
private val IconBlue = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveModuleListScreen(
    viewModel: LeaveModuleViewModel,
    onFileLeaveClicked: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val isTutorialActive by TutorialManager.isTutorialActive.collectAsState()
    val currentStep by TutorialManager.currentStep.collectAsState()

    LaunchedEffect(currentStep) {
        if (isTutorialActive && currentStep == TutorialStep.NAVIGATE_TO_LEAVE) {
            TutorialManager.nextStep(TutorialStep.LEAVE_OVERVIEW)
        }
    }

    // FIXED: MOCK DATA
    val activeState = remember(isTutorialActive, uiState) {
        if (isTutorialActive) {
            // Explicitly casting it to LeaveBalance to fix the 'Any' error
            val mockLeaveBalance: LeaveBalance? = try { Gson().fromJson("""{"available": 12.0, "used": 3.0}""", LeaveBalance::class.java) } catch(e:Exception) { null }
            val mockRequests = try { listOf(
                Gson().fromJson("""{"id":"1", "employee_id":"EMP", "leave_type": "vacation", "start_date": "2026-03-01", "end_date": "2026-03-02", "status": "Pending"}""", LeaveRequest::class.java),
                Gson().fromJson("""{"id":"2", "employee_id":"EMP", "leave_type": "sick", "start_date": "2026-01-10", "end_date": "2026-01-11", "status": "Approved"}""", LeaveRequest::class.java)
            ) } catch(e:Exception) { emptyList() }

            uiState.copy(
                isLoading = false,
                leaveBalance = mockLeaveBalance ?: uiState.leaveBalance,
                allRequests = mockRequests
                // Removed filteredRequests as it does not exist in your UiState!
            )
        } else uiState
    }

    var statsRect by remember { mutableStateOf<Rect?>(null) }
    var backBtnRect by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchData() }

    val pendingCount = activeState.allRequests.count { it.status.equals("Pending", ignoreCase = true) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Leave Management", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier.tutorialTarget(isActive = currentStep == TutorialStep.NAVIGATE_BACK_FROM_LEAVE) { backBtnRect = it },
                            onClick = { if (!isTutorialActive || currentStep == TutorialStep.NAVIGATE_BACK_FROM_LEAVE) onBackToMenu() }
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextHeader) }
                    },
                    actions = { IconButton(onClick = { if (!isTutorialActive) viewModel.showCalendar() }) { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar", tint = TextHeader) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WebBackground, titleContentColor = TextHeader)
                )
            },
            containerColor = WebBackground
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize()) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().tutorialTarget(isActive = currentStep == TutorialStep.LEAVE_STATS) { statsRect = it },
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatsCard(label = "AVAILABLE CREDITS", value = activeState.leaveBalance.available.toString(), icon = Icons.Default.EventAvailable, iconTint = IconBlue, modifier = Modifier.weight(1f))
                    StatsCard(label = "PENDING", value = pendingCount.toString(), icon = Icons.Default.PendingActions, iconTint = AccentYellow, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
                Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHeader, modifier = Modifier.padding(bottom = 12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WebActionButton("File Leave", R.drawable.ic_file_leave, { if (!isTutorialActive) onFileLeaveClicked() }, Modifier.weight(1f))
                    WebActionButton("Track Leaves", R.drawable.ic_track_request, { /* Add your normal tracking logic here */ }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(32.dp))
                Text("Recent History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHeader, modifier = Modifier.padding(bottom = 12.dp))
                if (activeState.allRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(WebSurface, RoundedCornerShape(8.dp)).border(1.dp, WebBorderColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("No leave history found", color = TextBody, fontSize = 14.sp) }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(activeState.allRequests.take(3)) { request -> LeaveHistoryItem(request, viewModel) } }
                }
            }
        }

        if (uiState.isCalendarVisible) LeaveCalendarDialog(requests = activeState.allRequests, onDismiss = { viewModel.hideCalendar() })

        // OVERLAYS
        if (isTutorialActive) {
            when (currentStep) {
                TutorialStep.LEAVE_OVERVIEW -> {
                    TutorialOverlay(title = "Leave Module", description = "This is the Leave Hub. You can view your remaining balances and file new requests here.", onNext = { TutorialManager.nextStep(TutorialStep.LEAVE_STATS) })
                }
                TutorialStep.LEAVE_STATS -> {
                    TutorialOverlay(title = "Your Leave Credits", description = "Your available credits and currently pending requests are summarized in these cards.", targetRect = statsRect, onNext = { TutorialManager.nextStep(TutorialStep.NAVIGATE_BACK_FROM_LEAVE) }, onBack = { TutorialManager.nextStep(TutorialStep.LEAVE_OVERVIEW) })
                }
                TutorialStep.NAVIGATE_BACK_FROM_LEAVE -> {
                    TutorialOverlay(title = "Back to Menu", description = "Tap the top-left back arrow to return to the Menu for our final stop.", targetRect = backBtnRect, showNextButton = false, onNext = {}, onBack = { TutorialManager.nextStep(TutorialStep.LEAVE_STATS) })
                }
                else -> {}
            }
        }
    }
}

// --- FIXED CALENDAR DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveCalendarDialog(
    requests: List<LeaveRequest>,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    val selectedDateMillis = datePickerState.selectedDateMillis
    val selectedDate = selectedDateMillis?.let {
        java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.of("UTC")).toLocalDate()
    }

    val requestsOnDate = remember(selectedDate, requests) {
        if (selectedDate == null) emptyList()
        else requests.filter {
            try {
                val start = LocalDate.parse(it.startDate)
                val end = LocalDate.parse(it.endDate)
                !selectedDate.isBefore(start) && !selectedDate.isAfter(end)
            } catch (e: Exception) { false }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WebSurface),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight() // Allow height to adjust dynamically
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Custom Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Leave Calendar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHeader
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextBody)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // CLEAN CALENDAR:
                // title=null and headline=null removes the huge top area
                // showModeToggle=false removes the pencil icon
                DatePicker(
                    state = datePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = DatePickerDefaults.colors(
                        containerColor = WebSurface,
                        selectedDayContainerColor = AccentYellow,
                        todayDateBorderColor = AccentYellow
                    )
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = if (selectedDate != null) "Leaves on $selectedDate" else "Select a date",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBody
                )

                LazyColumn(modifier = Modifier.height(100.dp).padding(top = 8.dp)) {
                    if (requestsOnDate.isEmpty()) {
                        item { Text("No leaves found for this date.", fontSize = 12.sp, color = Color.Gray) }
                    } else {
                        items(requestsOnDate) { req ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(req.leaveType.capitalize(), fontSize = 14.sp, color = TextHeader)
                                StatusChip(status = req.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER COMPOSABLES (Same as before) ---
@Composable
fun StatsCard(label: String, value: String, icon: ImageVector, iconTint: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(110.dp),
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
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextHeader)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextBody, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun WebActionButton(text: String, iconRes: Int, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.height(80.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = TextHeader, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = text, fontWeight = FontWeight.Bold, color = TextHeader, fontSize = 14.sp)
        }
    }
}

@Composable
fun LeaveHistoryItem(request: LeaveRequest, viewModel: LeaveModuleViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(viewModel.formatLeaveType(request.leaveType), fontWeight = FontWeight.Bold, color = TextHeader, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = TextBody, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${request.startDate} - ${request.endDate}", color = TextBody, fontSize = 12.sp)
                }
            }
            StatusChip(status = request.status)
        }
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
}