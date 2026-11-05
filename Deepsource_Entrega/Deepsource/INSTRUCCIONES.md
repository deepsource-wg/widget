# Deepsource Android 1.4

Prototipo nativo para celulares Android, escrito en Kotlin y Jetpack Compose. Conserva el diseño morado y turquesa del Figma y las funciones de registro de horas de tareas informáticas.

## Empezar sin API ni Firebase

1. Extrae el ZIP actualizado.
2. Abre Android Studio y usa **File > Open**. Selecciona la carpeta `Deepsource`, que contiene `settings.gradle.kts`.
3. Instala Android 17 (API 37) y Build-Tools 36.0.0 desde **SDK Manager**. Selecciona JDK 21 para Gradle y sincroniza. La primera sincronización requiere internet para descargar las dependencias. Los pasos por menú están en [README.md](README.md).
4. En **Build Variants**, selecciona **localDebug**. Es la variante predeterminada.
5. Ejecuta la app en un celular o emulador con Android 8.0 (API 26) o posterior.
6. En el login, pulsa **Crear cuenta local**. Ingresa nombre, correo y una contraseña de 8 a 128 caracteres con al menos una letra y un número.
7. La cuenta inicia sesión al crearse. Para comprobar el login, cierra sesión y vuelve a ingresar con ese correo y contraseña. Una contraseña incorrecta se rechaza.

El correo identifica la cuenta local; no se envía un correo de verificación ni se afirma que la cuenta pertenezca al sistema de la empresa. Las cuentas creadas aquí solo existen en este dispositivo. Para ver los proyectos y perfiles de ejemplo del Figma, utiliza **Explorar demostración** desde el login.

## Flujo funcional de horas

1. En **Inicio**, crea un proyecto, indicando nombre, cliente y estimación mensual.
2. Pulsa **Registrar horas** y selecciona el proyecto, fecha, horas/minutos y tarea.
3. Guarda el registro. Se muestra un comprobante y se actualizan el resumen y el widget.
4. En **Registros**, busca por tarea o proyecto y filtra por mes/proyecto. Toca un registro para editarlo o eliminarlo con confirmación.
5. Usa **Exportar** para guardar un CSV con fecha, proyecto, tarea, duración, segundos, usuario, correo y origen del registro.
6. Para medir el trabajo, abre **Cronómetro**, elige proyecto y tarea, inicia, pausa/reanuda y termina para registrar el tiempo activo. La pausa se excluye. Los segundos se conservan al guardar y al editar.

Las horas se guardan en SQLite bajo el UID de la sesión. El usuario no se elige desde un desplegable y las cuentas no pueden escribir o borrar registros de otro UID. El límite es 24 horas acumuladas por usuario y día; no se aceptan fechas futuras. Si una sesión cruza medianoche, su duración se asigna al día en que se termina.

El cronómetro se conserva al cerrar/reabrir la app. Al cerrar sesión se pausa, sin borrar lo trabajado. Las acciones de la app y del widget vuelven a leer su estado persistido dentro de una transacción para evitar usar una pausa/reanudación antigua.

## Cámara y galería

1. En **Perfil**, pulsa **Tomar foto**. Si todavía falta el permiso, Android muestra su aviso de acceso a la cámara. Al concederlo, se abre la cámara del sistema.
2. Toma una foto y confirma el resultado. Comprueba que el avatar muestra la imagen. **Elegir de la galería** permite reemplazarla con una foto existente.
3. Si deniegas el permiso, puedes seguir usando la galería. Para volver a probar el aviso después de concederlo, cambia el permiso de cámara de Deepsource desde **Ajustes > Apps > Deepsource > Permisos** en el dispositivo de prueba.
4. Cancela uno de los dos flujos y comprueba que la imagen anterior sigue visible. Cierra y vuelve a abrir la app para comprobar la persistencia. **Quitar foto** devuelve el avatar con iniciales.

`ProfilePhotoPanel` verifica `CAMERA` y usa `RequestPermission` si falta. Con el permiso concedido, `TakePicture` abre la cámara del sistema. `FileProvider` entrega una URI temporal con acceso al archivo de captura. Por separado, `PickVisualMedia` entrega acceso solo a la imagen elegida en la galería.

`ProfilePhotos` lee y reduce la imagen fuera del hilo principal, escribe una copia privada por UID y conserva la foto anterior si falla el reemplazo. `Avatar` recorta la imagen con `ContentScale.Crop` y una máscara circular. Si el emulador no trae imágenes, arrastra un PNG o JPG a su ventana antes de abrir el selector.

## Widget del celular

Desde **Widget**, pulsa **Añadir a pantalla de inicio** y acepta el diálogo de Android. También puedes mantener pulsado el escritorio, abrir **Widgets** y buscar **Deepsource · Mis horas**.

Incluye:

