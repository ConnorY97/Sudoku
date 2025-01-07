package com.example.sudoku

import android.widget.EditText
import android.widget.GridLayout
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import my.sudoku.game.GameState
import my.sudoku.game.GameViewModel
import my.sudoku.game.MainActivity
import my.sudoku.game.R

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testInitializeSudokuGrid() {
        // Launch MainActivity
        activityRule.scenario.onActivity { activity ->
            // Access the GridLayout and ViewModel
            val sudokuGrid = activity.findViewById<GridLayout>(R.id.sudokuGrid)
            val viewModelField = MainActivity::class.java.getDeclaredField("viewModel").apply {
                isAccessible = true
            }

            // Mock the ViewModel and set it in the MainActivity
            val mockViewModel = mockViewModel()
            viewModelField.set(activity, mockViewModel)

            // Access private initializeSudokuGrid method using reflection
            val method = MainActivity::class.java.getDeclaredMethod("initializeSudokuGrid")
            method.isAccessible = true

            // Act: Invoke the private method
            method.invoke(activity)

            // Assert: Check if the grid is populated with cells
            assertNotNull("Grid layout should not be null", sudokuGrid)
            assertEquals(
                "Sudoku grid should contain 81 cells",
                81,
                sudokuGrid.childCount
            )

            // Verify some properties of the added cells
            val firstCell = sudokuGrid.getChildAt(0) as EditText
            assertNotNull("First cell should not be null", firstCell)
            assertEquals(
                "First cell should have specific padding",
                10,
                firstCell.paddingLeft
            )
        }
    }

    private fun mockViewModel(): GameViewModel {
        // Create a mock ViewModel with test GameState
        val gameState = MutableLiveData(
            GameState(
                board = Array(9) { IntArray(9) { 0 } }, // Empty board
                editableCells = mutableMapOf(Pair(0, 0) to true, Pair(1, 1) to false)
            )
        )

        return mock(GameViewModel::class.java).apply {
            `when`(getGameState()).thenReturn(gameState)
        }
    }
}
