package co.logu.unmashable

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class AppPref {
    companion object {
        const val masterSecretKey = "masterSecret"
        const val scannerPseudonymKey = "scannerPseudonym"
        const val advertiserPseudonymKey = "advertiserPseudonym"

        private fun Context.getAppPref() : SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(this)
        }

        fun Context.getPrefString(key: String, default: String): String {
            return this.getAppPref().getString(key, default)
        }

        fun Context.setPrefString(key: String, value: String) {
            val edit = this.getAppPref().edit()
            edit.putString(key, value)
            edit.apply()
        }
    }
}