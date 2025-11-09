package com.example.autopayroll_mobile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.autopayroll_mobile.data.model.Company // Import Company
import com.example.autopayroll_mobile.data.model.Employee // Import Employee
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private lateinit var empNameTextView: TextView
    private lateinit var empIDTextView: TextView
    private lateinit var empJobPositionTextView: TextView // The TextView for the job/company

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize all your TextViews
        empNameTextView = view.findViewById(R.id.EmpName)
        empIDTextView = view.findViewById(R.id.EmpID)
        empJobPositionTextView = view.findViewById(R.id.empJobPosition) // Initialize the job TextView

        val sessionManager = SessionManager(requireActivity())
        val employeeId = sessionManager.getEmployeeId()

        if (employeeId != null) {
            // If the user is logged in, start fetching their data
            fetchEmployeeAndCompanyData(employeeId)
        } else {
            // Handle the case where the user is not logged in
            empNameTextView.text = "Error: Not logged in"
            empIDTextView.text = "N/A"
            empJobPositionTextView.text = "Please log in"
        }
    }

    /**
     * Fetches employee data first, then uses that data to fetch company data.
     */
    private fun fetchEmployeeAndCompanyData(employeeId: String) {
        // Set initial loading text
        empNameTextView.text = "Loading..."
        empIDTextView.text = employeeId
        empJobPositionTextView.text = "Loading..."

        val apiService = ApiClient.getClient(requireContext())

        // Start a coroutine for the background network calls
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // --- First API Call: Get Employee Data ---
                val employee = apiService.getEmployeeProfile(employeeId)

                // Update UI with the data we have so far
                empNameTextView.text = "${employee.firstName} ${employee.lastName}"
                empIDTextView.text = employee.employeeId

                val jobPosition = employee.jobPosition
                empJobPositionTextView.text = "$jobPosition • Loading company..." // Placeholder

                // --- Second API Call: Get Company Data ---
                try {
                    val company = apiService.getCompany(employee.companyId)
                    val companyName = company.companyName

                    // Final UI Update with both pieces of data
                    empJobPositionTextView.text = "$jobPosition • $companyName"

                } catch (companyError: Exception) {
                    // Handle error if *only* the company fetch fails
                    Log.e("DashboardFragment", "Error fetching company data", companyError)
                    empJobPositionTextView.text = "$jobPosition • Unknown Company"
                }

            } catch (employeeError: Exception) {
                // Handle error if the *first* (employee) fetch fails
                employeeError.printStackTrace()
                Log.e("DashboardFragment", "Error fetching employee data", employeeError)
                empNameTextView.text = "Error loading data"
                empIDTextView.text = "N/A"
                empJobPositionTextView.text = "Error"
            }
        }
    }
}