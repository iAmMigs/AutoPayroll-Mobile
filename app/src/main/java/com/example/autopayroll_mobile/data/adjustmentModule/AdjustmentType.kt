package com.example.autopayroll_mobile.data.AdjustmentModule

import com.google.gson.annotations.SerializedName

/**
 * This represents a single item in the "Sub Type" dropdown.
 * This version matches your new controller.
 */
data class AdjustmentType(
    @SerializedName("adjustment_type_id")
    val id: String,

    @SerializedName("label")
    val name: String,

    val code: String,
    val description: String
)

/**
 * This wrapper class for the API response is UPDATED
 * to match your new JSON structure.
 */
data class AdjustmentTypesResponse(
    val success: Boolean,
    val data: List<AdjustmentType>,
    val message: String? = null // <-- THIS IS THE FIX
)