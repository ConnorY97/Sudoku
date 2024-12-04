package com.example.sudoku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class HomeActivity : ComponentActivity() {  // Using ComponentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)  // Set the home screen layout

        // Find the start game button
        val startGameButton = findViewById<Button>(R.id.startGameButton)

        // Set the click listener to navigate to the game board
        startGameButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // Navigate to the Sudoku game
            startActivity(intent)
        }
    }
}
