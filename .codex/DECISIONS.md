# Decisiones tecnicas

No borrar decisiones anteriores. Si una decision cambia, agregar una nueva entrada que indique cual reemplaza.

## D-001 - Memoria persistente del proyecto

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: usar `.codex/` como fuente de verdad entre sesiones, modelos y agentes.
- Motivo: continuidad independiente del historial del chat.

## D-002 - Android nativo y Compose

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: implementar una aplicacion Android nativa en Kotlin con Jetpack Compose y Material 3.
- Motivo: acceso directo y verificable a la linterna, Google Play Billing y Google Mobile Ads, sin una capa web innecesaria.

## D-003 - Apagado seguro antes del cobro

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: tanto el boton normal como el premium apagan primero la linterna; el premium abre despues la compra y desbloquea solo una experiencia visual adicional.
- Motivo: mantener el chiste sin inducir al usuario a pagar para recuperar una funcion basica o crear un riesgo fisico y de cumplimiento.

## D-004 - Servicios reales con adaptadores de desarrollo

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: usar Google Play Billing y Google Mobile Ads en produccion, con compra simulada determinista e identificadores publicitarios oficiales de prueba en debug.
- Motivo: permitir una prueba local segura antes de que existan Play Console, productos, anuncios o cuentas de cobro configuradas.

## D-005 - Compra no consumible

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: modelar `premium_blackout_pack` como producto no consumible que elimina anuncios y habilita el apagado premium permanentemente.
- Motivo: ofrece valor real, comprensible y restaurable, sin cargos repetidos ocultos.

## D-006 - Simulación limitada al APK de prueba

- Estado: vigente.
- Fecha: 2026-08-28.
- Decisión: cuando el emulador no ofrece flash, la variante `demo` permite simular el encendido para verificar la interfaz y la compra; en teléfonos con flash controla siempre el hardware y la variante `play` nunca usa la simulación.
- Motivo: permitir QA completo sin debilitar el comportamiento real de producción.

## D-007 - Apagado al perder foco

- Estado: vigente.
- Fecha: 2026-08-28.
- Decisión: apagar y limpiar el estado transitorio en `onPause`, además de hacerlo antes de cualquiera de los dos recorridos de apagado.
- Motivo: evitar que la linterna siga encendida al cambiar de aplicación, abrir Google Play, bloquear la pantalla o cerrar la actividad.

## D-008 - Protección comercial fuera de la función básica

- Estado: vigente.
- Fecha: 2026-08-28.
- Decisión: Premium elimina anuncios y agrega la presentación especial, pero el apagado normal permanece visible, gratuito e inmediato.
- Motivo: conservar el tono humorístico con consentimiento claro y sin bloquear una función esencial detrás de un pago.

## D-009 - Repositorio público y APK bajo demanda

- Estado: vigente.
- Fecha: 2026-08-28.
- Decisión: mantener el repositorio público sobre `main`, con CI rápido de pruebas, cobertura y lint; generar APKs únicamente por pedido explícito o para probar o reparar el pipeline de build.
- Motivo: facilitar la publicación del código sin gastar tiempo ni recursos construyendo artefactos Android en cada cambio.

## D-010 - Telegram sin mención a Nico

- Estado: vigente.
- Fecha: 2026-08-28.
- Decisión: los mensajes de Telegram que acompañan artefactos de Linterna Premium mencionan únicamente al propietario `@galerazo34` y nunca a Nico.
- Motivo: Linterna Premium es un proyecto personal del propietario.

## D-011 - Informacion comercial al confirmar

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: mantener el boton principal de Apagado Premium centrado en el chiste y mostrar precio, compra unica y ausencia de suscripcion en un dialogo inmediatamente anterior a la simulacion local o a Google Play.
- Motivo: simplificar la pantalla principal sin ocultar las condiciones comerciales en el momento de decidir la compra.

## D-012 - Celebracion Premium con apagado garantizado

- Estado: vigente; reemplaza parcialmente D-003 para usuarios que ya poseen Premium.
- Fecha: 2026-08-28.
- Decision: el apagado normal y el Premium sin licencia siguen apagando de inmediato; con Premium activo, ejecutar una curva lenta de potencia de menos de tres segundos junto con fuegos artificiales en pantalla y apagar el flash en un bloque `finally`. Usar niveles reales solo cuando Android y el hardware los exponen; en el resto conservar el LED estable y simular el pulso en la interfaz hasta el apagado final.
- Motivo: convertir Premium en una experiencia visible y graciosa sin introducir destellos rapidos ni permitir que el flash quede encendido al fallar, cancelar o salir de la app.

