# TODO

- [P1] Configurar en Play Console el producto `premium_blackout_pack`, firmar una version y probar el pago con un tester licenciado.
- [P1] Crear AdMob, registrar la app y reemplazar los identificadores de anuncios de produccion mediante propiedades locales seguras.
- [P1] Publicar politica de privacidad y completar las declaraciones de Data safety, anuncios y contenido en Google Play.
- [P1] Probar la intensidad física y los ciclos de encendido/apagado en al menos un teléfono Android real con flash.
- [P2] Evaluar validacion de compras en backend antes del lanzamiento publico si el modelo comercial o el riesgo de fraude lo justifican.

# IN PROGRESS

- Sin tareas activas.

# DONE

- [2026-08-31] Entregar Linterna PREMIUM 0.4.3 (versionCode 11): quitar todo aviso posterior al apagado plebeyo y ampliar la celebración Premium a once fuegos artificiales, 56 piezas de confeti/monedas, luces suaves de marquesina y tragamonedas 7·7·7; verificar 22/22 tests, cobertura 100% y lint sin generar APK.
- [2026-08-31] Agregar `.gitattributes` con finales de linea deterministas y formatos binarios explicitos; verificar el mismo control del proyecto desde un checkout limpio con `core.autocrlf=true` antes del push.
- [2026-08-28] Entregar Linterna PREMIUM 0.4.2 (versionCode 10): retirar el apagado de `onPause` para conservar el flash al ir a Inicio, cambiar de app, abrir Google Play o cerrar la actividad, aceptando que Android puede apagarlo al matar el proceso; verificar 22/22 tests, cobertura 100% y lint sin generar APK.
- [2026-08-28] Entregar Linterna PREMIUM 0.4.1 (versionCode 9): conservar el flash al abrir/cancelar la confirmación Premium, iniciar el apagado especial después de una compra exitosa, mantener el corte al salir/cerrar y sincronizar la UI con una linterna encendida externamente mediante `TorchCallback`; verificar 23/23 tests, cobertura 100% y lint sin generar APK.
- [2026-08-28] Entregar Linterna PREMIUM 0.4.0 (versionCode 8): anclar el dropdown debajo del botón de idiomas, limitarlo a 360 dp con scroll y ampliar la traducción integral a las 18 opciones reales de Galerazo Bot más mandarín, cantonés y coreano; verificar 21/21 tests, cobertura 100% y lint sin generar APK.
- [2026-08-28] Entregar Linterna PREMIUM 0.3.0 (versionCode 7) con selector persistente y traducción completa a los nueve idiomas de Tivio/Galerazo Bot; cambiar la distribución interna a `demoRelease` no depurable con el certificado QA estable compartido, verificar 20/20 tests, cobertura 100%, lint y firma sin generar APK.
- [2026-08-28] Diagnosticar la advertencia de Play Protect al instalar la APK 0.2.3 desde Telegram: firma V2 valida, `targetSdk 36`, paquete demo nuevo y certificado `Android Debug`; causa exacta pendiente solo si se necesita clasificar el texto concreto del aviso.
- [2026-08-28] Reemplazar `mortal/mortales` por `EDICIÓN PLEBEYA`, `Restablecer edición plebeya` y `Apagar linterna como un plebeyo`; validar pruebas, cobertura y lint sin generar APK.
- [2026-08-28] Cambiar el nombre visible, launcher, encabezado y metadatos publicos a `Linterna PREMIUM`, preservando package id y slug; validar pruebas, cobertura y lint sin generar APK.
- [2026-08-28] Diagnosticar cinco fallos de CI como `gradlew EACCES`, no cuota; corregir el modo Git a `100755`, conservar CI sin APK y verificar que `ldebortoli/linterna-premium` siga publico sobre `main`.
- [2026-08-28] Agregar `Restablecer edicion mortal` exclusivamente en demo para borrar Premium simulado, reactivar anuncios de prueba y repetir el recorrido; validar 17 pruebas, cobertura total y lint sin generar APK.
- [2026-08-28] Agregar fuegos artificiales y pulso visual al Apagado Premium, mas una curva lenta de potencia real en hardware compatible con apagado garantizado y fallback seguro; validar 17 pruebas, cobertura total y lint sin generar APK.
- [2026-08-28] Simplificar el boton de Apagado Premium y mover precio, compra unica y ausencia de suscripcion a la confirmacion previa; validar 13 pruebas, cobertura total y lint sin generar APK.
- [2026-08-28] Configurar Apps Dashboard para que los mensajes Telegram de Linterna Premium mencionen sólo a `@galerazo34`, nunca a Nico; validar sin generar ni enviar APK.
- [2026-08-28] Inicializar la memoria persistente del proyecto.
- [2026-08-28] Definir arquitectura Android nativa y limites de seguridad/compliance del chiste.
- [2026-08-28] Implementar la primera versión de Linterna Premium con linterna física, apagado normal, Apagado Premium, compra simulada/Google Play Billing y publicidad demo/AdMob.
- [2026-08-28] Verificar 13 pruebas unitarias, cobertura 100% del motor, Android Lint, compilación e instalación del APK `demoDebug`.
- [2026-08-28] Validar visualmente el recorrido gratuito y Premium, persistencia, ausencia de anuncios en Premium, apagado al perder foco y diseño desplazable en pantalla compacta con texto al 130%.
- [2026-08-28] Integrar el proyecto como perfil Android nativo en Apps Dashboard.
- [2026-08-28] Crear y publicar el repositorio privado `ldebortoli/linterna-premium` con autor local noreply.
- [2026-08-28] Hacer público `ldebortoli/linterna-premium` sobre `main`, habilitar Secret Scanning y Push Protection y agregar CI rápido sin build de APK.
