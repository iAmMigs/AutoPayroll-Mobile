package com.example.autopayroll_mobile.mainApp // ## FIX: Correct package name ##

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.autopayroll_mobile.AnnouncementDetailHostFragmentArgs // ## FIX: Import the generated Args class ##
import com.example.autopayroll_mobile.composableUi.AnnouncementDetailScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme

// ## FIX: Class name must start with an uppercase 'A' ##
class AnnouncementDetailHostFragment : Fragment() {

    // This line will now work
    private val args: AnnouncementDetailHostFragmentArgs by navArgs()

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
                    AnnouncementDetailScreen(
                        navController = findNavController(),
                        // This line will also work now
                        announcementId = args.announcementId
                    )
                }
            }
        }
    }
}