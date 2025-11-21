package com.example.autopayroll_mobile.composableUI.leaveModuleUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.data.model.LeaveRequest
import com.example.autopayroll_mobile.viewmodel.LeaveModuleUiState
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel
import com.example.autopayroll_mobile.viewmodel.filteredRequests

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveModuleListScreen(
    viewModel: LeaveModuleViewModel,
    onFileLeaveClicked: () -> Unit,
    onCalendarClicked: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabItems = viewModel.tabItems
    val lightGrayBackground = Color(0xFFF6F5F2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Request") },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Main Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCalendarClicked) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Leave Calendar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFileLeaveClicked,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "File a Leave")
            }
        },
        containerColor = lightGrayBackground
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LeaveBalanceCard(uiState = uiState)

            TabRow(
                selectedTabIndex = tabItems.indexOf(uiState.selectedTab)
            ) {
                tabItems.forEach { title ->
                    Tab(
                        selected = uiState.selectedTab == title,
                        onClick = { viewModel.onTabSelected(title) },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (uiState.isLoading && uiState.allRequests.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.filteredRequests.isEmpty()) {
                    Text(
                        text = "No ${uiState.selectedTab.lowercase()} requests found.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun LeaveBalanceCard(uiState: LeaveModuleUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BalanceItem(
                count = uiState.leaveBalance.available,
                label = "Available"
            )
            Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color.LightGray))
            BalanceItem(
                count = uiState.leaveBalance.used,
                label = "Used"
            )
        }
    }
}

@Composable
fun BalanceItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun LeaveRequestList(requests: List<LeaveRequest>, viewModel: LeaveModuleViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(requests, key = { it.id }) { request ->
            LeaveRequestItem(
                request = request,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun LeaveRequestItem(request: LeaveRequest, viewModel: LeaveModuleViewModel) {
    val statusColor = when (request.status.lowercase()) {
        "approved" -> Color(0xFF0A9396) // Greenish-Blue
        "declined", "rejected", "need revision", "needs revision" -> Color(0xFFAE2012) // Red
        else -> Color(0xFFEE9B00) // Orange (Pending)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ## CHANGE IS HERE ##
                // We use the helper function from the ViewModel to format the text
                // e.g., "vacation" becomes "Vacation Leave"
                Text(
                    text = viewModel.formatLeaveType(request.leaveType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = request.status.replaceFirstChar { it.uppercase() },
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${viewModel.formatDisplayDate(request.startDate)} to ${viewModel.formatDisplayDate(request.endDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = request.reason,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Filed on: ${viewModel.formatDisplayDate(request.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}