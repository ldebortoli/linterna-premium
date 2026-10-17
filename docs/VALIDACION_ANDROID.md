# Validación Android · Linterna PREMIUM 0.4.10/18

Fecha: 2026-10-16 (Argentina). Estado: APROBADO en el alcance emulado declarado. El recibo final portable está en `ANDROID_QA_RECEIPT.json`; capturas PNG, árboles de accesibilidad y dumps de insets quedan en `reports/android-qa-20261016-final/` (ignorados por Git).

## Actualización17/10 · original pendiente

El aprobado de este informe corresponde únicamente al AVD AOSP Premium_QA_API36 del16/10. La reparación y validación posterior del original Google Play siguen sin comprobar: se preparó Emulator37.2.12 con respaldo y guards, y el usuario indicó continuar sin iniciar el AVD. Estado, evidencia y continuación en [AVD_GOOGLE_PLAY_REPAIR.md](AVD_GOOGLE_PLAY_REPAIR.md). No se reutiliza este recibo como aprobación del original.

## Reparación del entorno

El perfil Google Play API36 presentaba ANR antes de abrir las apps: `executing service ...SystemUIService/KeyguardService`, con límite de 20 s. Aumentar RAM, cambiar GPU y compilar System UI AOT no resolvió de forma fiable su arranque frío. No atribuir ese fallo al producto.

Se creó `Premium_QA_API36` con la imagen oficial AOSP `system-images;android-36;default;x86_64` rev2, Android16, emulador36.6.11, WHPX, 3072 MB,2 vCPU, SwiftShader y720×1280/densidad320. El launcher usa arranque frío sin snapshots, comprueba5 GiB libres y evita competir con Gradle/Kotlin. En la imagen root-capable detiene el framework, establece y verifica `ro.hw_timeout_multiplier=3`, y lo reinicia. Son tiempos finitos ampliados para una VM lenta; no constituye un ensayo de rendimiento ni certificación de ANR de producción. Las animaciones del producto conservan escala1.

No reintentar mecanismos incompatibles sin evidencia de reparación: `-prop ro.*` es rechazado (sólo qemu.*); restaurar el snapshot de diagnóstico dejó ADB offline; la captura nativa Windows sigue bloqueada por `SetIsBorderRequired/0x80004002`. Se usan ADB, PNG del framebuffer y UiAutomation, sin ese mecanismo de Windows.

## Inventario de comprobaciones

| Perfil | Fuente | Orientación | Navegación | Extra |
|---|---:|---|---|---|
| es-AR |100%|vertical|gestos|base|
| ar |200%|vertical|gestos|recorte de cámara|
| ar |200%|horizontal|3 botones|recorte de cámara|
| de |200%|vertical|3 botones|etiquetas largas|
| gn, quz |100%|vertical|gestos|dos casos separados|
| bm-Nkoo |130%|vertical|gestos|RTL, N'Ko|
| hi |130%|horizontal|gestos|devanagari|
| iu |130%|vertical|gestos|silábicos|
| yua |100%|vertical|gestos|catálogo comunitario|
| es-AR compacto |200%|vertical|3 botones|320×480dp|

Once casos, tres capturas cada uno: inicio, menú y final del scroll. Se comprueba la orientación real del PNG, no sólo la rotación solicitada. La auditoría contrasta texto/etiquetas con los InsetsSource y DisplayCutout del mismo frame, nombre de controles y tamaño mínimo44dp de acciones completamente visibles; incluye la barra fija. Los anuncios WebView pertenecen al SDK y quedan fuera del tamaño/nombre de controles propios. Revisión visual representativa: etiquetas largas, compra, celebración, árabe/N'Ko, silábicos e hindi; no se afirma revisión lingüística nativa de250 catálogos.

Quince direcciones RTL verificadas al seleccionar y reiniciar: `ar, bal, bm-Nkoo, ckb, dv, fa, fa-AF, he, ms-Arab, pa-Arab, ps, sd, ug, ur, yi`. Se comprueba la posición del encabezado, etiqueta del idioma y preferencia exacta. Teclado físico Tab/Enter/Escape, primera/última opción del menú y selección real `en`/`zu` persistente tras reiniciar el proceso. No hay formularios ni entradas de texto en estas apps.

Escenas compactas200% en ambas orientaciones: encendido/sonido, apagado gratuito, cancelación, divulgaciones desplazables, compra simulada, licencia persistente y celebración. Una región live polite anuncia la felicitación dentro del área segura; los símbolos decorativos se ocultan al lector. Gemidos comprueba que el apagado esté visible sin scroll y restaure volumen5 desde un baseline fijado y verificado; además incluye una regresión Premium→plebeyo antes de1,5 s.

