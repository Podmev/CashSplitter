package com.podmev.cashsplitter.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns


fun getFilePathFromUri(uri: Uri): String =
    uri.path!!.split(":")[1]


/**
 * Obtains the file name for a URI using content resolvers. Taken from the following link
 * https://developer.android.com/training/secure-file-sharing/retrieve-info.html#RetrieveFileInfo
 *
 * @param uri a uri to query
 * @return the file name with no path
 * @throws IllegalArgumentException if the query is null, empty, or the column doesn't exist
 */
fun getFileName(context: Context, uri: Uri): String {
    // Obtain a cursor with information regarding this uri
    val cursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
    if (cursor.count <= 0) {
        cursor.close()
        throw IllegalArgumentException("Can't obtain file name, cursor is empty")
    }
    cursor.moveToFirst()
    val fileName: String =
        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
    cursor.close()
    return fileName
}