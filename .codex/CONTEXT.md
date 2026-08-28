# Linterna PREMIUM - Contexto del proyecto

## Descripcion general

Aplicacion Android humoristica de linterna. Enciende el flash fisico con la maxima intensidad disponible, ofrece un apagado normal gratuito y un "Apagado Premium" que muestra una confirmacion comercial antes de Google Play y desbloquea fuegos artificiales con una secuencia gradual del flash. Incluye publicidad mediante Google Mobile Ads sin bloquear la funcion principal.

## Estado detectado

- Stack: Android nativo, Kotlin, Jetpack Compose, Material 3, Gradle.
- Nombre visible: `Linterna PREMIUM`; el package id y el slug tecnico permanecen `com.linternapremium.app` y `linterna-premium`.
- Git: repositorio público `https://github.com/ldebortoli/linterna-premium` en rama principal `main`, con Secret Scanning y Push Protection habilitados.
- Android: `minSdk 24`, `compileSdk 36`, Java 17 como bytecode objetivo.
- Integraciones: Google Play Billing para el producto no consumible `premium_blackout_pack`; Google Mobile Ads con identificadores de prueba solo en builds de desarrollo.
- Privacidad: no hay cuentas, backend propio ni captura directa de tarjetas. Google Play procesa el pago.
- Idiomas: selector persistente con las 18 opciones reales de Galerazo Bot (`es-AR`, `es-ES`, `en`, `ru`, `la`, `ja`, `it`, `fr`, `de`, `nl`, `zh-Hans`, `zh-Hant`, `pt-BR`, `pt-PT`, `ca`, `eu`, `gn`, `quz`) más mandarín, cantonés y coreano; el catálogo cubre interfaz, errores, Billing, anuncios de prueba y accesibilidad.
- Dashboard: perfil Android nativo `Linterna PREMIUM` registrado en Apps Dashboard; distribuye `demoRelease` no depurable con la identidad QA estable compartida con Tivio/A la Altura y reserva `playRelease` para publicación.
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
- El wrapper Linux `apps/mobile/android/gradlew` se versiona como ejecutable (`100755`) para que GitHub Actions pueda iniciar Gradle.
- Baseline verificado: 23 pruebas unitarias, 100% de instrucciones/ramas/líneas/métodos en el alcance de cobertura y Android Lint sin observaciones.

## Convenciones

- Preservar cambios ajenos y secretos locales.
- Actualizar este archivo solo cuando cambie informacion estable.
- La memoria persistente vive en `.codex/` y se carga siguiendo `AGENTS.md`.
- La version canonica vive en `version.properties`; `app.json` y Gradle deben coincidir.
- Ningun identificador real de anuncios, producto o credencial se versiona como secreto. Los valores publicables de Google se inyectan por propiedades de Gradle.
- El apagado normal y el paso de la app a segundo plano cortan la linterna inmediatamente. Abrir o cancelar la confirmacion Premium conserva el flash encendido; una compra exitosa inicia la secuencia Premium y garantiza el apagado final. Con Premium ya activo, esa misma secuencia lenta de menos de tres segundos regula la potencia cuando el hardware lo permite.
- La variante `demo` simula la linterna únicamente cuando el dispositivo no tiene flash, para poder probar el recorrido en emulador; la variante `play` nunca simula hardware.
- La variante `demo` permite borrar la licencia simulada y volver a la edicion plebeya; la variante `play` no expone ni ejecuta este restablecimiento.
- La elección de idioma se guarda localmente; la primera apertura usa el idioma y la región compatibles del dispositivo y cae a español argentino cuando no coincide con el catálogo. El menú se ancla debajo del botón de idiomas, limita su alto a 360 dp y permite desplazamiento interno.
- La app observa el estado real del flash mediante `CameraManager.TorchCallback`: si Android ya lo tenia encendido al abrir o volver a la app, sincroniza la pantalla y muestra directamente ambos apagados.
- `demoRelease` usa por variable de entorno el keystore QA local de Tivio, ignorado por Git; `playRelease` no reutiliza esa identidad y debe configurarse con la firma de producción de Google Play.
