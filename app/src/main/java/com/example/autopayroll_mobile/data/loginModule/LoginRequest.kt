package com.example.autopayroll_mobile.data.loginModule

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val identifier: String,
    val password: String,

    @SerializedName("android_id")
    val androidId: String,

    @SerializedName("fcm_token")
    val fcmToken: String
)