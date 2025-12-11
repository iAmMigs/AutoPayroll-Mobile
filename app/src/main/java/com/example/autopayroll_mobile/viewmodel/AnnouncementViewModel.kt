package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
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

// 1. We keep 'category' here because the UI relies on it for filtering
data class AnnouncementUiItem(
    val id: String,
    val title: String,
    val message: String,
    val displayDate: String,
    val category: String, // UI needs this, even if API doesn't have it
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
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, -2) // Show last 2 months
                    val twoMonthsAgo: Date = calendar.time

                    val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // Adjusted format likely matches DB

                    val uiItems = response.announcements.filter { announcement ->
                        try {
                            // Handle cases where DB date might be slightly different format
                            val dateString = announcement.createdAt.replace("T", " ").substringBefore(".")
                            val date = parser.parse(dateString)
                            date != null && date.after(twoMonthsAgo)
                        } catch (e: Exception) {
                            true // If date parsing fails, keep it just in case
                        }
                    }.map { it.toUiItem() }

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

    fun getAnnouncementById(id: String): AnnouncementUiItem? {
        return _announcements.value.find { it.id == id }
    }

    // 2. THE TEMPLATE LOGIC
    private fun Announcement.toUiItem(): AnnouncementUiItem {
        // Since API lacks 'category', we guess it from the Title
        val generatedCategory = when {
            this.title.contains("Maintenance", ignoreCase = true) -> "Admin"
            this.title.contains("System", ignoreCase = true) -> "Admin"
            this.title.contains("Payslip", ignoreCase = true) -> "Payroll"
            this.title.contains("Salary", ignoreCase = true) -> "Payroll"
            this.title.contains("Policy", ignoreCase = true) -> "Admin"
            else -> "Memo" // Default for "Holiday", "Event", etc.
        }

        return AnnouncementUiItem(
            id = this.announcementId,
            title = this.title,
            message = this.message,
            displayDate = this.createdAt.toFormattedDate(),
            category = generatedCategory, // <--- Value is injected here
            icon = Icons.Default.Campaign
        )
    }

    private fun String.toFormattedDate(): String {
        return try {
            // Flexible parser for standard SQL timestamp
            val raw = this.replace("T", " ").substringBefore(".")
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault())
            parser.parse(raw)?.let { formatter.format(it) } ?: this
        } catch (e: Exception) {
            this
        }
    }
}