## D-013 - Restablecimiento limitado a demo

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: permitir borrar la licencia Premium simulada desde la pantalla apagada unicamente en la variante `demo`; persistir el regreso a la edicion mortal y reactivar anuncios de prueba. Ocultar la accion y rechazar su ejecucion en la variante Play.
- Motivo: repetir localmente los recorridos con y sin Premium sin fingir que una compra real puede revocarse o reembolsarse desde la aplicacion.

## D-014 - Mantener CI liviano y Gradle ejecutable

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: conservar el CI publico de pruebas, cobertura y lint sin build de APK; versionar `apps/mobile/android/gradlew` con modo `100755` para Linux.
- Motivo: los cinco fallos iniciales fueron `spawnSync ./gradlew EACCES`, no agotamiento de minutos. Corregir el permiso elimina la causa sin desactivar una verificacion util ni agregar trabajo costoso.

## D-015 - PREMIUM en mayusculas en el nombre visible

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: usar `Linterna PREMIUM` como nombre visible en launcher, interfaz, metadatos y documentacion publica; conservar `com.linternapremium.app`, `linterna-premium` y la URL del repositorio.
- Motivo: reforzar visualmente el chiste sin romper la identidad tecnica, las instalaciones ni las integraciones existentes.

## D-016 - Terminologia plebeya

- Estado: vigente; reemplaza la terminologia visible de D-013.
- Fecha: 2026-08-28.
- Decision: usar `EDICIÓN PLEBEYA`, `Restablecer edición plebeya` y `Apagar linterna como un plebeyo` en la experiencia activa; eliminar `mortal/mortales` de interfaz, mensajes y documentacion vigente.
- Motivo: adoptar el tono humoristico elegido por el usuario con concordancia natural en español.

## D-017 - Catálogo de nueve idiomas en la pantalla principal

- Estado: vigente.
- Fecha: 2026-08-28.
- Decision: ofrecer en la pantalla principal un selector persistente con Español, English, Português, Français, Italiano, Deutsch, Русский, 日本語 y 简体中文; resolver desde el mismo catálogo todo texto visible, mensajes de plataforma, Billing, anuncios de prueba y accesibilidad.
- Motivo: igualar el alcance lingüístico vigente de Tivio y Galerazo Bot sin agregar otra pantalla ni dejar recorridos parcialmente traducidos.

## D-018 - APK interna release con identidad QA compartida

- Estado: vigente; reemplaza la identidad de distribución interna descrita en D-006/D-009, pero no cambia la variante de desarrollo ni la firma futura de Play.
- Fecha: 2026-08-28.
- Decision: Apps Dashboard debe generar Linterna como `demoRelease`, no depurable y firmada con el keystore QA local estable de Tivio/A la Altura suministrado por variable de entorno. El archivo permanece ignorado y `playRelease` usa una firma de producción independiente.
- Motivo: conservar una identidad de instalación estable entre versiones internas y reducir advertencias repetidas asociadas a paquetes/certificados nuevos, sin publicar una clave ni confundir QA con producción.

## D-019 - Catálogo regional de 21 opciones y menú acotado

- Estado: vigente; reemplaza D-017.
- Fecha: 2026-08-28.
- Decision: ofrecer las 18 opciones reales del catálogo de Galerazo Bot más mandarín, cantonés y coreano, distinguiendo español argentino/español de España, portugués brasileño/portugués de Portugal y chino simplificado/tradicional. Euskera/vasco y Runa Simi/quechua son una sola opción cada uno. Mantener todos los textos visibles, errores, Billing, anuncios de prueba y accesibilidad dentro del mismo catálogo. Anclar el dropdown al botón de idiomas dentro del extremo derecho, limitarlo a 360 dp de alto y habilitar scroll interno.
- Motivo: igualar el catálogo fuente y sumar las variantes pedidas sin duplicar nombres de una misma lengua ni permitir que un menú largo se desplace a la izquierda o sobrepase la pantalla.

## D-020 - Confirmacion Premium sin apagado y sincronizacion del flash externo

