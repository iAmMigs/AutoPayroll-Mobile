package com.example.autopayroll_mobile.creditAdjustment // Use your package name

// Data class for one item in the credit adjustment list.
// It reuses the CreditStatus enum.
data class AdjustmentRequestItem(
    val date: String,
    val type: String, // Changed from 'id' to 'type'
    val status: CreditStatus
)