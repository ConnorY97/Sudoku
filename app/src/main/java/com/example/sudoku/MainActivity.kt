package com.example.sudoku

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import kotlin.random.Random
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val checkSolutionButton = findViewById<Button>(R.id.btnCheckSolution)  // Define the button
        val saveGameButton = findViewById<Button>(R.id.btnSaveGame)
        val sudokuGrid: GridLayout = findViewById(R.id.sudokuGrid)
        val boardNameInput = findViewById<EditText>(R.id.boardNameInput)
        val confirmSaveButton = findViewById<Button>(R.id.confirmSaveButton)
        val gridSize = 9

        // Retrieve difficulty level from the Intent
        val difficulty = intent.getStringExtra("DIFFICULTY_LEVEL") ?: "easy"

        // Generate the board
        val fullBoard = generateFullBoard() // Full solved board
        val puzzleBoard = createPuzzle(fullBoard, difficulty) // Puzzle with empty cells

        initializeGrid(this, gridSize, puzzleBoard, sudokuGrid)

        checkSolutionButton.setOnClickListener {
            checkSolutions(gridSize, sudokuGrid, fullBoard)
        }

        saveGameButton.setOnClickListener {
            // Hide the board while we take input
            sudokuGrid.visibility = View.INVISIBLE
            checkSolutionButton.visibility = View.INVISIBLE
            saveGameButton.visibility = View.INVISIBLE
            // Show the input field and confirm button
            boardNameInput.visibility = View.VISIBLE
            confirmSaveButton.visibility = View.VISIBLE
            //showSaveDialog(this, puzzleBoard, fullBoard)
        }

        confirmSaveButton.setOnClickListener {
            // Get the board name from the input field
            val boardName = boardNameInput.text.toString().trim()

            if (boardName.isEmpty()) {
                // Show a toast if the board name is empty
                Toast.makeText(this, "Please enter a valid name.", Toast.LENGTH_SHORT).show()
            } else {
                // Save the game with the entered board name
                val success = saveGame(this, boardName, puzzleBoard, fullBoard)
                if (success) {
                    Toast.makeText(this, "Board saved successfully!", Toast.LENGTH_SHORT).show()

                    // Hide the input field and confirm button after saving
                    boardNameInput.visibility = View.GONE
                    confirmSaveButton.visibility = View.GONE

                    // Clear the input field for future use
                    boardNameInput.text.clear()

                    // Optionally, hide the board and buttons
                    sudokuGrid.visibility = View.VISIBLE
                    checkSolutionButton.visibility = View.VISIBLE
                    saveGameButton.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Name already exists. Choose another name.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}

// Initialize the Sudoku grid with EditTexts, as you already did in the previous code
private fun initializeGrid(context: Context, gridSize: Int, puzzleBoard: Array<IntArray>, sudokuGrid: GridLayout) {
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            val cell = EditText(context)
            cell.layoutParams = GridLayout.LayoutParams().apply {
                width = 100 // Reduce the width slightly
                height = 100 // Keep the height as is
                columnSpec = GridLayout.spec(col)
                rowSpec = GridLayout.spec(row)

                // Apply thicker margins for 3rd and 6th rows and columns
                setMargins(
                    if (col % 3 == 0 && col != 0) 4 else 1,  // Left margin
                    if (row % 3 == 0 && row != 0) 4 else 1,  // Top margin
                    1,                                       // Right margin
                    1                                        // Bottom margin
                )
            }

            cell.textAlignment = EditText.TEXT_ALIGNMENT_CENTER

            cell.setBackgroundResource(android.R.color.white)
            cell.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            cell.maxLines = 1  // Ensure only 1 line of input
            cell.filters = arrayOf(InputFilter.LengthFilter(1))  // Limit input to one digit
            cell.gravity = Gravity.CENTER // Center text both vertically and horizontally
            cell.setPadding(10, 10, 10, 10) // Add padding around text

            // Decide whether to show a number or leave the cell empty (random chance)
            val number = puzzleBoard[row][col]
            if (number != 0) {
                cell.setText(number.toString()) // Pre-fill number
                cell.isFocusable = false // Make non-editable
                cell.isClickable = false // Prevent interaction
                cell.setTypeface(null, android.graphics.Typeface.BOLD)
                //cell.setBackgroundColor(Color.LTGRAY) // Shade pre-filled cells
            } else {
                cell.isFocusableInTouchMode = true // Allow user input on empty cells
            }

            sudokuGrid.addView(cell)
        }
    }
}

