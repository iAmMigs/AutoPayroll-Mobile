package com.example.autopayroll_mobile.composableUI.announcementUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp // Added for custom header typography
import androidx.navigation.NavController
import com.example.autopayroll_mobile.viewmodel.AnnouncementViewModel
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.clickable // Added for back button clickable area

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementDetailScreen(
    navController: NavController,
    announcementId: String,
    viewModel: AnnouncementViewModel
) {
    val isLoading by viewModel.isLoading.collectAsState()

    val item = if (!isLoading) {
        viewModel.getAnnouncementById(announcementId)
    } else {
        null
    }

    // Replace Scaffold with custom Box/Column structure for uniformity
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars) // Handle status bar padding
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- Custom Header Area (Uniform Style) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.popBackStack() } // Make the whole area clickable for back
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Title uses uniform typography
                Text(
                    "Announcement Details",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // --- End Custom Header Area ---

            // Use Box to center loading/error states within the remaining space
            Box(
                modifier = Modifier
                    .weight(1f) // Take up remaining vertical space
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (item != null) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.displayDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Text(
                            text = item.message,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(32.dp)) // Add bottom padding
                    }
                } else {
                    Text(
                        text = "Announcement not found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}