package com.example.autopayroll_mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.autopayroll_mobile.databinding.FragmentMenuBinding
import com.example.autopayroll_mobile.mainApp.ProfileFragment
import com.example.autopayroll_mobile.auth.LoginActivity
// import com.example.autopayroll_mobile.leaveRequest.LeaveRequest // (Old import)
import com.example.autopayroll_mobile.mainApp.LeaveModuleFragment
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
        apiService = ApiClient.getClient(requireContext().applicationContext)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Logout Button Listener (UPDATED) ---
        binding.logoutButton.setOnClickListener {

            // Launch a coroutine in the fragment's lifecycle scope
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, ProfileFragment())
                .commit()
        }

        // --- Leave Request Button Listener ---
        binding.btnLeaveRequest.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, LeaveModuleFragment()) // Use our new Fragment
                .commit()
        }

        // --- Payroll Credit Button Listener ---
        binding.btnPayrollCredit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, com.example.autopayroll_mobile.mainApp.AdjustmentModuleFragment())
                .commit()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}