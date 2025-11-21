package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class LeaveCreditsResponse(
    val success: Boolean,

    // The PHP code sends the value in the "data" key:
    // 'data' => $credits->credit_days
    @SerializedName("data")
    val creditDays: Double, // We use Double to safely catch decimals like 1.5

    val message: String? = null
)