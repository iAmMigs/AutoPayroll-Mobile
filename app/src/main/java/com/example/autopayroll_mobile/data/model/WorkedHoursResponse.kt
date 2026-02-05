package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class WorkedHoursResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("total") val total: Double,
    @SerializedName("overtime") val overtime: Double, // Added to match PHP
    @SerializedName("late") val late: Int             // Added to match PHP (minutes)
)