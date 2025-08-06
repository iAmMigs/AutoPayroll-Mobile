package com.example.autopayroll_mobile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopayroll_mobile.paysliphandling.PayslipAdapter

class PayslipFragment : Fragment(R.layout.fragment_payslip) { // Use your layout file here

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Find the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPayslips)

        // 2. Create your data list
        val payslipData = listOf(
            Payslip("July 16 - 31, 2025", 5456.15, PayslipStatus.PROCESSING),
            Payslip("July 1 - 15, 2025", 5456.15, PayslipStatus.COMPLETED),
            Payslip("June 16 - 30, 2025", 5320.50, PayslipStatus.COMPLETED),
            Payslip("June 1 - 15, 2025", 5456.15, PayslipStatus.COMPLETED),
            Payslip("May 16 - 31, 2025", 5100.00, PayslipStatus.COMPLETED),
            Payslip("May 16 - 31, 2025", 5100.00, PayslipStatus.COMPLETED),
            Payslip("May 16 - 31, 2025", 5100.00, PayslipStatus.COMPLETED),
            Payslip("May 16 - 31, 2025", 5100.00, PayslipStatus.COMPLETED),
            Payslip("May 16 - 31, 2025", 5100.00, PayslipStatus.COMPLETED),
            Payslip("May 16 - 31, 2025", 5100.00, PayslipStatus.COMPLETED)
            // Add as many as you want here!
        )

        // 3. Create and set the adapter
        val adapter = PayslipAdapter(payslipData)
        recyclerView.adapter = adapter

        // 4. Set a Layout Manager (this is required)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}