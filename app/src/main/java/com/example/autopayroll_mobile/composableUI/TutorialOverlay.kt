package com.example.autopayroll_mobile.composableUI

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.utils.TutorialManager

fun Modifier.tutorialTarget(
    isActive: Boolean,
    onPositioned: (Rect) -> Unit
): Modifier = this.onGloballyPositioned { coordinates ->
    if (isActive) {
        onPositioned(coordinates.boundsInRoot())
    }
}

@Composable
fun TutorialOverlay(
    title: String,
    description: String,
    targetRect: Rect? = null,
    customYOffset: Dp = 0.dp,
    showNextButton: Boolean = true,
    nextButtonText: String = "Next", // NEW: Allows custom button text
    pointerBias: Float? = null,
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (showExitDialog) {
            showExitDialog = false
            TutorialManager.endTutorial()
        } else {
            showExitDialog = true
        }
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height))
                if (targetRect != null) {
                    val padding = 16.dp.toPx()
                    addRoundRect(
                        RoundRect(
                            rect = targetRect.inflate(padding),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    )
                }
                fillType = PathFillType.EvenOdd
            }
            drawPath(path, Color.Black.copy(alpha = 0.75f))
        }

        val cardAlignment = remember(targetRect, screenHeightPx) {
            if (targetRect == null) Alignment.Center
            else if (targetRect.center.y > screenHeightPx / 2) Alignment.TopCenter
            else Alignment.BottomCenter
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 32.dp,
                    end = 32.dp,
                    top = if (cardAlignment == Alignment.TopCenter) 100.dp else 32.dp,
                    bottom = if (cardAlignment == Alignment.BottomCenter) 120.dp else 32.dp
                ),
            contentAlignment = cardAlignment
        ) {
            Card(
                modifier = Modifier.offset(y = customYOffset),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
                        IconButton(onClick = { showExitDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = description, fontSize = 15.sp, color = Color(0xFF64748B), lineHeight = 22.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (onBack != null) {
                            TextButton(onClick = onBack) {
                                Text("Back", color = Color.Gray)
                            }
                        }

                        if (showNextButton) {
                            Button(
                                onClick = onNext,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD147))
                            ) {
                                // USE CUSTOM TEXT HERE
                                Text(nextButtonText, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (pointerBias != null) {
            val infiniteTransition = rememberInfiniteTransition(label = "bounce")
            val yOffset by infiniteTransition.animateFloat(
                initialValue = -15f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "arrow_bounce"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                contentAlignment = BiasAlignment(horizontalBias = pointerBias, verticalBias = 1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Point Down",
                    tint = Color(0xFFFFD147),
                    modifier = Modifier
                        .offset(y = yOffset.dp)
                        .size(64.dp)
                )
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Tutorial?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to exit? You can restart this anytime.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        TutorialManager.endTutorial()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("Exit", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("Cancel") } }
        )
    }
}