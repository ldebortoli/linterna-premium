# Configuracion de Google Play y AdMob

La variante de prueba funciona sin cuentas comerciales. La variante publicable esta preparada para usar exclusivamente los sistemas oficiales de Google.

## 1. Google Play Console

1. Crear la aplicacion con el paquete definitivo `com.linternapremium.app`. Este identificador no puede cambiar despues de publicar.
2. Crear un producto integrado no consumible con ID exacto `premium_blackout_pack`.
3. Definir nombre, descripcion y precio. La app muestra el precio localizado que devuelve Google Play; no lo escribe a mano.
4. Subir un Android App Bundle firmado a una pista de prueba interna.
5. Agregar cuentas de prueba de licencia y validar compra, cancelacion, restauracion y pago pendiente.

La app no muestra ni guarda formularios de tarjeta. `BillingClient.launchBillingFlow` abre la pantalla administrada por Google Play. Antes de un lanzamiento con volumen relevante conviene validar el token de compra en un backend mediante Google Play Developer API; queda registrado en el backlog.

## 2. AdMob

1. Crear la app Android en AdMob con el mismo paquete.
2. Crear una unidad de anuncio tipo banner.
3. Guardar los IDs publicables en el archivo local global de Gradle, nunca en Git:

```properties
LINTERNAPREMIUM_ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
LINTERNAPREMIUM_ADMOB_BANNER_ID=ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ
```

En Windows, Gradle suele leer ese archivo desde `%USERPROFILE%\.gradle\gradle.properties`. Los IDs no son contrasenas, pero mantenerlos fuera del codigo evita mezclar ambientes. La compilacion `play` falla si falta alguno.

El modo demo usa solamente los IDs de prueba publicados por Google y rotula el banner como prueba. Nunca hagas clic repetidamente en anuncios propios, ni siquiera durante pruebas.

## 3. Consentimiento y privacidad

La variante `play` consulta User Messaging Platform antes de pedir anuncios. Tambien hay que:

- publicar una politica de privacidad accesible por HTTPS;
- completar Data safety y la declaracion de anuncios en Play Console;
- configurar mensajes de privacidad en AdMob para las regiones aplicables;
- declarar con claridad que Premium es una compra unica, elimina anuncios y agrega el efecto especial;
- conservar siempre el apagado normal gratuito y visible.

## 4. Compilacion publicable

Despues de configurar firma e IDs, ejecutar desde Apps Dashboard o Android Studio la variante `playRelease`. No se versionan keystores, contrasenas ni archivos de credenciales.

