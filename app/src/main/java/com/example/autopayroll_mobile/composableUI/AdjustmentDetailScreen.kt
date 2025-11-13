package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.data.model.AdjustmentModuleUiState
import com.example.autopayroll_mobile.data.model.AdjustmentRequestDetail
import com.example.autopayroll_mobile.ui.theme.TextPrimary
import com.example.autopayroll_mobile.ui.theme.TextSecondary
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel

/**
 * Replaces `AdjustmentDetailsFragment`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentDetailScreen(
    uiState: AdjustmentModuleUiState,
    requestId: Int,
    viewModel: AdjustmentModuleViewModel,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // This 'LaunchedEffect' will run once when the screen is first
    // composed or if the 'requestId' changes.
    LaunchedEffect(key1 = requestId) {
        viewModel.fetchAdjustmentRequestDetail(requestId)
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
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // --- Handle Loading and Error States ---
            if (uiState.isLoadingDetail) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.detailError != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Error: ${uiState.detailError}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (uiState.selectedRequestDetail != null) {
                // --- Show Details on Success ---
                val details = uiState.selectedRequestDetail
                RequestDetails(details = details)
            }
        }
    }
}

/**
 * The main content of the detail screen, showing all data.
 */
@Composable
private fun RequestDetails(details: AdjustmentRequestDetail) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Admin Review Section ---
        // This part matches your Figma mockup
        DetailRow(label = "Reviewed by:", value = details.reviewedBy ?: "N/A")
        DetailRow(label = "Date Reviewed:", value = details.dateReviewed ?: "N/A")

        // Use the StatusChip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Status:",
                fontSize = 16.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(120.dp)
            )
            StatusChip(status = details.status)
        }

        DetailRow(
            label = "Remarks:",
            value = details.remarks ?: "No remarks provided.",
            isVertical = true
        )

        Divider(Modifier.padding(vertical = 16.dp))

        // --- Original Request Section ---
        Text(
            "Submitted Request Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        DetailRow(label = "Date Submitted:", value = details.requestDate)
        DetailRow(label = "Main Type:", value = details.adjustmentType)
        DetailRow(label = "Sub Type:", value = details.subType)
        DetailRow(label = "Affected Dates:", value = "${details.startDate} to ${details.endDate}")
        DetailRow(label = "Hours Requested:", value = details.hours ?: "N/A")
        DetailRow(
            label = "Reason:",
            value = details.reason,
            isVertical = true
        )
        DetailRow(
            label = "Attachment:",
            value = details.attachmentUrl ?: "None",
            isVertical = true
        )
    }
}

/**
 * A reusable row for displaying a label and its value.
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    isVertical: Boolean = false
) {
    if (isVertical) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                color = TextPrimary,
                lineHeight = 24.sp
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(120.dp) // Aligns the values
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = TextPrimary
            )
        }
    }
}