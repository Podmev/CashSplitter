package com.podmev.cashsplitter.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.podmev.cashsplitter.R
import com.podmev.cashsplitter.data.UIDataState


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

    }

    override fun onResume() {
        super.onResume()
        UIDataState.hideMenu()
        requireActivity().invalidateOptionsMenu()
    }

    override fun onStop() {
        super.onStop()
        UIDataState.unhideMenu()
        requireActivity().invalidateOptionsMenu()
    }

}