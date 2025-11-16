package com.example.autopayroll_mobile.data.qrModule

data class QRScanData(
    val company_id: String,
    val token: String,
    val signature: String
)