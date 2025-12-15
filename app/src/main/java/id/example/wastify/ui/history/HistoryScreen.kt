// File: id/example/wastify/ui/history/HistoryScreen.kt
package id.example.wastify.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.example.wastify.ui.theme.WasteGreen
import id.example.wastify.ui.theme.WasteSurface
import id.example.wastify.ui.theme.WasteTextPrimary

@Composable
fun HistoryScreen() {
    val dummyHistory = listOf(
        "Plastic Bottle" to "Recyclable",
        "Banana Peel" to "Organic",
        "Glass Jar" to "Recyclable",
        "Paper" to "Recyclable"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WasteSurface)
            .padding(24.dp)
    ) {
        Text(
            text = "Scan History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = WasteTextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(dummyHistory.size) { index ->
                val item = dummyHistory[index]
                HistoryItem(name = item.first, type = item.second)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun HistoryItem(name: String, type: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = WasteGreen,
                modifier = Modifier.size(40.dp).padding(end = 16.dp)
            )
            Column {
                Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = type, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}