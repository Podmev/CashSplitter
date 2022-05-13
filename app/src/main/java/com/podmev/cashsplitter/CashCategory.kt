package com.podmev.cashsplitter

/*Main entity for cash category - each category shows one line in grid*/
data class CashCategory(var name:String, var sum: Double, var canNameBeEdited: Boolean, var canBeDeleted: Boolean) {

}
//TODO change for basic serialization
const val fieldDelimiter:String = "|"
const val recordDelimiter:String = "\n"

fun CashCategory.toLine():String =
    listOf(name, sum, canNameBeEdited, canBeDeleted).joinToString(fieldDelimiter)

fun parseCashCategoryFromLine(line: String):CashCategory {
    val list = line.split(fieldDelimiter)

    return CashCategory(
        list.component1(),
        list.component2().toDouble(),
        list.component3().toBooleanStrict(),
        list.component4().toBooleanStrict()
    )
}
fun List<CashCategory>.toLines():String =
    joinToString(recordDelimiter) { category->category.toLine() }

fun parseCashCategoriesFromLines(lines: String):List<CashCategory> {
    val list = lines.split(recordDelimiter)
    return list.map(::parseCashCategoryFromLine)
}

/*Converts cash categories to list of strings used for filling categories grid 
* 
* */
fun toPlainGridView(categories:List<CashCategory>): List<String> =
    categories.flatMap { listOf(it.name, it.sum.toString()) }.toList()

fun totalSumByCategories(categories:List<CashCategory>): Double =
    categories.sumOf { it.sum}

fun createSimpleCategories(): List<CashCategory> =
    listOf(
        CashCategory("Other", 100.0, false, false),
        CashCategory("Food", 200.0, false, false),
        CashCategory("Medicine", 600.0, false, false),
        CashCategory("Fun", 50.0, false, false)
    )

fun createMinCategories(): List<CashCategory> =
    listOf(
        CashCategory("Other", 0.0, false, false)
    )
