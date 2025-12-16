package id.example.wastify.helper

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import id.example.wastify.ui.theme.WasteDarkGreen
import id.example.wastify.ui.theme.WasteGreen
import id.example.wastify.ui.theme.WasteYellow
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

class WasteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null

    init {
        try {
            val model = FileUtil.loadMappedFile(context, "model_dynamic_quant.tflite")
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(model, options)
        } catch (e: Exception) {
            Log.e("WasteClassifier", "Error initializing TFLite interpreter", e)
            e.printStackTrace()
        }
    }

    fun classify(features: FloatArray): ClassificationResult {
        if (interpreter == null) return ClassificationResult.Error("Interpreter is null")
        if (features.size != 114) {
            Log.e("WasteClassifier", "Invalid input size: ${features.size}, expected 114")
            return ClassificationResult.Error("Invalid input size")
        }

        // Input shape: [1, 114]
        val input = arrayOf(features)
        Log.d("SHAPE", "Outer array size: ${input.size}")
        Log.d("SHAPE", "Inner array size: ${input[0].size}")

        val output = Array(1) { FloatArray(1) }

        try {
            interpreter!!.run(input, output)
        } catch (e: Exception) {
            Log.e("WasteClassifier", "Error running inference", e)
            return ClassificationResult.Error("Error running model inference")
        }
        val score = output[0][0]
        Log.d("Classifier", "score=$score")
        return when {
            score > 0.5 -> ClassificationResult.Recyclable
            score <= 0.5 -> ClassificationResult.Organic
            else -> ClassificationResult.Unknown
        }
    }
}

sealed class ClassificationResult(val title: String, val color: androidx.compose.ui.graphics.Color, val instruction: String) {
    object Recyclable : ClassificationResult("Recyclable", WasteGreen, "Rinse the item and place it in the BLUE bin.")
    object Organic : ClassificationResult("Organic", WasteYellow, "Place this in the COMPOST or GREEN bin.")
    object Unknown : ClassificationResult("Unsure", WasteDarkGreen, "We couldn't identify this. Please check local guidelines.")
    data class Error(val errorMessage: String) :
        ClassificationResult("Error", Color.Red, errorMessage)
}