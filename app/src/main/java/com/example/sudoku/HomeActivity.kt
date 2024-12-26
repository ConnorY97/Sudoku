package com.example.sudoku

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        try {
            val backgroundDrawable = resources.getDrawable(R.drawable.ic_launcher_background, null)
            val foregroundDrawable = resources.getDrawable(R.drawable.ic_launcher_foreground, null)
            Log.d("Debug", "Resources loaded successfully.")
        } catch (e: Resources.NotFoundException) {
            Log.e("Debug", "Error loading resources", e)
        }


        // Easy Button
        val easyButton = findViewById<Button>(R.id.easyButton)
        easyButton.setOnClickListener {
            startGame("easy")
        }

        // Medium Button
        val mediumButton = findViewById<Button>(R.id.mediumButton)
        mediumButton.setOnClickListener {
            startGame("medium")
        }

        // Hard Button
        val hardButton = findViewById<Button>(R.id.hardButton)
        hardButton.setOnClickListener {
            startGame("hard")
        }

        val loadButton = findViewById<Button>(R.id.loadGameButton)
        loadButton.setOnClickListener {
            loadBoard()
        }
    }

    private fun startGame(difficulty: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("DIFFICULTY_LEVEL", difficulty) // Pass difficulty level
        startActivity(intent)
    }

    private fun loadBoard() {
        val intent = Intent(this, LoadActivity::class.java)
        startActivity(intent)
    }
}