package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy // Import this
import androidx.core.os.bundleOf // Import this
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.autopayroll_mobile.composableUI.announcementUI.AnnouncementScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.AnnouncementViewModel
import com.example.autopayroll_mobile.R

class AnnouncementFragment : Fragment() {

    private val announcementViewModel: AnnouncementViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // ## FIX: Set the composition strategy for proper lifecycle management ##
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setContent {
                AutoPayrollMobileTheme {
                    AnnouncementScreen(
                        viewModel = announcementViewModel,
                        onAnnouncementClicked = { announcementId ->
                            // 1. Create a bundle to pass the argument
                            val bundle = bundleOf("announcementId" to announcementId)

                            // 2. Navigate using the ID of the action
                            findNavController().navigate(
                                R.id.action_announcement_to_detail,
                                bundle
                            )
                        }
                    )
                }
            }
        }
    }
}