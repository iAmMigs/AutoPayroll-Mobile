package com.example.autopayroll_mobile.data.model

data class Payslip(
    val dateRange: String,       // Display text: "1-15 February"
    val periodIdentifier: String,// API param: "1-15" or "16-30"
    val originalPayDate: String, // Raw date: "2026-02-15"
    val netAmount: String,
    val status: String,
    val month: String,           // Display: "February"
    val year: Int                // Display: 2026
)