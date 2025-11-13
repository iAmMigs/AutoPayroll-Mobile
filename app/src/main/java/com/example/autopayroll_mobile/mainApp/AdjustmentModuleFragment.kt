package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel
import com.example.autopayroll_mobile.composableUI.AdjustmentModuleScreen

/**
 * A "host" Fragment that replaces the old XML-based fragments.
 * Its only job is to host our Jetpack Compose UI.
 */
class AdjustmentModuleFragment : Fragment() {

    // Initialize the ViewModel. It will be shared by all Composable screens
    // in this module.
    private val viewModel: AdjustmentModuleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // Apply your MaterialTheme
                AutoPayrollMobileTheme {
                    // Call our main Composable screen, passing in the ViewModel
                    AdjustmentModuleScreen(viewModel = viewModel)
                }
            }
        }
    }
}