// Set the button click listener here
private fun checkSolutions(gridSize: Int, sudokuGrid: GridLayout, fullBoard: Array<IntArray>) {
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            val cell = sudokuGrid.getChildAt(row * gridSize + col) as EditText
            val userInput = cell.text.toString()

            // Only check cells that the user has entered something into
            if (userInput.isNotEmpty() && cell.isFocusable) {
                val userAnswer = userInput.toInt()
                val correctAnswer = fullBoard[row][col]

                // Check if the user's answer matches the correct answer
                if (userAnswer == correctAnswer) {
                    cell.setBackgroundColor(Color.GREEN)  // Correct: green
                } else {
                    cell.setBackgroundColor(Color.RED)    // Incorrect: red
                }

                // Reset the cell's color after a slight delay
                Handler().postDelayed({
                    cell.setBackgroundColor(Color.WHITE) // Reset to white
                }, 1000) // 1000ms (1 second) delay
            }
        }
    }
}

// Function to generate a full Sudoku board
private fun generateFullBoard(): Array<IntArray> {
    val board = Array(9) { IntArray(9) { 0 } }
    fillBoard(board)
    return board
}

// Backtracking algorithm to fill the board
private fun fillBoard(board: Array<IntArray>): Boolean {
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

// Check if placing a number is valid
private fun isValidMove(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
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

// Function to remove numbers based on difficulty
private fun createPuzzle(board: Array<IntArray>, difficulty: String): Array<IntArray> {
    val chanceToBeEmpty = when (difficulty) {
        "easy" -> 0.2 // 20% cells empty
        "medium" -> 0.5 // 50% cells empty
        "hard" -> 0.7 // 70% cells empty
        else -> 0.2
    }

    val puzzle = board.map { it.clone() }.toTypedArray() // Clone the board
    for (row in 0 until 9) {
        for (col in 0 until 9) {
            if (Random.nextFloat() < chanceToBeEmpty) {
                puzzle[row][col] = 0 // Empty the cell
            }
        }
    }
    return puzzle
}

fun saveGame(context: Context, boardName: String, puzzleBoard: Array<IntArray>, solvedBoard: Array<IntArray>): Boolean {
    val sharedPreferences = context.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    // Retrieve existing saved boards
    val savedBoards = sharedPreferences.getStringSet("SavedBoards", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

    // Ensure the board name is unique
    if (savedBoards.contains(boardName)) {
        return false // Indicate failure if the board name already exists
    }

    // Add the new board name to the list
    savedBoards.add(boardName)
    editor.putStringSet("SavedBoards", savedBoards)

    // Save the puzzle and solution boards as JSON strings
    val gson = Gson()
    editor.putString("${boardName}_puzzleBoard", gson.toJson(puzzleBoard))
    editor.putString("${boardName}_solvedBoard", gson.toJson(solvedBoard))

    // Commit the changes
    editor.apply()

    return true // Indicate success
}

fun loadGame(context: Context, boardName: String): Pair<Array<IntArray>?, Array<IntArray>?> {
    val sharedPreferences = context.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
    val gson = Gson()

    // Retrieve boards from JSON strings
    val puzzleBoardJson = sharedPreferences.getString("${boardName}_puzzleBoard", null)
    val solvedBoardJson = sharedPreferences.getString("${boardName}_solvedBoard", null)

    val puzzleBoard = if (puzzleBoardJson != null) gson.fromJson(puzzleBoardJson, Array<IntArray>::class.java) else null
    val solvedBoard = if (solvedBoardJson != null) gson.fromJson(solvedBoardJson, Array<IntArray>::class.java) else null

    return Pair(puzzleBoard, solvedBoard)
}

