package com.example.sudoku

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlin.random.Random
import com.google.gson.Gson
import java.util.Locale

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
        var fullBoard = Array(gridSize) { IntArray(gridSize) { 0 } } // Empty 9x9 grid
        var puzzleBoard = Array(gridSize) { IntArray(gridSize) { 0 } } // Empty 9x9 grid

//        val toolbar: Toolbar? = findViewById(R.id.toolbar)
//        if (toolbar == null) {
//            Log.e("MainActivity", "Toolbar is null. Check your layout XML.")
//        } else {
//            toolbar.title = "Sudoku"
//            toolbar.setNavigationIcon(android.R.drawable.ic_menu_directions)
//            toolbar.setNavigationOnClickListener {
//                finish() // Navigate back or handle as needed
//            }
//        }

        // Get the game mode from the Intent (null check instead of empty string check)
        val gameMode = intent.getStringExtra("GAME_MODE")

        if (!gameMode.isNullOrEmpty()) {
            // If in load game mode, try to load the board
            val boardName = intent.getStringExtra("BOARD_NAME")

            if (boardName != null) {
                // Try loading the saved game
                val (loadedPuzzleBoard, loadedFullBoard, loadedInteractableBoard) = loadGame(this, boardName)

                if (loadedPuzzleBoard != null && loadedFullBoard != null && loadedInteractableBoard != null) {
                    // Successfully loaded, initialize grid with the puzzleBoard
                    puzzleBoard = loadedPuzzleBoard
                    fullBoard = loadedFullBoard
                    initializeGrid(this, gridSize, puzzleBoard, loadedInteractableBoard, sudokuGrid)
                } else {
                    // If board loading failed, show a toast and navigate to Home
                    Toast.makeText(this, "Failed to load board, invalid name!", Toast.LENGTH_SHORT).show()
                    val homeScreen = Intent(this, HomeActivity::class.java)
                    startActivity(homeScreen)
                    finish()  // Optionally finish the current activity to avoid going back
                }
            } else {
                // If no boardName is provided, show an error message
                Toast.makeText(this, "Invalid board name!", Toast.LENGTH_SHORT).show()
                val homeScreen = Intent(this, HomeActivity::class.java)
                startActivity(homeScreen)
                finish()  // Exit the activity
            }
        } else {
            // If in new game mode, generate a new board based on difficulty
            val difficulty = intent.getStringExtra("DIFFICULTY_LEVEL") ?: "easy"  // Default to easy if not provided
            fullBoard = generateFullBoard() // Full solved board
            puzzleBoard = createPuzzle(fullBoard, difficulty) // Puzzle with empty cells

            // Initialize the grid with the new puzzle
            initializeGrid(this, gridSize, puzzleBoard, sudokuGrid)
        }

        checkSolutionButton.setOnClickListener {
            checkSolutions(gridSize, sudokuGrid, fullBoard)
        }

        saveGameButton.setOnClickListener {
            updatePuzzleBoard(sudokuGrid, puzzleBoard)
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
                val success = saveGame(this, boardName, puzzleBoard, fullBoard, sudokuGrid)
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

// Function to update puzzleBoard with user input from EditText grid
private fun updatePuzzleBoard(sudokuGrid: GridLayout, puzzleBoard: Array<IntArray>) {
    for (row in 0 until 9) {
        for (col in 0 until 9) {
            val cell = sudokuGrid.getChildAt(row * 9 + col) as EditText
            val userInput = cell.text.toString()

            // If the cell has user input, update puzzleBoard
            if (userInput.isNotEmpty()) {
                puzzleBoard[row][col] = userInput.toInt()
            } else {
                puzzleBoard[row][col] = 0 // Empty cells are 0
            }
        }
    }
}

// Overload to initialize a new grid without an interactableBoard
private fun initializeGrid(
    context: Context,
    gridSize: Int,
    puzzleBoard: Array<IntArray>,
    sudokuGrid: GridLayout
) {
    // Generate a default interactableBoard based on puzzleBoard
    val interactableBoard = Array(gridSize) { IntArray(gridSize) }
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            interactableBoard[row][col] = if (puzzleBoard[row][col] == 0) 1 else 0
        }
    }

    // Call the main initializeGrid function with the generated interactableBoard
    initializeGrid(context, gridSize, puzzleBoard, interactableBoard, sudokuGrid)
}

