package com.sliebald.pairshare.utils


import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.sliebald.pairshare.MyApplication

class PreferenceUtils {

    companion object {

        const val PREFERENCE_KEY_SELECTED_EXPENSE = "PREFERENCE_KEY_SELECTED_LIST"

        /**
         * Returns the id of the currently selected shared expense list. Returns null if
         * none was ever selected.
         *
         * @return The id of the currently selected expense.
         */
        var selectedSharedExpenseListID: String
            get() = PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
                    .getString(PREFERENCE_KEY_SELECTED_EXPENSE, "")!!
            set(listID) {
                val preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
                val editor = preferences.edit()
                editor.putString(PREFERENCE_KEY_SELECTED_EXPENSE, listID)
                editor.apply()
            }

        fun registerActiveListChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            PreferenceManager.getDefaultSharedPreferences(MyApplication.context).registerOnSharedPreferenceChangeListener(listener)
        }

        fun unregisterSelectedListChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            PreferenceManager.getDefaultSharedPreferences(MyApplication.context).unregisterOnSharedPreferenceChangeListener(listener)
        }
    }


}
