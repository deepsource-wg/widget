@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.deepsource.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.deepsource.AppViewModel
import com.example.deepsource.domain.*
import com.example.deepsource.ui.theme.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable fun ProjectPicker(projects:List<Project>,selected:String?,onSelect:(String?)->Unit,onDismiss:()->Unit,allowAll:Boolean=false) {
    var search by rememberSaveable {mutableStateOf("")}
    ModalBottomSheet(onDismissRequest=onDismiss,containerColor=Panel) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text("Selecciona un proyecto",style=MaterialTheme.typography.titleLarge)
            Text("El proyecto se asociará a tu registro.",color=Muted,style=MaterialTheme.typography.bodyMedium)
            Field(search,{search=it},"Buscar",placeholder="Proyecto o cliente",leading="search")
            LazyColumn(Modifier.heightIn(max=380.dp),contentPadding=PaddingValues(bottom=30.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                if(allowAll) item {PickerRow("Todos los proyectos","Sin filtro",selected==null,Violet) {onSelect(null)}}
                val result=projects.filter {it.name.contains(search,true)||it.client.contains(search,true)}
                if(result.isEmpty()) item {EmptyState("Sin resultados","Prueba con otro nombre.","search")}
                items(result,key={it.id}) {p ->PickerRow(p.name,p.client,p.id==selected,Color(p.color)) {onSelect(p.id)}}
            }
        }
    }
}

@Composable private fun PickerRow(title:String,subtitle:String,selected:Boolean,color:Color,onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if(selected) Mint.copy(alpha=.1f) else Elevated)
        .clickable(onClick=onClick).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(color.copy(alpha=.15f)),contentAlignment=Alignment.Center) {Text(title.take(2).uppercase(Spanish),color=color,fontWeight=FontWeight.Bold,fontSize=12.sp)}
        Column(Modifier.weight(1f)) {Text(title,fontWeight=FontWeight.SemiBold,fontSize=14.sp);Text(subtitle,color=Muted,style=MaterialTheme.typography.bodySmall)}
        Glyph(if(selected) "check" else "next",tint=if(selected) Mint else Muted)
    }
}

@Composable fun EntryScreen(vm:AppViewModel) {
    val original=vm.editing
    var project by rememberSaveable(original?.id) {mutableStateOf(vm.selectedProject)}
    var dayText by rememberSaveable(original?.id) {mutableStateOf((original?.day ?: LocalDate.now()).toString())}
    val day=LocalDate.parse(dayText)
    var hours by rememberSaveable(original?.id) {mutableStateOf(if(original==null) "0" else (original.seconds/3600).toString())}
    var minutes by rememberSaveable(original?.id) {mutableStateOf(if(original==null) "0" else (original.seconds%3600/60).toString())}
    var task by rememberSaveable(original?.id) {mutableStateOf(original?.task.orEmpty())}
    var submitted by rememberSaveable(original?.id) {mutableStateOf(false)}
    var picker by remember {mutableStateOf(false)}
    var calendar by remember {mutableStateOf(false)}
    var delete by remember {mutableStateOf(false)}
    val extraSeconds=(original?.seconds?.rem(60) ?: 0L).toInt()
    val selected = vm.projects.firstOrNull { it.id == project }
    val validation = validateEntry(selected?.id, hours, minutes, task, day, seconds = extraSeconds)
    val errors = if (submitted) validation else EntryErrors()
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        Text(if(original==null) "Haz que tu trabajo cuente." else "Actualiza los detalles de tu registro.",color=Muted)
        Identity(vm.session!!)
        SelectField("Proyecto",selected?.let {"${it.name} · ${it.client}"} ?: "Selecciona un proyecto",error=errors.project) {picker=true}
        if(vm.projects.isEmpty()) CreateProjectControl(vm)
        SelectField("Día",day.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy",Spanish)),"calendar",errors.date) {calendar=true}
        Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("Tiempo dedicado",fontWeight=FontWeight.SemiBold)
            Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                Field(hours,{hours=it.filter(Char::isDigit).take(2)},"Horas",Modifier.weight(1f),keyboard=KeyboardType.Number,error=errors.duration)
                Field(minutes,{minutes=it.filter(Char::isDigit).take(2)},"Minutos",Modifier.weight(1f),keyboard=KeyboardType.Number,error=errors.minutes)
            }
            if(original?.source=="Cronómetro" && original.seconds%60>0) Text("El registro incluye ${original.seconds%60} segundos adicionales, que se conservarán al editar.",color=Muted,style=MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf(15,30,60).forEach {value -> SuggestionChip(onClick={val total=(hours.toIntOrNull() ?: 0)*60+(minutes.toIntOrNull() ?: 0)+value;if(total<=1440){hours=(total/60).toString();minutes=(total%60).toString()}},label={Text("+$value min")}) }
        }
        Field(task,{task=it.take(160)},"Nombre de la tarea",placeholder="Ej. Implementar inicio de sesión",error=errors.task)
        Text("${task.length}/160 · Describe qué hiciste",color=Muted,style=MaterialTheme.typography.bodySmall)
        Primary(if(original==null) "Guardar registro" else "Guardar cambios","check",!vm.busy) {
            submitted=true
            if (selected != null && validation.valid) {
                val seconds = hours.toLong() * 3600 + minutes.toLong() * 60 + extraSeconds
                vm.save(selected.id, day, seconds, task)
            }
        }
        if(original!=null) TextButton({delete=true},Modifier.align(Alignment.CenterHorizontally),enabled=!vm.busy) {Glyph("trash",tint=Danger);Spacer(Modifier.width(8.dp));Text("Eliminar registro",color=Danger)}
        Text("El registro se asigna siempre a ${vm.session!!.name}.",color=Muted,style=MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))
    }
    if(picker) ProjectPicker(vm.projects,project,{project=it;picker=false},{picker=false})
    if(calendar) {
        val state=rememberDatePickerState(initialSelectedDateMillis=day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),selectableDates=object:SelectableDates {
            override fun isSelectableDate(utcTimeMillis:Long)=utcTimeMillis<=LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        })
        DatePickerDialog(onDismissRequest={calendar=false},confirmButton={TextButton({state.selectedDateMillis?.let {dayText=Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()};calendar=false}) {Text("Aceptar")}},dismissButton={TextButton({calendar=false}) {Text("Cancelar")}}) {DatePicker(state=state)}
    }
    if(delete) AlertDialog(onDismissRequest={delete=false},title={Text("¿Eliminar este registro?")},text={Text("Se descontarán ${durationText(original!!.seconds)} del proyecto. Esta acción no se puede deshacer.")},confirmButton={TextButton({delete=false;vm.delete(original!!)}) {Text("Eliminar",color=Danger)}},dismissButton={TextButton({delete=false}) {Text("Cancelar")}})
}

