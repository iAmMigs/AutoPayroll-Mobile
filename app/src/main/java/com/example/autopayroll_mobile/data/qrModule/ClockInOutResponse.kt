package com.example.autopayroll_mobile.data.qrModule

import com.google.gson.annotations.SerializedName

data class ClockInOutResponse(
    val message: String,
    // Captures the 'data' field returned by the PHP controller
    @SerializedName("data") val data: AttendanceLog? = null
)