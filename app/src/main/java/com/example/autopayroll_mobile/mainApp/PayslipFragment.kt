package com.example.autopayroll_mobile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopayroll_mobile.paysliphandling.PayslipAdapter

class PayslipFragment : Fragment(R.layout.fragment_payslip) {

    // Request #4: Prepare properties for dynamic data
    private lateinit var payslipAdapter: PayslipAdapter
    private val payslipList = mutableListOf<Payslip>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPayslips)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        payslipAdapter = PayslipAdapter(payslipList)
        recyclerView.adapter = payslipAdapter

        // Fetch the initial data
        fetchPayslipsFromServer()
    }

    /**
     * Request #4: Placeholder for fetching data. In a real app, you would
     * make a network call here. For now, it loads our hardcoded data.
     */
    private fun fetchPayslipsFromServer() {
        // Request #2: Manually varied hardcoded data
        val newData = listOf(
            Payslip("July 16 - 31, 2025", 5456.15, PayslipStatus.PROCESSING),
            Payslip("July 1 - 15, 2025", 5510.80, PayslipStatus.COMPLETED),
            Payslip("June 16 - 30, 2025", 5320.50, PayslipStatus.COMPLETED),
            Payslip("June 1 - 15, 2025", 5495.00, PayslipStatus.ON_HOLD),
            Payslip("May 16 - 31, 2025", 5100.00, PayslipStatus.COMPLETED),
            Payslip("May 1 - 15, 2025", 5050.25, PayslipStatus.COMPLETED),
            Payslip("April 16 - 31, 2025", 5495.00, PayslipStatus.ON_HOLD),
            Payslip("April 1 - 15, 2025", 5215.00, PayslipStatus.COMPLETED),
            Payslip("March 16 - 31, 2025", 5520.00, PayslipStatus.COMPLETED),
            Payslip("March 1 - 15, 2025", 5015.00, PayslipStatus.COMPLETED)
        )

        // Update the list and notify the adapter
        payslipList.clear()
        payslipList.addAll(newData)
        payslipAdapter.notifyDataSetChanged()
    }
}