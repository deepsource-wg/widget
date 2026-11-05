package com.example.deepsource.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepsource.AppViewModel
import com.example.deepsource.domain.*
import com.example.deepsource.ui.theme.*
import com.example.deepsource.widget.HoursWidget
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable fun WidgetScreen(vm:AppViewModel) {
    val context=LocalContext.current
    val entries=vm.records.inMonth(YearMonth.now())
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
        Badge("TU ENFOQUE, A UN TOQUE")
        Text("Lo esencial.\nEn tu pantalla de inicio.",style=MaterialTheme.typography.headlineLarge)
        Text("Consulta tus horas y registra tu próxima tarea sin perder el ritmo.",color=Muted)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(Brush.linearGradient(listOf(Color(0xFF2E1E51),Color(0xFF163B42)))).padding(18.dp)) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(Panel).border(1.dp,Violet.copy(alpha=.22f),RoundedCornerShape(23.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(13.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) {Text("deepsource",Modifier.weight(1f),fontWeight=FontWeight.Bold);Glyph("widget",Modifier.size(18.dp),Mint)}
                Text(YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM yyyy",Spanish)).uppercase(Spanish),color=Muted,style=MaterialTheme.typography.labelSmall)
                Text(durationText(entries.totalSeconds()),fontWeight=FontWeight.Bold,fontSize=32.sp)
                if(entries.isEmpty()) Text("Aún no registras horas este mes",color=Muted,fontSize=12.sp)
                else vm.projects.sortedByDescending {entries.totalSeconds(it.id)}.take(4).forEach {p ->
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {Box(Modifier.size(6.dp).clip(CircleShape).background(Color(p.color)));Text(p.name,Modifier.weight(1f),fontSize=12.sp);Text(durationText(entries.totalSeconds(p.id)),color=Mint,fontSize=12.sp,fontWeight=FontWeight.SemiBold)}
                }
                if(vm.timer!=null) TimerBanner(vm)
                Primary("Registrar horas","plus") {vm.openEntry()}
                Text("${vm.session!!.name.substringBefore(' ')} · Datos en este dispositivo",color=Muted,fontSize=10.sp)
            }
        }
        Primary("Añadir a pantalla de inicio","widget") {
            if(!HoursWidget.pin(context)) vm.message="Mantén pulsada la pantalla de inicio, entra a Widgets y busca Deepsource"
        }
        Secondary("Actualizar información","clock",!vm.busy) {vm.refresh()}
        PanelCard {
            Text("Pequeño por fuera. Completo por dentro.",fontWeight=FontWeight.SemiBold)
            listOf("Horas de tu sesión actual, agrupadas por proyecto.","Acceso directo al registro manual y al cronómetro.","Pausa o reanuda el cronómetro desde el widget.","Se adapta al tamaño y se actualiza al guardar.").forEach {text ->Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {Glyph("check",Modifier.size(17.dp),Mint);Text(text,color=Muted,style=MaterialTheme.typography.bodySmall)}}
        }
        Text("Vista previa de la app. El widget real usa componentes nativos del escritorio de Android y puede variar según el launcher.",color=Muted,style=MaterialTheme.typography.bodySmall)
    }
}
