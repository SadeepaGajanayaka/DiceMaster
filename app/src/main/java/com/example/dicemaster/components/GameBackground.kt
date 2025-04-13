package com.example.dicemaster.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import androidx.compose.ui.draw.shadow

/**
 * Wooden textured background for the game
 */
@Composable
fun GameBackground(content: @Composable () -> Unit) {
    // Use explicit brown wooden colors instead of theme colors
    val backgroundWoodLight = Color(0xFFF5EBDC)  // Very light wood color
    val backgroundWoodMedium = Color(0xFFEEDCB5) // Light wooden background
    val backgroundWoodDarker = Color(0xFFD2B48C) // Tan wood color
    val woodGrainColor = Color(0xFF8B4513).copy(alpha = 0.05f) // SaddleBrown with low opacity for lines

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundWoodLight,
                        backgroundWoodMedium,
                        backgroundWoodDarker
                    ),
                    startY = 0f,
                    endY = 2000f
                )
            )
    ) {
        // Add subtle horizontal wood grain lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Draw subtle wood grain lines using wood color
            val strokeWidth = 1.5f

            val random = Random(1) // Fixed seed for consistent pattern
            val lineCount = 50

            for (i in 0..lineCount) {
                val y = random.nextFloat() * canvasHeight
                val xOffset = random.nextFloat() * 30

                drawLine(
                    color = woodGrainColor, // Using wood grain color
                    start = Offset(xOffset, y),
                    end = Offset(canvasWidth, y + random.nextFloat() * 20),
                    strokeWidth = strokeWidth
                )
            }
        }

        content()
    }
}