package com.example.autopayroll_mobile.composableUI.leaveModuleUI

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel
import com.example.autopayroll_mobile.viewmodel.NavigationEvent

// Define the routes for the screens *within* this module
private object LeaveModuleRoutes {
    const val LIST = "leave_list"
    const val FORM = "leave_form"
    // const val CALENDAR = "leave_calendar" // Removed: Calendar is now a pop-up dialog
}

@Composable
fun LeaveModuleNavHost(
    viewModel: LeaveModuleViewModel,
    onBackToMenu: () -> Unit
) {
    val internalNavController = rememberNavController()

    // Observe Navigation Events for internal navigation (e.g., Form -> List after submit)
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event is NavigationEvent.NavigateBack) {
                internalNavController.popBackStack()
                viewModel.onNavigationHandled()
            }
        }
    }

    NavHost(
        navController = internalNavController,
        startDestination = LeaveModuleRoutes.LIST,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {

        composable(LeaveModuleRoutes.LIST) {
            LeaveModuleListScreen(
                viewModel = viewModel,
                onFileLeaveClicked = {
                    internalNavController.navigate(LeaveModuleRoutes.FORM)
                },
                // Calendar click is now handled internally in ListScreen via state
                onBackToMenu = onBackToMenu
            )
        }

        composable(LeaveModuleRoutes.FORM) {
            LeaveModuleFormScreen(
                viewModel = viewModel,
                onBackClicked = {
                    internalNavController.popBackStack()
                }
            )
        }
    }
}