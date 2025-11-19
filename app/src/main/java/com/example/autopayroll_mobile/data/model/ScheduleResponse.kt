package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class ScheduleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("schedule") val schedule: Schedule?
)