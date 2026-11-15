# Historias de usuario · UNLaM Tuiter

Backlog del trabajo práctico, basado en el **Anexo III de la consigna v1.4.0**.
Cada historia está redactada con el formato **Como / Quiero / Para** y tiene sus
**criterios de aceptación** en formato **Dado / Cuando / Entonces**, incluyendo siempre
al menos un caso de error, lista vacía o dato inválido.

> **Cómo se usan:** cada historia se carga como un **issue** en el repositorio, se desarrolla
> en su rama `feature/…` y se cierra con el **pull request** que la resuelve. En este proyecto
> el repositorio todavía no tiene un remoto configurado, así que los issues no pudieron
> crearse en GitHub: el texto de abajo está listo para copiar y pegar (o cargar con `gh`,
> ver el final del archivo).
>
> **Nota:** los nombres de rama de la tabla son los **propuestos por la consigna**. En este
> repositorio, para que cada rama se pudiera compilar y mergear por separado, algunas
> historias se agruparon en ramas (`feature/HU-04-registro`, `feature/HU-05-login`,
> `feature/HU-06-perfil`, `feature/HU-01-07-11-feed-ux`). El detalle está en el
> [README → Gitflow](../README.md#gitflow).

## Índice

| Historia | Título | Épica | Requerimiento | Rama | Wireframe |
|---|---|---|---|---|---|
| [HU-01](#hu-01--ver-el-feed-de-posts) | Ver el feed de posts | #1 · Posts | R1 | `feature/HU-01-feed` | `feed.svg` |
| [HU-02](#hu-02--publicar-un-post) | Publicar un post | #1 · Posts | R2 | `feature/HU-02-publicar` | `publicar.svg` |
| [HU-03](#hu-03--guardar-un-borrador) | Guardar un borrador | #1 · Posts | R3 | `feature/HU-03-borrador` | `publicar.svg` |
| [HU-04](#hu-04--registrarme) | Registrarme | #2 · Usuarios | R4 | `feature/HU-04-registro` | `registro.svg` |
| [HU-05](#hu-05--iniciar-sesión) | Iniciar sesión | #2 · Usuarios | R4 | `feature/HU-05-login` | `login.svg` |
| [HU-06](#hu-06--ver-y-editar-mi-perfil) | Ver y editar mi perfil | #2 · Usuarios | R5 | `feature/HU-06-perfil` | `perfil.svg` |
| [HU-07](#hu-07--guardar-un-autor-como-favorito) | Guardar un autor como favorito | #2 · Usuarios | R6 | `feature/HU-07-guardar-favorito` | `feed.svg` |
| [HU-08](#hu-08--ver-mis-usuarios-favoritos) | Ver mis usuarios favoritos | #2 · Usuarios | R6 | `feature/HU-08-lista-favoritos` | `favoritos.svg` |
| [HU-09](#hu-09--ver-las-respuestas-de-un-post) | Ver las respuestas de un post | #3 · Interacción | R7 | `feature/HU-09-ver-respuestas` | `detalle.svg` |
| [HU-10](#hu-10--responder-un-post) | Responder un post | #3 · Interacción | R7 | `feature/HU-10-responder` | `detalle.svg` |
| [HU-11](#hu-11--dar-y-quitar-me-gusta) | Dar y quitar "me gusta" | #3 · Interacción | R8 | `feature/HU-11-likes` | `feed.svg` · `detalle.svg` |

---

## Épica #1 · Posts

### HU-01 · Ver el feed de posts

**Épica:** #1 · Posts — **Requerimiento:** R1 — **Endpoint:** `GET /api/v1/me/feed`

**Como** usuario logueado,
**quiero** ver los posts que se publicaron en la red,
**para** enterarme de lo que comparten los demás.

**Criterios de aceptación**

- **Escenario 1 · Hay posts publicados**
  **Dado** que inicié sesión y hay posts en la red,
  **cuando** abro la pantalla de feed,
  **entonces** veo los posts ordenados del más nuevo al más viejo
  **y** cada uno muestra autor, avatar, mensaje, fecha y cantidad de likes.
- **Escenario 2 · Carga**
  **Dado** que abrí el feed,
  **cuando** los posts todavía se están descargando,
  **entonces** veo un indicador de carga.
- **Escenario 3 · Paginado**
  **Dado** que estoy viendo el feed,
  **cuando** llego al final de la lista,
  **entonces** se cargan los posts de la página siguiente sin perder los que ya veía.
- **Escenario 4 · Sin conexión**
  **Dado** que no tengo conexión a internet,
  **cuando** abro el feed,
  **entonces** veo un mensaje de error y un botón para reintentar.

**Rama:** `feature/HU-01-feed` · **Wireframe:** [`docs/wireframes/feed.svg`](wireframes/feed.svg)

---

### HU-02 · Publicar un post

**Épica:** #1 · Posts — **Requerimiento:** R2 — **Endpoint:** `POST /api/v1/me/tuits`

**Como** usuario logueado,
**quiero** escribir y publicar un mensaje,
**para** compartirlo con el resto de la red.

**Criterios de aceptación**

- **Escenario 1 · Publicar**
  **Dado** que escribí un mensaje en la pantalla de publicación,
  **cuando** toco "Publicar",
  **entonces** vuelvo al feed y mi post aparece primero en la lista.
- **Escenario 2 · Mensaje vacío**
  **Dado** que el campo de texto está vacío o solo tiene espacios,
  **entonces** el botón "Publicar" está deshabilitado.
- **Escenario 3 · Envío en curso**
  **Dado** que toqué "Publicar",
  **cuando** la publicación todavía se está enviando,
  **entonces** veo un indicador de progreso y no puedo volver a tocar el botón,
  para no publicar dos veces.
- **Escenario 4 · Error**
  **Dado** que la API responde con un error,
  **cuando** intento publicar,
  **entonces** veo un mensaje de error y el texto que escribí sigue en el campo.

**Rama:** `feature/HU-02-publicar` · **Wireframe:** [`docs/wireframes/publicar.svg`](wireframes/publicar.svg)

---

### HU-03 · Guardar un borrador

**Épica:** #1 · Posts — **Requerimiento:** R3 — **Persistencia:** base de datos local (Room)

**Como** usuario que está escribiendo un post,
**quiero** guardar lo que escribí sin publicarlo,
**para** poder terminarlo más tarde sin perder el texto.

**Criterios de aceptación**

- **Escenario 1 · Guardar**
  **Dado** que escribí un texto en la pantalla de publicación,
  **cuando** toco "Guardar borrador",
  **entonces** veo una confirmación de que se guardó.
- **Escenario 2 · Recuperar**
  **Dado** que tengo un borrador guardado,
  **cuando** vuelvo a la pantalla de publicación, aunque haya cerrado la app,
  **entonces** el texto del borrador aparece cargado.
- **Escenario 3 · Publicar descarta el borrador**
  **Dado** que tengo un borrador cargado,
  **cuando** lo publico con éxito,
  **entonces** el borrador se elimina y la próxima vez la pantalla aparece vacía.
- **Escenario 4 · Texto vacío**
  **Dado** que el campo de texto está vacío,
  **entonces** el botón "Guardar borrador" está deshabilitado.

**Rama:** `feature/HU-03-borrador` · **Wireframe:** [`docs/wireframes/publicar.svg`](wireframes/publicar.svg)

---

## Épica #2 · Usuarios

### HU-04 · Registrarme

**Épica:** #2 · Usuarios — **Requerimiento:** R4 — **Endpoint:** `POST /api/v1/users`

**Como** persona sin cuenta,
**quiero** registrarme con mi nombre, email y contraseña,
**para** poder usar la red.

**Criterios de aceptación**

- **Escenario 1 · Registro exitoso**
  **Dado** que completé nombre, email y contraseña válidos,
  **cuando** toco "Registrarme",
  **entonces** veo una confirmación de que mi cuenta se creó
  **y** paso a la pantalla de inicio de sesión.
- **Escenario 2 · Campos incompletos**
  **Dado** que dejé algún campo vacío,
  **entonces** el botón "Registrarme" está deshabilitado.
- **Escenario 3 · Email inválido**
  **Dado** que escribí un email con formato inválido,
  **cuando** intento registrarme,
  **entonces** veo un mensaje junto al campo de email que indica el error.
- **Escenario 4 · Registro rechazado**
  **Dado** que la API rechaza el registro (por ejemplo, porque el email ya está registrado),
  **cuando** toco "Registrarme",
  **entonces** veo un mensaje que explica que no se pudo crear la cuenta
  **y** los datos que cargué siguen en el formulario.

**Rama:** `feature/HU-04-registro` · **Wireframe:** [`docs/wireframes/registro.svg`](wireframes/registro.svg)

---

### HU-05 · Iniciar sesión

**Épica:** #2 · Usuarios — **Requerimiento:** R4 — **Endpoint:** `POST /api/v1/login`
**Persistencia:** token de usuario

**Como** usuario registrado,
**quiero** iniciar sesión con mi email y contraseña,
**para** acceder a mi feed y a mi perfil.

**Criterios de aceptación**

- **Escenario 1 · Login exitoso**
  **Dado** que ingresé un email y una contraseña correctos,
  **cuando** toco "Ingresar",
  **entonces** entro a la pantalla de feed.
- **Escenario 2 · Datos incorrectos**
  **Dado** que ingresé una contraseña incorrecta,
  **cuando** toco "Ingresar",
  **entonces** veo un mensaje de error y sigo en la pantalla de inicio de sesión.
- **Escenario 3 · Sesión recordada**
  **Dado** que ya inicié sesión,
  **cuando** cierro la app y la vuelvo a abrir,
  **entonces** entro directamente al feed, sin volver a ingresar mis datos.
- **Escenario 4 · Ir al registro**
  **Dado** que estoy en la pantalla de inicio de sesión y no tengo cuenta,
  **cuando** toco "Crear cuenta",
  **entonces** voy a la pantalla de registro.

**Rama:** `feature/HU-05-login` · **Wireframe:** [`docs/wireframes/login.svg`](wireframes/login.svg)

---

### HU-06 · Ver y editar mi perfil

**Épica:** #2 · Usuarios — **Requerimiento:** R5 — **Endpoints:** `GET` / `PUT /api/v1/me/profile`

**Como** usuario logueado,
**quiero** ver y modificar mis datos de perfil,
**para** mantener actualizado cómo me ven los demás.

**Criterios de aceptación**

- **Escenario 1 · Ver perfil**
  **Dado** que inicié sesión,
  **cuando** abro la pantalla de perfil,
  **entonces** veo mi nombre, mi email y mi avatar.
- **Escenario 2 · Editar**
  **Dado** que estoy en mi perfil,
  **cuando** cambio mi nombre o la URL de mi avatar y toco "Guardar",
  **entonces** veo una confirmación y el perfil muestra los datos nuevos.
- **Escenario 3 · Email fijo**
  **Dado** que estoy editando mi perfil,
  **entonces** veo mi email pero no puedo modificarlo.
- **Escenario 4 · Cambiar contraseña**
  **Dado** que ingresé una contraseña nueva y guardé los cambios,
  **cuando** vuelvo a iniciar sesión,
  **entonces** ingreso con la contraseña nueva.
- **Escenario 5 · Error**
  **Dado** que la API responde con un error,
  **cuando** toco "Guardar",
  **entonces** veo un mensaje de error y los cambios que hice siguen en el formulario.

**Rama:** `feature/HU-06-perfil` · **Wireframe:** [`docs/wireframes/perfil.svg`](wireframes/perfil.svg)

---

### HU-07 · Guardar un autor como favorito

**Épica:** #2 · Usuarios — **Requerimiento:** R6 — **Persistencia:** base de datos local
(`author` y `avatar_url` del post)

**Como** usuario logueado,
**quiero** guardar como favorito al autor de un post,
**para** tener a mano a las personas que me interesan.

**Criterios de aceptación**

- **Escenario 1 · Guardar**
  **Dado** que estoy viendo un post en el feed,
  **cuando** toco "Agregar autor a favoritos",
  **entonces** veo una confirmación
  **y** el autor queda guardado en el dispositivo con su nombre y su avatar.
- **Escenario 2 · Ya es favorito**
  **Dado** que el autor de un post ya está en mis favoritos,
  **cuando** veo ese post en el feed,
  **entonces** la opción indica que ya es favorito y el autor no se guarda dos veces.
- **Escenario 3 · Persistencia**
  **Dado** que guardé un autor como favorito,
  **cuando** cierro la app y la vuelvo a abrir,
  **entonces** el autor sigue en mis favoritos.

**Rama:** `feature/HU-07-guardar-favorito` · **Wireframe:** [`docs/wireframes/feed.svg`](wireframes/feed.svg)

---

### HU-08 · Ver mis usuarios favoritos

**Épica:** #2 · Usuarios — **Requerimiento:** R6 — **Persistencia:** base de datos local

**Como** usuario logueado,
**quiero** ver la lista de autores que guardé como favoritos,
**para** encontrarlos rápido cuando quiera.

**Criterios de aceptación**

- **Escenario 1 · Ver lista**
  **Dado** que guardé autores como favoritos,
  **cuando** abro la pantalla de favoritos,
  **entonces** veo cada autor con su nombre y su avatar.
- **Escenario 2 · Lista vacía**
  **Dado** que todavía no guardé favoritos,
  **cuando** abro la pantalla de favoritos,
  **entonces** veo un mensaje que explica cómo agregarlos.
- **Escenario 3 · Quitar**
  **Dado** que estoy en la lista de favoritos,
  **cuando** quito a un autor,
  **entonces** desaparece de la lista y no vuelve a aparecer al reabrir la app.
- **Escenario 4 · Sin conexión**
  **Dado** que no tengo conexión a internet,
  **cuando** abro la pantalla de favoritos,
  **entonces** igual veo la lista completa.

**Rama:** `feature/HU-08-lista-favoritos` · **Wireframe:** [`docs/wireframes/favoritos.svg`](wireframes/favoritos.svg)

---

## Épica #3 · Interacción entre posts

### HU-09 · Ver las respuestas de un post

**Épica:** #3 · Interacción — **Requerimiento:** R7
**Endpoints:** `GET /api/v1/me/tuits/{id}` · `GET /api/v1/me/tuits/{id}/replies`

**Como** usuario logueado,
**quiero** ver las respuestas que recibió un post,
**para** seguir la conversación.

**Criterios de aceptación**

- **Escenario 1 · Ver respuestas**
  **Dado** que estoy en el feed,
  **cuando** toco un post,
  **entonces** veo el post completo y debajo, sus respuestas.
- **Escenario 2 · Sin respuestas**
  **Dado** un post que no tiene respuestas,
  **cuando** lo abro,
  **entonces** veo un mensaje que indica que todavía no tiene respuestas.
- **Escenario 3 · Error**
  **Dado** que la API responde con un error,
  **cuando** abro un post,
  **entonces** veo un mensaje de error y un botón para reintentar.

**Rama:** `feature/HU-09-ver-respuestas` · **Wireframe:** [`docs/wireframes/detalle.svg`](wireframes/detalle.svg)

---

### HU-10 · Responder un post

**Épica:** #3 · Interacción — **Requerimiento:** R7
**Endpoint:** `POST /api/v1/me/tuits/{id}/replies`

**Como** usuario logueado,
**quiero** responder un post,
**para** participar de la conversación.

**Criterios de aceptación**

- **Escenario 1 · Responder**
  **Dado** que estoy viendo un post,
  **cuando** escribo una respuesta y toco "Responder",
  **entonces** mi respuesta aparece en la lista de respuestas de ese post.
- **Escenario 2 · Respuesta vacía**
  **Dado** que el campo de respuesta está vacío,
  **entonces** el botón "Responder" está deshabilitado.
- **Escenario 3 · Error**
  **Dado** que la API responde con un error,
  **cuando** envío la respuesta,
  **entonces** veo un mensaje de error y el texto que escribí no se pierde.

**Rama:** `feature/HU-10-responder` · **Wireframe:** [`docs/wireframes/detalle.svg`](wireframes/detalle.svg)

---

### HU-11 · Dar y quitar "me gusta"

**Épica:** #3 · Interacción — **Requerimiento:** R8
**Endpoints:** `POST` / `DELETE /api/v1/me/tuits/{id}/likes`

**Como** usuario logueado,
**quiero** marcar un post con "me gusta" y poder desmarcarlo,
**para** mostrar qué publicaciones me interesan.

**Criterios de aceptación**

- **Escenario 1 · Dar like**
  **Dado** un post al que no le di like,
  **cuando** toco el ícono de "me gusta",
  **entonces** el ícono aparece activo y el contador sube en 1.
- **Escenario 2 · Quitar like**
  **Dado** un post al que ya le di like,
  **cuando** vuelvo a tocar el ícono,
  **entonces** el ícono aparece inactivo y el contador baja en 1.
- **Escenario 3 · Error al guardar**
  **Dado** que la API responde con un error,
  **cuando** toco el ícono de "me gusta",
  **entonces** el ícono y el contador vuelven a su estado anterior
  **y** veo un mensaje que avisa que no se pudo guardar.
- **Escenario 4 · Persistencia**
  **Dado** que le di like a un post,
  **cuando** recargo el feed,
  **entonces** el post sigue apareciendo con el like.

**Rama:** `feature/HU-11-likes` · **Wireframe:** [`docs/wireframes/feed.svg`](wireframes/feed.svg)

---

## Historias opcionales (no son requerimientos)

Estas funcionalidades no forman parte de R1–R8, pero están implementadas o sirven para
practicar la redacción de historias propias.

### HU-12 · Cerrar sesión

**Como** usuario logueado,
**quiero** cerrar sesión desde mi perfil,
**para** salir de mi cuenta en un dispositivo compartido.

**Criterios de aceptación**

- **Escenario 1 · Salir**
  **Dado** que estoy en mi perfil,
  **cuando** toco "Salir",
  **entonces** vuelvo al login y se borran el borrador, los favoritos y el token guardado.
- **Escenario 2 · Datos limpios**
  **Dado** que cerré sesión y vuelvo a abrir la app,
  **entonces** veo la pantalla de login, no el feed.

**Implementada en:** `data/repository/SessionRepository.kt`, `MainActivity.kt` (`logout()`).

### HU-13 · Ver el perfil de otro usuario

**Como** usuario logueado,
**quiero** ver el perfil de otro usuario,
**para** conocer más sobre quien publica.

**Endpoint:** `GET /api/v1/users/{user_id}`

**Estado:** no implementada (fuera del alcance de R1–R8). Queda como mejora futura.

### HU-14 · Actualizar el feed deslizando hacia abajo

**Como** usuario logueado,
**quiero** deslizar el feed hacia abajo para actualizarlo,
**para** ver los posts nuevos sin usar un botón.

**Estado:** no implementada. Hoy el feed se actualiza con el botón **Actualizar**.

### HU-15 · Ver solo posts principales

**Como** usuario logueado,
**quiero** ocultar las respuestas del feed,
**para** ver solo las publicaciones principales.

**Endpoint:** `GET /api/v1/me/feed?only_parents=true`

**Estado:** el parámetro `only_parents` ya está soportado por `PostRepository`/`TuiterApi`,
pero la pantalla no expone un interruptor. Queda como mejora futura.

---

## Cómo cargar estas historias como issues

El repositorio es **local** (no tiene remoto), por eso los issues no pudieron crearse.
Cuando se cree el repositorio en GitHub y se agregue el remoto:

```bash
# 1) Autenticarse con GitHub CLI (una sola vez)
gh auth login

# 2) Crear las etiquetas
gh label create "epica-1-posts"        --color "1d76db" --description "Épica #1 · Posts"
gh label create "epica-2-usuarios"     --color "0e8a16" --description "Épica #2 · Usuarios"
gh label create "epica-3-interaccion"  --color "5319e7" --description "Épica #3 · Interacción"
gh label create "R1" --color "fbca04"
gh label create "R2" --color "fbca04"
gh label create "R3" --color "fbca04"
gh label create "R4" --color "fbca04"
gh label create "R5" --color "fbca04"
gh label create "R6" --color "fbca04"
gh label create "R7" --color "fbca04"
gh label create "R8" --color "fbca04"

# 3) Crear un issue por historia (ejemplo con HU-01)
gh issue create \
  --title "HU-01 · Ver el feed de posts" \
  --body-file docs/historias-usuario.md \
  --label "epica-1-posts,R1"

# 4) Cerrar el issue desde el pull request
#    En el cuerpo del PR: "Closes #<numero-del-issue>"
```

> **Nota para la entrega:** la consigna pide que cada issue se cierre con el pull request
> que lo resuelve. Sin remoto no hay pull requests; en este repositorio las ramas
> `feature/*` se integraron a `develop` con `git merge --no-ff`, que deja un commit de merge
> equivalente. Al publicar el repositorio, se pueden recrear los PRs con `gh pr create`.
