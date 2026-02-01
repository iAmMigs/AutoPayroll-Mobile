package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.LeaveBalance
import com.example.autopayroll_mobile.data.model.LeaveRequest
import com.example.autopayroll_mobile.data.model.ValidationErrorResponse
import com.example.autopayroll_mobile.network.ApiClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class LeaveModuleUiState(
    val isLoading: Boolean = true,
    val leaveBalance: LeaveBalance = LeaveBalance(),
    val selectedTab: String = "Pending",

    val leaveTypes: Map<String, String> = mapOf(
        "Sick" to "Sick Leave",
        "Vacation" to "Vacation Leave",
        "Maternity" to "Maternity Leave",
        "Bereavement" to "Bereavement Leave",
        "Paternity" to "Paternity Leave"
    ),

    val errorMessage: String? = null,
    val formLeaveType: String = "Sick Leave",
    val formStartDate: String = "",
    val formEndDate: String = "",
    val formReason: String = "",
    val formIsSubmitting: Boolean = false,
    val allRequests: List<LeaveRequest> = emptyList(),
    val formAttachment: File? = null,

    // New State for Calendar Dialog
    val isCalendarVisible: Boolean = false
)

// Extension for filtering list
val LeaveModuleUiState.filteredRequests: List<LeaveRequest>
    get() = when (selectedTab) {
        "Pending" -> allRequests.filter { it.status.equals("pending", ignoreCase = true) }
        "Approved" -> allRequests.filter { it.status.equals("approved", ignoreCase = true) }
        "Declined" -> allRequests.filter {
            val s = it.status.lowercase()
            s == "declined" || s == "rejected" || s == "need revision" || s == "needs revision"
        }
        else -> allRequests
    }

class LeaveModuleViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.getClient(application.applicationContext)
    private val _uiState = MutableStateFlow(LeaveModuleUiState())
    val uiState: StateFlow<LeaveModuleUiState> = _uiState.asStateFlow()
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    val tabItems = listOf("Pending", "Approved", "Declined")

    init {
        fetchData()
    }

    fun fetchData() {
        fetchLeaveBalances()
        fetchLeaveRequests()
    }

    // --- Calendar Actions ---
    fun showCalendar() {
        _uiState.update { it.copy(isCalendarVisible = true) }
    }

    fun hideCalendar() {
        _uiState.update { it.copy(isCalendarVisible = false) }
    }

    // --- Existing API Calls (Unchanged) ---
    private fun fetchLeaveBalances() {
        viewModelScope.launch {
            try {
                val response = apiService.getLeaveCredits()
                if (response.success) {
                    _uiState.update {
                        it.copy(leaveBalance = LeaveBalance(available = response.creditDays.toInt(), used = 0))
                    }
                }
            } catch (e: Exception) {
                Log.e("LeaveVM", "Error fetching credits", e)
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
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // --- Form Submission (Unchanged) ---
    fun submitLeaveRequest() {
        val state = _uiState.value
        if (state.formStartDate.isBlank() || state.formEndDate.isBlank() || state.formReason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required.") }
            return
        }

        val leaveTypeApiKey = state.leaveTypes.entries.find { it.value == state.formLeaveType }?.key ?: "sick"

        viewModelScope.launch {
            _uiState.update { it.copy(formIsSubmitting = true, errorMessage = null) }
            try {
                val leaveTypePart = leaveTypeApiKey.toRequestBody("text/plain".toMediaTypeOrNull())
                val startDatePart = state.formStartDate.toRequestBody("text/plain".toMediaTypeOrNull())
                val endDatePart = state.formEndDate.toRequestBody("text/plain".toMediaTypeOrNull())
                val reasonPart = state.formReason.toRequestBody("text/plain".toMediaTypeOrNull())

                val filePart = state.formAttachment?.let { file ->
                    val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("attachment", file.name, requestFile)
                }

                val response = apiService.submitLeaveRequest(leaveTypePart, startDatePart, endDatePart, reasonPart, filePart)

                if (response.success) {
                    _uiState.update { it.copy(formIsSubmitting = false) }
                    fetchLeaveRequests()
                    fetchLeaveBalances()
                    _navigationEvent.value = NavigationEvent.NavigateBack
                    clearForm()
                } else {
                    val errorMsg = response.errors?.values?.firstOrNull()?.firstOrNull() ?: response.message ?: "Submission failed"
                    _uiState.update { it.copy(formIsSubmitting = false, errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(formIsSubmitting = false, errorMessage = e.message) }
            }
        }
    }

    // --- Helper Functions ---
    fun formatLeaveType(rawType: String?): String {
        if (rawType == null) return "Leave"
        val mapped = _uiState.value.leaveTypes[rawType.lowercase()]
        if (mapped != null) return mapped
        return rawType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + " Leave"
    }

    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
        fetchLeaveRequests()
    }

    fun onLeaveTypeChanged(type: String) { _uiState.update { it.copy(formLeaveType = type) } }
    fun onStartDateChanged(date: String) { _uiState.update { it.copy(formStartDate = date) } }
    fun onEndDateChanged(date: String) { _uiState.update { it.copy(formEndDate = date) } }
    fun onReasonChanged(reason: String) { _uiState.update { it.copy(formReason = reason) } }
    fun onAttachmentSelected(uri: Uri) {
        viewModelScope.launch {
            val file = copyUriToCache(uri)
            _uiState.update { it.copy(formAttachment = file) }
        }
    }
    fun onAttachmentRemoved() { _uiState.update { it.copy(formAttachment = null) } }
    fun clearErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }
    fun onNavigationHandled() { _navigationEvent.value = null }
    fun clearForm() {
        _uiState.update { it.copy(formLeaveType = "Sick Leave", formStartDate = "", formEndDate = "", formReason = "", formAttachment = null, formIsSubmitting = false, errorMessage = null) }
    }
    fun navigateBackToMenu() { _navigationEvent.value = NavigationEvent.NavigateBackToMenu }

    fun formatDisplayDate(date: String): String {
        val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
        return try {
            val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            LocalDateTime.parse(date, parser).format(outputFormatter)
        } catch (e: Exception) {
            try {
                LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).format(outputFormatter)
            } catch (e2: Exception) { date }
        }
    }

    private fun copyUriToCache(uri: Uri): File? {
        return try {
            val contentResolver = getApplication<Application>().contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            val name = cursor?.use {
                if (it.moveToFirst()) it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else "temp_file"
            } ?: "temp_file"
            val file = File(getApplication<Application>().cacheDir, name)
            FileOutputStream(file).use { out -> contentResolver.openInputStream(uri)?.use { inp -> inp.copyTo(out) } }
            file
        } catch (e: Exception) { null }
    }
}

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
    object NavigateBackToMenu : NavigationEvent()
}