package com.example.autopayroll_mobile.data.qrModule

import com.google.gson.annotations.SerializedName

data class ClockInOutRequest(
    @SerializedName("company_id") val companyId: String,
    val token: String,
    val signature: String,
    val latitude: Double,
    val longitude: Double,
    // Made nullable: Required for Clock In, Null for Clock Out
    @SerializedName("android_id") val androidId: String? = null
)