package com.example.autopayroll_mobile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.autopayroll_mobile.databinding.FragmentMenuBinding
import com.example.autopayroll_mobile.mainApp.ProfileFragment
import com.example.autopayroll_mobile.auth.LoginActivity
import com.example.autopayroll_mobile.leaveRequest.LeaveRequest
import com.example.autopayroll_mobile.utils.SessionManager // ## 1. ADD THIS IMPORT ##

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    // ## 2. ADD A VARIABLE FOR THE SESSION MANAGER ##
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)

        // ## 3. INITIALIZE THE SESSION MANAGER ##
        sessionManager = SessionManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Logout Button Listener (UPDATED) ---
        binding.logoutButton.setOnClickListener {
            // ## 4. CLEAR THE SAVED TOKEN AND USER ID ##
            sessionManager.clearSession()

            // This code correctly sends the user back to the login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
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
                .replace(R.id.nav_host_fragment, LeaveRequest())
                .commit()
        }

        // --- Payroll Credit Button Listener ---
        binding.btnPayrollCredit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, com.example.autopayroll_mobile.creditAdjustment.PayrollCreditFragment())
                .commit()
        }

        // TODO: Settings and HELP.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}