- Estado: vigente; reemplaza D-003 y la parte del recorrido sin licencia de D-012. D-007 conserva vigencia para salida y segundo plano.
- Fecha: 2026-08-28.
- Decision: mantener el flash encendido al abrir la confirmacion Premium y al cancelarla con `Ahora no`; apagarlo con la opcion plebeya, al completar una compra mediante la secuencia Premium o al pasar la app a segundo plano/cerrarla. Observar `CameraManager.TorchCallback` durante la vida de la actividad para reflejar inmediatamente un flash encendido desde Android y liberar el callback al destruirla. Una compra exitosa con el flash ya apagado no debe volver a encenderlo.
- Motivo: preservar el remate del flujo Premium hasta que exista una decisión efectiva, reflejar el estado físico aunque la app no haya encendido el LED y mantener un cierre seguro.

## D-021 - Flash persistente fuera de la actividad

- Estado: vigente; reemplaza D-007 y la regla de salida/segundo plano de D-020.
- Fecha: 2026-08-28.
- Decision: no apagar ni limpiar el estado del flash en `onPause`, al ir a Inicio, cambiar de app, abrir Google Play ni cerrar la actividad. Solo la accion plebeya, la secuencia Premium completada o el propio sistema deben apagarlo. Mantener la secuencia Premium activa al perder foco para que complete su apagado; cancelarla en `onDestroy` solo si todavia estaba ejecutandose, confiando en su `finally`. No agregar un servicio persistente para intentar sobrevivir a la muerte del proceso.
- Motivo: la persistencia del flash fuera de la app forma parte central del chiste; Android puede apagarlo al matar el proceso, reclamar la camara o aplicar sus propias restricciones y ese limite se acepta.

## D-022 - Contraste entre apagado plebeyo y celebracion Premium

- Estado: vigente; amplía D-012 y D-016.
- Fecha: 2026-08-31.
- Decision: el apagado plebeyo debe volver directamente al estado listo sin aviso de exito. La celebracion Premium debe concentrar el feedback visual con once fuegos artificiales, confeti y monedas, luces de marquesina pulsantes y tres rodillos que terminan en 7·7·7, además del pulso y la curva de potencia existentes. Mantener las luces decorativas por debajo de tres cambios por segundo y evitar estrobos de pantalla completa.
- Motivo: hacer deliberadamente austera la salida gratuita y convertir el Premium en un remate mucho más absurdo y gratificante sin introducir destellos rápidos innecesarios.

## D-023 - Finales de linea deterministas

- Estado: vigente.
- Fecha: 2026-08-31.
- Decision: versionar `.gitattributes` con LF canonico para texto, CRLF explicito para `.bat` y formatos de imagen, Java archive y keystore marcados como binarios.
- Motivo: evitar falsos fallos o reescrituras masivas de formato entre el desarrollo en Windows y el CI en Linux.

## D-024 - SDK Android aislado y fijado en CI

- Estado: reemplazada por D-025.
- Fecha: 2026-08-31.
- Decision: ejecutar CI con `ANDROID_HOME` y `ANDROID_SDK_ROOT` apuntando a `${{ runner.temp }}/android-sdk`, instalando de forma explicita plataforma Android 36 y build-tools 36.0.0. Conservar `compileSdk`/`targetSdk` 36 hasta una migracion probada, sin desactivar `OldTargetApi` ni reducir `warningsAsErrors`.
- Motivo: `ubuntu-latest` incorporo una plataforma 37 ajena al workflow; Android Lint la descubrio y fallo todos los runs por `OldTargetApi` pese a que el job solicitaba API 36 y las pruebas/cobertura pasaban. Un SDK limpio hace reproducible el conjunto de plataformas sin ocultar errores reales.

## D-025 - Inicializacion del SDK aislado durante la ejecucion

- Estado: vigente; reemplaza la forma de inicializacion de D-024 y conserva su SDK/API fijados.
- Fecha: 2026-08-31.
- Decision: crear el SDK aislado en un paso Bash usando `$RUNNER_TEMP` y exportar sus rutas mediante `$GITHUB_ENV` antes de `setup-android`; no usar el contexto `runner` en `jobs.<job>.env`.
- Motivo: GitHub valida el contexto de `jobs.<job>.env` antes de asignar un runner; `${{ runner.temp }}` en ese nivel invalido por completo el workflow `33411847184` y no llego a crear jobs.

