package my.sudoku.game

import GameState
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import my.sudoku.game.management.GameManager
import my.sudoku.game.viewmodel.GameViewModel
import org.junit.Test
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anySet
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class LoadingFunctionalityTesting {

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
        val elapsedTime = 120L
        val gameState = GameState()
        val savedBoards = mutableSetOf<String>()
        val viewModel = GameViewModel()
        val gameManager = GameManager(mockContext)

        // Mock retrieving saved boards
        `when`(mockSharedPreferences.getStringSet("SavedBoards", mutableSetOf()))
            .thenReturn(savedBoards)

        // Act
        val result = gameManager.saveGame(boardName, gameState, elapsedTime, viewModel)

        // Assert
        assertEquals("Failed to save a board", true, result)
        verify(mockEditor).putStringSet("SavedBoards", setOf(boardName))
        verify(mockEditor).putString("${boardName}_board", gson.toJson(gameState.board))
        verify(mockEditor).putString("${boardName}_editableCells", gson.toJson(gameState.editableCells))
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
        val gameState = GameState()
        val elapsedTime = 120L
        val viewModel = GameViewModel()
        val savedBoards = mutableSetOf(boardName) // Pretend the board already exists
        val gameManager = GameManager(mockContext)
        // Mock retrieving saved boards
        `when`(mockSharedPreferences.getStringSet("SavedBoards", mutableSetOf()))
            .thenReturn(savedBoards)

        // Act
        val result = gameManager.saveGame(boardName, gameState, elapsedTime, viewModel)

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
        val gameManager = GameManager(mockContext)

        // Mock stored data
        val boardJson = gson.toJson(board)
        val editableCellsJson = gson.toJson(editableCells)

        `when`(mockSharedPreferences.getString("${boardName}_board", null)).thenReturn(boardJson)
        `when`(mockSharedPreferences.getString("${boardName}_editableCells", null))
            .thenReturn(editableCellsJson)
        `when`(mockSharedPreferences.getLong("${boardName}_elapsedTime", 0)).thenReturn(elapsedTime)

        // Act
        val game = gameManager.loadGame(boardName)

        // Assert
        assertNotNull("Load failed", game) // Ensure result is not null
        assertEquals("Failed to retrieve board", board.size, game.board.size) // Verify board loaded
        assertEquals("Failed to retrieve editableCells", editableCells, game.editableCells)  // Verify editable cells loaded
        assertEquals("Failed to retrieve elapsed time", elapsedTime, game.elapsedTime)     // Verify elapsed time loaded
    }
}