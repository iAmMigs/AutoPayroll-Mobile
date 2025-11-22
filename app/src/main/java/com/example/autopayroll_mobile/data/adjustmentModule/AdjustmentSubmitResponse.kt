package com.example.autopayroll_mobile.data.AdjustmentModule

/**
 * This wrapper class for the API response is UPDATED
 * to match your new JSON structure.
 */
data class AdjustmentSubmitResponse(
    val success: Boolean,
    val message: String? = null,
    val data: AdjustmentRequest? = null,
    val errors: Map<String, List<String>>? = null // For validation errors
)