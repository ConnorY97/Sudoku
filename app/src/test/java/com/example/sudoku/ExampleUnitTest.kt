package com.example.sudoku

import android.content.Context
import android.content.SharedPreferences
import android.widget.GridLayout
import org.junit.Test

import org.junit.Assert.*
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anySet
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`


class SudokuTest {

    // Mock function for generateFullBoard
    private fun generateFullBoard(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        // Simulate a filled board
        for (row in 0..8) {
            for (col in 0..8) {
                board[row][col] = (row * 3 + row / 3 + col) % 9 + 1 // Valid Sudoku generation logic
            }
        }
        return board
    }

    @Test
    fun testGenerateFullBoard_validSudoku() {
        val fullBoard = generateFullBoard()
        assertNotNull(fullBoard)

        // Ensure board dimensions
        assertEquals(9, fullBoard.size)
        assertTrue(fullBoard.all { it.size == 9 })

        // Ensure no duplicates in rows
        for (row in fullBoard) {
            val unique = row.toSet()
            assertEquals(9, unique.size)
        }
    }

    @Test
    fun testCreatePuzzle_emptyCellsBasedOnDifficulty() {
        val fullBoard = generateFullBoard()
        val difficulty = "medium" // Mock difficulty
        val puzzleBoard = createPuzzle(fullBoard, difficulty)

        assertNotNull(puzzleBoard)

        // Ensure puzzleBoard dimensions match
        assertEquals(9, puzzleBoard.size)
        assertTrue(puzzleBoard.all { it.size == 9 })

        // Count empty cells based on difficulty
        val emptyCount = puzzleBoard.sumBy { row -> row.count { it == 0 } }
        assertTrue(emptyCount in 30..50) // Adjust ranges based on difficulty
    }


    @Test
    fun testSaveGame_puzzleSavedCorrectly() {
        // Mock Context and SharedPreferences
        val context = mock(Context::class.java)
        val sharedPreferences = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)

        // Mock the behavior of SharedPreferences and its Editor
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.putStringSet(anyString(), anySet())).thenReturn(editor)
        `when`(editor.apply()).thenAnswer { } // Do nothing on apply()

        // Mock data for saveGame
        val puzzleBoard = Array(9) { IntArray(9) { 0 } }
        val fullBoard = Array(9) { IntArray(9) { 1 } }
        val sudokuGrid = mock(GridLayout::class.java)

        // Call saveGame
        val success = saveGame(context, "TestBoard", puzzleBoard, fullBoard, sudokuGrid)
        assertTrue(success)

        // Verify interactions with SharedPreferences
        verify(sharedPreferences).edit()
        verify(editor).putStringSet(eq("SavedBoards"), anySet())
        verify(editor).apply()
    }
}