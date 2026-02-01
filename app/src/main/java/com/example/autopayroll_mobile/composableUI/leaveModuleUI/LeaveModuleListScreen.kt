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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.composableUI.StatusChip
import com.example.autopayroll_mobile.data.model.LeaveRequest
import com.example.autopayroll_mobile.viewmodel.LeaveModuleUiState
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel
import com.example.autopayroll_mobile.viewmodel.filteredRequests
import java.time.LocalDate

// --- WEB DESIGN TOKENS ---
private val WebBackground = Color(0xFFF8F9FA)
private val WebSurface = Color.White
private val WebBorderColor = Color(0xFFE2E8F0)
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF64748B)
private val AccentYellow = Color(0xFFFFC107)
private val IconBlue = Color(0xFF3B82F6)

@Composable
fun LeaveModuleListScreen(
    viewModel: LeaveModuleViewModel,
    onFileLeaveClicked: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentView by remember { mutableStateOf("hub") }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    if (currentView == "hub") {
        LeaveHubContent(
            uiState = uiState,
            viewModel = viewModel,
            onFileClick = onFileLeaveClicked,
            onTrackClick = { currentView = "list" },
            onBack = onBackToMenu
        )
    } else {
        LeaveListContent(
            uiState = uiState,
            viewModel = viewModel,
            onBack = { currentView = "hub" },
            onFileLeaveClicked = onFileLeaveClicked
        )
    }

    // --- POP-UP CALENDAR DIALOG ---
    if (uiState.isCalendarVisible) {
        LeaveCalendarDialog(
            requests = uiState.allRequests,
            onDismiss = { viewModel.hideCalendar() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveHubContent(
    uiState: LeaveModuleUiState,
    viewModel: LeaveModuleViewModel,
    onFileClick: () -> Unit,
    onTrackClick: () -> Unit,
    onBack: () -> Unit
) {
    val pendingCount = uiState.allRequests.count { it.status.equals("Pending", ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextHeader)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCalendar() }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar", tint = TextHeader)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WebBackground, titleContentColor = TextHeader)
            )
        },
        containerColor = WebBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    label = "AVAILABLE CREDITS",
                    value = uiState.leaveBalance.available.toString(),
                    icon = Icons.Default.EventAvailable,
                    iconTint = IconBlue,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    label = "PENDING",
                    value = pendingCount.toString(),
                    icon = Icons.Default.PendingActions,
                    iconTint = AccentYellow,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHeader, modifier = Modifier.padding(bottom = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WebActionButton("File Leave", R.drawable.ic_file_leave, onFileClick, Modifier.weight(1f))
                WebActionButton("Track Leaves", R.drawable.ic_track_request, onTrackClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(32.dp))
            Text("Recent History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHeader, modifier = Modifier.padding(bottom = 12.dp))
            if (uiState.allRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(WebSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, WebBorderColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No leave history found", color = TextBody, fontSize = 14.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.allRequests.take(3)) { request -> LeaveHistoryItem(request, viewModel) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveListContent(
    uiState: LeaveModuleUiState,
    viewModel: LeaveModuleViewModel,
    onBack: () -> Unit,
    onFileLeaveClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Leaves", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextHeader)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WebBackground, titleContentColor = TextHeader)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFileLeaveClicked, containerColor = AccentYellow, contentColor = TextHeader) {
                Icon(Icons.Default.Add, contentDescription = "File")
            }
        },
        containerColor = WebBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = viewModel.tabItems.indexOf(uiState.selectedTab),
                containerColor = WebBackground,
                contentColor = TextHeader,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[viewModel.tabItems.indexOf(uiState.selectedTab)]),
                        color = AccentYellow
                    )
                }
            ) {
                viewModel.tabItems.forEach { title ->
                    Tab(
                        selected = uiState.selectedTab == title,
                        onClick = { viewModel.onTabSelected(title) },
                        text = { Text(title, fontWeight = if (uiState.selectedTab == title) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = TextHeader)
                } else if (uiState.filteredRequests.isEmpty()) {
                    Text("No ${uiState.selectedTab.lowercase()} requests.", modifier = Modifier.align(Alignment.Center), color = TextBody)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.filteredRequests) { request -> LeaveHistoryItem(request, viewModel) }
                    }
                }
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