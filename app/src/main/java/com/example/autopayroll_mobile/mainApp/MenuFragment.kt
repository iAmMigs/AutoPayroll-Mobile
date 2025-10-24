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

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Logout Button Listener ---
        binding.logoutButton.setOnClickListener {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // --- Profile Button Listener (Using your app's navigation logic) ---
        binding.btnProfile.setOnClickListener {
            // This code now perfectly matches the logic from your NavbarActivity's replaceFragment function.
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, ProfileFragment()) // Uses your container ID
                .commit() // Does not add to the back stack, as requested
        }

        binding.btnLeaveRequest.setOnClickListener {
            // This code now perfectly matches the logic from your NavbarActivity's replaceFragment function.
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, LeaveRequest()) // Uses your container ID
                .commit() // Does not add to the back stack, as requested
        }


        binding.btnPayrollCredit.setOnClickListener {
            // This code now perfectly matches the logic from your NavbarActivity's replaceFragment function.
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, com.example.autopayroll_mobile.creditAdjustment.PayrollCreditFragment()) // Uses your container ID
                .commit() // Does not add to the back stack, as requested
        }

        // TODO: Settings and HELP.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}