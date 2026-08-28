# Session handoff

## Objetivo general

Entregar Linterna Premium como aplicacion Android nativa mantenible, verificable y lista para que el usuario pruebe antes de configurar sus cuentas comerciales.

## Tarea actual

Configurar las cuentas e identificadores reales de Google cuando el usuario decida avanzar con la publicación comercial.

## Estado actual

- Android nativo Kotlin/Compose implementado en versión 0.1.0 (versionCode 1).
- Repositorio público en `https://github.com/ldebortoli/linterna-premium`, rama principal `main`, con Secret Scanning y Push Protection habilitados.
- El APK de prueba está en `apps/mobile/android/app/build/outputs/apk/demo/debug/app-demo-debug.apk`; usa compra simulada explícita y anuncios oficiales de prueba.
- Google Play Billing y Google Mobile Ads quedan separados por variantes hasta que existan cuentas e identificadores reales.
- Apps Dashboard reconoce el perfil Android nativo y puede generar/archivar el APK.
- Validación más reciente: `npm test`, `npm run coverage` y `npm run lint`; 13/13 pruebas, 100% de instrucciones/ramas/líneas/métodos del motor y Android Lint correcto. No se generó un APK para el cambio de visibilidad/CI.
- CI público sobre `main` y pull requests ejecuta pruebas, cobertura y lint, con cancelación de corridas reemplazadas y sin build de APK.
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
- Los identificadores reales y la firma de producción no deben versionarse; Secret Scanning y Push Protection están habilitados como segunda barrera.