TalkBack real instalado desde el APK de la imagen SDK Google local (16.0.0.738667889), con UiAutomation configurado para no suprimir servicios: foco accesible, roles, selección y activación de idioma/apagado. No se evalúa voz/pronunciación: el emulador se ejecuta sin audio. El rol Button se comprueba también cuando Compose lo exporta en un hijo virtual; no se exige esa clase al padre que recibe el clic ([implementación AndroidX](https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/platform/AndroidComposeViewAccessibilityDelegateCompat.android.kt)). Contraste de cinco pares opacos del tema con umbral4,5:1; no incluye creatividades del SDK, animación ni estados deshabilitados.

## Reproducción optativa

Requiere SDK/platform/build-tools36, JDK17+ y Python3.9+ en PATH. Crear previamente el AVD AOSP llamado exactamente `Premium_QA_API36` con avdmanager. No se borra ningún perfil existente. Compilar primero y cerrar los daemons propios antes de emular; el launcher no cierra procesos del usuario.

```powershell
npm test
node tools/run-gradle.mjs testDemoDebugUnitTest jacocoTestReport jacocoTestCoverageVerification lintDemoDebug assembleDemoDebug --no-daemon -Pkotlin.compiler.execution.strategy=in-process --max-workers=2
$env:PREMIUM_QA_RUN='android-ui'
./tools/android-qa/build-probe.ps1
./tools/android-qa/start-emulator.ps1
adb -s emulator-5556 install -r reports/android-qa/harness/probe.apk
adb -s emulator-5556 install -r apps/mobile/android/app/build/outputs/apk/demo/debug/app-demo-debug.apk
python tools/android-qa/matrix.py linterna
python tools/android-qa/audit.py linterna
python tools/android-qa/rtl.py linterna
python tools/android-qa/scenes.py linterna 0
python tools/android-qa/scenes.py linterna 1
python tools/android-qa/interaction.py linterna
python tools/android-qa/contrast.py
# Con TalkBack instalado y habilitado sólo en este AVD:
python tools/android-qa/accessibility.py linterna
```

`rapid-stop.py` se ejecuta sólo en el repo Gemidos. Las pruebas modifican únicamente fixtures `.demo.debug` en el AVD aislado; nunca teléfonos. Restaurar servicios de accesibilidad, volumen, insets, fuente, rotación y navegación del AVD al terminar. Cerrar las apps normalmente antes de detenerlo. Estas suites costosas son manuales; CI conserva sólo el subconjunto rápido, cobertura y lint, sin monitoreo remoto posterior al push.

## Límites y calidad local

El umbral JaCoCo exige100% de instrucciones, ramas, líneas, métodos, complejidad y clases en dominio/modelos/contratos/localización pura. Preferencias Android, Compose, adaptadores de hardware y SDK requieren esta integración Android y no se presentan como cobertura JVM. Los puentes generados Kotlin no son código de producto. `npm test`, `npm run coverage` y `npm run lint` documentan los gates rápidos existentes; en Gemidos, Node mide también el plan puro de audio al100% de sus métricas soportadas.

La validación emulada no sustituye flash/altavoz, volumen/ruteo e interrupciones en un teléfono real, pagos/AdMob de producción ni revisión nativa de traducciones. No se envió ningún APK nuevo a Telegram ni se cambió la última versión anunciada.

Resultado local: 52 pruebas JVM sin omitidas/fallos, 3 pruebas Node, seis métricas JaCoCo al100% del núcleo puro y Lint/build correctos. SHA-256 del APK QA: `3597ba4d3db92ce79fb2a7fb0c1b128bd4da8c2a4ada2a4f801c706494d2627c`.

El perfil Google Play original permanece apartado del QA: la consola documentada `-shell` no habilita root en esta imagen user (`ro.debuggable=0`, servicio console ausente), y el último arranque volvió a registrar KeyguardService ANR20002ms. No se insistirá en esa vía ni en snapshots. Se restauró su config.ini respaldado sin borrar datos, se retiró sólo el snapshot de diagnóstico creado en esta tarea y el helper de QA; ambos emuladores quedaron detenidos. Los ajustes Android modificados durante el diagnóstico volvieron a valores normales (no se afirma recuperar valores personalizados previos que no se hubieran registrado). El AVD QA queda reproducible mediante el launcher, con dos arranques fríos verificados.

Trazabilidad: la matriz y las escenas compactas se capturaron con el mismo layout de la fuente final. Después se añadió sólo la cancelación de la coroutine de Gemidos y se ajustó indentación; tras recompilar se repitieron pruebas/cobertura/lint/build y se validaron TalkBack en ambas apps y la regresión rápida en Gemidos sobre los APK cuyos hashes figuran en el recibo. No se repitió la matriz completa por ese cambio sin efecto en layout.
