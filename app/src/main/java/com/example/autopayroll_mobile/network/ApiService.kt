package com.example.autopayroll_mobile.network

import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.data.model.EmployeeApiResponse
import com.example.autopayroll_mobile.data.model.ClockInOutRequest
import com.example.autopayroll_mobile.data.model.ClockInOutResponse
import com.example.autopayroll_mobile.data.model.LoginRequest
import com.example.autopayroll_mobile.data.model.LoginResponse
import com.example.autopayroll_mobile.data.model.Company
import com.example.autopayroll_mobile.data.model.CompanyResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/employee/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/employees")
    suspend fun getEmployees(): EmployeeApiResponse
    @GET("api/employee/{id}/profile")
    suspend fun getEmployeeProfile(@Path("id") employeeId: String): Employee

    @GET("api/companies/{id}")
    suspend fun getCompany(@Path("id") companyId: String): Company

    @POST("api/attendance/clock-in")
    suspend fun clockIn(@Body request: ClockInOutRequest): ClockInOutResponse

    @POST("api/attendance/clock-out")
    suspend fun clockOut(@Body request: ClockInOutRequest): ClockInOutResponse


}