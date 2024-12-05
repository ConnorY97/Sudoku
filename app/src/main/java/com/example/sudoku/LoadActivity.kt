package com.example.sudoku

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity

class LoadActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        val listView: ListView = findViewById(R.id.boardsListView)
        val sharedPreferences = getSharedPreferences("SudokuGame", MODE_PRIVATE)

        // Retrieve the list of saved board names
        val savedBoards = sharedPreferences.getStringSet("SavedBoards", emptySet())?.toList() ?: emptyList()

        if (savedBoards.isEmpty()) {
            Toast.makeText(this, "No saved boards available.", Toast.LENGTH_SHORT).show()
            finish() // Close activity if no boards are available
            return
        }

        // Populate the ListView with the saved board names
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, savedBoards)
        listView.adapter = adapter

        // Handle board selection
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedBoard = savedBoards[position]

            // Navigate back to MainActivity with the selected board name
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("GAME_MODE", "load")
            intent.putExtra("BOARD_NAME", selectedBoard)
            startActivity(intent)
        }
    }
}
