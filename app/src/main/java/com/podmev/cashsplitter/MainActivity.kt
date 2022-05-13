package com.podmev.cashsplitter

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.podmev.cashsplitter.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var categories = mutableListOf<CashCategory>()
    var selectedCategoryPosition:Int = -1
    var plainGridData = mutableListOf<String>()
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.i("mainActivity", "onCreate: started")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            adapter = object:ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, plainGridData){
                override fun getView(position:Int, convertView: View?, parent: ViewGroup):View{
                    val view = super.getView(position, convertView, parent)
                    var color = Color.TRANSPARENT
                    val row = selectedCategoryPosition
                    val numColumns = binding.gridCategories.numColumns
                    if(position in row*numColumns until (row+1)*numColumns){
                        //in selected row
                        color = Color.YELLOW
                    }
                    view.setBackgroundColor(color)
                    return view
                }

            }
            binding.gridCategories.adapter = adapter
            binding.gridCategories.setOnItemClickListener{
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long->
                        updateSelectedCategoryPosition(position)
                        adapter.notifyDataSetChanged()
            }
            //first line of buttons
            binding.buttonPlus.setOnClickListener{plusAction()}
            binding.buttonMinus.setOnClickListener{minusAction()}
            binding.buttonClear.setOnClickListener{clearAction()}
            //second line of buttons
            binding.buttonDown.setOnClickListener{downAction()}
            binding.buttonUp.setOnClickListener{upAction()}
            binding.buttonEdit.setOnClickListener{editAction()}
            binding.buttonCreate.setOnClickListener{createAction()}
            binding.buttonDelete.setOnClickListener{deleteAction()}

            uploadFromFileOrDefault()
            //for test now - put stub data
            //updateWithNewCategories(createSimpleCategories())

            //end of testing
            Log.i("mainActivity", "onCreate: finished")
        }catch (e:Throwable){
            Log.e("mainActivity", "onCreate failed: ${e.javaClass}, ${e.message}")
        }
    }

    private fun showNeedToSelectRow(){
        Toast.makeText(this, R.string.toast_need_to_select_category, Toast.LENGTH_SHORT).show()
    }

    private fun showDontNeedToSelectRow(){
        Toast.makeText(this, R.string.toast_dont_need_to_select_category, Toast.LENGTH_SHORT).show()
    }

    private fun updateSelectedCategoryPosition(plainPosition: Int){
        val rowPos = plainPosition / binding.gridCategories.numColumns
        if(selectedCategoryPosition==rowPos){
            selectedCategoryPosition = -1
        } else{
          selectedCategoryPosition = rowPos
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

    fun uploadFromFileOrDefault(){
        uploadFromFile()
        if(categories.isEmpty()) {
            categories.add(CashCategory("Other", 0.0, false, false))
            updateAll()
        }
    }

    //actions
    private fun plusAction(){
        if(selectedCategoryPosition == -1){
            showNeedToSelectRow()
            return
        }
        val cashCategory = categories[selectedCategoryPosition]
        val categoryName = cashCategory.name
        val categorySum = cashCategory.sum

        val dialog = NumberEditTextDialog(this)
        val context = this
        dialog.show(
            String.format(resources.getString(R.string.dialog_plus_category_title), categoryName, categorySum),
            resources.getString(R.string.dialog_plus_category_hint)
        ){ responseType, number ->
            when(responseType){
                NumberEditTextDialog.ResponseType.YES -> {
                    if(number<0){
                        Toast.makeText(context, resources.getString(R.string.toast_plus_negative_forbidden), Toast.LENGTH_SHORT).show()
                        //unselect position
                        selectedCategoryPosition = -1
                        updateAll()
                        return@show
                    }
                    cashCategory.sum+= number
                    selectedCategoryPosition = -1
                    updateAll()
                }
                NumberEditTextDialog.ResponseType.NO ->{}
                NumberEditTextDialog.ResponseType.CANCEL->{}
            }
        }
    }

    private fun minusAction(){
        if(selectedCategoryPosition == -1){
            showNeedToSelectRow()
            return
        }
        val cashCategory = categories[selectedCategoryPosition]
        val categoryName = cashCategory.name
        val categorySum = cashCategory.sum

        val dialog = NumberEditTextDialog(this)
        val context = this
        dialog.show(
            String.format(resources.getString(R.string.dialog_minus_category_title), categoryName, categorySum),
            String.format(resources.getString(R.string.dialog_minus_category_hint), categorySum)
        ){ responseType, number ->
            when(responseType){
                NumberEditTextDialog.ResponseType.YES -> {
                    if(number<0){
                        Toast.makeText(context, resources.getString(R.string.toast_minus_negative_forbidden), Toast.LENGTH_SHORT).show()
                        //unselect position
                        selectedCategoryPosition = -1
                        updateAll()
                        return@show
                    }
                    if(number > categorySum){
                        Toast.makeText(context, resources.getString(R.string.toast_minus_too_much_forbidden), Toast.LENGTH_SHORT).show()
                        //unselect position
                        selectedCategoryPosition = -1
                        updateAll()
                        return@show
                    }
                    cashCategory.sum-= number
                    selectedCategoryPosition = -1
                    updateAll()
                }
                NumberEditTextDialog.ResponseType.NO ->{}
                NumberEditTextDialog.ResponseType.CANCEL->{}
            }
        }
    }

    private fun clearAction(){
        if(selectedCategoryPosition == -1){
            showNeedToSelectRow()
            return
        }
        val cashCategory = categories[selectedCategoryPosition]
        val categoryName = cashCategory.name

        val dialog = SimpleDialog(this)
        dialog.show(
            resources.getString(R.string.dialog_clear_category_title),
            String.format(resources.getString(R.string.dialog_clear_category_message), categoryName)
        ){
            when(it){
                SimpleDialog.ResponseType.YES -> {
                    //clear here the sum
                    cashCategory.sum = 0.0
                    //unselect position
                    selectedCategoryPosition = -1
                    updateAll()
                }
                SimpleDialog.ResponseType.NO ->{}
                SimpleDialog.ResponseType.CANCEL ->{}
            }
        }
    }

    private fun downAction(){
        if(selectedCategoryPosition == -1){
            showNeedToSelectRow()
            return
        }
        if(selectedCategoryPosition==categories.lastIndex){
            Toast.makeText(this, R.string.toast_cannot_go_down, Toast.LENGTH_SHORT).show()
            return
        }
        val curCategory = categories[selectedCategoryPosition]
        val nextCategory = categories[selectedCategoryPosition+1]
        categories.set(selectedCategoryPosition, nextCategory)
        categories.set(selectedCategoryPosition+1, curCategory)
        selectedCategoryPosition++
        updateAll()
    }

    private fun upAction(){
        if(selectedCategoryPosition == -1){
            showNeedToSelectRow()
            return
        }
        if(selectedCategoryPosition==0){
            Toast.makeText(this, R.string.toast_cannot_go_up, Toast.LENGTH_SHORT).show()
            return
        }
        val curCategory = categories[selectedCategoryPosition]
        val prevCategory = categories[selectedCategoryPosition-1]
        categories.set(selectedCategoryPosition, prevCategory)
        categories.set(selectedCategoryPosition-1, curCategory)
        selectedCategoryPosition--
        updateAll()
    }
    private fun editAction(){
        if(selectedCategoryPosition == -1){
            showNeedToSelectRow()
            return
        }
        val cashCategory = categories[selectedCategoryPosition]
        val categoryName = cashCategory.name
        val categorySum = cashCategory.sum

        val dialog = EditTextDialog(this)
        val context = this
        dialog.show(
            String.format(resources.getString(R.string.dialog_edit_category_title), categoryName, categorySum),
            categoryName,
            String.format(resources.getString(R.string.dialog_edit_category_hint), categoryName)
        ){ responseType, text ->
            when(responseType){
                EditTextDialog.ResponseType.YES -> {
                    if(text==categoryName){
                        Toast.makeText(context, String.format(resources.getString(R.string.toast_edit_no_changes), text), Toast.LENGTH_SHORT).show()
                        //unselect position
                        selectedCategoryPosition = -1
                        updateAll()
                        return@show
                    }
                    val foundExistedCategory = categories.find{it.name==text}
                    if(foundExistedCategory!=null){
                        Toast.makeText(context, String.format(resources.getString(R.string.toast_edit_collision), text), Toast.LENGTH_SHORT).show()
                        //unselect position
                        selectedCategoryPosition = -1
                        updateAll()
                        return@show
                    }
                    cashCategory.name = text
                    selectedCategoryPosition = -1
                    updateAll()
                }
                EditTextDialog.ResponseType.NO ->{}
                EditTextDialog.ResponseType.CANCEL ->{}
            }
        }
    }

    private fun createAction(){
        //Single opposite check - shouldn't be selected
        if(selectedCategoryPosition != -1){
            showDontNeedToSelectRow()
            return
        }

        val dialog = EditTextDialog(this)
        val context = this
        dialog.show(
            resources.getString(R.string.dialog_create_category_title),
            "",
            resources.getString(R.string.dialog_create_category_hint)
        ){ responseType, text ->
            when(responseType){
                EditTextDialog.ResponseType.YES -> {
                    val foundExistedCategory = categories.find{it.name==text}
                    if(foundExistedCategory!=null){
                        Toast.makeText(context, String.format(resources.getString(R.string.toast_create_collision), text), Toast.LENGTH_SHORT).show()
                        return@show
                    }
                    val cashCategory = CashCategory(text, 0.0, true, true)
                    categories.add(cashCategory)
                    updateAll()
                }
                EditTextDialog.ResponseType.NO ->{}
                EditTextDialog.ResponseType.CANCEL ->{}
            }
        }
    }

    private fun deleteAction(){
        if(selectedCategoryPosition == -1){
            showNeedToSelectRow()
            return
        }
        val cashCategory = categories[selectedCategoryPosition]
        val categoryName = cashCategory.name

        if(!cashCategory.canBeDeleted){
            Toast.makeText(this,
                String.format(resources.getString(R.string.toast_cannot_delete_category,categoryName)),
                Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = SimpleDialog(this)
        dialog.show(
            resources.getString(R.string.dialog_delete_category_title),
            String.format(resources.getString(R.string.dialog_delete_category_message), categoryName)
        ){
            when(it){
                SimpleDialog.ResponseType.YES -> {
                    categories.removeAt(selectedCategoryPosition)
                    //unselect position
                    selectedCategoryPosition = -1
                    updateAll()
                }
                SimpleDialog.ResponseType.NO ->{}
                SimpleDialog.ResponseType.CANCEL ->{}
            }
        }
    }


}