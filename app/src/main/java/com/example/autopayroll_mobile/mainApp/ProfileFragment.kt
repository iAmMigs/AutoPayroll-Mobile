package com.example.autopayroll_mobile.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.databinding.FragmentProfileBinding // Import the generated binding class

class ProfileFragment : Fragment() {

    // This is the recommended way to handle View Binding in Fragments.
    // _binding is nullable and only valid between onCreateView and onDestroyView.
    private var _binding: FragmentProfileBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This is the perfect place to update your UI with data.
        // The views are created and ready to be modified.

        // Example of how you would change the text:
        // binding.textViewFullName.text = "A New User's Name"
        // binding.textViewEmail.text = "newuser@email.com"

        // TODO: Add your logic here to fetch user data and call a function to update the UI.
        loadUserData()
    }

    private fun loadUserData() {
        // In the future, you'll get user data from a database or API here.
        // For now, we can use the placeholder data.
        val userFullName = "Marc Jurell Afable"
        val userEmail = "jurellAfable@gmail.com"
        // ... get all other user details

        // Now, update the UI using the binding object.
        // Make sure you have added android:id attributes to your TextViews in the XML.
        binding.textViewFullName.text = userFullName
        binding.textViewEmail.text = userEmail
        // ... set the text for all other TextViews
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding instance to prevent memory leaks
        _binding = null
    }
}