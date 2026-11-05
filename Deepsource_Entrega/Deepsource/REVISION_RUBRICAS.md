# Revisión de rúbricas de Deepsource

La Parcial 2 evalúa el encargo en pareja y pesa 8 % de la asignatura. La Parcial 3 pesa 32 % y evalúa la defensa individual de la misma app. Los porcentajes de las tablas son los pesos dentro de cada pauta.

## Parcial 2

| Indicador | Peso | Evidencia en Deepsource |
| --- | ---: | --- |
| IE 2.1.1 Interfaz, jerarquía y navegación | 15 % | Material 3, navegación inferior, encabezados y acciones principales diferenciadas. El contenido se limita a 640 dp en pantallas anchas. Los formularios se desplazan con el teclado y se etiquetaron acciones de solo ícono para accesibilidad. |
| IE 2.1.2 Formularios y retroalimentación por campo | 15 % | Inicio de sesión, registro, creación de proyecto, registro y edición de horas, cronómetro y edición de perfil tienen mensajes junto a los campos. `FieldError` muestra el ícono de error y abre su mensaje al tocarlo. El correo repetido y la contraseña actual incorrecta se informan en su campo. |
| IE 2.2.1 Validación centralizada y estado | 10 % | `AuthValidation.kt`, `FormValidation.kt`, `Models.kt` y `Passwords.kt` concentran las reglas. Los Composables muestran errores según el estado. La base vuelve a comprobar el límite total de 24 horas por día dentro de la transacción. |
| IE 2.2.2 Animaciones funcionales | 10 % | `FieldError` usa `AnimatedVisibility` al mostrar u ocultar el mensaje y `BalanceCard` usa `animateFloatAsState` cuando cambia el total visible. Los indicadores de progreso informan trabajo en curso. |
| IE 2.3.1 Modularidad y persistencia local | 15 % | UI, ViewModel, repositorios y dominio separados. `HoursRepository` ejecuta operaciones de datos en IO. `LocalStore` usa SQLite y permite guardar, consultar, editar y eliminar registros. |
| IE 2.3.2 Colaboración | 20 % | README preparado con espacios para el repositorio y el tablero. La evidencia se acredita con el GitHub público real, historial de ambos integrantes, issues y tareas asignadas en Trello o equivalente. Completa los datos del equipo y verifica la entrega en AVA. |
| IE 2.4.1 Dos recursos nativos | 15 % | Cámara del sistema y selector de fotos integrados en Perfil. El avatar muestra el resultado y la foto queda guardada por cuenta. El widget y el selector de documentos para CSV también se pueden mostrar. |

La Parcial 2, página 3, establece una nota 1,0 si el repositorio se modifica después de la entrega y antes de la evaluación. Verifica qué versión autorizó el docente para la defensa y conserva la copia enviada por AVA.

## Parcial 3

| Indicador | Peso | Cómo practicar la demostración |
| --- | ---: | --- |
| IE 2.1.1 Validación y modificación en vivo | 20 % | Intenta guardar un registro con tarea de dos caracteres. Toca el ícono para mostrar el mensaje. Localiza `validateTask` y practica modificar el rango y el mensaje en una copia. |
| IE 2.1.2 Diseño visual y navegación | 7 % | Recorre Inicio, Registros y Perfil. Explica los niveles de títulos, botones primario/secundario, controles reutilizables y el tema Material 3. Prueba un formulario con el teclado abierto y gira el emulador. |
| IE 2.2.1 Incorporación modular | 22 % | Practica un Composable pequeño con parámetros en `ui/` y úsalo desde una pantalla. Mantén el acceso a datos en `data/` y las reglas en `domain/`. |
| IE 2.2.2 Estado, controles y UI | 7 % | Abre `AppViewModel` y sigue `save` → `reload` → `records` → resumen. Explica `mutableStateOf`, el `when` de rutas y el `if` que selecciona mensajes o contenido. |
| IE 2.3.1 Almacenamiento y cambio en vivo | 18 % | Crea un registro, edítalo, cierra y reabre la app y búscalo por tarea. Sigue `HoursRepository.saveEntry` a `LocalStore.saveEntry` y explica el `INSERT` para ID nuevo y el `UPDATE` para ID existente. Para practicar, ajusta temporalmente el `ORDER BY` de `entries` y observa el cambio con varios registros. |
| IE 2.3.2 Arquitectura y colaboración | 7 % | Explica qué hace cada capa de MVVM y por qué el ViewModel delega las operaciones de SQLite al repositorio. Prepara el repositorio y tablero reales del equipo. |
| IE 2.4.1 Integración y permisos | 7 % | Muestra `CAMERA` en el manifiesto y la solicitud `RequestPermission` al tocar **Tomar foto**. Explica que `TakePicture` se ejecuta cuando Android concede el permiso y que `FileProvider` comparte la URI de la captura. `PickVisualMedia` sigue disponible si se deniega la cámara. |
| IE 2.4.2 Resultado visible del recurso | 12 % | Toma una foto y luego reemplázala con una de la galería. Muestra el avatar. Practica cambiar su tamaño de 64 dp a 80 dp desde `ProfileScreen` y explica `ContentScale.Crop`. |

### Orden para ensayar en 15 minutos

1. **1 min.** Ejecuta `localDebug`, abre **Explorar demostración** y entra como Luis.
2. **2 min.** Identifica Screens, ViewModel, Repository, Model y SQLite.
3. **1 min.** Recorre las pantallas y justifica navegación y jerarquía.
4. **2 min.** Muestra el formulario de horas con errores y después guarda una tarea válida.
5. **1 min.** Sigue el cambio de estado del ViewModel hasta la UI.
6. **1 min.** Muestra un mensaje de error animado y el balance al cambiar de mes.
7. **2 min.** Edita el registro, reabre la app y comprueba el dato guardado.
8. **2 min.** Toma una foto y reemplázala desde la galería. Explica cómo llega al avatar.
9. **3 min.** Practica las modificaciones que pueden pedirte. Cambia una validación, un Composable y una operación de lectura, cada uno en su capa.

El Word y esta guía sirven para estudiar antes de presentar. La Parcial 3, página 5, excluye IA y materiales de apoyo durante la defensa.

### Relación con la PPT de clase

AdoptaUnCompa sirve de referencia para formularios Material 3, estado que sobrevive a la rotación, animaciones, cámara y galería. Deepsource aplica ese flujo a horas de proyectos y al perfil. Usa `mutableStateOf` y `rememberSaveable` para la interfaz, `SQLiteOpenHelper` para los registros y archivos privados para las fotos. La rúbrica acepta el mecanismo de persistencia implementado. DataStore es un bonus de la actividad de clase.

### Verificación técnica

- Se compilaron `localDebug` y `cloudDebug` con el Gradle Wrapper y JDK 21. La autenticación remota necesita un proyecto Firebase y un cliente OAuth configurados para probarla.
- Pasaron 20 pruebas unitarias y 7 pruebas instrumentadas en un emulador Android 17. Las pruebas incluyen cuentas locales, sesión, registros SQLite, límite diario, cronómetro y persistencia de fotos por cuenta.
- El APK del ZIP es la variante `localDebug` firmada para depuración y permite abrir la app directamente en un dispositivo Android.

Se revisaron **Ev. Parcial 2 Estudiante Encargo DSY1105**, páginas 3 a 6, **Evaluación Parcial 3 Estudiante Presentación DSY1105**, páginas 2 a 8, y **Clase Integradora AdoptaUnCompa**, niveles 1 a 4.
