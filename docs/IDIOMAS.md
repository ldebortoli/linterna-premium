# Idiomas · 0.4.9/17

El catálogo pasa de 21 a 250 opciones: conserva las 21 opciones, códigos y traducciones anteriores (incluidos `gn` y `quz`) y añade 229 catálogos de traducción automática. Cada uno contiene las 51 claves de esta app, sin descarga ni sustitución por otro idioma durante el uso.

El inventario se congeló antes de traducir: 251 candidatos documentados por [Google Cloud](https://docs.cloud.google.com/translate/docs/languages) y el [selector público de Google Translate](https://translate.google.com/), con alias resueltos contra el catálogo previo. No representa todos los recursos de Internet ni todas las variedades lingüísticas. Los nombres nuevos se tradujeron automáticamente al idioma correspondiente y conservan el código para distinguir escrituras y regiones.

`LANGUAGE_INVENTORY.json` es portátil: contiene las opciones previas, candidatos, fuentes/fechas/hashes, entrada efectiva, reformulaciones equivalentes para términos ambiguos y resultados por clave. `approved-machine` significa integridad técnica y ausencia de fragmentos fuente detectados, **no revisión nativa ni certificación lingüística**. El traductor puede usar préstamos; no se inventaron palabras para superar controles. Hace falta revisión especializada para calidad, tono y variantes, especialmente en lenguas con pocos recursos.

Candidatos no publicados: `jam`. Sus recursos devolvieron cadenas copiadas o fragmentos del idioma fuente; las claves exactas están en el inventario. No se muestran opciones parciales ni se sustituyen cadenas faltantes con español. La selección inicial de un idioma de dispositivo no compatible conserva el comportamiento previo: español argentino.

Los catálogos Kotlin están divididos en lotes para respetar los límites de inicialización de la JVM/DEX. El selector conserva estilo/scroll, elección persistente y variantes existentes. El diseño aplica RTL al idioma elegido, independientemente del idioma del teléfono. No modifica webs, audio, compras ni firma Play.

Validación reproducible: `npm test`, `npm run coverage`, `npm run lint`. La cobertura JVM incluye dominio, modelos, contratos y localización pura; el adaptador Android `PreferencesLanguageStore`, UI Compose y SDK externos requieren comprobación Android y no se presentan como cobertura JVM. El alcance, pruebas UI y limitaciones de la entrega se registran en `.codex/SESSION_HANDOFF.md`.

`LANGUAGE_PORTABLE.json` comparte la misma lista de códigos, etiquetas, dirección y procedencia entre ambas apps para alinear futuras webs; `npm test` verifica que coincida con lo que se instala. No modifica ni sincroniza sitios web.
