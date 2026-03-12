package com.example.autopayroll_mobile.mainApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.auth.LoginActivity
import com.example.autopayroll_mobile.composableUI.menuModule.MenuScreen
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.network.ApiService
import com.example.autopayroll_mobile.utils.SessionManager
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.utils.TutorialManager
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sessionManager = SessionManager(requireContext())
        apiService = ApiClient.getClient(requireContext().applicationContext)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AutoPayrollMobileTheme {
                    MenuScreen(
                        onNavigateToProfile = { findNavController().navigate(R.id.action_menu_to_profile) },
                        onNavigateToLeave = { findNavController().navigate(R.id.action_menu_to_leave) },
                        onNavigateToAdjustment = { findNavController().navigate(R.id.action_menu_to_adjustment) },
                        onNavigateToChangePassword = { findNavController().navigate(R.id.action_menu_to_change_password) },

                        onStartTutorial = {
                            // 1. Activate the tutorial state globally
                            TutorialManager.startTutorial()

                            // 2. INSTANTLY force the app back to the Dashboard
                            findNavController().popBackStack(R.id.navigation_dashboard, false)
                        },

                        onLogout = { logoutUser() }
                    )
                }
            }
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            try {
                val response = apiService.logout()
                if (response.isSuccessful) {
                    Log.d("MenuFragment", "Server logout successful.")
                } else {
                    Log.e("MenuFragment", "Server logout failed")
                }
            } catch (e: Exception) {
                Log.e("MenuFragment", "API logout failed, proceeding with local logout", e)
            } finally {
                if (isAdded && activity != null) {
                    sessionManager.clearSession()
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }
    }
}