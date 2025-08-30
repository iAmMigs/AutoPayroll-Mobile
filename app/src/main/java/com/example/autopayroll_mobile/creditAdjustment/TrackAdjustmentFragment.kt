package com.example.autopayroll_mobile.creditAdjustment // Use your package name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopayroll_mobile.R
import com.google.android.material.button.MaterialButton

class TrackAdjustmentFragment : Fragment() {

    private lateinit var trackAdjustmentAdapter: TrackAdjustmentAdapter

    // Create a master list of all adjustment requests
    private val allAdjustmentRequests = listOf(
        AdjustmentRequestItem("July 7, 2025", "Leave", CreditStatus.Pending),
        AdjustmentRequestItem("July 7, 2025", "Attendance", CreditStatus.Rejected),
        AdjustmentRequestItem("July 7, 2025", "Leave", CreditStatus.Approved),
        AdjustmentRequestItem("July 6, 2025", "Holiday", CreditStatus.Approved),
        AdjustmentRequestItem("July 5, 2025", "Attendance", CreditStatus.Pending)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_track_adjustment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        val backButton: ImageView = view.findViewById(R.id.backButton)
        val statusFilterButton: MaterialButton = view.findViewById(R.id.statusFilterButton)
        val adjustmentListRecyclerView: RecyclerView = view.findViewById(R.id.adjustmentListRecyclerView)

        // Setup RecyclerView
        adjustmentListRecyclerView.layoutManager = LinearLayoutManager(context)
        trackAdjustmentAdapter = TrackAdjustmentAdapter(allAdjustmentRequests) { clickedRequest ->
            val detailsFragment = AdjustmentDetailsFragment.newInstance(clickedRequest.type) // Pass data here
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment, detailsFragment)
                addToBackStack(null)
                commit()
            }
        }
        adjustmentListRecyclerView.adapter = trackAdjustmentAdapter

        // Setup Listeners
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        statusFilterButton.setOnClickListener {
            showFilterMenu(it)
        }
    }

    private fun showFilterMenu(anchor: View) {
        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.credit_status_filter_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            val filteredList = when (item.itemId) {
                R.id.filter_pending -> allAdjustmentRequests.filter { it.status == CreditStatus.Pending }
                R.id.filter_rejected -> allAdjustmentRequests.filter { it.status == CreditStatus.Rejected }
                R.id.filter_approved -> allAdjustmentRequests.filter { it.status == CreditStatus.Approved }
                else -> allAdjustmentRequests // Show all
            }
            trackAdjustmentAdapter.filterList(filteredList)
            true
        }
        popup.show()
    }
}