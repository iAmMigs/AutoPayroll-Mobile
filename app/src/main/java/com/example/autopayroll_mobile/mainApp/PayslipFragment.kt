package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.autopayroll_mobile.composableUI.PayslipScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.PayslipViewModel

class PayslipFragment : Fragment() {

    private val viewModel: PayslipViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AutoPayrollMobileTheme {
                    PayslipScreen(viewModel = viewModel)
                }
            }
        }
    }
}
