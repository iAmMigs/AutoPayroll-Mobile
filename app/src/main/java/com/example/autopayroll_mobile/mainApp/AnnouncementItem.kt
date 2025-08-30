package com.example.autopayroll_mobile // Use your package name

enum class AnnouncementCategory {
    All, Payroll, Admin, Memo
}

data class AnnouncementItem(
    val iconResId: Int,
    val title: String,
    val message: String,
    val date: String,
    val category: AnnouncementCategory
)