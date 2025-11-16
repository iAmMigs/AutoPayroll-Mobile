package com.example.autopayroll_mobile.mainApp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
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
import com.example.autopayroll_mobile.data.model.ApiErrorResponse
import com.example.autopayroll_mobile.data.qrModule.ClockInOutRequest
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.data.qrModule.QRScanData
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
import com.example.autopayroll_mobile.data.qrModule.TodayAttendanceResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
class QrScannerFragment : Fragment() {

    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var scannedQrData: QRScanData? = null
    private var currentLocation: Location? = null
    private var isRequestingLocationUpdates = false
    private var isProcessingQr = false
    private var employeeProfile: Employee? = null

    // NEW: Variable to hold today's actual attendance log
    private var todayAttendanceLog: TodayAttendanceResponse? = null

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
        fetchEmployeeProfileAndAttendance() // Combine profile and status fetching

        binding.clockInButton.setOnClickListener {
            submitAttendance("clock-in")
        }

        // Clock Out Listener: Shows confirmation dialog first
        binding.clockOutButton.setOnClickListener {
            showClockOutConfirmationDialog()
        }
    }

    // NEW: Function to check both profile and attendance status
    private fun fetchEmployeeProfileAndAttendance() {
        showLoading("Getting user profile and attendance status...")

        val apiService = ApiClient.getClient(requireContext().applicationContext)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                employeeProfile = apiService.getEmployeeProfile()
                todayAttendanceLog = apiService.getTodayAttendance()

                hideLoading()

                // Determine status and set message
                val log = todayAttendanceLog?.data

                if (log == null) {
                    // No log found: Needs Clock In
                    binding.instructionTextView.visibility = View.VISIBLE
                    showToast("Ready to clock in.")
                } else if (log.clock_out_time == null) {
                    // Clock In exists, Clock Out is missing: Needs Clock Out
                    binding.statusTextView.text = "You are currently clocked in."
                    binding.statusTextView.visibility = View.VISIBLE
                    binding.instructionTextView.visibility = View.VISIBLE // Keep instructions visible for scanning
                } else {
                    // Both exist: Completed
                    binding.statusTextView.text = "You have already clocked out for today."
                    binding.statusTextView.visibility = View.VISIBLE
                    // Stop, no need to scan
                    return@launch
                }

                Log.d("QrScannerFragment", "Attendance status loaded. Log: $log")

                startCamera()
                startLocationUpdates()

            } catch (e: Exception) {
                hideLoading()
                Log.e("QrScannerFragment", "Failed to get initial data", e)
                showToast("Error: Could not load data. Please check connection.")
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkLocationPermissionAndStart()
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
                checkLocationEnabledAndStart()
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
            // Location is enabled.
        } else {
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

    // NOTE: Removed old fetchEmployeeProfile() as it's merged into fetchEmployeeProfileAndAttendance()

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (todayAttendanceLog?.data?.clock_out_time != null) return // Already completed, don't need location

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

    private fun stopCamera() {
        try {
            cameraProviderFuture.get()?.unbindAll()
        } catch (e: Exception) {
            Log.e("QrScannerFragment", "Error unbinding camera", e)
        }
    }

    private fun startCamera() {
        // Only start camera if not already completed for the day
        if (todayAttendanceLog?.data?.clock_out_time != null) return

        binding.statusTextView.visibility = View.GONE
        binding.instructionTextView.visibility = View.VISIBLE
        resetButtonState()
        isProcessingQr = false

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
        if (mediaImage != null && !isProcessingQr) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty() && !isProcessingQr) {
                        isProcessingQr = true
                        val barcode = barcodes.first()
                        val qrValue = barcode.rawValue

                        try {
                            val qrData = Gson().fromJson(qrValue, QRScanData::class.java)

                            if (employeeProfile == null) {
                                activity?.runOnUiThread {
                                    showToast("Your profile is still loading. Please try again.")
                                    isProcessingQr = false
                                }
                                return@addOnSuccessListener
                            }

                            // Check if user already completed the log (Clock Out exists)
                            if (todayAttendanceLog?.data?.clock_out_time != null) {
                                activity?.runOnUiThread {
                                    binding.statusTextView.text = "Already clocked out today."
                                    binding.statusTextView.visibility = View.VISIBLE
                                }
                                return@addOnSuccessListener
                            }


                            if (qrData.company_id == employeeProfile!!.companyId) {
                                scannedQrData = qrData
                                activity?.runOnUiThread {
                                    binding.statusTextView.text = "QR Code Validated!"
                                    binding.statusTextView.visibility = View.VISIBLE
                                    checkScanAndLocationReady()
                                }
                            } else {
                                activity?.runOnUiThread {
                                    showToast("Error: You are not assigned to this company.")
                                    stopCamera()
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        startCamera()
                                    }, 2000)
                                }
                            }

                        } catch (e: Exception) {
                            Log.e("QrScannerFragment", "Failed to parse QR JSON: $qrValue", e)
                            activity?.runOnUiThread {
                                showToast("Invalid QR Code. Scanning again...")
                                stopCamera()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    startCamera()
                                }, 2000)
                            }
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
        if (scannedQrData != null && currentLocation != null) {
            activity?.runOnUiThread {
                binding.statusTextView.text = "QR and Location acquired. Ready."
                binding.statusTextView.visibility = View.VISIBLE

                // Determine if we show Clock In or Clock Out
                val log = todayAttendanceLog?.data

                if (log == null) {
                    // Case 1: Needs Clock In (No log exists)
                    binding.clockInButton.visibility = View.VISIBLE
                    binding.clockInButton.isEnabled = true
                    binding.clockOutButton.visibility = View.GONE
                } else if (log.clock_out_time == null) {
                    // Case 2: Needs Clock Out (Log exists, no clock_out_time)
                    binding.clockInButton.visibility = View.GONE
                    binding.clockOutButton.visibility = View.VISIBLE
                    binding.clockOutButton.isEnabled = true
                } else {
                    // Case 3: Already Clocked Out (Should have been handled earlier, but for safety)
                    resetButtonState()
                    binding.statusTextView.text = "Already clocked out today."
                    binding.statusTextView.visibility = View.VISIBLE
                }

                binding.instructionTextView.visibility = View.GONE
                stopCamera()
                stopLocationUpdates()
            }
        }
    }

    private fun resetButtonState() {
        binding.clockInButton.visibility = View.GONE
        binding.clockOutButton.visibility = View.GONE
        binding.clockInButton.isEnabled = false
        binding.clockOutButton.isEnabled = false
    }

    // NEW: Clock Out Confirmation Dialog
    private fun showClockOutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Clock Out")
            .setMessage("Are you sure you want to clock out?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                submitAttendance("clock-out")
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                // Keep buttons visible and ready after cancellation
                binding.statusTextView.text = "Clock Out cancelled. Ready."
                binding.statusTextView.visibility = View.VISIBLE
            }
            .show()
    }

    private fun submitAttendance(action: String) {
        if (scannedQrData == null || currentLocation == null) {
            showToast("QR Code and location are required.")
            return
        }

        // Final check for already clocked in/out status before API call
        val log = todayAttendanceLog?.data
        if (action == "clock-in" && log?.clock_in_time != null) {
            showToast("Error: Already clocked in today.")
            return
        }
        if (action == "clock-out" && log?.clock_out_time != null) {
            showToast("Error: Already clocked out today.")
            return
        }
        if (action == "clock-out" && log == null) {
            showToast("Error: Cannot clock out without clocking in.")
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

        val apiService = ApiClient.getClient(requireContext().applicationContext)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = if (action == "clock-in") {
                    apiService.clockIn(request)
                } else {
                    apiService.clockOut(request)
                }
                hideLoading()
                showToast(response.message)

                // On success, refetch status to update buttons and UI correctly
                fetchEmployeeProfileAndAttendance()

            } catch (e: Exception) {
                hideLoading()
                Log.e("QrScannerFragment", "API call failed", e)

                var errorMessage = e.message ?: "An unknown error occurred"

                if (e is HttpException) {
                    // The server returns 400 for already clocked in/out or geofence error (AttendanceController.php)
                    try {
                        val errorBody = e.response()?.errorBody()?.string()
                        if (errorBody != null) {
                            val errorResponse = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                            errorMessage = errorResponse.message
                        }
                    } catch (jsonError: Exception) {
                        Log.e("QrScannerFragment", "Failed to parse error JSON", jsonError)
                    }
                }

                showToast(errorMessage)

                // Reset state to force rescan after failed submission
                scannedQrData = null
                currentLocation = null
                // Re-fetch status to ensure button visibility is correct after error
                fetchEmployeeProfileAndAttendance()
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

            if (employeeProfile != null) {
                fetchEmployeeProfileAndAttendance()
            }
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
}