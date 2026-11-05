package com.example.deepsource.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepsource.AppViewModel
import com.example.deepsource.domain.*
import com.example.deepsource.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable fun LoginScreen(vm:AppViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var reveal by rememberSaveable { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var demo by remember { mutableStateOf(false) }
    var register by remember {mutableStateOf(false)}
    val errors = validateLogin(email, password)
    val canSubmit = !vm.busy && email.isNotBlank() && password.isNotBlank()
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF22183D),Ink,Ink))).statusBarsPadding().navigationBarsPadding().imePadding()
        .verticalScroll(rememberScrollState()).padding(horizontal=26.dp,vertical=30.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        Brand()
        Spacer(Modifier.height(10.dp))
        Badge("MENOS FRICCIÓN. MÁS ENFOQUE.")
        Text("Cada hora\ncuenta.",style=MaterialTheme.typography.displayLarge,lineHeight=52.sp)
        Text("Tus proyectos, tu tiempo y todo tu progreso.\nEn un solo lugar.",color=Muted)
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).border(1.dp,Line,RoundedCornerShape(18.dp)).padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(13.dp)) {
            Glyph("timer",Modifier.size(30.dp),Mint)
            Column { Text("De la idea al trabajo hecho",fontWeight=FontWeight.SemiBold,fontSize=14.sp); Text("Registra, mide y sigue avanzando.",color=Muted,style=MaterialTheme.typography.bodySmall) }
        }
        Text("Inicia sesión",style=MaterialTheme.typography.titleLarge)
        Text("MODO LOCAL · Cuentas y horas en este celular",color=Mint,style=MaterialTheme.typography.bodySmall)
        Field(email,{email=it},"Correo electrónico",placeholder="nombre@empresa.com",keyboard=KeyboardType.Email,error=if (submitted) errors.email else null,leading="mail")
        Field(password,{password=it},"Contraseña",placeholder="Introduce tu contraseña",keyboard=KeyboardType.Password,error=if (submitted) errors.password else null,
            leading="lock",visual=if(reveal) VisualTransformation.None else PasswordVisualTransformation(),
            trailing={ PasswordEye(reveal) { reveal = !reveal } })
        Primary(
            text = if (vm.busy) "Iniciando sesión…" else "Iniciar sesión",
            icon = "next",
            enabled = canSubmit,
            disabledContainerColor = Color(0xFF48484D)
        ) {
            submitted=true
            if (errors.valid) vm.login(email, password)
        }
        TextButton({register=true},Modifier.align(Alignment.CenterHorizontally),enabled=!vm.busy) {Text("¿Primera vez? Crear cuenta local",color=Mint)}
        TextButton({demo=true},Modifier.align(Alignment.CenterHorizontally),enabled=!vm.busy) { Text("Explorar demostración",color=Muted) }
        Text("Tu sesión se mantiene en este dispositivo.",Modifier.align(Alignment.CenterHorizontally),style=MaterialTheme.typography.bodySmall,color=Muted)
    }
    if(register) RegisterDialog(vm,{register=false})
    if(demo) AlertDialog(onDismissRequest={demo=false},title={Text("Prueba Deepsource")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Datos de ejemplo, guardados localmente. El perfil que elijas se mantiene hasta cerrar sesión.")
        Primary("Entrar como Luis · DEV",enabled=!vm.busy) { demo=false; vm.demo() }
    }},confirmButton={TextButton({demo=false}) {Text("Cancelar")}})
}

