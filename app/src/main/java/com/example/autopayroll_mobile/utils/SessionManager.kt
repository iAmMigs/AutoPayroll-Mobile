package com.example.autopayroll_mobile.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    // We can use one preferences file for all session data
    private var prefs: SharedPreferences = context.getSharedPreferences("AutoPayrollSession", Context.MODE_PRIVATE)

    companion object {
        const val KEY_EMPLOYEE_ID = "employee_id"
        const val KEY_AUTH_TOKEN = "auth_token" // Add key for the token
    }


    fun saveSession(employeeId: String, token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_EMPLOYEE_ID, employeeId)
        editor.putString(KEY_AUTH_TOKEN, token) // Save the token
        editor.apply()
    }

    fun getEmployeeId(): String? {
        return prefs.getString(KEY_EMPLOYEE_ID, null)
    }


    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }


    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}