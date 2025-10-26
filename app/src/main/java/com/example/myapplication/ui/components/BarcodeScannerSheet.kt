package com.example.myapplication.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerSheet(
    onScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Permissão da câmera
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) onDismiss()
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) requestPermission.launch(Manifest.permission.CAMERA)
    }
    if (!hasPermission) return

    // Evita múltiplos callbacks
    var emitted by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        // Controller do CameraX
        val cameraController = remember {
            LifecycleCameraController(context).apply {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            }
        }

        // ML Kit (EAN/UPC/Code128)
        val options = remember {
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_CODE_128
                )
                .build()
        }
        val scanner = remember { BarcodeScanning.getClient(options) }

        DisposableEffect(Unit) {
            val executor = ContextCompat.getMainExecutor(context)

            // Analyzer
            cameraController.setImageAnalysisAnalyzer(executor) { imageProxy: ImageProxy ->
                val media = imageProxy.image
                if (media != null && !emitted) {
                    val image = InputImage.fromMediaImage(
                        media,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    scanner.process(image)
                        .addOnSuccessListener { list ->
                            val raw = list.firstOrNull()?.rawValue
                            val ean = raw?.filter { it.isDigit() }
                            if (!ean.isNullOrEmpty() && !emitted) {
                                emitted = true
                                onScanned(ean) // o chamador fecha o sheet
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }

            cameraController.bindToLifecycle(lifecycleOwner)

            onDispose {
                try { cameraController.unbind() } catch (_: Throwable) {}
                scanner.close()
            }
        }

        // Preview da câmera
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.controller = cameraController
                }
            }
        )

        Text("Aponte para o EAN. Ao ler, a busca será preenchida automaticamente.")
    }
}
