# Session handoff

## Objetivo general

Entregar Linterna Premium como aplicacion Android nativa mantenible, verificable y lista para que el usuario pruebe antes de configurar sus cuentas comerciales.

## Tarea actual

Primera entrega terminada; la siguiente etapa es configurar las cuentas e identificadores reales de Google.

## Estado actual

- Android nativo Kotlin/Compose implementado en versión 0.1.0 (versionCode 1).
- Repositorio privado publicado en `https://github.com/ldebortoli/linterna-premium`, rama `main`.
- El APK de prueba está en `apps/mobile/android/app/build/outputs/apk/demo/debug/app-demo-debug.apk`; usa compra simulada explícita y anuncios oficiales de prueba.
- Google Play Billing y Google Mobile Ads quedan separados por variantes hasta que existan cuentas e identificadores reales.
- Apps Dashboard reconoce el perfil Android nativo y puede generar/archivar el APK.
- Validación final: `npm test`, `npm run coverage`, `npm run lint` y `npm run build:apk`; 13/13 pruebas y 100% de instrucciones/ramas/líneas/métodos del motor.
- QA visual: encendido demo, apagado normal, diálogo de compra sin cobro, Premium persistente sin anuncios, apagado al perder foco y pantalla compacta con texto al 130%.
- No hay elementos procesados en `USER_QUEUE.md`.

## Proximos pasos

1. Instalar el APK de prueba en un teléfono físico y validar el flash real.
2. Crear la app y el producto no consumible `premium_blackout_pack` en Play Console.
3. Crear la app/unidad banner en AdMob, configurar consentimiento y publicar la política de privacidad.
4. Inyectar IDs y firma de producción según `docs/CONFIGURACION_GOOGLE.md`, generar `playRelease` y probar con un tester licenciado.

## Riesgos

- El cobro real queda bloqueado hasta crear la app y el producto en Play Console; debug debe permanecer sin cargos.
- Los anuncios reales quedan bloqueados hasta registrar AdMob y completar consentimiento/privacidad.
- Nunca conservar la linterna encendida al salir, perder foco o pulsar cualquiera de los apagados.
- GitHub Secret Scanning no es elegible para este repositorio privado en el plan actual: la API respondió HTTP 422; Push Protection permanece deshabilitada por esa dependencia.
