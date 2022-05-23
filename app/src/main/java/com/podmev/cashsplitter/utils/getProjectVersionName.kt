package com.podmev.cashsplitter.utils

import android.content.Context
import android.content.pm.PackageManager

fun getProjectVersionName(context: Context): String?{
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}