package id.example.wastify.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.example.wastify.ui.theme.WasteDarkGreen
import id.example.wastify.ui.theme.WasteGreen
import id.example.wastify.ui.theme.WasteYellow
import id.example.wastify.ui.theme.WasteYellowGreen

@Composable
fun HomeScreen(
    onNavigateToClassify: () -> Unit,
) {
    val brush = Brush.verticalGradient(
        colors = listOf(WasteDarkGreen, WasteGreen)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Recycling,
            contentDescription = "Recycle Logo",
            tint = WasteYellow,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to EcoSort",
            style = MaterialTheme.typography.headlineLarge,
            color = WasteYellow,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Not sure if it's Recyclable or Organic?\nTake a picture and let our AI guide you to the right bin.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNavigateToClassify,
            colors = ButtonDefaults.buttonColors(
                containerColor = WasteYellowGreen,
                contentColor = WasteDarkGreen
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text(
                text = "Start Classifying",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}