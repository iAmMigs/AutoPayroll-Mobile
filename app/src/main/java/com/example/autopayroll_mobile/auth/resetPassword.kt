package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Make sure Button is imported
import androidx.appcompat.app.AppCompatActivity
// Remove unused imports if any, like TextWatcher, KeyEvent, View, EditText, TextView, Toast
// if they are not used for other functionality in this specific activity.
import com.example.autopayroll_mobile.R

class resetPassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_reset) // Make sure this matches your XML file name

        // Find the cancel button from your password_reset.xml layout
        val cancelButton: Button = findViewById(R.id.cancelButton) // Ensure this ID matches your XML

        cancelButton.setOnClickListener {
            // Option 1: Just finish this activity if loginActivity is directly below it in the stack.
            // finish()

            // Option 2: Explicitly navigate to loginActivity and clear the task stack above it.
            // This is generally safer if the back stack could be complex.
            val intent = Intent(this, loginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Also finish this activity so it's removed from the back stack
        }

        // --- Add listeners and logic for your password input fields and the "Verify" (or "Save Password") button here ---
        // Example for the other button (assuming it's for saving the new password):
        // val savePasswordButton: Button = findViewById(R.id.verifyButton) // ID from your XML, you named it verifyButton
        // savePasswordButton.setOnClickListener {
        //     // Get new password and confirm password
        //     // Validate them
        //     // If valid, save the password (API call, etc.)
        //     // Then navigate to login or show success message
        //     Toast.makeText(this, "Password reset logic here...", Toast.LENGTH_SHORT).show()
        //     // Example: After successful password change, go to login
        //     // val loginIntent = Intent(this, loginActivity::class.java)
        //     // loginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        //     // startActivity(loginIntent)
        //     // finish()
        // }
    }
}