package software.blob.audio.tuner.preference

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import software.blob.android.preference.fragment.GenericPreferenceFragment
import software.blob.audio.tuner.R

/**
 * The preference activity for TuneBlob
 */
class PreferenceActivity : AppCompatActivity() {

    private lateinit var prefs: TunerPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = TunerPreferences(this)

        // Set the content view which is just a single FrameLayout
        setContentView(R.layout.activity_settings)

        // Open the settings based on the current active fragment
        val fragment = GenericPreferenceFragment(
            if (prefs.activeFragment == "graph")
                R.xml.graph_prefs
            else
                R.xml.advanced_prefs
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, fragment)
            .commit()
    }
}