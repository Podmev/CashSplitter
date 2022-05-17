package com.podmev.cashsplitter

import com.podmev.cashsplitter.data.*
import org.junit.Assert
import org.junit.Test

class DataStateUnitTest {

    @Test
    fun serialisationTest() {
        val categories = listOf(
            CashCategory("Other", 100.0, false, false, false),
            CashCategory("Food", 200.0, false, false, false),
            CashCategory("Medicine", 600.0, false, false, false),
            CashCategory("Fun", 50.0, false, false, false)
        )
        val dataState = DataState(categories.toMutableList(), -1, 1000.0)

        Assert.assertEquals(dataState, deserializeCashCategoriesFromString(dataState.serialize()))
    }
}