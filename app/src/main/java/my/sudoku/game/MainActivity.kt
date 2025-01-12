package my.sudoku.game

import GameState
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import my.sudoku.game.game.GameLogic
import my.sudoku.game.management.GameManager
import my.sudoku.game.ui.UIManager
import my.sudoku.game.viewmodel.GameViewModel
import java.util.Locale

// Constants
const val GRID_SIZE = 9
const val PREFS_NAME = "AppPreferences"
const val KEY_ERROR_CHECKING = "IS_ERROR_CHECKING_ENABLED"
const val NUMBER_MARGIN_BUFFER = 180

class MainActivity : ComponentActivity() {
    // UI
    private val confirmSaveButton: Button by lazy { findViewById(R.id.confirmSaveButton) }
    private val boardNameInput: EditText by lazy { findViewById(R.id.boardNameInput) }
    //private val sudokuGrid: GridLayout by lazy { findViewById(R.id.sudokuGrid) }
    private val numberGrid: GridLayout by lazy { findViewById(R.id.numberGrid) }
    private val timer: Chronometer by lazy { findViewById(R.id.chronometer) }

    // Initialize GameState with its default values
    private val viewModel: GameViewModel by viewModels()

    // Initialize Game logic object
    private val gameLogic = GameLogic()

    // Initialize the Manager
    private val gameManager = GameManager(this)

    // Initialize the UI Manager
    private val uiManager = UIManager(this)

    // Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate", "Entered main activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                viewModel.getGameState().value = gameManager.loadGame(boardName)
                Log.i("onCreate", "Successfully loaded board")
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
            val difficulty = intent.getStringExtra("DIFFICULTY_LEVEL")
            if (difficulty != null) {
                Log.i("onCreate", "Successfully loaded difficulty")
                viewModel.getGameState().value?.board = viewModel.getGameState().value?.editableCells?.let { gameLogic.generatePuzzle(this, difficulty, it) }!!
            } else {
                Log.e("onCreate", "Failed to load difficulty")
                val homeScreen = Intent(this, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(homeScreen)
                finish()  // Optional if you want to finish this activity explicitly
            }

        }

        Log.i("onCreate", "Initializing UI")
        setUpUI()
        uiManager.initializeSudokuGrid(viewModel)

        // Once everything has been initialized set up the number grid
        initializeNumberButtons(gameLogic)
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
                findViewById<GridLayout>(R.id.sudokuGrid).visibility = View.GONE
                findViewById<GridLayout>(R.id.numberGrid).visibility = View.GONE
                timer.visibility = View.INVISIBLE
                findViewById<Button>(R.id.confirmSaveButton).visibility = View.VISIBLE
                findViewById<EditText>(R.id.boardNameInput).visibility = View.VISIBLE
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

//    private fun initializeSudokuGrid() {
//        val gameState = viewModel.getGameState()
//        for (row in 0 until 9) {
//            for (col in 0 until 9) {
//                val isEditable = Pair(row, col) in (gameState.value?.editableCells ?: return)
//                val cell = EditText(this).apply {
//                    layoutParams = GridLayout.LayoutParams().apply {
//                        width = 100
//                        height = 100
//                        columnSpec = GridLayout.spec(col)
//                        rowSpec = GridLayout.spec(row)
//                        setMargins(
//                            if (col % 3 == 0 && col != 0) 4 else 1,
//                            if (row % 3 == 0 && row != 0) 4 else 1,
//                            1,
//                            1
//                        )
//                    }
//                    gravity = Gravity.CENTER
//                    textAlignment = EditText.TEXT_ALIGNMENT_CENTER
//                    setBackgroundColor(ContextCompat.getColor(context, R.color.cell_background))
//                    setTextColor(Color.BLACK)
//                    setPadding(10, 10, 10, 10)
//
//                    if (isEditable) {
//                        setOnClickListener {
//                            selectCell(row, col, sudokuGrid, context, viewModel)
//                        }
//                    }
//
//                    // Make sure the cells cannot be interacted with a keyboard
//                    isFocusableInTouchMode = false
//                    isFocusable = false
//                    inputType = InputType.TYPE_NULL
//                }
//                val number = gameState.value?.board?.get(row)?.get(col)
//                if (number != 0) {
//                    cell.setText(String.format(Locale.getDefault(), "%d", number))
//                    if (!isEditable) {
//                        cell.setTypeface(null, Typeface.BOLD)
//                    }
//                } else {
//                    cell.setText("")
//                }
//
//                sudokuGrid.addView(cell)
//            }
//        }
//    }

