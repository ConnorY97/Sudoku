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

    fun getRow(
        board: Array<IntArray>,
        row: Int
    ): IntArray {
        return IntArray(board.size) {col -> board[row][col]}
    }

    fun getSubgridStart(
        row: Int,
        col: Int,
        subgridSize: Int = 3
    ): Pair<Int, Int> {
        val startRow = (row / subgridSize) * subgridSize
        val startCol = (col / subgridSize) * subgridSize
        return Pair(startRow, startCol)
    }

    fun checkInput(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        num: Int,
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
        board: Array<IntArray>,
        gameLogic: GameLogic
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
            val column = gameLogic.getColumn(board, col)
            val duplicates = findDuplicatePositions(col, column, false)
            duplicates.forEach { problemCell ->
                problematicCells.add(problemCell) // Add problematic cell to the set
            }
        }

        // Check the sub grids
        for (row in 0 until GRID_SIZE step 3) {
            for (col in 0 until GRID_SIZE step 3) {
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
}