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

    private lateinit var leaveListRecyclerView: RecyclerView
    private lateinit var trackLeaveAdapter: TrackLeaveAdapter
    private lateinit var backButton: ImageView
    private lateinit var statusFilterButton: MaterialButton

    // Create a master list of all leave requests
    private val allLeaveRequests = listOf(
        LeaveRequestItem("July 7, 2025", "LR0001", LeaveStatus.Pending),
        LeaveRequestItem("July 8, 2025", "LR0002", LeaveStatus.Revision),
        LeaveRequestItem("July 9, 2025", "LR0003", LeaveStatus.Rejected),
        LeaveRequestItem("July 10, 2025", "LR0004", LeaveStatus.Approved),
        LeaveRequestItem("July 11, 2025", "LR0005", LeaveStatus.Approved),
        LeaveRequestItem("July 12, 2025", "LR0006", LeaveStatus.Pending)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_track_leave, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        backButton = view.findViewById(R.id.backButton)
        statusFilterButton = view.findViewById(R.id.statusFilterButton)
        leaveListRecyclerView = view.findViewById(R.id.leaveListRecyclerView)

        // Setup RecyclerView
        leaveListRecyclerView.layoutManager = LinearLayoutManager(context)
        trackLeaveAdapter = TrackLeaveAdapter(allLeaveRequests)
        leaveListRecyclerView.adapter = trackLeaveAdapter

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