package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.autopayroll_mobile.composableUI.adjustmentModuleUI.AdjustmentModuleScreen
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel
import com.example.autopayroll_mobile.viewmodel.AdjustmentNavigationEvent
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AdjustmentModuleFragment : Fragment() {

    private val viewModel: AdjustmentModuleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AutoPayrollMobileTheme {
                    AdjustmentModuleScreen(
                        viewModel = viewModel,
                        // FIX: Pass the missing 'onBackToMenu' parameter, which handles back navigation from the Hub screen.
                        onBackToMenu = {
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe navigation events from the ViewModel (used for internal back presses from filing/tracking screens)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                if (event is AdjustmentNavigationEvent.NavigateBackToMenu) {
                    findNavController().popBackStack()
                    viewModel.onNavigationHandled()
                }
            }
        }
    }
}