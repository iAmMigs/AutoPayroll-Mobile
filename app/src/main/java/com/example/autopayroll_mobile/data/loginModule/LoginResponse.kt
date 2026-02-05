package com.example.autopayroll_mobile.data.loginModule

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    // PHP now returns these directly on success, so we make success/message nullable
    @SerializedName("success") val success: Boolean? = true,
    @SerializedName("message") val message: String? = null,

    @SerializedName("token", alternate = ["access_token"])
    val token: String,

    @SerializedName("employee_id") val employeeId: String,

    @SerializedName("fcm_token") val fcmToken: String? = null
)