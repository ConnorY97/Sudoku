package my.sudoku.game

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test

class LoadActivityInstrumentTesting {
    @JvmField
    @Rule
    val activityScenarioRule: ActivityScenarioRule<HomeActivity> = ActivityScenarioRule(HomeActivity::class.java)

    @Test
    fun testLoadGameAndReturnToMainMenu() {
        // Ensure the activity is launched and resumed.
        activityScenarioRule.scenario.onActivity { activity ->
            assertNotNull(activity)
        }

        // Test loading a saved game
        onView(withId(R.id.loadGameButton))
            .perform(click())

        onView(withId(R.id.backButton)).perform(click())

        // Verify if the homeSceneRoot view is displayed
        onView(withId(R.id.homeActivityRoot)).check(matches(isDisplayed()))
    }
}