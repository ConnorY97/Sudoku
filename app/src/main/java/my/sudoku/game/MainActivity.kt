package my.sudoku.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
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
import my.sudoku.game.game.GameLogic
import my.sudoku.game.management.GameManager
import my.sudoku.game.ui.NumberGridManager
import my.sudoku.game.ui.SudokuGridManager
import my.sudoku.game.viewmodel.GameViewModel

// Constants
const val GRID_SIZE = 9
const val PREFS_NAME = "AppPreferences"
const val KEY_ERROR_CHECKING = "IS_ERROR_CHECKING_ENABLED"
const val NUMBER_MARGIN_BUFFER = 180
const val DEFAULT_BOARD_NAME = "Default101"

class MainActivity : ComponentActivity() {
    // UI
    private val confirmSaveButton: Button by lazy { findViewById(R.id.confirmSaveButton) }
    private val boardNameInput: EditText by lazy { findViewById(R.id.boardNameInput) }
    private val timer: Chronometer by lazy { findViewById(R.id.chronometer) }

    // Variables to be initialized in onCreate
    private lateinit var viewModel: GameViewModel
    private lateinit var gameLogic: GameLogic
    private lateinit var gameManager: GameManager
    private lateinit var sudokuGridManager: SudokuGridManager
    private lateinit var numberGridManager: NumberGridManager
    private lateinit var boardName: String

    // Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate", "Entered main activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("onCreate", "Initializing Managers")
        // Initialize variables
        viewModel = viewModels<GameViewModel>().value
        gameLogic = GameLogic()
        gameManager = GameManager(this)
        sudokuGridManager = SudokuGridManager(this)
        numberGridManager = NumberGridManager(this)
        Log.i("onCreate", "Initialized Managers")

        // Get the game mode from the Intent (null check instead of empty string check)
        val gameMode = intent.getStringExtra("GAME_MODE")

        // Assigning a default name to the board that will be overridden if the board is loaded
        Log.i("onCreate", "Assigning default boardName")
        boardName = DEFAULT_BOARD_NAME

        Log.i("onCreate", "Check Game Mode")
        if (!gameMode.isNullOrEmpty()) {
            Log.i("onCreate", "Attempt to load map")
            // If in load game mode, try to load the board
                Log.i("onCreate", "Name is null")
            boardName = intent.getStringExtra("BOARD_NAME").toString()
            Log.i("onCreate", "Retrieved map name")
            // Try loading the saved game
            Log.i("onCreate", "Attempting to load game")
            viewModel.getGameState().value = gameManager.loadGame(boardName)
            Log.i("onCreate", "Successfully loaded board")
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
        sudokuGridManager.initializeSudokuGrid(viewModel)

        // Once everything has been initialized set up the number grid
        numberGridManager.initializeNumberButtons(gameLogic, viewModel)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        Log.d("MainActivity", "Menu created")

        // Find the menu item by its ID
        val toggleFeatureMenuItem = menu?.findItem(R.id.action_toggle_feature)
        if (toggleFeatureMenuItem != null) {
            toggleFeatureMenuItem.isChecked = numberGridManager.isErrorCheckingEnabled()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                // Handle Save Game action
                Log.i("menu_save", "Saving board")
                if (boardName != DEFAULT_BOARD_NAME)
                {
                    Log.i("menu_save", "Board name present, overriding existing save")
                    val finalTime = SystemClock.elapsedRealtime() - timer.base
                    val success = gameManager.saveGame(boardName, viewModel.getGameState().value!!, finalTime, viewModel, true)
                    if (success) {
                        Toast.makeText(this, "Board saved successfully!", Toast.LENGTH_SHORT).show()
                        true
                    } else {
                        Toast.makeText(this, "Failed to save the board!", Toast.LENGTH_SHORT).show()
                        false
                    }
                } else {
                    Log.i("menu_save", "Board name not present, creating new save")
                    findViewById<GridLayout>(R.id.sudokuGrid).visibility = View.GONE
                    findViewById<GridLayout>(R.id.numberGrid).visibility = View.GONE
                    timer.visibility = View.INVISIBLE
                    findViewById<Button>(R.id.confirmSaveButton).visibility = View.VISIBLE
                    findViewById<EditText>(R.id.boardNameInput).visibility = View.VISIBLE
                    true
                }
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

    private fun setUpUI() {
        Log.i("setUpUI", "Buttons created")

        // Timer
        val timer: Chronometer = findViewById(R.id.chronometer)
        Log.i("setUpUI", "Timer created")

        if (viewModel.getGameState().value?.elapsedTime == Long.MAX_VALUE) {
            // Start the timer
            timer.base = SystemClock.elapsedRealtime()
        }
        else
        {
            timer.base = SystemClock.elapsedRealtime() - viewModel.getGameState().value?.elapsedTime!!
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
                    gameManager.saveGame(boardName, viewModel.getGameState().value!!, finalTime, viewModel)
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
                        sudokuGridManager.getSudokuGrid()!!.visibility = View.VISIBLE
                        numberGridManager.getNumberGrid()!!.visibility = View.VISIBLE

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

    private fun toggleFeature(
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

    private fun hideKeyboard(
        context: Context
    ) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // Get the root view of the activity
        val view = (context as Activity).findViewById<View>(android.R.id.content)
        // Hide the keyboard from the current window
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

