package com.example.autopayroll_mobile.data.model

data class QrValidationResponse(
    // The server must return these three fields
    val latitude: Double,
    val longitude: Double,
    val radius: Float, // The allowed radius in meters
    val message: String // A success message like "QR Validated"
)