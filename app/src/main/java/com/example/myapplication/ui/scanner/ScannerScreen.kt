package com.example.myapplication.ui.scanner

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.media.AudioManager
import android.media.ToneGenerator
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onCode: (String) -> Unit,
    title: String = "Leitor de Códigos"
) {
    val ctx = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val mainExecutor: Executor = remember { ContextCompat.getMainExecutor(ctx) }

    // 1) Permissão
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permanentlyDenied by remember { mutableStateOf(false) }

    val askCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { ok ->
            granted = ok
            if (!ok) {
                permanentlyDenied =
                    ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            }
        }
    )
    LaunchedEffect(Unit) { if (!granted) askCamera.launch(Manifest.permission.CAMERA) }

    // 2) ML Kit (APENAS EAN-13)
    val scanner = remember {
        val opts = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13)
            .build()
        BarcodeScanning.getClient(opts)
    }
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }
    val fired = remember { AtomicBoolean(false) }

    // 3) Preview + controller
    val previewView = remember {
        PreviewView(ctx).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val controller = remember {
        LifecycleCameraController(ctx).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS) // PREVIEW é implícito
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    // 4) Limpeza ao sair
    DisposableEffect(Unit) {
        onDispose {
            try {
                controller.clearImageAnalysisAnalyzer()
                controller.setEnabledUseCases(0)
                previewView.controller = null
            } catch (_: Throwable) {}
        }
    }

    // 5) Estados para estabilização (precisa confirmar 2 frames iguais)
    var lastRead by remember { mutableStateOf<String?>(null) }
    var repeatCount by remember { mutableStateOf(0) }

    // 6) Analyzer
    LaunchedEffect(controller) {
        val analyzer: (ImageProxy) -> Unit = @ExperimentalGetImage { proxy ->
            val media = proxy.image
            if (media == null) {
                proxy.close()
            } else {
                val input = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                scanner.process(input)
                    .addOnSuccessListener { list ->
                        val value = list.firstOrNull()?.rawValue
                            ?.trim()
                            ?.filter { it.isDigit() } // garante só dígitos

                        if (value != null && value.length == 13 && isValidEan13(value)) {
                            // estabilização por 2 frames iguais
                            if (value == lastRead) {
                                repeatCount += 1
                            } else {
                                lastRead = value
                                repeatCount = 1
                            }

                            if (repeatCount >= 2 && fired.compareAndSet(false, true)) {
                                // Para tudo antes de navegar (evita duplo disparo/tela branca)
                                controller.clearImageAnalysisAnalyzer()
                                controller.setEnabledUseCases(0)

                                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                                onCode(value)
                                // não reabilite aqui
                            }
                        }
                    }
                    .addOnCompleteListener { proxy.close() }
            }
        }

        controller.setImageAnalysisAnalyzer(mainExecutor, analyzer)
    }

    // 7) Bind da câmera
    LaunchedEffect(granted) { if (granted) controller.bindToLifecycle(lifecycle) }

    // 8) UI
    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when {
                granted -> {
                    AndroidView(
                        factory = { previewView.also { it.controller = controller } },
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .size(width = 260.dp, height = 140.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.10f))
                    )
                }
                permanentlyDenied -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Permissão de Câmera negada permanentemente.\n" +
                                    "Abra as Configurações do app para permitir."
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", ctx.packageName, null)
                            )
                            ctx.startActivity(intent)
                        }) { Text("Abrir Configurações") }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Precisamos da permissão de Câmera para continuar.")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { askCamera.launch(Manifest.permission.CAMERA) }) {
                            Text("Conceder permissão")
                        }
                    }
                }
            }
        }
    }
}

/** Validação do dígito verificador do EAN-13 */
private fun isValidEan13(code: String): Boolean {
    if (code.length != 13 || !code.all { it.isDigit() }) return false
    val digits = code.map { it - '0' }
    val sum = (0..11).sumOf { i ->
        val w = if (i % 2 == 0) 1 else 3
        digits[i] * w
    }
    val check = (10 - (sum % 10)) % 10
    return check == digits[12]
}
