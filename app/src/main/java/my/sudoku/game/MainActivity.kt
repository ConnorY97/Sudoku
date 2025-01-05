package my.sudoku.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

// Constants
const val GRIDSIZE = 9
var FINISHED = false
const val PREFS_NAME = "AppPreferences"
const val KEY_ERROR_CHECKING = "IS_ERROR_CHECKING_ENABLED"
var SELECTED_CELL: Pair<Int, Int>? = null
const val NUMBER_MARGIN_BUFFER = 180

class MainActivity : ComponentActivity() {
    // Variables
    private val editableCells: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()
    private var sudokuBoard = Array(GRIDSIZE) { IntArray(GRIDSIZE) { 0 } }
    private var elapsedTime: Long = Long.MAX_VALUE
    private lateinit var timer: Chronometer
    private lateinit var confirmSaveButton: Button
    private lateinit var boardNameInput: EditText
    private lateinit var sudokuGrid: GridLayout
    private lateinit var numberGrid: GridLayout

    // Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate", "Entered main activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the number Grid
        numberGrid = findViewById(R.id.numberGrid)

        Log.i("onCreate", "Error checking is ${if (isErrorCheckingEnabled(this)) "Error checking Enabled" else "Error checking is disabled"}")
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
                val game = loadGame(this, boardName)
                if (game.board == null || game.editableCells == null) {
                    Log.i("onCreate", "Failed to load game")
                    // If board loading failed, show a toast and navigate to Home
                    Toast.makeText(this, "Failed to load board, invalid name!", Toast.LENGTH_SHORT).show()
                    val homeScreen = Intent(this, HomeActivity::class.java)
                    startActivity(homeScreen)
                    finish()  // Optionally finish the current activity to avoid going back
                } else {
                    Log.i("onCreate", "Successfully loaded board")
                    sudokuBoard = game.board
                    editableCells.clear()
                    editableCells.putAll(game.editableCells)
                    elapsedTime = game.elapsedTime
                }
            } else {
                // If no boardName is provided, show an error message
                Log.i("onCreate", "Invalid board name, returning home")
                Toast.makeText(this, "Invalid board name!", Toast.LENGTH_SHORT).show()

                val homeScreen = Intent(this, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(homeScreen)
                finish()  // Optional if you want to finish this activity explicitly
            }
        } else {
            Log.i("onCreate", "Initializing Variables")
            sudokuBoard = generatePuzzle(this,"Easy", editableCells)
        }

        Log.i("onCreate", "Initializing UI")
        val (retrievedGrid, retrievedTimer, retrievedConfirmButton, retrievedBoardName) =
            setUpUI(this, sudokuBoard, editableCells, elapsedTime)?.map { it } ?: throw IllegalStateException("UI setup failed")
        if (retrievedGrid is GridLayout && retrievedTimer is Chronometer && retrievedConfirmButton is Button && retrievedBoardName is EditText) {
            sudokuGrid = retrievedGrid
            timer = retrievedTimer
            confirmSaveButton = retrievedConfirmButton
            boardNameInput = retrievedBoardName
            Log.i("onCreate", "Successfully initialized UI")

            //initializeGrid(this, sudokuGrid, sudokuBoard, editableCells, timer)
            initializeSudokuGrid(this, sudokuGrid, sudokuBoard, editableCells)
        }

        // Once everything has been initialized set up the number grid
        initializeNumberButtons(this, numberGrid, sudokuGrid, sudokuBoard, editableCells)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        Log.d("MainActivity", "Menu created")

        // Find the menu item by its ID
        val toggleFeatureMenuItem = menu?.findItem(R.id.action_toggle_feature)
        if (toggleFeatureMenuItem != null) {
            toggleFeatureMenuItem.isChecked = isErrorCheckingEnabled(this)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                // Handle Save Game action
                Toast.makeText(this, "Save Game clicked", Toast.LENGTH_SHORT).show()
                sudokuGrid.visibility = View.GONE
                numberGrid.visibility = View.GONE
                timer.visibility = View.INVISIBLE
                boardNameInput.visibility = View.VISIBLE
                confirmSaveButton.visibility = View.VISIBLE
                true
            }
            R.id.action_settings -> {
                // No specific action here since submenu items will be handled below
                true
            }
            R.id.action_toggle_feature -> {
                toggleFeature(this, item)
                true
            }
            R.id.menu_main -> {
                val homeScreen = Intent(this, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(homeScreen)
                finish() // Optional if you want to finish this activity explicitly
                true
            }
            R.id.menu_exit -> {
                // Handle Exit action
                finishAffinity() // Close the app
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

fun toggleFeature(context: Context,
                  item: MenuItem,
) {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Toggle the CheckBox state
    item.isChecked = !item.isChecked

    // Save the new option
    sharedPreferences.edit().putBoolean(KEY_ERROR_CHECKING, item.isChecked).apply()

    // Example of user feedback
    Toast.makeText(context, if (item.isChecked) "Error Checking Enabled!" else "Error Checking Disabled!", Toast.LENGTH_SHORT).show()
}

fun isErrorCheckingEnabled(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(KEY_ERROR_CHECKING, false)
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
        val numberGrid = context.findViewById<GridLayout>(R.id.numberGrid)
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
                    if (FINISHED) {
                        //If the board is finished the we should return to the home screen
                        val homeScreen = Intent(context, HomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        context.startActivity(homeScreen)
                        context.finish()  // Optional if you want to finish this activity explicitly
                    } else { // The board is not finished so the user can continue playing
                        Toast.makeText(context, "Board saved successfully!", Toast.LENGTH_SHORT)
                            .show()

                        // Hide the input field and confirm button after saving
                        boardNameInput.visibility = View.GONE
                        confirmSaveButton.visibility = View.GONE

                        // Show the board and buttons again
                        sudokuGrid.visibility = View.VISIBLE
                        numberGrid.visibility = View.VISIBLE

                        hideKeyboard(context)
                        // Clear the input field for future use
                        boardNameInput.text.clear()
                    }
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

private fun hideKeyboard(context: Context) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    // Get the root view of the activity
    val view = (context as Activity).findViewById<View>(android.R.id.content)
    // Hide the keyboard from the current window
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showSaveScreen(context: Context) {
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

fun areAllCellsFilled(sudokuGrid: GridLayout
): Boolean {
    for (row in 0 until GRIDSIZE) {
        for (col in 0 until GRIDSIZE) {
            val cell = sudokuGrid.getChildAt(row * GRIDSIZE + col) as EditText
            if (cell.text.toString().isEmpty()) {
                return false // Found an empty cell
            }
        }
    }
    return true // All cells are filled
}

fun initializeSudokuGrid(context: Context,
                         sudokuGrid: GridLayout,
                         board: Array<IntArray>,
                         editableCells: MutableMap<Pair<Int, Int>, Boolean>
) {
    for (row in 0 until 9) {
        for (col in 0 until 9) {
            val isEditable = Pair(row,col) in editableCells
            val cell = EditText(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
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
                gravity = Gravity.CENTER
                textAlignment = EditText.TEXT_ALIGNMENT_CENTER
                setBackgroundColor(ContextCompat.getColor(context, R.color.cell_background))
                setTextColor(Color.BLACK)
                setPadding(10, 10, 10, 10)

                if (isEditable) {
                    setOnClickListener {
                        selectCell(row, col, sudokuGrid)
                    }
                }

                // Make sure the cells cannot be interacted with a keyboard
                isFocusableInTouchMode = false
                isFocusable = false
                inputType = InputType.TYPE_NULL
            }
            val number = board[row][col]
            if (number != 0) {
                cell.setText(String.format(Locale.getDefault(), "%d", number))
                if (!isEditable) {
                    cell.setTypeface(null, Typeface.BOLD)
                }
            } else {
                cell.setText("")
            }

            sudokuGrid.addView(cell)
        }
    }
}

private fun initializeNumberButtons(context: Context,
                                    numberGrid: GridLayout,
                                    sudokuGrid: GridLayout,
                                    sudokuBoard: Array<IntArray>,
                                    editableCells: MutableMap<Pair<Int, Int>, Boolean>
) {
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
                onNumberClicked(context, number, sudokuGrid, sudokuBoard, editableCells)

                if (isErrorCheckingEnabled(context)) {
                    SELECTED_CELL?.let { (row, col) ->
                        val cellIndex = row * 9 + col
                        if (checkInput(sudokuBoard, row, col, number)) {
                            (sudokuGrid.getChildAt(cellIndex) as? EditText)?.setBackgroundColor(
                                Color.GREEN
                            )
                        } else {
                            (sudokuGrid.getChildAt(cellIndex) as? EditText)?.setBackgroundColor(
                                Color.RED
                            )
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            (sudokuGrid.getChildAt(cellIndex) as? EditText)?.setBackgroundColor(ContextCompat.getColor(context, R.color.cell_background))
                            // Reset to default background
                        }, 250) // Adjust the delay as necessary
                    }
                }
            }
        }

        numberGrid.addView(button)
    }
}

private fun onNumberClicked(context: Context,
                            number: Int,
                            sudokuGrid: GridLayout,
                            sudokuBoard: Array<IntArray>,
                            editableCells: MutableMap<Pair<Int, Int>, Boolean>,
) {
    SELECTED_CELL?.let { (row, col) ->
        val cellIndex = row * 9 + col
        val selectedCell = sudokuGrid.getChildAt(cellIndex) as? EditText

        // Simulate inputting the number into the Sudoku cell
        selectedCell?.setBackgroundColor(Color.LTGRAY)

        // Update the cell text
        selectedCell?.setText(if (number != 0) number.toString() else "")
        sudokuBoard[row][col] = number
    } ?: Toast.makeText(context, "Select a cell first!", Toast.LENGTH_SHORT).show()

    if (areAllCellsFilled(sudokuGrid)) {
        val problematicCells = confirmEditableCells(editableCells, sudokuBoard)

        if (showCorrectCells(sudokuGrid, editableCells, problematicCells)) {
            FINISHED = true
            showSaveScreen(context)
        }
    }
}

private fun selectCell(row: Int,
                       col: Int,
                       sudokuGrid: GridLayout,
) {
    clearCellHighlights(sudokuGrid)
    val cellIndex = row * 9 + col
    val selectedView = sudokuGrid.getChildAt(cellIndex)
    selectedView.setBackgroundColor(Color.YELLOW)
    SELECTED_CELL = Pair(row, col)
}

private fun clearCellHighlights(sudokuGrid: GridLayout
) {
    for (i in 0 until sudokuGrid.childCount) {
        sudokuGrid.getChildAt(i).setBackgroundColor(Color.LTGRAY)
    }
}

fun showCorrectCells(
    sudokuGrid: GridLayout,
    editableCells: MutableMap<Pair<Int, Int>, Boolean>,
    problematicCells: Set<Pair<Int, Int>>
): Boolean {
    var finished = true

    for (row in 0 until GRIDSIZE) {
        for (col in 0 until GRIDSIZE) {
            val cell = sudokuGrid.getChildAt(row * GRIDSIZE + col) as EditText
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
        for (row in 0 until GRIDSIZE) {
            for (col in 0 until GRIDSIZE) {
                val cell = sudokuGrid.getChildAt(row * GRIDSIZE + col) as EditText
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
    var grid = Array(GRIDSIZE) { IntArray(GRIDSIZE) {0} }
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
        grid = Array(GRIDSIZE) { IntArray(GRIDSIZE) {0} }
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
    val subGridSize = sqrt(GRIDSIZE.toDouble()).toInt() // 3 for a 9x9 board

    // Validate rows and columns
    for (i in 0 until GRIDSIZE) {
        if (!isUnique(board[i]) || !isUnique(getColumn(board, i))) {
            return false
        }
    }

    // Validate sub-grids
    for (row in 0 until GRIDSIZE step subGridSize) {
        for (col in 0 until GRIDSIZE step subGridSize) {
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

fun getRow(board: Array<IntArray>,
           row: Int
): IntArray {
    return IntArray(board.size) {col -> board[row][col]}
}

// Helper to get a sub-grid as an array
fun getSubGrid(board: Array<IntArray>,
               startRow: Int,
               startCol: Int,
               size: Int
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

fun getSubgridStart(row: Int, col: Int, subgridSize: Int = 3): Pair<Int, Int> {
    val startRow = (row / subgridSize) * subgridSize
    val startCol = (col / subgridSize) * subgridSize
    return Pair(startRow, startCol)
}

fun checkInput(
    board: Array<IntArray>,
    row: Int,
    col: Int,
    num: Int
): Boolean {
    val (startRow, startCol) = getSubgridStart(row, col)

    // Filter out the cell at (row, col) in the current row, column, and subgrid
    val currentRow = getRow(board, row).filterIndexed { index, _ -> index != col }
    val currentColumn = getColumn(board, col).filterIndexed { index, _ -> index != row }
    val currentSubgrid = getSubGrid(board, startRow, startCol, 3).filterIndexed { index, _ ->
        val subgridRow = startRow + index / 3
        val subgridCol = startCol + index % 3
        !(subgridRow == row && subgridCol == col)
    }

    return when (num) {
        in currentRow -> false
        in currentColumn -> false
        in currentSubgrid -> false
        else -> true
    }
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
    for (row in 0 until GRIDSIZE step 3) {
        for (col in 0 until GRIDSIZE step 3) {
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
fun saveGame(
    context: Context, boardName: String,
    board: Array<IntArray>,
    editableCells: Map<Pair<Int, Int>, Boolean>,
    elapsedTime: Long,
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
    savedBoards += boardName
    editor.putStringSet("SavedBoards", savedBoards)

    // Save board and editable cells
    val gson = Gson()
    editor.putString("${boardName}_board", gson.toJson(board))
    editor.putString("${boardName}_editableCells", gson.toJson(editableCells))

    // Save timer
    editor.putLong("${boardName}_elapsedTime", elapsedTime)

    // Save whether the game is finished
    editor.putBoolean("${boardName}_isFinished", FINISHED)

    // Commit changes
    editor.apply()
    return true // Indicate success
}

// Data class to encapsulate game state
data class GameState(
    val board: Array<IntArray>?,
    val editableCells: Map<Pair<Int, Int>, Boolean>?,
    val elapsedTime: Long,
    val finished: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (board != null) {
            if (other.board == null) return false
            if (!board.contentDeepEquals(other.board)) return false
        } else if (other.board != null) return false
        if (editableCells != other.editableCells) return false
        if (elapsedTime != other.elapsedTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board?.contentDeepHashCode() ?: 0
        result = 31 * result + (editableCells?.hashCode() ?: 0)
        result = 31 * result + elapsedTime.hashCode()
        return result
    }
}


// Load game state
fun loadGame(context: Context,
             boardName: String
): GameState {
    val sharedPreferences = context.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
    val gson = Gson()

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

    val game = GameState(board, editableCells, elapsedTime, finished)

    return game
}
