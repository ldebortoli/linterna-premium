# Session handoff

## Objetivo general

Entregar Linterna Premium como aplicacion Android nativa mantenible, verificable y lista para que el usuario pruebe antes de configurar sus cuentas comerciales.

## Tarea actual

Configurar las cuentas e identificadores reales de Google cuando el usuario decida avanzar con la publicacion comercial.

## Estado actual

- Android nativo Kotlin/Compose implementado en version 0.2.0 (versionCode 3).
- Repositorio público en `https://github.com/ldebortoli/linterna-premium`, rama principal `main`, con Secret Scanning y Push Protection habilitados.
- El boton principal de Apagado Premium ya no muestra precio ni condiciones comerciales; al tocarlo aparece una confirmacion con precio, `Compra unica · Sin suscripcion` y luego Google Play. La variante local presenta la misma confirmacion como simulacion sin cobro.
- Con Premium activo, el boton ejecuta fuegos artificiales y un pulso visual de tres segundos sincronizado con una curva del flash: baja, sube, baja, sube y desciende hasta apagarse. Android 13+ usa potencia real si el hardware expone varios niveles; el fallback conserva el LED estable, anima la pantalla y apaga al final.
- La secuencia se cancela al perder foco y el ejecutor apaga el flash en `finally` ante exito, error o cancelacion.
- El ultimo APK de prueba en `apps/mobile/android/app/build/outputs/apk/demo/debug/app-demo-debug.apk` corresponde a la version 0.1.0 y no contiene los cambios 0.1.1/0.2.0; no se regenero por la politica de build bajo demanda.
- Google Play Billing y Google Mobile Ads quedan separados por variantes hasta que existan cuentas e identificadores reales.
- Apps Dashboard reconoce el perfil Android nativo y puede generar/archivar el APK.
- Apps Dashboard 0.2.8 genera para Linterna Premium una leyenda Telegram que menciona sólo a `@galerazo34` y nunca a Nico.
- Validacion mas reciente: `npm test`, `npm run coverage` y `npm run lint`; 17/17 pruebas, 100% de instrucciones/ramas/lineas/metodos del dominio y Android Lint correcto. No se genero un APK para la version 0.2.0.
- CI público sobre `main` y pull requests ejecuta pruebas, cobertura y lint, con cancelación de corridas reemplazadas y sin build de APK.
- QA visual previo sobre 0.1.0: encendido demo, apagado normal, dialogo de compra sin cobro, Premium persistente sin anuncios, apagado al perder foco y pantalla compacta con texto al 130%. La confirmacion de 0.1.1 y la celebracion de 0.2.0 quedan pendientes de revision visual cuando se solicite una nueva APK.
- No hay elementos procesados en `USER_QUEUE.md`.

## Proximos pasos

1. Cuando el usuario lo pida, generar la APK 0.2.0 y revisar visualmente la confirmacion y los fuegos artificiales en un telefono.
2. Instalarla en un telefono fisico y validar el flash real.
3. Crear la app y el producto no consumible `premium_blackout_pack` en Play Console.
4. Crear la app/unidad banner en AdMob, configurar consentimiento y publicar la politica de privacidad.
5. Inyectar IDs y firma de produccion segun `docs/CONFIGURACION_GOOGLE.md`, generar `playRelease` y probar con un tester licenciado.

## Riesgos

- El cobro real queda bloqueado hasta crear la app y el producto en Play Console; debug debe permanecer sin cargos.
- Los anuncios reales quedan bloqueados hasta registrar AdMob y completar consentimiento/privacidad.
- Nunca conservar la linterna encendida al salir, perder foco o pulsar cualquiera de los apagados.
- La regulacion fisica de potencia requiere Android 13+ y un flash que informe mas de un nivel; debe validarse en hardware real y no se reemplaza por parpadeos rapidos en dispositivos incompatibles.
- Los identificadores reales y la firma de producción no deben versionarse; Secret Scanning y Push Protection están habilitados como segunda barrera.
- El APK existente queda desactualizado respecto del codigo 0.2.0 hasta que el usuario solicite expresamente un nuevo build.
