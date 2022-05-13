package com.podmev.cashsplitter

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.podmev.cashsplitter.databinding.ActivityMainBinding

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
            categories.apply {
                clear()
                addAll(createSimpleCategories())
            }
            updateAll()
            Log.i("mainActivity", "onCreate: finished")
        }catch (e:Throwable){
            Log.e("mainActivity", "onCreate failed: ${e.javaClass}, ${e.message}")
        }
    }

    /*updates view + file on disk*/
    fun updateAll(){
        Log.i("mainActivity", "updateAll: started")
        updateView()
        dumpToFile()
        Log.i("mainActivity", "updateAll: finished")

    }

    /* updates grid with plain data and total sum*/
    fun updateView(){
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

    fun dumpToFile(){
        //TODO
        Log.i("mainActivity", "dumpToFile: started")
        Log.i("mainActivity", "dumpToFile: finished")
    }

    fun uploadFromFile(){
         //TODO
        Log.i("mainActivity", "uploadFromFile: started")
        Log.i("mainActivity", "uploadFromFile: finished")
    }


}