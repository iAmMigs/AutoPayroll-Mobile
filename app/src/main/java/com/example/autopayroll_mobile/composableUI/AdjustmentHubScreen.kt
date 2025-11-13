package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.data.model.AdjustmentModuleUiState
import com.example.autopayroll_mobile.composableUI.StatusChip
import com.example.autopayroll_mobile.ui.theme.CardSurface
import com.example.autopayroll_mobile.ui.theme.TextPrimary
import com.example.autopayroll_mobile.ui.theme.TextSecondary

/**
 * Replaces the old `PayrollCreditFragment`
 * This is the main "hub" screen for the adjustment module.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentHubScreen(
    uiState: AdjustmentModuleUiState,
    onNavigateToFiling: () -> Unit,
    onNavigateToTracking: () -> Unit,
    onBack: () -> Unit
) {
    // --- Derived State ---
    // These values are dynamically calculated from the uiState
    val pendingCount by remember(uiState.adjustmentRequests) {
        derivedStateOf {
            uiState.adjustmentRequests.count { it.status.lowercase() == "pending" }
        }
    }

    val latestAdjustment by remember(uiState.adjustmentRequests) {
        derivedStateOf {
            uiState.adjustmentRequests.maxByOrNull { it.date } // Simple sort by date string
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll Credit Adjustment") },
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
        ) {
            // --- Info Card ---
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Easily request and track payroll credit adjustment in just a few steps.",
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- Navigation Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HubNavigationButton(
                    text = "Request Filing",
                    iconResId = R.drawable.ic_file_leave, // You need to provide this drawable
                    onClick = onNavigateToFiling,
                    modifier = Modifier.weight(1f)
                )
                HubNavigationButton(
                    text = "Track Request",
                    iconResId = R.drawable.ic_track_request, // You need to provide this drawable
                    onClick = onNavigateToTracking,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            // --- Status Summary ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .padding(16.dp)
            ) {
                // --- Pending Requests ---
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

                Divider(Modifier.padding(vertical = 16.dp))

                // --- Latest Adjustment ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Latest Adjustment", color = TextSecondary)
                    if (latestAdjustment != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            StatusChip(status = latestAdjustment!!.status)
                            Text(
                                text = "(${latestAdjustment!!.date})",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        Text("No requests found", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun HubNavigationButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f) // Makes it a square
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                modifier = Modifier.size(48.dp),
                tint = TextPrimary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = text,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}