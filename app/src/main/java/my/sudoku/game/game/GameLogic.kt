package my.sudoku.game.game

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import my.sudoku.game.GRID_SIZE
import kotlin.math.sqrt
import kotlin.random.Random

open class GameLogic {
    // Game Logic
    fun generatePuzzle(
        context: Context,
        difficulty: String,
        editableCells: MutableMap<Pair<Int, Int>, Boolean>
    ): Array<IntArray> {
        Log.i("generatePuzzle","Started generating puzzle")
        // Generate and return a new Sudoku puzzle
        var grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) {0} }
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
            grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) {0} }
        }
        return grid
    }

    fun createPuzzle(
        board: Array<IntArray>,
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

    fun fillBoard(
        board: Array<IntArray>
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

    // Check if placing a number is valid
    private fun isValidMove(
        board: Array<IntArray>,
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

    private fun logBoard(
        board: Array<IntArray>
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

    fun validateBoard(
        board: Array<IntArray>
    ): Boolean {
        val subGridSize = sqrt(GRID_SIZE.toDouble()).toInt() // 3 for a 9x9 board

        // Validate rows and columns
        for (i in 0 until GRID_SIZE) {
            if (!isUnique(board[i]) || !isUnique(getColumn(board, i))) {
                return false
            }
        }

        // Validate sub-grids
        for (row in 0 until GRID_SIZE step subGridSize) {
            for (col in 0 until GRID_SIZE step subGridSize) {
                if (!isUnique(getSubGrid(board, row, col, subGridSize))) {
                    return false
                }
            }
        }
        return true
    }

    // Helper to check if all numbers in an array are unique (ignores zeros)
    private fun isUnique(
        array: IntArray
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
    fun getColumn(
        board: Array<IntArray>,
        col: Int
    ): IntArray {
        return IntArray(board.size) { row -> board[row][col] }
    }

    // Helper to get a sub-grid as an array
    fun getSubGrid(
        board: Array<IntArray>,
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
}