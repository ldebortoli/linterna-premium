# Plan de publicación de Linterna PREMIUM

Revisado el 2026-09-29. Estado: la app no está publicada en Google Play; `demoRelease` es solo QA. La variante comercial usa `com.linternapremium.app`, API 36, Google Play Billing 9.1.0 y anuncios AdMob/UMP. `playRelease` todavía no tiene una clave de subida de producción configurada.

## Acciones del propietario

1. Usar una cuenta Google propia para abrir [Play Console](https://play.google.com/console/signup), elegir cuenta personal u organización según la titularidad real, abonar el alta única de USD 25 y completar identidad, contacto y verificación con un Android físico si Play lo solicita. Una sola cuenta puede contener Linterna PREMIUM y Gemidos PREMIUM. Una cuenta personal monetizada exhibe nombre legal, país, email de desarrollador y dirección completa; revisar esto antes del alta. La cuenta de organización requiere D-U-N-S.
2. Crear o vincular el perfil de pagos de Play en **Configuración > Perfil de pagos**, completar datos fiscales y cuenta bancaria a nombre del titular, y adherirse al nivel de comisión del 15 % para el primer millón anual cuando corresponda. Argentina admite registro como desarrollador y comerciante y pagos bancarios.
3. Crear la app **gratuita** Linterna PREMIUM en Play Console con el paquete definitivo `com.linternapremium.app`. El cobro es una compra dentro de la app, no el precio de descarga. Mantener ese paquete fijo.
4. En **Monetizar con Play > Productos > Productos únicos**, crear `premium_blackout_pack` como compra única permanente, opción **Comprar**, con precio y países elegidos por el propietario. Activar el producto. No configurar suscripción, alquiler ni múltiples ofertas antes de validar el cliente actual.
5. Abrir [AdMob](https://admob.google.com/) con una cuenta del mismo titular, completar verificación y pagos, registrar la app Android y crear una unidad banner. Entregar al proyecto únicamente el ID de app y el ID de banner; no compartir contraseñas, tarjetas ni claves por chat.
6. Elegir un sitio HTTPS público para la política de privacidad y el `app-ads.txt` de AdMob, y un email de soporte. El borrador de `docs/PRIVACIDAD.md` requiere identidad, fecha, contacto y declaraciones finales. El sitio debe figurar como web de desarrollador en la ficha de Play; AdMob verifica `app-ads.txt` cuando la ficha esté disponible.
7. Conseguir 12 personas con cuentas Google que acepten participar en una prueba cerrada de **esta app** durante 14 días continuos si la cuenta Play personal se creó después del 13-11-2023. Pueden ser los mismos testers de la otra app, pero deben adherirse a ambas pruebas. Registrar por separado los testers de licencia para compras de prueba sin cargo.

## Trabajo técnico antes de enviar a revisión

1. Completar la política y la ficha de Play: descripción, capturas reales, icono, contacto, países, clasificación IARC, público objetivo, declaración de anuncios, seguridad de datos y acceso para revisión.
2. Configurar los dos IDs AdMob de `docs/CONFIGURACION_GOOGLE.md` fuera de Git, consentimiento UMP y una **clave de subida de producción** propia para Linterna. La firma QA de `demoRelease` no se reutiliza. Activar Play App Signing y construir un AAB `playRelease` firmado; el proyecto aún no configura esa firma en Gradle.
3. Verificar pruebas locales, cobertura, Lint y `audio:verify`; validar el flash y los 15 segundos de audio en un teléfono real. Probar instalación desde la pista interna y compra, cancelación, compra pendiente, restauración y eliminación de anuncios con un tester licenciado.
4. Completar la prueba cerrada, solicitar acceso a producción, responder observaciones de revisión y lanzar la versión aprobada. Después, vincular la ficha pública en AdMob y verificar `app-ads.txt` y la aprobación de anuncios.

## Costos y límites

- Alta Play Console: USD 25 una vez por cuenta, no por app. Publicar una app gratuita no añade una cuota fija de Play. Las compras integradas sí pagan una comisión según mercado y programa; en Argentina, el nivel del 15 % para los primeros USD 1 millón anuales requiere adhesión.
- AdMob no exige una cuota de alta, pero sus pagos requieren verificación y umbral. Sitio/política pueden alojarse sin comprar dominio; impuestos, retenciones y gastos bancarios dependen de la situación del titular.
- La API 36 del proyecto cumple el requisito de Google Play vigente desde el 31-08-2026. La publicación efectiva depende de revisión de Google y de pruebas satisfactorias en hardware.

## Fuentes oficiales

- [Alta y costo de Play Console](https://support.google.com/googleplay/android-developer/answer/6112435)
- [Datos públicos de la cuenta](https://support.google.com/googleplay/android-developer/answer/13628312)
- [Ubicaciones admitidas](https://support.google.com/googleplay/android-developer/answer/9306917)
- [Prueba cerrada de cuentas personales nuevas](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Productos únicos](https://support.google.com/googleplay/android-developer/answer/16430488)
- [Pruebas de compras](https://support.google.com/googleplay/android-developer/answer/6062777)
- [Requisito de API objetivo](https://support.google.com/googleplay/android-developer/answer/11926878)
- [Firma de apps](https://support.google.com/googleplay/android-developer/answer/9842756)
- [Verificación AdMob con app-ads.txt](https://support.google.com/admob/answer/14538460)
- [Comisiones de Play](https://support.google.com/googleplay/android-developer/answer/112622)
