# Linterna PREMIUM

Una linterna Android con un chiste transparente: el apagado normal siempre es gratis y el **Apagado Premium** ofrece una compra unica que elimina anuncios y desbloquea fuegos artificiales junto con una secuencia gradual del flash que termina siempre apagada.

## Estado de esta entrega

- Linterna fisica con intensidad maxima cuando Android y el hardware lo permiten.
- El flash permanece encendido al ir a Inicio, cambiar de app, abrir Google Play o cerrar la actividad; Android puede apagarlo si mata el proceso o necesita la cámara.
- Si el flash ya estaba encendido desde Android al abrir la app, la pantalla se sincroniza y muestra directamente ambos apagados.
- Boton Premium grande y boton normal gratuito claramente visible.
- Abrir o cancelar la confirmacion Premium no apaga el flash; la compra exitosa inicia el efecto Premium y lo apaga al terminar.
- Apagado Premium con once fuegos artificiales, confeti/monedas, luces de marquesina pulsantes, tragamonedas 7·7·7, pulso visual y una curva lenta de potencia real en telefonos compatibles; en el resto se conserva el efecto visual y el apagado final seguro.
- El apagado plebeyo vuelve directamente al estado listo, sin mostrar un aviso de confirmacion.
- La fiesta dura 15 segundos y encadena siete festejos reales completos, con solapes y repeticiones sin silencios, sobre las fanfarrias originales;
  el flash se apaga durante los primeros tres. Ver [preparación y licencias del audio](docs/AUDIO_LICENSES.md).
- La variante demo permite restablecer la edicion plebeya para repetir las pruebas de compra y anuncios; esta accion no existe en Play.
- Selector persistente en la pantalla principal con los 18 idiomas de Galerazo
  Bot —incluidas las variantes regionales de español, portugués y chino,
  latín, catalán, euskera, guaraní y quechua/Runa Simi— más mandarín, cantonés
  y coreano. El menú se abre debajo de su botón, tiene alto limitado y scroll;
  toda la interfaz, los avisos, Billing y la accesibilidad usan el idioma elegido.
- Compra simulada, sin dinero ni tarjeta, en la variante `demo`. Si un
  emulador no ofrece flash, esa misma variante simula el encendido para permitir
  revisar toda la interfaz; un teléfono con flash siempre usa el LED real.
- Apps Dashboard distribuye la prueba como `demoRelease`, no depurable y firmada
  con la identidad QA estable compartida con Tivio y A la Altura. El keystore se
  conserva únicamente en la máquina de desarrollo y nunca se versiona.
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

Antes del primer build, preparar los siete efectos licenciados según [AUDIO_LICENSES.md](docs/AUDIO_LICENSES.md).
Los clips están excluidos del repositorio público y el comando de build comprueba sus hashes.

El APK de prueba queda en `apps/mobile/android/app/build/outputs/apk/demo/release/`. Para instalarlo con un teléfono conectado y depuración USB habilitada:

```powershell
npm run install:android
```

No ejecutes ni publiques una variante `play` hasta completar [la configuracion de Google](docs/CONFIGURACION_GOOGLE.md).

## Arquitectura breve

- `domain/LinternaEngine`: reglas testeables y orden seguro de apagado/pago.
- `domain/PremiumSequenceRunner`: curva de intensidad cancelable con apagado garantizado en `finally`.
- `platform/AndroidTorchPort`: acceso aislado a `CameraManager`.
- `billing/`: pasarela oficial de Google Play; la app nunca recibe datos de tarjeta.
- `ads/`: consentimiento e inicializacion de Google Mobile Ads.
- `ui/`: interfaz Compose y banner.

Las decisiones y el estado de continuidad viven en `.codex/`.
