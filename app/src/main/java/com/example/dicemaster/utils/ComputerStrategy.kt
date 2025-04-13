package com.example.dicemaster.utils



import com.example.dicemaster.model.Die

/**
 * Implements an optimal strategy for the computer player.
 *
 * Strategy Overview:
 * 1. Early Game (Score Gap <= 30):
 *    - Focus on maximizing the current roll's value
 *    - Keep dice with high values (4, 5, 6)
 *    - Reroll dice with low values (1, 2, 3)
 *
 * 2. Mid Game (Player ahead by > 30 but < 60):
 *    - Take more risks to catch up
 *    - Only keep very high values (5, 6)
 *    - Reroll everything else
 *
 * 3. Late Game (Player ahead by >= 60):
 *    - Maximum risk strategy
 *    - Only keep 6s
 *    - Reroll everything else
 *
 * 4. Leading Game (Computer ahead):
 *    - Play conservatively
 *    - Keep more dice (values 3, 4, 5, 6)
 *    - Only reroll very low values (1, 2)
 *
 * 5. End Game (Near Target):
 *    - If within striking distance of target (< 30 points away):
 *      - Calculate optimal strategy based on what is needed to win
 *      - If current roll would win, just keep it
 *      - If not, calculate whether rerolling would improve chances
 *
 * Advantages:
 * - Adapts to the game state dynamically
 * - Makes appropriate risk/reward decisions
 * - Considers the score differential to optimize play
 * - Makes logical decisions near the end of the game
 *
 * Disadvantages:
 * - Does not account for all possible probability distributions
 * - Simplified approach rather than a full probability tree calculation
 * - May make suboptimal decisions in specific edge cases
 */
object ComputerStrategy {

    /**
     * Determines whether to reroll and which dice to keep based on game state
     *
     * @param computerDice Current dice the computer has
     * @param computerScore Current computer score in the game
     * @param playerScore Current player score in the game
     * @param targetScore The score needed to win
     * @param currentRollNumber Current roll number (1, 2, or 3)
     * @return Updated list of dice (kept or rerolled)
     */

    // Make sure makeOptimalDecision handles special cases properly
    fun makeOptimalDecision(
        computerDice: List<Die>,
        computerScore: Int,
        playerScore: Int,
        targetScore: Int,
        currentRollNumber: Int
    ): List<Die> {
        // If this is the last roll (3), we must score it
        if (currentRollNumber >= 3) {
            return computerDice
        }

        // Calculate scores and gaps
        val currentRollValue = computerDice.sumOf { it.value }
        val computerTotal = computerScore + currentRollValue
        val scoreDifference = playerScore - computerScore
        val pointsToTarget = targetScore - computerTotal

        // If we've hit the target with this roll, keep it
        if (computerTotal >= targetScore) {
            return computerDice
        }

        // If we're very close to the target (within 10 points), be more strategic
        if (pointsToTarget <= 10) {
            return endGameStrategy(computerDice, computerScore, playerScore, targetScore)
        }

        // Choose appropriate strategy based on game state
        return when {
            // Computer is ahead - play conservatively
            scoreDifference < 0 -> conservativeStrategy(computerDice)

            // Player is far ahead - take risks
            scoreDifference >= 60 -> highRiskStrategy(computerDice)

            // Player is ahead - take moderate risks
            scoreDifference >= 30 -> moderateRiskStrategy(computerDice)

            // Early game or close game - standard strategy
            else -> standardStrategy(computerDice)
        }
    }

    /**
     * Standard strategy for early game or close game situations
     * Keep 4, 5, 6 and reroll others
     */
    private fun standardStrategy(dice: List<Die>): List<Die> {
        return dice.map { die ->
            if (die.value >= 4) {
                // Keep high values
                die
            } else {
                // Reroll low values
                Die.random()
            }
        }
    }

    /**
     * Conservative strategy when computer is ahead
     * Keep 3, 4, 5, 6 and reroll others
     */
    private fun conservativeStrategy(dice: List<Die>): List<Die> {
        return dice.map { die ->
            if (die.value >= 3) {
                // Keep medium to high values
                die
            } else {
                // Reroll very low values
                Die.random()
            }
        }
    }

    /**
     * Moderate risk strategy when player is ahead
     * Keep 5, 6 and reroll others
     */
    private fun moderateRiskStrategy(dice: List<Die>): List<Die> {
        return dice.map { die ->
            if (die.value >= 5) {
                // Keep only high values
                die
            } else {
                // Reroll low to medium values
                Die.random()
            }
        }
    }

    /**
     * High risk strategy when player is far ahead
     * Keep only 6s and reroll others
     */
    private fun highRiskStrategy(dice: List<Die>): List<Die> {
        return dice.map { die ->
            if (die.value == 6) {
                // Keep only the highest value
                die
            } else {
                // Reroll everything else
                Die.random()
            }
        }
    }

    /**
     * End game strategy for situations close to the target score
     */
    private fun endGameStrategy(
        dice: List<Die>,
        computerScore: Int,
        playerScore: Int,
        targetScore: Int
    ): List<Die> {
        val currentRollValue = dice.sumOf { it.value }
        val computerTotal = computerScore + currentRollValue

        // If this roll would win, keep it
        if (computerTotal >= targetScore) {
            return dice
        }

        // Calculate the average improvement we might expect from rerolling
        val diceToReroll = dice.filter { it.value <= 3 }

        // If we don't have any low values, keep what we have
        if (diceToReroll.isEmpty()) {
            return dice
        }

        // Calculate expected gain from rerolling low dice
        // Average die value is 3.5, so expected gain per die is (3.5 - dieValue)
        val expectedGain = diceToReroll.sumOf { 3.5 - it.value }

        // If expected gain is significant or we need a big improvement, reroll low dice
        return if (expectedGain >= 2.0 || targetScore - computerTotal > 10) {
            dice.map { die ->
                if (die.value <= 3) {
                    Die.random()
                } else {
                    die
                }
            }
        } else {
            // Otherwise, keep what we have
            dice
        }
    }
}