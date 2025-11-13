package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

// This represents the full detail of a single request
data class AdjustmentRequestDetail(
    @SerializedName("reviewed_by")
    val reviewedBy: String?,
    @SerializedName("date_reviewed")
    val dateReviewed: String?,
    val status: String,
    val remarks: String?,

    // It's good practice to also show what was submitted
    @SerializedName("request_date")
    val requestDate: String,
    @SerializedName("adjustment_type")
    val adjustmentType: String,
    @SerializedName("sub_type")
    val subType: String,
    val reason: String,
    @SerializedName("affected_start_date")
    val startDate: String,
    @SerializedName("affected_end_date")
    val endDate: String,
    val hours: String?,
    @SerializedName("attachment_url")
    val attachmentUrl: String?
)

// This is the wrapper for the API response
data class AdjustmentRequestDetailResponse(
    val data: AdjustmentRequestDetail
)