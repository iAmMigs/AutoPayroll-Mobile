package com.example.autopayroll_mobile.leaveRequest // Use your package name

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.autopayroll_mobile.R

// MODIFIED: The constructor now accepts a function to handle clicks
class TrackLeaveAdapter(
    private var leaveRequests: List<LeaveRequestItem>,
    private val onItemClicked: (LeaveRequestItem) -> Unit
) : RecyclerView.Adapter<TrackLeaveAdapter.LeaveViewHolder>() {

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

        holder.statusTextView.text = request.status.name
        holder.statusTextView.background = getStatusDrawable(request.status, holder.itemView.context)

        // The click listener now calls the function that was passed in
        holder.itemView.setOnClickListener {
            onItemClicked(request)
        }
    }

    override fun getItemCount() = leaveRequests.size

    private fun getStatusDrawable(status: LeaveStatus, context: Context) = when (status) {
        LeaveStatus.Pending -> ContextCompat.getDrawable(context, R.drawable.status_chip_pending)
        LeaveStatus.Revision -> ContextCompat.getDrawable(context, R.drawable.status_chip_revision)
        LeaveStatus.Rejected -> ContextCompat.getDrawable(context, R.drawable.status_chip_rejected)
        LeaveStatus.Approved -> ContextCompat.getDrawable(context, R.drawable.status_chip_approved)
    }

    fun filterList(filteredList: List<LeaveRequestItem>) {
        leaveRequests = filteredList
        notifyDataSetChanged()
    }
}