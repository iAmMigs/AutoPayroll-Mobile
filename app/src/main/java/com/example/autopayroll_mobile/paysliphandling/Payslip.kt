// Make sure this package name matches your other files
package com.example.autopayroll_mobile

// Enum to represent the status in a safe way
enum class PayslipStatus {
    PROCESSING,
    COMPLETED
}

// Data class to hold the information for one payslip
data class Payslip(
    val dateRange: String,
    val netAmount: Double,
    val status: PayslipStatus
)