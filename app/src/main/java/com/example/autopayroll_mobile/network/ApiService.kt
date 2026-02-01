package com.example.autopayroll_mobile.network

import com.example.autopayroll_mobile.data.generalData.Employee
import com.example.autopayroll_mobile.data.model.PayrollResponse
import com.example.autopayroll_mobile.data.qrModule.ClockInOutRequest
import com.example.autopayroll_mobile.data.qrModule.ClockInOutResponse
import com.example.autopayroll_mobile.data.loginModule.LoginRequest
import com.example.autopayroll_mobile.data.loginModule.LoginResponse
import com.example.autopayroll_mobile.data.qrModule.TodayAttendanceResponse
import com.example.autopayroll_mobile.data.model.LeaveRequestListResponse
import com.example.autopayroll_mobile.data.model.LeaveRequestSubmitResponse
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentRequestListResponse
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentSubmitResponse
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentTypesResponse
import com.example.autopayroll_mobile.data.model.AnnouncementResponse
import com.example.autopayroll_mobile.data.model.ScheduleResponse
import com.example.autopayroll_mobile.data.model.WorkedHoursResponse
import com.example.autopayroll_mobile.data.model.LeaveCreditsResponse
import com.example.autopayroll_mobile.data.model.AbsencesResponse
import com.example.autopayroll_mobile.data.model.ChangePasswordRequest
import com.example.autopayroll_mobile.data.model.GenericSuccessResponse
import com.example.autopayroll_mobile.data.auth.OtpRequest
import com.example.autopayroll_mobile.data.auth.OtpResponse
import com.example.autopayroll_mobile.data.auth.OtpVerifyRequest
import com.example.autopayroll_mobile.data.auth.ResetPasswordRequest
import com.example.autopayroll_mobile.data.auth.ResetPasswordResponse
import com.example.autopayroll_mobile.data.auth.VerifyOtpResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    // --- Auth Endpoints ---
    @POST("api/employee/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("api/employee/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/employee/password-reset") // Ensure this matches your Laravel route
    suspend fun resetPassword(@Body request: ChangePasswordRequest): GenericSuccessResponse

    // --- Employee Profile ---
    @GET("api/employee/profile")
    suspend fun getEmployeeProfile(): Employee

    // --- Attendance Endpoints ---
    @POST("api/attendance/clock-in")
    suspend fun clockIn(@Body request: ClockInOutRequest): ClockInOutResponse

    @POST("api/attendance/clock-out")
    suspend fun clockOut(@Body request: ClockInOutRequest): ClockInOutResponse

    @GET("api/attendance/today")
    suspend fun getTodayAttendance(): TodayAttendanceResponse

    @GET("api/payroll/view")
    suspend fun getPayrolls(): PayrollResponse

    @GET("api/employee/schedules")
    suspend fun getSchedule(): ScheduleResponse

    @GET("api/employee/work-hours")
    suspend fun getTotalWorkedHours(): WorkedHoursResponse

    @GET("api/employee/leave-credit")
    suspend fun getLeaveCredits(): LeaveCreditsResponse

    @GET("api/employee/absences")
    suspend fun getAbsences(): AbsencesResponse

    // --- Leave Request Endpoints ---
    @Multipart
    @POST("api/employee/leave-request")
    suspend fun submitLeaveRequest(
        @Part("leave_type") leaveType: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part("end_date") endDate: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part attachment: MultipartBody.Part?
    ): LeaveRequestSubmitResponse

    @GET("api/employee/show/leave-request")
    suspend fun getLeaveRequests(): LeaveRequestListResponse

    // --- Credit Adjustment Endpoints ---
    @Multipart
    @POST("api/employee/credit-adjustment")
    suspend fun submitAdjustmentRequest(
        @Part("employee_id") employeeId: RequestBody,
        @Part("main_type") mainType: RequestBody,
        @Part("subtype") subtype: RequestBody,
        @Part("start_date") startDate: RequestBody?,
        @Part("end_date") endDate: RequestBody?,
        @Part("affected_date") affectedDate: RequestBody?,
        @Part("reason") reason: RequestBody?,
        @Part attachment: MultipartBody.Part?
    ): AdjustmentSubmitResponse

    @GET("api/employee/show/adjustment-request")
    suspend fun getAdjustmentRequests(): AdjustmentRequestListResponse

    @GET("api/employee/credit-adjustment/types")
    suspend fun getAdjustmentTypes(
        @Query("main_type") mainType: String
    ): AdjustmentTypesResponse

    @GET("api/employee/track/adjustment-request")
    suspend fun getPendingAdjustments(): AdjustmentRequestListResponse

    @GET("api/employee/announcements")
    suspend fun getAnnouncements(): AnnouncementResponse

    // ----- OTP -------

    @POST("api/request-otp")
    suspend fun requestOtp(@Body request: OtpRequest): OtpResponse

    @POST("api/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): VerifyOtpResponse

    @POST("api/change-password")
    suspend fun changePassword(@Body request: ResetPasswordRequest): ResetPasswordResponse

}