## D-026 - Fiesta Premium prolongada e independiente del flash

- Estado: vigente; amplia D-012 y D-022 sin prolongar la regulacion fisica.
- Fecha: 2026-08-31.
- Decision: mantener la curva del flash y su brillo representativo dentro de los primeros tres segundos, pero prolongar la capa festiva a 15 segundos. Repetir cinco rondas de fuegos, confeti y marquesina; mostrar una felicitacion traducida a todo el catalogo y una tragamonedas central más cuatro compactas repartidas por la pantalla, todas terminando en 7·7·7.
- Motivo: dejar que el flash quede apagado enseguida y conservar durante más tiempo el remate visual que el usuario aprobo, sin convertir la prolongacion en parpadeos fisicos ni en un estrobo de pantalla completa.

## D-027 - Casino audiovisual sintetizado y cancelable

- Estado: vigente; amplia D-026.
- Fecha: 2026-08-31.
- Decision: mantener toda la fiesta Premium en 15 segundos y la linterna apagada dentro de los primeros 3. Elevar el contenido a 144 piezas de confeti/monedas, 36 premios flotantes y nueve tragamonedas. Sintetizar en memoria una pista PCM mono de 15 segundos con clics de rodillo, monedas, campanadas y acordes de jackpot; reproducirla mediante `AudioTrack` con uso multimedia/juego, sin permiso nuevo ni archivo de audio versionado. Cancelar y liberar el audio al volver a encender, restablecer demo o destruir la actividad.
- Motivo: sumar el feedback de casino pedido por el usuario con sincronizacion exacta, volumen controlado por Android y sin añadir una descarga, credencial, permiso sensible ni audio que sobreviva a la experiencia visible.

## D-028 - Fanfarria, ovacion y hurra sintetizados

- Estado: vigente; amplia D-027 sin cambiar la duración.
- Fecha: 2026-08-31.
- Decision: añadir a cada una de las cinco rondas una fanfarria de cuatro notas con timbre de trompeta, una ovacion filtrada y un coro sintetizado de `¡hurra!`; reforzar la quinta ronda con una trompeta aguda que se extiende hasta el segundo 15. Conservar la pista en PCM local, el canal multimedia, la cancelacion existente y el apagado fisico dentro de los primeros 3 segundos.
- Motivo: aumentar la sensación de victoria y premio pedida por el usuario sin depender de TTS, descargas o grabaciones externas, y sin prolongar ni hacer parpadear el flash.

## D-029 - Cargar AudioTrack estático antes de exigir READY

- Estado: vigente; corrige el diagnóstico preliminar de silencio registrado el 2026-08-31.
- Fecha: 2026-09-01.
- Decisión: aceptar `STATE_NO_STATIC_DATA` después de construir un `MODE_STATIC`; escribir la pista completa y comprobar `STATE_INITIALIZED` después. Registrar errores, liberar el track en el `finally` de su coroutine propietaria y dirigir los botones de volumen al canal multimedia sin subirlo automáticamente.
- Motivo: el código 0.4.6 descartaba todas las pistas recién creadas antes del primer write porque confundía un estado estático vacío válido con una inicialización fallida. No se demostró un rechazo por tamaño del búfer ni por volumen. El protocolo puro ahora tiene pruebas de orden y de errores; la comprobación en altavoz real queda para la siguiente APK solicitada.

## D-030 - Festejos reales escalonados y fuentes licenciadas fuera de Git

- Estado: vigente; amplía D-027/D-028 y reemplaza el requisito de no usar recursos grabados.
- Fecha: 2026-09-01.
- Decisión: incorporar los siete efectos Mixkit aprobados (531, 459, 437, 2012, 2011, 1934, 1928), excluyendo el 462 rechazado. Preparar recortes PCM de 0,85–1,4 segundos, fades de 30/140 ms y cinco tandas con máximo un premio más una voz/grupo. Conservar el remate sintetizado en el último segundo de cada ronda, con pausas entre tandas y sin fondo continuo. No cambiar los 15 segundos ni el flash/visual.
- Decisión de distribución: la licencia permite el producto creativo final pero prohíbe efectos aislados o con archivos fuente; mantener MP3 y PCM fuera del repositorio público. Versionar receta, URLs, SHA-256 de fuentes/recortes y documentación. Preparar localmente con FFmpeg 9.0 y verificar los siete recursos antes de builds/install solicitados; CI rápido no descarga ni redistribuye clips.
- Motivo: sumar los festejos reales elegidos sin superponer todos los audios ni publicar una biblioteca de terceros. Las tragamonedas son decorativas, sin apuestas ni premios monetarios; un cambio de ese alcance exige volver a revisar las condiciones.

