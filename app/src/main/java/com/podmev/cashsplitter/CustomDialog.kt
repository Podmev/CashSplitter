package com.podmev.cashsplitter

import android.app.AlertDialog
import android.content.Context

class CustomDialog(context: Context) : AlertDialog.Builder(context) {

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
        builder.setPositiveButton("Yes") { _, _ ->
            onResponse(ResponseType.YES)
        }

        // performing negative action
        builder.setNegativeButton("No") { _, _ ->
            onResponse(ResponseType.NO)
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
