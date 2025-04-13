package com.example.dicemaster.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dicemaster.R
import com.example.dicemaster.model.Die
import kotlinx.coroutines.launch

/**
 * Enhanced die component with animations and visual effects
 */
@Composable
fun DieComponent(
    die: Die,
    onDieClick: () -> Unit = {},
    isSelectable: Boolean = false,
    modifier: Modifier = Modifier,
    isRolling: Boolean = false,
    isLandscape: Boolean = false,
    animationKey: Int = 0
) {
    // Create a direct rotation value that doesn't depend on internal state
    val rotationAnimatable = remember(animationKey) {
        Animatable(0f)
    }

    // Launch the rotation effect when the key changes
    LaunchedEffect(animationKey, isRolling) {
        if (isRolling && !die.isSelected) {
            // Reset rotation first
            rotationAnimatable.snapTo(0f)
            // Then animate to full rotation
            rotationAnimatable.animateTo(
                targetValue = 720f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // Die bounce animation when selected/deselected
    val scale by animateFloatAsState(
        targetValue = if (die.isSelected && isSelectable) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dieScale"
    )

    // Border color animation with wooden brown tones
    val woodAccentColor = Color(0xFFCD853F) // Peru - golden brown wood color

    val borderColor by animateColorAsState(
        targetValue = if (die.isSelected && isSelectable)
            woodAccentColor // Golden wood highlight
        else
            Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )

    // Glow (shadow) animation with warm wooden colors
    val shadowElevation by animateFloatAsState(
        targetValue = if (die.isSelected && isSelectable) 8f else 3f,
        animationSpec = tween(durationMillis = 300),
        label = "shadowElevation"
    )

    // Background color for selected dice with wooden theme
    val backgroundColor by animateColorAsState(
        targetValue = if (die.isSelected && isSelectable)
            woodAccentColor.copy(alpha = 0.15f) // Soft golden highlight
        else
            Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    // Die image resources with enhanced graphics
    val dieImageResId = when (die.value) {
        1 -> R.drawable.die_1
        2 -> R.drawable.die_2
        3 -> R.drawable.die_3
        4 -> R.drawable.die_4
        5 -> R.drawable.die_5
        6 -> R.drawable.die_6
        else -> R.drawable.die_1
    }

    // Clickable function with haptic feedback
    val onClick = {
        if (isSelectable) {
            coroutineScope.launch {
                onDieClick()
            }
        }
    }

    // Responsive size based on orientation
    val dieSize = if (isLandscape) 48.dp else 64.dp

    // Define wooden colors
    val woodShadowAmbient = Color(0xFFD2B48C) // Tan wood color
    val woodShadowSpot = Color(0xFF8B4513) // SaddleBrown wood color

    // Calculate final rotation to ensure dice always end up at 0 degrees (upright)
    val finalRotation = if (!isRolling || die.isSelected) 0f else rotationAnimatable.value

    // Die component with enhanced wooden visual effects
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(dieSize)
            .shadow(
                elevation = shadowElevation.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = woodShadowAmbient, // Warm tan ambient glow
                spotColor = woodShadowSpot      // SaddleBrown spot light
            )
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (die.isSelected && isSelectable) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .scale(scale)
            .graphicsLayer(
                rotationZ = finalRotation,
                // Ensure rotation happens around center
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            )
            .clickable(enabled = isSelectable, onClick = onClick)
            .padding(2.dp)
    ) {
        // Die face image
        Image(
            painter = painterResource(id = dieImageResId),
            contentDescription = "Die showing ${die.value}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
        )
    }
}