@Composable private fun RegisterDialog(vm:AppViewModel,onDismiss:()->Unit) {
    var name by rememberSaveable {mutableStateOf("")}
    var email by rememberSaveable {mutableStateOf("")}
    var password by rememberSaveable {mutableStateOf("")}
    var confirm by rememberSaveable {mutableStateOf("")}
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    var submitted by remember {mutableStateOf(false)}
    var emailInUse by rememberSaveable { mutableStateOf<String?>(null) }
    val errors = validateRegistration(name, email, password, confirm)
    AlertDialog(onDismissRequest={ if (!vm.busy) onDismiss() },title={Text("Crea tu cuenta local")},text={Column(Modifier.imePadding().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Esta cuenta funciona en este celular. No requiere internet ni representa una cuenta del sistema de la empresa.",color=Muted,style=MaterialTheme.typography.bodySmall)
        Field(name,{name=it.take(80)},"Nombre completo",error=if (submitted) errors.name else null)
        Field(email,{email=it.take(254);emailInUse=null},"Correo electrónico",keyboard=KeyboardType.Email,
            readOnly=vm.busy,error=if (submitted) errors.email ?: emailInUse else null)
        Field(password, { password = it.take(128) }, "Contraseña", keyboard = KeyboardType.Password,
            visual = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            error = if (submitted) errors.password else null,
            trailing = { PasswordEye(showPassword) { showPassword = !showPassword } })
        Field(confirm, { confirm = it.take(128) }, "Repite la contraseña", keyboard = KeyboardType.Password,
            visual = if (showConfirmation) VisualTransformation.None else PasswordVisualTransformation(),
            error = if (submitted) errors.confirmation else null,
            trailing = {
                PasswordEye(showConfirmation, "confirmación de contraseña") { showConfirmation = !showConfirmation }
            })
    }},confirmButton={TextButton({
        submitted=true
        if (errors.valid) vm.register(name, email, password) { emailInUse = it }
    },enabled=!vm.busy) {Text(if(vm.busy) "Creando…" else "Crear e ingresar")}},dismissButton={TextButton(onDismiss, enabled=!vm.busy) {Text("Cancelar")}})
}

@Composable fun DemoBanner(vm:AppViewModel) {
    if(vm.session?.demo==true) Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Violet.copy(alpha=.08f)).padding(horizontal=12.dp,vertical=9.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
        Glyph("lock",Modifier.size(14.dp),Violet); Text("DEMOSTRACIÓN · Datos en este dispositivo",color=Violet,style=MaterialTheme.typography.labelSmall)
    }
}

@Composable fun HomeScreen(vm:AppViewModel) {
    val session=vm.session!!
    val monthRecords=vm.records.inMonth(vm.month)
    val total=monthRecords.totalSeconds()
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        item { DemoBanner(vm) }
        item { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM",Spanish)).uppercase(Spanish),color=Muted,style=MaterialTheme.typography.labelSmall)
            Text("Hola, ${session.name.substringBefore(' ')}.",style=MaterialTheme.typography.headlineLarge)
            Text("Dale espacio a lo que importa.",color=Muted)
        } }
        item { HeroStats(vm,total) }
        item { Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Primary("Registrar horas","plus",!vm.busy) {vm.openEntry()}
            Secondary(if(vm.timer==null) "Abrir cronómetro" else "Volver al cronómetro","timer") {vm.screen="timer"}
        } }
        if(vm.timer!=null) item { TimerBanner(vm) }
        item { Section("Tus proyectos","Ver resumen") {vm.screen="summary"} }
        if(vm.projects.isEmpty()) item { PanelCard { EmptyState("Tu próximo proyecto empieza aquí","Crea tu primer proyecto para registrar horas.","plus"); CreateProjectControl(vm) } }
        else item { PanelCard { vm.projects.forEachIndexed { index,p ->
            ProjectRow(p,monthRecords.totalSeconds(p.id)) {vm.selectedProject=p.id;vm.screen="project"}
            if(index<vm.projects.lastIndex) HorizontalDivider(color=Line.copy(alpha=.55f))
        } } }
        if(vm.projects.isNotEmpty()) item {CreateProjectControl(vm)}
        item { Section("Registros recientes","Ver todos") {vm.screen="history"} }
        if(vm.records.isEmpty()) item {EmptyState("Todavía no registras horas","Tu primer registro aparecerá aquí.")}
        else items(vm.records.take(3),key={it.id}) {EntryCard(it,vm.projects.firstOrNull {p->p.id==it.projectId}) {vm.openEntry(it)}}
        item { Text("Hecho para mantener tu enfoque.",Modifier.fillMaxWidth().padding(top=4.dp),color=Muted,style=MaterialTheme.typography.bodySmall,textAlign=androidx.compose.ui.text.style.TextAlign.Center) }
    }
}

