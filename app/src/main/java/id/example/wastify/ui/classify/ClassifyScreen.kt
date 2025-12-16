package id.example.wastify.ui.classify

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import id.example.wastify.helper.ClassificationResult
import id.example.wastify.helper.WasteClassifier
import id.example.wastify.network.RetrofitClient
import id.example.wastify.ui.theme.WasteDarkGreen
import id.example.wastify.ui.theme.WasteYellow
import id.example.wastify.ui.theme.WasteYellowGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun ClassifyScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope() // Use rememberCoroutineScope for UI events

    var hasCameraPermission by remember { mutableStateOf(false) }
    var classificationResult by remember { mutableStateOf<ClassificationResult?>(null) }
    var isLoading by remember { mutableStateOf(false) } // Add loading state

    val imageCapture = remember { ImageCapture.Builder().build() }
    val classifier = remember { WasteClassifier(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize().background(WasteDarkGreen)) {
            // 1. Camera Preview (Hidden when result is shown)
            if (classificationResult == null) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build()
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            preview.setSurfaceProvider(previewView.surfaceProvider)

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (exc: Exception) {
                                Log.e("CameraX", "Use case binding failed", exc)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Guide Box
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(280.dp)
                        .border(2.dp, WasteYellow.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                )
            }

            // 2. Loading Indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WasteYellow)
                }
            }

            // 3. Controls / Result
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        color = if (classificationResult != null) WasteDarkGreen else Color.Transparent
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (classificationResult == null && !isLoading) {
                    Button(
                        onClick = {
                            isLoading = true
                            captureAndClassify(context, imageCapture, classifier) { result ->
                                classificationResult = result
                                isLoading = false
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = WasteYellowGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take Photo",
                            tint = WasteDarkGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else if (classificationResult != null) {
                    ResultCard(result = classificationResult!!) {
                        classificationResult = null
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(WasteDarkGreen),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission required.", color = WasteYellow)
        }
    }
}

@Composable
fun ResultCard(result: ClassificationResult, onRetake: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = result.color),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = result.title.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = WasteDarkGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = result.instruction,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = WasteDarkGreen
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetake,
                colors = ButtonDefaults.buttonColors(containerColor = WasteDarkGreen)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = WasteYellow)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Another", color = WasteYellow)
            }
        }
    }
}

fun captureAndClassify(
    context: Context,
    imageCapture: ImageCapture,
    classifier: WasteClassifier,
    onResult: (ClassificationResult) -> Unit
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)

    imageCapture.takePicture(
        mainExecutor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                // Launch coroutine to handle file I/O and network
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val rotation = imageProxy.imageInfo.rotationDegrees.toFloat()
                        val bitmap = imageProxy.toBitmap().rotate(rotation)
                        imageProxy.close() // Close imageProxy ASAP

                        // Prepare file
                        val file = File(context.cacheDir, "upload_image.jpg")
                        val outputStream = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        outputStream.flush()
                        outputStream.close()

                        // Network Request
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                        try {
                            // Call API
                            val response = RetrofitClient.apiService.uploadImage(body)
                            val vector = response.features

                            // Classify locally using TFLite
                            val result = classifier.classify(vector)

                            withContext(Dispatchers.Main) {
                                onResult(result)
                            }
                        } catch (e: Exception) {
                            Log.e("API_ERROR", "Network request failed", e)
                            withContext(Dispatchers.Main) {
                                onResult(ClassificationResult.Error("Connection Failed: ${e.message}"))
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("Camera", "Bitmap processing error", e)
                        imageProxy.close()
                        withContext(Dispatchers.Main) {
                            onResult(ClassificationResult.Error("Image Error: ${e.message}"))
                        }
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "Capture failed", exception)
                onResult(ClassificationResult.Error("Capture Failed"))
            }
        }
    )
}

private fun Bitmap.rotate(rotation: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(rotation) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}