    private fun setUpUI() {
        val gameState = viewModel.getGameState()
        Log.i("setUpUI", "Buttons created")

        // Timer
        val timer: Chronometer = findViewById(R.id.chronometer)
        Log.i("setUpUI", "Timer created")

        if (gameState.value?.elapsedTime == Long.MAX_VALUE) {
            // Start the timer
            timer.base = SystemClock.elapsedRealtime()
        }
        else
        {
            timer.base = SystemClock.elapsedRealtime() - gameState.value?.elapsedTime!!
        }
        timer.start()
        Log.i("setUpUI", "Timer Started")


        confirmSaveButton.setOnClickListener {
            // Get the board name from the input field
            val boardName = boardNameInput.text.toString().trim()

            if (boardName.isEmpty()) {
                // Show a toast if the board name is empty
                Toast.makeText(this, "Please enter a valid name.", Toast.LENGTH_SHORT).show()
            } else {
                // Save the game with the entered board name
                val finalTime = SystemClock.elapsedRealtime() - timer.base
                val success =
                    gameManager.saveGame(boardName, gameState.value!!, finalTime, viewModel)
                if (success) {
                    if (viewModel.getFinished()) {
                        //If the board is finished the we should return to the home screen
                        val homeScreen = Intent(this, HomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(homeScreen)
                        finish()  // Optional if you want to finish this activity explicitly
                    } else { // The board is not finished so the user can continue playing
                        Toast.makeText(this, "Board saved successfully!", Toast.LENGTH_SHORT)
                            .show()

                        // Hide the input field and confirm button after saving
                        boardNameInput.visibility = View.GONE
                        confirmSaveButton.visibility = View.GONE

                        // Show the board and buttons again
                        uiManager.getSudokuGrid()!!.visibility = View.VISIBLE
                        numberGrid.visibility = View.VISIBLE

                        hideKeyboard(this)
                        // Clear the input field for future use
                        boardNameInput.text.clear()
                    }
                } else {
                    Toast.makeText(this, "Name already exists. Choose another name.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeNumberButtons(
        gameLogic: GameLogic
    ) {
        val gameState = viewModel.getGameState()
        for (number in 1..9) {
            val button = Button(this).apply {
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
                    onNumberClicked(context, number, uiManager.getSudokuGrid()!!, gameState.value!!, gameLogic, viewModel)

                    if (isErrorCheckingEnabled(context)) {
                        viewModel.getSelectedCell()?.let { (row, col) ->
                            val cellIndex = row * 9 + col
                            if (gameLogic.checkInput(gameState.value?.board!!, row, col, number)) {
                                (uiManager.getSudokuGrid()!!.getChildAt(cellIndex) as? EditText)?.setBackgroundColor(
                                    Color.GREEN
                                )
                            } else {
                                (uiManager.getSudokuGrid()!!.getChildAt(cellIndex) as? EditText)?.setBackgroundColor(
                                    Color.RED
                                )
                            }

                            Handler(Looper.getMainLooper()).postDelayed({
                                (uiManager.getSudokuGrid()!!.getChildAt(cellIndex) as? EditText)?.setBackgroundColor(ContextCompat.getColor(context, R.color.cell_background))
                                // Reset to default background
                            }, 250) // Adjust the delay as necessary
                        }
                    }
                }
            }
            numberGrid.addView(button)
        }
    }
}

fun toggleFeature(
    context: Context,
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

fun isErrorCheckingEnabled(
    context: Context
): Boolean {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(KEY_ERROR_CHECKING, false)
}

fun hideKeyboard(
    context: Context
) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    // Get the root view of the activity
    val view = (context as Activity).findViewById<View>(android.R.id.content)
    // Hide the keyboard from the current window
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showSaveScreen(
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

fun areAllCellsFilled(
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

fun onNumberClicked(
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

fun showCorrectCells(
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
