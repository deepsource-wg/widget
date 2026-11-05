package com.example.deepsource.domain

import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import kotlin.math.roundToInt

val Spanish = Locale.forLanguageTag("es-CL")

data class Session(
    val uid: String, val name: String, val email: String,
    val role: String = "DEV", val demo: Boolean = false,
    val provider: String = "Correo", val token: String = "",
    val refreshToken: String = "", val expiresAt: Long = 0
) {
    val workspace get() = if (demo) "demo" else uid
    val initials get() = name.trim().split(Regex("\\s+")).take(2).joinToString("") { it.take(1) }.uppercase(Spanish)
}

data class Project(val id: String, val name: String, val client: String,
    val estimateMinutes: Int, val teamEstimateMinutes: Int, val color: Long)

data class TimeEntry(val id: Long = 0, val uid: String, val userName: String,
    val projectId: String, val day: LocalDate, val seconds: Long, val task: String,
    val source: String = "Manual")

data class RunningTimer(val projectId: String, val task: String,
    val accumulatedMs: Long = 0, val startedWall: Long = 0,
    val startedElapsed: Long = 0, val boot: Int = 0) {
    val running get() = startedWall > 0
    fun elapsed(wall: Long, elapsed: Long, currentBoot: Int): Long = accumulatedMs +
        if (!running) 0 else if (boot == currentBoot) (elapsed - startedElapsed).coerceAtLeast(0)
        else (wall - startedWall).coerceAtLeast(0)
}

data class EntryErrors(val project: String? = null, val duration: String? = null,
    val minutes: String? = null, val task: String? = null, val date: String? = null) {
    val valid get() = listOf(project, duration, minutes, task, date).all { it == null }
}

fun validateEntry(projectId: String?, hours: String, minutes: String, task: String,
    day: LocalDate, today: LocalDate = LocalDate.now(), seconds: Int = 0): EntryErrors {
    val h = hours.toIntOrNull()
    val m = minutes.toIntOrNull()
    val minutesError = if (m == null || m !in 0..59) "Usa un valor entre 0 y 59" else null
    val durationError = when {
        h == null || h !in 0..24 -> "Usa un valor entre 0 y 24"
        seconds !in 0..59 -> "Revisa los segundos del registro"
        m != null && minutesError == null && h * 3600 + m * 60 + seconds !in 1..86400 ->
            "El tiempo total debe estar entre 1 segundo y 24 horas"
        else -> null
    }
    return EntryErrors(
        project = validateProjectSelection(projectId),
        duration = durationError,
        minutes = minutesError,
        task = validateTask(task),
        date = if (day > today) "La fecha no puede estar en el futuro" else null
    )
}

fun List<TimeEntry>.inMonth(month: YearMonth) = filter { YearMonth.from(it.day) == month }
fun List<TimeEntry>.totalSeconds(projectId: String? = null) =
    filter { projectId == null || it.projectId == projectId }.sumOf { it.seconds }

fun durationText(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0)
    val h = safe / 3600
    val m = safe % 3600 / 60
    return when {
        safe == 0L -> "0 h"
        safe < 60 -> "$safe s"
        h == 0L -> "$m min"
        m == 0L -> "$h h"
        else -> "$h h $m min"
    }
}

fun clockText(ms: Long): String {
    val s = ms.coerceAtLeast(0) / 1000
    return String.format(Locale.ROOT, "%02d:%02d:%02d", s / 3600, s % 3600 / 60, s % 60)
}

fun deviation(seconds: Long, estimateMinutes: Int): Int? =
    if (estimateMinutes <= 0) null else ((seconds / 60.0 - estimateMinutes) / estimateMinutes * 100).roundToInt()
