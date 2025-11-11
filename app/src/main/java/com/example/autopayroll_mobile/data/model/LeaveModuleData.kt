package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data for a single leave request,
 * matching your 'leave_request' database table.
 */
data class LeaveRequest(
    @SerializedName("leave_request_id") // ## FIX: Matches your table ##
    val id: String,

    @SerializedName("employee_id")
    val employeeId: String,

    @SerializedName("leave_type")
    val leaveType: String,

    @SerializedName("start_date")
    val startDate: String, // e.g., "2025-08-20"

    @SerializedName("end_date")
    val endDate: String,

    val reason: String,
    val status: String, // e.g., "pending", "approved", "declined"

    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Represents the API response when GETTING a list of requests.
 * Your API route is: /employee/show/leave-request
 */
data class LeaveRequestListResponse(
    val success: Boolean,
    val data: List<LeaveRequest> // Assuming server sends a 'data' key
)

/**
 * Represents the data we SEND to the server to create a new request.
 * Your API route is: /employee/leave-request
 */
data class LeaveRequestSubmit(
    @SerializedName("leave_type")
    val leaveType: String,

    @SerializedName("start_date")
    val startDate: String, // "YYYY-MM-DD"

    @SerializedName("end_date")
    val endDate: String, // "YYYY-MM-DD"

    val reason: String
)

/**
 * Represents the server's response after we submit a new request.
 */
data class LeaveRequestSubmitResponse(
    val success: Boolean,
    val message: String,
    val data: LeaveRequest // The newly created request
)

/**
 * A UI-specific model for the Leave Balance card.
 */
data class LeaveBalance(
    val available: Int = 0,
    val used: Int = 0
)