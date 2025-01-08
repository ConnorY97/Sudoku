package my.sudoku.game

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstrumentTest {


    @JvmField
    @Rule
    val activityScenarioRule: ActivityScenarioRule<HomeActivity> = ActivityScenarioRule(HomeActivity::class.java)


    @Test
    fun appLaunchTest() {
        // The ActivityScenarioRule automatically launches the activity.
        // When the test ends, the activity is automatically closed.
        // If no exceptions occur, the test is considered successful.
    }

    @Test
    fun loadSceneTest() {
        // Ensure the activity is launched and resumed.
        activityRule.scenario.onActivity { activity ->
            // Assert that the activity is not null and is properly launched.
            assertNotNull(activity)
        }

        // Simulate a click on the button that opens the Load Scene
        onView(withId(R.id.loadGameButton)) // Replace with your actual button ID
            .perform(click())

        // Ensure the Load Scene root view is displayed
        onView(withId(R.id.loadSceneRoot)) // Replace with the actual root view ID of the Load Scene
            .check(matches(isDisplayed()))
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    @Test
    fun loadDifficultiesTest() {
        // Ensure the activity is launched and resumed.
        activityRule.scenario.onActivity { activity ->
            assertNotNull(activity)
        }

        // Test loading Easy Difficulty
        onView(withId(R.id.easyButton)) // Replace with the actual button ID for easy difficulty
            .perform(click())
        onView(withId(R.id.sudokuGrid)) // Ensure the grid loads correctly
            .check(matches(isDisplayed()))
    }
}