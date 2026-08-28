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
