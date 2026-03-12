package com.example.autopayroll_mobile.composableUI.announcementUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.autopayroll_mobile.composableUI.TutorialOverlay
import com.example.autopayroll_mobile.composableUI.tutorialTarget
import com.example.autopayroll_mobile.utils.TutorialManager
import com.example.autopayroll_mobile.utils.TutorialStep
import com.example.autopayroll_mobile.viewmodel.AnnouncementUiItem
import com.example.autopayroll_mobile.viewmodel.AnnouncementViewModel
import java.util.Date // ADDED THIS IMPORT

// --- DESIGN TOKENS ---
private val WebBackground = Color(0xFFF8F9FA)
private val WebSurface = Color.White
private val WebBorderColor = Color(0xFFE2E8F0)
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF64748B)
private val IconBg = Color(0xFFF1F5F9)
private val AccentYellow = Color(0xFFFFC107)

@Composable
fun AnnouncementScreen(
    viewModel: AnnouncementViewModel,
    onAnnouncementClicked: (String) -> Unit
) {
    val realAnnouncements by viewModel.announcements.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- TUTORIAL STATE ---
    val isTutorialActive by TutorialManager.isTutorialActive.collectAsState()
    val currentStep by TutorialManager.currentStep.collectAsState()

    var tabsTargetRect by remember { mutableStateOf<Rect?>(null) }
    var cardTargetRect by remember { mutableStateOf<Rect?>(null) }

    // Auto-advance when user lands on this screen from the QR tutorial
    LaunchedEffect(currentStep) {
        if (isTutorialActive && currentStep == TutorialStep.NAVIGATE_TO_ANNOUNCEMENT) {
            TutorialManager.nextStep(TutorialStep.ANNOUNCEMENT_OVERVIEW)
        }
    }

    // MOCK DATA OVERRIDE
    val activeAnnouncements = if (isTutorialActive) {
        listOf(
            AnnouncementUiItem(
                id = "mock1",
                title = "System Maintenance",
                rawDate = Date(), // FIXED: Passes a Date object instead of a String
                displayDate = "Today",
                category = "Admin",
                message = "The payroll system will undergo maintenance tonight at 12:00 AM.",
                icon = Icons.Default.Notifications
            ),
            AnnouncementUiItem(
                id = "mock2",
                title = "Bonus Released",
                rawDate = Date(), // FIXED: Passes a Date object instead of a String
                displayDate = "Yesterday",
                category = "Payroll",
                message = "Quarterly bonuses have been successfully disbursed.",
                icon = Icons.Default.Notifications
            )
        )
    } else {
        realAnnouncements
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshAnnouncements()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val categories = listOf("All", "Payroll", "Admin", "Memo")
    var selectedCategory by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) { viewModel.refreshAnnouncements() }

    Box(modifier = Modifier.fillMaxSize().background(WebBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Announcements", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextHeader)
                IconButton(onClick = { if (!isTutorialActive) viewModel.refreshAnnouncements() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = TextBody)
                }
            }

            // --- HIGHLIGHT 1: TABS ---
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color.Transparent,
                contentColor = TextBody,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory)]),
                        height = 3.dp, color = AccentYellow
                    )
                },
                divider = { HorizontalDivider(color = WebBorderColor, thickness = 1.dp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .tutorialTarget(isActive = currentStep == TutorialStep.ANNOUNCEMENT_TABS) { tabsTargetRect = it }
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { if (!isTutorialActive) selectedCategory = category },
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

            val filteredList = remember(selectedCategory, activeAnnouncements) {
                if (selectedCategory == "All") activeAnnouncements
                else activeAnnouncements.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            }

            if (isLoading && !isTutorialActive) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = TextHeader) }
            } else if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "No updates available.", color = TextHeader, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Pull to refresh or check back later.", color = TextBody, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        val isFirst = item == filteredList.first()
                        WebAnnouncementCard(
                            item = item,
                            onClick = { if (!isTutorialActive) onAnnouncementClicked(item.id) },
                            // --- HIGHLIGHT 2: THE FIRST CARD ---
                            modifier = if (isFirst) Modifier.tutorialTarget(isActive = currentStep == TutorialStep.ANNOUNCEMENT_CARD) { cardTargetRect = it } else Modifier
                        )
                    }
                }
            }
        }

        // --- TUTORIAL OVERLAYS ---
        if (isTutorialActive) {
            when (currentStep) {
                TutorialStep.ANNOUNCEMENT_OVERVIEW -> {
                    TutorialOverlay(
                        title = "Announcements",
                        description = "Welcome to the Announcements board! This is where you will receive important company updates and memos.",
                        onNext = { TutorialManager.nextStep(TutorialStep.ANNOUNCEMENT_TABS) }
                    )
                }
                TutorialStep.ANNOUNCEMENT_TABS -> {
                    TutorialOverlay(
                        title = "Filter by Category",
                        description = "You can use these tabs to quickly filter the announcements to show only Payroll updates or Admin memos.",
                        targetRect = tabsTargetRect,
                        onNext = { TutorialManager.nextStep(TutorialStep.ANNOUNCEMENT_CARD) },
                        onBack = { TutorialManager.nextStep(TutorialStep.ANNOUNCEMENT_OVERVIEW) }
                    )
                }
                TutorialStep.ANNOUNCEMENT_CARD -> {
                    TutorialOverlay(
                        title = "Read Updates",
                        description = "Each card gives you a quick preview. You can tap on any card to read the full, detailed announcement.",
                        targetRect = cardTargetRect,
                        customYOffset = 30.dp,
                        onNext = { TutorialManager.nextStep(TutorialStep.NAVIGATE_TO_MENU) },
                        onBack = { TutorialManager.nextStep(TutorialStep.ANNOUNCEMENT_TABS) }
                    )
                }
                TutorialStep.NAVIGATE_TO_MENU -> {
                    TutorialOverlay(
                        title = "Explore the Menu",
                        description = "Finally, let's look at how to file requests. Tap the 'Menu' icon on the far right of the bottom navigation bar.",
                        targetRect = null,
                        showNextButton = false,
                        pointerBias = 1.0f, // Bouncing arrow points perfectly to the far right Menu icon!
                        onNext = {},
                        onBack = { TutorialManager.nextStep(TutorialStep.ANNOUNCEMENT_CARD) }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun WebAnnouncementCard(
    item: AnnouncementUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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

                val displayCategory = when(item.category.uppercase()) {
                    "ADMIN" -> "ADMIN UPDATE"
                    "MEMO" -> "GENERAL MEMO"
                    "PAYROLL" -> "PAYROLL UPDATE"
                    else -> item.category.uppercase()
                }

                val categoryColor = when(item.category.uppercase()) {
                    "PAYROLL" -> Color(0xFF166534)
                    "ADMIN" -> Color(0xFFB45309)
                    else -> TextBody
                }

                Text(
                    text = displayCategory,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor,
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