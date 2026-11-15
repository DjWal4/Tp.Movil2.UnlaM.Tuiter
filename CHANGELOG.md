# Changelog

Todos los cambios importantes de este proyecto se documentan en este archivo.
El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el versionado [SemVer](https://semver.org/lang/es/).

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

[1.0.0]: https://example.com/unlam-tuiter/releases/tag/v1.0.0
