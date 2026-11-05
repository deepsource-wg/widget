package com.example.deepsource.domain

data class WidgetLayout(val rows:Int,val showTotal:Boolean,val showMonth:Boolean,val showFooter:Boolean)

/** Reserve space for every action before adding optional project rows. */
fun widgetLayout(heightDp:Int,activeTimer:Boolean,fontScale:Float=1f):WidgetLayout {
    val height=(heightDp/fontScale.coerceAtLeast(1f)).toInt()
    val total=!activeTimer || height>=280
    val month=height>=240 || !activeTimer
    val footer=height>=260
    val used=146+(if(total) 38 else 0)+(if(month) 18 else 0)+(if(footer) 20 else 0)+(if(activeTimer) 52 else 0)
    return WidgetLayout(((height-used)/28).coerceIn(0,4),total,month,footer)
}
