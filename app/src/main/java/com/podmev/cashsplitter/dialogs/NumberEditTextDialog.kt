package com.podmev.cashsplitter.dialogs

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.EditText
import com.podmev.cashsplitter.R

class NumberEditTextDialog(context: Context) : AlertDialog.Builder(context) {

    lateinit var onResponse: (r : ResponseType, number: Double) -> Unit

    enum class ResponseType {
        YES, NO, CANCEL
    }

    fun show(title: String, hint: String, listener: (r : ResponseType, number: Double) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val input = EditText(context)
        input.setHint(hint)
        input.inputType = InputType.TYPE_CLASS_NUMBER //TODO cannot be decimal
        builder.setView(input)
        builder.setIcon(android.R.drawable.ic_dialog_info)
        onResponse = listener

        // performing positive action
        builder.setPositiveButton(R.string.dialog_button_positive) { _, _ ->
            val text = input.text.toString()
            val num = if(text == "") 0.0 else text.toDouble()
            onResponse(ResponseType.YES, num)
        }

        // performing negative action
        builder.setNegativeButton(R.string.dialog_button_negative) { _, _ ->
            val text = input.text.toString()
            val num = if(text == "") 0.0 else text.toDouble()
            onResponse(ResponseType.NO, num)
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
