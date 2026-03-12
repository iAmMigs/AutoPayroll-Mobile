package com.example.autopayroll_mobile.composableUI.menuModule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.composableUI.TutorialOverlay
import com.example.autopayroll_mobile.composableUI.tutorialTarget
import com.example.autopayroll_mobile.utils.TutorialManager
import com.example.autopayroll_mobile.utils.TutorialStep

private val WebBackground = Color(0xFFF8F9FA)
private val WebSurface = Color.White
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF64748B)
private val DividerColor = Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToLeave: () -> Unit,
    onNavigateToAdjustment: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onStartTutorial: () -> Unit,
    onLogout: () -> Unit
) {
    var showTutorialPrompt by remember { mutableStateOf(false) }

    val isTutorialActive by TutorialManager.isTutorialActive.collectAsState()
    val currentStep by TutorialManager.currentStep.collectAsState()

    var profileRect by remember { mutableStateOf<Rect?>(null) }
    var leaveRect by remember { mutableStateOf<Rect?>(null) }
    var adjustmentRect by remember { mutableStateOf<Rect?>(null) }

    // --- AUTO ADVANCER: Detects when user returns from other modules ---
    LaunchedEffect(currentStep) {
        if (isTutorialActive) {
            when (currentStep) {
                TutorialStep.NAVIGATE_TO_MENU -> TutorialManager.nextStep(TutorialStep.MENU_OVERVIEW)
                TutorialStep.NAVIGATE_BACK_FROM_PROFILE -> TutorialManager.nextStep(TutorialStep.MENU_LEAVE_HIGHLIGHT)
                TutorialStep.NAVIGATE_BACK_FROM_LEAVE -> TutorialManager.nextStep(TutorialStep.MENU_ADJUSTMENT_HIGHLIGHT)
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.autopayrolltitle),
                            contentDescription = "Auto Payroll",
                            modifier = Modifier.height(40.dp).fillMaxWidth(),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WebSurface)
                )
            },
            containerColor = WebBackground
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WebSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {

                        // HIGHLIGHT 1: Profile
                        MenuItem(
                            label = "Profile",
                            iconRes = R.drawable.ic_personal_info,
                            modifier = Modifier.tutorialTarget(isActive = currentStep == TutorialStep.MENU_PROFILE_HIGHLIGHT || currentStep == TutorialStep.NAVIGATE_TO_PROFILE) { profileRect = it },
                            onClick = { if (!isTutorialActive || currentStep == TutorialStep.NAVIGATE_TO_PROFILE) onNavigateToProfile() }
                        )
                        MenuDivider()

                        // HIGHLIGHT 2: Leave Request
                        MenuItem(
                            label = "Leave Request",
                            iconRes = R.drawable.ic_leave_policy,
                            modifier = Modifier.tutorialTarget(isActive = currentStep == TutorialStep.MENU_LEAVE_HIGHLIGHT || currentStep == TutorialStep.NAVIGATE_TO_LEAVE) { leaveRect = it },
                            onClick = { if (!isTutorialActive || currentStep == TutorialStep.NAVIGATE_TO_LEAVE) onNavigateToLeave() }
                        )
                        MenuDivider()

                        // HIGHLIGHT 3: Payroll Credit Adjustment
                        MenuItem(
                            label = "Payroll Credit Adjustment",
                            iconRes = R.drawable.ic_payroll,
                            modifier = Modifier.tutorialTarget(isActive = currentStep == TutorialStep.MENU_ADJUSTMENT_HIGHLIGHT || currentStep == TutorialStep.NAVIGATE_TO_ADJUSTMENT) { adjustmentRect = it },
                            onClick = { if (!isTutorialActive || currentStep == TutorialStep.NAVIGATE_TO_ADJUSTMENT) onNavigateToAdjustment() }
                        )
                        MenuDivider()

                        MenuItem(label = "Reset Password", iconVector = Icons.Default.LockReset, onClick = { if (!isTutorialActive) onNavigateToChangePassword() })
                        MenuDivider()

                        MenuItem(label = "App Tutorial", iconVector = Icons.Default.School, onClick = { if (!isTutorialActive) showTutorialPrompt = true }, showChevron = false)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { if (!isTutorialActive) onLogout() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) { Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                Text("Version 1.0.0", modifier = Modifier.fillMaxWidth(), fontSize = 12.sp, color = Color.LightGray, textAlign = TextAlign.Center)
            }
        }

        // --- TUTORIAL OVERLAYS ---
        if (isTutorialActive) {
            when (currentStep) {
                TutorialStep.MENU_OVERVIEW -> {
                    TutorialOverlay(title = "App Menu", description = "Welcome to the Menu! This is your control center for managing your profile and formal requests.", onNext = { TutorialManager.nextStep(TutorialStep.MENU_PROFILE_HIGHLIGHT) })
                }
                TutorialStep.MENU_PROFILE_HIGHLIGHT -> {
                    TutorialOverlay(title = "Your Profile", description = "First, let's take a look at your personal records.", targetRect = profileRect, onNext = { TutorialManager.nextStep(TutorialStep.NAVIGATE_TO_PROFILE) }, onBack = { TutorialManager.nextStep(TutorialStep.MENU_OVERVIEW) })
                }
                TutorialStep.NAVIGATE_TO_PROFILE -> {
                    TutorialOverlay(title = "View Profile", description = "Tap 'Profile' to proceed.", targetRect = profileRect, showNextButton = false, onNext = {}, onBack = { TutorialManager.nextStep(TutorialStep.MENU_PROFILE_HIGHLIGHT) })
                }
                TutorialStep.MENU_LEAVE_HIGHLIGHT -> {
                    TutorialOverlay(title = "Leave Requests", description = "Here you can formally file for Vacation, Sick, or Emergency leaves, and track their approval status.", targetRect = leaveRect, onNext = { TutorialManager.nextStep(TutorialStep.NAVIGATE_TO_LEAVE) })
                }
                TutorialStep.NAVIGATE_TO_LEAVE -> {
                    TutorialOverlay(title = "Filing a Leave", description = "Tap 'Leave Request' to explore the Leave Module.", targetRect = leaveRect, showNextButton = false, onNext = {}, onBack = { TutorialManager.nextStep(TutorialStep.MENU_LEAVE_HIGHLIGHT) })
                }
                TutorialStep.MENU_ADJUSTMENT_HIGHLIGHT -> {
                    TutorialOverlay(title = "Payroll Adjustments", description = "If you notice missing punches or missing OT, file a formal credit adjustment right here.", targetRect = adjustmentRect, onNext = { TutorialManager.nextStep(TutorialStep.NAVIGATE_TO_ADJUSTMENT) })
                }
                TutorialStep.NAVIGATE_TO_ADJUSTMENT -> {
                    TutorialOverlay(title = "Fixing Credits", description = "Tap 'Payroll Credit Adjustment' to proceed to our final stop.", targetRect = adjustmentRect, showNextButton = false, onNext = {}, onBack = { TutorialManager.nextStep(TutorialStep.MENU_ADJUSTMENT_HIGHLIGHT) })
                }
                else -> {}
            }
        }
    }

    if (showTutorialPrompt) {
        AlertDialog(
            onDismissRequest = { showTutorialPrompt = false },
            title = { Text("App Tutorial", fontWeight = FontWeight.Bold, color = TextHeader) },
            text = { Text("Do you want to know how the app works?", color = TextBody, fontSize = 16.sp) },
            confirmButton = { Button(onClick = { showTutorialPrompt = false; onStartTutorial() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD147))) { Text("Yes", color = TextHeader, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showTutorialPrompt = false }) { Text("No, I already know how", color = TextBody) } },
            containerColor = WebSurface
        )
    }
}

@Composable
fun MenuItem(label: String, iconRes: Int? = null, iconVector: ImageVector? = null, onClick: () -> Unit, showChevron: Boolean = true, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
            if (iconRes != null) Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = TextHeader, modifier = Modifier.size(20.dp))
            else if (iconVector != null) Icon(imageVector = iconVector, contentDescription = null, tint = TextHeader, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextHeader, modifier = Modifier.weight(1f))
        if (showChevron) Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(24.dp))
    }
}
@Composable
fun MenuDivider() { HorizontalDivider(color = DividerColor, thickness = 1.dp, modifier = Modifier.padding(start = 76.dp)) }