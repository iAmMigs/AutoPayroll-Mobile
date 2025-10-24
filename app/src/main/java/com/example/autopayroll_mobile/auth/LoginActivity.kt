package com.example.autopayroll_mobile.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log
import android.util.Patterns // Import Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.utils.SessionManager
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class LoginActivity : AppCompatActivity() {

    private lateinit var userLoginInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordButton: Button
    private lateinit var loadingIndicator: ProgressBar

    companion object {
        const val EXTRA_VERIFICATION_REASON = "com.example.autopayroll_mobile.auth.VERIFICATION_REASON"
        const val REASON_FORGOT_PASSWORD = "forgot_password"
        const val REASON_LOGIN_VERIFICATION = "login_verification"
    }

    private val verificationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        // Make sure these IDs match your login_page.xml
        userLoginInput = findViewById(R.id.LoginInput) // Changed from EmailInput
        passwordInput = findViewById(R.id.PassInput)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordButton = findViewById(R.id.forgotPassButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        loginButton.setOnClickListener {
            //handleLogin()


            //skip login
            val intent = Intent(this, VerificationActivity::class.java)
            intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_LOGIN_VERIFICATION)
            verificationLauncher.launch(intent)

        }

        forgotPasswordButton.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun handleLogin() {
        val emailOrUsername = userLoginInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Log the input value before validation
        Log.d("LoginActivity", "Input value: '$emailOrUsername'")

        if (emailOrUsername.isEmpty()) {
            userLoginInput.error = "Email or Username is required"
            userLoginInput.requestFocus()
            return
        }
        // Ensure no Patterns.EMAIL_ADDRESS.matcher check exists here

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            passwordInput.requestFocus()
            return
        }

        authenticateUserOnline(emailOrUsername, password)
    }

    private fun authenticateUserOnline(loginIdentifier: String, pass: String) {
        loadingIndicator.visibility = View.VISIBLE
        loginButton.isEnabled = false

        Thread {
            // --- Request using "identifier" key ---
            val requestBody = JSONObject().apply {
                put("email", loginIdentifier)
                put("password", pass)
            }
            // --------------------------------------

            var loginSuccess = false
            var connectionError = false
            var errorMessage: String? = "Invalid credentials. Please try again."
            var employeeId: String? = null
            // ## ADDED LOGGING VARIABLES ##
            var serverResponseCode = -1
            var serverSuccessResponse: String? = null
            var serverErrorResponse: String? = null
            var exceptionMessage: String? = null

            try {
                val url = URL("https://autopayroll.org/api/employee/login")
                val connection = url.openConnection() as HttpsURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                connection.outputStream.use { os ->
                    val input = requestBody.toString().toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                serverResponseCode = connection.responseCode // Log the code
                if (serverResponseCode == HttpsURLConnection.HTTP_OK) {
                    serverSuccessResponse = connection.inputStream.bufferedReader().use { it.readText() } // Log success response
                    val userFound = JSONObject(serverSuccessResponse)

                    // Make sure "employee_id" is the correct key from your backend
                    employeeId = userFound.optString("employee_id", null)

                    if (employeeId != null) {
                        loginSuccess = true
                    } else {
                        errorMessage = "Login successful, but server did not return an employee ID."
                        connectionError = true
                    }

                } else {
                    connectionError = true
                    serverErrorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } // Log error response
                    if (serverErrorResponse != null) {
                        try { // Try parsing error JSON safely
                            val errorObject = JSONObject(serverErrorResponse)
                            errorMessage = errorObject.optString("message", errorMessage)
                        } catch (jsonE: Exception) {
                            Log.w("LoginActivity", "Failed to parse error response JSON: $serverErrorResponse")
                            // Keep default error message or use raw response
                        }
                    } else {
                        errorMessage = "Server error: $serverResponseCode" // Default if no error body
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                exceptionMessage = e.localizedMessage ?: "Unknown exception" // Log exception
                errorMessage = "A network or data error occurred."
                connectionError = true
            }

            // ## ADD LOGGING BEFORE UI UPDATE ##
            Log.d("LoginActivity", "Network call finished.")
            Log.d("LoginActivity", "Response Code: $serverResponseCode")
            Log.d("LoginActivity", "Success Response Body: $serverSuccessResponse")
            Log.d("LoginActivity", "Error Response Body: $serverErrorResponse")
            Log.d("LoginActivity", "Exception: $exceptionMessage")
            Log.d("LoginActivity", "Final errorMessage: $errorMessage")
            Log.d("LoginActivity", "Final employeeId: $employeeId")
            Log.d("LoginActivity", "Final loginSuccess: $loginSuccess")
            Log.d("LoginActivity", "Final connectionError: $connectionError")

            runOnUiThread {
                loadingIndicator.visibility = View.GONE
                loginButton.isEnabled = true

                if (loginSuccess && employeeId != null) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                    val sessionManager = SessionManager(this)
                    sessionManager.saveSession(employeeId) // No !! needed

                    val intent = Intent(this, VerificationActivity::class.java)
                    intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_LOGIN_VERIFICATION)
                    verificationLauncher.launch(intent)
                } else {
                    if (errorMessage == null) { // Provide a default error if none was set
                        errorMessage = "Login failed. Please check credentials."
                    }
                    // This Toast will display the actual error message
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun handleForgotPassword() {
        val intent = Intent(this, VerificationActivity::class.java)
        intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_FORGOT_PASSWORD)
        startActivity(intent)
    }
}