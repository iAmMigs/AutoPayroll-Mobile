package com.example.autopayroll_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.composableUI.DashboardScreen // ## Import your new screen ##

class DashboardFragment : Fragment() {

    // The ViewModel is now automatically created and handled by the DashboardScreen
    // so you don't even need a reference to it here.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create a ComposeView, which is the bridge between Fragments and Compose
        return ComposeView(requireContext()).apply {
            // Set the Compose content for this view
            setContent {
                // You can wrap this in your app's theme if you have one
                // e.g., AutoPayrollTheme { ... }

                // Call your main screen Composable.
                // It will automatically find its own ViewModel.
                DashboardScreen()
            }
        }
    }

}