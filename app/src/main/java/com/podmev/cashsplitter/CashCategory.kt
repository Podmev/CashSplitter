package com.podmev.cashsplitter

class CashCategory(var name:String, var sum: Double, var canNameBeEdited: Boolean, var canBeDeleted: Boolean) {
    
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
