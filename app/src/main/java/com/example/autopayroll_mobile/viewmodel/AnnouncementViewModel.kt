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

data class AnnouncementUiItem(
    val id: String,
    val title: String,
    val message: String,
    val displayDate: String,
    // Removed category field
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

    private fun fetchAnnouncements() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAnnouncements()

                if (response.success) {
                    // 1. Calculate date 2 months ago
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, -2)
                    val twoMonthsAgo: Date = calendar.time

                    // 2. Date Parser matching API format
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())

                    // 3. Filter and Map
                    val uiItems = response.announcements.filter { announcement ->
                        try {
                            val date = parser.parse(announcement.createdAt)
                            // Keep if date is not null and is after twoMonthsAgo
                            date != null && date.after(twoMonthsAgo)
                        } catch (e: Exception) {
                            false // If date parse fails, exclude it
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

    private fun Announcement.toUiItem(): AnnouncementUiItem {
        return AnnouncementUiItem(
            id = this.announcementId,
            title = this.title,
            message = this.message,
            displayDate = this.createdAt.toFormattedDate(),
            icon = Icons.Default.Campaign // Generic icon for all
        )
    }

    private fun String.toFormattedDate(): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            val formatter = SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault())
            parser.parse(this)?.let { formatter.format(it) } ?: this
        } catch (e: Exception) {
            this
        }
    }
}