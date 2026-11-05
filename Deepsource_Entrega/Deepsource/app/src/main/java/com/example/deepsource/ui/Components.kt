package com.example.deepsource.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepsource.domain.*
import com.example.deepsource.ui.theme.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun Glyph(name: String, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier.size(22.dp)) {
        val s=size.minDimension/24f
        fun p(x: Float,y: Float)=Offset(x*s,y*s)
        fun line(x:Float,y:Float,a:Float,b:Float)=drawLine(tint,p(x,y),p(a,b),1.7f*s,StrokeCap.Round)
        fun box(x:Float,y:Float,w:Float,h:Float,r:Float=2f)=drawRoundRect(tint,p(x,y),Size(w*s,h*s),CornerRadius(r*s),style=Stroke(1.7f*s))
        when(name) {
            "plus" -> { line(12f,5f,12f,19f); line(5f,12f,19f,12f) }
            "back" -> { line(19f,12f,5f,12f); line(5f,12f,11f,6f); line(5f,12f,11f,18f) }
            "next" -> { line(9f,6f,15f,12f); line(15f,12f,9f,18f) }
            "down" -> { line(6f,9f,12f,15f); line(12f,15f,18f,9f) }
            "close" -> { line(6f,6f,18f,18f); line(18f,6f,6f,18f) }
            "check" -> { line(5f,12f,10f,17f); line(10f,17f,20f,6f) }
            "error" -> {
                drawCircle(tint, 8.5f * s, p(12f, 12f), style = Stroke(1.7f * s))
                line(12f, 7f, 12f, 12.5f)
                drawCircle(tint, 1f * s, p(12f, 16f))
            }
            "clock","timer" -> { drawCircle(tint,8f*s,p(12f,13f),style=Stroke(1.7f*s)); line(12f,8f,12f,13f); line(12f,13f,16f,15f); if(name=="timer") line(9f,2f,15f,2f) }
            "play" -> drawPath(Path().apply { moveTo(8*s,5*s); lineTo(19*s,12*s); lineTo(8*s,19*s); close() },tint,style=Stroke(1.7f*s))
            "pause" -> { box(6f,5f,4f,14f,1f); box(14f,5f,4f,14f,1f) }
            "stop" -> box(5f,5f,14f,14f)
            "home" -> { line(3f,10f,12f,3f); line(12f,3f,21f,10f); line(5f,9f,5f,21f); line(19f,9f,19f,21f); line(5f,21f,19f,21f); box(10f,14f,4f,7f,0f) }
            "chart" -> { line(4f,20f,21f,20f); line(6f,16f,6f,11f); line(12f,16f,12f,5f); line(18f,16f,18f,8f) }
            "history" -> { box(5f,3f,14f,18f); line(8f,8f,16f,8f); line(8f,12f,16f,12f); line(8f,16f,13f,16f) }
            "widget" -> { box(3f,3f,8f,8f); box(14f,3f,7f,8f); box(3f,14f,8f,7f); box(14f,14f,7f,7f) }
            "person" -> { drawCircle(tint,3.3f*s,p(12f,7f),style=Stroke(1.7f*s)); drawArc(tint,180f,180f,false,p(5f,13f),Size(14*s,12*s),style=Stroke(1.7f*s)) }
            "calendar" -> { box(3f,5f,18f,16f); line(3f,10f,21f,10f); line(8f,3f,8f,7f); line(16f,3f,16f,7f); line(8f,14f,10f,14f); line(14f,14f,16f,14f) }
            "search" -> { drawCircle(tint,6.5f*s,p(10f,10f),style=Stroke(1.7f*s)); line(15f,15f,21f,21f) }
            "mail" -> { box(3f,5f,18f,14f); line(3f,6f,12f,13f); line(12f,13f,21f,6f) }
            "lock" -> { box(5f,10f,14f,11f); drawArc(tint,180f,180f,false,p(8f,2f),Size(8*s,13*s),style=Stroke(1.7f*s)); line(12f,14f,12f,17f) }
            "eye" -> { drawOval(tint,p(2f,6f),Size(20*s,12*s),style=Stroke(1.7f*s)); drawCircle(tint,3f*s,p(12f,12f),style=Stroke(1.7f*s)) }
            "edit" -> { line(5f,16f,16f,5f); line(8f,19f,19f,8f); line(16f,5f,19f,8f); line(5f,16f,4f,20f); line(4f,20f,8f,19f) }
            "logout" -> { box(3f,3f,9f,18f); line(10f,12f,22f,12f); line(17f,7f,22f,12f); line(22f,12f,17f,17f) }
            "trash" -> { line(4f,6f,20f,6f); box(6f,6f,12f,15f); line(9f,3f,15f,3f); line(10f,10f,10f,17f); line(14f,10f,14f,17f) }
            else -> { box(3f,3f,18f,18f,5f); line(8f,16f,8f,10f); line(12f,16f,12f,7f); line(16f,16f,16f,12f) }
        }
    }
}

