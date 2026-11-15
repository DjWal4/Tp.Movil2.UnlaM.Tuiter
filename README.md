# UNLaM Tuiter

Aplicación Android nativa del trabajo práctico de **UNLaM**, escrita en **Kotlin + Jetpack Compose**.
Consume la API REST de Tuiter y permite ver el feed, publicar tuits, guardar borradores, responder,
dar likes, marcar usuarios favoritos y cerrar sesión.

---

## Índice

1. [Funcionalidades](#funcionalidades)
2. [Tecnologías](#tecnologías)
3. [Arquitectura](#arquitectura)
4. [Estructura del proyecto](#estructura-del-proyecto)
5. [Cómo compilar y ejecutar](#cómo-compilar-y-ejecutar)
6. [API utilizada](#api-utilizada)
7. [Persistencia local](#persistencia-local)
8. [Manejo de errores](#manejo-de-errores)
9. [Gitflow](#gitflow)
10. [Decisiones de diseño](#decisiones-de-diseño)
11. [Autor](#autor)

---

## Funcionalidades

| # | Requisito | Estado | Dónde está implementado |
|---|---|---|---|
| **R1** | Ver el feed de tuits con paginación ("Cargar más") | ✅ | `ui/feed/FeedScreen.kt`, `ui/feed/FeedUiState.kt`, `PostRepository.getFeed()` |
| **R2** | Publicar un tuit nuevo | ✅ | `ui/newpost/NewPostScreen.kt`, `PostRepository.createPost()` |
| **R3** | Guardar y recuperar un borrador (persistente) | ✅ | `ui/newpost/NewPostViewModel.kt`, `data/local/DraftEntity.kt`, `data/repository/DraftRepository.kt` |
| **R4** | Login de usuario (token de sesión) | ✅ | `MainActivity.kt` (`LoginTestScreen`), `data/session/SessionManager.kt` |
| **R5** | Ver el perfil del usuario logueado | ✅ | `MainActivity.kt`, `TuiterApi.getProfile()` |
| **R6** | Usuarios favoritos (segunda tabla en Room) | ✅ | `ui/favorites/`, `data/local/FavoriteUserEntity.kt`, `data/repository/FavoriteRepository.kt` |
| **R7** | Ver las respuestas de un post y publicar una respuesta | ✅ | `ui/detail/PostDetailScreen.kt`, `PostRepository.getReplies()` / `replyToPost()` |
| **R8** | Dar y quitar "me gusta" (optimista) | ✅ | `FeedViewModel.toggleLike()`, `PostDetailViewModel.toggleLike()` |
| **DI** | Inyección de dependencias | ✅ | `di/AppContainer.kt`, `TuiterApplication.kt` |
| **Logout** | Cerrar sesión y limpiar datos locales | ✅ | `data/repository/SessionRepository.kt`, botón "Salir" del feed |
| **Errores** | Estados de carga / error / vacío unificados | ✅ | `data/error/ErrorMapper.kt`, `ui/components/StateViews.kt` |

### Detalle de la UX

- **Feed**: lista con avatar, autor, fecha (formateada de ISO 8601), texto, corazón (♡/♥) con contador,
  estrella de favorito (☆/★) por autor, botón **Responder** y botón **Cargar más**.
- **Nuevo post**: campo de texto, **Guardar borrador** y **Publicar**. Al volver a entrar, el borrador se recupera solo.
- **Detalle**: post padre + lista de respuestas + barra inferior para escribir y enviar una respuesta.
- **Favoritos**: lista de autores marcados, con avatar y botón **Quitar**.
- **Sesión**: el token se guarda en `SharedPreferences`, así la sesión sobrevive al cierre de la app.
  **Salir** borra el borrador, los favoritos y el token, y vuelve al login.

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
│  FeedScreen · PostDetailScreen · NewPostScreen · FavoritesScreen · LoginScreen   │
│  Observan un StateFlow y emiten eventos (onClick). NO conocen Retrofit ni Room.  │
└───────────────────────────────▲───────────────────────────────┬──────────────────┘
                                │ state                         │ eventos
┌───────────────────────────────┴───────────────────────────────▼──────────────────┐
│  ViewModels: FeedViewModel · PostDetailViewModel · NewPostViewModel ·            │
│              FavoritesViewModel                                                  │
│  Reciben sus repositorios por constructor (inyectados por AppContainer).          │
└───────────────────────────────┬──────────────────────────────────────────────────┘
                                │
┌───────────────────────────────▼──────────────────────────────────────────────────┐
│  Repositorios: PostRepository · DraftRepository · FavoriteRepository ·            │
│                SessionRepository                                                 │
│  Único lugar que sabe cómo se obtienen/guardan los datos.                         │
└───────┬───────────────────────────────────────────────────────────┬──────────────┘
        │ Retrofit                                                  │ Room
┌───────▼──────────────────────────┐                    ┌───────────▼──────────────┐
│  TuiterApi (endpoints REST)      │                    │  TuiterDatabase (v2)     │
│  ApiClient (headers + base URL)  │                    │  drafts · favorite_users │
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
├── MainActivity.kt              # Activity única: login, perfil y navegación por estado
├── TuiterApplication.kt         # Crea el AppContainer y restaura la sesión
├── data/
│   ├── error/ErrorMapper.kt     # Excepción -> mensaje amigable
│   ├── local/                   # Room: entidades, DAOs y base de datos
│   ├── model/                   # Modelos de request/response de la API
│   ├── remote/                  # TuiterApi (endpoints) y ApiClient (Retrofit + headers)
│   ├── repository/              # PostRepository, DraftRepository, FavoriteRepository, SessionRepository
│   └── session/SessionManager.kt# Token en memoria + SharedPreferences
├── di/AppContainer.kt           # Contenedor de dependencias
└── ui/
    ├── AppContainerProvider.kt  # Atajo para obtener el contenedor en Compose
    ├── components/StateViews.kt # LoadingView / ErrorView / EmptyView
    ├── detail/                  # R7: post + respuestas
    ├── favorites/               # R6: usuarios favoritos
    ├── feed/                    # R1 y R8: listado y likes
    ├── newpost/                 # R2 y R3: publicar y borrador
    └── theme/                   # Colores, tipografía y tema Material 3
```

---

## Cómo compilar y ejecutar

### Requisitos

- **Android Studio** (versión compatible con AGP 9.4.1) o **JDK 21** para compilar por consola.
- **Android SDK 37** instalado (se descarga desde el SDK Manager).
- Un **dispositivo físico** (API 24+) o un **emulador**.

### Opción 1: Android Studio (recomendada)

1. `File → Open…` y elegir la carpeta del proyecto.
2. Esperar a que Gradle sincronice (la primera vez descarga dependencias).
3. Conectar el celular con **Depuración USB** activada (o iniciar un emulador).
4. Presionar **Run ▶**.

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

### Credenciales de prueba

La pantalla inicial (de prueba) tiene el login hardcodeado con un usuario de la cátedra.
Para usar otro usuario, editar `MainActivity.kt` → `LoginTestScreen` → `LoginRequest(...)`.

---

## API utilizada

- **Base URL:** `https://tuiter.fragua.com.ar/api/v1/`
- **Headers** (los agrega el interceptor de `ApiClient`):
  - `Application-Token`: token de la aplicación (siempre).
  - `Authorization`: token del usuario logueado (solo si hay sesión).

| Método | Endpoint | Uso |
|---|---|---|
| `POST` | `login` | Iniciar sesión y obtener el token |
| `GET` | `me/profile` | Perfil del usuario logueado |
| `GET` | `me/feed?page=&only_parents=` | Feed paginado |
| `POST` | `me/tuits` | Crear un tuit |
| `GET` | `me/tuits/{id}` | Ver un tuit puntual |
| `POST` | `me/tuits/{id}/likes` | Dar like |
| `DELETE` | `me/tuits/{id}/likes` | Quitar like |
| `GET` | `me/tuits/{id}/replies` | Respuestas de un tuit |
| `POST` | `me/tuits/{id}/replies` | Publicar una respuesta |

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

La **sesión** (token) se guarda aparte, en `SharedPreferences` (`tuiter_session` / `user_token`).

---

## Manejo de errores

- `ErrorMapper` traduce excepciones a mensajes claros:
  sin conexión, timeout, 400, 401, 403, 404, 5xx y errores inesperados.
- `StateViews` define `LoadingView`, `ErrorView` (con **Reintentar**) y `EmptyView`,
  reutilizados por el feed, el detalle y favoritos.
- Los likes usan **actualización optimista**: la UI cambia al instante y, si la API falla,
  se revierte el cambio y se muestra el error.

---

## Gitflow

El repositorio sigue un flujo simplificado de **Gitflow**:

| Rama | Propósito |
|---|---|
| `main` | Versión estable / entregable (etiquetada) |
| `develop` | Integración de las funcionalidades |
| `feature/*` | Una rama por funcionalidad, se integra a `develop` con `--no-ff` |
| `release/1.0.0` | Preparación de la entrega; se integra a `main` y a `develop` |

Ramas de funcionalidad:

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

Versión entregada: **`v1.0.0`** (tag sobre `main`).

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
- **Modelos con campos `nullable`**: la API puede omitir campos; así se evitan crashes.

---

## Autor

- **Walter** — UNLaM
- Proyecto: `ar.edu.unlam.tuiter` — versión 1.0.0
