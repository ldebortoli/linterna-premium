# Session handoff

## Objetivo general

Entregar Linterna PREMIUM como aplicacion Android nativa mantenible, verificable y lista para que el usuario pruebe antes de configurar sus cuentas comerciales.

## Tarea actual

Entrega 0.4.7 implementada y validada: siete festejos reales escalonados y corrección de AudioTrack. Esperar un Build App solicitado para revisar la salida de audio real; no hay tareas activas ni elementos en USER_QUEUE.

## Estado actual

- Android nativo Kotlin/Compose implementado en version 0.4.7 (versionCode 15).
- El nombre visible es `Linterna PREMIUM` en el launcher, la interfaz y `app.json`; package id, slug y URL del repositorio no cambiaron.
- Repositorio público en `https://github.com/ldebortoli/linterna-premium`, rama principal `main`, con Secret Scanning y Push Protection habilitados.
- Visibilidad publica y rama `main` verificadas directamente con GitHub el 2026-08-28.
- El boton principal de Apagado Premium ya no muestra precio ni condiciones comerciales; al tocarlo aparece una confirmacion con precio, `Compra unica · Sin suscripcion` y luego Google Play. Abrirla o elegir `Ahora no` deja el flash encendido. La variante local presenta la misma confirmacion como simulacion sin cobro y, al confirmar, inicia recien entonces la secuencia Premium que termina apagando.
- La app registra `CameraManager.TorchCallback`: si el flash estaba encendido desde Android antes de abrir o volver a la actividad, el estado se sincroniza y aparecen directamente Apagado Premium y el apagado plebeyo. El callback se libera en `onDestroy`.
- `onPause` ya no apaga ni limpia la linterna: ir a Inicio, cambiar de app, abrir Google Play o cerrar la actividad deja el flash encendido mientras Android mantenga el proceso/recurso. No hay servicio persistente; Android puede apagarlo al matar el proceso, reclamar la camara o aplicar restricciones del dispositivo.
- Con Premium activo, el boton ejecuta un pulso visual de tres segundos sincronizado con una curva del flash: baja, sube, baja, sube y desciende hasta apagarse. Android 13+ usa potencia real si el hardware expone varios niveles; el fallback conserva el LED estable, anima la pantalla y apaga al final. El apagado fisico no se prolonga por la fiesta visual.
- La celebracion Premium 0.4.7 permanece 15 segundos y repite cinco rondas con once fuegos artificiales, 144 piezas de confeti/monedas, 36 regalos/diamantes/coronas/premios y luces suaves de marquesina en los cuatro bordes. Muestra `¡Felicitaciones por tu Apagado Premium!` traducido a las 21 opciones y distribuye una tragamonedas central más ocho compactas por la pantalla; todas cambian por debajo de tres cuadros por segundo y terminan en 7·7·7. El apagado plebeyo no muestra ningun aviso posterior y vuelve directamente al estado listo.
- El audio 0.4.7 alterna los siete Mixkit aprobados (531, 459, 437, 2012, 2011, 1934, 1928; excluye 462) en cinco tandas con máximo un premio y una voz/grupo, recortes de 0,85–1,4 s, fades y descansos. Conserva los remates sintetizados originales en el último segundo de cada ronda. Precarga la mezcla al abrir y usa multimedia; no fuerza volumen ni Bluetooth. MP3/PCM no se versionan por licencia: están preparados localmente, con procedencia/receta/hashes en audio/premium-sfx.json y docs/AUDIO_LICENSES.md. Los builds/install verifican sus hashes antes de Gradle.
- La secuencia Premium sigue ejecutandose al perder foco y apaga al completar; si la actividad se destruye durante ella, se cancela y el ejecutor apaga el flash en `finally`.
- La experiencia gratuita usa `EDICIÓN PLEBEYA`, `Restablecer edición plebeya` y `Apagar linterna como un plebeyo`; no quedan usos activos de mortal/mortales.
- La pantalla principal incluye un selector persistente de 21 opciones: los 18 idiomas/variantes reales de Galerazo Bot más mandarín, cantonés y coreano. Distingue español argentino/español de España, portugués brasileño/portugués de Portugal, chino simplificado/tradicional, mandarín y cantonés; euskera/vasco y Runa Simi/quechua aparecen una sola vez cada uno. El catálogo completo cubre interfaz, avisos, errores físicos, compra, anuncios de prueba y etiquetas de accesibilidad; la primera apertura toma idioma y región compatibles y cae a español argentino.
- El dropdown se ancla al propio botón de idiomas en el extremo derecho, abre debajo en el diseño actual, limita su alto a 360 dp, mantiene un ancho de 220-320 dp y permite scroll interno para no sobrepasar la pantalla.
- Cuando Premium simulado esta activo y la linterna apagada, demo muestra `Restablecer edición plebeya`; borra la licencia local, reactiva anuncios de prueba y permite repetir el recorrido. La UI y el controlador bloquean esta accion en Play.
- Apps Dashboard generó y archivó la APK 0.4.6 el 2026-08-31 como `demoRelease`, paquete `com.linternapremium.app.demo`, `versionCode 14`, no depurable y con la identidad QA estable compartida de Tivio/A la Altura. El artefacto vigente está en `artifacts/mobile/versions/0.4.6/linterna-premium-0.4.6-android-test.apk`.
- El silencio de 0.4.6 tenía una causa de código confirmada: se exigía STATE_INITIALIZED antes del primer write, pero MODE_STATIC devuelve STATE_NO_STATIC_DATA válido hasta cargar PCM. La pista se liberaba sin escribir ni reproducir. 0.4.7 acepta el estado vacío, carga y valida después; prueba ese orden con un adaptador falso y cubre fallos de asignación/escritura/inicio. Se registran errores bajo PremiumCelebrationAudio y la coroutine propietaria libera recursos al cancelar/terminar. El diagnóstico anterior sobre rechazo por tamaño del búfer no quedó demostrado.
- Al instalar esa APK desde Telegram, Play Protect muestra una advertencia sorteable con `Instalar de todas formas`. La evidencia local descarta una firma invalida o compatibilidad Android antigua; la explicacion mas probable es paquete/certificado debug nuevos y sin reputacion, pero hace falta el texto exacto o una captura para distinguir un analisis de app desconocida de un bloqueo por verificacion de desarrollador.
- Google Play Billing y Google Mobile Ads quedan separados por variantes hasta que existan cuentas e identificadores reales.
- Apps Dashboard reconoce el perfil Android nativo `Linterna PREMIUM` y puede generar/archivar el APK.
- Apps Dashboard 0.2.9 genera para Linterna PREMIUM una leyenda Telegram que menciona sólo a `@galerazo34` y nunca a Nico.
- Validacion local de 0.4.7: npm test, audio:verify, coverage y lint; 43/43 pruebas sin fallos ni skips locales, cobertura del dominio 100% (instrucciones/ramas/líneas/métodos) y Android Lint correcto. La prueba de clips genera build/reports/audio/premium-celebration-preview.wav bajo la app: 15 s exactos y sin saturación. CI ejecuta las 42 pruebas puras y omite explícitamente solo la integración cuando no existen assets licenciados; no descarga audios. No se generó APK 0.4.7 ni se validó todavía el altavoz de un teléfono con esta versión.
- Los cinco primeros runs de CI fallaron por `spawnSync ./gradlew EACCES`, no por cuota. `apps/mobile/android/gradlew` quedo corregido de `100644` a `100755`.
- `.gitattributes` fija LF para texto, CRLF para `.bat` y trata JAR, imagenes y keystores como binarios; se verifico el control rapido desde un checkout limpio compatible con `core.autocrlf=true`.
- CI público sobre `main` y pull requests conserva pruebas, cobertura y lint, con cancelación de corridas reemplazadas y sin build de APK. Por política no se monitorea habitualmente después de cada push; el usuario autorizó una excepción puntual el 2026-08-31 para depurarlo hasta verde.
- El run `33409711980` de `6d98b5f` confirmó que contratos y cobertura pasaban, pero Lint fallaba por `OldTargetApi`: el runner tenia API 37 preinstalada aunque el workflow solicitaba API 36. El primer arreglo (`3559668`) uso `${{ runner.temp }}` en `jobs.quality.env`; GitHub rechazo el workflow `33411847184` antes de crear jobs porque el contexto `runner` no existe en ese nivel. El fix `c0be8de` crea `$RUNNER_TEMP/android-sdk` en un paso Bash, exporta `ANDROID_HOME`/`ANDROID_SDK_ROOT` mediante `$GITHUB_ENV` e instala solo plataforma 36 y build-tools 36.0.0. El run `33412352768` terminó verde con contratos, cobertura y Android Lint; no se generó APK.
- `.kotlin/` queda ignorado como caché local del compilador y no se versiona.
- QA visual previo sobre 0.1.0 y pruebas del usuario sobre APKs posteriores. La fiesta audiovisual 0.4.7 queda pendiente de escucha/ruteo en teléfono cuando el usuario solicite la siguiente APK; no confundir las pruebas de PCM/protocolo con reproducción física verificada.
- No hay elementos procesados en `USER_QUEUE.md`.

