package com.podmev.cashsplitter.dialogs

import android.app.AlertDialog
import android.content.Context
import com.podmev.cashsplitter.R

class SimpleDialog(context: Context) : AlertDialog.Builder(context) {

    lateinit var onResponse: (r : ResponseType) -> Unit

    enum class ResponseType {
        YES, NO, CANCEL
    }

    fun show(title: String, message: String, listener: (r : ResponseType) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        onResponse = listener

        // performing positive action
        builder.setPositiveButton(R.string.dialog_button_positive) { _, _ ->
            onResponse(ResponseType.YES)
        }

        // performing negative action
        builder.setNegativeButton(R.string.dialog_button_negative) { _, _ ->
            onResponse(ResponseType.NO)
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
