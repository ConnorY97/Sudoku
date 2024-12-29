//package com.example.sudoku
//
//import android.widget.GridLayout
//import androidx.test.platform.app.InstrumentationRegistry
//import androidx.test.ext.junit.runners.AndroidJUnit4
//
//import org.junit.Test
//import org.junit.runner.RunWith
//
//import org.junit.Assert.*
//import org.mockito.Mockito.mock
//
//
///**
// * Instrumented test, which will execute on an Android device.
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//@RunWith(AndroidJUnit4::class)
//class ExampleInstrumentedTest {
//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.example.sudoku", appContext.packageName)
//    }
//}
//
//class SudokuTest {
//    @Test
//    fun testSaveGame_puzzleSavedCorrectly() {
//        // Get the app context from InstrumentationRegistry
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//
//        val puzzleBoard = Array(9) { IntArray(9) { 0 } }
//        val fullBoard = Array(9) { IntArray(9) { 1 } } // Mock solved board
//        val sudokuGrid = mock(GridLayout::class.java) // Mock GridLayout
//
//        // Call saveGame
//        val success = saveGame(context, "TestBoard", puzzleBoard, fullBoard, sudokuGrid, 0)
//        assertTrue(success)
//    }
//}