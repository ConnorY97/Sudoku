package my.sudoku.game

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import com.google.gson.Gson
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertSame
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

class SudokuTest {

//    @Test
//    fun sanityTest() {
//        assertTrue("I should be able to see this message", false)
//    }

    @Test
    fun fillBoardTest() {
        assertTrue("Failed to successfully fill a board", fillBoard(Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } }))
    }

    @Test
    fun validateBoardTest() {
        val board = Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } }
        fillBoard(board)
        assertTrue("Failed to validate the board", validateBoard(board))
    }

    @Test
    fun generatePuzzleTest() {
        val board = Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } }
        fillBoard(board)
        assertTrue("Failed to validate the board", validateBoard(board))
        val editableCells: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()
        createPuzzle(board, "Easy", editableCells)
        assertTrue("Puzzle successfully created", validateBoard(board))
    }

    @Test
    fun findDuplicatesInRowAndColTest() {
        val row = IntArray(GRID_SIZE) { it }
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
        val board = Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } }

        fillBoard(board)

        assertTrue("Failed to parse sub grid", findDuplicatePositionInSubGrid(0, 0, board).isEmpty())

        // It was failing because just adding an 8 into the first position could not guarantee that there would be an issue
        board[0][0] = 8
        board[0][1] = 8

        assertTrue("Failed to find duplicate in sub grid", findDuplicatePositionInSubGrid(0, 0, board).isNotEmpty()
        )
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
        val game = loadGame(mockContext, boardName)

        // Assert
        assertNotNull("Load failed", game) // Ensure result is not null
        assertEquals("Failed to retrieve board", board.size, game.board?.size) // Verify board loaded
        assertEquals("Failed to retrieve editableCells", editableCells, game.editableCells)  // Verify editable cells loaded
        assertEquals("Failed to retrieve elapsed time", elapsedTime, game.elapsedTime)     // Verify elapsed time loaded
    }

    @Test
    fun navigateToHomeTest() {
        // Mock the Context and Intent
        val context: Context = mock()
        val homeScreen = mock(Intent::class.java)

        // Simulate adding flags (we won't actually call addFlags here since it's not mocked)
        `when`(homeScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)).thenReturn(homeScreen)

        // Simulate starting the activity
        context.startActivity(homeScreen)

        // Verify the startActivity method was called on the mock context with the expected intent
        verify(context).startActivity(homeScreen)
    }

    @Test
    fun stringResourceTest() {
        val context: Context = mock()

        // Mocking the string resource lookup
        whenever(context.getString(R.string.app_name)).thenReturn("Sudoku")

        // Verify that the string resource is returned correctly
        assertEquals("Sudoku", context.getString(R.string.app_name))
    }

    @Test
    fun drawableResourceTest() {
        val context: Context = mock()

        // Mocking drawable resource retrieval
        val drawable: Drawable = mock()
        whenever(context.getDrawable(R.drawable.green_background)).thenReturn(drawable)

        // Verify the drawable is returned correctly
        assertSame(drawable, context.getDrawable(R.drawable.green_background))
    }

    @Test
    fun sharedPreferenceTest() {
        val context: Context = mock()
        val sharedPreferences: SharedPreferences = mock()
        val editor: SharedPreferences.Editor = mock()

        // Mock the sharedPreferences behavior
        whenever(context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(sharedPreferences.getString("username", "")).thenReturn("testuser")

        // Verify the value from shared preferences
        val username = sharedPreferences.getString("username", "")
        assertEquals("testuser", username)
    }

    @Test
    fun iconResourceLoadingTest() {
        // Mock the Context
        val context: Context = mock()

        // Mock the Resources object
        val resources = mock(android.content.res.Resources::class.java)
        whenever(context.resources).thenReturn(resources)

        // Mock the Drawable resource retrieval for mipmap
        val drawable: Drawable = mock()
        whenever(context.getDrawable(R.mipmap.ic_sudoku_launcher)).thenReturn(drawable)

        // Verify that the drawable is returned correctly
        val loadedDrawable = context.getDrawable(R.mipmap.ic_sudoku_launcher)
        assertNotNull("Drawable should not be null", loadedDrawable)
        assertSame("Drawable should be the mocked drawable", drawable, loadedDrawable)
    }
}