@Composable fun Brand(compact: Boolean=false) {
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(11.dp)) {
        Box(Modifier.size(if(compact) 34.dp else 44.dp).clip(RoundedCornerShape(13.dp)).background(Mint.copy(alpha=.12f)).border(1.dp,Mint.copy(alpha=.45f),RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center) { Glyph("brand",tint=Mint) }
        Column { Text("deepsource",fontWeight=FontWeight.Bold,fontSize=if(compact) 20.sp else 24.sp,letterSpacing=(-.7).sp)
            if(!compact) Text("TU TIEMPO, BIEN INVERTIDO",style=MaterialTheme.typography.labelSmall,color=Mint) }
    }
}

@Composable fun Avatar(session: Session, size: Int = 42, photo: Bitmap? = null) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(Violet.copy(alpha=.16f)).border(1.dp,Violet.copy(alpha=.25f),CircleShape),contentAlignment=Alignment.Center) {
        if (photo == null) {
            Text(session.initials,color=Violet,fontWeight=FontWeight.Bold,fontSize=(size*.32).sp)
        } else {
            Image(photo.asImageBitmap(), "Foto de perfil de ${session.name}", Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop)
        }
    }
}
@Composable fun Badge(text: String,color: Color=Mint) {
    Text(text,Modifier.clip(RoundedCornerShape(7.dp)).background(color.copy(alpha=.12f)).padding(horizontal=8.dp,vertical=4.dp),color=color,style=MaterialTheme.typography.labelSmall)
}
@Composable fun Primary(
    text: String,
    icon: String? = null,
    enabled: Boolean = true,
    disabledContainerColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Button(onClick,Modifier.fillMaxWidth().heightIn(min=54.dp),enabled=enabled,shape=RoundedCornerShape(15.dp),
        colors=ButtonDefaults.buttonColors(containerColor=Action, contentColor=Color.White,
            disabledContainerColor=disabledContainerColor),
        contentPadding=PaddingValues(16.dp)) {
        icon?.let { Glyph(it); Spacer(Modifier.width(9.dp)) }; Text(text,style=MaterialTheme.typography.labelLarge)
    }
}
@Composable fun Secondary(text:String, icon:String?=null,enabled:Boolean=true,onClick:()->Unit) {
    OutlinedButton(onClick,Modifier.fillMaxWidth().heightIn(min=52.dp),enabled=enabled,shape=RoundedCornerShape(15.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Line),colors=ButtonDefaults.outlinedButtonColors(contentColor=Paper)) {
        icon?.let { Glyph(it,tint=Mint); Spacer(Modifier.width(9.dp)) }; Text(text)
    }
}
@Composable fun Section(title:String,action:String?=null,onAction:()->Unit={}) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
        Text(title,style=MaterialTheme.typography.titleMedium)
        if(action!=null) TextButton(onAction,contentPadding=PaddingValues(horizontal=0.dp)) { Text(action,color=Mint,fontSize=12.sp) }
    }
}
@Composable fun PanelCard(modifier:Modifier=Modifier,content: @Composable ColumnScope.()->Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).border(1.dp,Line.copy(alpha=.45f),RoundedCornerShape(22.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
}
@Composable fun Field(value:String,onValueChange:(String)->Unit,label:String,modifier:Modifier=Modifier,
    placeholder:String="",error:String?=null,keyboard:KeyboardType=KeyboardType.Text,
    visual:VisualTransformation=VisualTransformation.None,readOnly:Boolean=false,
    leading:String?=null,trailing:(@Composable ()->Unit)?=null) {
    Column(modifier) {
        Text(label,style=MaterialTheme.typography.bodyMedium,color=Muted)
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(value,onValueChange,Modifier.fillMaxWidth().semantics { contentDescription = label }.withFieldError(error),readOnly=readOnly,singleLine=true,
            placeholder={ Text(placeholder,color=Muted.copy(alpha=.65f)) },isError=error!=null,
            shape=RoundedCornerShape(14.dp),keyboardOptions=KeyboardOptions(keyboardType=keyboard),visualTransformation=visual,
            leadingIcon=if(leading==null) null else { { Glyph(leading,tint=Muted) } },trailingIcon=trailing,
            colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Mint,unfocusedBorderColor=Line,
                focusedContainerColor=Panel,unfocusedContainerColor=Panel,focusedTextColor=Paper,unfocusedTextColor=Paper))
        FieldError(error, label)
    }
}
@Composable fun SelectField(label:String,value:String,icon:String="down",error:String?=null,onClick:()->Unit) {
    Column {
        Text(label,color=Muted,style=MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth().heightIn(min=57.dp).clip(RoundedCornerShape(14.dp)).background(Panel).border(1.dp,if(error==null) Line else Danger,RoundedCornerShape(14.dp)).clickable(onClick=onClick).semantics { contentDescription = label }.withFieldError(error).padding(horizontal=16.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically) {
            if(icon=="calendar") { Glyph(icon,tint=Mint); Spacer(Modifier.width(12.dp)) }
            Text(value,Modifier.weight(1f),color=Paper,maxLines=1,overflow=TextOverflow.Ellipsis)
            Glyph(if(icon=="calendar") "next" else icon,tint=Muted)
        }
        FieldError(error, label)
    }
}

private fun Modifier.withFieldError(message: String?): Modifier =
    if (message == null) this else semantics { error(message) }

@Composable
fun PasswordEye(visible: Boolean, fieldName: String = "contraseña", onToggle: () -> Unit) {
    IconButton(onClick = onToggle, modifier = Modifier.semantics {
        contentDescription = if (visible) "Ocultar $fieldName" else "Mostrar $fieldName"
    }) {
        Glyph("eye", tint = if (visible) Violet else Muted)
    }
}

@Composable
private fun FieldError(message: String?, fieldName: String) {
    if (message == null) return
    var expanded by remember(message) { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.semantics {
            contentDescription = if (expanded) "Ocultar error de $fieldName" else "Ver error de $fieldName"
        }) {
            Glyph("error", Modifier.size(16.dp), Danger)
        }
        AnimatedVisibility(expanded, modifier = Modifier.weight(1f), label = "Mensaje de error") {
            Text(message, color = Danger, style = MaterialTheme.typography.bodySmall)
        }
    }
}
@Composable fun Identity(s:Session) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Avatar(s); Column(Modifier.weight(1f)) { Text(s.name,fontWeight=FontWeight.SemiBold); Text("Sesión actual · ${s.role}",color=Muted,style=MaterialTheme.typography.bodySmall) }; Glyph("lock",tint=Mint)
    }
}
@Composable fun EmptyState(title:String,subtitle:String,icon:String="history") {
    Column(Modifier.fillMaxWidth().padding(vertical=22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(Violet.copy(alpha=.1f)),contentAlignment=Alignment.Center) { Glyph(icon,Modifier.size(28.dp),Violet) }
        Text(title,fontWeight=FontWeight.SemiBold); Text(subtitle,color=Muted,style=MaterialTheme.typography.bodyMedium)
    }
}
@Composable fun MonthSelector(month:YearMonth,onChange:(YearMonth)->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).padding(horizontal=6.dp),verticalAlignment=Alignment.CenterVertically) {
        IconButton({onChange(month.minusMonths(1))}, Modifier.semantics { contentDescription = "Mes anterior" }) { Glyph("back",tint=Muted) }
        Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy",Spanish)).replaceFirstChar { it.titlecase(Spanish) },Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        IconButton({onChange(month.plusMonths(1))},Modifier.semantics { contentDescription = "Mes siguiente" },enabled=month<YearMonth.now()) { Glyph("next",tint=if(month<YearMonth.now()) Muted else Line) }
    }
}
