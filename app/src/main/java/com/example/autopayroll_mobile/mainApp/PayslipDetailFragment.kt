package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.autopayroll_mobile.composableUI.BreakdownData
import com.example.autopayroll_mobile.composableUI.PayslipDetailScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.PayslipViewModel
import com.google.gson.Gson

class PayslipDetailFragment : Fragment() {

    // Share the exact same ViewModel instance as PayslipFragment
    private val viewModel: PayslipViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                AutoPayrollMobileTheme {
                    // Properly observe the state
                    val currentPayslip by viewModel.selectedPayslip.collectAsState()

                    // The .let block fixes the compiler's type-inference confusion
                    currentPayslip?.let { payslip ->
                        val gson = Gson()
                        val breakdownObj = try {
                            if (!payslip.breakdown.isNullOrBlank()) {
                                gson.fromJson(payslip.breakdown, BreakdownData::class.java)
                            } else {
                                BreakdownData()
                            }
                        } catch (e: Exception) {
                            BreakdownData()
                        }

                        PayslipDetailScreen(
                            payslip = payslip,
                            breakdown = breakdownObj,
                            onBack = { findNavController().popBackStack() }
                        )
                    }
                }
            }
        }
    }
}