package com.example.dicemaster.viewmodel


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicemaster.model.Die
import com.example.dicemaster.model.GameState
import com.example.dicemaster.utils.ComputerStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the game logic and state
 * Uses SavedStateHandle for preserving state during configuration changes
 */
class GameViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    companion object {
        private const val KEY_PLAYER_DICE = "player_dice"
        private const val KEY_COMPUTER_DICE = "computer_dice"
        private const val KEY_PLAYER_SCORE = "player_score"
        private const val KEY_COMPUTER_SCORE = "computer_score"
        private const val KEY_TARGET_SCORE = "target_score"
        private const val KEY_CURRENT_ROLL = "current_roll"
        private const val KEY_PLAYER_ATTEMPTS = "player_attempts"
        private const val KEY_COMPUTER_ATTEMPTS = "computer_attempts"
        private const val KEY_IS_GAME_OVER = "is_game_over"
        private const val KEY_IS_PLAYER_WINNER = "is_player_winner"
        private const val KEY_IS_TIE = "is_tie"
        private const val KEY_IS_TIEBREAKING = "is_tiebreaking"
        private const val KEY_PLAYER_WINS = "player_wins"
        private const val KEY_COMPUTER_WINS = "computer_wins"
    }

    // Game state as StateFlow for UI updates
    private val _gameState = MutableStateFlow(
        // Restore state from SavedStateHandle if available, otherwise use default
        GameState(
            playerDice = savedStateHandle.get<List<Die>>(KEY_PLAYER_DICE) ?: List(5) { Die.random() },
            computerDice = savedStateHandle.get<List<Die>>(KEY_COMPUTER_DICE) ?: List(5) { Die.random() },
            playerScore = savedStateHandle.get<Int>(KEY_PLAYER_SCORE) ?: 0,
            computerScore = savedStateHandle.get<Int>(KEY_COMPUTER_SCORE) ?: 0,
            targetScore = savedStateHandle.get<Int>(KEY_TARGET_SCORE) ?: 101,
            currentRollNumber = savedStateHandle.get<Int>(KEY_CURRENT_ROLL) ?: 1,
            playerAttempts = savedStateHandle.get<Int>(KEY_PLAYER_ATTEMPTS) ?: 0,
            computerAttempts = savedStateHandle.get<Int>(KEY_COMPUTER_ATTEMPTS) ?: 0,
            isGameOver = savedStateHandle.get<Boolean>(KEY_IS_GAME_OVER) ?: false,
            isPlayerWinner = savedStateHandle.get<Boolean>(KEY_IS_PLAYER_WINNER) ?: false,
            isTie = savedStateHandle.get<Boolean>(KEY_IS_TIE) ?: false,
            isTiebreaking = savedStateHandle.get<Boolean>(KEY_IS_TIEBREAKING) ?: false,
            playerWins = savedStateHandle.get<Int>(KEY_PLAYER_WINS) ?: 0,
            computerWins = savedStateHandle.get<Int>(KEY_COMPUTER_WINS) ?: 0
        )
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Save state changes to SavedStateHandle
    private fun saveState() {
        val state = _gameState.value
        savedStateHandle[KEY_PLAYER_DICE] = state.playerDice
        savedStateHandle[KEY_COMPUTER_DICE] = state.computerDice
        savedStateHandle[KEY_PLAYER_SCORE] = state.playerScore
        savedStateHandle[KEY_COMPUTER_SCORE] = state.computerScore
        savedStateHandle[KEY_TARGET_SCORE] = state.targetScore
        savedStateHandle[KEY_CURRENT_ROLL] = state.currentRollNumber
        savedStateHandle[KEY_PLAYER_ATTEMPTS] = state.playerAttempts
        savedStateHandle[KEY_COMPUTER_ATTEMPTS] = state.computerAttempts
        savedStateHandle[KEY_IS_GAME_OVER] = state.isGameOver
        savedStateHandle[KEY_IS_PLAYER_WINNER] = state.isPlayerWinner
        savedStateHandle[KEY_IS_TIE] = state.isTie
        savedStateHandle[KEY_IS_TIEBREAKING] = state.isTiebreaking
        savedStateHandle[KEY_PLAYER_WINS] = state.playerWins
        savedStateHandle[KEY_COMPUTER_WINS] = state.computerWins
    }

    /**
     * Starts a new game
     */
    fun startNewGame(targetScore: Int = 101) {
        _gameState.update {
            GameState(
                targetScore = targetScore,
                playerDice = List(5) { Die.random() },
                computerDice = List(5) { Die.random() }
            )
        }
        saveState()
        updateCurrentRollValues()
    }

    /**
     * Throws dice for both players
     */
    fun throwDice() {
        if (_gameState.value.currentRollNumber > 3 || _gameState.value.isGameOver) {
            return
        }

        // For the player, reroll only dice that aren't selected
        val newPlayerDice = _gameState.value.playerDice.map { die ->
            if (die.isSelected) die else Die.random()
        }

        // For the computer, follow random strategy for rerolls
        val newComputerDice = makeComputerRerollDecision()

        _gameState.update { currentState ->
            currentState.copy(
                playerDice = newPlayerDice,
                computerDice = newComputerDice,
                currentRollNumber = currentState.currentRollNumber + 1
            )
        }

        saveState()
        updateCurrentRollValues()

        // If we're at 3 rolls, automatically score
        if (_gameState.value.currentRollNumber > 3) {
            scoreRoll()
        }
    }

    /**
     * Toggles selection of a player die for reroll
     */
    fun toggleDieSelection(index: Int) {
        // Only allow selection if we're not at max rolls and the game isn't over
        if (_gameState.value.currentRollNumber >= 3 || _gameState.value.isGameOver) {
            return
        }

        val updatedDice = _gameState.value.playerDice.toMutableList()
        val die = updatedDice[index]
        updatedDice[index] = die.copy(isSelected = !die.isSelected)

        _gameState.update { it.copy(playerDice = updatedDice) }
        saveState()
    }

    /**
     * Scores the current roll for both players
     */
    fun scoreRoll() {
        val currentState = _gameState.value

        // Update scores
        val newPlayerScore = currentState.playerScore + currentState.playerCurrentRollValue
        val newComputerScore = currentState.computerScore + currentState.computerCurrentRollValue

        // Update attempts count
        val newPlayerAttempts = currentState.playerAttempts + 1
        val newComputerAttempts = currentState.computerAttempts + 1

        // Check if either player won
        val playerReachedTarget = newPlayerScore >= currentState.targetScore
        val computerReachedTarget = newComputerScore >= currentState.targetScore

        // Determine game outcome
        val isGameOver = playerReachedTarget || computerReachedTarget
        val isTie = playerReachedTarget && computerReachedTarget &&
                newPlayerScore == newComputerScore &&
                newPlayerAttempts == newComputerAttempts

        val isPlayerWinner = when {
            // If both reached target in same number of attempts
            playerReachedTarget && computerReachedTarget && newPlayerAttempts == newComputerAttempts ->
                newPlayerScore > newComputerScore

            // If player reached target in fewer attempts
            playerReachedTarget && (!computerReachedTarget || newPlayerAttempts < newComputerAttempts) ->
                true

            // Otherwise computer wins
            else -> false
        }

        // Update win statistics if game is over
        val newPlayerWins = if (isGameOver && !isTie && isPlayerWinner) {
            currentState.playerWins + 1
        } else {
            currentState.playerWins
        }

        val newComputerWins = if (isGameOver && !isTie && !isPlayerWinner) {
            currentState.computerWins + 1
        } else {
            currentState.computerWins
        }

        // Update game state
        _gameState.update { state ->
            state.copy(
                playerScore = newPlayerScore,
                computerScore = newComputerScore,
                playerAttempts = newPlayerAttempts,
                computerAttempts = newComputerAttempts,
                isGameOver = isGameOver && !isTie,
                isPlayerWinner = isPlayerWinner,
                isTie = isTie,
                isTiebreaking = false,
                playerWins = newPlayerWins,
                computerWins = newComputerWins,
                // Reset for next turn
                currentRollNumber = 1,
                playerDice = if (!isGameOver) List(5) { Die.random() } else state.playerDice,
                computerDice = if (!isGameOver) List(5) { Die.random() } else state.computerDice
            )
        }

        // Handle tie-breaking if needed
        if (_gameState.value.isTie) {
            startTiebreaker()
        } else {
            updateCurrentRollValues()
        }

        saveState()
    }

    /**
     * Starts a tiebreaker round
     * Implements requirement 9 - tie-breaking with no rerolls
     */
    private fun startTiebreaker() {
        _gameState.update { state ->
            state.copy(
                isTiebreaking = true,
                playerDice = List(5) { Die.random() },
                computerDice = List(5) { Die.random() },
                // Setting currentRollNumber to 3 ensures no rerolls are allowed
                currentRollNumber = 3
            )
        }
        updateCurrentRollValues()
        saveState()

        // Compare tie-breaking roll values
        val playerValue = _gameState.value.playerCurrentRollValue
        val computerValue = _gameState.value.computerCurrentRollValue

        if (playerValue != computerValue) {
            // Tie is broken
            val isPlayerWinner = playerValue > computerValue

            // Update win statistics
            val newPlayerWins = if (isPlayerWinner) {
                _gameState.value.playerWins + 1
            } else {
                _gameState.value.playerWins
            }

            val newComputerWins = if (!isPlayerWinner) {
                _gameState.value.computerWins + 1
            } else {
                _gameState.value.computerWins
            }

            _gameState.update { state ->
                state.copy(
                    isGameOver = true,
                    isTie = false,
                    isTiebreaking = false,
                    isPlayerWinner = isPlayerWinner,
                    playerWins = newPlayerWins,
                    computerWins = newComputerWins
                )
            }
        } else {
            // Still tied, continue with another tiebreaker
            viewModelScope.launch {
                // Small delay before next tiebreaker to allow UI to update
                kotlinx.coroutines.delay(500)
                startTiebreaker()
            }
        }

        saveState()
    }

    /**
     * Updates the current roll values
     */
    private fun updateCurrentRollValues() {
        val playerValue = _gameState.value.calculateDiceValue(_gameState.value.playerDice)
        val computerValue = _gameState.value.calculateDiceValue(_gameState.value.computerDice)

        _gameState.update { state ->
            state.copy(
                playerCurrentRollValue = playerValue,
                computerCurrentRollValue = computerValue
            )
        }
        saveState()
    }

    /**
     * Makes the computer's reroll decision based on strategy
     *
     * Note: This implementation has two strategies:
     * 1. Random strategy (default) - Makes random decisions
     * 2. Optimal strategy - Uses the ComputerStrategy helper to make better decisions
     */
    private fun makeComputerRerollDecision(): List<Die> {
        val currentState = _gameState.value
        val computerDice = currentState.computerDice

        // Uncomment to use the random strategy instead of the optimal one
        // return makeRandomRerollDecision(computerDice, currentState.currentRollNumber)

        // Use the optimal strategy
        return ComputerStrategy.makeOptimalDecision(
            computerDice = computerDice,
            computerScore = currentState.computerScore,
            playerScore = currentState.playerScore,
            targetScore = currentState.targetScore,
            currentRollNumber = currentState.currentRollNumber
        )
    }

    /**
     * Makes a random reroll decision (50% chance to reroll, 50% chance to keep each die)
     */
    private fun makeRandomRerollDecision(computerDice: List<Die>, currentRollNumber: Int): List<Die> {
        // First decide if computer wants to reroll at all (random decision)
        val shouldReroll = if (currentRollNumber < 3) {
            (0..1).random() == 1 // 50% chance to reroll
        } else {
            false // Must score after 3 rolls
        }

        if (!shouldReroll) {
            return computerDice
        }

        // Randomly decide which dice to keep
        return computerDice.map { die ->
            if ((0..1).random() == 1) { // 50% chance to keep each die
                die // Keep this die
            } else {
                Die.random() // Reroll this die
            }
        }
    }

    /**
     * Updates the target score
     */
    fun updateTargetScore(newTarget: Int) {
        _gameState.update { it.copy(targetScore = newTarget) }
        saveState()
    }

    /**
     * Resets all dice selections
     */
    fun resetDiceSelections() {
        val updatedDice = _gameState.value.playerDice.map { it.copy(isSelected = false) }
        _gameState.update { it.copy(playerDice = updatedDice) }
        saveState()
    }
}