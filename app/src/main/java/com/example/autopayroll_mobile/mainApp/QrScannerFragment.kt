package com.example.autopayroll_mobile.mainApp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.lifecycle.lifecycleScope
import com.example.autopayroll_mobile.data.model.ApiErrorResponse // Import error model
import com.example.autopayroll_mobile.data.model.ClockInOutRequest
import com.example.autopayroll_mobile.data.model.QRScanData
import com.example.autopayroll_mobile.databinding.FragmentQrScannerBinding
import com.example.autopayroll_mobile.network.ApiClient
import com.google.android.gms.location.*
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import retrofit2.HttpException // Import HttpException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrScannerFragment : Fragment() { // <-- START OF CLASS

    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // --- Variables to hold scanned data ---
    private var scannedQrData: QRScanData? = null
    private var currentLocation: Location? = null
    private var isRequestingLocationUpdates = false
    private var isProcessingQr = false // Flag to prevent multiple scans

    // Activity Result Launchers
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) checkLocationPermissionAndStart()
            else showToast("Camera permission is required.")
        }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) checkLocationEnabledAndStart()
            else showToast("Location permission is required for verification.")
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

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.lastLocation
                if (currentLocation != null) {
                    Log.d("QrScannerFragment", "Location acquired: ${currentLocation!!.latitude}, ${currentLocation!!.longitude}")
                    checkScanAndLocationReady()
                }
            }
        }

        checkCameraPermission()

        binding.clockInButton.setOnClickListener {
            submitAttendance("clock-in")
        }

        binding.clockOutButton.setOnClickListener {
            submitAttendance("clock-out")
        }
    }

    // --- (Permission check functions) ---
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
            startLocationUpdates() // Start listening for location
        } else {
            // Location is disabled, prompt user to enable it
            AlertDialog.Builder(requireContext())
                .setMessage("Location services are disabled. Please enable location to proceed.")
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    showToast("Location must be enabled to verify scans.")
                }
                .show()
        }
    }


    @SuppressLint("MissingPermission") // Permissions are checked before calling
    private fun startLocationUpdates() {
        if (!isRequestingLocationUpdates) {
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

    // Helper function to stop the camera
    private fun stopCamera() {
        try {
            cameraProviderFuture.get()?.unbindAll()
        } catch (e: Exception) {
            Log.e("QrScannerFragment", "Error unbinding camera", e)
        }
    }


    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        binding.statusTextView.visibility = View.GONE
        binding.instructionTextView.visibility = View.VISIBLE
        resetButtonState()
        isProcessingQr = false // Reset the scan flag

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
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e("QrScannerFragment", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @ExperimentalGetImage
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        // Use the flag to prevent re-scanning
        if (mediaImage != null && !isProcessingQr) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Check flag again
                    if (barcodes.isNotEmpty() && !isProcessingQr) {
                        isProcessingQr = true // Set flag
                        val barcode = barcodes.first()
                        val qrValue = barcode.rawValue

                        try {
                            scannedQrData = Gson().fromJson(qrValue, QRScanData::class.java)
                            activity?.runOnUiThread {
                                binding.statusTextView.text = "QR Code Scanned!"
                                binding.statusTextView.visibility = View.VISIBLE
                                checkScanAndLocationReady()
                            }
                        } catch (e: Exception) {
                            Log.e("QrScannerFragment", "Failed to parse QR JSON: $qrValue", e)
                            activity?.runOnUiThread { showToast("Invalid QR Code. Scanning again...") }
                            scannedQrData = null
                            resetButtonState()
                            isProcessingQr = false // Reset flag on parse error
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QrScannerFragment", "Barcode scanning failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun checkScanAndLocationReady() {
        // This function is called by both QR scanner and Location callback
        if (scannedQrData != null && currentLocation != null) {
            activity?.runOnUiThread {
                binding.statusTextView.text = "QR and Location acquired. Ready."
                binding.statusTextView.visibility = View.VISIBLE
                binding.clockInButton.visibility = View.VISIBLE
                binding.clockOutButton.visibility = View.VISIBLE
                binding.clockInButton.isEnabled = true
                binding.clockOutButton.isEnabled = true

                binding.instructionTextView.visibility = View.GONE
                stopCamera() // Stop scanning
                stopLocationUpdates() // Stop location
            }
        }
    }

    private fun resetButtonState() {
        binding.clockInButton.visibility = View.GONE
        binding.clockOutButton.visibility = View.GONE
        binding.clockInButton.isEnabled = false
        binding.clockOutButton.isEnabled = false
    }

    // This function has the new error handling
    private fun submitAttendance(action: String) {
        if (scannedQrData == null || currentLocation == null) {
            showToast("QR Code and location are required.")
            return
        }

        showLoading("Submitting $action...")

        val request = ClockInOutRequest(
            companyId = scannedQrData!!.company_id,
            token = scannedQrData!!.token,
            signature = scannedQrData!!.signature,
            latitude = currentLocation!!.latitude,
            longitude = currentLocation!!.longitude
        )

        val apiService = ApiClient.getClient(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Call the correct endpoint
                val response = if (action == "clock-in") {
                    apiService.clockIn(request)
                } else {
                    apiService.clockOut(request)
                }

                // Handle success
                hideLoading()
                showToast(response.message) // Show success message from server

                // Reset for next scan
                scannedQrData = null
                currentLocation = null
                resetButtonState()
                startCamera()
                startLocationUpdates()

            } catch (e: Exception) {
                // ## THIS IS THE FIX FOR ERROR HANDLING ##
                hideLoading()
                Log.e("QrScannerFragment", "API call failed", e)

                // Default error message
                var errorMessage = e.message ?: "An unknown error occurred"

                // Check if this is an HTTP error from our server
                if (e is HttpException) {
                    try {
                        // Read the raw error body
                        val errorBody = e.response()?.errorBody()?.string()
                        if (errorBody != null) {
                            // Parse the JSON to get the "message" field
                            val errorResponse = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                            errorMessage = errorResponse.message
                        }
                    } catch (jsonError: Exception) {
                        // Failed to parse the error
                        Log.e("QrScannerFragment", "Failed to parse error JSON", jsonError)
                    }
                }

                // Show the specific, helpful error message
                showToast(errorMessage)

                // Reset everything to allow the user to try again
                scannedQrData = null
                currentLocation = null
                resetButtonState()
                startCamera()
                startLocationUpdates()
            }
        }
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
        binding.clockInButton.isEnabled = false
        binding.clockOutButton.isEnabled = false
        binding.instructionTextView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.statusTextView.visibility = View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Reset to a clean state
            scannedQrData = null
            currentLocation = null
            resetButtonState()
            startCamera()
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        stopCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

} // <-- END OF CLASS