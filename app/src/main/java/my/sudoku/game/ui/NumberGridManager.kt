package my.sudoku.game.ui

import GameState
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import my.sudoku.game.GRID_SIZE
import my.sudoku.game.KEY_ERROR_CHECKING
import my.sudoku.game.NUMBER_MARGIN_BUFFER
import my.sudoku.game.PREFS_NAME
import my.sudoku.game.R
import my.sudoku.game.game.GameLogic
import my.sudoku.game.viewmodel.GameViewModel
import java.util.Locale

class NumberGridManager(private val context: Context) {
    // Member variable for the Sudoku grid
    private val numberGrid: GridLayout? = if (context is Activity) {
        context.findViewById(R.id.numberGrid)
    } else {
        Log.e("UIManager", "Failed to retrieve sudokuGrid - context is not an Activity")
        null
    }
    fun getNumberGrid(): GridLayout? {
        return numberGrid
    }

    private val sudokuGridManager = SudokuGridManager(context)

    fun initializeNumberButtons(
        gameLogic: GameLogic,
        viewModel: GameViewModel
    ) {
        val gameState = viewModel.getGameState()
        for (number in 1..9) {
            val button = Button(context).apply {
                text = String.format(Locale.getDefault(), "%d", number)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 200
                    height = 200
                    setMargins(4, 4, 4, 4) // Default margins between buttons

                    // Adjust margins for the first, fourth, and seventh buttons
                    when (number) {
                        1 -> setMargins(NUMBER_MARGIN_BUFFER, 4, 4, 4) // Push the first button to the right
                        4 -> setMargins(NUMBER_MARGIN_BUFFER, 4, 4, 4) // Adjust the fourth button
                        7 -> setMargins(NUMBER_MARGIN_BUFFER, 4, 4, 4) // Adjust the seventh button
                    }
                    gravity = Gravity.CENTER
                }

                setOnClickListener {
                    onNumberClicked(context, number, sudokuGridManager.getSudokuGrid()!!, gameState.value!!, gameLogic, viewModel)

                    if (isErrorCheckingEnabled()) {
                        viewModel.getSelectedCell()?.let { (row, col) ->
                            val cellIndex = row * 9 + col
                            if (gameLogic.checkInput(gameState.value?.board!!, row, col, number)) {
                                (sudokuGridManager.getSudokuGrid()!!.getChildAt(cellIndex) as? EditText)?.setBackgroundColor(
                                    Color.GREEN
                                )
                            } else {
                                (sudokuGridManager.getSudokuGrid()!!.getChildAt(cellIndex) as? EditText)?.setBackgroundColor(
                                    Color.RED
                                )
                            }

                            Handler(Looper.getMainLooper()).postDelayed({
                                (sudokuGridManager.getSudokuGrid()!!.getChildAt(cellIndex) as? EditText)?.setBackgroundColor(
                                    ContextCompat.getColor(context, R.color.cell_background))
                                // Reset to default background
                            }, 250) // Adjust the delay as necessary
                        }
                    }
                }
            }
            numberGrid!!.addView(button)
        }
    }

    private fun onNumberClicked(
        context: Context,
        number: Int,
        sudokuGrid: GridLayout,
        gameState: GameState,
        gameLogic: GameLogic,
        viewModel: GameViewModel
    ) {
        viewModel.getSelectedCell()?.let { (row, col) ->
            val cellIndex = row * 9 + col
            val selectedCell = sudokuGrid.getChildAt(cellIndex) as? EditText

            // Simulate inputting the number into the Sudoku cell
            selectedCell?.setBackgroundColor(ContextCompat.getColor(context, R.color.cell_background))

            // Update the cell text
            selectedCell?.setText(if (number != 0) number.toString() else "")
            gameState.board[row][col] = number
        } ?: Toast.makeText(context, "Select a cell first!", Toast.LENGTH_SHORT).show()

        if (areAllCellsFilled(sudokuGrid)) {
            val problematicCells = gameLogic.confirmEditableCells(gameState.editableCells, gameState.board, gameLogic)

            if (showCorrectCells(sudokuGrid, gameState.editableCells, problematicCells)) {
                viewModel.setFinished(true)
                showSaveScreen(context)
            }
        }
    }

    private fun showCorrectCells(
        sudokuGrid: GridLayout,
        editableCells: MutableMap<Pair<Int, Int>, Boolean>,
        problematicCells: Set<Pair<Int, Int>>
    ): Boolean {
        var finished = true

        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val cell = sudokuGrid.getChildAt(row * GRID_SIZE + col) as EditText
                val cellKey = Pair(row, col)

                when {
                    problematicCells.contains(cellKey) -> {
                        cell.setBackgroundColor(Color.RED) // Highlight problematic cells in red
                        finished = false
                    }
                    editableCells[cellKey] == false -> {
                        cell.setBackgroundColor(Color.RED) // Highlight correct editable cells in green
                    }
                    editableCells[cellKey] == true -> {
                        cell.setBackgroundColor(Color.GREEN) // Highlight correct editable cells in green
                    }
                }
            }
        }

        // Reset highlighting after a delay, if needed
        Handler(Looper.getMainLooper()).postDelayed({
            for (row in 0 until GRID_SIZE) {
                for (col in 0 until GRID_SIZE) {
                    val cell = sudokuGrid.getChildAt(row * GRID_SIZE + col) as EditText
                    cell.setBackgroundResource(R.color.cell_background) // Reset to default background
                }
            }
        }, 2000) // Adjust the delay as necessary

        return finished
    }

    fun isErrorCheckingEnabled(
    ): Boolean {
        if (context is Activity) {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(KEY_ERROR_CHECKING, false)
        }

        return false
    }

    private fun areAllCellsFilled(
        sudokuGrid: GridLayout
    ): Boolean {
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val cell = sudokuGrid.getChildAt(row * GRID_SIZE + col) as EditText
                if (cell.text.toString().isEmpty()) {
                    return false // Found an empty cell
                }
            }
        }
        return true // All cells are filled
    }

    private fun showSaveScreen(
        context: Context
    ) {
        if (context is Activity) {
            val confirmSaveButton = context.findViewById<Button>(R.id.confirmSaveButton)
            val boardNameInput = context.findViewById<EditText>(R.id.boardNameInput)
            val sudokuGrid = context.findViewById<GridLayout>(R.id.sudokuGrid)
            val numberGrid = context.findViewById<GridLayout>(R.id.numberGrid)

            confirmSaveButton.visibility = View.VISIBLE
            boardNameInput.visibility = View.VISIBLE
            sudokuGrid.visibility = View.GONE
            numberGrid.visibility = View.GONE
        }
    }
}