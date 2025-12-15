// File: id/example/wastify/ui/home/HomeScreen.kt
package id.example.wastify.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.example.wastify.data.EnvironmentalQuotes
import id.example.wastify.data.WeatherClient
import id.example.wastify.ui.theme.*

@Composable
fun HomeScreen(userName: String = "Wastify User") {
    var temperature by remember { mutableStateOf("--") }
    var weatherCode by remember { mutableStateOf(0) }
    var currentQuoteIndex by remember { mutableStateOf(0) }

    // Fetch Weather on launch
    LaunchedEffect(Unit) {
        try {
            val response = WeatherClient.service.getWeather()
            temperature = response.currentWeather.temperature.toString()
            weatherCode = response.currentWeather.weathercode
        } catch (e: Exception) {
            temperature = "N/A"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WasteSurface)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Welcome Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Hello, $userName! \uD83D\uDC4B",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = WasteTextPrimary
            )
            Text(
                text = "Ready to clean the world today?",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }

        // 2. Weather Card
        item {
            WeatherCard(temp = temperature, code = weatherCode)
        }

        // 3. Quote Section
        item {
            QuoteCard(
                quote = EnvironmentalQuotes[currentQuoteIndex],
                onRefresh = {
                    // Pick a random index different from current
                    var newIndex: Int
                    do {
                        newIndex = (EnvironmentalQuotes.indices).random()
                    } while (newIndex == currentQuoteIndex)
                    currentQuoteIndex = newIndex
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
        }
    }
}

@Composable
fun WeatherCard(temp: String, code: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WasteGreen),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(150.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "South Tangerang",
                    style = MaterialTheme.typography.titleMedium,
                    color = WasteYellow
                )
                Text(
                    text = "$temp°C",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Icon(
                imageVector = if (code < 3) Icons.Default.WbSunny else Icons.Default.Cloud,
                contentDescription = "Weather Icon",
                tint = WasteYellow,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun QuoteCard(quote: String, onRefresh: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Inspiration",
                    style = MaterialTheme.typography.labelLarge,
                    color = WasteGreen,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = WasteGreen)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.titleLarge,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = WasteTextPrimary
            )
        }
    }
}