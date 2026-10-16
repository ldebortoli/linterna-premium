# Linterna PREMIUM - Contexto del proyecto

## Descripcion general

Aplicacion Android humoristica de linterna. Enciende el flash fisico con la maxima intensidad disponible, ofrece un apagado normal gratuito y un "Apagado Premium" que muestra una confirmacion comercial antes de Google Play y desbloquea fuegos artificiales con una secuencia gradual del flash. Incluye publicidad mediante Google Mobile Ads sin bloquear la funcion principal.

## Estado detectado

- Stack: Android nativo, Kotlin, Jetpack Compose, Material 3, Gradle.
- Nombre visible: `Linterna PREMIUM`; el package id y el slug tecnico permanecen `com.linternapremium.app` y `linterna-premium`.
- Git: repositorio público `https://github.com/ldebortoli/linterna-premium` en rama principal `main`, con Secret Scanning y Push Protection habilitados.
- Android: `minSdk 24`, `compileSdk 36`, Java 17 como bytecode objetivo.
- Integraciones: Google Play Billing para el producto no consumible `premium_blackout_pack`; Google Mobile Ads con identificadores de prueba solo en builds de desarrollo.
- Privacidad: no hay cuentas, backend propio ni captura directa de tarjetas. Google Play procesa el pago.
- Versión actual: 0.4.9 / versionCode 17.
- Idiomas: 250 opciones persistentes, incluidas las 21 previas sin cambios y 229 catálogos de traducción automática completa por claves; 15 escrituras RTL. Inventario portable y limitaciones en docs/LANGUAGE_INVENTORY.json y docs/IDIOMAS.md. Sin revisión nativa afirmada; jam no publicado por fragmentos fuente.
- Dashboard: perfil Android nativo `Linterna PREMIUM` registrado en Apps Dashboard; distribuye `demoRelease` no depurable con la identidad QA estable compartida con Tivio/A la Altura y reserva `playRelease` para publicación.
- Telegram: los mensajes de artefactos generados por Apps Dashboard mencionan sólo al propietario `@galerazo34` y nunca a Nico.

## Estructura

- `apps/mobile/android/`: aplicacion Android.
- `apps/mobile/app.json`: metadatos portables usados por los controles de version y el Dashboard.
- `apps/mobile/package.json`: comandos de desarrollo expuestos al Dashboard.
- `docs/`: configuracion de Play Console, AdMob, privacidad, pruebas y entrega.
- `tools/`: controles locales de version y calidad.

## Ejecucion y tests

- Validacion rapida: `npm test` desde la raiz.
- Pruebas Android: `npm run test:android`.
- Cobertura: `npm run coverage`.
- Lint: `npm run lint`.
- APK de prueba: se genera desde Apps Dashboard o, cuando el usuario lo pide expresamente, con `npm run build:apk`.
- CI público: pruebas, cobertura y lint sobre `main` y pull requests; no genera APKs. Usa un SDK aislado bajo `runner.temp` con plataforma/build-tools 36 para que Lint no dependa de plataformas preinstaladas en `ubuntu-latest`.
- El wrapper Linux `apps/mobile/android/gradlew` se versiona como ejecutable (`100755`) para que GitHub Actions pueda iniciar Gradle.
- `.gitattributes` fija LF para texto, conserva CRLF en scripts `.bat` y marca formatos binarios para que los checkouts de Windows y Linux sean deterministas.
- `.gitignore` excluye `.kotlin/` además de los directorios de Gradle/build para no versionar cachés locales del compilador.
- Baseline verificado: 45 pruebas locales (44 puras más una integración con clips licenciados preparados), 100% de instrucciones/ramas/líneas/métodos del dominio y Android Lint sin observaciones. La integración de clips se omite explícitamente en CI público sin assets; no se descargan audios durante CI.

## Convenciones

