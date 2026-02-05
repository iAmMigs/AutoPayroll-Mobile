package com.example.autopayroll_mobile.composableUI.leaveModuleUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.composableUI.StatusChip
import com.example.autopayroll_mobile.data.model.LeaveRequest
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- COLORS ---
private val TimelineLineColor = Color(0xFFE0E0E0)
private val MonthHeaderBg = Color(0xFFF5F5F5)
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveModuleCalendarScreen(
    viewModel: LeaveModuleViewModel,
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // 1. Filter & Sort Requests (Newest First)
    // We only care about requests that have a valid date
    val sortedRequests = remember(uiState.allRequests) {
        uiState.allRequests
            .filter { it.startDate.isNotBlank() }
            .sortedByDescending { it.startDate } // Sort by date descending
    }

    // 2. Group by Month (e.g., "September 2025")
    val groupedRequests = remember(sortedRequests) {
        sortedRequests.groupBy { request ->
            try {
                // Parse "YYYY-MM-DD" -> "MMMM yyyy"
                val date = LocalDate.parse(request.startDate)
                date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
            } catch (e: Exception) {
                "Unknown Date"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Calendar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextHeader
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (sortedRequests.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No leave history found", color = Color.Gray)
                }
            }
        } else {
            // Timeline List
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                groupedRequests.forEach { (monthHeader, requestsInMonth) ->
                    // A. Sticky Header for the Month
                    item {
                        MonthSectionHeader(monthHeader)
                    }

                    // B. Items for that month
                    items(requestsInMonth) { request ->
                        TimelineItem(request, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MonthHeaderBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextBody,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun TimelineItem(request: LeaveRequest, viewModel: LeaveModuleViewModel) {
    // Helper to format the day number (e.g., "24") and day name (e.g., "Mon")
    val (dayNumber, dayName) = remember(request.startDate) {
        try {
            val date = LocalDate.parse(request.startDate)
            val dNum = date.format(DateTimeFormatter.ofPattern("dd"))
            val dName = date.format(DateTimeFormatter.ofPattern("EEE"))
            dNum to dName
        } catch (e: Exception) {
            "??" to "---"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Date Column (Left)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = dayName.uppercase(),
                fontSize = 12.sp,
                color = TextBody,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dayNumber,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextHeader
            )
        }

        Spacer(Modifier.width(12.dp))

        // 2. Vertical Line & Content
        Box(modifier = Modifier.weight(1f)) {
            // Vertical Line (Decoration)
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(TimelineLineColor)
                    .align(Alignment.CenterStart)
            )

            // Card Content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp), // Offset from the line
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Title
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.formatLeaveType(request.leaveType),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextHeader
                            )
                            Spacer(Modifier.height(4.dp))
                            // Date Range logic
                            val dateText = if (request.startDate == request.endDate) {
                                "Single Day"
                            } else {
                                "Until ${viewModel.formatDisplayDate(request.endDate)}"
                            }
                            Text(text = dateText, fontSize = 12.sp, color = TextBody)
                        }

                        // Status Chip
                        StatusChip(status = request.status)
                    }

                    if (request.reason.isNotBlank()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = request.reason,
                            fontSize = 13.sp,
                            color = TextBody,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}