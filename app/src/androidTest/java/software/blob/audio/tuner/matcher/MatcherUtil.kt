package software.blob.audio.tuner.matcher

import android.view.View
import androidx.annotation.IntegerRes
import org.hamcrest.Matcher

/**
 * Miscellaneous matcher helper methods
 */
object MatcherUtil {

    /**
     * Get the view matcher for a menu button with a given ID
     * @param id Resource ID
     * @return View matcher
     */
    fun menuButtonWithId(@IntegerRes id: Int): Matcher<View> = ActionBarButtonMatcher(id)
}