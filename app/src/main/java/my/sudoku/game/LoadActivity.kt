package my.sudoku.game

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import java.util.Locale

class LoadActivity : ComponentActivity() {
    private lateinit var listView: ListView
    private lateinit var emptyStateTextView: TextView
    private lateinit var adapter: SavedGamesAdapter
    private var savedBoards: MutableList<Triple<String, String, Boolean>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        listView = findViewById(R.id.boardsListView)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        val clearAllButton: Button = findViewById(R.id.clearAllButton)

        loadSavedGames()

        // Set up Clear All button
        clearAllButton.setOnClickListener {
            showClearAllConfirmationDialog()
        }

        val returnButton: ImageButton = findViewById(R.id.backButton)
        returnButton.setOnClickListener {
            // Navigate to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Close LoadActivity to prevent back navigation to it
        }
    }

    private fun loadSavedGames() {
        savedBoards = getSavedBoardsWithDetails(this).toMutableList()

        if (savedBoards.isEmpty()) {
            listView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
            return
        }

        listView.visibility = View.VISIBLE
        emptyStateTextView.visibility = View.GONE

        adapter = SavedGamesAdapter(this, savedBoards)
        listView.adapter = adapter
    }


    fun showDeleteConfirmationDialog(boardName: String, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Save")
            .setMessage("Are you sure you want to delete this saved game?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSavedGame(boardName, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Saves")
            .setMessage("Are you sure you want to delete all saved games? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllSaves()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSavedGame(boardName: String, position: Int) {
        val sharedPreferences = getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
        val savedBoards = sharedPreferences.getStringSet("SavedBoards", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        // Remove the board and its associated data
        savedBoards.remove(boardName)
        sharedPreferences.edit().apply {
            putStringSet("SavedBoards", savedBoards)
            remove("${boardName}_elapsedTime")
            remove("${boardName}_isFinished")
            remove(boardName) // Remove the actual board data
            apply()
        }

        // Update the list
        this.savedBoards.removeAt(position)
        adapter.notifyDataSetChanged()

        // Show empty state if no saves remain
        if (this.savedBoards.isEmpty()) {
            listView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
        }
    }

    private fun clearAllSaves() {
        val sharedPreferences = getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        savedBoards.clear()
        adapter.notifyDataSetChanged()

        listView.visibility = View.GONE
        emptyStateTextView.visibility = View.VISIBLE
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

    fun loadGame(boardName: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("GAME_MODE", "load")  // Indicate that it's a load operation
        intent.putExtra("BOARD_NAME", boardName)  // Pass the selected board name
        startActivity(intent)
    }
}

class SavedGamesAdapter(
    private val context: Context,
    private val savedBoards: List<Triple<String, String, Boolean>>
) : BaseAdapter() {

    override fun getCount(): Int = savedBoards.size
    override fun getItem(position: Int): Any = savedBoards[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_save_game, parent, false)

        val (name, timer, isFinished) = savedBoards[position]

        // Set the text for the name and details TextViews
        view.findViewById<TextView>(R.id.nameTextView).text = name
        view.findViewById<TextView>(R.id.detailsTextView).text =
            context.getString(R.string.time, timer, if (isFinished) "Finished" else "Incomplete")

        // Delete button functionality
        view.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            if (context is LoadActivity) {
                context.showDeleteConfirmationDialog(name, position)
            }
        }

        // Play button functionality (load the game)
        view.findViewById<Button>(R.id.playButton).setOnClickListener {
            if (context is LoadActivity) {
                val selectedBoard = savedBoards[position].first // The board name
                context.loadGame(selectedBoard) // Call the load game function
            }
        }

        return view
    }
}

