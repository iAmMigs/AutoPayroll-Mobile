package com.example.autopayroll_mobile.composableUI.announcementUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.navigation.NavController
import com.example.autopayroll_mobile.viewmodel.AnnouncementViewModel

// Reuse local design tokens for consistency
private val WebBackground = Color(0xFFF8F9FA)
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF334155)
private val TextLabel = Color(0xFF64748B)

@Composable
fun AnnouncementDetailScreen(
    navController: NavController,
    announcementId: String,
    viewModel: AnnouncementViewModel
) {
    val isLoading by viewModel.isLoading.collectAsState()

    // Retrieve item
    val item = if (!isLoading) viewModel.getAnnouncementById(announcementId) else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- HEADER (Back Button) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
                    .padding(horizontal = 8.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextHeader
                    )
                }
                Text(
                    text = "Back to Announcements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextHeader,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }

            // --- CONTENT AREA ---
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TextHeader)
                }
            } else if (item != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Date Tag
                    Text(
                        text = item.displayDate.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLabel,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Big Title
                    Text(
                        text = item.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHeader,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Message Body
                    Text(
                        text = item.message,
                        fontSize = 16.sp,
                        color = TextBody,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Announcement not found.", color = TextLabel)
                }
            }
        }
    }
}