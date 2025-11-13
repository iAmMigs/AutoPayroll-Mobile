package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autopayroll_mobile.data.model.AdjustmentModuleUiState
import com.example.autopayroll_mobile.data.model.AdjustmentRequest
import com.example.autopayroll_mobile.ui.theme.CardSurface
import com.example.autopayroll_mobile.ui.theme.TextPrimary
import com.example.autopayroll_mobile.ui.theme.TextSecondary
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel

/**
 * Replaces `TrackAdjustmentFragment` and `TrackAdjustmentAdapter`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentTrackScreen(
    uiState: AdjustmentModuleUiState,
    viewModel: AdjustmentModuleViewModel, // We need this to call onFilterChanged
    onBack: () -> Unit,
    onSelectRequest: (Int) -> Unit
) {
    // --- State ---
    // This derived state will automatically recalculate when the filter or list changes
    val filteredList by remember(uiState.adjustmentRequests, uiState.filterStatus) {
        derivedStateOf {
            if (uiState.filterStatus == "All") {
                uiState.adjustmentRequests
            } else {
                uiState.adjustmentRequests.filter { it.status.equals(uiState.filterStatus, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Adjustment Request") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            // --- Filter Button ---
            StatusFilterDropdown(
                selectedStatus = uiState.filterStatus,
                onStatusSelected = { newStatus ->
                    viewModel.onFilterChanged(newStatus)
                }
            )

            Spacer(Modifier.height(16.dp))

            // --- List Header ---
            ListHeader()

            // --- Loading and Error ---
            if (uiState.isLoadingRequests) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.requestsError != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.requestsError}", color = Color.Red)
                }
            } else {
                // --- The List ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { request ->
                        AdjustmentRequestItem(
                            request = request,
                            onItemClick = { onSelectRequest(request.id) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

/**
 * The dropdown menu for filtering by status
 */
@Composable
private fun StatusFilterDropdown(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("All", "Pending", "Approved", "Rejected")

    Box {
        OutlinedButton(
            onClick = { isExpanded = true }
        ) {
            Text(selectedStatus)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Filter Status")
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            filterOptions.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusSelected(status)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

/**
 * The "Date", "Type", "Status" header
 */
@Composable
private fun ListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Date", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Type", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Status", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

/**
 * A single row in the list
 */
@Composable
private fun AdjustmentRequestItem(
    request: AdjustmentRequest,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = request.date,
            modifier = Modifier.weight(1.2f),
            color = TextSecondary
        )
        Text(
            text = request.type,
            modifier = Modifier.weight(1f),
            color = TextSecondary
        )
        Box(
            modifier = Modifier.weight(1f)
        ) {
            // Use the reusable StatusChip we created earlier
            StatusChip(status = request.status)
        }
    }
}