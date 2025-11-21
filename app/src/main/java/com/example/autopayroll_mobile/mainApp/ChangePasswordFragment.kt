package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.autopayroll_mobile.composableUI.ChangePasswordScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.ChangePasswordViewModel

class ChangePasswordFragment : Fragment() {

    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AutoPayrollMobileTheme {
                    ChangePasswordScreen(
                        viewModel = viewModel,
                        // Cancel button action: navigate back
                        onBack = {
                            findNavController().popBackStack()
                        },
                        // Reset button action: show confirmation dialog
                        onConfirmReset = {
                            showConfirmationDialog()
                        }
                    )
                }
            }
        }
    }

    /**
     * Shows the confirmation dialog before attempting to reset the password.
     */
    private fun showConfirmationDialog() {
        // First, check if basic client-side validation passes.
        val validationError = viewModel.uiState.value.error
        if (validationError != null) {
            Toast.makeText(requireContext(), validationError, Toast.LENGTH_LONG).show()
            viewModel.clearError() // Clear the error after showing the toast
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Password Reset")
            .setMessage("Are you sure you want to change your password? This action is irreversible.")
            .setPositiveButton("Yes, Reset") { dialog, _ ->
                dialog.dismiss()
                // Proceed with submission (which now includes validation and API call)
                viewModel.validateAndSubmitPasswordChange()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Password reset cancelled.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // Clear inputs when leaving the fragment to Menu/Dashboard etc.
    override fun onStop() {
        super.onStop()
        if (isRemoving || isDetached) {
            viewModel.clearInputs()
        }
    }
}