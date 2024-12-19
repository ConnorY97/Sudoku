package com.example.sudoku

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.Chronometer
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

// Constants
const val gridSize = 9

class MainActivity : ComponentActivity() {
    // Variables
    private lateinit var sudokuGrid: GridLayout
    private val editableCells: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()
    private var sudokuBoard = Array(gridSize) { IntArray(gridSize) { 0 } }

    // Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate", "Entered main activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("onCreate", "Initializing Variables")
        sudokuBoard = generatePuzzle(this,"Easy", editableCells)

        Log.i("onCreate", "Initializing UI")
        sudokuGrid = findViewById(R.id.sudokuGrid)
        initializeGrid(this, sudokuGrid, sudokuBoard, editableCells)
    }
}

fun areAllCellsFilled(gridSize: Int, sudokuGrid: GridLayout): Boolean {
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            val cell = sudokuGrid.getChildAt(row * gridSize + col) as EditText
            if (cell.text.toString().isEmpty()) {
                return false // Found an empty cell
            }
        }
    }
    return true // All cells are filled
}

fun initializeGrid(
    context: Context,
    sudokuGrid: GridLayout,
    board: Array<IntArray>,
    editableCells: MutableMap<Pair<Int, Int>, Boolean>) {
    Log.i("initializeGrid", "Initializing")
    // Set up the grid for Sudoku
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            val cell = EditText(context)
            cell.layoutParams = GridLayout.LayoutParams().apply {
                width = 100
                height = 100
                columnSpec = GridLayout.spec(col)
                rowSpec = GridLayout.spec(row)
                setMargins(
                    if (col % 3 == 0 && col != 0) 4 else 1,
                    if (row % 3 == 0 && row != 0) 4 else 1,
                    1,
                    1
                )
            }

            cell.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
            cell.setBackgroundResource(R.color.cell_background)
            cell.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            cell.maxLines = 1
            cell.filters = arrayOf(InputFilter.LengthFilter(1))
            cell.gravity = Gravity.CENTER
            cell.setPadding(10, 10, 10, 10)

            // Populate the cell based on puzzleBoard
            val number = board[row][col]
            if (number != 0) {
                cell.setText(String.format(Locale.getDefault(), "%d", number))
            } else {
                cell.setText("")
            }

            // Set cell interactivity based on interactableBoard
            val isEditable = Pair(row, col) in editableCells

            if (isEditable) {
                cell.isFocusableInTouchMode = true

                // Add TextWatcher for user input
                cell.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // Check if all cells are filled
                        if(areAllCellsFilled(gridSize, sudokuGrid)) {
                            Toast.makeText(context, "Board Filled", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            } else {
                cell.isFocusable = false
                cell.isClickable = false
                cell.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            sudokuGrid.addView(cell)
        }
    }
}

// Game Logic
fun generatePuzzle(context: Context,
                   difficulty: String,
                   editableCells: MutableMap<Pair<Int, Int>, Boolean>):
        Array<IntArray> {
    Log.i("generatePuzzle","Started generating puzzle")
    // Generate and return a new Sudoku puzzle
    var grid = Array(gridSize) { IntArray(gridSize) {0} }
    if (fillBoard(grid)) {
        Log.i("generatePuzzle","Board filled")
        if (validateBoard(grid)) {
            Log.i("generatePuzzle", "Validated full board")
            createPuzzle(grid, difficulty, editableCells)
            Log.i("generatePuzzle", "Created puzzle")
            if (validateBoard(grid)) {
                Log.i("generatePuzzle", "Validated puzzle")
            } else {
                Log.e("generatePuzzle", "Created puzzle failed validation check")
                Toast.makeText(context, "Puzzle generation failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("generatePuzzle", "Full board failed validation check")
            Toast.makeText(context, "Board generation failed", Toast.LENGTH_SHORT).show()
            if (context is Activity)
                context.finish()
        }
    }
    else {
        Log.e("generatePuzzle", "Failed to fill board")
        Toast.makeText(context, "Failed to fill board", Toast.LENGTH_SHORT).show()
        // Failed to fill the board successfully, return an empty grid
        grid = Array(gridSize) { IntArray(gridSize) {0} }
    }
    return grid
}

// Backtracking algorithm to fill the board
fun fillBoard(board: Array<IntArray>): Boolean {
    for (row in 0 until 9) {
        for (col in 0 until 9) {
            if (board[row][col] == 0) {
                val numbers = (1..9).shuffled() // Shuffle numbers for randomness
                for (num in numbers) {
                    if (isValidMove(board, row, col, num)) {
                        board[row][col] = num
                        if (fillBoard(board)) {
                            return true
                        }
                        board[row][col] = 0 // Backtrack
                    }
                }
                return false
            }
        }
    }
    return true
}

fun createPuzzle(board: Array<IntArray>,
                 difficulty: String,
                 editableCells: MutableMap<Pair<Int, Int>, Boolean>):
        Array<IntArray> {
    val chanceToBeEmpty = when (difficulty) {
        "easy" -> 0.2 // 20% cells empty
        "medium" -> 0.5 // 50% cells empty
        "hard" -> 0.7 // 70% cells empty
        else -> 0.2
    }
    for (row in 0 until 9) {
        for (col in 0 until 9) {
            if (Random.nextFloat() < chanceToBeEmpty) {
                board[row][col] = 0 // Empty the cell
                // Adding the empty cell to the editable array for future reference
                editableCells[Pair(row, col)] = false // Or false based on validity

            }
        }
    }
    return board
}

fun validateBoard(board: Array<IntArray>): Boolean {
    val gridSize = board.size
    val subGridSize = sqrt(gridSize.toDouble()).toInt() // 3 for a 9x9 board

    // Validate rows and columns
    for (i in 0 until gridSize) {
        if (!isUnique(board[i]) || !isUnique(getColumn(board, i))) {
            return false
        }
    }

    // Validate sub-grids
    for (row in 0 until gridSize step subGridSize) {
        for (col in 0 until gridSize step subGridSize) {
            if (!isUnique(getSubGrid(board, row, col, subGridSize))) {
                return false
            }
        }
    }

    return true
}

// Helper to check if all numbers in an array are unique (ignores zeros)
fun isUnique(array: IntArray): Boolean {
    val seen = mutableSetOf<Int>()
    for (num in array) {
        if (num != 0 && !seen.add(num)) {
            return false
        }
    }
    return true
}

// Helper to get a column as an array
fun getColumn(board: Array<IntArray>, col: Int): IntArray {
    return IntArray(board.size) { row -> board[row][col] }
}

// Helper to get a sub-grid as an array
fun getSubGrid(board: Array<IntArray>, startRow: Int, startCol: Int, size: Int): IntArray {
    val subGrid = mutableListOf<Int>()
    for (row in startRow until startRow + size) {
        for (col in startCol until startCol + size) {
            subGrid.add(board[row][col])
        }
    }
    return subGrid.toIntArray()
}

// Check if placing a number is valid
fun isValidMove(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
    for (i in 0 until 9) {
        if (board[row][i] == num || board[i][col] == num) {
            return false // Row or column conflict
        }
        if (board[row / 3 * 3 + i / 3][col / 3 * 3 + i % 3] == num) {
            return false // Subgrid conflict
        }
    }
    return true
}



// Utilityfunctions
fun formatElapsedTime(elapsedMillis: Long): String {
    val minutes = (elapsedMillis / 1000) / 60
    val seconds = (elapsedMillis / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

// Save game state
fun saveGameState() {
    // Logic to save the current board
}

// Load game state
fun loadGameState() {
    // Logic to load the saved board
}