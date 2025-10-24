package com.example.autopayroll_mobile.mainApp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.autopayroll_mobile.databinding.FragmentQrScannerBinding
import com.example.autopayroll_mobile.utils.SessionManager // Import your SessionManager
import com.google.android.gms.location.*
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class QrScannerFragment : Fragment() {

    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isRequestingLocationUpdates = false
    private var scannedQrValue: String? = null // Store scanned QR value temporarily

    // Activity Result Launchers
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                checkLocationPermissionAndStart() // Check location perm next
            } else {
                showToast("Camera permission is required.")
            }
        }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                checkLocationEnabledAndStart() // Check if location is ON
            } else {
                showToast("Location permission is required for verification.")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Start permission checks
        checkCameraPermission()

        // Initialize location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                stopLocationUpdates() // Stop updates once we get one location
                val location = locationResult.lastLocation
                if (location != null && scannedQrValue != null) {
                    Log.d("QrScannerFragment", "Location acquired: ${location.latitude}, ${location.longitude}")
                    submitScanToServer(scannedQrValue!!, location.latitude, location.longitude)
                } else {
                    showToast("Failed to get location.")
                    hideLoading() // Hide loading if location fails
                    // Optionally restart camera here if needed, or prompt user
                    startCamera() // Restart scanning
                }
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkLocationPermissionAndStart() // Camera granted, check location
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionRationaleDialog("Camera access is needed to scan QR codes.") {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkLocationPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkLocationEnabledAndStart() // Location permission granted, check if enabled
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionRationaleDialog("Precise location access is needed to verify your scan location.") {
                    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun checkLocationEnabledAndStart() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            startCamera() // All checks passed, start camera
        } else {
            // Location is disabled, prompt user to enable it
            AlertDialog.Builder(requireContext())
                .setMessage("Location services are disabled. Please enable location to proceed.")
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                    // User needs to manually return to the app after enabling
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    showToast("Location must be enabled to verify scans.")
                    // Handle cancellation (e.g., disable scan functionality or close fragment)
                }
                .show()
        }
    }

    @SuppressLint("MissingPermission") // Permissions are checked before calling
    private fun startLocationUpdates() {
        if (!isRequestingLocationUpdates) {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 5000 // Update interval in milliseconds (e.g., 5 seconds)
                fastestInterval = 2000 // Fastest update interval
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper() /* Looper */
            )
            isRequestingLocationUpdates = true
            Log.d("QrScannerFragment", "Started location updates")
        }
    }

    private fun stopLocationUpdates() {
        if (isRequestingLocationUpdates) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isRequestingLocationUpdates = false
            Log.d("QrScannerFragment", "Stopped location updates")
        }
    }


    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        // Clear any previous status message
        binding.statusTextView.visibility = View.GONE

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        processImage(imageProxy)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalysis)
                Log.d("QrScannerFragment", "Camera started successfully.")
            } catch (exc: Exception) {
                Log.e("QrScannerFragment", "Use case binding failed", exc)
                showToast("Failed to start camera.")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @ExperimentalGetImage // Annotation is now here
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty() && scannedQrValue == null) { // Process only if not already processing a scan
                        val barcode = barcodes.first()
                        scannedQrValue = barcode.rawValue // Store the value
                        Log.d("QrScannerFragment", "QR Code detected: $scannedQrValue")

                        // Stop analysis and camera on the main thread
                        activity?.runOnUiThread {
                            showLoading("Verifying location...")
                            cameraProviderFuture.get().unbindAll() // Stop camera feed
                            startLocationUpdates() // Get location after stopping camera
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QrScannerFragment", "Barcode scanning failed", it)
                    // Keep scanning on failure
                }
                .addOnCompleteListener {
                    imageProxy.close() // Close the imageProxy in all cases
                }
        } else {
            imageProxy.close() // Ensure proxy is closed if mediaImage is null
        }
    }

    private fun submitScanToServer(qrData: String, latitude: Double, longitude: Double) {
        val sessionManager = SessionManager(requireContext())
        val employeeId = sessionManager.getEmployeeId()

        if (employeeId == null) {
            showToast("Error: User not logged in.")
            hideLoading()
            // Redirect to login?
            return
        }

        // Show loading state
        showLoading("Submitting scan...")

        Thread {
            var success = false
            var message = "An error occurred."

            try {
                val url = URL("https://autopayroll.org/api/attendance/scan") // Use your actual URL
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                val jsonObject = JSONObject().apply {
                    put("employee_id", employeeId)
                    put("qr_data", qrData)
                    put("latitude", latitude)
                    put("longitude", longitude)
                }
                val jsonInputString = jsonObject.toString()

                connection.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val resultJson = JSONObject(response)
                    success = resultJson.optString("status") == "success"
                    message = resultJson.optString("message", "Scan submitted.")
                } else {
                    // Try reading error response
                    val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    message = try {
                        JSONObject(errorStream ?: "{}").optString("message", "Server error: $responseCode")
                    } catch (e: Exception) {
                        "Server error: $responseCode"
                    }
                }
            } catch (e: Exception) {
                Log.e("QrScannerFragment", "API call failed", e)
                message = "Network error or invalid server response."
            }

            // Update UI on main thread
            activity?.runOnUiThread {
                hideLoading()
                showToast(message) // Show server message
                scannedQrValue = null // Reset scanned value
                if(success) {
                    // Maybe navigate away or show a success screen
                } else {
                    // Optionally restart camera on failure
                    startCamera()
                }
            }
        }.start()
    }

    private fun showPermissionRationaleDialog(message: String, positiveAction: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Grant") { _, _ -> positiveAction() }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                showToast("Permission denied. Feature unavailable.")
            }
            .show()
    }

    private fun showLoading(message: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.statusTextView.text = message
        binding.statusTextView.visibility = View.VISIBLE
        // Disable any buttons if you add them
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.statusTextView.visibility = View.GONE
        // Enable any buttons
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    override fun onPause() {
        super.onPause()
        stopLocationUpdates() // Stop location updates when the fragment is not visible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        try {
            // It's good practice to unbind explicitly on destroy
            cameraProviderFuture.get()?.unbindAll()
        } catch (e: Exception) {
            Log.e("QrScannerFragment", "Error unbinding camera", e)
        }
        _binding = null
    }
}