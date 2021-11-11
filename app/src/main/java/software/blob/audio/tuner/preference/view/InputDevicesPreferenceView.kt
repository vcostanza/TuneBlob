package software.blob.audio.tuner.preference.view

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import software.blob.android.compatibility.SDKCompat
import software.blob.android.preference.view.PreferenceView
import software.blob.audio.tuner.R
import software.blob.audio.tuner.preference.TunerPreferences

/**
 * Preference for displaying the list of available input devices
 */
class InputDevicesPreferenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : PreferenceView(context, attrs, defStyleAttr) {

    private val prefs = TunerPreferences(context)

    /**
     * Show a dialog containing the list of available input devices
     */
    override fun onClick() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        val adapter = AudioDeviceAdapter(context, devices)

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setAdapter(adapter) { _, which ->
                val device = adapter.getItem(which)
                if (device != null)
                    prefs.inputDevice = device.id
            }
            .show()
    }

    /**
     * List adapter for audio devices
     */
    private inner class AudioDeviceAdapter(context: Context, devices: Array<AudioDeviceInfo>)
        : ArrayAdapter<AudioDeviceInfo>(context, R.layout.input_device_list_row, devices) {

        private val inflater = LayoutInflater.from(context)
        private val deviceTypes = context.resources.getStringArray(R.array.device_types)
        private val addressMap = HashMap<String, String>()

        /**
         * Initialize the address alias map for more readable address names
         */
        init {
            val addresses = context.resources.getStringArray(R.array.device_addresses)
            val aliases = context.resources.getStringArray(R.array.device_addresses_aliases)
            for (i in addresses.indices) addressMap[addresses[i]] = aliases[i]
        }

        /**
         * Create/update each list row
         */
        override fun getView(position: Int, v: View?, parent: ViewGroup): View {
            val view = v ?: inflater.inflate(R.layout.input_device_list_row, parent, false)
            val title = view.findViewById<TextView>(R.id.title)
            val desc = view.findViewById<TextView>(R.id.description)
            val checkbox = view.findViewById<CheckBox>(R.id.checkbox)

            val device = getItem(position) as AudioDeviceInfo
            val address = if (SDKCompat.checkVersion(Build.VERSION_CODES.P)) device.address else ""
            val type = if (device.type < deviceTypes.size) deviceTypes[device.type] else ""
            val alias = addressMap[address]

            if (alias != null) {
                // Show both the address alias and type
                title.text = alias
                desc.text = type
                desc.visibility = View.VISIBLE
            } else {
                // If the address is not defined then show the type only
                title.text = type
                desc.visibility = View.GONE
            }

            // Check the box for the selected input device
            val prefId = prefs.inputDevice
            val selected = prefId == device.id || prefId == -1 && position == 0
            checkbox.isChecked = selected
            checkbox.visibility = if (selected) View.VISIBLE else View.GONE

            return view
        }
    }
}