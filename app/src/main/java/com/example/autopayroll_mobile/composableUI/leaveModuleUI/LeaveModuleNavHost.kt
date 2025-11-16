package com.example.autopayroll_mobile.composableUI.leaveModuleUI

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel

// Define the routes for the screens *within* this module
private object LeaveModuleRoutes {
    const val LIST = "leave_list"
    const val FORM = "leave_form"
    const val CALENDAR = "leave_calendar"
}

@Composable
fun LeaveModuleNavHost(
    viewModel: LeaveModuleViewModel,
    onBackToMenu: () -> Unit
) {
    val internalNavController = rememberNavController()

    NavHost(
        navController = internalNavController,
        startDestination = LeaveModuleRoutes.LIST
    ) {
        // Main List Screen
        composable(LeaveModuleRoutes.LIST) {
            LeaveModuleListScreen(
                viewModel = viewModel,
                onFileLeaveClicked = {
                    internalNavController.navigate(LeaveModuleRoutes.FORM)
                },
                onCalendarClicked = {
                    internalNavController.navigate(LeaveModuleRoutes.CALENDAR)
                },
                onBackToMenu = onBackToMenu
            )
        }

        // "File a Leave" Form Screen
        composable(LeaveModuleRoutes.FORM) {
            LeaveModuleFormScreen(
                viewModel = viewModel,
                onBackClicked = {
                    internalNavController.popBackStack()
                }
            )
        }

        // Calendar View Screen
        composable(LeaveModuleRoutes.CALENDAR) {
            LeaveModuleCalendarScreen(
                viewModel = viewModel,
                onBackClicked = {
                    internalNavController.popBackStack()
                }
            )
        }
    }
}