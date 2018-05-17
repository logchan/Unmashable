package co.logu.unmashable.framework

import android.content.Context
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.PreferenceManager
import android.util.AttributeSet

// Ret

/**
 * This class was created by Anthony M Cannon on 17/04/2018.
 */
class SummarisedEditTextPreference @JvmOverloads constructor(context: Context,
                                                             attrs: AttributeSet? = null) : EditTextPreference(context, attrs) {

    private var mOnChangeListener: OnPreferenceChangeListener? = null

    init {
        super.setOnPreferenceChangeListener { preference, newValue ->
            summary = newValue as String
            mOnChangeListener?.onPreferenceChange(preference, newValue) ?: true
        }
    }

    /**
     * Called when this Preference has been attached to a Preference hierarchy.
     * Make sure to call the super implementation.
     *
     * @param preferenceManager The PreferenceManager of the hierarchy.
     */
    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)
        summary = sharedPreferences.getString(key, null)
    }


    /**
     * Sets the callback to be invoked when this Preference is changed by the
     * user (but before the internal state has been updated).
     *
     * @param onPreferenceChangeListener The callback to be invoked.
     */
    override fun setOnPreferenceChangeListener(
            onPreferenceChangeListener: OnPreferenceChangeListener) {
        mOnChangeListener = onPreferenceChangeListener
    }
}