@Composable fun TimerScreen(vm:AppViewModel) {
    var project by rememberSaveable {mutableStateOf(vm.selectedProject ?: vm.projects.firstOrNull()?.id)}
    var task by rememberSaveable {mutableStateOf("")}
    var submitted by rememberSaveable { mutableStateOf(false) }
    var picker by remember {mutableStateOf(false)}
    var discard by remember {mutableStateOf(false)}
    val timer=vm.timer
    val elapsed=rememberTimerElapsed(vm)
    val selected = vm.projects.firstOrNull { it.id == project }
    val errors = validateTimer(selected?.id, task)
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        if(timer==null) {
            Text("Un proyecto. Una tarea. Todo tu enfoque.",Modifier.fillMaxWidth(),color=Muted)
            Identity(vm.session!!)
            SelectField("Proyecto", selected?.name ?: "Selecciona un proyecto",
                error = if (submitted) errors.project else null) { picker = true }
            if(vm.projects.isEmpty()) CreateProjectControl(vm)
            Field(task, { task = it.take(160) }, "¿En qué vas a trabajar?", placeholder = "Describe tu tarea",
                error = if (submitted) errors.task else null)
        } else PanelCard {
            val p=vm.projects.firstOrNull {it.id==timer.projectId}
            Text(p?.name.orEmpty(),color=Color(p?.color ?: 0xFFA99BFF),style=MaterialTheme.typography.labelSmall)
            Text(timer.task,style=MaterialTheme.typography.titleMedium)
            Text(vm.session!!.name,color=Muted,style=MaterialTheme.typography.bodySmall)
        }
        TimerDial(elapsed,timer?.running==true,if(timer==null) "Listo para empezar" else if(timer.running) "En marcha" else "En pausa")
        Text(if(timer?.running==true) "Concéntrate. Nosotros llevamos la cuenta." else if(timer!=null) "La pausa no suma tiempo." else "Cada sesión empieza con un primer paso.",color=Muted,style=MaterialTheme.typography.bodySmall)
        if (timer == null) Primary("Iniciar cronómetro", "play", !vm.busy) {
            submitted = true
            if (selected != null && errors.valid) {
                submitted = false
                vm.startTimer(selected.id, task)
            }
        }
        else {
            Primary(if(timer.running) "Pausar" else "Reanudar",if(timer.running) "pause" else "play",!vm.busy) {vm.toggleTimer()}
            Secondary("Terminar y registrar","stop",!vm.busy && elapsed>=1000) {vm.finishTimer()}
            Text("Se guarda únicamente el tiempo activo, con precisión de segundos.",color=Muted,style=MaterialTheme.typography.bodySmall)
            val p=vm.projects.firstOrNull {it.id==timer.projectId}
            if(p!=null) BalanceCard(p,vm.records.inMonth(vm.month).totalSeconds(p.id),p.estimateMinutes)
            TextButton({discard=true},enabled=!vm.busy) {Text("Descartar sesión",color=Muted)}
        }
        PanelCard {Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {Glyph("lock",tint=Mint);Text("Puedes salir de la app. Tu sesión del cronómetro se conserva y las pausas se excluyen del registro.",color=Muted,style=MaterialTheme.typography.bodySmall)}}
    }
    if(picker) ProjectPicker(vm.projects,project,{project=it;picker=false},{picker=false})
    if(discard) AlertDialog(onDismissRequest={discard=false},title={Text("¿Descartar la sesión?")},text={Text("El tiempo de este cronómetro no se registrará.")},confirmButton={TextButton({discard=false;vm.discardTimer()}) {Text("Descartar",color=Danger)}},dismissButton={TextButton({discard=false}) {Text("Conservar")}})
}

