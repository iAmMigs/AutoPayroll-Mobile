package com.example.autopayroll_mobile.paysliphandling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.autopayroll_mobile.Payslip
import com.example.autopayroll_mobile.PayslipStatus
import com.example.autopayroll_mobile.R
import java.util.Locale

class PayslipAdapter(private val payslips: List<Payslip>) :
    RecyclerView.Adapter<PayslipAdapter.PayslipViewHolder>() {

    inner class PayslipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateRangeTextView: TextView = itemView.findViewById(R.id.tvDateRange)
        val netAmountTextView: TextView = itemView.findViewById(R.id.tvNetAmount)
        val statusTextView: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayslipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payslip, parent, false)
        return PayslipViewHolder(view)
    }

    override fun getItemCount(): Int = payslips.size

    override fun onBindViewHolder(holder: PayslipViewHolder, position: Int) {
        val payslip = payslips[position]

        holder.dateRangeTextView.text = payslip.dateRange
        holder.netAmountTextView.text = String.format(Locale.US, "Net Amount: %.2f", payslip.netAmount)

        // ✅ Set the status text based on the enum value
        val statusText = when (payslip.status) {
            PayslipStatus.ON_HOLD -> "On Hold"
            else -> payslip.status.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
        holder.statusTextView.text = statusText

        // Set the color for the status
        val colorRes = when (payslip.status) {
            PayslipStatus.PROCESSING -> R.color.status_processing
            PayslipStatus.COMPLETED -> R.color.status_completed
            PayslipStatus.ON_HOLD -> R.color.status_on_hold
        }
        holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))

        // Make each item clickable
        holder.itemView.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "Clicked on payslip for ${payslip.dateRange}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}