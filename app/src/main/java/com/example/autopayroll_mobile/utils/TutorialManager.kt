package com.example.autopayroll_mobile.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TutorialStep {
    NONE,

    // Dashboard
    DASHBOARD_WELCOME, DASHBOARD_SUMMARY, DASHBOARD_RECENT_PAYSLIP, DASHBOARD_SCHEDULE,
    NAVIGATE_TO_PAYSLIP,

    // Payslip List
    PAYSLIP_OVERVIEW, PAYSLIP_FILTER, PAYSLIP_LIST_AREA, PAYSLIP_CARD, PAYSLIP_VIEW_BTN,
    PAYSLIP_DETAIL_INFO,
    NAVIGATE_TO_QR,

    // QR Steps
    QR_RULES, QR_HOW_TO_USE,
    NAVIGATE_TO_ANNOUNCEMENT,

    // Announcement Steps
    ANNOUNCEMENT_OVERVIEW, ANNOUNCEMENT_TABS, ANNOUNCEMENT_CARD,
    NAVIGATE_TO_MENU,

    // Menu & Profile
    MENU_OVERVIEW, MENU_PROFILE_HIGHLIGHT, NAVIGATE_TO_PROFILE,
    PROFILE_OVERVIEW, NAVIGATE_BACK_FROM_PROFILE,

    // Leave
    MENU_LEAVE_HIGHLIGHT, NAVIGATE_TO_LEAVE,
    LEAVE_OVERVIEW, LEAVE_STATS, NAVIGATE_BACK_FROM_LEAVE,

    // Adjustment & Conclusion
    MENU_ADJUSTMENT_HIGHLIGHT, NAVIGATE_TO_ADJUSTMENT,
    ADJUSTMENT_OVERVIEW, ADJUSTMENT_STATS, TUTORIAL_END
}

object TutorialManager {
    private val _isTutorialActive = MutableStateFlow(false)
    val isTutorialActive: StateFlow<Boolean> = _isTutorialActive.asStateFlow()

    private val _currentStep = MutableStateFlow(TutorialStep.NONE)
    val currentStep: StateFlow<TutorialStep> = _currentStep.asStateFlow()

    fun startTutorial() {
        _isTutorialActive.value = true
        _currentStep.value = TutorialStep.DASHBOARD_WELCOME
    }

    fun nextStep(step: TutorialStep) {
        if (_isTutorialActive.value) {
            _currentStep.value = step
        }
    }

    fun endTutorial() {
        _isTutorialActive.value = false
        _currentStep.value = TutorialStep.NONE
    }
}