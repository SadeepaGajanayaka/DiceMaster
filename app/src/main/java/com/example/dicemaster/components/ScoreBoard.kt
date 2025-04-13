package com.example.dicemaster.components


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import kotlinx.coroutines.delay

/**
 * Enhanced ScoreBoard with animations and visual improvements - now collapsible!
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScoreBoard(
    playerScore: Int,
    computerScore: Int,
    playerWins: Int,
    computerWins: Int,
    targetScore: Int,
    modifier: Modifier = Modifier
) {
    // Get current configuration to check orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // State for collapsed/expanded view
    var isCollapsed by remember { mutableStateOf(true) }

    // Animation for expansion
    val expandTransition = updateTransition(
        targetState = isCollapsed,
        label = "expandTransition"
    )

    // Size animation
    val cardWidth by expandTransition.animateDp(
        transitionSpec = { tween(300) },
        label = "cardWidth"
    ) { collapsed ->
        if (isLandscape) {
            if (collapsed) 60.dp else 160.dp
        } else {
            if (collapsed) 160.dp else 300.dp
        }
    }

    // Rotation for expand/collapse arrow
    val arrowRotation by expandTransition.animateFloat(
        transitionSpec = { tween(300) },
        label = "arrowRotation"
    ) { collapsed -> if (collapsed) 0f else 180f }

    // Animation for progress toward target
    val playerProgress = (playerScore.toFloat() / targetScore).coerceIn(0f, 1f)
    val computerProgress = (computerScore.toFloat() / targetScore).coerceIn(0f, 1f)

    // Animation for score changes
    var oldPlayerScore by remember { mutableStateOf(playerScore) }
    var oldComputerScore by remember { mutableStateOf(computerScore) }
    var playerScoreChanged by remember { mutableStateOf(false) }
    var computerScoreChanged by remember { mutableStateOf(false) }

    // Check for score changes and trigger animations
    LaunchedEffect(playerScore, computerScore) {
        if (playerScore != oldPlayerScore) {
            playerScoreChanged = true
            delay(300)
            playerScoreChanged = false
            oldPlayerScore = playerScore
        }

        if (computerScore != oldComputerScore) {
            computerScoreChanged = true
            delay(300)
            computerScoreChanged = false
            oldComputerScore = computerScore
        }
    }

    // Main card with elevation and shape
    Card(
        modifier = if (isLandscape) {
            modifier
                .width(cardWidth)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
                .clickable { isCollapsed = !isCollapsed }
        } else {
            modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
                .clickable { isCollapsed = !isCollapsed }
        },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        if (isLandscape) {
            // Landscape layout with vertical design
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Collapse/Expand icon
                Text(
                    text = if (isCollapsed) "📊" else "📊 ←",
                    modifier = Modifier.rotate(arrowRotation),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = !isCollapsed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    // Full content when expanded
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Game statistics section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Title
                                Text(
                                    text = "GAME STATS",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Divider(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .width(40.dp),
                                    thickness = 2.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )

                                // Win statistics
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Wins:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Player wins
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        ) {
                                            Text(
                                                text = "$playerWins",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Text(text = "-")

                                        // Computer wins
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                        ) {
                                            Text(
                                                text = "$computerWins",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }

                                // Target score
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "$targetScore",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }

                        // Player score card with animation
                        ScoreItem(
                            label = "YOU",
                            score = playerScore,
                            color = MaterialTheme.colorScheme.primary,
                            progress = playerProgress,
                            isScoreChanged = playerScoreChanged,
                            isCompact = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        // Computer score card with animation
                        ScoreItem(
                            label = "CPU",
                            score = computerScore,
                            color = MaterialTheme.colorScheme.secondary,
                            progress = computerProgress,
                            isScoreChanged = computerScoreChanged,
                            isCompact = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                // Minimal content when collapsed - only show in landscape
                AnimatedVisibility(
                    visible = isCollapsed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mini player score
                        MiniScoreItem(
                            score = playerScore,
                            color = MaterialTheme.colorScheme.primary,
                            label = "YOU"
                        )

                        // Mini computer score
                        MiniScoreItem(
                            score = computerScore,
                            color = MaterialTheme.colorScheme.secondary,
                            label = "CPU"
                        )
                    }
                }
            }
        } else {
            // Portrait layout with horizontal design
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with expand/collapse indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCOREBOARD",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = if (isCollapsed) "▼" else "▲",
                        modifier = Modifier.rotate(arrowRotation),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Minimal content when collapsed
                AnimatedVisibility(
                    visible = isCollapsed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini player score
                        MiniScoreItem(
                            score = playerScore,
                            color = MaterialTheme.colorScheme.primary,
                            label = "YOU"
                        )

                        Divider(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Mini computer score
                        MiniScoreItem(
                            score = computerScore,
                            color = MaterialTheme.colorScheme.secondary,
                            label = "CPU"
                        )

                        Divider(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Target indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TARGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$targetScore",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // Full content when expanded
                AnimatedVisibility(
                    visible = !isCollapsed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        // Game statistics - wins and target score
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Win statistics
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "WINS:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.5.sp
                                    )
                                )

                                // Player wins badge
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "$playerWins",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(text = "-")

                                // Computer wins badge
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "$computerWins",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            // Target score badge
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "TARGET:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )

                                    Text(
                                        text = "$targetScore",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        // Scores row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Player score
                            ScoreItem(
                                label = "YOUR SCORE",
                                score = playerScore,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary,
                                progress = playerProgress,
                                isScoreChanged = playerScoreChanged
                            )

                            // Computer score
                            ScoreItem(
                                label = "COMPUTER",
                                score = computerScore,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.secondary,
                                progress = computerProgress,
                                isScoreChanged = computerScoreChanged
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact score display for collapsed view
 */
@Composable
private fun MiniScoreItem(
    score: Int,
    color: Color,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$score",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}

/**
 * Enhanced score item with animations and progress indicator
 */
@Composable
private fun ScoreItem(
    label: String,
    score: Int,
    modifier: Modifier = Modifier,
    color: Color,
    progress: Float = 0f,
    isCompact: Boolean = false,
    isScoreChanged: Boolean = false
) {
    // Animation for score changes
    val animatedScale by animateFloatAsState(
        targetValue = if (isScoreChanged) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scoreScale"
    )

    // Progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "progressAnimation"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Progress background
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(color.copy(alpha = 0.1f))
            )

            if (isCompact) {
                // Compact layout for landscape
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Animated score
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = color,
                        modifier = Modifier.scale(animatedScale)
                    )
                }
            } else {
                // Original layout for portrait
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Animated score
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = color,
                        modifier = Modifier.scale(animatedScale)
                    )
                }
            }
        }
    }
}