package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define the status colors
val StatusPending = Color(0xFFFFA000) // Orange
val StatusApproved = Color(0xFF388E3C) // Green
val StatusRejected = Color(0xFFD32F2F) // Red

/**
 * A reusable Composable that displays a status chip based on the
 * text from the API.
 */
@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "pending" -> StatusPending to Color.White
        "approved" -> StatusApproved to Color.White
        "rejected" -> StatusRejected to Color.White
        else -> Color.Gray to Color.White
    }

    Text(
        text = status,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}