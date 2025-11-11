package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the raw announcement data from the API,
 * matching the database table structure.
 */
data class Announcement(
    @SerializedName("announcement_id")
    val announcementId: String,

    @SerializedName("admin_id")
    val adminId: String,

    val title: String,
    val message: String,

    @SerializedName("start_date")
    val startDate: String,

    @SerializedName("end_date")
    val endDate: String,

    @SerializedName("created_by")
    val createdBy: String,

    @SerializedName("is_active")
    val isActive: Int,

    @SerializedName("created_at")
    val createdAt: String
)

/**
 * A wrapper class for the API response
 * that holds the list of announcements.
 */
data class AnnouncementResponse(
    val data: List<Announcement>
)