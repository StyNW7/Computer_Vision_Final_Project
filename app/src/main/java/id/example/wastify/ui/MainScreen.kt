// File: id/example/wastify/ui/MainScreen.kt
package id.example.wastify.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.example.wastify.ui.classify.ClassifyScreen
import id.example.wastify.ui.history.HistoryScreen
import id.example.wastify.ui.home.HomeScreen
import id.example.wastify.ui.theme.WasteDarkGreen
import id.example.wastify.ui.theme.WasteGreen
import id.example.wastify.ui.theme.WasteYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onSignOut: () -> Unit) {
    val navController = rememberNavController()

    // We use Scaffold to hold the TopBar and BottomBar
    Scaffold(
        topBar = { WastifyTopBar(onSignOut) },
        bottomBar = { WastifyBottomBar(navController) }
    ) { innerPadding ->
        // NavHost handles switching screens
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WastifyTopBar(onSignOut: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Wastify", // You can replace this text with an Image/Logo composable
                color = WasteDarkGreen,
                fontWeight = FontWeight.Black
            )
        },
        navigationIcon = {
            // Optional: Put logo here if you prefer left alignment
            Icon(
                imageVector = Icons.Default.CameraAlt, // Placeholder for Logo
                contentDescription = "Logo",
                tint = WasteGreen,
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        actions = {
            IconButton(onClick = onSignOut) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = Color.Red
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun WastifyBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        contentColor = WasteDarkGreen,
        tonalElevation = 8.dp
    ) {
        // 1. Home Item
        NavigationBarItem(
            icon = { Icon(if(currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = WasteYellow.copy(alpha = 0.5f))
        )

        // 2. SCAN Item (Highlighted)
        NavigationBarItem(
            icon = {
                // Custom FAB-like look for the middle button
                Surface(
                    shape = CircleShape,
                    color = WasteGreen,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Scan",
                        tint = WasteYellow,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            },
            label = { Text("", maxLines = 1) }, // Empty label for clean look
            selected = currentRoute == "classify",
            onClick = { navController.navigate("classify") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent) // Remove default indicator
        )

        // 3. History Item
        NavigationBarItem(
            icon = { Icon(if(currentRoute == "history") Icons.Filled.History else Icons.Outlined.History, contentDescription = "History") },
            label = { Text("History") },
            selected = currentRoute == "history",
            onClick = { navController.navigate("history") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = WasteYellow.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen() }
        composable("classify") { ClassifyScreen() }
        composable("history") { HistoryScreen() }
    }
}