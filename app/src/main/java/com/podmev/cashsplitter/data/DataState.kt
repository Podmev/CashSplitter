package com.podmev.cashsplitter.data

//TODO private fields and safe setters and getters
//TODO make normal serialization through json lib
data class DataState(
    var categories:MutableList<CashCategory> = mutableListOf(),
    var selectedCategoryPosition:Int = -1,
    var availableSum:Double = 0.0){

    fun calcTotalSum():Double = totalSumByCategories(categories)
    fun calcNotPlannedSum():Double = calcTotalSum() - availableSum
}

fun DataState.serialize():String =
    listOf(selectedCategoryPosition, availableSum, categories.toLines()).joinToString(recordDelimiter)

fun deserializeCashCategoriesFromString(lines: String):DataState {
    //important that is 3, because we want only 2 lines and then full text till end of documents without split
    val list = lines.split(recordDelimiter, limit = 3)
    val selectedCategoryPosition = list.component1().toInt()
    val availableSum = list.component2().toDouble()
    val categories: List<CashCategory> = parseCashCategoriesFromLines(list.component3())
    return DataState(categories.toMutableList(), selectedCategoryPosition, availableSum)
}

