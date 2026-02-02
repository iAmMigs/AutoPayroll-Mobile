package com.example.autopayroll_mobile.data.model

data class Payslip(
    val dateRange: String,
    val netAmount: String,
    val status: String,
    val year: Int,
    val pdfUrl: String? // Added for PDF viewing
)