## D-031 - Cadena continua con clips completos

- Estado: vigente; reemplaza la secuenciación, recortes y pausas de D-030, pero conserva sus siete fuentes aprobadas y reglas de distribución.
- Fecha: 2026-09-01.
- Decisión: convertir cada clip completo desde offset cero, reproducirlo hasta su final natural y solapar el siguiente antes de su cola audible. Repetir festejos para sostener los 15 segundos sin silencios programados, conservar la pista sintetizada original a menor ganancia como continuidad y excluir siempre el 462. Ningún cue puede empezar si no termina completo antes del segundo 15.
- Motivo: el usuario aprobó los sonidos pero rechazó los huecos entre ellos; una cadena superpuesta mantiene la celebración constante sin mutilar voces, premios ni tragamonedas y conserva margen contra clipping.

## 2026-10-03 · Novedades en respuesta al APK

- Decisión del usuario: texto con todos los cambios de cada app después de confirmar el APK; si excede Telegram, el primero responde al archivo y cada continuación al texto anterior. Regla global vigente para todos los proyectos actuales/futuros que distribuyen APKs.
- Un único publicador central cubre los cinco perfiles de Apps Dashboard. Notas revisadas en RELEASE_NOTES.json, identidad app/versión/build validada antes de envío y registro por app/chat/plataforma; congelar partes iniciadas y persistir confirmaciones para continuar fallos sin subir el mismo archivo. No usar mensajes genéricos, logs Git ni inventar entrega auténtica/producción.
- Cambio de comportamiento del dashboard 0.2.16; archivos de notas de los cinco productos son documentación de builds existentes, sin cambios de app ni incremento móvil. No reconstruir ni redistribuir APKs para anunciar los tres recibos confirmados de Nico; completar sus respuestas sobre los originales. Cerrar sólo tickets totalmente implementados y verificados; los retests físicos y proveedores reales pendientes conservan bloqueos.

## 2026-10-16 · Ampliación verificable de idiomas y RTL

- Inventario congelado: 251 candidatos de los listados públicos de Google. Se conservan las 21 opciones/códigos/textos previos, incluidos gn/quz. Se incorporan 229 catálogos automáticos completos: 250 opciones en cada app. No representa todas las lenguas ni recursos de Internet.
- Catálogos locales sin red ni fallback de cadenas; fuentes, entrada efectiva/reformulaciones y hashes en docs/LANGUAGE_INVENTORY.json. La integridad técnica no es revisión nativa. Se bloquea jam: los recursos consultados devolvieron fragmentos fuente que no pueden validarse como criollo sin revisión especializada.
- Separar enum, catálogo puro y SharedPreferences para probar todas las claves y ampliar el umbral JVM del 100% al dominio y localización pura. El adaptador Android, Compose y SDK externos conservan verificación Android fuera de la instrumentación JVM. Lotes de 20 idiomas evitan límites de métodos JVM/DEX.
- Aplicar LocalLayoutDirection al idioma seleccionado, sin depender del idioma del teléfono; preservar UI, audio, variantes y preferencias. Una sola subida de patch/build por entrega. Dashboard original, firma demoRelease y Codex Logs autorizados; no Play, no webs ni CI remoto monitoreado.

- Medición ampliada final: dominio, modelos, contratos y localización pura, seis métricas JaCoCo al 100% (instrucciones, ramas, líneas, métodos, complejidad y clases). Sólo se excluyen SharedPreferences dependiente de Android y `$DefaultImpls` generado por Kotlin. Pruebas de límites del sintetizador verifican recorte defensivo antes/después del búfer; sin modificar audio ni quitar guardas.

## 2026-10-16 · Notas de APK y límite del harness Android

