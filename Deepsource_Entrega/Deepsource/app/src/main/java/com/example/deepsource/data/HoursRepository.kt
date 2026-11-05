package com.example.deepsource.data

import android.content.Context
import com.example.deepsource.domain.Project
import com.example.deepsource.domain.RunningTimer
import com.example.deepsource.domain.Session
import com.example.deepsource.domain.TimeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable

internal data class HoursSnapshot(
    val projects: List<Project>,
    val entries: List<TimeEntry>,
    val teamEntries: List<TimeEntry>,
    val timer: RunningTimer?
)

internal class HoursRepository(context: Context) : Closeable {
    private val store = LocalStore(context.applicationContext)

    suspend fun read(session: Session): HoursSnapshot = withContext(Dispatchers.IO) {
        HoursSnapshot(
            projects = store.projects(session),
            entries = store.entries(session),
            teamEntries = if (session.role == "LEADER") store.entries(session, team = true) else emptyList(),
            timer = store.timer(session)
        )
    }

    suspend fun seedDemo() = withContext(Dispatchers.IO) { store.seedDemo() }

    suspend fun saveEntry(session: Session, entry: TimeEntry): Long =
        withContext(Dispatchers.IO) { store.saveEntry(session, entry) }

    suspend fun deleteEntry(session: Session, id: Long) =
        withContext(Dispatchers.IO) { store.deleteEntry(session, id) }

    suspend fun createProject(session: Session, name: String, client: String, estimate: Int) =
        withContext(Dispatchers.IO) { store.createProject(session, name, client, estimate) }

    suspend fun saveTimer(session: Session, timer: RunningTimer?) =
        withContext(Dispatchers.IO) { store.saveTimer(session, timer) }

    suspend fun toggleTimer(session: Session, wall: Long, elapsed: Long, boot: Int, pauseOnly: Boolean = false) =
        withContext(Dispatchers.IO) { store.toggleTimer(session, wall, elapsed, boot, pauseOnly) }

    suspend fun completeTimer(session: Session, wall: Long, elapsed: Long, boot: Int): TimeEntry =
        withContext(Dispatchers.IO) { store.completeTimer(session, wall, elapsed, boot) }

    override fun close() = store.close()
}
