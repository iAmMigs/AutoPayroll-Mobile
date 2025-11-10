@file:OptIn(ExperimentalGetImage::class) // We still need this one for the camera

package com.example.autopayroll_mobile.composableUI

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autopayroll_mobile.data.model.QRScanData
import com.example.autopayroll_mobile.viewmodel.QrScannerUiState
import com.example.autopayroll_mobile.viewmodel.QrScannerViewModel
// ALL Accompanist permission imports have been REMOVED
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// 1. THE MAIN SCREEN ENTRY POINT
// This is now much simpler. It assumes permissions are granted.
@Composable
fun QrScannerScreen(
    viewModel: QrScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Check location setting on start.
    LaunchedEffect(Unit) {
        viewModel.checkLocationSetting()
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // The logic for permissions is gone. We just check if location is enabled.
            if (uiState.isLocationEnabled) {
                QrScannerContent(
                    uiState = uiState,
                    onQrCodeScanned = { qrData -> viewModel.onQrCodeScanned(qrData) },
                    onClockIn = { viewModel.onClockInClicked() },
                    onClockOut = { viewModel.onClockOutClicked() },
                    onStartLocationUpdates = { viewModel.startLocationUpdates() },
                    onStopLocationUpdates = { viewModel.stopLocationUpdates() }
                )
            } else {
                // Location is off, show the dialog to enable it.
                LocationDisabledDialog(
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    },
                    onDismiss = {
                        Toast.makeText(context, "Location must be enabled.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}


// 2. THE "HAPPY PATH" UI CONTENT (Unchanged)
@Composable
private fun QrScannerContent(
    uiState: QrScannerUiState,
    onQrCodeScanned: (QRScanData) -> Unit,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    onStartLocationUpdates: () -> Unit,
    onStopLocationUpdates: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // This effect manages the location updates based on the screen's lifecycle.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onStartLocationUpdates()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                onStopLocationUpdates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onStopLocationUpdates() // Ensure stops on exit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: The Camera Preview
        CameraPreview(
            isScanningEnabled = (uiState.scannedQrData == null),
            onQrCodeScanned = onQrCodeScanned,
            onScanFailed = {
                Log.w("QrScannerScreen", "Invalid QR Code scanned")
            }
        )

        // Layer 2: Instruction Text
        Text(
            text = "Point camera at Attendance QR Code",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp)
        )

        // Layer 3: Loading and Status
        if (uiState.isLoading || uiState.statusMessage != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                }
                if (uiState.statusMessage != null) {
                    val status = if (uiState.isReadyForClockIn) {
                        "QR and Location acquired. Ready to clock in/out."
                    } else {
                        uiState.statusMessage
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = status,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(8.dp)
                    )
                }
            }
        }

        // Layer 4: Buttons
        if (uiState.isReadyForClockIn) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = onClockIn,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clock In")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onClockOut,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clock Out")
                }
            }
        }
    }
}


// 3. THE CAMERA PREVIEW COMPOSABLE (Unchanged)
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    isScanningEnabled: Boolean,
    onQrCodeScanned: (QRScanData) -> Unit,
    onScanFailed: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    LaunchedEffect(previewView) {
        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (isScanningEnabled) {
                        processImage(imageProxy, barcodeScanner, onQrCodeScanned, onScanFailed)
                    } else {
                        imageProxy.close()
                    }
                }
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (exc: Exception) {
            Log.e("CameraPreview", "Use case binding failed", exc)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier.fillMaxSize())
}

// 4. IMAGE PROCESSING LOGIC (Unchanged)
private fun processImage(
    imageProxy: ImageProxy,
    barcodeScanner: BarcodeScanner,
    onQrScanned: (QRScanData) -> Unit,
    onScanFailed: () -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    val qrValue = barcode.rawValue
                    try {
                        val qrData = Gson().fromJson(qrValue, QRScanData::class.java)
                        onQrScanned(qrData)
                    } catch (e: Exception) {
                        Log.e("processImage", "Failed to parse QR JSON: $qrValue", e)
                        onScanFailed()
                    }
                }
            }
            .addOnFailureListener {
                Log.e("processImage", "Barcode scanning failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

// 5. HELPER FUNCTIONS (Unchanged)
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            continuation.resume(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }

// 6. LOCATION DIALOG (PermissionRationaleDialog is no longer needed)
@Composable
private fun LocationDisabledDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Services Disabled") },
        text = { Text("Please enable location services in your device settings to proceed.") },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}