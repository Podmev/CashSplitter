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
import com.podmev.cashsplitter.data.*
import com.podmev.cashsplitter.databinding.ActivityMainBinding
import com.podmev.cashsplitter.dialogs.EditTextDialog
import com.podmev.cashsplitter.dialogs.NumberEditTextDialog
import com.podmev.cashsplitter.dialogs.SimpleDialog
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /*main state of app - persisting one*/
    var dataState = createEmptyDataState()

    var plainGridData = mutableListOf<String>()
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.i("mainActivity", "onCreate: started")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            adapter = object :
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, plainGridData) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    changeGridCellView(view, dataState, position)
                    return view
                }

            }
            binding.gridCategories.adapter = adapter
            binding.gridCategories.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                updateSelectedCategoryPosition(dataState, position)
                adapter.notifyDataSetChanged()
            }
            binding.buttonPlus.setOnClickListener { plusAction(dataState) }
            binding.buttonMinus.setOnClickListener { minusAction(dataState) }
            binding.buttonClear.setOnClickListener { clearAction(dataState) }
            binding.buttonLock.setOnClickListener { lockAction(dataState) }
            binding.buttonCreate.setOnClickListener { createAction(dataState) }
            binding.buttonDown.setOnClickListener { downAction(dataState) }
            binding.buttonUp.setOnClickListener { upAction(dataState) }
            binding.buttonEdit.setOnClickListener { editAction(dataState) }
            binding.buttonDelete.setOnClickListener { deleteAction(dataState) }
            //TODO add erase method
            //text view actions
            binding.textViewAvailable.setOnClickListener { setAvailableAction(dataState) }

            uploadFromFileOrDefault(dataState)
            //for test now - put stub data
            //updateWithNewCategories(createSimpleCategories())

            //end of testing
            Log.i("mainActivity", "onCreate: finished")
        } catch (e: Throwable) {
            Log.e("mainActivity", "onCreate failed: ${e.javaClass}, ${e.message}")
        }
    }

    /*politics of changing colors of grid
    * yellow for selected
    * blue for locked
    * */
    fun changeGridCellView(view: View, state: DataState, position: Int) {

        val selectedRow = state.selectedCategoryPosition
        val numColumns = binding.gridCategories.numColumns
        val curRow = position / numColumns
        val curCategory: CashCategory = state.categories[curRow]
        val curLocked = curCategory.locked
        val curSelected = selectedRow == curRow

        var color = Color.TRANSPARENT
        if (curSelected) {
            //in selected row
            color = Color.YELLOW
            //locked and selected row is yellow anyway
        } else {
            if (curLocked) {
                //in locked row
                color = Color.GRAY
            }
        }
        view.setBackgroundColor(color)
    }

    private fun showNeedToSelectRow() {
        Toast.makeText(this, R.string.toast_need_to_select_category, Toast.LENGTH_SHORT).show()
    }

    private fun showDontNeedToSelectRow() {
        Toast.makeText(this, R.string.toast_dont_need_to_select_category, Toast.LENGTH_SHORT).show()
    }

    private fun updateSelectedCategoryPosition(state: DataState, plainPosition: Int) {
        val rowPos = plainPosition / binding.gridCategories.numColumns
        if (state.selectedCategoryPosition == rowPos) {
            state.unselectCategory()
        } else {
            state.selectedCategoryPosition = rowPos
        }
    }

    /*updates view + file on disk*/
    private fun updateAll() {
        Log.i("mainActivity", "updateAll: started")
        updateView(dataState)
        dumpToFile(dataState)
        Log.i("mainActivity", "updateAll: finished")

    }

    /* updates grid with plain data and total sum*/
    private fun updateView(state: DataState) {
        Log.i("mainActivity", "updateView: started")
        //refresh
        Log.i("mainActivity", "updateView: started updating grid")
        plainGridData.apply {
            clear()
            addAll(toPlainGridView(state.categories))
        }
        //update grid
        adapter.notifyDataSetChanged()
        Log.i("mainActivity", "updateView: finished updating grid")

        //total view
        Log.i("mainActivity", "updateView: started updating textViewTotal")
        val totalText =
            String.format(resources.getString(R.string.textView_total_text), state.calcTotalSum())
        binding.textViewTotal.text = totalText
        Log.i("mainActivity", "updateView: finished updating textViewTotal: $totalText")

        Log.i("mainActivity", "updateView: started updating textViewAvailable")
        val availableText =
            String.format(resources.getString(R.string.textView_available_text), state.availableSum)
        binding.textViewAvailable.text = availableText
        Log.i("mainActivity", "updateView: finished updating textViewAvailable: $availableText")

        Log.i("mainActivity", "updateView: started updating textViewNotPlanned")
        val notPlannedText = String.format(
            resources.getString(R.string.textView_not_planned_text),
            state.calcNotPlannedSum()
        )
        binding.textViewNotPlanned.text = notPlannedText
        Log.i("mainActivity", "updateView: finished updating textViewNotPlanned: $notPlannedText")

        Log.i("mainActivity", "updateView: finished")
    }

    private fun dumpToFile(state: DataState) {
        Log.i("mainActivity", "dumpToFile: started")
        val filePath = resources.getString(R.string.dumpFilePath)
        //write to private dir
        val file = File(filesDir, filePath)
        if (!file.exists()) {
            file.createNewFile()
        } else if (file.isDirectory) {
            file.delete()
            file.createNewFile()
        }
        val content = state.serialize()
        this.openFileOutput(filePath, Context.MODE_PRIVATE).write(content.toByteArray())
        Log.i("mainActivity", "dumpToFile: finished")
    }

    private fun uploadFromFile(state: DataState) {
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
            } else {
                val content = file.readText()
                val recoveredState = deserializeCashCategoriesFromString(content)
                state.reloadWithAnother(recoveredState)
            }
            updateAll()
            Log.i("mainActivity", "uploadFromFile: finished")
        } catch (e: Exception) {
            Log.e("mainActivity", "uploadFromFile: failed: ${e.javaClass}, ${e.message}")
            Toast.makeText(
                this,
                "Could upload dump file: ${e.message?.take(50)}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun uploadFromFileOrDefault(state: DataState) {
        uploadFromFile(state)
        if (state.categories.isEmpty()) {
            val defaultCategoryName = resources.getString(R.string.category_default_name)
            state.categories.add(CashCategory(defaultCategoryName, 0.0, false, false, false))
            updateAll()
        }
    }

    //actions

    private fun setAvailableAction(state: DataState) {
        val dialog = NumberEditTextDialog(this)
        dialog.show(
            String.format(
                resources.getString(R.string.dialog_edit_available_sum_title),
                state.availableSum
            ),
            resources.getString(R.string.dialog_edit_available_sum_hint)
        ) { responseType, number ->
            when (responseType) {
                NumberEditTextDialog.ResponseType.YES -> {
                    //value can be negative
                    state.availableSum = number
                    updateAll()
                }
                NumberEditTextDialog.ResponseType.NO -> {}
                NumberEditTextDialog.ResponseType.CANCEL -> {}
            }
        }
    }

    private fun plusAction(state: DataState) {
        if (!state.isSelectedCategory()) {
            showNeedToSelectRow()
            return
        }
        val cashCategory = state.curCategory()
        val categoryName = cashCategory.name

        val dialog = NumberEditTextDialog(this)
        val context = this
        dialog.show(
            String.format(resources.getString(R.string.dialog_plus_category_title), categoryName),
            resources.getString(R.string.dialog_plus_category_hint)
        ) { responseType, number ->
            when (responseType) {
                NumberEditTextDialog.ResponseType.YES -> {
                    if (number < 0) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.toast_plus_negative_forbidden),
                            Toast.LENGTH_SHORT
                        ).show()
                        //unselect position
                        state.unselectCategory()
                        updateAll()
                        return@show
                    }
                    cashCategory.sum += number
                    state.unselectCategory()
                    updateAll()
                }
                NumberEditTextDialog.ResponseType.NO -> {}
                NumberEditTextDialog.ResponseType.CANCEL -> {}
            }
        }
    }

    private fun minusAction(state: DataState) {
        if (!state.isSelectedCategory()) {
            showNeedToSelectRow()
            return
        }
        val cashCategory = state.curCategory()
        val categoryName = cashCategory.name
        val categorySum = cashCategory.sum

        val dialog = NumberEditTextDialog(this)
        val context = this
        dialog.show(
            String.format(resources.getString(R.string.dialog_minus_category_title), categoryName),
            String.format(resources.getString(R.string.dialog_minus_category_hint), categorySum)
        ) { responseType, number ->
            when (responseType) {
                NumberEditTextDialog.ResponseType.YES -> {
                    if (number < 0) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.toast_minus_negative_forbidden),
                            Toast.LENGTH_SHORT
                        ).show()
                        //unselect position
                        state.unselectCategory()
                        updateAll()
                        return@show
                    }
                    if (number > categorySum) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.toast_minus_too_much_forbidden),
                            Toast.LENGTH_SHORT
                        ).show()
                        //unselect position
                        state.unselectCategory()
                        updateAll()
                        return@show
                    }
                    cashCategory.sum -= number
                    state.unselectCategory()
                    updateAll()
                }
                NumberEditTextDialog.ResponseType.NO -> {}
                NumberEditTextDialog.ResponseType.CANCEL -> {}
            }
        }
    }

    private fun clearAction(state: DataState) {
        if (!state.isSelectedCategory()) {
            showNeedToSelectRow()
            return
        }
        val cashCategory = state.curCategory()
        val categoryName = cashCategory.name

        val dialog = SimpleDialog(this)
        dialog.show(
            resources.getString(R.string.dialog_clear_category_title),
            String.format(resources.getString(R.string.dialog_clear_category_message), categoryName)
        ) {
            when (it) {
                SimpleDialog.ResponseType.YES -> {
                    //clear here the sum
                    cashCategory.sum = 0.0
                    //unselect position
                    state.unselectCategory()
                    updateAll()
                }
                SimpleDialog.ResponseType.NO -> {}
                SimpleDialog.ResponseType.CANCEL -> {}
            }
        }
    }


    private fun lockAction(state: DataState) {
        if (!state.isSelectedCategory()) {
            showNeedToSelectRow()
            return
        }
        val cashCategory = state.curCategory()
        val categoryName = cashCategory.name
        if (cashCategory.locked) {
            //locked -> unlock
            val dialog = SimpleDialog(this)
            dialog.show(
                resources.getString(R.string.dialog_unlock_category_title),
                String.format(
                    resources.getString(R.string.dialog_unlock_category_message),
                    categoryName
                )
            ) {
                when (it) {
                    SimpleDialog.ResponseType.YES -> {
                        //unlock category
                        cashCategory.locked = false
                        //unselect position
                        state.unselectCategory()
                        updateAll()
                    }
                    SimpleDialog.ResponseType.NO -> {}
                    SimpleDialog.ResponseType.CANCEL -> {}
                }
            }
        } else {
            //unlocked -> lock
            val dialog = SimpleDialog(this)
            dialog.show(
                resources.getString(R.string.dialog_lock_category_title),
                String.format(
                    resources.getString(R.string.dialog_lock_category_message),
                    categoryName
                )
            ) {
                when (it) {
                    SimpleDialog.ResponseType.YES -> {
                        //unlock category
                        cashCategory.locked = true
                        //unselect position
                        state.unselectCategory()
                        updateAll()
                    }
                    SimpleDialog.ResponseType.NO -> {}
                    SimpleDialog.ResponseType.CANCEL -> {}
                }
            }
        }
    }

    private fun downAction(state: DataState) {
        if (!state.isSelectedCategory()) {
            showNeedToSelectRow()
            return
        }
        if (state.selectedCategoryPosition == state.categories.lastIndex) {
            Toast.makeText(this, R.string.toast_cannot_go_down, Toast.LENGTH_SHORT).show()
            return
        }
        state.moveSelectedCategoryDown()
        updateAll()
    }

    private fun upAction(state: DataState) {
        if (!state.isSelectedCategory()) {
            showNeedToSelectRow()
            return
        }
        if (state.selectedCategoryPosition == 0) {
            Toast.makeText(this, R.string.toast_cannot_go_up, Toast.LENGTH_SHORT).show()
            return
        }
        state.moveSelectedCategoryUp()
        updateAll()
    }

    private fun editAction(state: DataState) {
        if (!state.isSelectedCategory()) {
            showNeedToSelectRow()
            return
        }
        val cashCategory = state.curCategory()
        val categoryName = cashCategory.name

        val dialog = EditTextDialog(this)
        val context = this
        dialog.show(
            String.format(resources.getString(R.string.dialog_edit_category_title), categoryName),
            categoryName,
            resources.getString(R.string.dialog_edit_category_hint)
        ) { responseType, text ->
            when (responseType) {
                EditTextDialog.ResponseType.YES -> {
                    if (text == categoryName) {
                        Toast.makeText(
                            context,
                            String.format(
                                resources.getString(R.string.toast_edit_no_changes),
                                text
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        //unselect position
                        state.unselectCategory()
                        updateAll()
                        return@show
                    }
                    val foundExistedCategory = state.categories.find { it.name == text }
                    if (foundExistedCategory != null) {
                        Toast.makeText(
                            context,
                            String.format(resources.getString(R.string.toast_edit_collision), text),
                            Toast.LENGTH_SHORT
                        ).show()
                        //unselect position
                        state.unselectCategory()
                        updateAll()
                        return@show
                    }
                    cashCategory.name = text
                    state.unselectCategory()
                    updateAll()
                }
                EditTextDialog.ResponseType.NO -> {}
                EditTextDialog.ResponseType.CANCEL -> {}
            }
        }
    }

    private fun createAction(state: DataState) {
        //Single opposite check - shouldn't be selected
        if (state.isSelectedCategory()) {
            showDontNeedToSelectRow()
            return
        }

        val dialog = EditTextDialog(this)
        val context = this
        dialog.show(
            resources.getString(R.string.dialog_create_category_title),
            "",
            resources.getString(R.string.dialog_create_category_hint)
        ) { responseType, text ->
            when (responseType) {
                EditTextDialog.ResponseType.YES -> {
                    val foundExistedCategory = state.categories.find { it.name == text }
                    if (foundExistedCategory != null) {
                        Toast.makeText(
                            context,
                            String.format(
                                resources.getString(R.string.toast_create_collision),
                                text
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@show
                    }
                    val cashCategory = CashCategory(text, 0.0, true, true, false)
                    state.categories.add(cashCategory)
                    updateAll()
                }
                EditTextDialog.ResponseType.NO -> {}
                EditTextDialog.ResponseType.CANCEL -> {}
            }
        }
    }

    private fun deleteAction(state: DataState) {
        if (!state.isSelectedCategory()) {
            showNeedToSelectRow()
            return
        }
        val cashCategory = state.curCategory()
        val categoryName = cashCategory.name

        if (!cashCategory.canBeDeleted) {
            Toast.makeText(
                this,
                String.format(
                    resources.getString(
                        R.string.toast_cannot_delete_category,
                        categoryName
                    )
                ),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val dialog = SimpleDialog(this)
        dialog.show(
            resources.getString(R.string.dialog_delete_category_title),
            String.format(
                resources.getString(R.string.dialog_delete_category_message),
                categoryName
            )
        ) {
            when (it) {
                SimpleDialog.ResponseType.YES -> {
                    state.categories.removeAt(state.selectedCategoryPosition)
                    //unselect position
                    state.unselectCategory()
                    updateAll()
                }
                SimpleDialog.ResponseType.NO -> {}
                SimpleDialog.ResponseType.CANCEL -> {}
            }
        }
    }


}