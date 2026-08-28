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
