package com.example.autopayroll_mobile.creditAdjustment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.R

// An enum to represent the possible credit statuses
enum class CreditStatus {
    Approved,
    Rejected,
    Pending
}

class PayrollCreditFragment : Fragment() {

    private lateinit var latestAdjustmentStatusTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payroll_credit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        val backButton: ImageView = view.findViewById(R.id.backButton)
        val requestFilingCard: CardView = view.findViewById(R.id.cardRequestFiling)
        val trackRequestCard: CardView = view.findViewById(R.id.cardTrackRequest)
        latestAdjustmentStatusTextView = view.findViewById(R.id.latestAdjustmentStatusTextView)

        // Back button navigation
        backButton.setOnClickListener {
            // This will take the user to the previous screen on the back stack
            parentFragmentManager.popBackStack()
        }

        // Placeholder for navigating to the filing screen
        requestFilingCard.setOnClickListener {
            // Create an instance of the fragment to navigate to
            val requestFilingFragment = RequestFilingFragment()

            // Perform the fragment transaction
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment, requestFilingFragment) // IMPORTANT: Use your actual fragment container ID
                addToBackStack(null) // Allows the user to press the back button to return here
                commit()
            }
        }

        // Placeholder for navigating to the tracking screen
        trackRequestCard.setOnClickListener {

            val trackRequestCard: CardView = view.findViewById(R.id.cardTrackRequest)

            trackRequestCard.setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.nav_host_fragment, TrackAdjustmentFragment()) // Use your container ID
                    addToBackStack(null)
                    commit()
                }
            }
        }

        // Set the latest status (you can change this based on your actual data)
        updateLatestAdjustmentStatus(CreditStatus.Pending)
    }

    // This function dynamically updates the status UI
    private fun updateLatestAdjustmentStatus(status: CreditStatus) {
        latestAdjustmentStatusTextView.text = status.name

        val backgroundDrawable = when (status) {
            CreditStatus.Approved -> R.drawable.status_chip_approved
            CreditStatus.Rejected -> R.drawable.status_chip_rejected
            CreditStatus.Pending -> R.drawable.status_chip_pending
        }

        latestAdjustmentStatusTextView.background = ContextCompat.getDrawable(requireContext(), backgroundDrawable)
    }
}