package com.example.autopayroll_mobile.composableUI.adjustmentModuleUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autopayroll_mobile.composableUI.StatusChip
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentModuleUiState
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentRequest
import com.example.autopayroll_mobile.ui.theme.CardSurface
import com.example.autopayroll_mobile.ui.theme.TextPrimary
import com.example.autopayroll_mobile.ui.theme.TextSecondary
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentTrackScreen(
    uiState: AdjustmentModuleUiState,
    viewModel: AdjustmentModuleViewModel,
    onBack: () -> Unit,
    onSelectRequest: (String) -> Unit // ## UPDATED ##: ID is now a String
) {

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
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            StatusFilterDropdown(
                selectedStatus = uiState.filterStatus,
                onStatusSelected = { newStatus ->
                    viewModel.onFilterChanged(newStatus)
                }
            )

            Spacer(Modifier.height(16.dp))
            ListHeader()

            // ## UPDATED ##: Use the main 'isLoading' and 'pageError'
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.pageError != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.pageError}", color = Color.Red)
                }
            } else {
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

@Composable
private fun StatusFilterDropdown(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("All", "Pending", "Approved", "Rejected") // You can expand this

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

// ## UPDATED ##: Formats the date string
@Composable
private fun AdjustmentRequestItem(
    request: AdjustmentRequest,
    onItemClick: () -> Unit
) {
    // Formatter for the date
    val displayDate = remember {
        try {
            val odt = OffsetDateTime.parse(request.dateSubmitted)
            odt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US))
        } catch (e: Exception) {
            request.dateSubmitted // Fallback
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayDate,
            modifier = Modifier.weight(1.2f),
            color = TextSecondary
        )
        Text(
            text = request.type.replaceFirstChar { it.uppercase() },
            modifier = Modifier.weight(1f),
            color = TextSecondary
        )
        Box(
            modifier = Modifier.weight(1f)
        ) {
            StatusChip(status = request.status)
        }
    }
}