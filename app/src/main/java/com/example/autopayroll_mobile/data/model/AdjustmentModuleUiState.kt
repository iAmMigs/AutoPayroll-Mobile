package com.example.autopayroll_mobile.data.model

import com.example.autopayroll_mobile.data.model.AdjustmentRequest
import com.example.autopayroll_mobile.data.model.AdjustmentRequestDetail
import com.example.autopayroll_mobile.data.model.AdjustmentType
import java.io.File

/**
 * Represents the complete state for the entire Adjustment Module.
 */
data class AdjustmentModuleUiState(
    // --- Data States ---
    val adjustmentTypes: List<AdjustmentType> = emptyList(),
    val adjustmentRequests: List<AdjustmentRequest> = emptyList(),
    val selectedRequestDetail: AdjustmentRequestDetail? = null,

    // --- Loading States ---
    val isLoadingTypes: Boolean = false,
    val isLoadingRequests: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isSubmitting: Boolean = false,

    // --- Error States ---
    val typesError: String? = null,
    val requestsError: String? = null,
    val detailError: String? = null,
    val submissionError: String? = null,

    // --- Form Input State ---
    val formMainType: String = "Leave", // "Leave", "Attendance", or "Holiday"
    val formSubType: AdjustmentType? = null,
    val formStartDate: String = "",
    val formEndDate: String = "",
    val formHours: String = "",
    val formReason: String = "",
    val formAttachment: File? = null, // Store the selected file

    // --- Submission State ---
    val submissionStatus: FormSubmissionStatus = FormSubmissionStatus.IDLE,

    // ## NEW ##
    val filterStatus: String = "All" // "All", "Pending", "Approved", "Rejected"
)

/**
 * Enum to track the result of the form submission
 */
enum class FormSubmissionStatus {
    IDLE,
    SUCCESS,
    ERROR
}