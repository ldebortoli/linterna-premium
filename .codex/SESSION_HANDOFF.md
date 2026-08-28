# Session handoff

## Objetivo general

Entregar Linterna Premium como aplicacion Android nativa mantenible, verificable y lista para que el usuario pruebe antes de configurar sus cuentas comerciales.

## Tarea actual

Implementacion inicial de UI, control de linterna, compra no consumible, publicidad, pruebas, Dashboard y entrega privada.

## Estado actual

- Memoria persistente inicializada y reconciliada el 2026-08-28.
- Repositorio local vacio al inicio; se adopta Android nativo con Kotlin/Compose.
- Google Play Billing y Google Mobile Ads se abstraen con modos de prueba seguros hasta que existan cuentas e identificadores reales.
- No hay elementos procesados en `USER_QUEUE.md`.

## Proximos pasos

1. Crear el esqueleto Android y la interfaz.
2. Implementar los adaptadores de linterna, compra y anuncios.
3. Agregar pruebas/cobertura, documentacion y control de version.
4. Integrar al Dashboard, compilar/verificar el APK y publicar el repositorio privado.

## Riesgos

- El cobro real queda bloqueado hasta crear la app y el producto en Play Console; debug debe permanecer sin cargos.
- Los anuncios reales quedan bloqueados hasta registrar AdMob y completar consentimiento/privacidad.
- Nunca conservar la linterna encendida al salir, perder foco o pulsar cualquiera de los apagados.

