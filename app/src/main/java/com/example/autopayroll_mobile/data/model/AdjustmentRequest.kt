package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * This now represents the full CreditAdjustment model from your server.
 * We will use this for both the list and the detail screen.
 */
data class AdjustmentRequest(
    @SerializedName("adjustment_id")
    val id: String,

    @SerializedName("adjustment_type")
    val type: String,

    @SerializedName("subtype")
    val subType: String,

    val status: String,

    @SerializedName("reason")
    val reason: String?,

    @SerializedName("start_date")
    val startDate: String?,

    @SerializedName("end_date")
    val endDate: String?,

    @SerializedName("affected_date")
    val affectedDate: String?,

    @SerializedName("attachment_path")
    val attachmentUrl: String?,

    // These are for the detail screen
    @SerializedName("reviewed_by_id")
    val reviewedBy: String?,

    @SerializedName("remarks")
    val remarks: String?,

    @SerializedName("updated_at")
    val dateReviewed: String?,

    @SerializedName("created_at")
    val dateSubmitted: String
)

/**
 * This wrapper class for the API response is UPDATED
 * to match your new JSON structure.
 */
data class AdjustmentRequestListResponse(
    val success: Boolean,
    val data: List<AdjustmentRequest>
)
