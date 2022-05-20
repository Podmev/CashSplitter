package com.podmev.cashsplitter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.podmev.cashsplitter.data.*
import com.podmev.cashsplitter.databinding.ActivityMainBinding
import com.podmev.cashsplitter.dialogs.EditTextDialog
import com.podmev.cashsplitter.dialogs.NumberEditTextDialog
import com.podmev.cashsplitter.dialogs.SimpleDialog
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.i("mainActivity", "onCreate: started")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.toolbar)

            val navController = findNavController(R.id.nav_host_fragment_content_main)
            appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)

            Log.i("mainActivity", "onCreate: finished")
        } catch (e: Throwable) {
            Log.e("mainActivity", "onCreate failed: ${e.javaClass}, ${e.message}")
        }
    }

}