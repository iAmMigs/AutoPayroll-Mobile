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
import com.example.autopayroll_mobile.data.model.ClockInOutRequest
import com.example.autopayroll_mobile.data.model.Employee
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
        fetchEmployeeProfile()

        binding.clockInButton.setOnClickListener {
            submitAttendance("clock-in")
        }

        binding.clockOutButton.setOnClickListener {
            submitAttendance("clock-out")
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
            // Good.
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

    private fun fetchEmployeeProfile() {
        showLoading("Getting user profile...")

        // Use applicationContext to be consistent with ViewModels
        val apiService = ApiClient.getClient(requireContext().applicationContext)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                employeeProfile = apiService.getEmployeeProfile()
                hideLoading()
                binding.instructionTextView.visibility = View.VISIBLE
                Log.d("QrScannerFragment", "Employee profile loaded. Company ID: ${employeeProfile?.companyId}")

                startCamera()
                startLocationUpdates()

            } catch (e: Exception) {
                hideLoading()
                Log.e("QrScannerFragment", "Failed to get employee profile", e)
                showToast("Error: Could not load your profile. Please log out and back in.")
            }
        }
    }

    @SuppressLint("MissingPermission")
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

    private fun stopCamera() {
        try {
            cameraProviderFuture.get()?.unbindAll()
        } catch (e: Exception) {
            Log.e("QrScannerFragment", "Error unbinding camera", e)
        }
    }

    private fun startCamera() {
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
                binding.clockInButton.visibility = View.VISIBLE
                binding.clockOutButton.visibility = View.VISIBLE
                binding.clockInButton.isEnabled = true
                binding.clockOutButton.isEnabled = true

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

        // Use applicationContext to be consistent
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

                scannedQrData = null
                currentLocation = null
                resetButtonState()
                startCamera()
                startLocationUpdates()

            } catch (e: Exception) {
                hideLoading()
                Log.e("QrScannerFragment", "API call failed", e)

                var errorMessage = e.message ?: "An unknown error occurred"

                if (e is HttpException) {
                    if (e.code() == 500) {
                        errorMessage = "A server error occurred. Please try again later."
                    } else {
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
                }

                showToast(errorMessage)

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

            if (employeeProfile != null) {
                scannedQrData = null
                currentLocation = null
                resetButtonState()
                startCamera()
                startLocationUpdates()
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