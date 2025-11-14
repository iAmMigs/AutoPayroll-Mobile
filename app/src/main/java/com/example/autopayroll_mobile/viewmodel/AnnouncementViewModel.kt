package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.Announcement
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class AnnouncementUiItem(
    val id: String,
    val title: String,
    val message: String,
    val displayDate: String,
    val category: String,
    val icon: ImageVector
)

class AnnouncementViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.getClient(application.applicationContext)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allAnnouncements = MutableStateFlow<List<AnnouncementUiItem>>(emptyList())

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredAnnouncements: StateFlow<List<AnnouncementUiItem>> =
        _allAnnouncements.combine(_selectedCategory) { announcements, category ->
            if (category == "All") {
                announcements
            } else {
                announcements.filter { it.category == category }
            }
        }.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories = listOf("All", "Payroll", "Admin", "Memo")

    init {
        fetchAnnouncements()
    }

    private fun fetchAnnouncements() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAnnouncements()

                // ## FIX: Check the success flag and use .announcements ##
                if (response.success) {
                    val uiItems = response.announcements.map { it.toUiItem() }
                    _allAnnouncements.value = uiItems
                } else {
                    _allAnnouncements.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("AnnouncementViewModel", "Failed to fetch announcements", e)
                _allAnnouncements.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun getAnnouncementById(id: String): AnnouncementUiItem? {
        return _allAnnouncements.value.find { it.id == id }
    }

    // (Helper functions remain the same)
    private fun Announcement.toUiItem(): AnnouncementUiItem {
        val category = deriveCategory(this.title)
        return AnnouncementUiItem(
            id = this.announcementId,
            title = this.title,
            message = this.message,
            displayDate = this.createdAt.toFormattedDate(),
            category = category,
            icon = getIconForCategory(category)
        )
    }

    private fun deriveCategory(title: String): String {
        return when {
            title.contains("payroll", ignoreCase = true) -> "Payroll"
            title.contains("salary", ignoreCase = true) -> "Payroll"
            title.contains("memo", ignoreCase = true) -> "Memo"
            title.contains("follow-up", ignoreCase = true) -> "Admin"
            title.contains("system", ignoreCase = true) -> "Admin"
            else -> "Admin"
        }
    }

    private fun getIconForCategory(category: String): ImageVector {
        return when (category) {
            "Payroll" -> Icons.Default.Payment
            "Admin" -> Icons.Default.Person
            "Memo" -> Icons.AutoMirrored.Filled.Article
            else -> Icons.Default.Campaign
        }
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