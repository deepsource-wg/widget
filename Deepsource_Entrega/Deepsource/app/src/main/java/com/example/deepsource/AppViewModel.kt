package com.example.deepsource

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsource.data.*
import com.example.deepsource.domain.*
import com.example.deepsource.widget.HoursWidget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val hours = HoursRepository(application)
    private val photos = ProfilePhotos(application)
    private val vault = SessionVault(application)
    val auth = AuthRepository(application)
    var session by mutableStateOf<Session?>(null); private set
    var projects by mutableStateOf<List<Project>>(emptyList()); private set
    var records by mutableStateOf<List<TimeEntry>>(emptyList()); private set
    var teamRecords by mutableStateOf<List<TimeEntry>>(emptyList()); private set
    var timer by mutableStateOf<RunningTimer?>(null); private set
    var profilePhoto by mutableStateOf<Bitmap?>(null); private set
    var photoBusy by mutableStateOf(false); private set
    var busy by mutableStateOf(false); private set
    var loading by mutableStateOf(true); private set
    var message by mutableStateOf<String?>(null)
    var month by mutableStateOf(YearMonth.now())
    var screen by mutableStateOf("home")
    var editing by mutableStateOf<TimeEntry?>(null)
    var selectedProject by mutableStateOf<String?>(null)
    var lastSaved by mutableStateOf<TimeEntry?>(null); private set
    private var pendingRoute: String? = null
    val boot get() = Settings.Global.getInt(getApplication<Application>().contentResolver,Settings.Global.BOOT_COUNT,0)
    fun elapsedMs() = timer?.elapsed(System.currentTimeMillis(),SystemClock.elapsedRealtime(),boot) ?: 0L

    init { viewModelScope.launch {
        try {
            session = withContext(Dispatchers.IO) { vault.read() }
            profilePhoto = readProfilePhoto(session)
            reload()
            if (session != null) pendingRoute?.let { screen = it; pendingRoute = null }
        }
        catch (error: CancellationException) { throw error }
        catch(_: Exception) { message="No pudimos abrir los datos. Vuelve a iniciar sesión" }
        finally { loading=false }
    } }

    fun route(destination: String) {
        if(destination=="entry") editing=null
        if(session==null) pendingRoute=destination else screen=destination
    }
    fun refresh() = action { reload(); updateWidgets() }
    private suspend fun reload() {
        val s = session
        if(s==null) { projects=emptyList(); records=emptyList(); teamRecords=emptyList(); timer=null; return }
        val result = hours.read(s)
        projects = result.projects
        records = result.entries
        timer = result.timer
        teamRecords = result.teamEntries
    }
    private fun action(block: suspend () -> Unit) {
        if(busy) return
        busy=true
        viewModelScope.launch {
            try { block() }
            catch(_: AuthCancelled) { message="Acceso con Google cancelado" }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { message=e.message ?: "No pudimos completar la acción. Inténtalo de nuevo" }
            finally { busy=false }
        }
    }
    private suspend fun acceptSession(s: Session) {
        withContext(Dispatchers.IO) { vault.save(s) }
        if (s.demo) hours.seedDemo()
        profilePhoto = readProfilePhoto(s)
        session=s; reload(); screen=pendingRoute ?: "home"; pendingRoute=null
        if(s.demo && records.isNotEmpty()) month=YearMonth.from(records.maxBy {it.day}.day)
        updateWidgets()
    }
    fun login(email: String, password: String) = action { acceptSession(auth.signIn(email,password)) }
    fun register(name: String, email: String, password: String, onEmailInUse: (String) -> Unit) = action {
        try {
            acceptSession(auth.register(name, email, password))
        } catch (error: LocalEmailInUseException) {
            onEmailInUse(error.message.orEmpty())
        }
    }
    fun changePassword(
        current: String,
        next: String,
        onUpdated: () -> Unit,
        onCurrentPasswordError: (String) -> Unit
    ) = action {
        try {
            auth.changePassword(requireNotNull(session), current, next)
        } catch (e: LocalAuthenticationException) {
            onCurrentPasswordError(e.message.orEmpty())
            return@action
        }
        message = "Contraseña actualizada"
        onUpdated()
    }
    fun google(context: Context) = action { acceptSession(auth.google(context)) }
    fun demo(leader: Boolean = false) = action {
        val s = if(leader) Session("demo_ana","Ana Pérez","ana.perez@deepsource.demo","LEADER",true,"Demostración")
            else Session("demo_luis","Luis Gómez","luis.gomez@deepsource.demo","DEV",true,"Demostración")
        acceptSession(s)
    }
    fun resetPassword(email: String) = action {
        auth.resetPassword(email); message="Si el correo está registrado, recibirás un enlace para recuperar tu contraseña"
    }
    fun profile(name: String) = action {
        val nameError = validateName(name)
        require(nameError == null) { nameError.orEmpty() }
        val updated=auth.updateProfile(requireNotNull(session),name)
        withContext(Dispatchers.IO) { vault.save(updated) }; session=updated
        message="Perfil actualizado"
        updateWidgets()
    }
    private suspend fun readProfilePhoto(current: Session?): Bitmap? = withContext(Dispatchers.IO) {
        current?.let { photos.load(it.uid) }
    }

    fun updateProfilePhoto(owner: String, source: Uri?, capture: File? = null) {
        if (session?.uid != owner || photoBusy) {
            capture?.delete()
            return
        }
        photoBusy = true
        viewModelScope.launch {
            try {
                val updated = withContext(Dispatchers.IO) {
                    if (source == null) {
                        photos.remove(owner)
                        null
                    } else {
                        photos.save(owner, source)
                    }
                }
                if (session?.uid == owner) {
                    profilePhoto = updated
                    message = if (source == null) "Foto eliminada" else "Foto de perfil guardada"
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (session?.uid == owner) message = "No pudimos cambiar la foto de perfil"
            } finally {
                capture?.delete()
                photoBusy = false
            }
        }
    }

    fun logout() = action {
        val s=session
        if (s != null) {
            hours.toggleTimer(s, System.currentTimeMillis(), SystemClock.elapsedRealtime(), boot, pauseOnly = true)
            withContext(Dispatchers.IO) { vault.save(null) }
        }
        profilePhoto = null
        session=null; reload(); screen="home"; editing=null; selectedProject=null; lastSaved=null
        updateWidgets(); auth.signOut()
    }
    fun openEntry(entry: TimeEntry? = null, project: String? = null) { editing=entry; selectedProject=project ?: entry?.projectId; screen="entry" }
    fun save(project: String, day: LocalDate, seconds: Long, task: String) = action {
        val s=requireNotNull(session)
        val entry=TimeEntry(editing?.id ?: 0,s.uid,s.name,project,day,seconds,task,editing?.source ?: "Manual")
        val id = hours.saveEntry(s, entry)
        lastSaved = entry.copy(id = id)
        month = YearMonth.from(entry.day)
        editing = null
        reload()
        screen = "success"
        updateWidgets()
    }
    fun delete(entry: TimeEntry) = action {
        hours.deleteEntry(requireNotNull(session), entry.id)
        reload(); screen="history"; message="Registro eliminado"; updateWidgets()
    }
    fun createProject(name: String, client: String, estimate: Int, onCreated: () -> Unit) = action {
        hours.createProject(requireNotNull(session), name, client, estimate)
        reload()
        message = "Proyecto creado"
        onCreated()
    }
    fun startTimer(project: String, task: String) = action {
        require(timer==null) { "Termina o descarta el cronómetro actual antes de iniciar otro" }
        val taskError = validateTask(task)
        require(taskError == null) { taskError.orEmpty() }
        val s=requireNotNull(session)
        val value=RunningTimer(project,task.trim(),startedWall=System.currentTimeMillis(),startedElapsed=SystemClock.elapsedRealtime(),boot=boot)
        hours.saveTimer(s, value); reload(); updateWidgets()
    }
    fun toggleTimer() = action {
        timer = hours.toggleTimer(requireNotNull(session), System.currentTimeMillis(), SystemClock.elapsedRealtime(), boot)
        updateWidgets()
    }
    fun finishTimer() = action {
        val saved = hours.completeTimer(requireNotNull(session), System.currentTimeMillis(), SystemClock.elapsedRealtime(), boot)
        lastSaved = saved
        month = YearMonth.from(saved.day)
        reload(); screen="success"; updateWidgets()
    }
    fun discardTimer() = action {
        hours.saveTimer(requireNotNull(session), null)
        reload(); updateWidgets()
    }
    private suspend fun updateWidgets() = withContext(Dispatchers.IO) { HoursWidget.updateAll(getApplication()) }
    override fun onCleared() { hours.close(); super.onCleared() }
}
