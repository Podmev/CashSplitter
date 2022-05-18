package com.podmev.cashsplitter.dialogs

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.util.TypedValue
import android.widget.EditText
import com.podmev.cashsplitter.R

class EditTextDialog(context: Context) : AlertDialog.Builder(context) {

    lateinit var onResponse: (r: ResponseType, text: String) -> Unit

    enum class ResponseType {
        YES, NO, CANCEL
    }

    fun show(
        title: String,
        initialText: String,
        hint: String,
        listener: (r: ResponseType, text: String) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val input = EditText(context)
        input.setText(initialText)
        input.setHint(hint)
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, context.resources.getDimension(R.dimen.dialog_text_size))
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setIcon(android.R.drawable.ic_dialog_info)
        onResponse = listener

        // performing positive action
        builder.setPositiveButton(R.string.dialog_button_positive) { _, _ ->
            onResponse(ResponseType.YES, input.text.toString())
        }

        // performing negative action
        builder.setNegativeButton(R.string.dialog_button_negative) { _, _ ->
            onResponse(ResponseType.NO, input.text.toString())
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
