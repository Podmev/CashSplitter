package com.podmev.cashsplitter

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import com.podmev.cashsplitter.databinding.ActivityMainBinding
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var categories = mutableListOf<CashCategory>()
    var plainGridData = mutableListOf<String>()
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.i("mainActivity", "onCreate: started")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, plainGridData)
            binding.gridCategories.adapter = adapter

            //for test now - put stub data
            updateWithNewCategories(createSimpleCategories())
            Log.i("mainActivity", "onCreate: finished")
        }catch (e:Throwable){
            Log.e("mainActivity", "onCreate failed: ${e.javaClass}, ${e.message}")
        }
    }

    private fun updateWithNewCategories(newCategories: List<CashCategory>){
        categories.apply {
            clear()
            addAll(newCategories)
        }
        updateAll()
    }

    /*updates view + file on disk*/
    private fun updateAll(){
        Log.i("mainActivity", "updateAll: started")
        updateView()
        dumpToFile()
        Log.i("mainActivity", "updateAll: finished")

    }

    /* updates grid with plain data and total sum*/
    private fun updateView(){
        Log.i("mainActivity", "updateView: started")
        //refresh
        Log.i("mainActivity", "updateView: started updating grid")
        plainGridData.apply {
            clear()
            addAll(toPlainGridView(categories))
        }
        //update grid
        adapter.notifyDataSetChanged()
        Log.i("mainActivity", "updateView: finished updating grid")

        //total view
        Log.i("mainActivity", "updateView: started updating textViewTotal")
        val curTotal = totalSumByCategories(categories)
        val totalText = String.format(resources.getString(R.string.textView_total_text), curTotal)
        binding.textViewTotal.text = totalText
        Log.i("mainActivity", "updateView: finished updating textViewTotal: $totalText")
        Log.i("mainActivity", "updateView: finished")
    }

    private fun dumpToFile(){
        Log.i("mainActivity", "dumpToFile: started")
        val filePath = resources.getString(R.string.dumpFilePath)
        //write to private dir
        val file = File(filesDir, filePath)
        if(!file.exists()){
            file.createNewFile()
        } else if(file.isDirectory){
            file.delete()
            file.createNewFile()
        }
        val content = categories.toLines()
        this.openFileOutput(filePath, Context.MODE_PRIVATE).write(content.toByteArray())
        Log.i("mainActivity", "dumpToFile: finished")
    }

    private fun uploadFromFile(){
        try {
            Log.i("mainActivity", "uploadFromFile: started")
            val filePath = resources.getString(R.string.dumpFilePath)
            //read from private dir
            val file = File(filesDir, filePath)
            if (!file.exists()) {
                file.createNewFile()
            } else if (file.isDirectory) {
                file.delete()
                file.createNewFile()
            }
            val content = file.readText()
            val newCategories = parseCashCategoriesFromLines(content)
            updateWithNewCategories(newCategories)
            Log.i("mainActivity", "uploadFromFile: finished")
        } catch (e:Exception){
            Log.e("mainActivity", "uploadFromFile: failed: ${e.javaClass}, ${e.message}")
            Toast.makeText(this, "Could upload dump file: ${e.message?.take(50)}", Toast.LENGTH_LONG).show()
        }
    }


}