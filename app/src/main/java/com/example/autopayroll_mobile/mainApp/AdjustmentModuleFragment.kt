package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.autopayroll_mobile.composableUI.adjustmentModuleUI.AdjustmentModuleScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel

class AdjustmentModuleFragment : Fragment() {

    private val viewModel: AdjustmentModuleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AutoPayrollMobileTheme {
                    AdjustmentModuleScreen(
                        viewModel = viewModel,
                        onBackToMenu = {
                            // This handles the "Back" action from the main Hub screen
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }
}