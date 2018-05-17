package co.logu.unmashable

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import co.logu.unmashable.log.AppLog

class ConfigFragment : PreferenceFragmentCompat() {
    private var configUpdated = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.config, rootKey)
    }

    override fun onResume() {
        super.onResume()

        configUpdated = false
    }

    override fun onPause() {
        super.onPause()

        if (configUpdated) {
            AppLog.d("config updated, recompute member secrets")
            AppGlobal.initMembers(activity)
        }
    }
}
