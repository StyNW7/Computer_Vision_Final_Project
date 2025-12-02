package id.example.wastify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.example.wastify.ui.classify.ClassifyScreen
import id.example.wastify.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WasteClassifierApp()
        }
    }
}
@Preview(showBackground = true)
@Composable
fun WasteClassifierApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onNavigateToClassify = {
                    navController.navigate("classify")
                }
            )
        }

        composable("classify") {
            ClassifyScreen()
        }
    }
}