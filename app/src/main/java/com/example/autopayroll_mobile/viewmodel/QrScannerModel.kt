package com.example.autopayroll_mobile.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.ClockInOutRequest
import com.example.autopayroll_mobile.data.model.QRScanData
import com.example.autopayroll_mobile.network.ApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. DEFINE THE UI STATE
// This data class represents everything the UI needs to know.
data class QrScannerUiState(
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val scannedQrData: QRScanData? = null,
    val currentLocation: Location? = null,
    val isLocationEnabled: Boolean = true // Assume true until checked
) {
    // This is a "derived" state. The UI can just check this boolean
    // instead of doing the "scannedQrData != null && currentLocation != null" logic.
    val isReadyForClockIn: Boolean
        get() = scannedQrData != null && currentLocation != null && !isLoading
}


// 2. CREATE THE VIEWMODEL
// We use AndroidViewModel to get the Application Context for location services.
class QrScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(QrScannerUiState())
    val uiState: StateFlow<QrScannerUiState> = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private var isRequestingLocationUpdates = false

    // This callback updates our UI state directly.
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                Log.d("QrScannerViewModel", "Location acquired: ${location.latitude}, ${location.longitude}")
                _uiState.update { it.copy(currentLocation = location) }
            }
        }
    }

    init {
        checkLocationSetting()
    }

    // --- Event Handlers ---

    fun onQrCodeScanned(qrData: QRScanData) {
        _uiState.update {
            it.copy(
                scannedQrData = qrData,
                statusMessage = "QR Code Scanned!"
            )
        }
    }

    fun onClockInClicked() {
        submitAttendance("clock-in")
    }

    fun onClockOutClicked() {
        submitAttendance("clock-out")
    }

    fun checkLocationSetting() {
        val locationManager = getApplication<Application>().getSystemService(Application.LOCATION_SERVICE) as LocationManager
        val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        _uiState.update { it.copy(isLocationEnabled = enabled) }
    }

    // --- Private Logic ---

    private fun submitAttendance(action: String) {
        val currentState = _uiState.value
        if (currentState.scannedQrData == null || currentState.currentLocation == null) {
            showToast("QR Code and location are required.")
            return
        }

        // 1. Set loading state
        _uiState.update { it.copy(isLoading = true, statusMessage = "Submitting $action...") }

        // Build the request object
        val request = ClockInOutRequest(
            companyId = currentState.scannedQrData.company_id,
            token = currentState.scannedQrData.token,
            signature = currentState.scannedQrData.signature,
            latitude = currentState.currentLocation.latitude,
            longitude = currentState.currentLocation.longitude
        )

        val apiService = ApiClient.getClient(getApplication())

        // 2. Launch API call in Coroutine
        viewModelScope.launch {
            try {
                val response = if (action == "clock-in") {
                    apiService.clockIn(request)
                } else {
                    apiService.clockOut(request)
                }

                showToast(response.message)
                resetScannerState() // Success

            } catch (e: Exception) {
                Log.e("QrScannerViewModel", "API call failed", e)
                showToast("Error: ${e.message}")
                // 3. Handle failure (allow user to try again)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "Error. Please try again."
                    )
                }
            }
        }
    }

    // Resets the state for the next scan
    fun resetScannerState() {
        _uiState.update {
            it.copy(
                isLoading = false,
                statusMessage = null,
                scannedQrData = null,
                currentLocation = null // Clear location to wait for a fresh one
            )
        }
        // Location updates are still running, so a new location will arrive soon.
    }

    @SuppressLint("MissingPermission") // Permissions are checked in the Composable
    fun startLocationUpdates() {
        if (isRequestingLocationUpdates) return

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        isRequestingLocationUpdates = true
        Log.d("QrScannerViewModel", "Started location updates")
    }

    fun stopLocationUpdates() {
        if (!isRequestingLocationUpdates) return

        fusedLocationClient.removeLocationUpdates(locationCallback)
        isRequestingLocationUpdates = false
        Log.d("QrScannerViewModel", "Stopped location updates")
    }

    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}