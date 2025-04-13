package com.example.dicemaster.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dicemaster.R
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    // Start animation after a short delay
    LaunchedEffect(key1 = Unit) {
        delay(100)
        animationStarted = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = !isLandscape,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (isLandscape) {
                        Modifier.fillMaxWidth(0.7f) // Reduced width in landscape
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )
                .background(
                    color = Color(0xFF362A20),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp) // Reduced padding
        ) {
            if (isLandscape) {
                // Landscape layout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 330.dp) // Reduced height
                ) {
                    // Header with title and dice icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dice logo
                        Image(
                            painter = painterResource(id = R.drawable.die_6),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp) // Smaller icon
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Title
                        Text(
                            text = "HOW TO PLAY",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }

                    // Game rules in a grid layout
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(scrollState)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
                        ) {
                            // Top row: Game Objective and Turns
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
                            ) {
                                // Game objective
                                RuleSection(
                                    title = "GAME OBJECTIVE",
                                    content = "Be the first player to reach the target score. The default target is 101 points.",
                                    modifier = Modifier.weight(1f)
                                )

                                // Turns
                                RuleSection(
                                    title = "TURNS",
                                    content = "On your turn, you roll five dice and have up to 3 rolls. After each roll, you can choose to keep any dice by tapping them, and re-roll the rest.",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Bottom row: Scoring and Winning
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
                            ) {
                                // Scoring
                                RuleSection(
                                    title = "SCORING",
                                    content = "Your score for each turn is the sum of all five dice. The computer follows a similar process with its own set of dice.",
                                    modifier = Modifier.weight(1f)
                                )

                                // Winning
                                RuleSection(
                                    title = "WINNING",
                                    content = "If both players reach the target in the same number of turns, the player with the higher score wins. If still tied, a tie-breaker round determines the winner.",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Close button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .width(180.dp) // Fixed width
                                .height(40.dp), // Shorter height
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B694A)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF8B694A)
                            )
                        ) {
                            Text(
                                text = "CLOSE",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // Portrait layout
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo and title section
                    AnimatedVisibility(
                        visible = animationStarted,
                        enter = fadeIn(tween(durationMillis = 500))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Dice logo
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.die_6),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Title
                            Text(
                                text = "HOW TO PLAY",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Game rules section
                    AnimatedVisibility(
                        visible = animationStarted,
                        enter = slideInVertically(
                            initialOffsetY = { 100 },
                            animationSpec = tween(durationMillis = 700, delayMillis = 300)
                        ) + fadeIn(tween(durationMillis = 700, delayMillis = 300))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .verticalScroll(scrollState)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Game rules in sections
                            RuleSection(
                                title = "GAME OBJECTIVE",
                                content = "Be the first player to reach the target score. The default target is 101 points."
                            )

                            RuleSection(
                                title = "TURNS",
                                content = "On your turn, you roll five dice and have up to 3 rolls. After each roll, you can choose to keep any dice by tapping them, and re-roll the rest."
                            )

                            RuleSection(
                                title = "SCORING",
                                content = "Your score for each turn is the sum of all five dice. The computer follows a similar process with its own set of dice."
                            )

                            RuleSection(
                                title = "WINNING",
                                content = "If both players reach the target in the same number of turns, the player with the higher score wins. If still tied, a tie-breaker round determines the winner."
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Close button with wooden styling
                    AnimatedVisibility(
                        visible = animationStarted,
                        enter = slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = tween(durationMillis = 500, delayMillis = 700)
                        ) + fadeIn(tween(durationMillis = 500, delayMillis = 700))
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B694A)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF8B694A)
                            )
                        ) {
                            Text(
                                text = "CLOSE",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = Color(0xFF433529),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp) // Reduced padding
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFFD2B48C)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Content
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
            color = Color.White
        )
    }
}