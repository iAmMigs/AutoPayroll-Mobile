// In file: com/example/autopayroll_mobile/DashboardFragment.kt
package com.example.autopayroll_mobile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.autopayroll_mobile.utils.SessionManager
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DashboardFragment : Fragment() {

    private lateinit var empNameTextView: TextView
    private lateinit var empIDTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        empNameTextView = view.findViewById(R.id.EmpName)
        empIDTextView = view.findViewById(R.id.EmpID)

        // 1. Get the session manager
        val sessionManager = SessionManager(requireActivity())

        // 2. Get the logged-in user's ID
        val employeeId = sessionManager.getEmployeeId()

        if (employeeId != null) {
            // 3. If ID exists, fetch this user's details
            fetchEmployeeData(employeeId)
        } else {
            // This case shouldn't happen, but good to have
            empNameTextView.text = "Error: Not logged in"
            empIDTextView.text = "N/A"
            // You might want to navigate back to LoginActivity here
        }
    }

    /**
     * Fetches the full employee details from the server using their ID.
     */
    private fun fetchEmployeeData(employeeId: String) {
        // Set loading text
        empNameTextView.text = "Loading..."
        empIDTextView.text = employeeId

        Thread {
            try {
                // 4. Use the new endpoint: GET /api/employees/{id}
                val url = URL("https://autopayroll.org/api/employees/$employeeId")
                val connection = url.openConnection() as HttpsURLConnection

                // A GET request is the default, so we just check the response
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val userObject = JSONObject(response)

                    // 5. Parse the user's details
                    val firstName = userObject.getString("first_name")
                    val lastName = userObject.getString("last_name")

                    // 6. Update the UI on the main thread
                    activity?.runOnUiThread {
                        empNameTextView.text = "$firstName $lastName"
                        empIDTextView.text = employeeId
                    }
                } else {
                    activity?.runOnUiThread {
                        empNameTextView.text = "Error loading data"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    empNameTextView.text = "Network error"
                }
            }
        }.start()
    }
}