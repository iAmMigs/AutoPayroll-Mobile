package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class AbsencesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val count: Int // Mapped from "data"
)