- Preservar cambios ajenos y secretos locales.
- Actualizar este archivo solo cuando cambie informacion estable.
- La memoria persistente vive en `.codex/` y se carga siguiendo `AGENTS.md`.
- La version canonica vive en `version.properties`; `app.json` y Gradle deben coincidir.
- Ningun identificador real de anuncios, producto o credencial se versiona como secreto. Los valores publicables de Google se inyectan por propiedades de Gradle.
- El apagado normal corta la linterna inmediatamente. Abrir o cancelar la confirmacion Premium, ir a Inicio, cambiar de app, abrir Google Play o cerrar la actividad conservan el flash encendido mientras Android mantenga el recurso; una compra exitosa inicia la secuencia Premium y garantiza el apagado final. Con Premium ya activo, esa misma secuencia lenta de menos de tres segundos regula la potencia cuando el hardware lo permite.
- El apagado plebeyo no muestra aviso posterior. La celebracion Premium audiovisual dura 15 segundos aunque la curva fisica y su reflejo visual terminen en los primeros 3: repite cinco rondas de once fuegos artificiales, 144 piezas de confeti/monedas, 36 regalos/diamantes/coronas/premios y luces suaves de marquesina; muestra una felicitacion localizada y distribuye una tragamonedas central más ocho compactas que terminan en 7·7·7. El audio encadena completos desde offset cero los siete festejos reales Mixkit aprobados (531, 459, 437, 2012, 2011, 1934, 1928; nunca 462), repite los necesarios y solapa el siguiente antes de la cola audible del anterior. Conserva durante los 15 segundos la pista sintetizada original a menor ganancia: no hay recortes, fades ni descansos programados, y ningún clip se interrumpe al final. Usa multimedia sin permisos y respeta el volumen/ruteo del usuario. Las luces pulsan por debajo de tres cambios por segundo y no usan un estrobo de pantalla completa.
- La licencia de los efectos impide publicarlos junto con fuentes: MP3/PCM quedan ignorados; `audio/premium-sfx.json`, `tools/prepare-premium-audio.mjs` y `docs/AUDIO_LICENSES.md` conservan receta, procedencia, cantidad de muestras y SHA-256. Ejecutar `npm run audio:prepare -- --download` o usar `--source-dir` con las preescuchas aprobadas antes del primer build local; requiere FFmpeg 9.0. Los builds/install mediante los comandos del proyecto exigen `audio:verify` correcto. Tests/lint no requieren los clips.
- AudioTrack estático se carga antes de exigir `STATE_INITIALIZED`: `STATE_NO_STATIC_DATA` es válido después de crear el track. Los errores quedan en logcat como `PremiumCelebrationAudio`; la coroutine libera la pista al terminar/cancelarse y la fiesta no excede 15 segundos.
- La variante `demo` simula la linterna únicamente cuando el dispositivo no tiene flash, para poder probar el recorrido en emulador; la variante `play` nunca simula hardware.
- La variante `demo` permite borrar la licencia simulada y volver a la edicion plebeya; la variante `play` no expone ni ejecuta este restablecimiento.
- La elección de idioma se guarda localmente; la primera apertura usa el idioma y la región compatibles del dispositivo y cae a español argentino cuando no coincide con el catálogo. El menú se ancla debajo del botón de idiomas, limita su alto a 360 dp y permite desplazamiento interno.
- La app observa el estado real del flash mediante `CameraManager.TorchCallback`: si Android ya lo tenia encendido al abrir o volver a la app, sincroniza la pantalla y muestra directamente ambos apagados.
- `demoRelease` usa por variable de entorno el keystore QA local de Tivio, ignorado por Git; `playRelease` no reutiliza esa identidad y debe configurarse con la firma de producción de Google Play.

## Novedades de APK · regla 2026-10-03

- RELEASE_NOTES.json en la raíz conserva cambios revisados por versión/build y prefijo de artefactos. Apps Dashboard central publica esas notas después de confirmar cada archivo, como respuesta al APK y continuaciones en cadena cuando superan el límite de Telegram; incluye versiones no anunciadas para esa app/chat/plataforma.
- Codex Bot verifica identidad, destino, permisos, archivo y cada respuesta. Credenciales cifradas; recibos y progreso locales fuera de Git; reintentos continúan partes pendientes sin repetir el APK. Sólo avanzar la versión anunciada al confirmar la cadena completa. El transporte no garantiza exactamente una vez si se pierde la respuesta HTTP después de la aceptación remota.
- Preparar una entrada por cada nueva iteración distribuible y mantener el historial. Esta documentación no cambia el comportamiento ni versión del producto móvil; el cambio de publicador se entrega en Apps Dashboard 0.2.16.

- Validación desde ampliación 2026-10-16: 52 pruebas JVM Android sin omitidas; 100% de seis métricas JaCoCo en dominio/modelos/contratos/localización pura, con exclusiones explícitas del adaptador Android SharedPreferences y puentes Kotlin generados. Lint correcto. Catálogos y procedencia/hashes se controlan también desde npm test.
