package com.example.dicemaster.screens

import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dicemaster.R
import com.example.dicemaster.components.DieComponent
import com.example.dicemaster.model.Die
import com.example.dicemaster.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colors matching the screenshot
private val woodBackground = Color(0xFF634832)
private val cardBackground = Color(0xFF4D331F)
private val headerCardBackground = Color(0xDDEEE0D0)
private val rollIndicatorBackground = Color(0xFF2F1D0D)
private val buttonBackground = Color(0xFF2F1D0D)

@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    onBackToStart: () -> Unit
) {
    val gameState by gameViewModel.gameState.collectAsState()
    var showTargetScoreDialog by remember { mutableStateOf(false) }
    var isRolling by remember { mutableStateOf(false) }

    // Animation counter - increment to force animation restart
    var animationKey by remember { mutableStateOf(0) }

    // Get screen dimensions
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Calculate adaptive sizes based on screen dimensions
    val smallerDimension = if (screenWidth < screenHeight) screenWidth else screenHeight
    val dieSize = (smallerDimension * 0.15f).coerceIn(40.dp, 64.dp)
    val buttonHeight = (smallerDimension * 0.12f).coerceIn(36.dp, 56.dp)
    val headerHeight = (smallerDimension * 0.14f).coerceIn(50.dp, 80.dp)

    // Calculate paddings based on screen size
    val standardPadding = (smallerDimension * 0.02f).coerceIn(4.dp, 16.dp)
    val smallPadding = standardPadding / 2
    val largePadding = standardPadding * 1.5f

    // Animation states
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }

    // Function to trigger dice roll animation
    fun triggerRollAnimation(action: () -> Unit) {
        coroutineScope.launch {
            // Increment animation key to force restart
            animationKey += 1

            // Haptic feedback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }

            // Start animation
            isRolling = true

            // Execute action
            action()

            // Animation duration
            delay(800)

            // End animation
            isRolling = false
        }
    }

    // Main container with wooden background
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.gamescreenback),
            contentDescription = "Game Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (!isLandscape) {
            // Portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(standardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Complete header with YOU, TARGET, CPU scores
                FullHeaderSection(
                    playerScore = gameState.playerScore,
                    computerScore = gameState.computerScore,
                    playerWins = gameState.playerWins,
                    computerWins = gameState.computerWins,
                    targetScore = gameState.targetScore,
                    height = headerHeight,
                    padding = standardPadding
                )

                // Game content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Player section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = standardPadding / 2),
                        shape = RoundedCornerShape(standardPadding),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBackground
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = standardPadding / 2
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(standardPadding)
                        ) {
                            // Title with score
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "YOUR DICE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.width(standardPadding))

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                ) {
                                    Text(
                                        text = "${gameState.playerCurrentRollValue}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dice layout in 3-2 pattern
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Top row - 3 dice
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (i in 0 until minOf(3, gameState.playerDice.size)) {
                                        DieComponent(
                                            die = gameState.playerDice[i],
                                            onDieClick = {
                                                if (gameState.currentRollNumber < 3 && !gameState.isGameOver) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        val vibrationEffect = VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                                                        vibrator.vibrate(vibrationEffect)
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        vibrator.vibrate(20)
                                                    }
                                                    gameViewModel.toggleDieSelection(i)
                                                }
                                            },
                                            isSelectable = gameState.currentRollNumber < 3 && !gameState.isGameOver,
                                            isRolling = isRolling && !gameState.playerDice[i].isSelected,
                                            animationKey = animationKey,
                                            modifier = Modifier.size(dieSize)
                                        )
                                    }
                                }

                                // Bottom row - 2 dice
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                ) {
                                    for (i in 3 until minOf(5, gameState.playerDice.size)) {
                                        DieComponent(
                                            die = gameState.playerDice[i],
                                            onDieClick = {
                                                if (gameState.currentRollNumber < 3 && !gameState.isGameOver) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        val vibrationEffect = VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                                                        vibrator.vibrate(vibrationEffect)
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        vibrator.vibrate(20)
                                                    }
                                                    gameViewModel.toggleDieSelection(i)
                                                }
                                            },
                                            isSelectable = gameState.currentRollNumber < 3 && !gameState.isGameOver,
                                            isRolling = isRolling && !gameState.playerDice[i].isSelected,
                                            animationKey = animationKey,
                                            modifier = Modifier.size(dieSize)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Helper text
                            AnimatedVisibility(
                                visible = gameState.currentRollNumber < 3 && !gameState.isGameOver
                            ) {
                                Text(
                                    text = "Tap dice to keep for next roll",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Roll indicator in the middle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(standardPadding))
                            .background(rollIndicatorBackground)
                            .padding(horizontal = standardPadding * 1.5f, vertical = standardPadding * 0.75f)
                    ) {
                        Text(
                            text = "Roll ${gameState.currentRollNumber} of 3",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )
                    }

                    // Computer section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = standardPadding / 2),
                        shape = RoundedCornerShape(standardPadding),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBackground
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = standardPadding / 2
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(standardPadding)
                        ) {
                            // Title with score
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "COMPUTER'S DICE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.width(standardPadding))

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                ) {
                                    Text(
                                        text = "${gameState.computerCurrentRollValue}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dice layout in 3-2 pattern for computer
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Top row - 3 dice
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (i in 0 until minOf(3, gameState.computerDice.size)) {
                                        DieComponent(
                                            die = gameState.computerDice[i],
                                            isSelectable = false,
                                            isRolling = isRolling,
                                            animationKey = animationKey,
                                            modifier = Modifier.size(dieSize)
                                        )
                                    }
                                }

                                // Bottom row - 2 dice
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                ) {
                                    for (i in 3 until minOf(5, gameState.computerDice.size)) {
                                        DieComponent(
                                            die = gameState.computerDice[i],
                                            isSelectable = false,
                                            isRolling = isRolling,
                                            animationKey = animationKey,
                                            modifier = Modifier.size(dieSize)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(standardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = standardPadding/2)
                ) {
                    Button(
                        onClick = {
                            triggerRollAnimation { gameViewModel.throwDice() }
                        },
                        enabled = gameState.currentRollNumber < 3 && !gameState.isGameOver,
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBackground,
                            disabledContainerColor = buttonBackground.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "THROW",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            triggerRollAnimation { gameViewModel.scoreRoll() }
                        },
                        enabled = !gameState.isGameOver && gameState.currentRollNumber > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBackground,
                            disabledContainerColor = buttonBackground.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "SCORE",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(buttonHeight)
                            .clip(CircleShape)
                            .background(Color(0xFF49739D))
                            .clickable { showTargetScoreDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚙️",
                            fontSize = 22.sp
                        )
                    }
                }
            }
        } else {
            // Landscape layout code (unchanged)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(standardPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header in landscape mode - compact version
                LandscapeHeaderSection(
                    playerScore = gameState.playerScore,
                    computerScore = gameState.computerScore,
                    playerWins = gameState.playerWins,
                    computerWins = gameState.computerWins,
                    targetScore = gameState.targetScore,
                    height = headerHeight * 0.75f,
                    padding = smallPadding
                )

                // Middle section with player and computer dice side by side
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = smallPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player dice section (left)
                    LandscapePlayerSection(
                        dice = gameState.playerDice,
                        currentRollValue = gameState.playerCurrentRollValue,
                        onDieClick = { index ->
                            if (gameState.currentRollNumber < 3 && !gameState.isGameOver) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val vibrationEffect = VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                                    vibrator.vibrate(vibrationEffect)
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator.vibrate(20)
                                }
                                gameViewModel.toggleDieSelection(index)
                            }
                        },
                        isSelectable = gameState.currentRollNumber < 3 && !gameState.isGameOver,
                        isRolling = isRolling,
                        animationKey = animationKey,
                        dieSize = dieSize,
                        padding = smallPadding,
                        modifier = Modifier.weight(1f)
                    )

                    // Roll indicator in the middle - adaptive height
                    LandscapeRollIndicator(
                        rollNumber = gameState.currentRollNumber,
                        modifier = Modifier
                            .padding(horizontal = smallPadding)
                            .fillMaxHeight(0.7f)
                            .width(dieSize * 0.6f)
                    )

                    // Computer dice section (right)
                    LandscapeComputerSection(
                        dice = gameState.computerDice,
                        currentRollValue = gameState.computerCurrentRollValue,
                        isRolling = isRolling,
                        animationKey = animationKey,
                        dieSize = dieSize,
                        padding = smallPadding,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bottom controls in landscape mode - more compact
                LandscapeGameControls(
                    onThrowClick = {
                        triggerRollAnimation { gameViewModel.throwDice() }
                    },
                    onScoreClick = {
                        triggerRollAnimation { gameViewModel.scoreRoll() }
                    },
                    onTargetScoreClick = { showTargetScoreDialog = true },
                    isThrowEnabled = gameState.currentRollNumber < 3 && !gameState.isGameOver,
                    isScoreEnabled = !gameState.isGameOver && gameState.currentRollNumber > 0,
                    buttonHeight = buttonHeight * 0.8f,
                    padding = smallPadding
                )
            }
        }

        // Game result dialog (win/lose)
        if (gameState.isGameOver) {
            GameResultDialog(
                isPlayerWinner = gameState.isPlayerWinner,
                onNewGame = onBackToStart,
                padding = standardPadding
            )
        }

        // Target score dialog
        if (showTargetScoreDialog) {
            TargetScoreDialog(
                currentTarget = gameState.targetScore,
                onTargetSet = { newTarget ->
                    gameViewModel.updateTargetScore(newTarget)
                    showTargetScoreDialog = false
                },
                onDismiss = { showTargetScoreDialog = false },
                padding = standardPadding
            )
        }
    }
}

