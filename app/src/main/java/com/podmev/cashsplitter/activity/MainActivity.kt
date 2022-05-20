package com.podmev.cashsplitter.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.podmev.cashsplitter.R
import com.podmev.cashsplitter.data.UIDataState
import com.podmev.cashsplitter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    /*we need field so garbage collector would not clean it*/
    private lateinit var onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.i("mainActivity", "onCreate: started")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.toolbar)
            binding.toolbar.setNavigationOnClickListener { onBackPressed() }

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
            val navController = navHostFragment.navController
            appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)

            onSharedPreferenceChangeListener = usePreferencesOnInit()

            Log.i("mainActivity", "onCreate: finished")
        } catch (e: Throwable) {
            Log.e("mainActivity", "onCreate failed: ${e.javaClass}, ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu
    ): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        //specially for setting fragment make menu Invisible
        Log.i("mainActivity", "onCreateOptionsMenu isMenuHidden=${UIDataState.isMenuHidden()}")
        if(UIDataState.isMenuHidden()){
            for(i in 0 until menu.size()){
                menu.getItem(i).isVisible = false
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.action_settings){
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.open_settings_fragment)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //preferences

    private fun usePreferencesOnInit() : SharedPreferences.OnSharedPreferenceChangeListener{
        Log.i("mainActivity", "usePreferencesOnInit: started")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        usePreferencesValues(sharedPreferences)
        //Setup a shared preference listener for hpwAddress and restart transport
        val listener = SharedPreferences.OnSharedPreferenceChangeListener {
                preferences, key ->
            Log.i("mainActivity", "OnSharedPreferenceChangeListener: started")
            if(key!= null && preferences!= null)
                useUpdatedPreferenceValue(preferences, key)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        Log.i("mainActivity", "usePreferencesOnInit: finished")
        return  listener
    }

    private fun usePreferencesValues(sharedPreferences: SharedPreferences){
        Log.i("mainActivity", "usePreferencesValues: started")
        useDarkThemeSetting(sharedPreferences)
        //TODO add others
        Log.i("mainActivity", "usePreferencesValues: finished")
    }

    private fun useUpdatedPreferenceValue(sharedPreferences: SharedPreferences, key: String){
        Log.i("mainActivity", "useUpdatedPreferenceValue: started for key $key")
        when(key){
            resources.getString(R.string.settings_view_useDarkTheme_key)->useDarkThemeSetting(sharedPreferences)
            //TODO add others
        }
        Log.i("mainActivity", "useUpdatedPreferenceValue: started for key $key")
    }

    private fun useDarkThemeSetting(sharedPreferences: SharedPreferences){
        Log.i("mainActivity", "useDarkThemeSetting: started")
        val useDarkTheme = sharedPreferences.getBoolean(resources.getString(R.string.settings_view_useDarkTheme_key), false)
        //if it's night disable flag will not do anything
        val newNightMode = if(useDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        setDefaultNightMode(newNightMode)
        Log.i("mainActivity", "useDarkThemeSetting: finished")
    }
}