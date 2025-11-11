package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Import our new models
import com.example.autopayroll_mobile.data.model.LeaveBalance
import com.example.autopayroll_mobile.data.model.LeaveRequest
import com.example.autopayroll_mobile.data.model.LeaveRequestSubmit
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// This data class holds all the state for the entire Leave Module
data class LeaveModuleUiState(
    val isLoading: Boolean = true,
    val leaveBalance: LeaveBalance = LeaveBalance(),
    val selectedTab: String = "Pending",
    // These could be fetched from an API later
    val leaveTypes: List<String> = listOf("Sick Leave", "Vacation Leave", "Emergency Leave"),
    val errorMessage: String? = null,

    // Form state
    val formLeaveType: String = "Sick Leave",
    val formStartDate: String = "", // "YYYY-MM-DD"
    val formEndDate: String = "", // "YYYY-MM-DD"
    val formReason: String = "",
    val formIsSubmitting: Boolean = false,

    // Master list of all requests
    val allRequests: List<LeaveRequest> = emptyList()
)

// Public extension property to get the filtered list
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

    // This event flow tells the Fragment when to navigate
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    init {
        // Fetch both balances and the list when ViewModel is created
        fetchData()
    }

    fun fetchData() {
        fetchLeaveBalances()
        fetchLeaveRequests()
    }

    private fun fetchLeaveBalances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // We get leave balance from the main employee profile
                val employee = apiService.getEmployeeProfile()

                // ## FIX ##
                // This code will now work because you added
                // availableLeaves and usedLeaves to your Employee.kt file (in Step 1)
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

    fun submitLeaveRequest() {
        val state = _uiState.value

        if (state.formStartDate.isBlank() || state.formEndDate.isBlank() || state.formReason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required.") }
            return
        }

        val request = LeaveRequestSubmit(
            leaveType = state.formLeaveType,
            startDate = state.formStartDate,
            endDate = state.formEndDate,
            reason = state.formReason
        )

        viewModelScope.launch {
            _uiState.update { it.copy(formIsSubmitting = true, errorMessage = null) }
            try {
                // Use the API route you sent: /employee/leave-request
                apiService.submitLeaveRequest(request)
                // Success!
                _uiState.update { it.copy(formIsSubmitting = false) }
                // Re-fetch the list to show the new pending item
                fetchLeaveRequests()
                // Tell the UI to navigate back to the list
                _navigationEvent.value = NavigationEvent.NavigateBack

            } catch (e: Exception) {
                Log.e("LeaveModuleViewModel", "Failed to submit leave request", e)
                _uiState.update { it.copy(formIsSubmitting = false, errorMessage = "Submission failed: ${e.message}") }
            }
        }
    }

    // --- UI Event Handlers ---

    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onLeaveTypeChanged(type: String) {
        _uiState.update { it.copy(formLeaveType = type) }
    }

    fun onStartDateChanged(date: String) { // date is "YYYY-MM-DD"
        _uiState.update { it.copy(formStartDate = date) }
    }

    fun onEndDateChanged(date: String) { // date is "YYYY-MM-DD"
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

    // --- Date Formatting ---
    // Helper to format dates for the list UI
    fun formatListDate(date: String): String {
        return try {
            val parser = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.getDefault())
            OffsetDateTime.parse(date, parser).format(formatter)
        } catch (e: Exception) {
            // Try the other format from your table (e.g., "2025-11-11 17:22:52")
            try {
                val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.getDefault())
                OffsetDateTime.parse(date, parser).format(formatter)
            } catch (e2: Exception) {
                date // return original on error
            }
        }
    }
}

// Helper class for navigation events
sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
}