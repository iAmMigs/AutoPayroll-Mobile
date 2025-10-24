// In file: com/example/autopayroll_mobile/utils/SessionManager.kt
package com.example.autopayroll_mobile.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("AutoPayrollApp", Context.MODE_PRIVATE)

    companion object {
        const val KEY_EMPLOYEE_ID = "employee_id"
    }


    // Saves just the user's ID to start a session.
    fun saveSession(employeeId: String) {
        val editor = prefs.edit()
        editor.putString(KEY_EMPLOYEE_ID, employeeId)
        editor.apply()
    }

    // Retrieves the logged-in user's ID
    fun getEmployeeId(): String? {
        return prefs.getString(KEY_EMPLOYEE_ID, null)
    }

    // Clears the session (for logout).
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}