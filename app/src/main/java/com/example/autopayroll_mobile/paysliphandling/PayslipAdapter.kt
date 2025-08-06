package com.example.autopayroll_mobile.paysliphandling

// Imports from Android
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

// Imports from your project
import com.example.autopayroll_mobile.Payslip
import com.example.autopayroll_mobile.PayslipStatus
import com.example.autopayroll_mobile.R

class PayslipAdapter(private val payslips: List<Payslip>) :
    RecyclerView.Adapter<PayslipAdapter.PayslipViewHolder>() {

    // The ViewHolder holds the views for a single item.
    inner class PayslipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // These R.id references now work because of the import above
        val dateRangeTextView: TextView = itemView.findViewById(R.id.tvDateRange)
        val netAmountTextView: TextView = itemView.findViewById(R.id.tvNetAmount)
        val statusTextView: TextView = itemView.findViewById(R.id.tvStatus)
    }

    // Called when RecyclerView needs a new ViewHolder (a new item row).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayslipViewHolder {
        // This R.layout reference now works
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payslip, parent, false)
        return PayslipViewHolder(view)
    }

    // Returns the total number of items in the list.
    override fun getItemCount(): Int = payslips.size

    // Called to display the data at a specific position.
    override fun onBindViewHolder(holder: PayslipViewHolder, position: Int) {
        val payslip = payslips[position]

        // Bind data to the views
        holder.dateRangeTextView.text = payslip.dateRange
        holder.netAmountTextView.text = String.format(Locale.US, "Net Amount: %.2f", payslip.netAmount)
        holder.statusTextView.text = payslip.status.name.lowercase().replaceFirstChar { it.titlecase() }

        // These R.color references now work
        val colorRes = when (payslip.status) {
            PayslipStatus.PROCESSING -> R.color.status_processing
            PayslipStatus.COMPLETED -> R.color.status_completed
        }
        holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))
    }
}