## Proximos pasos

1. Cuando el usuario lo pida, generar APK 0.4.7 desde Apps Dashboard; los siete clips ya están preparados. Probar audio con volumen multimedia audible, ruteo correcto, interrupción al encender/restablecer/cerrar y ausencia de errores PremiumCelebrationAudio en logcat.
2. Revisar la APK 0.4.7 cuando se genere: dropdown anclado, 21 opciones traducidas, apagado plebeyo sin aviso, confirmación Premium sin apagado prematuro, persistencia al salir/cambiar de app, detección del flash externo, celebración visual de 15 segundos con nueve tragamonedas y restablecimiento demo.
3. Validar el flash real y, si se quiere clasificar la advertencia de Play Protect, capturar su texto exacto.
4. Crear la app y el producto no consumible `premium_blackout_pack` en Play Console.
5. Crear la app/unidad banner en AdMob, configurar consentimiento y publicar la politica de privacidad.
6. Inyectar IDs y firma de produccion segun `docs/CONFIGURACION_GOOGLE.md`, generar `playRelease` y probar con un tester licenciado.

## Riesgos

- El cobro real queda bloqueado hasta crear la app y el producto en Play Console; debug debe permanecer sin cargos.
- Los anuncios reales quedan bloqueados hasta registrar AdMob y completar consentimiento/privacidad.
- La persistencia fuera de la actividad depende de Android: el sistema puede apagar el flash al matar el proceso, reclamar la camara o aplicar restricciones; no se usa un servicio persistente para impedirlo.
- La regulacion fisica de potencia requiere Android 13+ y un flash que informe mas de un nivel; debe validarse en hardware real y no se reemplaza por parpadeos rapidos en dispositivos incompatibles.
- El balance y la salida audible dependen del altavoz y volumen/ruteo del teléfono; el PCM real y protocolo de carga ya están verificados, pero falta probar Android/hardware con APK 0.4.7. No generar APK automáticamente.
- Los identificadores reales y la firma de producción no deben versionarse; Secret Scanning y Push Protection están habilitados como segunda barrera.
- Las APK demo distribuidas fuera de Google Play pueden seguir mostrando advertencias de Play Protect hasta usar una firma estable y acumular reputacion o distribuir la app mediante un canal de pruebas de Google Play.