@Composable fun HeroStats(vm:AppViewModel,total:Long) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Color(0xFF453174),Color(0xFF2B2243)))).border(1.dp,Violet.copy(alpha=.22f),RoundedCornerShape(24.dp)).padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text("TUS HORAS · ${vm.month.format(DateTimeFormatter.ofPattern("MMMM",Spanish)).uppercase(Spanish)}",Modifier.weight(1f),color=Color(0xFFD0C3F2),style=MaterialTheme.typography.labelSmall)
            Glyph("clock",tint=Mint)
        }
        Row(verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(durationText(total),style=MaterialTheme.typography.displayLarge,fontSize=42.sp)
            Text("bien invertidas",Modifier.padding(bottom=7.dp),color=Muted,style=MaterialTheme.typography.bodySmall)
        }
        Row(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            vm.projects.forEach {p -> val seconds=vm.records.inMonth(vm.month).totalSeconds(p.id); if(seconds>0) Box(Modifier.weight(seconds.toFloat()).fillMaxHeight().background(Color(p.color))) }
            if(total==0L) Box(Modifier.fillMaxSize().background(Line))
        }
        Text("${vm.projects.size} proyectos · ${vm.records.inMonth(vm.month).size} registros este mes",color=Color(0xFFCEC5DF),style=MaterialTheme.typography.bodySmall)
    }
}

@Composable fun ProjectRow(project:Project,seconds:Long,onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick=onClick).padding(vertical=9.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(Color(project.color)))
        Column(Modifier.weight(1f)) { Text(project.name,fontWeight=FontWeight.Medium,maxLines=1,overflow=TextOverflow.Ellipsis); Text(project.client,color=Muted,style=MaterialTheme.typography.bodySmall) }
        Text(durationText(seconds),fontWeight=FontWeight.SemiBold,fontSize=14.sp); Glyph("next",Modifier.size(17.dp),Muted)
    }
}

@Composable fun EntryCard(entry:TimeEntry,project:Project?,onClick:()->Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Panel).clickable(onClick=onClick).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text(project?.name.orEmpty().uppercase(Spanish),Modifier.weight(1f),color=Color(project?.color ?: 0xFFA99BFF),style=MaterialTheme.typography.labelSmall,maxLines=1,overflow=TextOverflow.Ellipsis)
            Text(durationText(entry.seconds),fontWeight=FontWeight.SemiBold,fontSize=15.sp)
        }
        Text(entry.task,maxLines=2,overflow=TextOverflow.Ellipsis,fontWeight=FontWeight.Medium)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(entry.day.format(DateTimeFormatter.ofPattern("d MMM yyyy",Spanish)),color=Muted,style=MaterialTheme.typography.bodySmall)
            Text(entry.source,color=Muted,style=MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable fun SummaryScreen(vm:AppViewModel) {
    val entries=vm.records.inMonth(vm.month)
    val total=entries.totalSeconds()
    val estimate=vm.projects.sumOf {it.estimateMinutes}
    val percent=deviation(total,estimate)
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item {Identity(vm.session!!)}
        item {MonthSelector(vm.month) {vm.month=it}}
        item { PanelCard {
            Text("MIS HORAS",color=Muted,style=MaterialTheme.typography.labelSmall)
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {Text(durationText(total),style=MaterialTheme.typography.headlineLarge,fontSize=36.sp); Text("de ${durationText(estimate*60L)} estimadas",color=Muted,style=MaterialTheme.typography.bodySmall)}
                if(percent!=null) Column(horizontalAlignment=Alignment.End) {Text("${if(percent>0) "+" else ""}$percent %",fontSize=24.sp,fontWeight=FontWeight.Bold,color=if(percent>0) Danger else Mint);Text(if(total>estimate*60L) "Sobre estimación" else "Dentro de estimación",color=Muted,fontSize=10.sp)}
            }
        } }
        item {Section("Registradas frente a estimadas")}
        if(vm.projects.isEmpty()) item {EmptyState("Aún no hay proyectos","Crea un proyecto desde Inicio.","chart")}
        items(vm.projects,key={it.id}) {p -> BalanceCard(p,entries.totalSeconds(p.id),p.estimateMinutes)}
        item {Text("Las estimaciones se comparan con las horas del mes seleccionado. El porcentaje indica desviación de tiempo, no avance de tareas.",color=Muted,style=MaterialTheme.typography.bodySmall)}
    }
}

@Composable fun BalanceCard(p:Project,seconds:Long,estimate:Int) {
    val deviation=deviation(seconds,estimate)
    val progress by animateFloatAsState(
        targetValue = (seconds / (estimate * 60f).coerceAtLeast(1f)).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "Balance de horas"
    )
    PanelCard {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {Box(Modifier.size(8.dp).clip(CircleShape).background(Color(p.color)));Text(p.name,fontWeight=FontWeight.SemiBold)}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            Text("${durationText(seconds)} / ${durationText(estimate*60L)} est.",fontSize=14.sp)
            Badge(if(deviation==null) "Sin estimación" else "${if(deviation>0) "+" else ""}$deviation %",if((deviation ?: 0)>0) Danger else Mint)
        }
        LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),color=if(seconds>estimate*60L) Danger else Color(p.color),trackColor=Line)
    }
}

