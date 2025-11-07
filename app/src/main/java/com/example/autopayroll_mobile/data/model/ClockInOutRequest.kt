// In: data/model/ClockInOutRequest.kt
package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class ClockInOutRequest(
    @SerializedName("company_id") val companyId: String,
    val token: String,
    val signature: String,
    val latitude: Double,
    val longitude: Double
)