# Deepsource · versión de avance

Aplicación Android en Kotlin y Jetpack Compose para registrar horas de trabajo por proyecto. Este corte usa cuentas locales y SQLite.

## Ejecutar

1. Extrae el ZIP y abre en Android Studio la carpeta **Deepsource**, que contiene `settings.gradle.kts`.
2. Usa un Android Studio compatible con AGP 9.4, instala Android SDK API 37 y configura JDK 21 para Gradle.
3. Sincroniza Gradle y elige la variante **localDebug**.
4. Pulsa **Run** con un emulador o un dispositivo Android conectado. Para probar la cámara en el emulador, configura una cámara virtual.
5. Desde el inicio de sesión, elige **Explorar demostración → Entrar como Luis · DEV**, o crea una cuenta local.

Desde PowerShell, en la carpeta Deepsource:

```powershell
.\gradlew.bat :app:assembleLocalDebug :app:testLocalDebugUnitTest
```

Si compila correctamente, el APK queda en `app/build/outputs/apk/local/debug/app-local-debug.apk`. Este ZIP contiene el código fuente de la versión de avance. La compilación de este corte quedó pendiente de verificar porque la descarga de Gradle estaba bloqueada en el entorno de revisión.

## Prueba rápida

- Crea un proyecto, intenta guardar un registro inválido y revisa los mensajes por campo.
- Guarda un registro válido, edítalo y cierra y vuelve a abrir la aplicación. Búscalo en **Registros**.
- Inicia, pausa y termina el cronómetro para comprobar que guarda una sesión.
- En **Perfil → Tomar foto**, concede el permiso de cámara cuando Android lo solicite. Después de la captura, la foto aparece en el avatar. Si deniegas el permiso, puedes usar **Elegir de la galería**.
- Revisa el resumen mensual y el detalle por proyecto.
- En la pestaña **Widget**, selecciona **Añadir a pantalla de inicio**. Desde el escritorio, revisa el total y prueba pausar o reanudar el cronómetro.

## Organización del código

- `ui/` contiene las pantallas, los componentes y el tema Material 3.
- `AppViewModel.kt` gestiona el estado que observan las pantallas.
- `domain/` contiene modelos, cálculos y validaciones.
- `widget/` contiene el widget nativo de la pantalla de inicio.
- `data/` contiene el acceso a SQLite, las cuentas locales, la sesión y las fotos de perfil.

## Entrega del equipo

- Integrante registrado en el proyecto base: Vicente Jose Duran Leal.
- Pendientes de completar: segundo integrante, número de equipo, enlace de GitHub y enlace de Trello o equivalente.

Consulta [AVANCE_PARA_PPT.md](AVANCE_PARA_PPT.md) para el alcance y el texto de la diapositiva.
