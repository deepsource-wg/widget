# Deepsource

Deepsource es una app Android en Kotlin y Jetpack Compose para registrar horas de trabajo por proyecto. Permite ingresar horas manualmente, medir tiempo con un cronómetro y consultar balances mensuales. La variante **localDebug** funciona con cuentas y datos guardados en el dispositivo.

## Integrantes y colaboración

- Vicente Jose Duran Leal
- Segundo integrante pendiente de completar por el equipo

| Evidencia de la Parcial 2 | Información del equipo |
| --- | --- |
| Repositorio público de GitHub con commits de ambos integrantes | Completar con el enlace real |
| Tablero con tareas, responsables y estados | Completar con el enlace real de Trello o equivalente |
| Número de equipo en AVA | Completar con el número asignado |

## Ejecutar en otro PC

1. Extrae el ZIP a una carpeta local, por ejemplo `C:\Proyectos`. En Android Studio abre la carpeta **Deepsource** que contiene `settings.gradle.kts`.
2. Usa **Android Studio Quail 4 (2026.1.4) o una versión compatible con AGP 9.4**. En **Tools > SDK Manager**, instala **Android 17 (API 37)**. En **SDK Tools**, instala **Android SDK Build-Tools 36.0.0** y **Android SDK Platform-Tools**. Instala **Android Emulator** si vas a usar un equipo virtual.
3. En **Settings > Build, Execution, Deployment > Build Tools > Gradle**, selecciona **Gradle JDK**. La compilación se verificó con **JDK 21**. La ruta debe apuntar al directorio del JDK, por ejemplo `C:\Program Files\Java\jdk-21.0.11`. Android Studio también puede ofrecer un JDK incluido.
4. Con internet, espera el primer **Gradle Sync**. El Wrapper incluye los scripts, el JAR y la versión de Gradle. Gradle resuelve las bibliotecas declaradas en `app/build.gradle.kts` y `gradle/libs.versions.toml`. Android Studio genera el `local.properties` del equipo con la ubicación de su SDK.
5. En **View > Tool Windows > Build Variants**, elige **localDebug** en `app`. En **Tools > Device Manager**, crea un emulador con cámara trasera **Emulated**, o conecta un celular Android con **depuración USB**. La app requiere Android 8.0 (API 26) o posterior.
6. Selecciona el dispositivo y pulsa **Run ▶**. Desplázate hacia abajo en el login y abre **Explorar demostración > Entrar como Luis · DEV** para usar proyectos existentes. Elige **Crear cuenta local** si quieres probar el registro desde cero.

Ejecuta estos comandos en PowerShell desde la carpeta que contiene `gradlew.bat`.

```powershell
.\gradlew.bat :app:assembleLocalDebug :app:testLocalDebugUnitTest
.\gradlew.bat :app:connectedLocalDebugAndroidTest
```

El segundo comando necesita un dispositivo o emulador conectado. Si aparece `SDK location not found`, vuelve a abrir el proyecto en Android Studio o define `ANDROID_HOME` con la ruta de tu SDK. El APK que genere Gradle queda en `app/build/outputs/apk/local/debug/app-local-debug.apk`.

### APK de respaldo

El ZIP incluye `Deepsource-localDebug.apk` junto a la carpeta **Deepsource**. Con el emulador iniciado, arrastra el APK a su ventana. También puedes instalarlo con Platform-Tools.

```powershell
adb devices
adb install -r .\Deepsource-localDebug.apk
```

Ejecuta esos comandos desde la carpeta que contiene el APK y con un solo dispositivo conectado. También puedes abrir el APK en un celular Android y permitir la instalación desde la app que utilizaste para abrirlo. Si el dispositivo tiene una versión de Deepsource firmada con otra clave, usa un emulador limpio u otro dispositivo para conservar los datos de la instalación previa. Para modificar código en la defensa, abre el proyecto en Android Studio y ejecútalo desde el IDE.

## Funciones y archivos principales

| Ruta | Responsabilidad |
| --- | --- |
| `MainActivity.kt` y `ui/` | Navegación, pantallas de Material 3, formularios y animaciones. |
| `AppViewModel.kt` | Estado observable de la sesión, proyectos, registros, cronómetro y foto de perfil. |
| `domain/` | Modelos, validaciones, cálculos de horas y formateo de CSV. |
| `data/HoursRepository.kt` y `data/LocalStore.kt` | Operaciones de proyectos, registros y cronómetro con SQLite. |
| `data/AuthRepository.kt`, `LocalAccounts.kt` y `SessionVault.kt` | Cuentas locales, autenticación opcional en la nube y sesión cifrada. |
| `ui/ProfilePhotoPanel.kt` y `data/ProfilePhotos.kt` | Cámara, selector de fotos y almacenamiento privado por cuenta. |
| `widget/HoursWidget.kt` | Widget del escritorio con resumen y acceso al cronómetro. |

La variante `cloudDebug` usa configuración propia de Google y Firebase. La ejecución de `localDebug` es independiente de esa configuración. La revisión de ambas rúbricas y la secuencia para practicar la defensa están en [REVISION_RUBRICAS.md](REVISION_RUBRICAS.md). Los detalles de cada flujo están en [INSTRUCCIONES.md](INSTRUCCIONES.md).

Consulta la [compatibilidad de Android Studio y AGP](https://developer.android.com/build/releases/about-agp) y los [requisitos de AGP 9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes).
