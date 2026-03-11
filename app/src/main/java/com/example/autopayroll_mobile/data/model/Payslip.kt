package com.example.autopayroll_mobile.data.model

data class Payslip(
    val dateRange: String,
    val referenceId: String,
    val originalPayDate: String,
    val netAmount: String,
    val status: String,

    val downloadPeriod: String,
    val downloadYear: Int,
    val downloadMonth: Int,
    val year: Int
)