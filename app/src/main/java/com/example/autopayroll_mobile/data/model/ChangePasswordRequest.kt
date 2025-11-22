package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    // PHP validates: 'password' => 'required'
    @SerializedName("password")
    val currentPassword: String,

    // PHP validates: 'new_password' => 'required|...|confirmed'
    @SerializedName("new_password")
    val newPassword: String,

    // Laravel's 'confirmed' rule automatically looks for [field_name]_confirmation
    @SerializedName("new_password_confirmation")
    val confirmPassword: String
)