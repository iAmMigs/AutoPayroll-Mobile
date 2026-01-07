package com.example.autopayroll_mobile.composableUI.adjustmentModuleUI

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel

object AdjustmentModuleDestinations {
    const val HUB_SCREEN = "hub"
    const val FILING_SCREEN = "filing"
    const val TRACK_DETAIL_SCREEN = "trackDetail"
    const val REQUEST_ID_ARG = "requestId"
    val TRACK_DETAIL_ROUTE = "$TRACK_DETAIL_SCREEN/{$REQUEST_ID_ARG}"
}

@Composable
fun AdjustmentModuleScreen(
    viewModel: AdjustmentModuleViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    onBackToMenu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = AdjustmentModuleDestinations.HUB_SCREEN,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // 1. Dashboard (Stats + History)
        composable(AdjustmentModuleDestinations.HUB_SCREEN) {
            AdjustmentHubScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToFiling = { navController.navigate(AdjustmentModuleDestinations.FILING_SCREEN) },
                onNavigateToDetail = { requestId ->
                    navController.navigate("${AdjustmentModuleDestinations.TRACK_DETAIL_SCREEN}/$requestId")
                },
                onBack = onBackToMenu
            )
        }

        // 2. Filing Form
        composable(AdjustmentModuleDestinations.FILING_SCREEN) {
            AdjustmentFilingScreen(
                uiState = uiState,
                viewModel = viewModel,
                onBack = {
                    viewModel.clearForm()
                    navController.popBackStack()
                }
            )
        }

        // 3. Details View
        composable(
            route = AdjustmentModuleDestinations.TRACK_DETAIL_ROUTE,
            arguments = listOf(navArgument(AdjustmentModuleDestinations.REQUEST_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString(AdjustmentModuleDestinations.REQUEST_ID_ARG) ?: ""
            AdjustmentDetailScreen(
                uiState = uiState,
                requestId = requestId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}