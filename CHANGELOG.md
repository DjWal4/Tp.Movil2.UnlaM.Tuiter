# Changelog

Todos los cambios importantes de este proyecto se documentan en este archivo.
El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el versionado [SemVer](https://semver.org/lang/es/).

## [1.1.0] - 2026-11-15

Adecuación del proyecto a la **consigna v1.4.0**: registro real, login con formulario,
edición de perfil, seguridad del token y documentación obligatoria.

### Agregado

- **R4 – Registro** (`POST /users`): pantalla con nombre, email y contraseña, validación de
  formato de email, botón deshabilitado con campos vacíos y conservación de los datos si la
  API rechaza el alta (HU-04).
- **R4 – Login real** (HU-05): formulario de email y contraseña, mensaje de credenciales
  incorrectas, spinner de carga y **sesión recordada** al reabrir la app.
- **R5 – Edición de perfil** (`PUT /me/profile`): nombre, URL del avatar y contraseña nueva
  opcional; el email se muestra pero no se edita (HU-06).
- **R5 – Logout** en la pantalla de perfil: limpia borrador, favoritos y token.
- **R1 – Paginado automático** al llegar al final del feed, con pie de lista
  ("Cargando más...", "No hay más posts", "Reintentar").
- **R1/R7 – Tarjeta de post clickeable** para abrir el detalle de un post.
- **R6 – Confirmación (snackbar)** al agregar o quitar un autor de favoritos.
- **R2 – Feedback de publicación**: spinner + "Publicando..." y bloqueo de "Volver".
- **R7 – Botón "Responder"** en el detalle del post.
- **Documentación**: `docs/wireframes/` con un SVG por pantalla (login, registro, feed,
  publicar, detalle, perfil, favoritos), sus leyendas, los estados de carga / vacío / error
  y el mapeo a historias; `docs/historias-usuario.md` con el backlog HU-01 a HU-11; README
  actualizado con equipo, requisitos, token y wireframes.

### Cambiado

- **Seguridad (condición obligatoria)**: el `Application-Token` ya **no está en el código**.
  Se lee de `local.properties` (ignorado por Git) y se expone como
  `BuildConfig.APPLICATION_TOKEN`. Se agregó `local.properties.example` como plantilla.
- **`MainActivity`**: arranca en el login real (o directo en el feed si hay sesión),
  y navega a registro, perfil, favoritos, detalle y nuevo post.
- **`ApiClient`**: usa `BuildConfig.APPLICATION_TOKEN` y expone `isConfigured` para avisar
  si falta configurar el token.
- **`ErrorMapper`**: agrega mensajes para **409** (email ya registrado) y **422** (datos inválidos).
- **`AppContainer`**: provee `AuthRepository`.
- **README**: reescrito con el estado real de R1–R8, las condiciones técnicas, la configuración
  del token, los wireframes y las limitaciones.

### Corregido

- El login ya no usa credenciales escritas en el código.
- El botón "Salir" se movió del feed al perfil, para dejar lugar a "★ Favoritos" y "Perfil".
- El botón de enviar respuesta se llama "Responder" (antes "Enviar").
- **Perfil (`PUT /me/profile`)**: el servidor valida `avatar_url` como obligatorio y respondía
  **500** al guardar solo el nombre. Ahora la app envía siempre `name` y `avatar_url` (y la
  contraseña solo si se escribe una nueva). Detectado en la verificación en el celular.
- **Login**: al volver del registro ya no queda visible el error de un intento de login
  anterior junto al aviso "Cuenta creada...".
- **Login**: el texto que se escribe en los campos se ve más oscuro y legible, también con
  el tema dinámico de Android (nuevo `ui/theme/TextFieldColors.kt`).

### Notas técnicas

- Sin cambios de versiones: Kotlin 2.2.10, AGP 9.4.1, Compose BOM 2026.02.01,
  Retrofit 2.11.0, Room 3.0.3, KSP 2.3.14.
- `buildConfig = true` habilitado en `app/build.gradle.kts` para generar `BuildConfig`.
- Room sigue en versión 2; no se agregaron tablas nuevas.

## [1.0.0] - 2026-11-15

Primera versión entregable de **UNLaM Tuiter**.

### Agregado

- **R1 – Feed**: listado de tuits con avatar, autor, fecha, paginación ("Cargar más") y recarga manual.
- **R2 – Publicar**: creación de tuits contra `POST /me/tuits`.
- **R3 – Borrador**: guardado y recuperación del borrador en Room (persistente entre sesiones).
- **R4 – Login**: inicio de sesión con token guardado en `SharedPreferences`.
- **R5 – Perfil**: consulta del perfil del usuario logueado (`GET /me/profile`).
- **R6 – Favoritos**: usuarios favoritos en una segunda tabla de Room, con alta/baja desde el feed.
- **R7 – Respuestas**: pantalla de detalle con el post padre, sus respuestas y publicación de respuestas.
- **R8 – Likes**: dar/quitar "me gusta" con actualización optimista y reversión ante error.
- **Inyección de dependencias** manual mediante `AppContainer`.
- **Logout**: limpieza de borrador, favoritos y token; vuelta al login.
- **Manejo de errores** unificado (`ErrorMapper`) y estados de carga / error / vacío reutilizables.
- **Documentación**: `README.md`, `.gitignore`, `.gitattributes` y este changelog.

### Notas técnicas

- Kotlin 2.2.10, AGP 9.4.1, Jetpack Compose (BOM 2026.02.01), Retrofit 2.11.0, Room 3.0.3.
- `minSdk 24`, `targetSdk 37`, `compileSdk 37`.
- Migración no destructiva de Room 1 → 2 para conservar el borrador al agregar favoritos.

[1.1.0]: https://example.com/unlam-tuiter/releases/tag/v1.1.0
[1.0.0]: https://example.com/unlam-tuiter/releases/tag/v1.0.0
