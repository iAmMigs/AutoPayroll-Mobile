package com.example.autopayroll_mobile.mainApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController // Ensure this is imported
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.databinding.FragmentMenuBinding
import com.example.autopayroll_mobile.auth.LoginActivity
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.network.ApiService
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())
        apiService = ApiClient.getClient(requireContext().applicationContext) // Assuming ApiClient and ApiService are set up correctly

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Logout Button Listener ---
        binding.logoutButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    apiService.logout()
                    Log.d("MenuFragment", "Server logout successful.")
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

        // --- Profile Button Listener ---
        binding.btnProfile.setOnClickListener {
            // Uses the Navigation Component action defined in navigation.xml
            findNavController().navigate(R.id.action_menu_to_profile)
        }

        // --- Leave Request Button Listener ---
        binding.btnLeaveRequest.setOnClickListener {
            // Uses the Navigation Component action defined in navigation.xml
            findNavController().navigate(R.id.action_menu_to_leave)
        }

        // --- Payroll Credit Button Listener ---
        binding.btnPayrollCredit.setOnClickListener {
            // Uses the Navigation Component action defined in navigation.xml
            findNavController().navigate(R.id.action_menu_to_adjustment)
        }

        // --- Navigating to other Compose-based Fragments (example) ---
        // If you have buttons in your menu that should go directly to Dashboard,
        // Announcements, or Payslip, uncomment and use these actions:


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up the binding
    }
}