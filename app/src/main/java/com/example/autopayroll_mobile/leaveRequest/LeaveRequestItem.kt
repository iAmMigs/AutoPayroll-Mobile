package com.example.autopayroll_mobile.leaveRequest // Use your package name

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// An enum to represent the possible leave statuses
enum class LeaveStatus {
    Pending,
    Revision,
    Rejected,
    Approved
}

// Add the @Parcelize annotation and implement Parcelable
@Parcelize
data class LeaveRequestItem(
    val date: String,
    val id: String,
    val status: LeaveStatus,
    // Add the new fields for the details screen
    val leaveType: String,
    val leaveDuration: String,
    val remarks: String
) : Parcelable