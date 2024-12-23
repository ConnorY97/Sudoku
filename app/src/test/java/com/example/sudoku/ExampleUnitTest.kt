package com.example.sudoku

import android.app.Activity
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class SudokuTest {

//    @Test
//    fun sanityTest() {
//        assertTrue("I should be able to see this message", false)
//    }

    @Test
    fun fillBoardTest() {
        assertTrue("Failed to successfully fill a board", fillBoard(Array(gridSize) { IntArray(gridSize) { 0 } }))
    }

    @Test
    fun validateBoardTest() {
        val board = Array(gridSize) { IntArray(gridSize) { 0 } }
        fillBoard(board)
        assertTrue("Failed to validate the board", validateBoard(board))
    }

    @Test
    fun generatePuzzleTest() {
        val board = Array(gridSize) { IntArray(gridSize) { 0 } }
        fillBoard(board)
        assertTrue("Failed to validate the board", validateBoard(board))
        val editableCells: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()
        createPuzzle(board, "Easy", editableCells)
        assertTrue("Puzzle successfully created", validateBoard(board))
    }

    @Test
    fun findDuplicatesInRowAndColTest() {
        val row = IntArray(gridSize) { it }
        assertTrue(
            "Failed to parse valid row",
            findDuplicatePositions(0, row, true).isEmpty()
        )

        assertTrue(
            "Failed to parse valid column",
            findDuplicatePositions(0, row, false).isEmpty()
        )

        // Insert duplication
        row[0] = 8

        assertTrue(
            "Failed to find duplication in row",
            findDuplicatePositions(0, row, true).size == 2
        )

        assertTrue(
            "Failed to find duplication in column",
            findDuplicatePositions(0, row, false).size == 2
        )
    }

    @Test
    fun findDuplicatesInSubGridTest() {
        val board = Array(gridSize) { IntArray(gridSize) { 0 } }

        fillBoard(board)

        assertTrue("Failed to parse sub grid", findDuplicatePositionInSubGrid(0, 0, board).isEmpty())

        board[0][0] = 8

        assertTrue("Failed to find duplicate in sub grid", findDuplicatePositionInSubGrid(0,0, board).size == 2)
    }
}