# AVD Google Play original · 2026-10-17

Estado: preparación sin arranque completada; reparación y QA del original **sin verificar**. El usuario indicó continuar solamente con tareas que no requieran iniciar el AVD. Esta tarea no ordenó arrancar ningún AVD ni cerró aplicaciones ajenas. La memoria libre del host estaba por debajo de 2 GiB y llegó a unos 400 MiB; el preflight de QA requiere 5 GiB libres y ausencia de compiladores Gradle/Kotlin activos.

Al reconciliar procesos a las13:55 se observó una instancia del original en modo `-read-only`, puerto5582, creada a las13:41. No hay una orden de lanzamiento de esa instancia en el historial de esta tarea y no se confirmó su propietario; se dejó intacta. Su presencia no cuenta como reparación ni validación de las apps.

## Diagnóstico y cambios preparados

- Perfil conservado: `AppAlimentos_Pixel_API36`, imagen `system-images;android-36;google_apis_playstore;x86_64` revisión 7, Android 16/API 36, compilación user. No se recreó el AVD ni se sustituyó Google Play.
- El ANR previo de SystemUI/KeyguardService ocurre antes de abrir las apps, tras 20002 ms. El hilo main estaba Runnable: aproximadamente 3,8 s de CPU y 22,6 s esperando el scheduler; presión de CPU del huésped cercana al 79%. Esto justifica investigar recursos y emulador, pero no demuestra una causa única.
- Respaldo local completo del AVD y su archivo de identidad, más el emulador anterior para reversión, en `%LOCALAPPDATA%/CodexAvdBackups/AppAlimentos_Pixel_API36-20261017/`. `backup-hashes.json` conserva los SHA-256 locales de userdata y configuración previa. La copia de userdata se contrastó con el original antes de cambiar configuración; sus datos permanecieron intactos. No versionar este respaldo, imágenes, claves ni logs privados.
- Android Emulator actualizado de **36.6.11 a 37.2.12** (build 16428233) mediante el canal estable oficial. `-version`, `-list-avds` y `-accel-check` verificaron instalación, los tres perfiles existentes y WHPX disponible sin arrancar un AVD.
- Las [notas oficiales](https://developer.android.com/studio/releases/emulator) documentan desde 37.1.11 una corrección del parseo de unidades de RAM/heap ([526671872](https://issuetracker.google.com/issues/526671872)). El perfil usa `hw.ramSize=2G` y `vm.heapSize=228M`: existe evidencia concreta para actualizar el componente. **No se ha demostrado que esta actualización corrija el ANR.**
- Sólo se desactivó Quick Boot/restauración automática del snapshot incompatible en `config.ini`: `fastboot.forceColdBoot=yes`, `fastboot.forceFastBoot=no`, `firstboot.bootFromLocalSnapshot=no`, `firstboot.bootFromDownloadableSnapshot=no`, `firstboot.saveToLocalSnapshot=no`. RAM, heap, CPU, GPU, resolución e imagen permanecen como estaban. No se borraron snapshots ni datos.
- Recibo operativo local ignorado: `reports/google-play-repair-20261017/offline-repair.json`. Declara `avdStarted=false` y `repairVerified=false`.

## Harness y comprobaciones sin AVD

`tools/android-qa/qa.py` conserva como destino predeterminado `Premium_QA_API36`; permite seleccionar explícitamente sólo el original autorizado mediante `PREMIUM_QA_AVD`. Rechaza teléfonos, otros AVD y puertos cuyo nombre real no coincide; esos guards funcionan también con Python optimizado. Los paquetes siguen limitados al fixture `.demo.debug` del proyecto correspondiente y al probe `qa.premium`. Cada captura nueva registra el nombre real verificado del AVD y su serial.

Los ocho tests por proyecto de `tools/android-qa/tests/test_target.py` usan mocks: no llaman ADB ni inician emuladores. Comprueban selección, exclusiones, paquete, bloqueo de capturas ante un AVD distinto y trazabilidad del recibo.

```powershell
python -m unittest discover -s tools/android-qa/tests -v
python -O -m unittest discover -s tools/android-qa/tests -v
npm test
```

Resultado local, también desde checkout de índice con `core.autocrlf=true` y LF: 8/8 tests del harness por proyecto (normal y `-O`); pruebas Node: Linterna 3/3 y Gemidos 19/19. El plan sonoro puro de Gemidos mantiene 100% de líneas, ramas y funciones. Las suites JVM/JaCoCo, lint y build no se repitieron por la presión de RAM: el baseline del 16/10 sigue siendo 52 JVM en Linterna y 24 en Gemidos, seis métricas JaCoCo al 100% del núcleo puro, según `VALIDACION_ANDROID.md`. No se presenta ese baseline como ejecución nueva. No cambia producto, versión ni APK: Linterna 0.4.10/18 y Gemidos 0.1.3/4.

## QA del original pendiente

| Comprobación nueva en el original | Linterna | Gemidos |
|---|---|---|
| Perfiles de pantalla aprobados | 0/11 | 0/11 |
| Capturas requeridas | 0/33 | 0/33 |
| Variantes RTL aprobadas | 0/15 | 0/15 |
| TalkBack real, roles/foco/acciones | no ejecutado | no ejecutado |
| Teclado, selector, persistencia y extremos del menú | no ejecutado | no ejecutado |
| Escenas compactas, texto grande, insets y flujos | no ejecutado | no ejecutado |
| Volumen y regresión de apagado rápido | no aplica al audio principal | no ejecutado |

Arranques fríos de comprobación realizados en esta ejecución: **0/2**. Apertura de las dos apps y accesibilidad de System UI: no comprobadas. Los resultados AOSP del 16/10 conservan su alcance histórico y no aprueban este AVD original.

## Continuación concreta

Retomar el arranque cuando el usuario levante la restricción actual y haya recursos. No programar reintentos ni comprobaciones recurrentes. Con al menos 5 GiB libres y sin competir con Gradle/Kotlin, comprobar dos arranques fríos del mismo perfil, System UI, raíces accesibles y ausencia de nuevos ANR/crash. Conservar logs nuevos del emulador y logcat; identificar imagen, componente y APK instalados mediante SHA-256.

Para las suites normales, seleccionar el nombre y el puerto real verificado antes de cualquier instalación o mutación:

```powershell
$env:PREMIUM_QA_AVD='AppAlimentos_Pixel_API36'
$env:PREMIUM_QA_SERIAL='emulator-5554' # Sólo si éste es su puerto real verificado
$env:PREMIUM_QA_RUN='google-play-original-FECHA'
```

**No usar `start-emulator.ps1` ni `prepare-emulator.py` en el original**: son preparación exclusiva de AOSP/root. En Google Play user no sirven `adb root`, la consola `-shell` ni `-prop ro.*`; tampoco se reintenta restaurar el snapshot incompatible o la captura nativa Windows `SetIsBorderRequired/0x80004002` sin una corrección específica.

Tras confirmar reparación, repetir íntegramente el inventario de `VALIDACION_ANDROID.md` en ambos proyectos: matriz/audit, 15 RTL, escenas vertical/horizontal al 200%, teclado y selección real con persistencia, primera/última opción, TalkBack real, compra simulada/cancelación/celebración/apagado gratuito y restauración de volumen/regresión rápida de Gemidos. Examinar PNG, árboles accesibles e insets del mismo frame; no basta generar archivos. Guardar recibos nuevos identificados con el AVD original y los APK realmente probados, sin reemplazar ni reutilizar los recibos AOSP. Restaurar al final los ajustes registrados del AVD. No enviar APKs/Telegram ni monitorear CI remota.
