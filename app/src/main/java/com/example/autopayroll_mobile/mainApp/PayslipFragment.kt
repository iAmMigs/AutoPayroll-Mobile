package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.composableUI.PayslipScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.PayslipViewModel

class PayslipFragment : Fragment() {

    private val viewModel: PayslipViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                AutoPayrollMobileTheme {
                    PayslipScreen(
                        viewModel = viewModel,
                        onViewDetails = { clickedPayslip ->
                            viewModel.selectPayslip(clickedPayslip)
                            findNavController().navigate(R.id.action_payslip_to_detail)
                        },
                        onBack = {
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }
}