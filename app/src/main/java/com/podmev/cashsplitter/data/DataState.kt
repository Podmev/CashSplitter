package com.podmev.cashsplitter.data

//TODO private fields and safe setters and getters
//TODO make normal serialization through json lib
data class DataState(
    var categories: MutableList<CashCategory>,
    var selectedCategoryPosition: Int,
    var availableSum: Double
) {

    fun calcTotalSum(): Double = totalSumByCategories(categories)
    fun calcNotPlannedSum(): Double = availableSum - calcTotalSum()

    //use carefully -> can be null
    fun curCategory() = categories[selectedCategoryPosition]

    fun unselectCategory() {
        selectedCategoryPosition = UNSELECTED_CATEGORY_POSITION
    }

    fun isSelectedCategory(): Boolean = selectedCategoryPosition != UNSELECTED_CATEGORY_POSITION
    fun hasNotPlannedAlert(): Boolean = calcNotPlannedSum() < 0

    fun moveSelectedCategoryDown() {
        val curCategory = curCategory()
        val nextCategory = categories[selectedCategoryPosition + 1]
        categories[selectedCategoryPosition] = nextCategory
        categories[selectedCategoryPosition + 1] = curCategory
        selectedCategoryPosition++
    }

    fun moveSelectedCategoryUp() {
        val curCategory = curCategory()
        val prevCategory = categories[selectedCategoryPosition - 1]
        categories[selectedCategoryPosition] = prevCategory
        categories[selectedCategoryPosition - 1] = curCategory
        selectedCategoryPosition--
    }

    fun erase(newCategories: List<CashCategory>){
        categories.apply {
            clear()
            addAll(newCategories)
        }
        unselectCategory()
        availableSum = 0.0
    }

    companion object {
        const val UNSELECTED_CATEGORY_POSITION = -1
    }
}

fun createEmptyDataState() =
    DataState(mutableListOf(), DataState.UNSELECTED_CATEGORY_POSITION, 0.0)

fun DataState.serialize(): String =
    listOf(selectedCategoryPosition, availableSum, categories.toLines()).joinToString(
        recordDelimiter
    )

fun deserializeCashCategoriesFromString(lines: String): DataState {
    //important that is 3, because we want only 2 lines and then full text till end of documents without split
    val list = lines.split(recordDelimiter, limit = 3)
    val selectedCategoryPosition = list.component1().toInt()
    val availableSum = list.component2().toDouble()
    val categories: List<CashCategory> = parseCashCategoriesFromLines(list.component3())
    return DataState(categories.toMutableList(), selectedCategoryPosition, availableSum)
}

fun DataState.reloadWithAnother(otherState: DataState) {
    categories.apply {
        categories.clear()
        categories.addAll(otherState.categories)
    }
    selectedCategoryPosition = otherState.selectedCategoryPosition
    availableSum = otherState.availableSum
}

