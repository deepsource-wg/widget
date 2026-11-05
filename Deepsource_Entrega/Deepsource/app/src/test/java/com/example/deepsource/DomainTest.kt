package com.example.deepsource

import com.example.deepsource.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DomainTest {
    private val today=LocalDate.of(2026,10,28)
    @Test fun minutesOutsideRangeAreRejected() {
        val excessiveMinutes = validateEntry("p", "0", "72", "Implementar acceso", today, today)
        assertNotNull(excessiveMinutes.minutes)
        assertNull(excessiveMinutes.duration)
        assertNotNull(validateEntry("p","0","-1","Implementar acceso",today,today).minutes)
    }
    @Test fun emptyProjectZeroDurationAndFutureDayAreRejected() {
        val errors=validateEntry(null,"0","0","",today.plusDays(1),today)
        assertFalse(errors.valid);assertNotNull(errors.project);assertNotNull(errors.duration);assertNotNull(errors.task);assertNotNull(errors.date)
    }
    @Test fun acceptsNinetyMinutesAndLimitsTwentyFourHours() {
        assertTrue(validateEntry("p","1","30","Implementar acceso",today,today).valid)
        assertTrue(validateEntry("p","24","0","Implementar acceso",today,today).valid)
        assertFalse(validateEntry("p","24","1","Implementar acceso",today,today).valid)
    }
    @Test fun timerExcludesPausedTimeAndHandlesProcessRestarts() {
        val running=RunningTimer("p","Tarea",5000,100000,20000,4)
        assertEquals(8000,running.elapsed(103000,23000,4))
        val paused=running.copy(accumulatedMs=8000,startedWall=0,startedElapsed=0)
        assertEquals(8000,paused.elapsed(200000,60000,4))
        assertEquals(15000,running.elapsed(110000,1000,5))
    }
    @Test fun timerNeverSubtractsTimeAfterClockChanges() {
        val timer=RunningTimer("p","Tarea",5000,100000,20000,4)
        assertEquals(5000,timer.elapsed(90000,10000,4))
        assertEquals(5000,timer.elapsed(90000,1000,5))
    }
    @Test fun monthAndProjectTotalsAreComputedFromEntries() {
        val entries=listOf(TimeEntry(1,"u","Luis","p",today,5400,"Tarea"),
            TimeEntry(2,"u","Luis","p",today.minusMonths(1),3600,"Tarea"),
            TimeEntry(3,"u","Luis","q",today,1800,"Tarea"))
        assertEquals(5400,entries.inMonth(YearMonth.of(2026,10)).totalSeconds("p"))
        assertEquals(7200,entries.inMonth(YearMonth.of(2026,10)).totalSeconds())
    }
    @Test fun deviationHandlesMissingAndExceededEstimates() {
        assertNull(deviation(100,0));assertEquals(-25,deviation(24*3600L,32*60));assertEquals(19,deviation(38*3600L,32*60))
    }
    @Test fun durationFormattingRetainsShortSessions() {
        assertEquals("1 h 30 min",durationText(5400));assertEquals("45 s",durationText(45));assertEquals("01:30:00",clockText(5400000))
    }
}
