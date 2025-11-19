package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class LeaveCreditsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val creditDays: Int // Mapped from "data"
)