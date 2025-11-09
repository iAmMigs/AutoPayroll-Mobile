package com.example.autopayroll_mobile.data.model

data class LoginResponse(
    val employee_id: String,
    val token: String
    // Add any other fields the server returns
)