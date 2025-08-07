package com.example.autopayroll_mobile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.autopayroll_mobile.auth.LoginActivity

class MenuFragment : Fragment(R.layout.fragment_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the logout button from your XML layout
        val logoutButton: Button = view.findViewById(R.id.logoutButton)
        // You can find your other menu buttons here as well...

        // Set the click listener for the Logout button
        logoutButton.setOnClickListener {
            // Create an Intent to start the LoginActivity
            val intent = Intent(requireActivity(), LoginActivity::class.java)

            // These flags are important. They clear the activity history
            // so the user can't press the back button to get into the app again.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Start the LoginActivity
            startActivity(intent)

            // Finish the current activity that is hosting this fragment
            requireActivity().finish()
        }

        // You can set placeholder listeners for your other buttons here
        val profileButton: Button = view.findViewById(R.id.btnProfile)
        profileButton.setOnClickListener {
            Toast.makeText(requireContext(), "Profile Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}