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

