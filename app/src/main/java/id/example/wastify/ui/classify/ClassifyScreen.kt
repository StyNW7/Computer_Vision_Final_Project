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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import id.example.wastify.data.local.WastifyDatabase
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
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun ClassifyScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // 1. Initialize Database Safely
    // We use a 'remember' with a key to ensure we don't recreate it constantly
    val db = remember { WastifyDatabase.getDatabase(context) }

    // State
    var hasCameraPermission by remember { mutableStateOf(false) }
    var classificationResult by remember { mutableStateOf<ClassificationResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Camera Components
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val classifier = remember { WasteClassifier(context) }

    // Permission Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    // Request Permission on Start
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    // --- CAMERA LOGIC (Moved outside AndroidView for stability) ---
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val cameraProvider = context.getCameraProvider()
            try {
                cameraProvider.unbindAll() // Unbind any previous use

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("Camera", "Binding failed", e)
            }
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize().background(WasteDarkGreen)) {

            // 1. Camera Preview View
            if (classificationResult == null) {
                AndroidView(
                    factory = { previewView }, // Just return the already created view
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
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
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
                            // Launch in the scope
                            scope.launch {
                                captureAndClassify(context, imageCapture, classifier, db) { result ->
                                    classificationResult = result
                                    isLoading = false
                                }
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
                        // Re-bind camera when retaking might be needed depending on device
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

// --- Helper: Get CameraProvider safely with Coroutines ---
suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    cameraProviderFuture.addListener({
        continuation.resume(cameraProviderFuture.get())
    }, ContextCompat.getMainExecutor(this))
}

// --- Logic Function ---
fun captureAndClassify(
    context: Context,
    imageCapture: ImageCapture,
    classifier: WasteClassifier,
    db: WastifyDatabase, // Pass DB instead of DAO for safety
    onResult: (ClassificationResult) -> Unit
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)

    imageCapture.takePicture(
        mainExecutor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                // Use IO Dispatcher for File & Network operations
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // 1. Process Bitmap
                        val rotation = imageProxy.imageInfo.rotationDegrees.toFloat()
                        val bitmap = imageProxy.toBitmap().rotate(rotation)
                        imageProxy.close() // Close ASAP to free camera buffer

                        // 2. Save Image
                        val timestamp = System.currentTimeMillis()
                        val filename = "wastify_${timestamp}.jpg"
                        val file = File(context.filesDir, filename)
                        val outputStream = FileOutputStream(file)

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        outputStream.flush()
                        outputStream.close()

                        // 3. Prepare Network Request
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                        // 4. API Call
                        try {
                            val response = RetrofitClient.apiService.uploadImage(body)
                            val vector = response.features.toFloatArray()
                            val result = classifier.classify(vector)

                            // 5. Save to Database
                            if (result !is ClassificationResult.Error) {
                                val historyItem = id.example.wastify.data.local.ScanHistory(
                                    imagePath = file.absolutePath,
                                    resultTitle = result.title,
                                    resultColor = result.color.toArgb().toLong(),
                                    timestamp = timestamp
                                )
                                db.historyDao().insertHistory(historyItem)
                            }

                            withContext(Dispatchers.Main) {
                                onResult(result)
                            }

                        } catch (e: Exception) {
                            Log.e("API_ERROR", "Network Request Failed", e)
                            withContext(Dispatchers.Main) {
                                onResult(ClassificationResult.Error("Network Error: Check connection"))
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("Camera", "Bitmap processing error", e)
                        imageProxy.close()
                        withContext(Dispatchers.Main) {
                            onResult(ClassificationResult.Error("Image Error"))
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