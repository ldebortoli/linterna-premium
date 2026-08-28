# Session handoff

## Objetivo general

Entregar Linterna Premium como aplicacion Android nativa mantenible, verificable y lista para que el usuario pruebe antes de configurar sus cuentas comerciales.

## Tarea actual

Configurar las cuentas e identificadores reales de Google cuando el usuario decida avanzar con la publicacion comercial.

## Estado actual

- Android nativo Kotlin/Compose implementado en version 0.1.1 (versionCode 2).
- Repositorio público en `https://github.com/ldebortoli/linterna-premium`, rama principal `main`, con Secret Scanning y Push Protection habilitados.
- El boton principal de Apagado Premium ya no muestra precio ni condiciones comerciales; al tocarlo aparece una confirmacion con precio, `Compra unica · Sin suscripcion` y luego Google Play. La variante local presenta la misma confirmacion como simulacion sin cobro.
- El ultimo APK de prueba en `apps/mobile/android/app/build/outputs/apk/demo/debug/app-demo-debug.apk` corresponde a la version 0.1.0 y no contiene este cambio; no se regenero por la politica de build bajo demanda.
- Google Play Billing y Google Mobile Ads quedan separados por variantes hasta que existan cuentas e identificadores reales.
- Apps Dashboard reconoce el perfil Android nativo y puede generar/archivar el APK.
- Apps Dashboard 0.2.8 genera para Linterna Premium una leyenda Telegram que menciona sólo a `@galerazo34` y nunca a Nico.
- Validacion mas reciente: `npm test`, `npm run coverage` y `npm run lint`; 13/13 pruebas, 100% de instrucciones/ramas/lineas/metodos del motor y Android Lint correcto. No se genero un APK para la version 0.1.1.
- CI público sobre `main` y pull requests ejecuta pruebas, cobertura y lint, con cancelación de corridas reemplazadas y sin build de APK.
- QA visual previo sobre 0.1.0: encendido demo, apagado normal, dialogo de compra sin cobro, Premium persistente sin anuncios, apagado al perder foco y pantalla compacta con texto al 130%. La confirmacion redisenada de 0.1.1 queda pendiente de revision visual cuando se solicite una nueva APK.
- No hay elementos procesados en `USER_QUEUE.md`.

## Proximos pasos

1. Cuando el usuario lo pida, generar la APK 0.1.1 y revisar visualmente la nueva confirmacion en un telefono.
2. Instalarla en un telefono fisico y validar el flash real.
3. Crear la app y el producto no consumible `premium_blackout_pack` en Play Console.
4. Crear la app/unidad banner en AdMob, configurar consentimiento y publicar la politica de privacidad.
5. Inyectar IDs y firma de produccion segun `docs/CONFIGURACION_GOOGLE.md`, generar `playRelease` y probar con un tester licenciado.

## Riesgos

- El cobro real queda bloqueado hasta crear la app y el producto en Play Console; debug debe permanecer sin cargos.
- Los anuncios reales quedan bloqueados hasta registrar AdMob y completar consentimiento/privacidad.
- Nunca conservar la linterna encendida al salir, perder foco o pulsar cualquiera de los apagados.
- Los identificadores reales y la firma de producción no deben versionarse; Secret Scanning y Push Protection están habilitados como segunda barrera.
- El APK existente queda desactualizado respecto del codigo 0.1.1 hasta que el usuario solicite expresamente un nuevo build.
