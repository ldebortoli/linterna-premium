# Linterna Premium

Una linterna Android con un chiste transparente: el apagado normal siempre es gratis y el **Apagado Premium** corta primero la luz, luego ofrece una compra unica que elimina anuncios y desbloquea una celebracion visual.

## Estado de esta entrega

- Linterna fisica con intensidad maxima cuando Android y el hardware lo permiten.
- Apagado automatico al enviar la app a segundo plano.
- Boton Premium grande y boton normal gratuito claramente visible.
- Compra simulada, sin dinero ni tarjeta, en la variante `demoDebug`. Si un
  emulador no ofrece flash, esa misma variante simula el encendido para permitir
  revisar toda la interfaz; un teléfono con flash siempre usa el LED real.
- Integracion real de Google Play Billing disponible en la variante `play`.
- Banner oficial de prueba de Google en demo; AdMob real requiere configuracion explicita.
- Tema oscuro, interfaz desplazable y controles accesibles en pantallas compactas.

## Requisitos

- Android Studio con JDK 17 o superior.
- Android SDK 36.
- Node.js 20 o superior para los comandos de validacion.
- Un teléfono Android con flash para probar la función física. El emulador sirve para revisar la interfaz, pero normalmente no ofrece linterna.

## Validar

```powershell
npm install
npm test
npm run test:android
npm run coverage
npm run lint
```

El CI público ejecuta estos controles sobre `main` y pull requests, con cancelación de corridas reemplazadas. No compila ni publica APKs.

Los artefactos Android se generan únicamente cuando el usuario lo pide o cuando se está probando o reparando el pipeline de build. En ese caso:

```powershell
npm run build:apk
```

El APK de prueba queda en `apps/mobile/android/app/build/outputs/apk/demo/debug/`. Para instalarlo con un teléfono conectado y depuración USB habilitada:

```powershell
npm run install:android
```

No ejecutes ni publiques una variante `play` hasta completar [la configuracion de Google](docs/CONFIGURACION_GOOGLE.md).

## Arquitectura breve

- `domain/LinternaEngine`: reglas testeables y orden seguro de apagado/pago.
- `platform/AndroidTorchPort`: acceso aislado a `CameraManager`.
- `billing/`: pasarela oficial de Google Play; la app nunca recibe datos de tarjeta.
- `ads/`: consentimiento e inicializacion de Google Mobile Ads.
- `ui/`: interfaz Compose y banner.

Las decisiones y el estado de continuidad viven en `.codex/`.
