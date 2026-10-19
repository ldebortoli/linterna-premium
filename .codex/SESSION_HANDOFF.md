# Session handoff · Librerías Android · 2026-10-19

- Entrega0.4.11/build19: Activity1.13.0, Core1.18.0, Splash1.2.0, Fragment1.9.1, Lifecycle2.10.0, Ads25.5.0. minSdk24/compile36 y toolchain preservados; Core1.19.1/Compose posterior/Lifecycle2.11 requieren SDK37. Billing9.1/UMP4.0 ya actuales.
- Validación nueva: npm test3/3, contratos y versiones;52JVM correctos, seis métricas JaCoCo100% (instrucciones59017,ramas155,líneas13339,complejidad179,métodos99,clases33), lintDemoDebug y assembleDemoDebug BUILD SUCCESSFUL. Logs ignorados reports/dependencies-*-20261019.log, APK local demoDebug en app/build/outputs. No se inicia AVD ni se distribuye.
- USER_QUEUE vacía. Publicar cambios scoped con noreply; no monitorear CI. Pipeline central automático sólo admite las tres Expo: este perfil nativo se compila localmente/desde Dashboard; no se creó una programación nueva. Mantener bloqueos de Play/AdMob/hardware/AVD originales.


- QA pendiente: usuario autorizó continuar sólo sin iniciar el AVD Google Play original. Su reparación offline y backup17/10 no demuestran arranque/QA. Windows native capture SetIsBorderRequired/0x80004002 continúa deshabilitada. No repetir mecanismos incompatibles ni cerrar procesos ajenos. Con recursos y autorización futura, dos boots fríos y luego11perfiles/33capturas/15RTL, teclado/TalkBack/insets; hardware físico y revisión lingüística nativa siguen pendientes.
- QA AOSP histórica16/10 válida sólo para0.4.10 y su alcance, no transferir al original ni a esta actualización de librerías. Última distribución Linterna0.4.9/build17; esta entrega no envía Telegram, no publica Play/Figma ni provisiona AdMob/pagos. Historial/evidencias y bloqueos precisos conservados en BACKLOG/DECISIONS/docs.
