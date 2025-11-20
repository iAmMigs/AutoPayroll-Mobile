package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.autopayroll_mobile.composableUI.leaveModuleUI.LeaveModuleNavHost
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel
import com.example.autopayroll_mobile.viewmodel.NavigationEvent
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import kotlinx.coroutines.launch

class LeaveModuleFragment : Fragment() {

    private val viewModel: LeaveModuleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AutoPayrollMobileTheme {
                    LeaveModuleNavHost(
                        viewModel = viewModel,
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

        // Observe navigation events from the ViewModel (used for back presses triggered internally).
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                // Note: The NavigationEvent in LeaveModuleViewModel was updated to include NavigateBackToMenu
                if (event is NavigationEvent.NavigateBackToMenu) {
                    findNavController().popBackStack()
                    viewModel.onNavigationHandled()
                }
            }
        }
    }
}