@Composable
fun FullHeaderSection(
    playerScore: Int,
    computerScore: Int,
    playerWins: Int,
    computerWins: Int,
    targetScore: Int,
    height: Dp,
    padding: Dp
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = padding),
        shape = RoundedCornerShape(padding),
        colors = CardDefaults.cardColors(
            containerColor = headerCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = padding / 2
        )
    ) {
        Column(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title and wins
            Text(
                text = "DiceMaster",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )

            Text(
                text = "WINS: $playerWins - $computerWins",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(padding / 2))

            // Score boxes row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // YOU box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOU",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .border(0.5.dp, Color.Gray, RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = "$playerScore",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                    }
                }

                // TARGET column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TARGET",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "$targetScore",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                }

                // CPU box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CPU",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .border(0.5.dp, Color.Gray, RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = "$computerScore",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LandscapeHeaderSection(
    playerScore: Int,
    computerScore: Int,
    playerWins: Int,
    computerWins: Int,
    targetScore: Int,
    height: Dp,
    padding: Dp
) {
    // Calculate font sizes based on the height
    val fontSize = 14.sp
    val scoreFontSize = 16.sp
    val boxSize = (height * 0.7f).coerceIn(24.dp, 36.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(padding),
        colors = CardDefaults.cardColors(
            containerColor = headerCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = padding / 2
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = padding, vertical = padding / 2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Game title
            Text(
                text = "DiceMaster",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize * 1.2f
                ),
                color = Color.Black
            )

            // Wins counter
            Text(
                text = "WINS : $playerWins - $computerWins",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = fontSize
                ),
                color = Color.DarkGray
            )

            // YOU score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOU",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = fontSize * 0.8f
                    ),
                    color = Color.DarkGray
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(boxSize)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .border(0.5.dp, Color.Gray, RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = "$playerScore",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = scoreFontSize
                        ),
                        color = Color.Black
                    )
                }
            }

            // TARGET score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TARGET",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = fontSize * 0.8f
                    ),
                    color = Color.DarkGray
                )

                Text(
                    text = "$targetScore",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = scoreFontSize
                    ),
                    color = Color.Black
                )
            }

            // CPU score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CPU",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = fontSize * 0.8f
                    ),
                    color = Color.DarkGray
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(boxSize)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .border(0.5.dp, Color.Gray, RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = "$computerScore",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = scoreFontSize
                        ),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun LandscapePlayerSection(
    dice: List<Die>,
    currentRollValue: Int,
    onDieClick: (Int) -> Unit,
    isSelectable: Boolean,
    isRolling: Boolean,
    animationKey: Int,
    dieSize: Dp,
    padding: Dp,
    modifier: Modifier = Modifier
) {
    // Calculate font sizes based on die size
    val titleFontSize = 14.sp
    val helperTextSize = 12.sp
    val scoreCircleSize = dieSize * 0.6f

    Card(
        modifier = modifier
            .fillMaxHeight()
            .padding(end = padding),
        shape = RoundedCornerShape(padding),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = padding / 2
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Title with score
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "YOUR DICE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(padding))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(scoreCircleSize)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Text(
                        text = "$currentRollValue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = titleFontSize
                        ),
                        color = Color.Black
                    )
                }
            }

            // Dice layout
            LandscapeDiceGrid(
                dice = dice,
                onDieClick = onDieClick,
                isSelectable = isSelectable,
                isRolling = isRolling,
                animationKey = animationKey,
                dieSize = dieSize,
                padding = padding,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = padding)
            )

            // Helper text
            if (isSelectable) {
                Text(
                    text = "Tap dice to keep for next roll",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = helperTextSize
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LandscapeComputerSection(
    dice: List<Die>,
    currentRollValue: Int,
    isRolling: Boolean,
    animationKey: Int,
    dieSize: Dp,
    padding: Dp,
    modifier: Modifier = Modifier
) {
    val titleFontSize = 14.sp
    val scoreCircleSize = dieSize * 0.6f

    Card(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = padding),
        shape = RoundedCornerShape(padding),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = padding / 2
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Title with score
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "COMPUTER'S DICE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(padding))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(scoreCircleSize)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Text(
                        text = "$currentRollValue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = titleFontSize
                        ),
                        color = Color.Black
                    )
                }
            }

            // Dice grid
            LandscapeDiceGrid(
                dice = dice,
                onDieClick = { },
                isSelectable = false,
                isRolling = isRolling,
                animationKey = animationKey,
                dieSize = dieSize,
                padding = padding,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = padding)
            )
        }
    }
}

