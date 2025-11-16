package com.example.autopayroll_mobile.data.qrModule

data class AttendanceLog(
    val log_id: String,
    val employee_id: String,
    val clock_in_time: String?, // Nullable if not clocked in
    val clock_out_time: String?, // Nullable if not clocked out
    val clock_in_latitude: Double?,
    val clock_in_longitude: Double?,
    // ... potentially other fields
)

data class TodayAttendanceResponse(
    val data: AttendanceLog? // Null if no log found for today
)