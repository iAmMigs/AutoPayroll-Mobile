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
import com.example.autopayroll_mobile.composableUI.ProfileScreen
import com.example.autopayroll_mobile.viewmodel.ProfileViewModel
import com.example.autopayroll_mobile.viewmodel.ProfileNavigationEvent
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AutoPayrollMobileTheme {
                    ProfileScreen(
                        profileViewModel = viewModel,
                        // FIX: Pass the missing 'onBack' parameter, which handles the header back button.
                        onBack = {
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe navigation events from the ViewModel (used for internal back presses).
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                if (event is ProfileNavigationEvent.NavigateBack) {
                    findNavController().popBackStack()
                    viewModel.onNavigationHandled()
                }
            }
        }
    }
}