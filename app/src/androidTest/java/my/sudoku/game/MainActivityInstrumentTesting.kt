package my.sudoku.game

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentTesting {

    @JvmField
    @Rule
    val activityScenarioRule: ActivityScenarioRule<HomeActivity> = ActivityScenarioRule(HomeActivity::class.java)

    @Test
    fun testNavigateToMainMenu() {
        // Ensure the activity is launched and resumed.
        activityScenarioRule.scenario.onActivity { activity ->
            assertNotNull(activity)
        }

        // Test loading Easy Difficulty
        onView(withId(R.id.easyButton))
            .perform(click())

        // Open the options menu
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        openActionBarOverflowOrOptionsMenu(context)

        // Click on the "Main Menu" menu item
        onView(withText("Main Menu"))
            .perform(click())

        // Verify if the homeSceneRoot view is displayed
        onView(withId(R.id.homeActivityRoot)).check(matches(isDisplayed()))
    }



    @Test
    fun testNavigateThroughDifficultyLevels() {
        // Ensure the activity is launched and resumed.
        activityScenarioRule.scenario.onActivity { activity ->
            assertNotNull(activity)
        }

        // Test navigating to Medium Difficulty
        onView(withId(R.id.mediumButton))
            .perform(click())

        // Open the options menu
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        openActionBarOverflowOrOptionsMenu(context)

        // Click on the "Main Menu" menu item
        onView(withText("Main Menu"))
            .perform(click())

        // Verify if the homeSceneRoot view is displayed
        onView(withId(R.id.homeActivityRoot)).check(matches(isDisplayed()))

        // Test navigating to Hard Difficulty
        onView(withId(R.id.hardButton))
            .perform(click())

        // Open the options menu
        openActionBarOverflowOrOptionsMenu(context)

        // Click on the "Main Menu" menu item
        onView(withText("Main Menu"))
            .perform(click())

        // Verify if the homeSceneRoot view is displayed
        onView(withId(R.id.homeActivityRoot)).check(matches(isDisplayed()))
    }
}