// Initialize the Sudoku grid with EditTexts, as you already did in the previous code
private fun initializeGrid(
    context: Context,
    gridSize: Int,
    puzzleBoard: Array<IntArray>,
    interactableBoard: Array<IntArray>,
    sudokuGrid: GridLayout
) {
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
            val number = puzzleBoard[row][col]
            if (number != 0) {
                cell.setText(String.format(Locale.getDefault(), "%d", number))
            } else {
                cell.setText("")
            }

            // Set cell interactivity based on interactableBoard
            if (interactableBoard[row][col] == 1) {
                cell.isFocusableInTouchMode = true
            } else {
                cell.isFocusable = false
                cell.isClickable = false
                cell.setTypeface(null, android.graphics.Typeface.BOLD)
            }

            sudokuGrid.addView(cell)
        }
    }
}

// Set the button click listener here
fun checkSolutions(gridSize: Int, sudokuGrid: GridLayout, fullBoard: Array<IntArray>) {
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
                Handler(Looper.getMainLooper()).postDelayed({
                    cell.setBackgroundResource(R.color.cell_background)
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
fun createPuzzle(board: Array<IntArray>, difficulty: String): Array<IntArray> {
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

fun saveGame(
    context: Context,
    boardName: String,
    puzzleBoard: Array<IntArray>,
    solvedBoard: Array<IntArray>,
    sudokuGrid: GridLayout
): Boolean {
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

    // Create an interactableBoard to save cell interactivity
    val gridSize = puzzleBoard.size
    val interactableBoard = Array(gridSize) { IntArray(gridSize) }

    // Extract user input and interactivity from sudokuGrid
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            val cell = sudokuGrid.getChildAt(row * gridSize + col) as? EditText
            if (cell != null) {
                val userInput = cell.text.toString()
                puzzleBoard[row][col] = if (userInput.isNotEmpty()) userInput.toInt() else 0
                interactableBoard[row][col] = if (cell.isFocusableInTouchMode) 1 else 0
            }
        }
    }

    // Save the boards as JSON strings
    val gson = Gson()
    editor.putString("${boardName}_puzzleBoard", gson.toJson(puzzleBoard))
    editor.putString("${boardName}_solvedBoard", gson.toJson(solvedBoard))
    editor.putString("${boardName}_interactableBoard", gson.toJson(interactableBoard))

    // Commit the changes
    editor.apply()

    return true // Indicate success
}


fun loadGame(context: Context, boardName: String): Triple<Array<IntArray>?, Array<IntArray>?, Array<IntArray>?> {
    val sharedPreferences = context.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
    val gson = Gson()

    // Retrieve boards from JSON strings
    val puzzleBoardJson = sharedPreferences.getString("${boardName}_puzzleBoard", null)
    val solvedBoardJson = sharedPreferences.getString("${boardName}_solvedBoard", null)
    val interactableBoardJson = sharedPreferences.getString("${boardName}_interactableBoard", null)

    val puzzleBoard = if (puzzleBoardJson != null) gson.fromJson(puzzleBoardJson, Array<IntArray>::class.java) else null
    val solvedBoard = if (solvedBoardJson != null) gson.fromJson(solvedBoardJson, Array<IntArray>::class.java) else null
    val interactableBoard = if (interactableBoardJson != null) gson.fromJson(interactableBoardJson, Array<IntArray>::class.java) else null

    return Triple(puzzleBoard, solvedBoard, interactableBoard)
}
