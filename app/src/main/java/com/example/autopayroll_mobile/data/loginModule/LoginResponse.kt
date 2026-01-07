package com.example.autopayroll_mobile.data.loginModule

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,

    // Check BOTH "token" and "access_token"
    @SerializedName("token", alternate = ["access_token"])
    val token: String,

    @SerializedName("employee_id") val employeeId: String,

    // Optional: Capture user data if sent
    @SerializedName("user") val user: Any? = null
)