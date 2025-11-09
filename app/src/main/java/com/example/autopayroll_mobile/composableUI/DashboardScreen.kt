package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.viewmodel.DashboardUiState // ## Import new state class ##
import com.example.autopayroll_mobile.viewmodel.DashboardViewModel // ## Import new ViewModel ##

// These colors are placeholders. You should replace them
// with your actual colors from colors.xml (e.g., colorResource(id = R.color.text_primary))
val TextPrimary = Color(0xFF3C3C3C)
val CardSurface = Color.White
val AppBackground = Color(0xFFEEEEEE)

@Composable
fun DashboardScreen(
    // Import the ViewModel from its new package
    viewModel: DashboardViewModel = viewModel()
) {
    // Get the UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // This Column replaces your ScrollView and outer LinearLayout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        ProfileHeader(state = uiState)
        Spacer(modifier = Modifier.height(16.dp))
        AttendanceSummaryCard()
        Spacer(modifier = Modifier.height(16.dp))
        PreviewCards()
        Spacer(modifier = Modifier.height(16.dp))
        TransactionsCard()
    }
}

// This Composable replaces your top profile CardView
@Composable
fun ProfileHeader(state: DashboardUiState) {
    Card(
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Image(
                painter = painterResource(id = R.drawable.profiledefault),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )

            // Employee Info
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                // These Text fields get their data directly from the state
                Text(
                    text = state.employeeName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = state.employeeId,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = state.jobAndCompany,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// This Composable replaces the attendance summary CardView
@Composable
fun AttendanceSummaryCard() {
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(title = "Regular", value = "0", modifier = Modifier.weight(1f))
                StatItem(title = "Overtime", value = "0", modifier = Modifier.weight(1f))
                StatItem(title = "Late", value = "0", modifier = Modifier.weight(1f))
            }
            Text(
                text = "Accumulated hours over last attendance check",
                fontSize = 12.sp,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// This is a helper Composable for the items above
@Composable
fun StatItem(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 14.sp, color = TextPrimary)
        Text(
            text = value,
            fontSize = 28.sp,
            color = Color(0xFF3C3C3C),
            fontWeight = FontWeight.Bold
        )
    }
}

// This Composable replaces the horizontal layout with two preview cards
@Composable
fun PreviewCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Replaces margins
    ) {
        PreviewCard(
            title = "Leave Balance Preview",
            value = "0",
            modifier = Modifier.weight(1f)
        )
        PreviewCard(
            title = "Absence Preview",
            value = "0",
            modifier = Modifier.weight(1f)
        )
    }
}

// Helper Composable for the preview cards
@Composable
fun PreviewCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp)
            Text(
                text = value,
                fontSize = 36.sp,
                color = Color(0xFF3C3C3C),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// This Composable replaces the final "Transactions" card
@Composable
fun TransactionsCard() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Transactions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3C3C3C),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Net Earning", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                    Text(text = "Pay Date", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = TextPrimary, modifier = Modifier.weight(1f))
                    Text(text = "Status", fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = TextPrimary, modifier = Modifier.weight(1f))
                }
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions to show.",
                        color = Color(0xFFA0A0A0),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// This Preview allows you to see your UI in Android Studio
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val fakeState = DashboardUiState(
        isLoading = false,
        employeeName = "Juan Dela Cruz",
        employeeId = "20250165",
        jobAndCompany = "Job • Company"
    )
    Column(
        modifier = Modifier
            .background(AppBackground)
            .padding(20.dp)
    ) {
        ProfileHeader(state = fakeState)
        Spacer(modifier = Modifier.height(16.dp))
        AttendanceSummaryCard()
        Spacer(modifier = Modifier.height(16.dp))
        PreviewCards()
        Spacer(modifier = Modifier.height(16.dp))
        TransactionsCard()
    }
}