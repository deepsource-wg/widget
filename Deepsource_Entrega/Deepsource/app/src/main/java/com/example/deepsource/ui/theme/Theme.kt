package com.example.deepsource.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF141021)
val Panel = Color(0xFF211B34)
val Elevated = Color(0xFF2B2342)
val Muted = Color(0xFFADA5C1)
val Line = Color(0xFF3C3353)
val Violet = Color(0xFFA99BFF)
val Action = Color(0xFF6D4AFF)
val Mint = Color(0xFF25D9BC)
val Paper = Color(0xFFF7F4FF)
val Danger = Color(0xFFFFA6B2)

private val scheme = darkColorScheme(primary=Violet,onPrimary=Ink,primaryContainer=Action,
    onPrimaryContainer=Color.White,secondary=Mint,onSecondary=Ink,background=Ink,onBackground=Paper,
    surface=Panel,onSurface=Paper,surfaceVariant=Elevated,onSurfaceVariant=Muted,outline=Line,error=Danger)

private val type = Typography(
    displayLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=48.sp,letterSpacing=(-2).sp),
    headlineLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=30.sp,lineHeight=36.sp,letterSpacing=(-.6).sp),
    headlineMedium=TextStyle(fontWeight=FontWeight.Bold,fontSize=26.sp,lineHeight=32.sp,letterSpacing=(-.5).sp),
    titleLarge=TextStyle(fontWeight=FontWeight.SemiBold,fontSize=21.sp,lineHeight=28.sp),
    titleMedium=TextStyle(fontWeight=FontWeight.SemiBold,fontSize=16.sp,lineHeight=24.sp),
    bodyLarge=TextStyle(fontSize=16.sp,lineHeight=25.sp),
    bodyMedium=TextStyle(fontSize=14.sp,lineHeight=21.sp),
    bodySmall=TextStyle(fontSize=12.sp,lineHeight=18.sp),
    labelLarge=TextStyle(fontWeight=FontWeight.SemiBold,fontSize=14.sp),
    labelSmall=TextStyle(fontWeight=FontWeight.Medium,fontSize=11.sp,letterSpacing=.6.sp))

@Composable
fun DeepsourceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme=scheme,typography=type,
        shapes=Shapes(small=RoundedCornerShape(12.dp),medium=RoundedCornerShape(18.dp),large=RoundedCornerShape(26.dp)),content=content)
}
