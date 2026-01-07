package com.example.autopayroll_mobile.composableUI.announcementUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.viewmodel.AnnouncementUiItem
import com.example.autopayroll_mobile.viewmodel.AnnouncementViewModel

// --- DESIGN TOKENS ---
private val WebBackground = Color(0xFFF8F9FA)
private val WebSurface = Color.White
private val WebBorderColor = Color(0xFFE2E8F0)
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF64748B)
private val IconBg = Color(0xFFF1F5F9)
private val AccentYellow = Color(0xFFFFC107) // Matches the yellow underline in image

@Composable
fun AnnouncementScreen(
    viewModel: AnnouncementViewModel,
    onAnnouncementClicked: (String) -> Unit
) {
    val announcements by viewModel.announcements.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State for Tabs
    val categories = listOf("All", "Payroll", "Admin", "Memo")
    var selectedCategory by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        viewModel.refreshAnnouncements()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // --- HEADER ---
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Announcements",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextHeader
            )

            // --- TABS (Added Back) ---
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color.Transparent,
                contentColor = TextBody,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory)]),
                        height = 3.dp,
                        color = AccentYellow
                    )
                },
                divider = {
                    HorizontalDivider(color = WebBorderColor, thickness = 1.dp)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                text = category,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedCategory == category) TextHeader else TextBody
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- FILTER LOGIC ---
            val filteredList = remember(selectedCategory, announcements) {
                if (selectedCategory == "All") {
                    announcements
                } else {
                    announcements.filter {
                        // Case-insensitive check (e.g., "ADMIN" matches "Admin")
                        it.category.equals(selectedCategory, ignoreCase = true)
                    }
                }
            }

            // --- LIST CONTENT ---
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TextHeader)
                }
            } else if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No available announcements.",
                        color = TextBody,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        WebAnnouncementCard(
                            item = item,
                            onClick = { onAnnouncementClicked(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WebAnnouncementCard(
    item: AnnouncementUiItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WebSurface),
        border = BorderStroke(1.dp, WebBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(IconBg, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = TextHeader,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHeader,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.displayDate,
                        fontSize = 12.sp,
                        color = TextBody,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Dynamic Category Label
                // Displays "ADMIN UPDATE" if category is Admin, etc.
                val displayCategory = when(item.category.uppercase()) {
                    "ADMIN" -> "ADMIN UPDATE"
                    "MEMO" -> "GENERAL MEMO"
                    "PAYROLL" -> "PAYROLL"
                    else -> item.category.uppercase()
                }

                Text(
                    text = displayCategory,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBody,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    fontSize = 14.sp,
                    color = TextBody,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }
        }
    }
}