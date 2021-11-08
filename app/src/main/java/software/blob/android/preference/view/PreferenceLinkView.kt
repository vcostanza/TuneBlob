package software.blob.android.preference.view

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.FragmentActivity
import software.blob.android.preference.fragment.GenericPreferenceFragment
import software.blob.audio.tuner.R

/**
 * A preference that links to another preference fragment
 */
open class PreferenceLinkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : PreferenceView(context, attrs, defStyleAttr) {

    var xmlId: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceLinkView, 0, 0)
        xmlId = a.getResourceId(R.styleable.PreferenceLinkView_xml, 0)
        a.recycle()
    }

    /**
     * Navigate to the linked preference screen
     */
    override fun onClick() {
        if (xmlId != 0 && context is FragmentActivity) {
            (context as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, GenericPreferenceFragment(xmlId))
                .addToBackStack(null)
                .commit()
        }
    }
}