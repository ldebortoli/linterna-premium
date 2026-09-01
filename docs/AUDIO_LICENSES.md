# Audio de la celebración Premium

## Licencia y procedencia

Revisado el 2026-09-01: [Mixkit Sound Effects Free License](https://mixkit.co/license/#sfxFree),
incluido su [texto completo](https://mixkit.co/license/modal/sfxFree/), permite incorporar estos
efectos a productos creativos comerciales y no comerciales. No permite redistribuir los efectos
aislados, como stock, en herramientas/plantillas ni junto con archivos fuente; tampoco atribuirse
su autoría o registrarlos en sistemas de gestión de derechos.

Por eso **ni los MP3 originales ni los PCM preparados se versionan**. Se empaquetan localmente como
parte de Linterna PREMIUM. El repositorio público conserva la receta, los enlaces oficiales y
los hashes SHA-256 de originales y conversiones en `audio/premium-sfx.json`. La licencia no convierte
los clips en código abierto. No exportar los efectos como biblioteca o soundboard.

También se revisó la [Acceptable Use Policy de Envato](https://help.elements.envato.com/hc/en-us/articles/31035788503321-Acceptable-Use-Policy).
Las tragamonedas y premios de Linterna son decorativos, sin apuestas ni premios monetarios.
El apagado gratuito siempre está disponible; la compra Premium es una mejora audiovisual explícita.
Un futuro cambio hacia apuestas reales o promoción de apuestas exige revisar la licencia antes de reutilizar estos recursos.

Fuentes: [catálogo Crowd](https://mixkit.co/free-sound-effects/crowd/) y
[catálogo Win](https://mixkit.co/free-sound-effects/win/). Los siete clips fueron aprobados por el
usuario; `Huge crowd cheering victory` (462, candidato 1) fue rechazado y está excluido.

| ID | Efecto aprobado | Archivo completo utilizado |
| --- | --- | --- |
| 531 | Birthday crowd party cheer | 5,385 s desde el inicio |
| 459 | Male crowd cheering short | 4,320 s desde el inicio |
| 437 | Small crowd ovation | 6,399 s desde el inicio |
| 2012 | Males yes victory | 1,035 s desde el inicio |
| 2011 | Male voice cheer victory | 0,945 s desde el inicio |
| 1934 | Payout award | 4,098 s desde el inicio |
| 1928 | Slot machine win | 5,038 s desde el inicio |

## Preparación reproducible (solo antes de un build solicitado)

Requiere Node.js y FFmpeg 9.0 disponible en PATH. Tras leer la licencia:

```powershell
npm run audio:prepare -- --source-dir <carpeta-de-preescuchas-aprobadas>
# O descargar exactamente las siete fuentes oficiales fijadas por SHA-256:
npm run audio:prepare -- --download
npm run audio:verify
```

El generador comprueba los originales y convierte cada archivo completo, desde el offset cero y sin
recortarlo, a PCM mono s16le/22.050 Hz; después comprueba cantidad exacta de muestras y SHA-256 de
salida. Si otra versión de FFmpeg cambia los bytes, detenerse y revisar la
diferencia; no actualizar hashes a ciegas. El caché descargado queda bajo `artifacts/` y los PCM
bajo `apps/mobile/android/app/src/main/assets/premium-sfx/`, ambos ignorados por Git.

`build:apk` e `install:android` verifican estos recursos antes de Gradle y fallan con una instrucción
de preparación si faltan o no coinciden. Invocar Gradle directamente omite ese control: usar los
comandos del proyecto para las distribuciones. No se generan APKs al preparar o validar audio.

## Mezcla y reproducción

`PremiumRealSoundPlan.kt` inicia los clips completos a 0 / 0,15 / 0,5 / 3,9 / 4,1 / 6,6 / 7 / 8,6 /
9,15 / 13 / 14,055 segundos. Cada festejo arranca desde su primera muestra y termina naturalmente
antes del segundo 15; el siguiente entra antes de la cola audible del anterior. Las repeticiones
cubren los tramos más suaves sin superar un premio/casino ni dos voces/grupos simultáneos.
La pista sintetizada original permanece durante los 15 segundos a menor ganancia como continuidad
y remate final. No hay fades añadidos, recortes ni descansos programados. La mezcla completa dura
15 segundos; no cambia la curva física del flash ni la fiesta visual.

El reproductor precarga la mezcla al abrir la pantalla. En modo estático Android devuelve
`STATE_NO_STATIC_DATA` hasta escribir PCM: ese estado es válido y ahora se comprueba `READY`
**después** de la carga. Ver [contrato de AudioTrack](https://developer.android.com/reference/android/media/AudioTrack#STATE_NO_STATIC_DATA).
Los fallos se registran con la etiqueta `PremiumCelebrationAudio`. Si una ejecución fuera del
build validado carece de clips, registra el problema y conserva como fallback la pista sintetizada.
Los botones de volumen controlan multimedia; no se fuerza el volumen ni se cambia la salida Bluetooth.
La cancelación libera el track en `finally` y la reproducción no excede el plazo visual de 15 segundos.

## Validación

```powershell
npm test
npm run audio:verify
npm run coverage
npm run lint
```

Las pruebas puras comprueban los siete IDs, clips completos, solapes continuos, concurrencia limitada,
duración, decodificación PCM, señal final y el protocolo de carga estática (incluidos errores y escritura
incompleta). El umbral de cobertura del dominio sigue en 100% para instrucciones, ramas, líneas
y métodos. No incluye adaptadores Android ni UI.

La prueba local `PremiumAudioAssetsTest` utiliza los clips preparados y escribe una preescucha
en `apps/mobile/android/app/build/reports/audio/premium-celebration-preview.wav`. Se omite
explícitamente en el checkout público sin assets licenciados; el resto de pruebas y Lint no
necesitan descargas. Además rechaza cualquier ventana silenciosa de 20 ms en la mezcla real y
comprueba que ningún clip se corte al llegar al segundo 15. La escucha en un teléfono y la prueba real de volumen/ruteo quedan para la
siguiente APK solicitada: una señal PCM válida no prueba por sí sola la salida del altavoz.
