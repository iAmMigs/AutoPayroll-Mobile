package com.example.autopayroll_mobile.leaveRequest // Use your package name

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.autopayroll_mobile.R

class TrackLeaveAdapter(private var leaveRequests: List<LeaveRequestItem>) :
    RecyclerView.Adapter<TrackLeaveAdapter.LeaveViewHolder>() {

    class LeaveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leave_request_item, parent, false)
        return LeaveViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        val request = leaveRequests[position]
        holder.dateTextView.text = request.date
        holder.idTextView.text = request.id

        // Set the status text and background based on the enum
        holder.statusTextView.text = request.status.name
        holder.statusTextView.background = getStatusDrawable(request.status, holder.itemView.context)

        // Set an on-click listener for the entire row
        holder.itemView.setOnClickListener {
            // TODO: Handle click event, e.g., navigate to a details fragment
        }
    }

    override fun getItemCount() = leaveRequests.size

    // Helper function to get the correct drawable for each status
    private fun getStatusDrawable(status: LeaveStatus, context: Context) = when (status) {
        LeaveStatus.Pending -> ContextCompat.getDrawable(context, R.drawable.status_chip_pending)
        LeaveStatus.Revision -> ContextCompat.getDrawable(context, R.drawable.status_chip_revision)
        LeaveStatus.Rejected -> ContextCompat.getDrawable(context, R.drawable.status_chip_rejected)
        LeaveStatus.Approved -> ContextCompat.getDrawable(context, R.drawable.status_chip_approved)
    }

    // Function to update the list when filtering
    fun filterList(filteredList: List<LeaveRequestItem>) {
        leaveRequests = filteredList
        notifyDataSetChanged()
    }
}