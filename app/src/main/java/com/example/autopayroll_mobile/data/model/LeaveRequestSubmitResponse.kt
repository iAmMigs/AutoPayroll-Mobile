package com.example.autopayroll_mobile.data.model

data class LeaveRequestSubmitResponse(
    val success: Boolean,
    val message: String? = null,

    val leave: LeaveRequest? = null,

    val data: LeaveRequest? = null,

    val errors: Map<String, List<String>>? = null
)