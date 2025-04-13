package com.example.dicemaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.dicemaster.screens.GameScreen
import com.example.dicemaster.viewmodel.GameViewModel

class GameActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameScreen(
                gameViewModel = gameViewModel,
                onBackToStart = { finish() }
            )
        }
    }
}