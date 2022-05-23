package com.podmev.cashsplitter.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.podmev.cashsplitter.R
import com.podmev.cashsplitter.data.*
import com.podmev.cashsplitter.databinding.FragmentMainBinding
import com.podmev.cashsplitter.dialog.EditTextDialog
import com.podmev.cashsplitter.dialog.NumberEditTextDialog
import com.podmev.cashsplitter.dialog.SimpleDialog
import java.io.File

class MainFragment : Fragment() {
    //HACK
    companion object {
        private const val logTag = "mainFragment"

        var fragmentInstance: MainFragment? = null
    }

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /*main state of app - persisting one*/
    var dataState = createEmptyDataState()

    var plainGridData = mutableListOf<String>()
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            _binding = FragmentMainBinding.inflate(inflater, container, false)

            adapter = object :
                ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    plainGridData
                ) {
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
            binding.buttonErase.setOnClickListener { eraseAction(dataState) }
            //text view actions
            binding.textViewAvailable.setOnClickListener { setAvailableAction(dataState) }

            uploadFromFileOrDefault(dataState)

            //HACK - remove it later
            fragmentInstance = this

            Log.i(logTag, "onCreateView: finished")
            return binding.root
        } catch (e: Throwable) {
            Log.e(logTag, "onCreateView failed: ${e.javaClass}, ${e.message}")
            e.printStackTrace()
            return inflater.inflate(R.layout.fragment_main, container, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentInstance = null
        _binding = null
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

        var backgroundColor = R.color.grid_base_row_background
        var textColor = R.color.grid_base_row_text
        if (curSelected) {
            //in selected row
            backgroundColor = R.color.grid_selected_row_background
            textColor = R.color.grid_selected_row_text
            //locked and selected row is yellow anyway
        } else {
            if (curLocked) {
                //in locked row
                backgroundColor = R.color.grid_locked_row_background
                textColor = R.color.grid_locked_row_text
            }
        }
        view as TextView
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColor))
        view.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getDimension(R.dimen.grid_text_size))
    }

    fun changeNotPlannedView(state: DataState) {
        val backgroundColor: Int
        val textColor: Int

        if (state.hasNotPlannedAlert()) {
            backgroundColor = R.color.textView_alert_background
            textColor = R.color.textView_alert_text
        } else {
            backgroundColor = R.color.textView_base_background
            textColor = R.color.textView_base_text
        }
        val textView = binding.textViewNotPlanned
        textView.setBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColor))
        textView.setTextColor(ContextCompat.getColor(requireContext(), textColor))
    }

    private fun showNeedToSelectRow() {
        Toast.makeText(requireContext(), R.string.toast_need_to_select_category, Toast.LENGTH_SHORT)
            .show()
    }

    private fun showDontNeedToSelectRow() {
        Toast.makeText(
            requireContext(),
            R.string.toast_dont_need_to_select_category,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showCannotChangeLockedCategory() {
        Toast.makeText(
            requireContext(),
            R.string.toast_cannot_change_locked_category,
            Toast.LENGTH_SHORT
        ).show()
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
    fun updateAll() {
        Log.i(logTag, "updateAll: started")
        updateView(dataState)
        dumpToFile(dataState)
        Log.i(logTag, "updateAll: finished")

    }

    /* updates grid with plain data and total sum*/
    private fun updateView(state: DataState) {
        Log.i(logTag, "updateView: started")
        //refresh
        Log.i(logTag, "updateView: started updating grid")
        plainGridData.apply {
            clear()
            addAll(toPlainGridView(state.categories))
        }
        //update grid
        adapter.notifyDataSetChanged()
        Log.i(logTag, "updateView: finished updating grid")

        //total view
        Log.i(logTag, "updateView: started updating textViewTotal")
        val totalText =
            String.format(resources.getString(R.string.textView_total_text), state.calcTotalSum())
        binding.textViewTotal.text = totalText
        Log.i(logTag, "updateView: finished updating textViewTotal: $totalText")

        Log.i(logTag, "updateView: started updating textViewAvailable")
        val availableText =
            String.format(resources.getString(R.string.textView_available_text), state.availableSum)
        binding.textViewAvailable.text = availableText
        Log.i(logTag, "updateView: finished updating textViewAvailable: $availableText")

        Log.i(logTag, "updateView: started updating textViewNotPlanned")
        val notPlannedText = String.format(
            resources.getString(R.string.textView_not_planned_text),
            state.calcNotPlannedSum()
        )
        binding.textViewNotPlanned.text = notPlannedText
        changeNotPlannedView(state)
        Log.i(logTag, "updateView: finished updating textViewNotPlanned: $notPlannedText")

        Log.i(logTag, "updateView: finished")
    }

    private fun dumpToFile(state: DataState) {
        try {
            Log.i(logTag, "dumpToFile: started")
            val filePath = resources.getString(R.string.dumpFilePath)
            //write to private dir
            val file = File(requireContext().filesDir, filePath)
            saveToChosenFile(state, file)
            Log.i(logTag, "dumpToFile: finished")
        } catch (e: Exception) {
            Log.e(logTag, "dumpToFile: failed: ${e.javaClass}, ${e.message}")
            Toast.makeText(
                requireContext(),
                String.format(
                    resources.getString(R.string.toast_dump_file_save_fail),
                    e.message?.take(50)
                ),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveToChosenFile(state: DataState, file: File) {
        if (!file.exists()) {
            file.createNewFile()
        } else if (file.isDirectory) {
            file.delete()
            file.createNewFile()
        }
        val content = state.serialize()
        requireContext().openFileOutput(file.name, Context.MODE_PRIVATE)
            .write(content.toByteArray())
        Log.i(logTag, "saveToFile: finished")
    }

    private fun loadFromFile(state: DataState) {
        try {
            Log.i(logTag, "loadFromFile: started")
            val filePath = resources.getString(R.string.dumpFilePath)
            //read from private dir
            val file = File(requireContext().filesDir, filePath)
            loadFromChosenFile(state, file)
            Log.i(logTag, "loadFromFile: finished")
        } catch (e: Exception) {
            Log.e(logTag, "loadFromFile: failed: ${e.javaClass}, ${e.message}")
            Toast.makeText(
                requireContext(),
                String.format(
                    resources.getString(R.string.toast_dump_file_load_fail),
                    e.message?.take(50)
                ),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadFromChosenFile(state: DataState, file: File) {
        if (!file.exists()) {
            file.createNewFile()
        } else if (file.isDirectory) {
            file.delete()
            file.createNewFile()
        } else {
            val content = file.readText()
            val recoveredState = deserializeDataStateFromString(content)
            state.reloadWithAnother(recoveredState)
        }
        updateAll()
        Log.i(logTag, "loadFromChosenFile: finished")
    }

    private fun uploadFromFileOrDefault(state: DataState) {
        loadFromFile(state)
        //TODO make smarter check
        if (state.categories.isEmpty()) {
            state.categories.addAll(createDefaultCategories())
            updateAll()
        }
    }

    private fun createDefaultCategories(): List<CashCategory> {
        val defaultCategoryName = resources.getString(R.string.category_default_name)
        return listOf(CashCategory(defaultCategoryName, 0.0, false, false, false))
    }

    //actions

    private fun setAvailableAction(state: DataState) {
        val dialog = NumberEditTextDialog(requireContext())
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
        if (cashCategory.locked) {
            showCannotChangeLockedCategory()
            return
        }
        val categoryName = cashCategory.name

        val dialog = NumberEditTextDialog(requireContext())
        val context = requireContext()
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
        if (cashCategory.locked) {
            showCannotChangeLockedCategory()
            return
        }
        val categoryName = cashCategory.name
        val categorySum = cashCategory.sum

        val dialog = NumberEditTextDialog(requireContext())
        val context = requireContext()
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
        if (cashCategory.locked) {
            showCannotChangeLockedCategory()
            return
        }
        val categoryName = cashCategory.name

        val dialog = SimpleDialog(requireContext())
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
            val dialog = SimpleDialog(requireContext())
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
            val dialog = SimpleDialog(requireContext())
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
            Toast.makeText(requireContext(), R.string.toast_cannot_go_down, Toast.LENGTH_SHORT)
                .show()
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
            Toast.makeText(requireContext(), R.string.toast_cannot_go_up, Toast.LENGTH_SHORT).show()
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
        if (cashCategory.locked) {
            showCannotChangeLockedCategory()
            return
        }
        val categoryName = cashCategory.name

        val dialog = EditTextDialog(requireContext())
        val context = requireContext()
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

        val dialog = EditTextDialog(requireContext())
        val context = requireContext()
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
        if (cashCategory.locked) {
            showCannotChangeLockedCategory()
            return
        }
        val categoryName = cashCategory.name

        if (!cashCategory.canBeDeleted) {
            Toast.makeText(
                requireContext(),
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
        val dialog = SimpleDialog(requireContext())
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

    /*to erase all data we need to type password in each language different*/
    private fun eraseAction(state: DataState) {
        for (cashCategory in state.categories) {
            if (cashCategory.locked) {
                showCannotChangeLockedCategory()
                return
            }
        }
        val dialog = EditTextDialog(requireContext())
        val context = requireContext()
        dialog.show(
            resources.getString(R.string.dialog_erase_category_title),
            "",
            String.format(
                resources.getString(R.string.dialog_erase_category_hint),
                resources.getString(R.string.dialog_erase_category_password)
            )
        ) { responseType, text ->
            when (responseType) {
                EditTextDialog.ResponseType.YES -> {
                    if (text == resources.getString(R.string.dialog_erase_category_password)) {
                        //password is correct - we can erase data
                        state.erase(createDefaultCategories())
                        Toast.makeText(
                            context,
                            String.format(
                                resources.getString(R.string.toast_erased_successfully),
                                text
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        updateAll()
                        return@show
                    } else {
                        Toast.makeText(
                            context,
                            String.format(
                                resources.getString(R.string.toast_didnt_erase),
                                text
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@show
                    }
                }
                EditTextDialog.ResponseType.NO -> {}
                EditTextDialog.ResponseType.CANCEL -> {}
            }
        }
    }


}