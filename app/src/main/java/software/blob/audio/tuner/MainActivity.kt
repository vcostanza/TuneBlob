package software.blob.audio.tuner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import software.blob.audio.tuner.databinding.ActivityMainBinding
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import software.blob.audio.tuner.engine.TunerInputEngine
import software.blob.audio.tuner.fragment.DualMeterFragment
import software.blob.audio.tuner.fragment.GraphMeterFragment
import software.blob.audio.tuner.fragment.RadialMeterFragment
import software.blob.audio.tuner.preference.PreferenceActivity
import software.blob.audio.tuner.preference.TunerPreferences
import kotlin.system.exitProcess

// Request code for recording audio
private const val PERMISSION_REQUEST_AUDIO = 0

/**
 * The main application activity
 * Manages top-level views and the [TunerInputEngine] instance
 */
class MainActivity : AppCompatActivity() {

    // Preferences
    private lateinit var prefs: TunerPreferences

    // View binding
    private lateinit var binding: ActivityMainBinding

    // Active fragment
    private var fragment: Fragment? = null

    // Menu buttons
    private var radialBtn: MenuItem? = null
    private var graphBtn: MenuItem? = null
    private var dualBtn: MenuItem? = null

    /**
     * App creation method
     * @param savedInstanceState Saved instance state (unused for now)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup preferences
        prefs = TunerPreferences(this)

        // Set day night mode based on preference
        setNightMode(prefs.nightMode)

        // Setup views
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show the active fragment
        showFragment(getFragment(prefs.activeFragment))
    }

    /**
     * Set the action bar menu
     * @param menu Menu builder
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        radialBtn = menu?.findItem(R.id.radial_meter)
        graphBtn = menu?.findItem(R.id.graph_meter)
        dualBtn = menu?.findItem(R.id.dual_meter)
        updateMenu()
        return true
    }

    /**
     * Action bar button callback
     * @param item Menu item that was tapped
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.radial_meter -> {
                prefs.activeFragment = "radial"
                showFragment(RadialMeterFragment())
                true
            }
            R.id.graph_meter -> {
                prefs.activeFragment = "graph"
                showFragment(GraphMeterFragment())
                true
            }
            R.id.dual_meter -> {
                prefs.activeFragment = "dual"
                showFragment(DualMeterFragment())
                true
            }
            R.id.theme_toggle -> {
                toggleNightMode()
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, PreferenceActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * The result of a user permission request
     * @param requestCode Permission request code (app-defined)
     * @param permissions Array of permissions that were requested
     * @param grantResults Corresponding request results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Record audio permission
        if (requestCode == PERMISSION_REQUEST_AUDIO) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Proceed to start the app
                showFragment(getFragment(prefs.activeFragment))
            } else {
                // Nothing we can do without this permission - exit
                exitProcess(1)
            }
        }
    }

    /**
     * Request permission to record audio from the user's microphone
     * The user is given 2 chances to accept the permission before the
     * app automatically shuts down
     */
    private fun requestAudioPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            // The user has denied the permission once, let them know audio recording is required
            Snackbar.make(binding.root, R.string.microphone_access_required,
                Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),
                    PERMISSION_REQUEST_AUDIO)
            }.show()
        } else {
            // Initial permission request
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_AUDIO)
        }
    }

    /**
     * Get fragment based on its name
     * @param name Fragment name (radial, graph, or duel)
     */
    private fun getFragment(name: String): Fragment {
        return when (name) {
            "radial" -> RadialMeterFragment()
            "graph" -> GraphMeterFragment()
            else -> DualMeterFragment()
        }
    }

    /**
     * Show a fragment
     * @param fragment Fragment to show
     */
    private fun showFragment(fragment: Fragment) {
        // Need to make sure we can record audio first
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED) {
            requestAudioPermission()
            return
        }

        this.fragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        // Update the menu so we're not showing both fragment buttons at once
        updateMenu()
    }

    /**
     * Update the menu to the current state
     * Currently this just toggles the visibility of the fragment buttons
     */
    private fun updateMenu() {
        radialBtn?.isVisible = fragment !is RadialMeterFragment
        graphBtn?.isVisible = fragment !is GraphMeterFragment
        dualBtn?.isVisible = fragment !is DualMeterFragment
    }

    /**
     * Get the current night mode based on configuration settings
     * @return Configuration night mode (not the same as [AppCompatDelegate] constants)
     */
    private fun getNightMode() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    /**
     * Set day/night mode based on preference
     * @param mode Night mode ([AppCompatDelegate] constant)
     */
    private fun setNightMode(mode: Int) {
        // Get the corresponding night mode preference from the current configuration
        val configMode = when (getNightMode()) {
            Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
            Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        }

        // Avoid an unnecessary activity restart if the configured mode is the same
        // as the preferred mode
        if (mode != configMode && mode != AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)
            AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Toggle between night and day mode
     */
    private fun toggleNightMode() {
        val newMode = when (getNightMode()) {
            Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_YES
        }
        prefs.nightMode = newMode
        AppCompatDelegate.setDefaultNightMode(newMode)
    }
}