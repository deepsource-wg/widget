package com.example.deepsource

import com.example.deepsource.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class LocalPrototypeTest {
    @Test fun passwordDigestChecksCorrectPasswordAndRejectsWrongPassword() {
        val digest=Passwords.create("Deepsource2026!")
        assertTrue(Passwords.verify("Deepsource2026!",digest))
        assertFalse(Passwords.verify("OtraClave2026!",digest))
        assertFalse(Passwords.verify("",digest))
    }
    @Test fun everyAccountReceivesAnIndependentSalt() {
        val a=Passwords.create("Deepsource2026!")
        val b=Passwords.create("Deepsource2026!")
        assertFalse(a.salt.contentEquals(b.salt));assertFalse(a.hash.contentEquals(b.hash))
        assertTrue(Passwords.verify("Deepsource2026!",a));assertTrue(Passwords.verify("Deepsource2026!",b))
    }
    @Test fun weakPasswordsAreRejected() {
        assertNotNull(Passwords.validate("12345678"));assertNotNull(Passwords.validate("password"))
        assertNotNull(Passwords.validate("Ab1"));assertNull(Passwords.validate("Proyecto2026"))
    }
    @Test fun smallWidgetReservesSpaceForActionsAndTimer() {
        val small=widgetLayout(220,true)
        assertEquals(0,small.rows);assertFalse(small.showTotal);assertFalse(small.showFooter)
        val large=widgetLayout(420,true)
        assertEquals(4,large.rows);assertTrue(large.showTotal);assertTrue(large.showFooter)
    }
    @Test fun largerFontReducesOptionalContent() {
        val normal=widgetLayout(330,false)
        val large=widgetLayout(330,false,1.5f)
        assertTrue(large.rows<normal.rows)
    }
    @Test fun secondPrecisionCanBeEditedWithoutInventingMinutes() {
        val today=LocalDate.now()
        assertTrue(validateEntry("p","0","0","Tarea breve",today,seconds=5).valid)
        assertFalse(validateEntry("p","24","0","Tarea breve",today,seconds=1).valid)
    }
    @Test fun csvEscapesQuotesSemicolonsAndSpreadsheetFormulas() {
        assertEquals("\"'=SUM(A1:A2)\"",csvCell("=SUM(A1:A2)"))
        assertEquals("\"Tarea; \"\"prueba\"\"\"",csvCell("Tarea; \"prueba\""))
    }
    @Test fun csvContainsExactSecondsAndAccountIdentity() {
        val e=TimeEntry(uid="u",userName="Luis",projectId="p",day=LocalDate.of(2026,10,28),seconds=65,task="Implementar validación")
        val csv=exportHoursCsv(listOf(e),listOf(Project("p","Portal","Cliente",60,60,0)),"luis@example.test")
        assertTrue(csv.startsWith("\uFEFFFecha;"));assertTrue(csv.contains("\"65\""));assertTrue(csv.contains("luis@example.test"))
    }
}
