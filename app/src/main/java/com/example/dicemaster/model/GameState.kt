package com.example.dicemaster.model


/**
 * Game state and parameters
 */
data class GameState(
    // Player dice
    val playerDice: List<Die> = List(5) { Die.random() },
    val computerDice: List<Die> = List(5) { Die.random() },

    // Scores
    val playerScore: Int = 0,
    val computerScore: Int = 0,

    // Current roll values
    val playerCurrentRollValue: Int = 0,
    val computerCurrentRollValue: Int = 0,

    // Game settings
    val targetScore: Int = 101,

    // Roll tracking
    val currentRollNumber: Int = 1, // 1, 2, or 3 (max 3 rolls per turn)
    val playerAttempts: Int = 0,    // Number of completed turns
    val computerAttempts: Int = 0,  // Number of completed turns

    // Game state tracking
    val isGameOver: Boolean = false,
    val isPlayerWinner: Boolean = false,
    val isTie: Boolean = false,
    val isTiebreaking: Boolean = false,

    // Win statistics
    val playerWins: Int = 0,
    val computerWins: Int = 0
) {
    /**
     * Calculates the current value of a set of dice
     */
    fun calculateDiceValue(dice: List<Die>): Int {
        return dice.sumOf { it.value }
    }
}