package com.example.autopayroll_mobile.data.auth

import com.google.gson.annotations.SerializedName

data class OtpRequest(
    val email: String
)

data class OtpVerifyRequest(
    val email: String,
    val otp: String
)

data class OtpResponse(
    val success: Boolean,
    val message: String
)

data class VerifyOtpResponse(
    val success: Boolean,
    val message: String
)

data class OtpErrorResponse(
    val otp: String? = null,
    val message: String? = null
)