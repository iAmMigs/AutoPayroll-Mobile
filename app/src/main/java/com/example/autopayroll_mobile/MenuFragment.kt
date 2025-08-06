package com.example.autopayroll_mobile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment, this part is perfect.
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    /**
     * This is the new method you should add.
     * It's called right after onCreateView() and is the perfect place
     * to access and modify the views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the listener on the root view of the fragment
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            // Get the insets for the system bars (status bar & navigation bar)
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply the insets as padding to the root view
            v.updatePadding(
                top = insets.top,
                bottom = insets.bottom
            )

            // Return CONSUMED to signal that we've handled the insets
            WindowInsetsCompat.CONSUMED
        }
    }
}