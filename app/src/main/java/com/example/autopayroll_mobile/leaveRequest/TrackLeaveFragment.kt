package com.example.autopayroll_mobile.leaveRequest // Use your package name

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

class TrackLeaveFragment : Fragment() {

    private lateinit var trackLeaveAdapter: TrackLeaveAdapter

    // The data list now includes the extra details for the preview screen
    private val allLeaveRequests = listOf(
        LeaveRequestItem("June 15 - 20, 2025", "LR0001", LeaveStatus.Approved, "Bereavement Leave", "5 days", "Leave approved. Credits deducted."),
        LeaveRequestItem("July 8, 2025", "LR0002", LeaveStatus.Revision, "Sick Leave", "1 day", "Please provide a medical certificate."),
        LeaveRequestItem("July 9, 2025", "LR0003", LeaveStatus.Rejected, "Vacation Leave", "3 days", "Request conflicts with a major project deadline."),
        LeaveRequestItem("July 10, 2025", "LR0004", LeaveStatus.Approved, "Official Business", "1 day", "Approved."),
        LeaveRequestItem("July 12, 2025", "LR0006", LeaveStatus.Pending, "Vacation Leave", "2 days", "Pending supervisor approval.")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_track_leave, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton: ImageView = view.findViewById(R.id.backButton)
        val statusFilterButton: MaterialButton = view.findViewById(R.id.statusFilterButton)
        val leaveListRecyclerView: RecyclerView = view.findViewById(R.id.leaveListRecyclerView)

        leaveListRecyclerView.layoutManager = LinearLayoutManager(context)

        // Correctly create the adapter, passing the list and the click handler logic
        trackLeaveAdapter = TrackLeaveAdapter(allLeaveRequests) { clickedRequest ->
            val detailsFragment = LeaveDetailsFragment.newInstance(clickedRequest)

            parentFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment, detailsFragment)
                addToBackStack(null)
                commit()
            }
        }
        leaveListRecyclerView.adapter = trackLeaveAdapter

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        statusFilterButton.setOnClickListener {
            showFilterMenu(it)
        }
    }

    // This function was missing
    private fun showFilterMenu(anchor: View) {
        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.status_filter_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            val filteredList = when (item.itemId) {
                R.id.filter_pending -> allLeaveRequests.filter { it.status == LeaveStatus.Pending }
                R.id.filter_revision -> allLeaveRequests.filter { it.status == LeaveStatus.Revision }
                R.id.filter_rejected -> allLeaveRequests.filter { it.status == LeaveStatus.Rejected }
                R.id.filter_approved -> allLeaveRequests.filter { it.status == LeaveStatus.Approved }
                else -> allLeaveRequests // Show all
            }
            trackLeaveAdapter.filterList(filteredList)
            true
        }
        popup.show()
    }
}