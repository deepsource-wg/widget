# Deepsource · avance y próximos pasos

**Avance aproximado del alcance funcional: 77%.** El cálculo agrupa el proyecto original en 13 bloques funcionales con igual peso. Esta medida describe el alcance de esta versión. La nota se calcula con los criterios de la rúbrica.

## Bloques incluidos en este corte (10)

1. Registro de cuenta e inicio de sesión local.
2. Creación y detalle de proyectos.
3. Registro manual de horas con validaciones.
4. Edición y eliminación de registros.
5. Cronómetro con pausa y reanudación.
6. Historial con búsqueda y filtros.
7. Resumen mensual de horas.
8. Widget de Android con resumen de horas y controles del cronómetro.
9. Perfil con edición de nombre y cambio de contraseña local.
10. Foto de perfil con cámara, permiso de Android y selector de galería.

La aplicación usa Material 3, animaciones de retroalimentación, estado observable, arquitectura MVVM y almacenamiento local con SQLite.

## Bloques pendientes en este corte (3)

| Función | Qué falta integrar |
| --- | --- |
| Resumen del equipo | Añadir al perfil líder una vista con totales de horas por proyecto e integrante. |
| Exportación CSV | Descargar los registros filtrados desde el historial para abrirlos en una planilla. |
| Acceso en la nube | Incorporar el inicio de sesión con Google, configurar Firebase y habilitar la recuperación por correo. |

## Texto para una diapositiva

**Avance del proyecto · 77% del alcance funcional**

**Implementado:** inicio de sesión local, proyectos, registro de horas, cronómetro, historial, resumen personal y widget de Android con acceso desde la pantalla de inicio. El perfil permite tomar una foto con permiso de Android o elegirla de la galería.

**Próxima etapa:** integrar el resumen de equipo para el rol líder, habilitar la exportación CSV y añadir el acceso con Google y la recuperación de contraseña con Firebase.
