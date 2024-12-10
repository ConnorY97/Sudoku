package com.example.sudoku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.widget.SimpleAdapter

class LoadActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        val listView: ListView = findViewById(R.id.boardsListView)

        // Retrieve the list of saved boards with timers
        val savedBoardsWithTimers = getSavedBoardsWithTimers(this)

        if (savedBoardsWithTimers.isEmpty()) {
            Toast.makeText(this, "No saved boards available.", Toast.LENGTH_SHORT).show()
            finish() // Close activity if no boards are available
            return
        }

        // Prepare data for SimpleAdapter
        val data = savedBoardsWithTimers.map { (name, timer) ->
            mapOf("name" to name, "timer" to timer)
        }

        // Create and set adapter for ListView
        val adapter = SimpleAdapter(
            this,
            data,
            android.R.layout.simple_list_item_2,
            arrayOf("name", "timer"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        listView.adapter = adapter

        // Handle board selection
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedBoard = savedBoardsWithTimers[position].first

            // Navigate back to MainActivity with the selected board name
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("GAME_MODE", "load")
            intent.putExtra("BOARD_NAME", selectedBoard)
            startActivity(intent)
        }
    }

    private fun getSavedBoardsWithTimers(context: Context): List<Pair<String, String>> {
        val sharedPreferences = context.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
        val savedBoards = sharedPreferences.getStringSet("SavedBoards", mutableSetOf()) ?: return emptyList()

        return savedBoards.map { boardName ->
            val elapsedTime = sharedPreferences.getLong("${boardName}_elapsedTime", 0L)
            val formattedTime = formatElapsedTime(elapsedTime)
            boardName to formattedTime
        }
    }

    private fun formatElapsedTime(elapsedMillis: Long): String {
        val minutes = (elapsedMillis / 1000) / 60
        val seconds = (elapsedMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
