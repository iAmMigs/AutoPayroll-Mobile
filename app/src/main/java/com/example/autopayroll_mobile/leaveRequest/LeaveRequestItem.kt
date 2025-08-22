package com.example.autopayroll_mobile.leaveRequest // Use your package name

// An enum to represent the possible leave statuses
enum class LeaveStatus {
    Pending,
    Revision,
    Rejected,
    Approved
}

// A data class to hold the data for one item in the list
data class LeaveRequestItem(
    val date: String,
    val id: String,
    val status: LeaveStatus
)