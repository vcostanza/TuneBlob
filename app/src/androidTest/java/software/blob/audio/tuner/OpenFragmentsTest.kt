package software.blob.audio.tuner

import android.content.res.Resources
import androidx.annotation.IntegerRes
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import software.blob.audio.tuner.matcher.MatcherUtil
import software.blob.audio.tuner.preference.TunerPreferences

/**
 * Instrumented test that opens and checks the validity of each of the fragments
 */
@RunWith(AndroidJUnit4::class)
class OpenFragmentsTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity>
            = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var resources: Resources
    private lateinit var prefs: TunerPreferences

    /**
     * Initialize context-based variables
     */
    @Before
    fun init() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        prefs = TunerPreferences(appContext)
        resources = appContext.resources
    }

    /**
     * Click each of the action bar buttons that corresponds to an open fragment and check
     * that the fragment contains the expected views
     */
    @Test
    fun checkFragments() {
        // Radial meter
        clickMenuButton(R.id.radial_meter, "radial")
        assertViewsExist(R.id.text, R.id.radial_view)

        // Graph meter
        clickMenuButton(R.id.graph_meter, "graph")
        assertViewsExist(R.id.text, R.id.graph_view)

        // Dual meter
        clickMenuButton(R.id.dual_meter, "dual")
        assertViewsExist(R.id.text, R.id.graph_view, R.id.radial_view)
    }

    /**
     * Click a menu button while asserting non-null and the description
     * @param id Resource ID
     * @param fragValue Corresponding fragment preference value (null to ignore)
     */
    fun clickMenuButton(@IntegerRes id: Int, fragValue: String? = null) {
        var visible = false
        val view = onView(MatcherUtil.menuButtonWithId(id)).check { view, _ ->
            val activeFragment = prefs.activeFragment
            assertNotNull("Active fragment is null", activeFragment)
            if (fragValue != null) {
                if (view == null)
                    assertEquals("Unexpected active fragment", activeFragment, fragValue)
                else
                    assertNotEquals("Unexpected active fragment", activeFragment, fragValue)
            } else
                assertNotNull("Action bar button missing", view)
            visible = view != null
        }
        if (visible) view.perform(click())
    }

    /**
     * Check if a set of views exist
     * @param ids Resource IDs
     */
    fun assertViewsExist(vararg ids: Int) {
        for (id in ids) {
            onView(withId(id)).check { view, _ ->
                val viewName = resources.getResourceName(id) ?: id.toString()
                assertNotNull("$viewName not found", view)
            }
        }
    }
}