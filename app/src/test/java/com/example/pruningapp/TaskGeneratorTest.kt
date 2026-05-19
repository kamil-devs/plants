package com.example.pruningapp

import com.example.pruningapp.data.computeTaskDates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskGeneratorTest {

    // Helper: 2026-03-15 (mid-spring, both years have data ahead)
    private val mid2026 = LocalDate.of(2026, 3, 15)

    @Test
    fun `generates tasks for current and next year when both windows are in the future`() {
        val pairs = computeTaskDates("04-01", "04-30", today = mid2026)
        assertEquals(2, pairs.size)
        assertEquals(LocalDate.of(2026, 4, 1), pairs[0].first)
        assertEquals(LocalDate.of(2026, 4, 30), pairs[0].second)
        assertEquals(LocalDate.of(2027, 4, 1), pairs[1].first)
        assertEquals(LocalDate.of(2027, 4, 30), pairs[1].second)
    }

    @Test
    fun `skips current year window that already ended`() {
        // Today is 2026-03-15; window 01-01..01-31 ended in January
        val pairs = computeTaskDates("01-01", "01-31", today = mid2026)
        assertEquals(1, pairs.size)
        assertEquals(LocalDate.of(2027, 1, 1), pairs[0].first)
    }

    @Test
    fun `skips both years when window ends before today across both`() {
        // Only possible if today is very late in year and window is early
        val lateYear = LocalDate.of(2026, 12, 31)
        val pairs = computeTaskDates("01-01", "01-15", today = lateYear)
        // 2026 window ended; 2027 window hasn't started yet but endDate 2027-01-15 > 2026-12-31
        assertEquals(1, pairs.size)
        assertEquals(2027, pairs[0].first.year)
    }

    @Test
    fun `includes window that ends exactly today`() {
        val today = LocalDate.of(2026, 4, 30)
        val pairs = computeTaskDates("04-01", "04-30", today = today)
        assertTrue(pairs.isNotEmpty())
        assertEquals(today, pairs.first().second)
    }

    @Test
    fun `skips window with invalid MM-dd gracefully`() {
        val pairs = computeTaskDates("13-01", "13-31", today = mid2026)
        assertTrue(pairs.isEmpty())
    }

    @Test
    fun `handles leap-year date 02-29 gracefully`() {
        // 2026 is not a leap year — parse should fail; 2027 is not either
        val non_leap = LocalDate.of(2026, 1, 1)
        val pairs = computeTaskDates("02-29", "03-15", today = non_leap)
        // Both 2026 and 2027 have no Feb 29; should produce 0 results
        assertTrue(pairs.isEmpty())
    }
}
