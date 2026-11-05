package com.example.deepsource.domain

internal fun validateName(name: String): String? =
    if (name.trim().length in 3..80) null else "Ingresa un nombre de 3 a 80 caracteres"

internal fun validateTask(task: String): String? =
    if (task.trim().length in 3..160) null else "Describe la tarea en 3 a 160 caracteres"

internal fun validateProjectSelection(projectId: String?): String? =
    if (projectId.isNullOrBlank()) "Selecciona un proyecto" else null

internal data class ProjectErrors(
    val name: String?,
    val client: String?,
    val estimate: String?
) {
    val valid get() = name == null && client == null && estimate == null
}

internal fun validateProject(name: String, client: String, estimate: Int?): ProjectErrors =
    ProjectErrors(
        name = validateName(name),
        client = if (client.trim().length in 2..60) null else "Ingresa un cliente de 2 a 60 caracteres",
        estimate = if (estimate != null && estimate in 1..10000) null else "Ingresa entre 1 y 10000 horas"
    )

internal data class TimerErrors(val project: String?, val task: String?) {
    val valid get() = project == null && task == null
}

internal fun validateTimer(projectId: String?, task: String): TimerErrors =
    TimerErrors(validateProjectSelection(projectId), validateTask(task))