- RELEASE_NOTES debe ordenar builds únicos de mayor a menor, conservar historial y comenzar con la versión/build de app.json. El primer intento rechazado no publicó archivos; una prueba local reproduce el orden incorrecto y ahora forma parte de npm test. Corregir sólo notas/tests no exige nueva versión ni recompilar; compartir los APK existentes y verificar recibos.
- El lote de idioma consta de250 opciones, no certifica traducción nativa ni exhaustividad de Internet. jam sigue bloqueado. Las pruebas puras100% no sustituyen UI/lector/insets ni hardware.
- API36 y API33 locales fallan en System UI antes de poder ejecutar la matriz. Mantener ese harness deshabilitado hasta evidencia de reparación o pedido explícito de retest; no atribuir el ANR a la app ni contar casos como aprobados. Fallback compatible usado: JVM/catálogos, lint, metadatos/dex/firma y recibos del pipeline. Windows SetIsBorderRequired/0x80004002 sigue sin reintentarse.

## 2026-10-16 · Reparación y retest solicitados

- Pedido explícito autoriza retomar el harness Android bloqueado. Diagnosticar System UI y validar ambas apps con ADB/instrumentación, preservando perfiles y datos con respaldos. No reintentar captura nativa Windows incompatible. Sin nuevos envíos Telegram autorizados por este pedido.


## 2026-10-16 · Hallazgos de UI reproducidos

- La captura con texto 200% y paisaje demuestra confirmación Premium sin precio/ayuda visibles y acciones superpuestas en ambas apps. Sustituir el layout fijo de AlertDialog por Dialog con toda la confirmación y acciones en un único flujo desplazable; conservar textos y cancelación.
- El selector marca visualmente el idioma pero AccessibilityNodeInfo.selected es false. Exponer selected por opción; conservar nombre, idioma y preferencia. Barras del sistema siempre claras sobre el tema oscuro. Gemidos identifica el apagado gratuito como botón accesible.
- Snapshot restore confirmado incompatible en este entorno: ADB permanece offline tras restaurar premium-systemui-ready; no repetir sin evidencia de reparación. -prop rechaza propiedades ro.* (sólo qemu.*), por lo que el multiplicador no se aplicó. Datos y respaldos de AVD preservados. Recuperación puntual de System UI comprobada tras arranque; no equivale a corregir el fallo de arranque en frío.

## 2026-10-16 · QA final en ejecución

- Corregidas alturas rígidas: textos de acciones completos con alemán/español al 200%. Gemidos mueve apagado gratuito a barra explícita dentro de safeDrawing; visible durante sonido y celebración, con rol Button y error inmediatamente encima. Cancelar también la coroutine Premium al apagar para evitar que termine sobre el nuevo estado.
- Celebraciones recortadas al contenido seguro del Scaffold; una región live polite anuncia el mensaje completo y oculta símbolos decorativos al lector. Priorizar felicitación visual sobre rodillos en vistas compactas.
- APK candidatos compilados, lint y gates puros100% aprobados. Matriz final:11 perfiles/33 capturas por app y auditoría de geometría sin errores; incluye controles fijos. Escenas compactas200% Linterna pasan ambas orientaciones. Gemidos/RTL/teclado/TalkBack siguen en ejecución, sin contarlos completos. Test de volumen ahora fija y comprueba baseline5 en el AVD aislado, no presupone estado de un proceso de test forzado.

## 2026-10-16 · Cierre del QA Android

