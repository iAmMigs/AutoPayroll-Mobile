package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class Schedule(
    @SerializedName("id") val id: Int,
    @SerializedName("employee_id") val employeeId: String,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("break_start") val breakStart: String?,
    @SerializedName("break_end") val breakEnd: String?
)