package my.sudoku.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.widget.SimpleAdapter
import java.util.Locale

class LoadActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        val listView: ListView = findViewById(R.id.boardsListView)

        // Retrieve the list of saved boards with timers and status
        val savedBoardsWithDetails = getSavedBoardsWithDetails(this)

        if (savedBoardsWithDetails.isEmpty()) {
            Toast.makeText(this, "No saved boards available.", Toast.LENGTH_SHORT).show()
            finish() // Close activity if no boards are available
            return
        }

        // Prepare data for SimpleAdapter
        val data = savedBoardsWithDetails.map { (name, timer, isFinished) ->
            mapOf(
                "name" to name,
                "details" to "Time: $timer | ${if (isFinished) "Finished" else "Incomplete"}"
            )
        }

        // Create and set adapter for ListView
        val adapter = SimpleAdapter(
            this,
            data,
            android.R.layout.simple_list_item_2,
            arrayOf("name", "details"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        listView.adapter = adapter

        // Handle board selection
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedBoard = savedBoardsWithDetails[position].first

            // Navigate back to MainActivity with the selected board name
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("GAME_MODE", "load")
            intent.putExtra("BOARD_NAME", selectedBoard)
            startActivity(intent)
        }
    }

    private fun getSavedBoardsWithDetails(context: Context): List<Triple<String, String, Boolean>> {
        val sharedPreferences = context.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
        val savedBoards = sharedPreferences.getStringSet("SavedBoards", mutableSetOf()) ?: return emptyList()

        return savedBoards.map { boardName ->
            val elapsedTime = sharedPreferences.getLong("${boardName}_elapsedTime", 0L)
            val formattedTime = formatElapsedTime(elapsedTime)
            val isFinished = sharedPreferences.getBoolean("${boardName}_isFinished", false)
            Triple(boardName, formattedTime, isFinished)
        }
    }
    private fun formatElapsedTime(elapsedMillis: Long): String {
        val minutes = (elapsedMillis / 1000) / 60
        val seconds = (elapsedMillis / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

}
