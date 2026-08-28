# Linterna Premium - Contexto del proyecto

## Descripcion general

Aplicacion Android humoristica de linterna. Enciende el flash fisico con la maxima intensidad disponible, ofrece un apagado normal gratuito y un "Apagado Premium" que muestra una confirmacion comercial antes de Google Play y desbloquea fuegos artificiales con una secuencia gradual del flash. Incluye publicidad mediante Google Mobile Ads sin bloquear la funcion principal.

## Estado detectado

- Stack: Android nativo, Kotlin, Jetpack Compose, Material 3, Gradle.
- Git: repositorio público `https://github.com/ldebortoli/linterna-premium` en rama principal `main`, con Secret Scanning y Push Protection habilitados.
- Android: `minSdk 24`, `compileSdk 36`, Java 17 como bytecode objetivo.
- Integraciones: Google Play Billing para el producto no consumible `premium_blackout_pack`; Google Mobile Ads con identificadores de prueba solo en builds de desarrollo.
- Privacidad: no hay cuentas, backend propio ni captura directa de tarjetas. Google Play procesa el pago.
- Dashboard: perfil Android nativo `Linterna Premium` registrado en Apps Dashboard; genera la variante `demoDebug` para pruebas y `playRelease` para publicación.
- Telegram: los mensajes de artefactos generados por Apps Dashboard mencionan sólo al propietario `@galerazo34` y nunca a Nico.

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
- CI público: pruebas, cobertura y lint sobre `main` y pull requests; no genera APKs.
- Baseline verificado: 17 pruebas unitarias, 100% de instrucciones/ramas/líneas/métodos en el dominio y Android Lint sin observaciones.

## Convenciones

- Preservar cambios ajenos y secretos locales.
- Actualizar este archivo solo cuando cambie informacion estable.
- La memoria persistente vive en `.codex/` y se carga siguiendo `AGENTS.md`.
- La version canonica vive en `version.properties`; `app.json` y Gradle deben coincidir.
- Ningun identificador real de anuncios, producto o credencial se versiona como secreto. Los valores publicables de Google se inyectan por propiedades de Gradle.
- El apagado normal y el recorrido Premium sin licencia cortan la linterna inmediatamente. Con Premium activo, una secuencia lenta de menos de tres segundos regula la potencia cuando el hardware lo permite y garantiza el apagado final incluso ante fallo, cancelacion o salida de la app.
- La variante `demo` simula la linterna únicamente cuando el dispositivo no tiene flash, para poder probar el recorrido en emulador; la variante `play` nunca simula hardware.
- La variante `demo` permite borrar la licencia simulada y volver a la edicion mortal; la variante `play` no expone ni ejecuta este restablecimiento.
