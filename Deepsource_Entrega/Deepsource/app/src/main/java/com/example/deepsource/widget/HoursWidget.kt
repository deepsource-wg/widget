package com.example.deepsource.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.RemoteViews
import com.example.deepsource.MainActivity
import com.example.deepsource.R
import com.example.deepsource.data.LocalStore
import com.example.deepsource.data.SessionVault
import com.example.deepsource.domain.*
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class HoursWidget:AppWidgetProvider() {
    override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray) { async { ids.forEach {update(context,manager,it)} } }
    override fun onAppWidgetOptionsChanged(context:Context,manager:AppWidgetManager,id:Int,newOptions:Bundle) {async {update(context,manager,id)}}
    override fun onReceive(context:Context,intent:Intent) {
        super.onReceive(context,intent)
        when(intent.action) {
            REFRESH -> async {updateAll(context)}
            TOGGLE -> async {
                val session=SessionVault(context).read()
                if(session!=null) LocalStore(context).use {store ->
                    val boot=Settings.Global.getInt(context.contentResolver,Settings.Global.BOOT_COUNT,0)
                    store.toggleTimer(session,System.currentTimeMillis(),SystemClock.elapsedRealtime(),boot)
                }
                updateAll(context)
            }
            Intent.ACTION_DATE_CHANGED,Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED -> async {updateAll(context)}
        }
    }
    private fun async(block:()->Unit) {val pending=goAsync();executor.execute {try {block()} catch(e:Exception) {android.util.Log.e("DeepsourceWidget","No se pudo actualizar el widget",e)} finally {pending.finish()}}}
    companion object {
        private val executor=Executors.newSingleThreadExecutor()
        private const val REFRESH="com.example.deepsource.REFRESH_WIDGET"
        private const val TOGGLE="com.example.deepsource.TOGGLE_WIDGET_TIMER"
        fun updateAll(context:Context) {
            val manager=AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context,HoursWidget::class.java)).forEach {update(context,manager,it)}
        }
        fun pin(context:Context):Boolean {
            val manager=AppWidgetManager.getInstance(context)
            return manager.isRequestPinAppWidgetSupported && manager.requestPinAppWidget(ComponentName(context,HoursWidget::class.java),null,null)
        }
        private fun open(context:Context,route:String,request:Int,project:String?=null):PendingIntent = PendingIntent.getActivity(context,request,
            Intent(context,MainActivity::class.java).putExtra("route",route).putExtra("project",project)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        private fun broadcast(context:Context,action:String,request:Int)=PendingIntent.getBroadcast(context,request,
            Intent(context,HoursWidget::class.java).setAction(action),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        private fun update(context:Context,manager:AppWidgetManager,id:Int) {
            val views=RemoteViews(context.packageName,R.layout.hours_widget)
            views.setOnClickPendingIntent(R.id.widget_root,open(context,"home",100))
            views.setOnClickPendingIntent(R.id.widget_manual,open(context,"entry",101))
            views.setOnClickPendingIntent(R.id.widget_timer_button,open(context,"timer",102))
            views.setOnClickPendingIntent(R.id.widget_refresh,broadcast(context,REFRESH,103))
            val month=YearMonth.now()
            views.setTextViewText(R.id.widget_month,month.format(DateTimeFormatter.ofPattern("MMMM yyyy",Spanish)).uppercase(Spanish))
            val names=intArrayOf(R.id.widget_name1,R.id.widget_name2,R.id.widget_name3,R.id.widget_name4)
            val values=intArrayOf(R.id.widget_hours1,R.id.widget_hours2,R.id.widget_hours3,R.id.widget_hours4)
            val rows=intArrayOf(R.id.widget_row1,R.id.widget_row2,R.id.widget_row3,R.id.widget_row4)
            rows.forEach {views.setViewVisibility(it,View.GONE)}
            views.setViewVisibility(R.id.widget_active,View.GONE)
            views.setViewVisibility(R.id.widget_message,View.GONE)
            try {
                val session=SessionVault(context).read()
                if(session==null) {
                    views.setTextViewText(R.id.widget_total,"Tu tiempo, en orden")
                    views.setTextViewText(R.id.widget_message,"Inicia sesión para ver tus proyectos")
                    views.setViewVisibility(R.id.widget_message,View.VISIBLE)
                    views.setTextViewText(R.id.widget_manual,"Iniciar sesión")
                    views.setViewVisibility(R.id.widget_timer_button,View.GONE)
                    views.setTextViewText(R.id.widget_updated,"Sesión protegida")
                } else LocalStore(context).use {store ->
                    val entries=store.entries(session).inMonth(month)
                    val projects=store.projects(session)
                    val height=manager.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,280)
                    val timer=store.timer(session)
                    val layout=widgetLayout(height,timer!=null,context.resources.configuration.fontScale)
                    views.setViewVisibility(R.id.widget_total,if(layout.showTotal) View.VISIBLE else View.GONE)
                    views.setViewVisibility(R.id.widget_month,if(layout.showMonth) View.VISIBLE else View.GONE)
                    views.setViewVisibility(R.id.widget_updated,if(layout.showFooter) View.VISIBLE else View.GONE)
                    views.setTextViewText(R.id.widget_total,durationText(entries.totalSeconds()))
                    views.setTextViewText(R.id.widget_manual,"+ Registrar horas")
                    views.setViewVisibility(R.id.widget_timer_button,View.VISIBLE)
                    if(entries.isEmpty()) {
                        views.setViewVisibility(R.id.widget_message,if(timer==null) View.VISIBLE else View.GONE)
                        if(height<260) views.setViewVisibility(R.id.widget_month,View.GONE)
                        views.setTextViewText(R.id.widget_message,"Aún no registras horas este mes")
                    } else projects.sortedByDescending {entries.totalSeconds(it.id)}.take(layout.rows).forEachIndexed {i,p ->
                        views.setViewVisibility(rows[i],View.VISIBLE)
                        views.setTextViewText(names[i],p.name); views.setTextColor(names[i],p.color.toInt())
                        views.setTextViewText(values[i],durationText(entries.totalSeconds(p.id)))
                        // Project rows are informational; the 48dp primary action opens the project picker.
                    }
                    if(timer!=null) {
                        views.setViewVisibility(R.id.widget_active,View.VISIBLE)
                        val boot=Settings.Global.getInt(context.contentResolver,Settings.Global.BOOT_COUNT,0)
                        val ms=timer.elapsed(System.currentTimeMillis(),SystemClock.elapsedRealtime(),boot)
                        views.setTextViewText(R.id.widget_timer_label,if(timer.running) "EN MARCHA" else "EN PAUSA")
                        views.setTextViewText(R.id.widget_toggle,if(timer.running) "Pausar" else "Reanudar")
                        views.setContentDescription(R.id.widget_toggle,if(timer.running) "Pausar cronómetro" else "Reanudar cronómetro")
                        views.setChronometer(R.id.widget_chronometer,SystemClock.elapsedRealtime()-ms,"%s",timer.running)
                        views.setOnClickPendingIntent(R.id.widget_toggle,broadcast(context,TOGGLE,104))
                        views.setOnClickPendingIntent(R.id.widget_chronometer,open(context,"timer",102))
                    }
                    views.setTextViewText(R.id.widget_updated,"${session.name.substringBefore(' ')} · ${if(session.demo) "Demo · " else ""}Actualizado ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }
            } catch(_:Exception) {
                // Do not pretend that a failed local read was a successful refresh.
                views.setTextViewText(R.id.widget_total,"Revisa tus registros")
                views.setViewVisibility(R.id.widget_message,View.VISIBLE)
                views.setTextViewText(R.id.widget_message,"No se pudo leer la información. Abre la app para reintentar.")
                views.setTextViewText(R.id.widget_updated,"Toca para abrir Deepsource")
            }
            manager.updateAppWidget(id,views)
        }
    }
}
