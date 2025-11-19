package com.example.autopayroll_mobile.data.generalData

// This class matches the top-level JSON structure
data class EmployeeApiResponse(
    val data: List<Employee>,
    val count: Int
)