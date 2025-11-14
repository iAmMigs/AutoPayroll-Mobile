package com.example.autopayroll_mobile.composableUI

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
import com.example.autopayroll_mobile.composableUI.AdjustmentHubScreen
import com.example.autopayroll_mobile.composableUI.AdjustmentTrackScreen
import com.example.autopayroll_mobile.composableUI.AdjustmentFilingScreen
import com.example.autopayroll_mobile.composableUI.AdjustmentDetailScreen

object AdjustmentModuleDestinations {
    const val HUB_SCREEN = "hub"
    const val FILING_SCREEN = "filing"
    const val TRACK_LIST_SCREEN = "trackList"
    const val TRACK_DETAIL_SCREEN = "trackDetail"
    const val REQUEST_ID_ARG = "requestId" // ID is now a String
    val TRACK_DETAIL_ROUTE = "$TRACK_DETAIL_SCREEN/{$REQUEST_ID_ARG}"
}

@Composable
fun AdjustmentModuleScreen(
    viewModel: AdjustmentModuleViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = AdjustmentModuleDestinations.HUB_SCREEN,
        // No animations
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }

    ) {

        composable(AdjustmentModuleDestinations.HUB_SCREEN) {
            AdjustmentHubScreen(
                uiState = uiState,
                onNavigateToFiling = {
                    navController.navigate(AdjustmentModuleDestinations.FILING_SCREEN)
                },
                onNavigateToTracking = {
                    navController.navigate(AdjustmentModuleDestinations.TRACK_LIST_SCREEN)
                },
                onBack = {
                    // TODO: Go back to MenuFragment
                }
            )
        }

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

        composable(AdjustmentModuleDestinations.TRACK_LIST_SCREEN) {
            AdjustmentTrackScreen(
                uiState = uiState,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSelectRequest = { requestId -> // requestId is a String
                    navController.navigate("${AdjustmentModuleDestinations.TRACK_DETAIL_SCREEN}/$requestId")
                }
            )
        }

        /**
         * Screen 4: The "Track Request Detail"
         * ## UPDATED ##
         */
        composable(
            route = AdjustmentModuleDestinations.TRACK_DETAIL_ROUTE,
            arguments = listOf(navArgument(AdjustmentModuleDestinations.REQUEST_ID_ARG) {
                type = NavType.StringType // ## CHANGED to StringType ##
            })
        ) { backStackEntry ->
            // Get the request ID
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