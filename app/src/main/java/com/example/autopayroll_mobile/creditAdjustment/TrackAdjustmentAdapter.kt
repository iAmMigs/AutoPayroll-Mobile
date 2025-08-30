package com.example.autopayroll_mobile.creditAdjustment // Use your package name

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.autopayroll_mobile.R

class TrackAdjustmentAdapter(
    private var adjustmentRequests: List<AdjustmentRequestItem>,
    private val onItemClicked: (AdjustmentRequestItem) -> Unit
) : RecyclerView.Adapter<TrackAdjustmentAdapter.AdjustmentViewHolder>() {

    class AdjustmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val typeTextView: TextView = itemView.findViewById(R.id.typeTextView) // Changed from idTextView
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdjustmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adjustment_request_item, parent, false)
        return AdjustmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdjustmentViewHolder, position: Int) {
        val request = adjustmentRequests[position]
        holder.dateTextView.text = request.date
        holder.typeTextView.text = request.type // Bind the 'type' field
        holder.statusTextView.text = request.status.name
        holder.statusTextView.background = getStatusDrawable(request.status, holder.itemView.context)
        holder.itemView.setOnClickListener { onItemClicked(request) }
    }

    override fun getItemCount() = adjustmentRequests.size

    private fun getStatusDrawable(status: CreditStatus, context: Context) = when (status) {
        CreditStatus.Pending -> ContextCompat.getDrawable(context, R.drawable.status_chip_pending)
        CreditStatus.Rejected -> ContextCompat.getDrawable(context, R.drawable.status_chip_rejected)
        CreditStatus.Approved -> ContextCompat.getDrawable(context, R.drawable.status_chip_approved)
    }

    fun filterList(filteredList: List<AdjustmentRequestItem>) {
        adjustmentRequests = filteredList
        notifyDataSetChanged()
    }
}