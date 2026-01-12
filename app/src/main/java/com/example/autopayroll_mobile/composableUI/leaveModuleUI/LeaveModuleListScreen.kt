package com.example.autopayroll_mobile.composableUI.leaveModuleUI

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.composableUI.StatusChip
import com.example.autopayroll_mobile.data.model.LeaveRequest
import com.example.autopayroll_mobile.viewmodel.LeaveModuleUiState
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel
import com.example.autopayroll_mobile.viewmodel.filteredRequests // Ensure this extension is imported or available
import java.time.format.DateTimeFormatter
import java.util.Locale

val CardSurface = Color.White
val TextPrimary = Color(0xFF1F2937)
val TextSecondary = Color(0xFF4B5563)
val AppBackground = Color(0xFFF5F5F5)

@Composable
fun LeaveModuleListScreen(
    viewModel: LeaveModuleViewModel,
    onFileLeaveClicked: () -> Unit,
    onCalendarClicked: () -> Unit,
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
            onFileClick = onFileLeaveClicked,
            onTrackClick = { currentView = "list" },
            onCalendarClick = onCalendarClicked,
            onBack = onBackToMenu
        )
    } else {
        LeaveListContent(
            uiState = uiState,
            viewModel = viewModel,
            onBack = { currentView = "hub" },
            onCalendarClicked = onCalendarClicked,
            onFileLeaveClicked = onFileLeaveClicked
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveHubContent(
    uiState: LeaveModuleUiState,
    onFileClick: () -> Unit,
    onTrackClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onBack: () -> Unit
) {
    val pendingCount by remember(uiState.allRequests) {
        derivedStateOf { uiState.allRequests.count { it.status.equals("Pending", ignoreCase = true) } }
    }

    val latestRequest by remember(uiState.allRequests) {
        derivedStateOf {
            uiState.allRequests.maxByOrNull { it.createdAt }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Module") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            LeaveBalanceCardHub(uiState)

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HubNavigationButton(
                    text = "File Leave",
                    iconResId = R.drawable.ic_file_leave,
                    onClick = onFileClick,
                    modifier = Modifier.weight(1f)
                )
                HubNavigationButton(
                    text = "Track Leaves",
                    iconResId = R.drawable.ic_track_request,
                    onClick = onTrackClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Optional: Calendar Button (Full Width)
            OutlinedButton(
                onClick = onCalendarClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(painterResource(id = R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("View Leave Calendar")
            }

            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppBackground)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pending Requests", color = TextSecondary)
                    Text(
                        text = pendingCount.toString(),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(Modifier.padding(vertical = 16.dp), color = Color.LightGray)

                // Latest Request Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Latest Request", color = TextSecondary)
                    if (latestRequest != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            StatusChip(status = latestRequest!!.status) // Reuse your StatusChip

                            val displayDate = remember(latestRequest) {
                                try {
                                    // Parse your date format here
                                    latestRequest!!.startDate
                                } catch (e: Exception) { "N/A" }
                            }

                            Text(
                                text = "($displayDate)",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        Text("No history", fontSize = 12.sp, color = TextSecondary)
                    }
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
    onCalendarClicked: () -> Unit,
    onFileLeaveClicked: () -> Unit
) {
    val tabItems = viewModel.tabItems

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Leaves") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCalendarClicked) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Leave Calendar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFileLeaveClicked,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "File a Leave")
            }
        },
        containerColor = AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = tabItems.indexOf(uiState.selectedTab),
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabItems.forEach { title ->
                    Tab(
                        selected = uiState.selectedTab == title,
                        onClick = { viewModel.onTabSelected(title) },
                        text = { Text(title) }
                    )
                }
            }

            // List Content
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (uiState.isLoading && uiState.allRequests.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.filteredRequests.isEmpty()) {
                    Text(
                        text = "No ${uiState.selectedTab.lowercase()} requests found.",
                        modifier = Modifier.align(Alignment.Center),
                        color = TextSecondary
                    )
                } else {
                    LeaveRequestList(
                        requests = uiState.filteredRequests,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun LeaveBalanceCardHub(uiState: LeaveModuleUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppBackground), // Light gray on white
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Leave Balance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BalanceItem(
                    count = uiState.leaveBalance.available,
                    label = "Available"
                )
                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color.LightGray)
                )
                BalanceItem(
                    count = uiState.leaveBalance.used,
                    label = "Used"
                )
            }
        }
    }
}

@Composable
fun BalanceItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun HubNavigationButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = text,
                modifier = Modifier.size(40.dp),
                tint = TextPrimary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = text,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun LeaveRequestList(requests: List<LeaveRequest>, viewModel: LeaveModuleViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(requests, key = { it.id }) {
            LeaveRequestItem(
                request = it,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun LeaveRequestItem(request: LeaveRequest, viewModel: LeaveModuleViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.formatLeaveType(request.leaveType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                StatusChip(status = request.status)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${viewModel.formatDisplayDate(request.startDate)} to ${viewModel.formatDisplayDate(request.endDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            if (request.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = request.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Filed on: ${viewModel.formatDisplayDate(request.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}