- Usar AOSP API36 default/x86_64 rev2 en Premium_QA_API36, sólo emulador y paquetes demo.debug. Launcher con preflight de memoria/compiladores; stop/framework/ro.hw_timeout_multiplier=3/start verificado, tiempos finitos y animator1. Dos arranques fríos funcionan; no equivale a certificar rendimiento de producción. No se cierra ninguna app del usuario.
- Perfil Google Play original sigue ANR KeyguardService20002ms: ro.debuggable0 y sin servicio console impiden aplicar la configuración root probada. La ruta documentada -shell tampoco está disponible en esa imagen; deshabilitada sin nuevos reintentos hasta evidencia concreta de reparación. Restaurar config respaldado, conservar datos/default_boot y eliminar sólo snapshot diagnóstico/helper propios. Windows nativo y restauración de snapshots siguen deshabilitados.
- Once perfiles por app/33 capturas pasan geometría runtime, objetivos44dp y revisión visual de acciones finales; quince RTL por app, en/zu por callbacks reales, último menú, persistencia y teclado aprobados. Escenas compactas200% en ambas orientaciones pasan; una región polite y figuras decorativas ocultas. TalkBack16 real conserva servicios (DONT_SUPPRESS), foco, acciones y rol Button virtual; no afirmar voz/pronunciación ni cobertura lingüística nativa.
- Ajustar tests al contrato real: SharedPreferences.apply persiste de forma asíncrona, checked representa selected del menú y Compose puede exponer Button en un hijo virtual no clicable. Verificar rol alrededor de la etiqueta y actuación en su padre, sin cambiar código sano para satisfacer una suposición del harness.
- Gemidos: reproducir carrera de tarea Premium al apagar en menos de1,5s; cancelar premiumSilenceJob antes de apagar. Regresión falla en APK anterior y pasa con nuevo APK (581ms), cuenta regresiva conservada y volumen5 restaurado. Sin cambiar audios, catálogos,15s de fiesta ni dependencias.
- QA manual optativo fuera de CI; suites puras100% y lint/build locales correctos. Recibos portables públicos y evidencias crudas locales ignoradas. Una sola subida patch/build por producto y entrega; sin APK Telegram nuevo ni monitoreo CI. Ambos AVD detenidos, accesibilidad/volumen/insets de QA restaurados.

## 2026-10-17 · Reparación del AVD original programada por el usuario

- Pedido directo: «Arreglá también el AVD Google Play original, no lo dejes apartado. pero a las 11 de la mañana». Retomar el17 de octubre a las11:00, Buenos Aires (UTC-3), en este mismo chat, una sola ejecución. No ejecutar ahora el diagnóstico ni arrancar emuladores.
- Reemplaza la decisión de dejar AppAlimentos_Pixel_API36 apartado como resultado definitivo. Premium_QA_API36 conserva su QA válido, pero no satisface esta reparación adicional. Preservar el perfil Google Play original y sus datos; actualizar componentes compatibles con respaldo cuando sirva a la reparación.
- Automatización heartbeat activa reparar-avd-google-play-original, vinculada a este chat, una única ejecución a esa hora. No crea trabajos diarios, no envía APKs/Telegram ni monitorea CI. Mantener PC encendida y Codex abierto para trabajo local.
- Los fallos conocidos de snapshots, -prop ro.* y -shell siguen documentados; el nuevo pedido requiere resolver la causa por una vía compatible, no repetir mecanismos sin evidencia de una corrección. No utilizar captura nativa Windows incompatible. Si la reparación sigue fallando, informar evidencia y el siguiente impedimento concreto sin presentarlo como reparado.

## 2026-10-17 · Validación posterior obligatoria en Google Play original

- Ampliación explícita del usuario: una vez reparado AppAlimentos_Pixel_API36, repetir allí la validación visual, accesibilidad, RTL e insets de Linterna PREMIUM y Gemidos PREMIUM. Se incorpora a la misma ejecución única de las11:00, Buenos Aires; no crear otra automatización ni adelantar la ejecución.
- Repetir el protocolo previo de11 perfiles/33 capturas y15 RTL por app, TalkBack real, teclado, persistencia/menú y flujos gratuitos/Premium, incluidos volumen y cancelación rápida de Gemidos. Identificar el AVD y los APK probados en evidencia/recibos nuevos; la aprobación AOSP no sustituye este retest.
- Adaptar guards del harness sólo al AVD original autorizado, conservando la exclusión de teléfonos y sin ejecutar preparación root exclusiva de AOSP en la imagen user. No declarar completo el pedido sólo porque arranque System UI; completar el retest o registrar sus bloqueos exactos.

## 2026-10-17 · Preparación del original sin arranque