@Composable fun HistoryScreen(vm:AppViewModel) {
    var search by rememberSaveable {mutableStateOf("")}
    var project by rememberSaveable {mutableStateOf<String?>(null)}
    var picker by remember {mutableStateOf(false)}
    val rows=vm.records.inMonth(vm.month).filter { (project==null || it.projectId==project) && (it.task.contains(search,true) || vm.projects.firstOrNull {p->p.id==it.projectId}?.name?.contains(search,true)==true) }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item {MonthSelector(vm.month) {vm.month=it}}
        item {Field(search,{search=it},"Buscar registros",placeholder="Tarea o proyecto",leading="search")}
        item {SelectField("Proyecto",vm.projects.firstOrNull {it.id==project}?.name ?: "Todos los proyectos") {picker=true}}
        item { Text("${rows.size} registros · ${durationText(rows.totalSeconds())}",color=Muted,fontSize=12.sp) }
        if(rows.isEmpty()) item {EmptyState("Sin registros para esta búsqueda","Prueba otro mes o proyecto.","search")}
        items(rows,key={it.id}) {e ->EntryCard(e,vm.projects.firstOrNull {it.id==e.projectId}) {vm.openEntry(e)}}
        item {Primary("Nuevo registro","plus") {vm.openEntry()}}
    }
    if(picker) ProjectPicker(vm.projects,project,{project=it;picker=false},{picker=false},allowAll=true)
}

@Composable fun CreateProjectControl(vm:AppViewModel) {
    var show by remember {mutableStateOf(false)}
    var name by rememberSaveable {mutableStateOf("")}
    var client by rememberSaveable {mutableStateOf("")}
    var hours by rememberSaveable {mutableStateOf("32")}
    var submitted by remember { mutableStateOf(false) }

    Secondary("Crear proyecto", "plus", !vm.busy) {
        submitted = false
        show = true
    }
    if (show) {
        val estimate = hours.toIntOrNull()
        val errors = validateProject(name, client, estimate)
        AlertDialog(
            onDismissRequest = { if (!vm.busy) show = false },
            title = { Text("Nuevo proyecto") },
            text = {
                Column(
                    Modifier.imePadding().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Field(name, { name = it }, "Nombre del proyecto", readOnly = vm.busy,
                        error = if (submitted) errors.name else null)
                    Field(client, { client = it }, "Cliente", readOnly = vm.busy,
                        error = if (submitted) errors.client else null)
                    Field(hours, { hours = it.filter(Char::isDigit).take(5) }, "Horas estimadas por mes",
                        keyboard = KeyboardType.Number, readOnly = vm.busy, error = if (submitted) errors.estimate else null)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    submitted = true
                    val currentEstimate = hours.toIntOrNull()
                    if (currentEstimate != null && validateProject(name, client, currentEstimate).valid) {
                        vm.createProject(name, client, currentEstimate) {
                            show = false
                            name = ""
                            client = ""
                        }
                    }
                }, enabled = !vm.busy) { Text(if (vm.busy) "Creando…" else "Crear") }
            },
            dismissButton = { TextButton({ show = false }, enabled = !vm.busy) { Text("Cancelar") } }
        )
    }
}

@Composable fun ProjectScreen(vm:AppViewModel) {
    val p=vm.projects.firstOrNull {it.id==vm.selectedProject} ?: return
    val entries=vm.records.inMonth(vm.month).filter {it.projectId==p.id}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        item {Badge(p.client,Color(p.color));Spacer(Modifier.height(12.dp));Text(p.name,style=MaterialTheme.typography.headlineLarge)}
        item {MonthSelector(vm.month) {vm.month=it}}
        item {BalanceCard(p,entries.totalSeconds(),p.estimateMinutes)}
        item {Primary("Registrar horas en este proyecto","plus") {vm.openEntry(project=p.id)}}
        item {Secondary("Abrir cronómetro","timer") {vm.selectedProject=p.id;vm.screen="timer"}}
        item {Section("Tus registros")}
        if(entries.isEmpty()) item {EmptyState("Todavía no hay horas","Registra tu primera tarea de este proyecto.")}
        items(entries,key={it.id}) {e ->EntryCard(e,p) {vm.openEntry(e)}}
    }
}
