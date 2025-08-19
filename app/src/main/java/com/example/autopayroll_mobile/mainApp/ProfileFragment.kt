package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
// Make sure MenuFragment and R are imported correctly
import com.example.autopayroll_mobile.MenuFragment // Assuming MenuFragment is in this package
import com.example.autopayroll_mobile.R // For R.id.your_fragment_container_in_hosting_activity
import com.example.autopayroll_mobile.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            // Use parentFragmentManager to navigate.
            // Replace 'R.id.your_fragment_container_in_hosting_activity'
            // with the actual ID of the container in the Activity that hosts ProfileFragment.
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, MenuFragment())
                // Optional: Add to back stack if you want the system back button
                // to return from MenuFragment to ProfileFragment.
                // .addToBackStack(null)
                .commit()
        }

        loadUserData()
    }

    private fun loadUserData() {
        val userFullName = "Marc Jurell Afable"
        binding.textViewFullName.text = userFullName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}