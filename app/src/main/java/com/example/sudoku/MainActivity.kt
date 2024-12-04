package com.example.sudoku

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import androidx.activity.ComponentActivity
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    // Define the correct solution (this is just an example; replace with your actual solution)
    private val correctSolution = arrayOf(
        intArrayOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
        intArrayOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
        intArrayOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
        intArrayOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
        intArrayOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
        intArrayOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
        intArrayOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
        intArrayOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
        intArrayOf(3, 4, 5, 2, 8, 6, 1, 7, 9)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sudokuGrid: GridLayout = findViewById(R.id.sudokuGrid)
        val checkSolutionButton = findViewById<Button>(R.id.btnCheckSolution)  // Define the button

        val gridSize = 9
        val chanceToBeEmpty = 0.3 // 30% chance for a cell to be empty (0)

        // Initialize the Sudoku grid with EditTexts, as you already did in the previous code
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val cell = EditText(this)
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
                cell.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                cell.maxLines = 1  // Ensure only 1 line of input
                cell.filters = arrayOf(InputFilter.LengthFilter(1))  // Limit input to one digit
                cell.gravity = Gravity.CENTER // Center text both vertically and horizontally
                cell.setPadding(10, 10, 10, 10) // Add padding around text

                // Decide whether to show a number or leave the cell empty (random chance)
                val number = correctSolution[row][col]
                val randomChance = Random.nextFloat()

                // If the number is not zero and the random chance is below the threshold, pre-fill it
                if (number != 0 && randomChance > chanceToBeEmpty) {
                    cell.setText(number.toString()) // Set predefined number
                    cell.isFocusable = false // Make pre-filled cells non-editable
                    cell.isClickable = false // Prevent interaction with pre-filled cells
                    cell.setBackgroundColor(Color.LTGRAY)
                } else {
                    cell.isFocusableInTouchMode = true // Allow user input on empty cells
                }

                sudokuGrid.addView(cell)
            }
        }

        // Set the button click listener here
        checkSolutionButton.setOnClickListener {
            val gridSize = 9
            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    val cell = sudokuGrid.getChildAt(row * gridSize + col) as EditText
                    val userInput = cell.text.toString()

                    // Only check cells that the user has entered something into
                    if (userInput.isNotEmpty() && cell.isFocusable) {
                        val userAnswer = userInput.toInt()
                        val correctAnswer = correctSolution[row][col]

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
    }
}