@Composable
fun LandscapeDiceGrid(
    dice: List<Die>,
    onDieClick: (Int) -> Unit,
    isSelectable: Boolean,
    isRolling: Boolean,
    animationKey: Int,
    dieSize: Dp,
    padding: Dp,
    modifier: Modifier = Modifier
) {
    // Determine layout based on dice count
    val diceCount = dice.size
    val rowsCount = if (diceCount <= 3) 1 else 2

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (rowsCount == 1) {
            // Single row layout
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = padding / 2)
            ) {
                for (i in dice.indices) {
                    DieComponent(
                        die = dice[i],
                        onDieClick = { onDieClick(i) },
                        isSelectable = isSelectable,
                        isRolling = isRolling && !dice[i].isSelected,
                        animationKey = animationKey,
                        isLandscape = true,
                        modifier = Modifier.size(dieSize)
                    )
                }
            }
        } else {
            // Two row layout - first row with 3 dice
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = padding / 2)
            ) {
                for (i in 0 until minOf(3, diceCount)) {
                    DieComponent(
                        die = dice[i],
                        onDieClick = { onDieClick(i) },
                        isSelectable = isSelectable,
                        isRolling = isRolling && !dice[i].isSelected,
                        animationKey = animationKey,
                        isLandscape = true,
                        modifier = Modifier.size(dieSize)
                    )
                }
            }

            // Second row with remaining dice
            if (diceCount > 3) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = padding / 2)
                ) {
                    for (i in 3 until diceCount) {
                        Box(
                            modifier = Modifier.width(dieSize * 1.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            DieComponent(
                                die = dice[i],
                                onDieClick = { onDieClick(i) },
                                isSelectable = isSelectable,
                                isRolling = isRolling && !dice[i].isSelected,
                                animationKey = animationKey,
                                isLandscape = true,
                                modifier = Modifier.size(dieSize)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun LandscapeRollIndicator(
    rollNumber: Int,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // The background pill shape
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(0.7f)
                .clip(RoundedCornerShape(20.dp))
                .background(rollIndicatorBackground)
                .padding(vertical = 12.dp)
        ) {
            // Simple vertical text with reasonable spacing
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Display words with better spacing
                Text(
                    text = "ROLL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$rollNumber",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "OF",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "3",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LandscapeGameControls(
    onThrowClick: () -> Unit,
    onScoreClick: () -> Unit,
    onTargetScoreClick: () -> Unit,
    isThrowEnabled: Boolean,
    isScoreEnabled: Boolean,
    buttonHeight: Dp,
    padding: Dp
) {
    val buttonFontSize = 14.sp
    val buttonRadius = buttonHeight / 4
    val settingsButtonSize = buttonHeight

    Row(
        horizontalArrangement = Arrangement.spacedBy(padding),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = padding / 2)
    ) {
        // THROW button
        Button(
            onClick = onThrowClick,
            enabled = isThrowEnabled,
            modifier = Modifier
                .weight(1f)
                .height(buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBackground,
                disabledContainerColor = buttonBackground.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(buttonRadius)
        ) {
            Text(
                text = "THROW",
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = buttonFontSize,
                color = Color.White
            )
        }

        // SCORE button
        Button(
            onClick = onScoreClick,
            enabled = isScoreEnabled,
            modifier = Modifier
                .weight(1f)
                .height(buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBackground,
                disabledContainerColor = buttonBackground.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(buttonRadius)
        ) {
            Text(
                text = "SCORE",
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = buttonFontSize,
                color = Color.White
            )
        }

        // Settings button
        Box(
            modifier = Modifier
                .size(settingsButtonSize)
                .clip(CircleShape)
                .background(Color(0xFF49739D))
                .clickable { onTargetScoreClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚙️",
                fontSize = buttonFontSize * 1.5f
            )
        }
    }
}

@Composable
fun TargetScoreDialog(
    currentTarget: Int,
    onTargetSet: (Int) -> Unit,
    onDismiss: () -> Unit,
    padding: Dp
) {
    var targetText by remember { mutableStateOf(currentTarget.toString()) }
    var isError by remember { mutableStateOf(false) }

    val titleFontSize = 20.sp
    val bodyFontSize = 16.sp
    val buttonFontSize = 18.sp
    val inputFontSize = 24.sp

    val buttonHeight = 48.dp
    val inputHeight = 56.dp

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF362A20),
                    shape = RoundedCornerShape(padding)
                )
                .padding(padding * 1.5f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(padding)
            ) {
                // Title
                Text(
                    text = "SET TARGET SCORE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(padding / 2))

                // Instructions
                Text(
                    text = "Enter the target score for winning:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = bodyFontSize
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Text field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(inputHeight)
                        .background(
                            color = Color(0xFF8B694A),
                            shape = RoundedCornerShape(padding / 2)
                        )
                        .padding(horizontal = padding),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = targetText,
                        onValueChange = {
                            // Only allow numbers
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                targetText = it
                                isError = false
                            }
                        },
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontSize = inputFontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        cursorBrush = SolidColor(Color.White)
                    )
                }

                // Error message
                if (isError) {
                    Text(
                        text = "Please enter a valid number greater than 0",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = bodyFontSize * 0.8f
                        )
                    )
                }

                Spacer(modifier = Modifier.height(padding))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(padding),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cancel button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight),
                        shape = RoundedCornerShape(buttonHeight / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF8B694A)
                        ),
                        border = BorderStroke(
                            width = 2.dp,
                            color = Color(0xFF8B694A)
                        )
                    ) {
                        Text(
                            text = "CANCEL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = buttonFontSize
                            )
                        )
                    }

                    // Set button
                    Button(
                        onClick = {
                            try {
                                val newTarget = targetText.toInt()
                                if (newTarget > 0) {
                                    onTargetSet(newTarget)
                                } else {
                                    isError = true
                                }
                            } catch (e: NumberFormatException) {
                                isError = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight),
                        shape = RoundedCornerShape(buttonHeight / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B694A)
                        )
                    ) {
                        Text(
                            text = "SET",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = buttonFontSize
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameResultDialog(
    isPlayerWinner: Boolean,
    onNewGame: () -> Unit,
    padding: Dp
) {
    val titleFontSize = 20.sp
    val resultFontSize = 24.sp
    val buttonFontSize = 16.sp
    val buttonHeight = 42.dp

    Dialog(
        onDismissRequest = { /* Do nothing, force user to see result */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(
                    color = Color(0xFF362A20),
                    shape = RoundedCornerShape(padding)
                )
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(padding * 0.75f)
            ) {
                // Title
                Text(
                    text = "GAME OVER",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = titleFontSize
                    ),
                    color = Color.White
                )

                // Win/Lose message with reduced height
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isPlayerWinner)
                                Color(0xFFE8F5E9) // Light green background for win
                            else
                                Color(0xFFFFEBEE), // Light red background for lose
                            shape = RoundedCornerShape(padding * 0.75f)
                        )
                        .padding(vertical = padding * 0.75f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlayerWinner) "YOU WIN!" else "YOU LOSE!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = resultFontSize
                        ),
                        color = if (isPlayerWinner)
                            Color(0xFF2E7D32) // Green text for win
                        else
                            Color(0xFFC62828), // Red text for lose
                        textAlign = TextAlign.Center
                    )
                }

                // New game button
                Button(
                    onClick = onNewGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    shape = RoundedCornerShape(buttonHeight / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B694A)
                    )
                ) {
                    Text(
                        text = "NEW GAME",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = buttonFontSize
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}