- Total de horas del mes y hasta cuatro proyectos, según el espacio disponible.
- Botón **Registrar horas**, que abre el formulario con el usuario de la sesión actual.
- Acceso al cronómetro desde el icono de reloj.
- Cronómetro nativo en marcha/pausa y botón para pausar o reanudar desde el escritorio.
- Actualización al guardar, editar, eliminar, iniciar/cerrar sesión o modificar el cronómetro; también incluye actualización manual.
- Estados sin sesión, sin registros y error de lectura.
- Botones principales con áreas de toque de 48 dp y contenido opcional que se reduce al redimensionarlo o aumentar el tamaño de letra.

Las filas de proyectos son informativas. El botón principal abre el selector de proyecto. La vista dentro de la app es una previsualización; el widget del escritorio usa RemoteViews nativas y su tamaño depende del launcher.

El widget solicita además una actualización periódica cada 30 minutos; Android puede posponerla para ahorrar batería. El cronómetro visible usa `Chronometer`, sin un servicio ejecutándose cada segundo.

## Login y sesión

- Orden: correo, contraseña, **Iniciar sesión** y luego **Iniciar sesión con Google**.
- Alta local y validación real de contraseña contra el registro local.
- Contraseñas derivadas con PBKDF2-HMAC-SHA256, 210.000 iteraciones y sal aleatoria por cuenta; no se guardan en texto.
- Bloqueo local de un minuto tras cinco intentos fallidos.
- Sesión cifrada con AES-GCM y una clave de Android Keystore.
- Edición de nombre y cambio de contraseña local desde **Perfil**.
- Rol de solo lectura. Una cuenta nueva es DEV; LEADER solo está disponible en el perfil de ejemplo de la demostración.
- El correo permanece fijo. No hay selector de usuarios dentro de la sesión.

No se ofrece una recuperación de contraseña local por correo, porque no existe un servicio de envío ni identidad remota. Para el prototipo, conserva tu contraseña. El acceso con Google aparece debajo del login, pero queda deshabilitado y explicado mientras no exista una configuración de Google/Firebase.

## Qué puede recibir la empresa en esta versión

El prototipo recibe y conserva horas en el celular y permite exportarlas como CSV. **No envía automáticamente los datos a la empresa y no sincroniza varios celulares.**

Para una etapa posterior sin API propia, Firebase Authentication y una base compartida, como Firestore, pueden cubrir esas necesidades. Esta entrega no crea un proyecto Firebase ni implementa una base compartida de horas.

## Integración de autenticación en la nube preparada

La variante **cloudDebug** conserva el código de Firebase Authentication por REST y de Google mediante Credential Manager. Sus dependencias se descargan solo para esa variante. Los proyectos y registros de horas continúan siendo locales también en `cloudDebug`.

Para activar la autenticación real:

1. Registra `com.example.deepsource` en tu proyecto Firebase.
2. Activa Correo/contraseña y Google en Authentication. Crea o habilita los usuarios correspondientes.
3. Registra SHA-1/SHA-256 de la firma Android y configura un cliente OAuth de tipo **Web**.
4. Agrega estos valores en tu `gradle.properties` local:

```properties
FIREBASE_API_KEY=tu_clave_api_web_de_firebase
GOOGLE_WEB_CLIENT_ID=tu_cliente_web.apps.googleusercontent.com
```

5. Cambia a `cloudDebug`, sincroniza y ejecuta en un dispositivo con Google Play Services.

Las credenciales de Google se validan en Firebase; el botón nunca simula un inicio exitoso. No se incluyen claves privadas, cuentas de empresa ni secretos de servidor.

## Verificación

Ejecuta estos comandos desde la carpeta `Deepsource`. El primero compila la variante local y ejecuta las pruebas unitarias. El segundo requiere un dispositivo o emulador conectado.

```powershell
.\gradlew.bat :app:assembleLocalDebug :app:testLocalDebugUnitTest
.\gradlew.bat :app:connectedLocalDebugAndroidTest
```

En el login, **Iniciar sesión** queda gris hasta que ingreses correo y contraseña. En **Crear cuenta local**, cada campo de contraseña tiene su propio ojo para mostrar u ocultar el valor.

Para comprobar los formularios durante la demostración, intenta crear un proyecto con campos vacíos y cero horas, guarda un nombre de perfil demasiado corto e inicia el cronómetro sin describir la tarea. Cada campo inválido muestra un ícono **!**. Tócalo para leer el motivo. El ícono desaparece al corregir el campo. En **Resumen**, cambia entre meses con distintos registros para ver la animación del balance.

La compilación usa AGP 9.4.1, Gradle 9.6.0, plugin Kotlin/Compose 2.2.10 y SDK 37. Se verificó el Wrapper con JDK 21.

## Correcciones del Figma aplicadas

- Sin vinculación ni accesos de GitHub o Trello.
- Sin cambio de usuario mediante desplegable.
- Google debajo del inicio con correo y contraseña.
- Se conservan registro manual, selector de proyecto, calendario, validaciones, confirmación, cronómetro, resumen personal, resumen de equipo de demostración y perfil.
- Se mejora la presentación, navegación inferior, búsqueda y exportación.

