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
}