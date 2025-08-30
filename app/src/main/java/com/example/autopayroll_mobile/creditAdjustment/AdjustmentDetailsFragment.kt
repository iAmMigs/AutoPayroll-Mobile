package com.example.autopayroll_mobile.creditAdjustment // Use your package name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.R

class AdjustmentDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_adjustment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton: ImageView = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // TODO: Use passed-in data to populate the text fields
    }

    companion object {
        fun newInstance(requestId: String): AdjustmentDetailsFragment {
            // This is where you would pass data to the fragment
            return AdjustmentDetailsFragment()
        }
    }
}