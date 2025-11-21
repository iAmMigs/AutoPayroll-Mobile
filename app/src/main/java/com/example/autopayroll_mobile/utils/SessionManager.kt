package com.example.autopayroll_mobile.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("AutoPayrollSession", Context.MODE_PRIVATE)
    companion object {
        const val KEY_EMPLOYEE_ID = "employee_id"
        const val KEY_AUTH_TOKEN = "auth_token"
    }
    fun saveSession(employeeId: String, token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_EMPLOYEE_ID, employeeId)
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }
    fun getEmployeeId(): String? {
        return prefs.getString(KEY_EMPLOYEE_ID, null)
    }
    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
    fun clearSession() {
        prefs.edit {
            clear()
        }
    }
}