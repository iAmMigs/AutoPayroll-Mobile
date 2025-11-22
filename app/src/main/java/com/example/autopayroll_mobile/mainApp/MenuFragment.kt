package com.example.autopayroll_mobile.mainApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.auth.LoginActivity
import com.example.autopayroll_mobile.composableUI.menuModule.MenuScreen
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.network.ApiService
import com.example.autopayroll_mobile.utils.SessionManager
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme // Ensure your theme is imported
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    // Note: Since we are moving the UI to Compose, we no longer need the FragmentMenuBinding.
    // private var _binding: FragmentMenuBinding? = null
    // private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize managers
        sessionManager = SessionManager(requireContext())
        apiService = ApiClient.getClient(requireContext().applicationContext)

        // Return the ComposeView hosting the MenuScreen
        return ComposeView(requireContext()).apply {
            setContent {
                AutoPayrollMobileTheme {
                    MenuScreen(
                        onNavigateToProfile = {
                            findNavController().navigate(R.id.action_menu_to_profile)
                        },
                        onNavigateToLeave = {
                            findNavController().navigate(R.id.action_menu_to_leave)
                        },
                        onNavigateToAdjustment = {
                            findNavController().navigate(R.id.action_menu_to_adjustment)
                        },
                        // NEW NAVIGATION
                        onNavigateToChangePassword = {
                            findNavController().navigate(R.id.action_menu_to_change_password)
                        },
                        onLogout = {
                            logoutUser()
                        }
                    )
                }
            }
        }
    }

    // Extracted logout logic into a function
    private fun logoutUser() {
        lifecycleScope.launch {
            try {
                // ## VERIFIED: Calls the API service logout ##
                val response = apiService.logout()
                if (response.isSuccessful) {
                    Log.d("MenuFragment", "Server logout successful.")
                } else {
                    Log.e("MenuFragment", "Server logout failed with code: ${response.code()}, message: ${response.errorBody()?.string()}")
                    // Optionally, show a toast to the user about the server logout failure
                }
            } catch (e: Exception) {
                Log.e("MenuFragment", "API logout failed, proceeding with local logout", e)
            } finally {
                // Always clear local session and navigate to login, regardless of server logout success
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

    override fun onDestroyView() {
        super.onDestroyView()
    }

    // Note: onViewCreated is no longer overridden as the click listeners are now in Compose.
}