package id.example.wastify.helper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import id.example.wastify.ui.theme.WasteDarkGreen
import id.example.wastify.ui.theme.WasteGreen
import id.example.wastify.ui.theme.WasteYellow
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class WasteClassifier(private val context: Context) {

    private var classifier: ImageClassifier? = null

    init {
        try {
            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setMaxResults(1)
                .build()
            classifier = ImageClassifier.createFromFileAndOptions(
                context, "model_dynamic_quant.tflite", options
            )
        } catch (e: Exception) {
            // Log the specific error to help with debugging
            Log.e("WasteClassifier", "Error initializing TFLite classifier.", e)
            // e.printStackTrace() is still good to keep
            e.printStackTrace()
        }
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        if (classifier == null) return ClassificationResult.Error

        val image = TensorImage.fromBitmap(bitmap)
        val results = classifier?.classify(image)
        Log.d("Classifier", "Results: $results")
        results?.flatMap { it.categories }?.forEach {
            Log.d("Classifier", "Label: '${it.label}', Score: ${it.score}")
        }

        val topCategory = results?.flatMap { it.categories }
            ?.maxByOrNull { it.score }
        Log.d("Classifier", "Label: ${topCategory?.label}, Score: ${topCategory?.score}")

        return when (topCategory?.label?.lowercase()) {
            "recyclable" -> ClassificationResult.Recyclable
            "organic" -> ClassificationResult.Organic
            else -> ClassificationResult.Unknown
        }
    }
}

sealed class ClassificationResult(val title: String, val color: androidx.compose.ui.graphics.Color, val instruction: String) {
    object Recyclable : ClassificationResult("Recyclable", WasteGreen, "Rinse the item and place it in the BLUE bin.")
    object Organic : ClassificationResult("Organic", WasteYellow, "Place this in the COMPOST or GREEN bin.")
    object Unknown : ClassificationResult("Unsure", WasteDarkGreen, "We couldn't identify this. Please check local guidelines.")
    object Error : ClassificationResult("Error", androidx.compose.ui.graphics.Color.Red, "Could not load AI model.")
}