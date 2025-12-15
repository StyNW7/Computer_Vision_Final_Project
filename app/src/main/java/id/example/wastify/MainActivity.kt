// File: id/example/wastify/MainActivity.kt
package id.example.wastify

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import id.example.wastify.ui.MainScreen
import id.example.wastify.ui.theme.WastifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WastifyTheme {
                MainScreen(
                    onSignOut = {
                        // Handle Sign Out Logic here
                        Toast.makeText(this, "Signed Out Successfully", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}