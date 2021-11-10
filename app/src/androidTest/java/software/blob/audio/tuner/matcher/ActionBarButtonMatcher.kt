package software.blob.audio.tuner.matcher

import android.content.res.Resources
import android.view.View
import androidx.annotation.IntegerRes
import androidx.appcompat.view.menu.ActionMenuItemView
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher

/**
 * A view matcher that specifically matches [ActionMenuItemView] buttons with a given resource ID
 * @param id Resource ID to match
 */
class ActionBarButtonMatcher(@IntegerRes private val id: Int) : TypeSafeDiagnosingMatcher<View>() {

    private var resources: Resources? = null

    /**
     * Get the description for the match
     * @param description Description to append to
     */
    override fun describeTo(description: Description) {
        description.appendText("view.getId() ")
        val res = resources
        if (res != null)
            description.appendText(res.getResourceName(id))
        else
            description.appendText(id.toString())
    }

    /**
     * Test if a given view is an [ActionMenuItemView] and matches the provided resource ID
     * @param item View to check
     * @param mismatchDescription Description to append when a mismatch occurs (unused)
     */
    override fun matchesSafely(item: View, mismatchDescription: Description): Boolean {
        resources = item.resources
        return item is ActionMenuItemView && item.id == id
    }
}