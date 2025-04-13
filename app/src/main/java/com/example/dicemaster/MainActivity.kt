package com.example.dicemaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dicemaster.components.AboutDialog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Log the start of MainActivity creation
            Log.d("MainActivity", "onCreate started")

            // Set the content view to the main layout
            setContentView(R.layout.activity_main)
            Log.d("MainActivity", "setContentView completed")

            // Find UI elements
            val btnNewGame = findViewById<Button>(R.id.btnNewGame)
            val btnAbout = findViewById<Button>(R.id.btnAbout)
            val ivDice = findViewById<ImageView>(R.id.ivDice)

            Log.d("MainActivity", "findViewById completed")

            // Set button click listeners
            btnNewGame.setOnClickListener {
                // Navigate to Game Screen
                val intent = Intent(this, GameActivity::class.java)
                startActivity(intent)
            }

            btnAbout.setOnClickListener {
                // Show the About Dialog
                showAboutDialog()
            }

            // Animate the dice (simple rotation)
            ivDice.animate()
                .rotation(360f)
                .setDuration(3000)
                .withEndAction {
                    // Reset rotation and start again
                    ivDice.rotation = 0f
                    ivDice.animate()
                        .rotation(360f)
                        .setDuration(3000)
                        .start()
                }
                .start()

            Log.d("MainActivity", "onCreate completed successfully")

        } catch (e: Exception) {
            // Log any errors to help diagnose the issue
            Log.e("MainActivity", "Error in onCreate: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showAboutDialog() {
        // For XML-based AboutDialog
        val aboutDialogFragment = AboutDialogFragment()
        aboutDialogFragment.show(supportFragmentManager, "AboutDialog")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
    }
}