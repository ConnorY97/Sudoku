package com.example.sudoku

//region imports
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random
//endregion

// Constants
const val gridSize = 9

class MainActivity : ComponentActivity() {
    // Variables
    private val editableCells: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()
    private var sudokuBoard = Array(gridSize) { IntArray(gridSize) { 0 } }
    private var elapsedTime: Long = Long.MAX_VALUE
    private lateinit var timer: Chronometer
    private lateinit var confirmSaveButton: Button
    private lateinit var boardNameInput: EditText
    private lateinit var sudokuGrid: GridLayout

    // Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate", "Entered main activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the game mode from the Intent (null check instead of empty string check)
        val gameMode = intent.getStringExtra("GAME_MODE")

        Log.i("onCreate", "Check Game Mode")
        if (!gameMode.isNullOrEmpty()) {
            Log.i("onCreate", "Attempt to load map")
            // If in load game mode, try to load the board
            val boardName = intent.getStringExtra("BOARD_NAME")

            if (boardName != null) {
                Log.i("onCreate", "Retrieved map name")
                // Try loading the saved game
                Log.i("onCreate", "Attempting to load game")
                val (board, cells, savedTime) = loadGame(this, boardName)

                if (board == null || cells == null) {
                    Log.i("onCreate", "Failed to load game")
                    // If board loading failed, show a toast and navigate to Home
                    Toast.makeText(this, "Failed to load board, invalid name!", Toast.LENGTH_SHORT).show()
                    val homeScreen = Intent(this, HomeActivity::class.java)
                    startActivity(homeScreen)
                    finish()  // Optionally finish the current activity to avoid going back
                } else {
                    Log.i("onCreate", "Successfully loaded board")
                    sudokuBoard = board
                    editableCells.clear()
                    editableCells.putAll(cells)
                    elapsedTime = savedTime
                }
            } else {
                // If no boardName is provided, show an error message
                Log.i("onCreate", "Invalid board name")
                Toast.makeText(this, "Invalid board name!", Toast.LENGTH_SHORT).show()
                val homeScreen = Intent(this, HomeActivity::class.java)
                startActivity(homeScreen)
                finish()  // Exit the activity
            }
        } else {
            Log.i("onCreate", "Initializing Variables")
            sudokuBoard = generatePuzzle(this,"Easy", editableCells)
        }

