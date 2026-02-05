package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
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

    // --- RENAMED: Changed from isCalendar to isCalendarVisible to match your UI ---
    val isCalendarVisible: Boolean = false,

    val leaveTypes: Map<String, String> = mapOf(
        "sick" to "Sick Leave",
        "vacation" to "Vacation Leave",
        "maternity" to "Maternity Leave",
        "bereavement" to "Bereavement Leave",
        "emergency" to "Emergency Leave",
        "paternity" to "Paternity Leave"
    ),

    val errorMessage: String? = null,
    val formLeaveType: String = "Sick Leave",
    val formStartDate: String = "",
    val formEndDate: String = "",
    val formReason: String = "",
    val formIsSubmitting: Boolean = false,
    val allRequests: List<LeaveRequest> = emptyList(),

    // --- Attachment State ---
    val formAttachment: File? = null,
    val formAttachmentMimeType: String? = null
)

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

    private fun fetchLeaveBalances() {
        viewModelScope.launch {
            try {
                val response = apiService.getLeaveCredits()
                if (response.success) {
                    _uiState.update {
                        it.copy(leaveBalance = LeaveBalance(available = response.creditDays.toInt(), used = 0))
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Failed to load leave credits") }
                }
            } catch (e: Exception) {
                Log.e("LeaveVM", "Failed to fetch leave credits", e)
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
                Log.e("LeaveVM", "Failed to fetch leave requests", e)
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

        val leaveTypeMapReverse = state.leaveTypes.entries.associate { (k, v) -> v to k }
        val rawType = leaveTypeMapReverse[state.formLeaveType] ?: "sick"
        val apiLeaveType = rawType.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        viewModelScope.launch {
            _uiState.update { it.copy(formIsSubmitting = true, errorMessage = null) }
            try {
                val leaveTypePart = apiLeaveType.toRequestBody("text/plain".toMediaTypeOrNull())
                val startDatePart = state.formStartDate.toRequestBody("text/plain".toMediaTypeOrNull())
                val endDatePart = state.formEndDate.toRequestBody("text/plain".toMediaTypeOrNull())
                val reasonPart = state.formReason.toRequestBody("text/plain".toMediaTypeOrNull())

                val filePart = state.formAttachment?.let { file ->
                    val mimeType = state.formAttachmentMimeType ?: "application/octet-stream"
                    val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("attachment", file.name, requestFile)
                }

                val response = apiService.submitLeaveRequest(
                    leaveType = leaveTypePart,
                    startDate = startDatePart,
                    endDate = endDatePart,
                    reason = reasonPart,
                    attachment = filePart
                )

                if (response.success) {
                    _uiState.update { it.copy(formIsSubmitting = false) }
                    fetchLeaveRequests()
                    fetchLeaveBalances()
                    _navigationEvent.value = NavigationEvent.NavigateBack
                    clearForm()
                } else {
                    val errorMsg = response.errors?.values?.firstOrNull()?.firstOrNull()
                        ?: response.message
                        ?: "Submission failed"
                    Log.w("LeaveVM", "Submission failed: $errorMsg")
                    _uiState.update { it.copy(formIsSubmitting = false, errorMessage = errorMsg) }
                }

            } catch (e: Exception) {
                Log.e("LeaveVM", "Failed to submit leave request", e)
                var errorMsg = "Submission failed: ${e.message}"

                if (e is HttpException) {
                    if (e.code() == 422) {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            val validationResponse = Gson().fromJson(errorBody, ValidationErrorResponse::class.java)
                            errorMsg = validationResponse.errors?.values?.firstOrNull()?.firstOrNull()
                                ?: validationResponse.message
                                        ?: "Invalid data"
                        } catch (jsonError: Exception) {
                            errorMsg = "Submission failed: Invalid data"
                        }
                    } else if (e.code() == 413) {
                        errorMsg = "File is too large. Max size is 2MB."
                    }
                }
                _uiState.update { it.copy(formIsSubmitting = false, errorMessage = errorMsg) }
            }
        }
    }

    // --- Calendar Toggles (Updated to match 'isCalendarVisible') ---
    fun showCalendar() {
        _uiState.update { it.copy(isCalendarVisible = true) }
    }

    fun hideCalendar() {
        _uiState.update { it.copy(isCalendarVisible = false) }
    }

    // --- Attachment Helpers ---

    fun onAttachmentSelected(uri: Uri) {
        viewModelScope.launch {
            val result = copyUriToCache(uri)
            if (result != null) {
                _uiState.update {
                    it.copy(
                        formAttachment = result.first,
                        formAttachmentMimeType = result.second
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to attach file") }
            }
        }
    }

    fun onAttachmentRemoved() {
        _uiState.update { it.copy(formAttachment = null, formAttachmentMimeType = null) }
    }

    private fun copyUriToCache(uri: Uri): Pair<File, String>? {
        return try {
            val context = getApplication<Application>()
            val contentResolver = context.contentResolver

            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

            var fileName = "temp_attachment"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) fileName = cursor.getString(index)
                }
            }

            fileName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")

            if (!fileName.contains(".")) {
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                if (ext != null) {
                    fileName = "$fileName.$ext"
                }
            }

            val file = File(context.cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            Pair(file, mimeType)
        } catch (e: Exception) {
            Log.e("LeaveVM", "Error copying file", e)
            null
        }
    }

    // --- Other Helpers ---

    fun formatLeaveType(rawType: String?): String {
        if (rawType == null) return "Leave"
        val mapped = _uiState.value.leaveTypes[rawType.lowercase()]
        if (mapped != null) return mapped
        return rawType.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        } + " Leave"
    }

    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
        fetchLeaveRequests()
    }
    fun onLeaveTypeChanged(type: String) { _uiState.update { it.copy(formLeaveType = type) } }
    fun onStartDateChanged(date: String) { _uiState.update { it.copy(formStartDate = date) } }
    fun onEndDateChanged(date: String) { _uiState.update { it.copy(formEndDate = date) } }
    fun onReasonChanged(reason: String) { _uiState.update { it.copy(formReason = reason) } }
    fun clearErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }
    fun onNavigationHandled() { _navigationEvent.value = null }

    fun clearForm() {
        _uiState.update {
            it.copy(
                formLeaveType = "Sick Leave",
                formStartDate = "",
                formEndDate = "",
                formReason = "",
                formAttachment = null,
                formAttachmentMimeType = null,
                formIsSubmitting = false,
                errorMessage = null
            )
        }
    }

    fun navigateBackToMenu() {
        _navigationEvent.value = NavigationEvent.NavigateBackToMenu
    }

    fun formatDisplayDate(date: String): String {
        val outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.getDefault())
        try {
            val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return LocalDateTime.parse(date, parser).format(outputFormatter)
        } catch (e: Exception) {
            try {
                return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).format(outputFormatter)
            } catch (e2: Exception) {
                return date
            }
        }
    }
}

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
    object NavigateBackToMenu : NavigationEvent()
}