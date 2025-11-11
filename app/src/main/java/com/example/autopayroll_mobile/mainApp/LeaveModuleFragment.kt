package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
// import androidx.navigation.fragment.findNavController // <-- REMOVED
import com.example.autopayroll_mobile.composableUi.LeaveModuleNavHost
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel

class LeaveModuleFragment : Fragment() {

    // Get the ViewModel shared with the Activity
    private val viewModel: LeaveModuleViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setContent {
                AutoPayrollMobileTheme {
                    // ## FIX: We no longer pass the NavController ##
                    LeaveModuleNavHost(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}