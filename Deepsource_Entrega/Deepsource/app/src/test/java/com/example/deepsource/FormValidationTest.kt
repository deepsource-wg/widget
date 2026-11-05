package com.example.deepsource

import com.example.deepsource.domain.validatePasswordChange
import com.example.deepsource.domain.validateProject
import com.example.deepsource.domain.validateTimer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormValidationTest {
    @Test
    fun projectReportsEachInvalidFieldAndAcceptsCorrectedValues() {
        val invalid = validateProject("A", " ", 0)
        assertFalse(invalid.valid)
        assertNotNull(invalid.name)
        assertNotNull(invalid.client)
        assertNotNull(invalid.estimate)
        assertTrue(validateProject("Portal web", "Cliente", 32).valid)
    }

    @Test
    fun timerIdentifiesTheMissingProjectAndTask() {
        val invalid = validateTimer(null, "  ")
        assertFalse(invalid.valid)
        assertNotNull(invalid.project)
        assertNotNull(invalid.task)
        assertTrue(validateTimer("proyecto-1", "Corregir formulario").valid)
    }

    @Test
    fun passwordChangeReportsCurrentPasswordAndConfirmationIndependently() {
        val invalid = validatePasswordChange("", "NuevaClave123", "Diferente123")
        assertFalse(invalid.valid)
        assertNotNull(invalid.current)
        assertNotNull(invalid.confirmation)
        assertTrue(validatePasswordChange("Actual123", "NuevaClave123", "NuevaClave123").valid)
    }
}