        Log.i("onCreate", "Initializing UI")
        val (retrievedGrid, retrievedTimer, retrievedConfirmButton, retrievedBoardName) =
            setUpUI(this, sudokuBoard, editableCells, elapsedTime)?.map { it as View } ?: throw IllegalStateException("UI setup failed")
        if (retrievedGrid is GridLayout && retrievedTimer is Chronometer && retrievedConfirmButton is Button && retrievedBoardName is EditText) {
            sudokuGrid = retrievedGrid
            timer = retrievedTimer
            confirmSaveButton = retrievedConfirmButton
            boardNameInput = retrievedBoardName
            Log.i("onCreate", "Successfully initialized UI")

            initializeGrid(this, sudokuGrid, sudokuBoard, editableCells, timer)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        Log.d("MainActivity", "Menu created")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                // Handle Save Game action
                Toast.makeText(this, "Save Game clicked", Toast.LENGTH_SHORT).show()
                // Hide the board while we take input
                sudokuGrid.visibility = View.GONE
                timer.visibility = View.INVISIBLE
                // Show the input field and confirm button
                boardNameInput.visibility = View.VISIBLE
                confirmSaveButton.visibility = View.VISIBLE
                true
            }
            R.id.menu_settings -> {
                // Handle Settings action
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_exit -> {
                // Handle Exit action
                finish() // Close the app
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

fun setUpUI(context: Context,
            sudokuBoard: Array<IntArray>,
            editableCells: MutableMap<Pair<Int, Int>, Boolean>,
            elapsedTime: Long
): List<View>? {
    if (context is Activity)
    {
        // Buttons
        val confirmSaveButton = context.findViewById<Button>(R.id.confirmSaveButton)
        val boardNameInput = context.findViewById<EditText>(R.id.boardNameInput)
        val sudokuGrid = context.findViewById<GridLayout>(R.id.sudokuGrid)
        Log.i("setUpUI", "Buttons created")

        // Timer
        val timer: Chronometer = context.findViewById(R.id.chronometer)
        Log.i("setUpUI", "Timer created")

        if (elapsedTime == Long.MAX_VALUE) {
            // Start the timer
            timer.base = SystemClock.elapsedRealtime()
        }
        else
        {
            timer.base = SystemClock.elapsedRealtime() - elapsedTime
        }
        timer.start()
        Log.i("setUpUI", "Timer Started")


        confirmSaveButton.setOnClickListener {
            // Get the board name from the input field
            val boardName = boardNameInput.text.toString().trim()

            if (boardName.isEmpty()) {
                // Show a toast if the board name is empty
                Toast.makeText(context, "Please enter a valid name.", Toast.LENGTH_SHORT).show()
            } else {
                // Save the game with the entered board name
                val finalTime = SystemClock.elapsedRealtime() - timer.base
                val success =
                    saveGame(context, boardName, sudokuBoard, editableCells, finalTime)
                if (success) {
                    Toast.makeText(context, "Board saved successfully!", Toast.LENGTH_SHORT).show()

                    // Hide the input field and confirm button after saving
                    boardNameInput.visibility = View.GONE
                    confirmSaveButton.visibility = View.GONE

                    // Hide the board while we take input
                    sudokuGrid.visibility = View.VISIBLE

                    // Clear the input field for future use
                    boardNameInput.text.clear()
                } else {
                    Toast.makeText(
                        context, "Name already exists. Choose another name.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return listOf(sudokuGrid, timer, confirmSaveButton, boardNameInput, sudokuGrid)
    }
    return null
}

fun areAllCellsFilled(sudokuGrid: GridLayout
): Boolean {
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
    editableCells: MutableMap<Pair<Int, Int>, Boolean>,
    timer: Chronometer
) {
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
                        try {
                            // Retrieve input and convert to integer
                            val inputText = s?.toString() ?: ""
                            val inputNumber = inputText.toIntOrNull() ?: 0 // Default to 0 for invalid input

                            // Update the board with user input
                            board[row][col] = inputNumber

                            // Check if all cells are filled
                            if (areAllCellsFilled(sudokuGrid)) {
                                // Individually validate the cells
                                val problematicCells = confirmEditableCells(editableCells, board)

                                Toast.makeText(context, "Board Filled", Toast.LENGTH_SHORT).show()

                                // Provide visual feedback for valid and invalid cells
                                val finished = showCorrectCells(sudokuGrid, editableCells, problematicCells)
                                if (finished) {
                                    timer.stop()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("initializeGrid", "Error updating cell ($row, $col): ${e.message}")
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

fun showCorrectCells(
    sudokuGrid: GridLayout,
    editableCells: MutableMap<Pair<Int, Int>, Boolean>,
    problematicCells: Set<Pair<Int, Int>>
): Boolean {
    var finished = true

    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            val cell = sudokuGrid.getChildAt(row * gridSize + col) as EditText
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
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val cell = sudokuGrid.getChildAt(row * gridSize + col) as EditText
                cell.setBackgroundResource(R.color.cell_background) // Reset to default background
            }
        }
    }, 2000) // Adjust the delay as necessary

    return finished
}

// Game Logic
fun generatePuzzle(context: Context,
                   difficulty: String,
                   editableCells: MutableMap<Pair<Int, Int>, Boolean>
): Array<IntArray> {
    Log.i("generatePuzzle","Started generating puzzle")
    // Generate and return a new Sudoku puzzle
    var grid = Array(gridSize) { IntArray(gridSize) {0} }
    if (fillBoard(grid)) {
        Log.i("generatePuzzle","Board filled")
        logBoard(grid)
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

fun logBoard(board: Array<IntArray>
) {
    val boardString = StringBuilder()
    for (row in board) {
        for (col in row) {
            boardString.append("$col ")
        }
        boardString.append("\n") // Newline after each row
    }
    Log.i("logBoard", boardString.toString())
}

fun fillBoard(board: Array<IntArray>
): Boolean {
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
                 editableCells: MutableMap<Pair<Int, Int>, Boolean>
) {
    val chanceToBeEmpty = when (difficulty) {
        "easy" -> 0.1 // 20% cells empty
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
}

fun validateBoard(board: Array<IntArray>
): Boolean {
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
fun isUnique(array: IntArray
): Boolean {
    val seen = mutableSetOf<Int>()
    for (num in array) {
        if (num != 0 && !seen.add(num)) {
            return false
        }
    }
    return true
}

// Helper to get a column as an array
fun getColumn(board: Array<IntArray>, col: Int
): IntArray {
    return IntArray(board.size) { row -> board[row][col] }
}

// Helper to get a sub-grid as an array
fun getSubGrid(board: Array<IntArray>,
               startRow: Int,
               startCol: Int, size: Int
): IntArray {
    val subGrid = mutableListOf<Int>()
    for (row in startRow until startRow + size) {
        for (col in startCol until startCol + size) {
            subGrid.add(board[row][col])
        }
    }
    return subGrid.toIntArray()
}

// Check if placing a number is valid
fun isValidMove(board: Array<IntArray>,
                row: Int,
                col: Int,
                num: Int
): Boolean {
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

fun findDuplicatePositions(
    index: Int,
    array: IntArray,
    isRow: Boolean
): Set<Pair<Int, Int>> {
    val seen = mutableMapOf<Int, MutableList<Pair<Int, Int>>>() // Map to store positions of each number
    val duplicates = mutableSetOf<Pair<Int, Int>>() // Set to store duplicate positions

    array.forEachIndexed { i, num ->
        if (num != 0) { // Ignore zero values (empty cells)
            val position = if (isRow) Pair(index, i) else Pair(i, index) // Row or Column logic
            if (seen.containsKey(num)) {
                seen[num]?.add(position) // Add this index to the list of positions for this number
            } else {
                seen[num] = mutableListOf(position) // Initialize a list with the current index
            }
        }
    }

    // Iterate through the map and add all duplicates (more than one occurrence of a number)
    seen.forEach { (_, positions) ->
        if (positions.size > 1) {
            duplicates.addAll(positions) // Add all duplicates to the set
        }
    }

    return duplicates
}


fun findDuplicatePositionInSubGrid(
    startRow: Int,
    startCol: Int,
    board: Array<IntArray>
): Set<Pair<Int, Int>> {
    val seen = mutableMapOf<Int, MutableList<Pair<Int, Int>>>() // Map to store positions of each number
    val duplicates = mutableSetOf<Pair<Int, Int>>() // Set to store duplicate positions

    // Iterate through the subgrid
    for (row in startRow until startRow + 3) {
        for (col in startCol until startCol + 3) {
            val num = board[row][col]
            if (num != 0) { // Ignore zero values (empty cells)
                if (seen.containsKey(num)) {
                    seen[num]?.add(Pair(row, col)) // Add this position to the list for this number
                } else {
                    seen[num] = mutableListOf(Pair(row, col)) // Initialize the list with the current position
                }
            }
        }
    }

    // Iterate through the map and add all duplicates (more than one occurrence of a number)
    seen.forEach { (_, positions) ->
        if (positions.size > 1) {
            positions.forEach { pos ->
                duplicates.add(pos) // Add to duplicates set
            }
        }
    }

    return duplicates
}


fun confirmEditableCells(
    editableCells: MutableMap<Pair<Int, Int>, Boolean>,
    board: Array<IntArray>
): Set<Pair<Int, Int>> {
    // Create a set to track problematic cells
    val problematicCells = mutableSetOf<Pair<Int, Int>>()

    // Check rows for duplicates
    for (row in board.indices) {
        val duplicates = findDuplicatePositions(row, board[row], true)
        duplicates.forEach { problemCell ->
            problematicCells.add(problemCell) // Add problematic cell to the set
        }
    }

    // Check columns for duplicates
    for (col in board.indices) {
        val column = getColumn(board, col)
        val duplicates = findDuplicatePositions(col, column, false)
        duplicates.forEach { problemCell ->
            problematicCells.add(problemCell) // Add problematic cell to the set
        }
    }

    // Check the sub grids
    for (row in 0 until gridSize step 3) {
        for (col in 0 until gridSize step 3) {
            val duplicates = findDuplicatePositionInSubGrid(row, col, board)
            duplicates.forEach { problemCell ->
                problematicCells.add(problemCell) // Add problematic cell to the set
            }
        }
    }

    // Update the editableCells map with the validation results
    for (row in board.indices) {
        for (col in board.indices) {
            val isEditable = editableCells.containsKey(Pair(row, col))
            val isValid = !problematicCells.contains(Pair(row, col))
            if (isEditable) {
                editableCells[Pair(row, col)] = isValid // Update validity for editable cells
            }
        }
    }

    // Return the set of problematic cells
    return problematicCells
}

// Save game state
fun saveGame(context: Context, boardName: String,
             board: Array<IntArray>,
             editableCells: Map<Pair<Int, Int>, Boolean>,
             elapsedTime: Long
): Boolean {
    val sharedPreferences = context.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    // Retrieve existing saved boards
    val savedBoards = sharedPreferences.getStringSet("SavedBoards", mutableSetOf()) ?: mutableSetOf()

    // Ensure unique board name
    if (savedBoards.contains(boardName)) {
        return false // Indicate failure
    }

    // Add new board name to saved list
    savedBoards.add(boardName)
    editor.putStringSet("SavedBoards", savedBoards)

    // Save board and editable cells
    val gson = Gson()
    editor.putString("${boardName}_board", gson.toJson(board))
    editor.putString("${boardName}_editableCells", gson.toJson(editableCells))

    // Save timer
    editor.putLong("${boardName}_elapsedTime", elapsedTime)

    // Commit changes
    editor.apply()
    return true // Indicate success
}


// Load game state
fun loadGame(context: Context,
             boardName: String
): Triple<Array<IntArray>?, Map<Pair<Int, Int>, Boolean>?, Long> {
    val sharedPreferences = context.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
    val gson = Gson()

    // Load board
    val boardJson = sharedPreferences.getString("${boardName}_board", null)
    val board = if (boardJson != null) gson.fromJson(boardJson, Array<IntArray>::class.java) else null

    // Load editable cells
    val editableCellsJson = sharedPreferences.getString("${boardName}_editableCells", null)
    val editableCells = if (editableCellsJson != null) {
        val tempMap = gson.fromJson(editableCellsJson, Map::class.java) as Map<String, Boolean>

        tempMap.mapNotNull { (key, value) ->
            // Parse the string key "(x, y)" into a Pair<Int, Int>
            val match = "\\((\\d+),\\s*(\\d+)\\)".toRegex().matchEntire(key)
            val (first, second) = match?.destructured ?: return@mapNotNull null
            Pair(first.toInt(), second.toInt()) to value
        }.toMap()
    } else {
        null
    }

    // Load elapsed time
    val elapsedTime = sharedPreferences.getLong("${boardName}_elapsedTime", 0)

    return Triple(board, editableCells, elapsedTime)
}
