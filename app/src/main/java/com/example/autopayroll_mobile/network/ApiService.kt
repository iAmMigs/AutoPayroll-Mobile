package com.example.autopayroll_mobile.network

import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.data.model.EmployeeApiResponse
import com.example.autopayroll_mobile.data.model.PayrollResponse
import com.example.autopayroll_mobile.data.model.ClockInOutRequest
import com.example.autopayroll_mobile.data.model.ClockInOutResponse
import com.example.autopayroll_mobile.data.model.LoginRequest
import com.example.autopayroll_mobile.data.model.LoginResponse
import com.example.autopayroll_mobile.data.model.Company // This was commented out, but GET /companies will need it
//import com.example.autopayroll_mobile.data.model.Schedule // TODO: Create data class Schedule
//import com.example.autopayroll_mobile.data.model.TodayAttendanceResponse // TODO: Create data class TodayAttendanceResponse
//import com.example.autopayroll_mobile.data.model.PasswordResetRequest // TODO: Create data class PasswordResetRequest
//import com.example.autopayroll_mobile.data.model.PasswordResetResponse // TODO: Create data class PasswordResetResponse
//import com.example.autopayroll_mobile.data.model.LeaveRequest // TODO: Create data class LeaveRequest
//import com.example.autopayroll_mobile.data.model.LeaveRequestResponse // TODO: Create data class LeaveRequestResponse
//import com.example.autopayroll_mobile.data.model.LeaveRequestStatus // TODO: Create data class LeaveRequestStatus
//import com.example.autopayroll_mobile.data.model.AdjustmentRequest // TODO: Create data class AdjustmentRequest
//import com.example.autopayroll_mobile.data.model.AdjustmentRequestResponse // TODO: Create data class AdjustmentRequestResponse
//import com.example.autopayroll_mobile.data.model.AdjustmentRequestStatus // TODO: Create data class AdjustmentRequestStatus
//import com.example.autopayroll_mobile.data.model.Announcement // TODO: Create data class Announcement
//import com.example.autopayroll_mobile.data.model.AdjustmentType // TODO: Create data class AdjustmentType
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // --- Auth Endpoints ---

    @POST("api/employee/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse


    @POST("api/employee/{id}/logout")
    suspend fun logout(@Path("id") employeeId: Int): Response<Unit>

    // TODO: Wait Until Needed
//    @POST("api/employee/password-reset")
//    suspend fun resetPassword(@Body request: PasswordResetRequest): PasswordResetResponse


    // --- Employee Profile ---

    @GET("api/employee/profile")
    suspend fun getEmployeeProfile(): Employee



    // --- Attendance Endpoints ---

    @POST("api/attendance/clock-in")
    suspend fun clockIn(@Body request: ClockInOutRequest): ClockInOutResponse

    @POST("api/attendance/clock-out")
    suspend fun clockOut(@Body request: ClockInOutRequest): ClockInOutResponse

    // TODO: Wait Until Needed
//    @GET("api/attendance/today")
//    suspend fun getTodayAttendance(): TodayAttendanceResponse



    // --- Payroll Endpoint ---

    @GET("api/payroll/view")
    suspend fun getPayrolls(): PayrollResponse // Assumes this returns a list

    // --- Leave Request Endpoints ---

    // TODO: Wait Until Needed
//    @POST("api/employee/leave-request")
//    suspend fun submitLeaveRequest(@Body request: LeaveRequest): LeaveRequestResponse
    // TODO: Wait Until Needed
//    @GET("api/employee/show/leave-request")
//    suspend fun getLeaveRequests(): List<LeaveRequest> // Assuming it returns a list
    // TODO: Wait Until Needed
//    @GET("api/employee/track/leave-request")
//    suspend fun trackLeaveRequests(): List<LeaveRequestStatus> // Assuming it returns a list

// --- Credit Adjustment Endpoints ---
    // TODO: Wait Until Needed
//    @POST("api/employee/credit-adjustment")
//    suspend fun submitAdjustmentRequest(@Body request: AdjustmentRequest): AdjustmentRequestResponse
    // TODO: Wait Until Needed
//    @GET("api/employee/show/adjustment-request")
//    suspend fun getAdjustmentRequests(): List<AdjustmentRequest> // Assuming it returns a list
    // TODO: Wait Until Needed
//    @GET("api/employee/track/adjustment-request")
//    suspend fun trackAdjustmentRequests(): List<AdjustmentRequestStatus> // Assuming it returns a list
    // TODO: Wait Until Needed
//    @GET("api/employee/credit-adjustment/types")
//    suspend fun getAdjustmentTypes(): List<AdjustmentType> // Assuming it returns a list



    // --- General Endpoints ---


    // TODO: Wait Until Needed
//    @GET("api/employee/announcements")
//    suspend fun getAnnouncements(): List<Announcement> // Assuming it returns a list
    // TODO: Wait Until Needed
//    @GET("api/companies")
//    suspend fun getCompanies(): List<Company> // Assuming it returns a list
    // TODO: Wait Until Needed
//    @GET("api/schedules")
//    suspend fun getSchedules(): List<Schedule> // Assuming it returns a list
}