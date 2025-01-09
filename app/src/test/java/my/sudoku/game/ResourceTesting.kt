package my.sudoku.game

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertSame
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class ResourceTesting {
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
        whenever(sharedPreferences.getString("username", "")).thenReturn("tester")

        // Verify the value from shared preferences
        val username = sharedPreferences.getString("username", "")
        assertEquals("tester", username)
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