package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText // Added for password fields
import android.widget.Toast    // Added for user feedback
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.R

class ResetPassword : AppCompatActivity() {

    // Declare EditText fields as properties if you need to access them in multiple methods
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_reset)

        // Initialize EditText fields
        newPasswordInput = findViewById(R.id.newpasswordInput)
        confirmPasswordInput = findViewById(R.id.confirmpasswordInput)

        // Find the cancel button
        val cancelButton: Button = findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Find the confirm button
        val confirmButton: Button = findViewById(R.id.confirmButton) // <--- ADD THIS LINE
        confirmButton.setOnClickListener {
            // Implement your password reset logic here
            handleConfirmNewPassword()
        }
    }

    private fun handleConfirmNewPassword() {
        val newPassword = newPasswordInput.text.toString().trim()
        val confirmedPassword = confirmPasswordInput.text.toString().trim()

        // 1. Validate inputs

        // Clear previous errors first to ensure fresh validation state
        newPasswordInput.error = null
        confirmPasswordInput.error = null

        if (newPassword.isEmpty()) {
            newPasswordInput.error = "New password cannot be empty"
            newPasswordInput.requestFocus()
            return
        }

        if (newPassword.length < 6) { // Example: minimum length
            newPasswordInput.error = "Password must be at least 6 characters"
            newPasswordInput.requestFocus()
            return
        }

        if (confirmedPassword.isEmpty()) {
            confirmPasswordInput.error = "Please confirm your password"
            confirmPasswordInput.requestFocus()
            return
        }

        if (newPassword != confirmedPassword) {
            // Set the error on the confirmation field
            confirmPasswordInput.error = "Passwords do not match"
            // Request focus to make sure the error is visible and the user is directed there
            confirmPasswordInput.requestFocus()
            // DO NOT clear the text here immediately.
            // Let the user see their input and the error.
            // They will likely edit it, which will clear the error automatically,
            // or you can clear it if they attempt to submit again.
            return
        }

        // 2. If all validations pass, proceed with password reset
        Toast.makeText(this, "Password reset successfully (simulated)", Toast.LENGTH_LONG).show()

        // 3. Navigate back to login screen after successful password reset
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish() // Finish this activity
    }
}