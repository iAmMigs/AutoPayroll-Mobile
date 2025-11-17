package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy // Import this
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.autopayroll_mobile.composableUI.PayslipScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.PayslipViewModel

/**
 * This is the host Fragment for your Payslip Composable UI.
 */
class PayslipFragment : Fragment() {

    private val viewModel: PayslipViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // ## FIX: Set the composition strategy for proper lifecycle management ##
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                AutoPayrollMobileTheme {
                    PayslipScreen(
                        viewModel = viewModel,
                        // FIX: Pass the required 'onBack' lambda.
                        // This uses the Fragment Manager's popBackStack for simple back navigation.
                        onBack = {
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                    )
                }
            }
        }
    }
}