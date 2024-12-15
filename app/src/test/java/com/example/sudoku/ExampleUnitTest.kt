//package com.example.sudoku
//
//import android.content.Context
//import android.content.SharedPreferences
//import android.graphics.Color
//import android.text.Editable
//import android.text.SpannableStringBuilder
//import android.widget.EditText
//import android.widget.GridLayout
//import org.junit.Test
//
//import org.junit.Assert.*
//import org.mockito.ArgumentMatchers.anyInt
//import org.mockito.ArgumentMatchers.anySet
//import org.mockito.ArgumentMatchers.anyString
//import org.mockito.ArgumentMatchers.eq
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.verify
//import org.mockito.Mockito.`when`
//
//
//class SudokuTest {
//
//    // Mock function for generateFullBoard
//    private fun generateFullBoard(): Array<IntArray> {
//        val board = Array(9) { IntArray(9) }
//        // Simulate a filled board
//        for (row in 0..8) {
//            for (col in 0..8) {
//                board[row][col] = (row * 3 + row / 3 + col) % 9 + 1 // Valid Sudoku generation logic
//            }
//        }
//        return board
//    }
//
//    @Test
//    fun generate_Full_Board() {
//        val fullBoard = generateFullBoard()
//        assertNotNull(fullBoard)
//
//        // Ensure board dimensions
//        assertEquals(9, fullBoard.size)
//        assertTrue(fullBoard.all { it.size == 9 })
//
//        // Ensure no duplicates in rows
//        for (row in fullBoard) {
//            val unique = row.toSet()
//            assertEquals(9, unique.size)
//        }
//    }
//
//    @Test
//    fun create_Puzzle() {
//        val fullBoard = generateFullBoard()
//        val difficulty = "medium" // Mock difficulty
//        val puzzleBoard = createPuzzle(fullBoard, difficulty)
//
//        assertNotNull(puzzleBoard)
//
//        // Ensure puzzleBoard dimensions match
//        assertEquals(9, puzzleBoard.size)
//        assertTrue(puzzleBoard.all { it.size == 9 })
//
//        // Count empty cells based on difficulty
//        val emptyCount = puzzleBoard.sumBy { row -> row.count { it == 0 } }
//        assertTrue(emptyCount in 30..50) // Adjust ranges based on difficulty
//    }
//
//
//    @Test
//    fun save_Game() {
//        // Mock Context and SharedPreferences
//        val context = mock(Context::class.java)
//        val sharedPreferences = mock(SharedPreferences::class.java)
//        val editor = mock(SharedPreferences.Editor::class.java)
//
//        // Mock the behavior of SharedPreferences and its Editor
//        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
//        `when`(sharedPreferences.edit()).thenReturn(editor)
//        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
//        `when`(editor.putStringSet(anyString(), anySet())).thenReturn(editor)
//        `when`(editor.apply()).thenAnswer { } // Do nothing on apply()
//
//        // Mock data for saveGame
//        val puzzleBoard = Array(9) { IntArray(9) { 0 } }
//        val fullBoard = Array(9) { IntArray(9) { 1 } }
//        val sudokuGrid = mock(GridLayout::class.java)
//
//        // Call saveGame
//        val success = saveGame(context, "TestBoard", puzzleBoard, fullBoard, sudokuGrid, 0)
//        assertTrue(success)
//
//        // Verify interactions with SharedPreferences
//        verify(sharedPreferences).edit()
//        verify(editor).putStringSet(eq("SavedBoards"), anySet())
//        verify(editor).apply()
//    }
//
////    @Test
////    fun testCheckSolutions_correctAndIncorrectAnswers() {
////        val gridSize = 9
////        val difficulty = "easy"
////
////        // Mock GridLayout and Context
////        val sudokuGrid = mock(GridLayout::class.java)
////        val context = mock(Context::class.java)
////
////        // Generate full board and puzzle board
////        val fullBoard = generateFullBoard()
////        val puzzleBoard = createPuzzle(fullBoard, difficulty)
////
////        // Mock cells
////        val cells = Array(gridSize * gridSize) { mock(EditText::class.java) }
////        for (i in cells.indices) {
////            `when`(sudokuGrid.getChildAt(i)).thenReturn(cells[i])
////        }
////
////        // Initialize the grid
////        initializeGrid(context, gridSize, puzzleBoard, sudokuGrid)
////
////        // Populate some cells with correct and incorrect user input
////        val correctCellIndex = 0
////        val incorrectCellIndex = 1
////
////        `when`(cells[correctCellIndex].text).thenReturn(mockEditable(fullBoard[0][0].toString())) // Correct value
////        `when`(cells[incorrectCellIndex].text).thenReturn(mockEditable("999")) // Incorrect value
////        `when`(cells[correctCellIndex].isFocusable).thenReturn(true)
////        `when`(cells[incorrectCellIndex].isFocusable).thenReturn(true)
////
////        // Populate the grid with correct values (simulated action)
////        populateGridWithCorrectValues(gridSize, sudokuGrid, fullBoard)
////
////        // Call checkSolutions to validate
////        checkSolutions(gridSize, sudokuGrid, fullBoard)
////
////        // Verify the background colors of the cells
////        verify(cells[correctCellIndex]).setBackgroundColor(Color.GREEN) // Correct answer
////        verify(cells[incorrectCellIndex]).setBackgroundColor(Color.RED) // Incorrect answer
////    }
////
////    // Helper function to mock Editable
////    private fun mockEditable(text: String): Editable {
////        val editable = mock(Editable::class.java)
////        `when`(editable.toString()).thenReturn(text)
////        return editable
////    }
////
////
////    private fun populateGridWithCorrectValues(gridSize: Int, sudokuGrid: GridLayout, fullBoard: Array<IntArray>) {
////        for (row in 0 until gridSize) {
////            for (col in 0 until gridSize) {
////                // Calculate the index of the child in the GridLayout
////                val index = row * gridSize + col
////
////                // Access the child view (EditText)
////                val cell = sudokuGrid.getChildAt(index) as? EditText
////
////                // Populate the cell with the correct value from fullBoard
////                if (cell != null) {
////                    val correctValue = fullBoard[row][col]
////                    cell.setText(correctValue.toString())
////                }
////            }
////        }
////    }
//}