- El usuario respondió «Seguí con las tareas que no requieren iniciar el AVD» ante el preflight con menos de2 GiB libres; la medición llegó a unos400 MiB. No arrancar ahora ningún AVD ni cerrar aplicaciones ajenas. La reparación y el retest original quedan BLOCKED por esta restricción y recursos, sin nueva automatización o monitor.
- Respaldar íntegro AppAlimentos_Pixel_API36, identidad y Emulator36.6.11 en %LOCALAPPDATA%/CodexAvdBackups/AppAlimentos_Pixel_API36-20261017. Contrastar SHA-256 del userdata y configuración previa. Actualizar componente estable a37.2.12: notas oficiales37.1.11 corrigen unidades RAM/heap (526671872), aplicable a2G/228M. Es candidato concreto; no demuestra corregir ANR sin arranque.
- Desactivar sólo Quick Boot/snapshots automáticos en config.ini, con hardware/imagen GooglePlay API36rev7/datos preservados. No reintentar -shell, -prop ro.*, snapshots ni captura Windows incompatible. No usar preparación root AOSP en la imagen user.
- Harness con selección explícita de los dos AVD autorizados, comparación de nombre real y serial, exclusión de teléfonos, paquete demo.debug propio y metadatos de origen por captura. Ocho tests offline por repo, también bajo Python -O; las suites Android completas requieren Python normal. No cambian producto/versión/APK.
- Mantener recibo AOSP16/10 como histórico; original0/11 perfiles,0/33 capturas,0/15 RTL por app y0/2 boots en esta ejecución. Informar límites exactos en docs/AVD_GOOGLE_PLAY_REPAIR.md, no aprobar por preparación offline. JVM/JaCoCo/lint/build no repetidos por RAM; baseline anterior no se presenta como nuevo.

- Reconciliación posterior17/10 13:55: se observó instancia original -read-only/puerto5582 creada13:41; no consta lanzamiento en historial de esta tarea y propietario no confirmado. Dejar intacta por exclusión de procesos ajenos; no usar para QA ni afirmar ambos AVD detenidos. Esta observación corrige el estado supuesto anterior, sin aprobar reparación.

## 2026-10-19 · Librerías Android compatibles

- Pedido explícito de Lucas desde En la misma: actualizar librerías en todas las apps donde sea compatible. Entrega 0.4.11/19 con seis familias estables, conservando minSdk24/compileSdk36, AGP8.13.2, Kotlin2.3.21 y Compose2026.06.01. Billing9.1.0 y UMP4.0.0 ya coinciden con últimos estables del registro Google.
- Inspección oficial de AAR/POM: Activity1.13/Core1.18 requieren compile36/AGP8.9.1; Splash1.2 compile35/AGP8.6; Fragment1.9.1/Lifecycle2.11 compile34/AGP8.1.1. Ads25.5 sube minSdk a24, ya existente en ambas apps. Core1.19.1 y Compose2026.08/09 requieren compile37/AGP9.1: no forzar migración de toolchain. Registro Maven no publica BOM2026.07; Junio01 es el último compatible inspeccionado.
- Changelogs oficiales: https://developer.android.com/jetpack/androidx/releases/activity ; https://developer.android.com/jetpack/androidx/releases/core ; https://developer.android.com/jetpack/androidx/releases/fragment ; https://developer.android.com/jetpack/androidx/releases/lifecycle ; https://developers.google.com/admob/android/rel-notes . Validar Node/JVM/JaCoCo100%/lint/build antes de publicar; no AVD, servicios pagos, Telegram o tiendas.

## 2026-10-19 · Límite transitivo de Lifecycle

- Sustituye sólo la selección Lifecycle2.11.0 del bloque anterior: aunque runtime-android declara compile34, su alineación de grupo fuerza lifecycle-runtime-compose-android2.11.0, que requiere compile37/AGP9.1. Gradle checkDemoDebugAarMetadata lo rechazó antes de compilar. Seleccionar Lifecycle2.10.0, cuyo AAR Compose requiere compile36/AGP8.9.1, y repetir sólo tras esta corrección concreta. No elevar SDK/AGP ni desactivar el guard de metadatos. Las otras cinco familias conservan versiones candidatas.


## Aclaración de metadatos y validación · 2026-10-19

- El AAR lifecycle-runtime-compose-android2.10.0 declara minCompileSdk35 y AGP8.6, no36/8.9.1 como se transcribió en el bloque anterior. Es compatible con compile36/AGP8.13.2. Gradle verificó la resolución completa tras rechazar2.11 por SDK37. Mantener Activity1.13/Core1.18/Splash1.2/Fragment1.9.1/Lifecycle2.10/Ads25.5, Kotlin/Compose/Billing/UMP intactos.
- Revisión de corrección, simplicidad, arquitectura, seguridad y rendimiento: cambios limitados a dependencias y metadatos, sin nuevos permisos ni lógica de cobro.52JVM y seis gates JaCoCo1.00, lint y APK demoDebug pasan; no atribuir esto a QA visual/física o servicios reales. Secret Scanning y Push Protection verificados habilitados19/10.
