package com.dongbantest.app

import com.dongbantest.app.data.repository.QuestionBank
import com.dongbantest.app.domain.model.DataSourceType
import com.dongbantest.app.domain.model.Trip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainLogicTest {

    @Test
    fun questionBank_hasAtLeastTenQuestions() {
        assertTrue(QuestionBank.questions.size >= 10)
    }

    @Test
    fun questionBank_nextIsAlwaysDifferent() {
        var current = QuestionBank.questions.first()
        repeat(50) {
            val next = QuestionBank.next(current)
            assertNotEquals(current, next)
            current = next
        }
    }

    @Test
    fun drivenDistance_requiresBothValues() {
        val base = Trip(
            id = 1, title = "t", destination = "d", companions = emptyList(), startedAt = 0, endedAt = 1,
            startOdometer = 100f, endOdometer = 150.5f, odometerSource = DataSourceType.MOCK, isSample = false,
        )
        assertEquals(50.5f, base.drivenDistance!!, 0.001f)
        assertNull(base.copy(endOdometer = null).drivenDistance)
        assertNull(base.copy(endOdometer = 90f).drivenDistance)
    }
}
