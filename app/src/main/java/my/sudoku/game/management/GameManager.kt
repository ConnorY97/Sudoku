package my.sudoku.game.management

import GameState
import android.content.Context
import com.google.gson.Gson
import my.sudoku.game.viewmodel.GameViewModel

class GameManager(private val context: Context) {
    private val sharedPreferences by lazy {
        context.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
    }
    private val gson = Gson()

    // Save game state
    fun saveGame(
        boardName: String,
        gameState: GameState,
        finalTime: Long,
        viewModel: GameViewModel,
        loaded: Boolean = false
    ): Boolean {
        val editor = sharedPreferences.edit()

        // Retrieve existing saved boards
        val savedBoards = sharedPreferences.getStringSet("SavedBoards", mutableSetOf()) ?: mutableSetOf()

        // Ensure unique board name and it has not been loaded
        if (savedBoards.contains(boardName) && !loaded) {
            return false // Indicate failure
        }

        // Add new board name to saved list
        savedBoards += boardName
        editor.putStringSet("SavedBoards", savedBoards)

        // Save board and editable cells
        val gson = Gson()
        editor.putString("${boardName}_board", gson.toJson(gameState.board))
        editor.putString("${boardName}_editableCells", gson.toJson(gameState.editableCells))

        // Save timer
        editor.putLong("${boardName}_elapsedTime", finalTime)

        // Save whether the game is finished
        editor.putBoolean("${boardName}_isFinished", viewModel.getFinished())

        // Commit changes
        editor.apply()
        return true // Indicate success
    }

    // Load game state
    fun loadGame(
                 boardName: String
    ): GameState {
        // Load board
        val boardJson = sharedPreferences.getString("${boardName}_board", null)
        val board = if (boardJson != null) gson.fromJson(boardJson, Array<IntArray>::class.java) else null

        // Load editable cells
        val editableCellsJson = sharedPreferences.getString("${boardName}_editableCells", null)
        val editableCells = if (editableCellsJson != null) {
            val tempMap = gson.fromJson(editableCellsJson, Map::class.java) as Map<*, *>

            tempMap.mapNotNull { (key, value) ->
                // Parse the string key "(x, y)" into a Pair<Int, Int>
                val match = "\\((\\d+),\\s*(\\d+)\\)".toRegex().matchEntire(key as String)
                val (first, second) = match?.destructured ?: return@mapNotNull null
                Pair(first.toInt(), second.toInt()) to value as Boolean
            }.toMap()
        } else {
            null
        }

        // Load elapsed time
        val elapsedTime = sharedPreferences.getLong("${boardName}_elapsedTime", 0)

        // Load if the game is finished
        val finished = sharedPreferences.getBoolean("${boardName}_isFinished", false)

        val game = GameState(board as Array<IntArray>, editableCells as MutableMap<Pair<Int, Int>, Boolean>, elapsedTime, finished)

        return game
    }
}

