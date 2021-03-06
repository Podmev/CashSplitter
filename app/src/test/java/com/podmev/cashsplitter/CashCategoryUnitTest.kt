package com.podmev.cashsplitter

import com.podmev.cashsplitter.data.*
import org.junit.Assert
import org.junit.Test

class CashCategoryUnitTest {

    @Test
    fun serialisationTest() {
        val category = CashCategory("fun", 150.5, false, true, false)
        Assert.assertEquals(category, parseCashCategoryFromLine(category.toLine()))
    }

    @Test
    fun manySerialisationTest() {
        val categories = listOf(
            CashCategory("Other", 100.0, false, false, false),
            CashCategory("Food", 200.0, false, false, false),
            CashCategory("Medicine", 600.0, false, false, false),
            CashCategory("Fun", 50.0, false, false, false)
        )
        Assert.assertEquals(categories, parseCashCategoriesFromLines(categories.toLines()))
    }
}