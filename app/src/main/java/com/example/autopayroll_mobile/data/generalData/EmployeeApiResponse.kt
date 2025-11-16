package com.example.autopayroll_mobile.data.model

// This class matches the top-level JSON structure
data class EmployeeApiResponse(
    val data: List<Employee>,
    val count: Int
)