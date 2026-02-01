package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.Announcement
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AnnouncementUiItem(
    val id: String,
    val title: String,
    val message: String,
    val displayDate: String,
    val rawDate: Date?, // Added for sorting
    val category: String,
    val icon: ImageVector
)

class AnnouncementViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.getClient(application.applicationContext)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _announcements = MutableStateFlow<List<AnnouncementUiItem>>(emptyList())
    val announcements: StateFlow<List<AnnouncementUiItem>> = _announcements.asStateFlow()

    init {
        fetchAnnouncements()
    }

    fun refreshAnnouncements() {
        fetchAnnouncements()
    }

    private fun fetchAnnouncements() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAnnouncements()

                if (response.success) {
                    // Filter: Last 3 months only
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, -3)
                    val cutOffDate: Date = calendar.time

                    val uiItems = response.announcements
                        .map { it.toUiItem() }
                        .filter {
                            // Keep if date is valid AND recent, OR if parsing failed (safety)
                            it.rawDate == null || it.rawDate.after(cutOffDate)
                        }
                        .sortedByDescending { it.rawDate } // FIX: Show newest first

                    _announcements.value = uiItems
                } else {
                    _announcements.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("AnnouncementViewModel", "Failed to fetch announcements", e)
                _announcements.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // FIX: This method must exist for the Detail Screen to work
    fun getAnnouncementById(id: String): AnnouncementUiItem? {
        return _announcements.value.find { it.id == id }
    }

    private fun Announcement.toUiItem(): AnnouncementUiItem {
        // Improved Category Logic
        val textContent = "${this.title} ${this.message}".lowercase()

        val (generatedCategory, generatedIcon) = when {
            textContent.contains("payslip") || textContent.contains("salary") || textContent.contains("bonus") || textContent.contains("payroll") -> {
                "Payroll" to Icons.Default.Payment
            }
            textContent.contains("maintenance") || textContent.contains("system") || textContent.contains("policy") || textContent.contains("admin") -> {
                "Admin" to Icons.Default.Security
            }
            else -> "Memo" to Icons.Default.Notifications
        }

        // Parse Date for Sorting
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateObj = try {
            val dateString = this.createdAt.replace("T", " ").substringBefore(".")
            parser.parse(dateString)
        } catch (e: Exception) { null }

        return AnnouncementUiItem(
            id = this.announcementId,
            title = this.title,
            message = this.message,
            displayDate = this.createdAt.toFormattedDate(),
            rawDate = dateObj,
            category = generatedCategory,
            icon = generatedIcon
        )
    }

    private fun String.toFormattedDate(): String {
        return try {
            val raw = this.replace("T", " ").substringBefore(".")
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            parser.parse(raw)?.let { formatter.format(it) } ?: this
        } catch (e: Exception) {
            this
        }
    }
}