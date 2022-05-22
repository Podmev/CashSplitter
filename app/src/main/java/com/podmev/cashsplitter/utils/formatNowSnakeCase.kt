package com.podmev.cashsplitter.utils

import java.util.*

fun formatNowSnakeCase():String{
    val calendar: Calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1 //so month starts with number 1
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    return listOf(year, month, dayOfMonth, hour, minute, second).joinToString("_")
}