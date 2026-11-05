package com.example.deepsource.domain

fun csvCell(value:String):String {
    val safe=if(value.trimStart().firstOrNull() in listOf('=','+','-','@','\t','\r')) "'$value" else value
    return "\""+safe.replace("\"","\"\"")+"\""
}

fun exportHoursCsv(entries:List<TimeEntry>,projects:List<Project>,email:String):String=buildString {
    append('\uFEFF')
    append("Fecha;Proyecto;Tarea;Duración;Segundos;Usuario;Correo;Origen\r\n")
    entries.forEach {e ->
        append(listOf(e.day.toString(),projects.firstOrNull {it.id==e.projectId}?.name.orEmpty(),e.task,
            durationText(e.seconds),e.seconds.toString(),e.userName,email,e.source).joinToString(";",transform=::csvCell))
        append("\r\n")
    }
}
