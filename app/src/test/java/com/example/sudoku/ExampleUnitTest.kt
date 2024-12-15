package com.example.sudoku

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class SudokuTest {

    @Test
    fun fillBoardTest() {
        assertTrue("Failed to successfully fill a board", fillBoard(Array(gridSize) { IntArray(gridSize) { 0 } }))
    }

    @Test
    fun validateBoardTest() {
        val activity = mock(MainActivity::class.java)
        assertTrue("Failed to mock activity for test", activity != null)
        val board = Array(gridSize) { IntArray(gridSize) { 0 } }
        fillBoard(board)
        assertTrue("Failed to validate the board", activity.validateBoard(board))
    }

    // Backtracking algorithm to fill the board
    private fun fillBoard(board: Array<IntArray>): Boolean {
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
    private fun isValidMove(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
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
}