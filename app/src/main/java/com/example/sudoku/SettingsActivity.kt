package com.example.sudoku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val returnButton = findViewById<Button>(R.id.returnButton)
        returnButton.visibility = View.VISIBLE
        if (returnButton == null) {
            Log.e("OnCreate", "Return button is invalid")
        }

        returnButton.setOnClickListener {
            Log.i("returnButtonOnClick", "Return to the game")
            val mainActivity = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(mainActivity)
        }
    }
}
