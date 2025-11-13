package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

// This represents one row in the tracking list
data class AdjustmentRequest(
    val id: Int,
    @SerializedName("request_date")
    val date: String,
    @SerializedName("adjustment_type")
    val type: String,
    val status: String
)

// This is the wrapper for the API response
data class AdjustmentRequestListResponse(
    val data: List<AdjustmentRequest>
)