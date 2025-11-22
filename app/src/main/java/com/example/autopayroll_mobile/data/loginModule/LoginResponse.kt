package com.example.autopayroll_mobile.data.loginModule

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("employee_id")
    val employeeId: String,

    @SerializedName("token")
    val token: String
)