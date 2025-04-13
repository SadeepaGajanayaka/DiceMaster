package com.example.dicemaster.model

/**
 * Represents a single die in the game
 */
data class Die(
    val value: Int = 1,        // Value shown on the die (1-6)
    val isSelected: Boolean = false // Whether this die is selected to keep during reroll
) {
    companion object {
        /**
         * Creates a die with random value
         */
        fun random(): Die {
            return Die(value = (1..6).random())
        }
    }
}