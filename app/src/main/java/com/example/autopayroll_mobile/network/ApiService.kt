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
import com.example.autopayroll_mobile.data.model.LeaveRequestListResponse // ## NEW ##
import com.example.autopayroll_mobile.data.model.LeaveRequestSubmit // ## NEW ##
import com.example.autopayroll_mobile.data.model.LeaveRequestSubmitResponse
import com.example.autopayroll_mobile.data.model.AdjustmentRequestDetailResponse
import com.example.autopayroll_mobile.data.model.AdjustmentRequestListResponse
import com.example.autopayroll_mobile.data.model.AdjustmentSubmitResponse
import com.example.autopayroll_mobile.data.model.AdjustmentTypesResponse
import com.example.autopayroll_mobile.data.model.Announcement
import com.example.autopayroll_mobile.data.model.AnnouncementResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query

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

    @POST("api/employee/leave-request")
    suspend fun submitLeaveRequest(@Body request: LeaveRequestSubmit): LeaveRequestSubmitResponse

    // ## UPDATED ##
    @GET("api/employee/show/leave-request")
    suspend fun getLeaveRequests(): LeaveRequestListResponse


// --- Credit Adjustment Endpoints ---
@Multipart
@POST("api/employee/credit-adjustment")
suspend fun submitAdjustmentRequest(
    @Part("adjustment_type") adjustmentType: RequestBody,
    @Part("sub_type") subType: RequestBody,
    @Part("start_date") startDate: RequestBody,
    @Part("end_date") endDate: RequestBody,
    @Part("reason") reason: RequestBody,
    @Part("hours") hours: RequestBody?, // Nullable if not provided
    @Part file: MultipartBody.Part?      // Nullable if no file
): AdjustmentSubmitResponse

    /**
     * Gets the list of all past adjustment requests for the tracking list.
     * Corresponds to: GET /employee/show/adjustment-request
     */
    @GET("api/employee/show/adjustment-request")
    suspend fun getAdjustmentRequests(): AdjustmentRequestListResponse

    /**
     * Gets the details for a SINGLE adjustment request.
     * Corresponds to: GET /employee/track/adjustment-request
     * We use @Query("id") to pass the ID, e.g., .../track/adjustment-request?id=123
     */
    @GET("api/employee/track/adjustment-request")
    suspend fun getAdjustmentRequestDetail(@Query("id") requestId: Int): AdjustmentRequestDetailResponse

    /**
     * Gets the list of all available adjustment sub-types for the form dropdown.
     * Corresponds to: GET /employee/credit-adjustment/types
     */
    @GET("api/employee/credit-adjustment/types")
    suspend fun getAdjustmentTypes(): AdjustmentTypesResponse



    // --- General Endpoints ---

    @GET("api/employee/announcements")
    suspend fun getAnnouncements(): AnnouncementResponse // Assuming it returns a list
    // TODO: Wait Until Needed
//    @GET("api/companies")
//    suspend fun getCompanies(): List<Company> // Assuming it returns a list
    // TODO: Wait Until Needed
//    @GET("api/schedules")
//    suspend fun getSchedules(): List<Schedule> // Assuming it returns a list
}