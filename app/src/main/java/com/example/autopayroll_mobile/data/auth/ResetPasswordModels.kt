package com.example.autopayroll_mobile.data.auth

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    val email: String,
    val password: String,

    // CRITICAL FIX: Maps the variable to the exact key Laravel expects
    @SerializedName("password_confirmation")
    val passwordConfirmation: String
)

data class ResetPasswordResponse(
    val success: Boolean,
    val message: String
)