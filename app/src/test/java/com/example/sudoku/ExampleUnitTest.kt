package com.example.sudoku

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mockito.*

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

    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private val gson = Gson()
    private var initialized = false

    private fun setUp() {
        // Mock SharedPreferences and its editor
        mockContext = mock(Context::class.java)
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences("SudokuGame", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putStringSet(anyString(), anySet())).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.apply()).then {}
        initialized = true
    }

    @Test
    fun saveGameSuccessTest() {
        // Only trigger setup if required
        if (!initialized)
            setUp()

        // Arrange
        val boardName = "TestBoard"
        val gridSize = 9
        val board = Array(gridSize) { IntArray(gridSize) { 0 } }
        val editableCells = mapOf(Pair(0, 0) to true, Pair(1, 1) to false)
        val elapsedTime = 120L

        val savedBoards = mutableSetOf<String>()

        // Mock retrieving saved boards
        `when`(mockSharedPreferences.getStringSet("SavedBoards", mutableSetOf()))
            .thenReturn(savedBoards)

        // Act
        val result = saveGame(mockContext, boardName, board, editableCells, elapsedTime)

        // Assert
        assertEquals("Failed to save a board", true, result)
        verify(mockEditor).putStringSet("SavedBoards", setOf(boardName))
        verify(mockEditor).putString("${boardName}_board", gson.toJson(board))
        verify(mockEditor).putString("${boardName}_editableCells", gson.toJson(editableCells))
        verify(mockEditor).putLong("${boardName}_elapsedTime", elapsedTime)
        verify(mockEditor).apply()
    }

    @Test
    fun saveGameFailureTest() {
        // Only trigger setup if required
        if (!initialized)
            setUp()

        // Arrange
        val boardName = "TestBoard"
        val gridSize = 9
        val board = Array(gridSize) { IntArray(gridSize) { 0 } }
        val editableCells = mapOf(Pair(0, 0) to true, Pair(1, 1) to false)
        val elapsedTime = 120L

        val savedBoards = mutableSetOf(boardName) // Pretend the board already exists

        // Mock retrieving saved boards
        `when`(mockSharedPreferences.getStringSet("SavedBoards", mutableSetOf()))
            .thenReturn(savedBoards)

        // Act
        val result = saveGame(mockContext, boardName, board, editableCells, elapsedTime)

        // Assert
        assertEquals("Failed to save a board", false, result) // Saving should fail
        verify(mockEditor, never()).putString(anyString(), anyString()) // Ensure no saving occurred
    }

    @Test
    fun loadGameSuccessTest() {
        if (!initialized)
            setUp()

        // Arrange
        val boardName = "TestBoard"
        val gridSize = 9
        val board = Array(gridSize) { IntArray(gridSize) { 0 } }
        val editableCells = mapOf(Pair(0, 0) to true, Pair(1, 1) to false)
        val elapsedTime = 120L

        // Mock stored data
        val boardJson = gson.toJson(board)
        val editableCellsJson = gson.toJson(editableCells)

        `when`(mockSharedPreferences.getString("${boardName}_board", null)).thenReturn(boardJson)
        `when`(mockSharedPreferences.getString("${boardName}_editableCells", null))
            .thenReturn(editableCellsJson)
        `when`(mockSharedPreferences.getLong("${boardName}_elapsedTime", 0)).thenReturn(elapsedTime)

        // Act
        val result = loadGame(mockContext, boardName)

        // Assert
        assertNotNull("Load failed", result) // Ensure result is not null
        assertEquals("Failed to retrieve board", board.size, result.first?.size) // Verify board loaded
        assertEquals("Failed to retrieve editableCells", editableCells, result.second)  // Verify editable cells loaded
        assertEquals("Failed to retrieve elapsed time", elapsedTime, result.third)     // Verify elapsed time loaded
    }
}