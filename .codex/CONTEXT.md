# Linterna Premium - Contexto del proyecto

## Descripcion general

Aplicacion Android humoristica de linterna. Enciende el flash fisico con la maxima intensidad disponible, ofrece un apagado normal gratuito y un "Apagado Premium" que apaga primero la luz y luego abre una compra oficial de Google Play para desbloquear una presentacion especial. Incluye publicidad mediante Google Mobile Ads sin bloquear la funcion principal.

## Estado detectado

- Stack: Android nativo, Kotlin, Jetpack Compose, Material 3, Gradle.
- Git: repositorio local en rama `main`; el remoto privado se crea en la primera entrega.
- Android: `minSdk 24`, `compileSdk 36`, Java 17 como bytecode objetivo.
- Integraciones: Google Play Billing para el producto no consumible `premium_blackout_pack`; Google Mobile Ads con identificadores de prueba solo en builds de desarrollo.
- Privacidad: no hay cuentas, backend propio ni captura directa de tarjetas. Google Play procesa el pago.

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

## Convenciones

- Preservar cambios ajenos y secretos locales.
- Actualizar este archivo solo cuando cambie informacion estable.
- La memoria persistente vive en `.codex/` y se carga siguiendo `AGENTS.md`.
- La version canonica vive en `version.properties`; `app.json` y Gradle deben coincidir.
- Ningun identificador real de anuncios, producto o credencial se versiona como secreto. Los valores publicables de Google se inyectan por propiedades de Gradle.
- Los dos recorridos de apagado cortan la linterna inmediatamente. El pago nunca es requisito para apagarla.