@Composable fun rememberTimerElapsed(vm:AppViewModel):Long {
    var elapsed by remember {mutableLongStateOf(vm.elapsedMs())}
    val lifecycle=LocalContext.current as LifecycleOwner
    LaunchedEffect(vm.timer,lifecycle) {
        lifecycle.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {while(true) {elapsed=vm.elapsedMs();delay(250)}}
    }
    return elapsed
}

@Composable fun TimerDial(elapsed:Long,running:Boolean,label:String) {
    val color=if(running) Mint else Violet
    Box(Modifier.size(246.dp).padding(8.dp),contentAlignment=Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Line,style=Stroke(3.dp.toPx()))
            drawArc(color,-90f,if(running) ((elapsed%3600000)/3600000f*360f).coerceAtLeast(8f) else 360f,false,
                topLeft=Offset.Zero,size=Size(size.width,size.height),style=Stroke(3.dp.toPx(),cap=StrokeCap.Round))
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(13.dp)) {
            Badge(label,color)
            Text(clockText(elapsed),fontSize=34.sp,fontWeight=FontWeight.SemiBold,fontFamily=FontFamily.Monospace,letterSpacing=(-1).sp)
            Text("HORAS : MINUTOS : SEGUNDOS",color=Muted,fontSize=9.sp,letterSpacing=.6.sp)
        }
    }
}

@Composable fun TimerBanner(vm:AppViewModel) {
    val timer=vm.timer ?: return
    val elapsed=rememberTimerElapsed(vm)
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Mint.copy(alpha=.08f)).border(1.dp,Mint.copy(alpha=.18f),RoundedCornerShape(16.dp)).clickable {vm.screen="timer"}.padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Glyph(if(timer.running) "timer" else "pause",tint=Mint)
        Column(Modifier.weight(1f)) {Text(if(timer.running) "Cronómetro en marcha" else "Cronómetro en pausa",color=Mint,fontSize=13.sp,fontWeight=FontWeight.SemiBold);Text(timer.task,color=Muted,maxLines=1,fontSize=12.sp)}
        Text(clockText(elapsed),fontFamily=FontFamily.Monospace,fontSize=13.sp,color=Mint)
    }
}

@Composable fun SuccessScreen(vm:AppViewModel) {
    val entry=vm.lastSaved ?: return
    val project=vm.projects.firstOrNull {it.id==entry.projectId}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(24.dp)) {
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(88.dp).clip(CircleShape).background(Mint.copy(alpha=.12f)).border(1.dp,Mint.copy(alpha=.5f),CircleShape),contentAlignment=Alignment.Center) {Glyph("check",Modifier.size(40.dp),Mint)}
        Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {Text("¡Tiempo bien registrado!",style=MaterialTheme.typography.headlineMedium);Text("${durationText(entry.seconds)} en ${project?.name.orEmpty()}",color=Muted,fontSize=14.sp)}
        PanelCard {
            listOf("Usuario" to vm.session!!.name,"Día" to entry.day.format(DateTimeFormatter.ofPattern("d MMM yyyy",Spanish)),"Tarea" to entry.task).forEach {(label,value)->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(20.dp)) {Text(label,Modifier.width(62.dp),color=Muted,fontSize=13.sp);Text(value,Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.Medium)}
            }
            HorizontalDivider(color=Line)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {Text("Tiempo dedicado",color=Muted,fontSize=13.sp);Badge(durationText(entry.seconds))}
        }
        Text("El resumen y tu widget ya están actualizados.",color=Mint,style=MaterialTheme.typography.bodySmall)
        Primary("Volver al inicio","home") {vm.screen="home"}
        Secondary("Registrar otra tarea","plus") {vm.openEntry(project=entry.projectId)}
    }
}
