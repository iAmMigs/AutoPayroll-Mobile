package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.LeaveBalance
import com.example.autopayroll_mobile.data.model.LeaveRequest
import com.example.autopayroll_mobile.data.model.LeaveRequestSubmit
import com.example.autopayroll_mobile.data.model.ValidationErrorResponse
import com.example.autopayroll_mobile.network.ApiClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate // ## NEW IMPORT ##
import java.time.LocalDateTime // ## NEW IMPORT ##
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ## 1. THIS DATA CLASS IS UPDATED ##
data class LeaveModuleUiState(
    val isLoading: Boolean = true,
    val leaveBalance: LeaveBalance = LeaveBalance(),
    val selectedTab: String = "Pending",

    // ## FIX: Added all leave types as requested ##
    // The KEY is the API value (e.g., "sick")
    // The VALUE is the UI display name (e.g., "Sick Leave")
    val leaveTypes: Map<String, String> = mapOf(
        "sick" to "Sick Leave",
        "vacation" to "Vacation Leave",
        "maternity" to "Maternity Leave",
        "bereavement" to "Bereavement Leave",
        "emergency" to "Emergency Leave"
    ),

    val errorMessage: String? = null,
    val formLeaveType: String = "Sick Leave", // Default is still "Sick Leave"
    val formStartDate: String = "",
    val formEndDate: String = "",
    val formReason: String = "",
    val formIsSubmitting: Boolean = false,
    val allRequests: List<LeaveRequest> = emptyList()
)

// (extension property is unchanged)
val LeaveModuleUiState.filteredRequests: List<LeaveRequest>
    get() = when (selectedTab) {
        "Pending" -> allRequests.filter { it.status.equals("pending", ignoreCase = true) }
        "Approved" -> allRequests.filter { it.status.equals("approved", ignoreCase = true) }
        "Declined" -> allRequests.filter { it.status.equals("declined", ignoreCase = true) }
        else -> allRequests
    }


class LeaveModuleViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.getClient(application.applicationContext)
    private val _uiState = MutableStateFlow(LeaveModuleUiState())
    val uiState: StateFlow<LeaveModuleUiState> = _uiState.asStateFlow()
    val tabItems = listOf("Pending", "Approved", "Declined")
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        fetchLeaveBalances()
        fetchLeaveRequests()
    }

    // (fetchLeaveBalances is unchanged)
    private fun fetchLeaveBalances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val employee = apiService.getEmployeeProfile()
                _uiState.update {
                    it.copy(
                        leaveBalance = LeaveBalance(
                            available = employee.availableLeaves,
                            used = employee.usedLeaves
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("LeaveModuleViewModel", "Failed to fetch profile for leave balance", e)
                _uiState.update { it.copy(errorMessage = "Failed to load leave balance") }
            }
        }
    }

    // (fetchLeaveRequests is unchanged)
    fun fetchLeaveRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getLeaveRequests()
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, allRequests = response.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, allRequests = emptyList()) }
                }
            } catch (e: Exception) {
                Log.e("LeaveModuleViewModel", "Failed to fetch leave requests", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load requests: ${e.message}") }
            }
        }
    }

    // (submitLeaveRequest is unchanged)
    fun submitLeaveRequest() {
        val state = _uiState.value

        if (state.formStartDate.isBlank() || state.formEndDate.isBlank() || state.formReason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required.") }
            return
        }

        // This logic correctly finds the API key (e.g., "sick")
        // from the display name (e.g., "Sick Leave")
        val leaveTypeApiKey = state.leaveTypes.entries
            .find { it.value == state.formLeaveType }
            ?.key
            ?: "sick" // Fallback

        val request = LeaveRequestSubmit(
            leaveType = leaveTypeApiKey,
            startDate = state.formStartDate,
            endDate = state.formEndDate,
            reason = state.formReason
        )

        viewModelScope.launch {
            _uiState.update { it.copy(formIsSubmitting = true, errorMessage = null) }
            try {
                apiService.submitLeaveRequest(request)
                _uiState.update { it.copy(formIsSubmitting = false) }
                fetchLeaveRequests()
                _navigationEvent.value = NavigationEvent.NavigateBack

            } catch (e: Exception) {
                // This catch block correctly handles 422 and 500 errors
                Log.e("LeaveModuleViewModel", "Failed to submit leave request", e)
                var errorMsg = "Submission failed: ${e.message}"

                if (e is HttpException) {
                    if (e.code() == 422) {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            val validationResponse = Gson().fromJson(errorBody, ValidationErrorResponse::class.java)
                            val firstError = validationResponse.errors?.values?.firstOrNull()?.firstOrNull()
                            errorMsg = firstError ?: validationResponse.message
                        } catch (jsonError: Exception) {
                            Log.e("LeaveModuleViewModel", "Failed to parse 422 error body", jsonError)
                            errorMsg = "Submission failed: Invalid data"
                        }
                    } else if (e.code() == 500) {
                        errorMsg = "A server error occurred. Please try again later."
                    }
                }

                _uiState.update { it.copy(formIsSubmitting = false, errorMessage = errorMsg) }
            }
        }
    }

    // (UI event handlers are unchanged)
    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    fun onLeaveTypeChanged(type: String) {
        _uiState.update { it.copy(formLeaveType = type) }
    }
    fun onStartDateChanged(date: String) {
        _uiState.update { it.copy(formStartDate = date) }
    }
    fun onEndDateChanged(date: String) {
        _uiState.update { it.copy(formEndDate = date) }
    }
    fun onReasonChanged(reason: String) {
        _uiState.update { it.copy(formReason = reason) }
    }
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    // ## NEW: Function to trigger back navigation to the MenuFragment ##
    fun navigateBackToMenu() {
        _navigationEvent.value = NavigationEvent.NavigateBackToMenu
    }

    // --- Date Formatting ---

    // ## 2. THIS FUNCTION IS THE FIX FOR THE TIME ##
    /**
     * Formats a date string (e.g., "2025-08-20") or a timestamp string
     * (e.g., "2025-11-11 17:22:52") into a display-friendly date
     * (e.g., "August 20, 2025").
     */
    fun formatDisplayDate(date: String): String {
        // Define the output format
        val outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.getDefault())

        // Try 1: Parse "yyyy-MM-dd HH:mm:ss" (for created_at)
        try {
            val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return LocalDateTime.parse(date, parser).format(outputFormatter)
        } catch (e: Exception) {
            // Try 2: Parse "YYYY-MM-DD" (for start_date/end_date)
            try {
                // ISO_LOCAL_DATE is the standard for "YYYY-MM-DD"
                return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).format(outputFormatter)
            } catch (e2: Exception) {
                // Try 3: Parse full ISO format (e.g., "2025-08-20T00:00:00Z")
                try {
                    return OffsetDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME).format(outputFormatter)
                } catch (e3: Exception) {
                    // All parsing failed. Return original string.
                    Log.w("LeaveModuleViewModel", "Could not parse date: $date")
                    return date
                }
            }
        }
    }
}

// ## UPDATED: Add a dedicated back-to-menu event ##
sealed class NavigationEvent {
    object NavigateBack : NavigationEvent() // Used for navigation *within* the module (e.g., Form -> List)
    object NavigateBackToMenu : NavigationEvent() // Used for navigation *out* of the module (e.g., List -> Menu)
}