package my.sudoku.game

import junit.framework.TestCase.assertTrue
import my.sudoku.game.game.GameLogic
import org.junit.Test

class SudokuTest {

//    @Test
//    fun sanityTest() {
//        assertTrue("I should be able to see this message", false)
//    }

    @Test
    fun fillBoardTest() {
        val gameLogic = GameLogic()
        assertTrue("Failed to successfully fill a board", gameLogic.fillBoard(Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } }))
    }

    @Test
    fun validateBoardTest() {
        val gameLogic = GameLogic()
        val board = Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } }
        gameLogic.fillBoard(board)
        assertTrue("Failed to validate the board", gameLogic.validateBoard(board))
    }

    @Test
    fun generatePuzzleTest() {
        val gameLogic = GameLogic()
        val board = Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } }
        gameLogic.fillBoard(board)
        assertTrue("Failed to validate the board", gameLogic.validateBoard(board))
        val editableCells: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()
        gameLogic.createPuzzle(board, "Easy", editableCells)
        assertTrue("Puzzle successfully created", gameLogic.validateBoard(board))
    }

    @Test
    fun findDuplicatesInRowAndColTest() {
        val gameLogic = GameLogic()
        val row = IntArray(GRID_SIZE) { it }
        assertTrue(
            "Failed to parse valid row",
            gameLogic.findDuplicatePositions(0, row, true).isEmpty()
        )

        assertTrue(
            "Failed to parse valid column",
            gameLogic.findDuplicatePositions(0, row, false).isEmpty()
        )

        // Insert duplication
        row[0] = 8

        assertTrue(
            "Failed to find duplication in row",
            gameLogic.findDuplicatePositions(0, row, true).size == 2
        )

        assertTrue(
            "Failed to find duplication in column",
            gameLogic.findDuplicatePositions(0, row, false).size == 2
        )
    }

    @Test
    fun findDuplicatesInSubGridTest() {
        val gameLogic = GameLogic()
        val board = Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } }

        gameLogic.fillBoard(board)

        assertTrue("Failed to parse sub grid", gameLogic.findDuplicatePositionInSubGrid(0, 0, board).isEmpty())

        // It was failing because just adding an 8 into the first position could not guarantee that there would be an issue
        board[0][0] = 8
        board[0][1] = 8

        assertTrue("Failed to find duplicate in sub grid", gameLogic.findDuplicatePositionInSubGrid(0, 0, board).isNotEmpty()
        )
    }
}