package software.blob.android.preference.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

/**
 * A generic preference fragment that can be used to easily inflate a preference layout
 * in cases where special sub-class behavior isn't needed
 */
class GenericPreferenceFragment(var xmlId: Int = 0) : PreferenceFragmentCompat() {

    /**
     * Set preference from the given XML resource ID
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (xmlId != 0)
            setPreferencesFromResource(xmlId, rootKey)
    }
}