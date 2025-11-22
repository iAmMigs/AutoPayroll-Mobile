package com.example.autopayroll_mobile.data.model

import com.example.autopayroll_mobile.data.model.LeaveRequest

data class LeaveRequestSubmitResponse(
    val success: Boolean,
    val message: String? = null,
    val data: LeaveRequest? = null, // Assuming you might return the created LeaveRequest
    val errors: Map<String, List<String>>? = null // ## NEW: For validation errors ##
)