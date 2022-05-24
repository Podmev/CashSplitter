package com.podmev.cashsplitter.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.appcompat.view.menu.MenuBuilder
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.podmev.cashsplitter.R
import com.podmev.cashsplitter.data.*
import com.podmev.cashsplitter.databinding.ActivityMainBinding
import com.podmev.cashsplitter.fragment.MainFragment
import com.podmev.cashsplitter.utils.formatNowSnakeCase
import com.podmev.cashsplitter.utils.getVisibilityInt
import java.io.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    /*we need field so garbage collector would not clean it*/
    private lateinit var onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    private var getFilesSaveLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val data: Intent = it.data!!
            saveFiles(data)
        }
    }

    private var getFilesOpenLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val data: Intent = it.data!!
            openFiles(data)
        }
    }

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

    @SuppressLint("RestrictedApi")
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
        } else{
            if (menu is MenuBuilder) {
                menu.setOptionalIconsVisible(true)
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
        if(id == R.id.action_save_to_file){
            //TODO make backup through file manager
            val fileName = "cash_splitter_backup_${formatNowSnakeCase()}.txt"

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intent.putExtra(Intent.EXTRA_TITLE, fileName)
            intent.putExtra("myType", "save")
            intent.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
            getFilesSaveLauncher.launch(intent)
            return true
        }
        if(id == R.id.action_load_from_file){
            //TODO restore backup through file manager
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intent.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
            getFilesOpenLauncher.launch(intent)
            return true
        }
        if(id == R.id.action_about){
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.open_about_fragment)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //files

    private fun saveFiles(intent: Intent){
        try {
            Log.i("mainActivity", "saveFiles started")
            //TODO make normal way
            val uri = intent.data!!
            val outputStream = contentResolver.openOutputStream(uri)
            val writer = BufferedWriter(OutputStreamWriter(outputStream))

            //HACK
            val mainFragment = MainFragment.fragmentInstance!!
            val content = mainFragment.dataState.serialize()
            writer.write(content)
            writer.flush()
            writer.close()
            Toast.makeText(this,  resources.getString(R.string.toast_backup_created), Toast.LENGTH_SHORT).show()
            Log.i("mainActivity", "saveFiles finished: ${content}")
        } catch (e:Throwable){
            Toast.makeText(this,  resources.getString(R.string.toast_backup_creating_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFiles(intent: Intent){
        try {
            //TODO make normal way
            val uri = intent.data!!
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            //HACK
            val content = reader.readLines().joinToString("\n")
            reader.close()
            val maybeBackupDataState: DataState? = parseBackupContent(content)
            if (maybeBackupDataState == null) {
                Toast.makeText(this,  resources.getString(R.string.toast_backup_broken), Toast.LENGTH_SHORT).show()
                Log.i("mainActivity", "openFiles interrupted: ${content}")
                return
            }
            val mainFragment = MainFragment.fragmentInstance!!
            mainFragment.dataState.reloadWithAnother(maybeBackupDataState)
            mainFragment.updateAll()
            Toast.makeText(this,  resources.getString(R.string.toast_backup_loaded), Toast.LENGTH_SHORT).show()
            Log.i("mainActivity", "openFiles finished: ${content}")
        } catch (e:Throwable){
            Toast.makeText(this,  resources.getString(R.string.toast_backup_loading_failed), Toast.LENGTH_SHORT).show()
        }
    }

    fun parseBackupContent(content: String): DataState? = try {
        deserializeDataStateFromString(content)
    } catch (e: Throwable){
        null
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
        useTotalSumSetting(sharedPreferences)
        useAvailableSumSetting(sharedPreferences)
        useNotPlannedSumSetting(sharedPreferences)
        //TODO add others
        Log.i("mainActivity", "usePreferencesValues: finished")
    }

    private fun useUpdatedPreferenceValue(sharedPreferences: SharedPreferences, key: String){
        Log.i("mainActivity", "useUpdatedPreferenceValue: started for key $key")
        when(key){
            resources.getString(R.string.settings_view_useDarkTheme_key)->useDarkThemeSetting(sharedPreferences)
            resources.getString(R.string.settings_fragment_main_useTotal_key)->useTotalSumSetting(sharedPreferences)
            resources.getString(R.string.settings_fragment_main_useAvailable_key)->useAvailableSumSetting(sharedPreferences)
            resources.getString(R.string.settings_fragment_main_useNotPlanned_key)->useNotPlannedSumSetting(sharedPreferences)
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

    private fun useTotalSumSetting(sharedPreferences: SharedPreferences){
        Log.i("mainActivity", "useTotalSumSetting: started")
        val useTotal = sharedPreferences.getBoolean(resources.getString(R.string.settings_fragment_main_useTotal_key), true)
        //HACK
        val totalTextView:View? = findViewById(R.id.textView_total)
        if(totalTextView!=null){
            totalTextView.visibility = getVisibilityInt(useTotal)
        }
        UIDataState.useTotalTextView = useTotal
        Log.i("mainActivity", "useTotalSumSetting: finished")
    }

    private fun useAvailableSumSetting(sharedPreferences: SharedPreferences){
        Log.i("mainActivity", "useAvailableSumSetting: started")
        val useAvailable = sharedPreferences.getBoolean(resources.getString(R.string.settings_fragment_main_useAvailable_key), true)
        //use anyway
        useNotPlannedSumSetting(sharedPreferences)

        //HACK
        val availableTextView:View? = findViewById(R.id.textView_available)
        if(availableTextView!=null){
            availableTextView.visibility = getVisibilityInt(useAvailable)
        }
        UIDataState.useAvailableTextView = useAvailable
        Log.i("mainActivity", "useAvailableSumSetting: finished")
    }

    /*Not planned works only if available sum is on*/
    private fun useNotPlannedSumSetting(sharedPreferences: SharedPreferences){
        Log.i("mainActivity", "useNotPlannedSumSetting: started")
        val useNotPlanned =
            sharedPreferences.getBoolean(resources.getString(R.string.settings_fragment_main_useAvailable_key), true) &&
            sharedPreferences.getBoolean(resources.getString(R.string.settings_fragment_main_useNotPlanned_key), true)
        //HACK
        val notPlannedTextView:View? = findViewById(R.id.textView_total)
        if(notPlannedTextView!=null){
            notPlannedTextView.visibility = getVisibilityInt(useNotPlanned)
        }
        UIDataState.useNotPlannedTextView = useNotPlanned
        Log.i("mainActivity", "useNotPlannedSumSetting: finished")
    }

}