# UNLaM Tuiter

Aplicación Android nativa del trabajo práctico integrador de **Programación Móvil 2 (UNLaM)**,
escrita en **Kotlin + Jetpack Compose**. Consume la API REST de Tuiter y permite registrarse,
iniciar sesión, ver el feed, publicar tuits, guardar borradores, responder, dar likes, marcar
usuarios favoritos, ver y editar el perfil y cerrar sesión.

**Versión:** `1.1.0` · **Consigna:** v1.4.0 · **Paquete:** `ar.edu.unlam.tuiter`

---

## Índice

1. [Equipo](#equipo)
2. [Requerimientos resueltos](#requerimientos-resueltos)
3. [Condiciones técnicas](#condiciones-técnicas)
4. [Funcionalidades](#funcionalidades)
5. [Tecnologías](#tecnologías)
6. [Arquitectura](#arquitectura)
7. [Estructura del proyecto](#estructura-del-proyecto)
8. [Cómo compilar y ejecutar](#cómo-compilar-y-ejecutar)
9. [Configurar el Application-Token](#configurar-el-application-token)
10. [API utilizada](#api-utilizada)
11. [Persistencia local](#persistencia-local)
12. [Manejo de errores](#manejo-de-errores)
13. [Wireframes](#wireframes)
14. [Historias de usuario](#historias-de-usuario)
15. [Gitflow](#gitflow)
16. [Decisiones de diseño](#decisiones-de-diseño)
17. [Limitaciones conocidas](#limitaciones-conocidas)

---

## Equipo

| Integrante | Rol | Contacto |
|---|---|---|
| **Walter** | Desarrollo, documentación y pruebas | augusto.wal.89@gmail.com |

> El trabajo se desarrolló de forma individual en este repositorio. Si se suma otro
> integrante, agregarlo a esta tabla y repartir las historias de usuario del backlog.

---

## Requerimientos resueltos

| # | Requerimiento | Estado | Historias | Dónde está implementado |
|---|---|---|---|---|
| **R1** | Feed de posts (autor, avatar, mensaje, fecha, likes, paginado) | ✅ | HU-01 | `ui/feed/FeedScreen.kt`, `ui/feed/FeedUiState.kt`, `PostRepository.getFeed()` |
| **R2** | Publicar un post | ✅ | HU-02 | `ui/newpost/NewPostScreen.kt`, `PostRepository.createPost()` |
| **R3** | Guardar y recuperar borrador (local) | ✅ | HU-03 | `ui/newpost/NewPostViewModel.kt`, `data/local/DraftEntity.kt`, `DraftRepository` |
| **R4** | Registro (`POST /users`) e inicio de sesión (`POST /login`) | ✅ | HU-04, HU-05 | `ui/register/`, `ui/login/`, `AuthRepository`, `SessionRepository` |
| **R5** | Ver y **editar** el perfil (`GET`/`PUT /me/profile`) | ✅ | HU-06 | `ui/profile/ProfileScreen.kt`, `ProfileViewModel.kt`, `AuthRepository.updateProfile()` |
| **R6** | Usuarios favoritos (tabla local) | ✅ | HU-07, HU-08 | `ui/favorites/`, `data/local/FavoriteUserEntity.kt`, `FavoriteRepository` |
| **R7** | Ver respuestas y responder un post | ✅ | HU-09, HU-10 | `ui/detail/PostDetailScreen.kt`, `PostRepository.getReplies()` / `replyToPost()` |
| **R8** | Dar y quitar "me gusta" (optimista) | ✅ | HU-11 | `FeedViewModel.toggleLike()`, `PostDetailViewModel.toggleLike()` |
| Extra | Cerrar sesión y limpiar datos locales | ✅ | HU-12 | `SessionRepository.logout()`, `MainActivity.logout()` |

### Detalle de la UX

- **Login**: formulario real de email y contraseña, con spinner y mensajes de error.
  Si ya hay una sesión guardada, la app abre **directo en el feed** (HU-05 esc. 3).
- **Registro**: nombre, email y contraseña. Valida el formato del email, deshabilita el botón
  con campos vacíos y conserva los datos si la API rechaza el alta.
- **Feed**: avatar, autor, fecha (formateada desde ISO 8601), texto, corazón (♡/♥) con contador,
  estrella de favorito (☆/★) por autor y botón **Responder**. La tarjeta completa abre el detalle.
  El **paginado es automático** al llegar al final de la lista, con estados "Cargando más...",
  "No hay más posts" y "Reintentar".
- **Nuevo post**: campo de texto, **Guardar borrador** y **Publicar**, con spinner y
  "Publicando..." mientras se envía. Al volver a entrar, el borrador se recupera solo.
- **Detalle**: post padre + respuestas + barra inferior para escribir y enviar una respuesta.
- **Perfil**: avatar, nombre editable, email **solo lectura**, URL del avatar y contraseña
  nueva opcional. Guarda solo los campos modificados y confirma con "Perfil actualizado.".
- **Favoritos**: lista de autores guardados, con avatar y botón **Quitar**. Se lee de Room,
  así que funciona sin internet.
- **Salir**: está en el perfil; borra borrador, favoritos y token, y vuelve al login.

---

## Condiciones técnicas

| Condición | Cómo se cumple |
|---|---|
| **Arquitectura Android** | MVVM + Repository, separación clara entre UI (`ui/`), presentación (ViewModels) y datos (`data/`). |
| **Patrón Observer** | La UI observa `StateFlow` con `collectAsState()`; cada cambio de estado redibuja la pantalla. |
| **Patrón Repositorio** | `PostRepository`, `AuthRepository`, `DraftRepository`, `FavoriteRepository` y `SessionRepository`. La UI nunca conoce Retrofit ni Room. |
| **Inyección de dependencias** | Contenedor manual `di/AppContainer.kt`, creado una sola vez en `TuiterApplication`. Los ViewModels reciben sus repositorios por constructor. |
| **Persistencia local** | Room 3 para borradores (R3) y usuarios favoritos (R6); `SharedPreferences` para el token de sesión. |
| **Gitflow** | `main`, `develop`, ramas `feature/*` integradas con `--no-ff` y `release/1.1.0`. Ver [Gitflow](#gitflow). |
| **Seguridad del token** | El `Application-Token` **no está en el código ni en el repositorio**: se lee de `local.properties` y se expone vía `BuildConfig`. Ver [Configurar el Application-Token](#configurar-el-application-token). |
| **Buenas prácticas** | Estados de carga/error/vacío unificados, sin lógica de negocio en la UI, nombres claros y commits descriptivos. |

---

## Tecnologías

| Componente | Versión | Uso |
|---|---|---|
| Kotlin | 2.2.10 | Lenguaje |
| Android Gradle Plugin (AGP) | 9.4.1 | Build (Kotlin integrado, sin `kotlin-android`) |
| Jetpack Compose (BOM) | 2026.02.01 | UI declarativa |
| Material 3 | BOM | Componentes visuales |
| Navigation | — | Navegación simple por estado en `MainActivity` (sin librería) |
| ViewModel + StateFlow | 2.8.7 | Estado y lógica de presentación |
| Retrofit + Gson | 2.11.0 | Consumo de la API REST |
| OkHttp (interceptor) | (incluido en Retrofit) | Headers `Application-Token` / `Authorization` |
| Room 3 + KSP | 3.0.3 / 2.3.14 | Base de datos local (borradores y favoritos) |
| Coil | 2.7.0 | Carga de avatares desde URL |
| Coroutines | (incluido en Compose/ViewModel) | Trabajo asincrónico |

- `minSdk = 24`, `targetSdk = 37`, `compileSdk = 37`.
- `applicationId = "ar.edu.unlam.tuiter"`.

---

## Arquitectura

Se usa el patrón **MVVM + Repository**, con **inyección de dependencias manual**
(sin Hilt ni Koin, para no sumar dependencias al build).

```
┌────────────────────────────── UI (Jetpack Compose) ──────────────────────────────┐
│  LoginScreen · RegisterScreen · FeedScreen · PostDetailScreen · NewPostScreen ·  │
│  FavoritesScreen · ProfileScreen                                                 │
│  Observan un StateFlow y emiten eventos (onClick). NO conocen Retrofit ni Room.  │
└───────────────────────────────▲───────────────────────────────┬──────────────────┘
                                │ state                         │ eventos
┌───────────────────────────────┴───────────────────────────────▼──────────────────┐
│  ViewModels: LoginViewModel · RegisterViewModel · FeedViewModel ·                 │
│              PostDetailViewModel · NewPostViewModel · FavoritesViewModel ·        │
│              ProfileViewModel                                                    │
│  Reciben sus repositorios por constructor (inyectados por AppContainer).          │
└───────────────────────────────┬──────────────────────────────────────────────────┘
                                │
┌───────────────────────────────▼──────────────────────────────────────────────────┐
│  Repositorios: PostRepository · AuthRepository · DraftRepository ·                │
│                FavoriteRepository · SessionRepository                            │
│  Único lugar que sabe cómo se obtienen/guardan los datos.                         │
└───────┬───────────────────────────────────────────────────────────┬──────────────┘
        │ Retrofit                                                  │ Room / SharedPreferences
┌───────▼──────────────────────────┐                    ┌───────────▼──────────────┐
│  TuiterApi (endpoints REST)      │                    │  TuiterDatabase (v2)     │
│  ApiClient (headers + base URL)  │                    │  drafts · favorite_users │
│  BuildConfig.APPLICATION_TOKEN   │                    │  SessionManager (token)  │
└──────────────────────────────────┘                    └──────────────────────────┘
```

### Inyección de dependencias

`TuiterApplication` (declarada en `AndroidManifest.xml`) crea **una sola vez** un `AppContainer`
con la API, la base de datos y los repositorios. Las pantallas piden lo que necesitan con
`rememberAppContainer()` y crean sus ViewModels con `viewModel { ... }`.

Beneficios: una única instancia de cada cosa, código testeable (se puede pasar un repositorio falso)
y un solo lugar donde cambiar cómo se construye algo.

---

## Estructura del proyecto

```
app/src/main/java/ar/edu/unlam/tuiter/
├── MainActivity.kt              # Activity única: navegación por estado (login, registro, feed…)
├── TuiterApplication.kt         # Crea el AppContainer y restaura la sesión
├── data/
│   ├── error/ErrorMapper.kt     # Excepción -> mensaje amigable
│   ├── local/                   # Room: entidades, DAOs y base de datos
│   ├── model/                   # Modelos de request/response de la API
│   ├── remote/                  # TuiterApi (endpoints) y ApiClient (Retrofit + headers)
│   ├── repository/              # Post, Auth, Draft, Favorite y Session repositories
│   └── session/SessionManager.kt# Token en memoria + SharedPreferences
├── di/AppContainer.kt           # Contenedor de dependencias
└── ui/
    ├── AppContainerProvider.kt  # Atajo para obtener el contenedor en Compose
    ├── components/StateViews.kt # LoadingView / ErrorView / EmptyView
    ├── detail/                  # R7: post + respuestas
    ├── favorites/               # R6: usuarios favoritos
    ├── feed/                    # R1 y R8: listado y likes
    ├── login/                   # R4 / HU-05: inicio de sesión
    ├── newpost/                 # R2 y R3: publicar y borrador
    ├── profile/                 # R5 / HU-06: ver y editar perfil
    ├── register/                # R4 / HU-04: registro
    └── theme/                   # Colores, tipografía y tema Material 3
```

Documentación:

```
docs/
├── historias-usuario.md         # Backlog HU-01 a HU-11 (+ opcionales)
└── wireframes/                  # Un wireframe SVG por pantalla + leyendas
    ├── README.md
    ├── login.svg
    ├── registro.svg
    ├── feed.svg
    ├── publicar.svg
    ├── detalle.svg
    ├── perfil.svg
    └── favoritos.svg
```

---

## Cómo compilar y ejecutar

### Requisitos

- **Android Studio** (versión compatible con AGP 9.4.1) o **JDK 21** para compilar por consola.
- **Android SDK 37** instalado (se descarga desde el SDK Manager).
- Un **dispositivo físico** (API 24+) o un **emulador**.
- Una cuenta en <https://tuiter.fragua.com.ar> y un **Application-Token** (ver la sección siguiente).

### Opción 1: Android Studio (recomendada)

1. `File → Open…` y elegir la carpeta del proyecto.
2. Configurar el token (ver [Configurar el Application-Token](#configurar-el-application-token)).
3. Esperar a que Gradle sincronice (la primera vez descarga dependencias).
4. Conectar el celular con **Depuración USB** activada (o iniciar un emulador).
5. Presionar **Run ▶**.

### Opción 2: consola

```powershell
# Compilar el APK de debug
.\gradlew.bat assembleDebug

# Instalarlo en el dispositivo conectado
adb install -r -t app\build\outputs\apk\debug\app-debug.apk
```

> Si `JAVA_HOME` no está configurado:
> ```powershell
> $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
> ```

### Primer uso

La app abre en **Inicio de sesión**. Si no tenés cuenta, tocá **"¿No tenés cuenta? Crear cuenta"**
y registrate con nombre, email y contraseña. Después volvé al login e ingresá con esos datos.

> Algunas cuentas nuevas pueden requerir **confirmar el email** antes de poder iniciar sesión;
> eso lo maneja la API de la cátedra, no la app.

---

## Configurar el Application-Token

> **Importante (condición obligatoria de seguridad):** el token **no** se escribe en ningún
> archivo `.kt` ni se sube al repositorio. Se configura por fuera del código, en
> `local.properties`, y se expone a la app mediante `BuildConfig.APPLICATION_TOKEN`.

### Paso 1 · Obtener el token

1. Crear una cuenta en <https://tuiter.fragua.com.ar> y confirmar el email.
2. Entrar al sitio y tocar **CREATE TOKEN**.
3. Copiar el token generado (es una cadena larga). Ese es el **Application-Token**.

### Paso 2 · Crear `local.properties`

El archivo `local.properties` **no se versiona** (está en `.gitignore`). Hay una plantilla
versionada: `local.properties.example`.

**Windows (PowerShell):**

```powershell
Copy-Item local.properties.example local.properties
```

**macOS / Linux:**

```bash
cp local.properties.example local.properties
```

### Paso 3 · Completar los valores

Abrir `local.properties` y dejarlo así:

```properties
# Ruta al Android SDK (Android Studio la completa sola).
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

# Token generado en tuiter.fragua.com.ar
APPLICATION_TOKEN=pega-aqui-tu-token
```

### Paso 4 · Recompilar

Gradle lee `local.properties` en `app/build.gradle.kts` y genera la constante:

```kotlin
buildConfigField("String", "APPLICATION_TOKEN", "\"$applicationToken\"")
```

Después de cambiar el token hay que **volver a compilar** (en Android Studio:
`Build → Rebuild Project`) para que `BuildConfig` se regenere.

### Verificación rápida

Si el token falta, el login muestra el mensaje:
*"Falta configurar el Application-Token en local.properties. Mirá el README."*
y `ApiClient.isConfigured` devuelve `false`.

### ¿Por qué así?

- `local.properties` está en `.gitignore` → el token nunca llega a Git.
- `local.properties.example` sí se versiona → sirve de guía para otro integrante.
- `BuildConfig` permite usar el token como una constante de Kotlin sin escribirlo en el código.
- Si el token se filtrara, se puede revocar y generar uno nuevo en el sitio sin tocar el código.

---

## API utilizada

- **Base URL:** `https://tuiter.fragua.com.ar/api/v1/`
- **Documentación interactiva (Swagger):** <https://tuiter.fragua.com.ar/api/documentation>
- **Headers** (los agrega el interceptor de `ApiClient`):
  - `Application-Token`: token de la aplicación (siempre), leído de `BuildConfig`.
  - `Authorization`: token del usuario logueado (solo si hay sesión).

| Método | Endpoint | Uso | Requerimiento |
|---|---|---|---|
| `POST` | `users` | Registrar un usuario | R4 |
| `POST` | `login` | Iniciar sesión y obtener el token | R4 |
| `GET` | `me/profile` | Ver el perfil propio | R5 |
| `PUT` | `me/profile` | Editar nombre, avatar y contraseña | R5 |
| `GET` | `me/feed?page=&only_parents=` | Feed paginado | R1 |
| `POST` | `me/tuits` | Publicar un tuit | R2 |
| `GET` | `me/tuits/{id}` | Ver un tuit puntual | R7 |
| `GET` | `me/tuits/{id}/replies` | Ver las respuestas | R7 |
| `POST` | `me/tuits/{id}/replies` | Publicar una respuesta | R7 |
| `POST` | `me/tuits/{id}/likes` | Dar like | R8 |
| `DELETE` | `me/tuits/{id}/likes` | Quitar like | R8 |

> **Detalle importante:** cuando una página del feed o un post sin respuestas no tiene datos,
> la API responde **HTTP 200 con el body `null`**. Por eso esos endpoints se declaran como
> `Response<List<Post>>` y el repositorio hace `.body() ?: emptyList()`.
> Además, como Retrofit no lanza excepción al devolver `Response<T>`, el repositorio verifica
> `isSuccessful` y lanza `HttpException` para que la UI muestre el error correcto.

---

## Persistencia local

**Room 3** (`androidx.room3`), base `tuiter.db`, versión **2**.

| Tabla | Campos | Uso |
|---|---|---|
| `drafts` | `id` (PK fija = 1), `message` | Borrador del nuevo post (R3) |
| `favorite_users` | `authorName` (PK), `avatarUrl` | Usuarios favoritos (R6) |

La versión 2 agrega `favorite_users` mediante una **migración no destructiva**
(`MIGRATION_1_2`), para no perder el borrador del usuario.

La **sesión** (token) se guarda aparte, en `SharedPreferences` (`tuiter_session` / `user_token`),
y se restaura al abrir la app. El logout borra las dos tablas y el token.

---

## Manejo de errores

- `ErrorMapper` traduce excepciones a mensajes claros:
  sin conexión, timeout, 400, 401, 403, 404, **409** (email ya registrado),
  **422** (datos inválidos), 5xx y errores inesperados.
- `StateViews` define `LoadingView`, `ErrorView` (con **Reintentar**) y `EmptyView`,
  reutilizados por el feed, el detalle, el perfil y favoritos.
- Los likes usan **actualización optimista**: la UI cambia al instante y, si la API falla,
  se revierte el cambio y se muestra el error.
- El login distingue un **401 de credenciales incorrectas** de una sesión vencida.

---

## Wireframes

Un wireframe de baja fidelidad por cada pantalla, con los elementos numerados, la leyenda y
los estados de carga / vacío / error:

📁 **[`docs/wireframes/`](docs/wireframes/)** · índice con leyendas: [`docs/wireframes/README.md`](docs/wireframes/README.md)

| Pantalla | Archivo | Historias |
|---|---|---|
| Inicio de sesión | [`login.svg`](docs/wireframes/login.svg) | HU-05 |
| Registro | [`registro.svg`](docs/wireframes/registro.svg) | HU-04 |
| Feed | [`feed.svg`](docs/wireframes/feed.svg) | HU-01, HU-07, HU-11 |
| Publicar post | [`publicar.svg`](docs/wireframes/publicar.svg) | HU-02, HU-03 |
| Detalle de post | [`detalle.svg`](docs/wireframes/detalle.svg) | HU-09, HU-10, HU-11 |
| Perfil | [`perfil.svg`](docs/wireframes/perfil.svg) | HU-06 |
| Usuarios favoritos | [`favoritos.svg`](docs/wireframes/favoritos.svg) | HU-08 |

Los `.svg` se abren con cualquier navegador o desde Android Studio
(clic derecho → *Open in Browser*).

---

## Historias de usuario

El backlog completo (HU-01 a HU-11, más las opcionales) está en:

📄 **[`docs/historias-usuario.md`](docs/historias-usuario.md)**

Cada historia tiene formato **Como / Quiero / Para**, criterios **Dado / Cuando / Entonces**,
el requerimiento que cubre, el endpoint, el nombre propuesto de rama y el wireframe asociado.
Al final del archivo están los comandos `gh` para cargarlas como issues cuando el repositorio
tenga un remoto.

---

## Gitflow

El repositorio sigue **Gitflow**:

| Rama | Propósito |
|---|---|
| `main` | Versión estable / entregable (etiquetada) |
| `develop` | Integración de las funcionalidades |
| `feature/*` | Una rama por funcionalidad, se integra a `develop` con `--no-ff` |
| `release/1.1.0` | Preparación de la entrega; se integra a `main` y a `develop` |

Ramas de funcionalidad de la versión 1.1.0:

```
feature/seguridad-token
feature/HU-04-registro
feature/HU-05-login
feature/HU-06-perfil
feature/HU-01-07-11-feed-ux
feature/docs-wireframes
```

Ramas de la versión 1.0.0:

```
feature/capa-datos
feature/inyeccion-dependencias
feature/manejo-errores
feature/r1-feed
feature/r2-r3-publicar-borrador
feature/r7-detalle-respuestas
feature/r6-favoritos
feature/r4-r5-login-perfil
```

Los mensajes de commit siguen **Conventional Commits** en español:
`feat(...)`, `fix(...)`, `chore(...)`, `docs(...)`, `refactor(...)`.

Para ver el historial completo:

```bash
git log --graph --oneline --all --decorate
```

Versiones entregadas: **`v1.0.0`** y **`v1.1.0`** (tags sobre `main`).

> **Limitación:** el repositorio es local y no tiene remoto configurado. Por eso los pull
> requests e issues de GitHub no pudieron crearse: las ramas se integraron con
> `git merge --no-ff`, que deja un commit de merge equivalente. Al publicar el repositorio,
> se pueden recrear los PRs y cerrar los issues con `gh pr create`.

---

## Decisiones de diseño

- **Compose en lugar de XML**: menos archivos, UI declarativa y estados más simples de manejar.
- **Navegación por estado** (`remember { mutableStateOf(...) }`) en lugar de Navigation Compose:
  la app tiene pocas pantallas y así se evita una dependencia extra.
- **DI manual (`AppContainer`)** en lugar de Hilt/Koin: cumple el requisito sin sumar
  procesadores de anotaciones ni riesgo de build.
- **Favoritos por `authorName`**: la API no expone un `author_id` estable en todos los endpoints.
- **Migración de Room en vez de `fallbackToDestructiveMigration()`**: conserva el borrador.
- **Token en `SharedPreferences`**: la sesión sobrevive al cierre de la app.
- **Token de aplicación en `BuildConfig`**: cumple la condición de seguridad de la consigna.
- **`PUT /me/profile` con campos opcionales**: se envía solo lo que el usuario modificó.
- **Modelos con campos `nullable`**: la API puede omitir campos; así se evitan crashes.

---

## Limitaciones conocidas

- **Sin remoto de Git**: no hay pull requests ni issues en GitHub (ver [Gitflow](#gitflow)).
- **Registro con confirmación de email**: la API de la cátedra puede exigir confirmar el correo
  antes del primer login; la app no puede automatizar ese paso.
- **`only_parents`**: el repositorio soporta el parámetro, pero la UI todavía no expone un
  interruptor para ocultar respuestas en el feed.
- **Pull to refresh**: no implementado; el feed se recarga con el botón **Actualizar**.
- **Ver perfil de otro usuario** (`GET /users/{id}`): fuera del alcance de R1–R8.
