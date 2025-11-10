package com.example.autopayroll_mobile.network

import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.data.model.EmployeeApiResponse
import com.example.autopayroll_mobile.data.model.ClockInOutRequest
import com.example.autopayroll_mobile.data.model.ClockInOutResponse
import com.example.autopayroll_mobile.data.model.LoginRequest
import com.example.autopayroll_mobile.data.model.LoginResponse
// import com.example.autopayroll_mobile.data.model.Company // Not needed
// import com.example.autopayroll_mobile.data.model.CompanyResponse // Not needed
import retrofit2.Response // Import this for logout
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
// import retrofit2.http.Path // No longer needed

interface ApiService {

    @POST("api/employee/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/employee/logout")
    suspend fun logout(): Response<Unit> // Added the missing logout endpoint

    // --- THIS IS THE MAIN FIX ---
    // The server uses the token to find the user, not an ID in the URL.
    @GET("api/employee/profile")
    suspend fun getEmployeeProfile(): Employee // No parameter needed

    // This endpoint wasn't provided, so I'm leaving it as-is
    @GET("api/employees")
    suspend fun getEmployees(): EmployeeApiResponse

    // Removed getCompany() as it's not needed.
    // The profile endpoint now provides the company name.

    @POST("api/attendance/clock-in")
    suspend fun clockIn(@Body request: ClockInOutRequest): ClockInOutResponse

    @POST("api/attendance/clock-out")
    suspend fun clockOut(@Body request: ClockInOutRequest): ClockInOutResponse
}