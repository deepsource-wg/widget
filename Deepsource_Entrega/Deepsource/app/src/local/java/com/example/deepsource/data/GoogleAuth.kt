package com.example.deepsource.data

import android.content.Context

/** Google requires a configured cloud identity provider; it is never faked locally. */
object GoogleAuth {
    val available=false
    suspend fun token(activity:Context):String=error("Google se habilita en la variante cloud con Firebase y un cliente OAuth")
    suspend fun clear(context:Context)=Unit
}
