package com.example.deepsource.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.deepsource.domain.*
import java.time.LocalDate
import java.util.UUID

/** SQLite keeps entries and timers after process death, always scoped to the session uid. */
class LocalStore(context: Context) : SQLiteOpenHelper(context, "deepsource.db", null, 1) {
    override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE projects(id TEXT PRIMARY KEY, workspace TEXT NOT NULL, name TEXT NOT NULL, client TEXT NOT NULL, estimate INTEGER NOT NULL, team_estimate INTEGER NOT NULL, color INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE entries(id INTEGER PRIMARY KEY AUTOINCREMENT, uid TEXT NOT NULL, user_name TEXT NOT NULL, project_id TEXT NOT NULL REFERENCES projects(id), day TEXT NOT NULL, seconds INTEGER NOT NULL CHECK(seconds > 0), task TEXT NOT NULL, source TEXT NOT NULL)")
        db.execSQL("CREATE INDEX entries_user_day ON entries(uid,day)")
        db.execSQL("CREATE TABLE timers(uid TEXT PRIMARY KEY, project_id TEXT NOT NULL REFERENCES projects(id), task TEXT NOT NULL, accumulated INTEGER NOT NULL, wall INTEGER NOT NULL, elapsed INTEGER NOT NULL, boot INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE flags(name TEXT PRIMARY KEY)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun projects(session: Session): List<Project> = readableDatabase.query("projects", null,
        "workspace=?", arrayOf(session.workspace), null, null, "rowid").use { c ->
        buildList { while(c.moveToNext()) add(Project(c.getString(c.getColumnIndexOrThrow("id")),
            c.getString(c.getColumnIndexOrThrow("name")), c.getString(c.getColumnIndexOrThrow("client")),
            c.getInt(c.getColumnIndexOrThrow("estimate")), c.getInt(c.getColumnIndexOrThrow("team_estimate")),
            c.getLong(c.getColumnIndexOrThrow("color")))) }
    }

    fun entries(session: Session): List<TimeEntry> {
        val args = arrayOf(session.uid, session.workspace)
        return readableDatabase.rawQuery("SELECT e.* FROM entries e JOIN projects p ON p.id=e.project_id WHERE e.uid=? AND p.workspace=? ORDER BY e.day DESC,e.id DESC", args).use { c ->
            buildList { while(c.moveToNext()) add(TimeEntry(c.getLong(0),c.getString(1),c.getString(2),
                c.getString(3),LocalDate.parse(c.getString(4)),c.getLong(5),c.getString(6),c.getString(7))) }
        }
    }

    fun saveEntry(session: Session, entry: TimeEntry): Long {
        require(entry.uid == session.uid) { "Solo puedes registrar tus propias horas" }
        require(projects(session).any { it.id == entry.projectId }) { "Proyecto no disponible" }
        require(entry.day <= LocalDate.now() && entry.seconds in 1..86400 && validateTask(entry.task) == null) {
            "Revisa la fecha, duración y tarea"
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val already = db.rawQuery("SELECT COALESCE(SUM(seconds),0) FROM entries WHERE uid=? AND day=? AND id<>?",
                arrayOf(session.uid, entry.day.toString(), entry.id.toString())).use { it.moveToFirst(); it.getLong(0) }
            require(already + entry.seconds <= 86400) { "Los registros de un día no pueden superar 24 horas" }
            val values = ContentValues().apply {
                put("uid",session.uid); put("user_name",session.name); put("project_id",entry.projectId)
                put("day",entry.day.toString()); put("seconds",entry.seconds); put("task",entry.task.trim()); put("source",entry.source)
            }
            val id = if(entry.id == 0L) db.insertOrThrow("entries",null,values) else {
                require(db.update("entries",values,"id=? AND uid=?",arrayOf(entry.id.toString(),session.uid)) == 1) { "Registro no disponible" }
                entry.id
            }
            db.setTransactionSuccessful()
            return id
        } finally { db.endTransaction() }
    }

    fun deleteEntry(session: Session, id: Long) {
        require(writableDatabase.delete("entries","id=? AND uid=?",arrayOf(id.toString(),session.uid)) == 1)
    }

    fun createProject(session: Session, name: String, client: String, estimate: Int) {
        require(validateProject(name, client, estimate).valid) { "Revisa los datos del proyecto" }
        writableDatabase.insertOrThrow("projects",null,ContentValues().apply {
            put("id",UUID.randomUUID().toString()); put("workspace",session.workspace)
            put("name",name.trim()); put("client",client.trim()); put("estimate",estimate * 60)
            put("team_estimate",estimate * 60); put("color",0xFFA99BFF)
        })
    }

    fun timer(session: Session): RunningTimer? = readableDatabase.query("timers",null,"uid=?",arrayOf(session.uid),null,null,null).use { c ->
        if(!c.moveToFirst()) null else RunningTimer(c.getString(1),c.getString(2),c.getLong(3),c.getLong(4),c.getLong(5),c.getInt(6))
    }

    fun saveTimer(session: Session, timer: RunningTimer?) {
        if(timer == null) { writableDatabase.delete("timers","uid=?",arrayOf(session.uid)); return }
        require(projects(session).any { it.id == timer.projectId })
        writableDatabase.insertWithOnConflict("timers",null,ContentValues().apply {
            put("uid",session.uid); put("project_id",timer.projectId); put("task",timer.task)
            put("accumulated",timer.accumulatedMs); put("wall",timer.startedWall)
            put("elapsed",timer.startedElapsed); put("boot",timer.boot)
        },SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun toggleTimer(session:Session,wall:Long,elapsed:Long,boot:Int,pauseOnly:Boolean=false):RunningTimer? {
        val db=writableDatabase
        db.beginTransaction()
        try {
            val current=timer(session) ?: return null
            val next=if(current.running) current.copy(accumulatedMs=current.elapsed(wall,elapsed,boot),startedWall=0,startedElapsed=0)
                else if(pauseOnly) current else current.copy(startedWall=wall,startedElapsed=elapsed,boot=boot)
            saveTimer(session,next)
            db.setTransactionSuccessful()
            return next
        } finally {db.endTransaction()}
    }

    fun completeTimer(session:Session,wall:Long,elapsed:Long,boot:Int):TimeEntry {
        val db=writableDatabase
        db.beginTransaction()
        try {
            val current=timer(session) ?: error("No hay un cronómetro activo")
            val seconds=current.elapsed(wall,elapsed,boot)/1000
            require(seconds>0) {"El cronómetro debe registrar al menos un segundo"}
            val result=finishTimer(session,seconds)
            db.setTransactionSuccessful()
            return result
        } finally {db.endTransaction()}
    }

    /** One transaction avoids duplicate entries if the app closes while finishing a timer. */
    fun finishTimer(session: Session, seconds: Long): TimeEntry {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val timer = timer(session) ?: error("No hay un cronómetro activo")
            val entry = TimeEntry(uid=session.uid,userName=session.name,projectId=timer.projectId,
                day=LocalDate.now(),seconds=seconds,task=timer.task,source="Cronómetro")
            val saved = entry.copy(id=saveEntry(session,entry))
            saveTimer(session,null)
            db.setTransactionSuccessful()
            return saved
        } finally { db.endTransaction() }
    }

    fun seedDemo() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.rawQuery("SELECT name FROM flags WHERE name='demo_seeded'",null).use { if(it.moveToFirst()) return }
            val names = listOf("Portal Cliente AFC","Rediseño Web","Dashboard Admin","API Integración")
            val clients = listOf("AFC","NOVA","AFC","NOVA")
            val estimates = listOf(32,32,16,8)
            val teamEstimates = listOf(40,36,24,12)
            val colors = listOf(0xFFB6A0FF,0xFFF5BA78,0xFF81B8FF,0xFFE79CD4)
            val dev = listOf(24,38,12,7)
            val leader = listOf(8,4,4,3)
            val today=LocalDate.now()
            val base = (if(today.dayOfMonth>=16) today else today.minusMonths(1)).withDayOfMonth(1)
            names.forEachIndexed { i,name ->
                val id = "demo_$i"
                db.execSQL("INSERT INTO projects VALUES(?,?,?,?,?,?,?)",arrayOf<Any>(id,"demo",name,clients[i],estimates[i]*60,teamEstimates[i]*60,colors[i]))
                // Split long example tasks over different days so the 24 h/day rule also holds for the demo.
                listOf("demo_luis" to dev[i],"demo_ana" to leader[i]).forEach { (uid,hours) ->
                    var remaining = hours
                    var n = i * 5
                    while(remaining > 0) {
                        val chunk = minOf(remaining,8)
                        val day = base.plusDays(n.toLong()).coerceAtMost(LocalDate.now())
                        db.execSQL("INSERT INTO entries(uid,user_name,project_id,day,seconds,task,source) VALUES(?,?,?,?,?,?,?)",
                            arrayOf<Any>(uid,if(uid=="demo_luis") "Luis Gómez" else "Ana Pérez",id,day.toString(),chunk*3600,
                                listOf("Desarrollo del portal de clientes","Diseño y componentes de la interfaz","Métricas y visualización de datos","Pruebas de integración")[i],"Ejemplo"))
                        remaining -= chunk; n++
                    }
                }
            }
            db.execSQL("INSERT INTO flags VALUES('demo_seeded')")
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
}
