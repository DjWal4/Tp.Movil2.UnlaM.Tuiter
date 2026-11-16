# Wireframes · UNLaM Tuiter

Bocetos de **baja fidelidad** de cada pantalla de la app. Muestran qué elementos tiene,
cómo se ordenan y a dónde lleva cada acción. No definen colores ni diseño final: eso se
resuelve en el código.

> Requisito de la consigna (Anexo IV): un wireframe por pantalla, con los elementos
> **numerados** y una **leyenda** que explique qué hace cada uno y qué historia de usuario
> cubre. Además del estado normal, se dibujan los estados de **carga**, **lista vacía** y
> **error** cuando la pantalla los tiene.

## Índice

| Pantalla | Archivo | Historias |
|---|---|---|
| Inicio de sesión | [`login.svg`](login.svg) | HU-05 |
| Registro | [`registro.svg`](registro.svg) | HU-04 |
| Feed | [`feed.svg`](feed.svg) | HU-01, HU-07, HU-11 |
| Publicar post | [`publicar.svg`](publicar.svg) | HU-02, HU-03 |
| Detalle de post | [`detalle.svg`](detalle.svg) | HU-09, HU-10, HU-11 |
| Perfil | [`perfil.svg`](perfil.svg) | HU-06 |
| Usuarios favoritos | [`favoritos.svg`](favoritos.svg) | HU-08 |

### Cómo verlos

- **Android Studio / IntelliJ:** clic derecho sobre el `.svg` → *Open in Browser*.
- **Navegador:** arrastrar el archivo a Chrome, Edge o Firefox.
- **VS Code:** extensión *SVG Preview*.

Cada archivo tiene varios **marcos** uno al lado del otro: el primero es el estado normal y
los siguientes son las variantes (cargando, vacío, error). La leyenda de abajo indica qué
marco es cada uno.

---

## 1 · Inicio de sesión (`login.svg`) · HU-05

| # | Elemento | Qué hace | Historia |
|---|---|---|---|
| 1 | Campo **Email** | El usuario escribe su email. El teclado muestra `@` y `.com`. | HU-05 |
| 2 | Campo **Contraseña** | Se escribe oculta (`••••`). | HU-05 |
| 3 | Botón **Ingresar** | Llama a `POST /login`. Se deshabilita si falta un campo o mientras carga. | HU-05 esc. 1 |
| 4 | Enlace **¿No tenés cuenta? Crear cuenta** | Navega a la pantalla de registro. | HU-05 esc. 4 · HU-04 |
| 5 | Estado **cargando** | Spinner + "Ingresando..." dentro del botón; los campos quedan bloqueados. | HU-05 esc. 1 |
| 6 | Estado **error** | Mensaje rojo debajo del botón ("Email o contraseña incorrectos"). El usuario sigue en el login. | HU-05 esc. 2 |

**Sesión recordada (HU-05 esc. 3):** no se dibuja como pantalla aparte porque no muestra
nada nuevo. Si ya hay token guardado, la app abre directamente el feed.

---

## 2 · Registro (`registro.svg`) · HU-04

| # | Elemento | Qué hace | Historia |
|---|---|---|---|
| 1 | **Volver** | Cancela el registro y vuelve al login. | HU-04 |
| 2 | Campo **Nombre** | Nombre del usuario nuevo. | HU-04 esc. 1 |
| 3 | Campo **Email** | Email del usuario. Se valida el formato. | HU-04 esc. 3 |
| 4 | Campo **Contraseña** | Contraseña del usuario nuevo. | HU-04 esc. 1 |
| 5 | Botón **Registrarme** | Llama a `POST /users`. Deshabilitado si falta un campo. | HU-04 esc. 2 |
| 6 | Error de **formato de email** | Texto rojo debajo del campo, con el borde en rojo. | HU-04 esc. 3 |
| 7 | Botón **deshabilitado** | Mientras el email es inválido no se puede enviar el formulario. | HU-04 esc. 3 |
| 8 | Error de la **API** | "Ese email ya está registrado". Los datos cargados siguen en el formulario. | HU-04 esc. 4 |
| 9 | Estado **enviando** | Spinner + "Registrando..."; los campos y **Volver** quedan deshabilitados. | HU-04 esc. 1 |

Al registrarse bien, la app vuelve al login con el aviso **"Cuenta creada. Ya podés iniciar
sesión."** (HU-04 esc. 1).

---

## 3 · Feed (`feed.svg`) · HU-01, HU-07, HU-11

| # | Elemento | Qué hace | Historia |
|---|---|---|---|
| 1 | **★ Favoritos** (ícono en la barra superior) | Abre la pantalla de usuarios favoritos. | HU-08 |
| 2 | **👤 Perfil** (ícono en la barra superior) | Abre el perfil del usuario logueado. | HU-06 |
| 3 | Tarjeta de post | Muestra autor, avatar, fecha y mensaje. Al tocarla se abre el detalle. | HU-01 · HU-09 |
| 4 | **Me gusta** (♡/♥ + contador) | Da o quita like. La segunda tarjeta muestra el estado activo. | HU-11 |
| 5 | **☆/★ Favorito** | Guarda o quita al autor de la lista local de favoritos. | HU-07 |
| 6 | **💬 Responder** | Abre el detalle del post para escribir una respuesta. | HU-10 |
| 7 | **Paginado** | Al llegar al final de la lista se carga la página siguiente ("Cargando más..."). | HU-01 esc. 3 |
| 8 | Botón flotante **+** | Abre la pantalla de publicación. | HU-02 |
| 9 | Estado **cargando** | Spinner centrado mientras llega la primera página. | HU-01 esc. 2 |
| 10 | Estado **lista vacía** | "Todavía no hay posts. Tocá el botón + para publicar el primero." | HU-01 |
| 11 | Estado **error** | Mensaje de red ("Sin conexión a internet..."). | HU-01 esc. 4 |
| 12 | Botón **Reintentar** | Vuelve a pedir el feed después de un error. | HU-01 esc. 4 |

El ícono **⟳ Actualizar** (barra superior, junto a la estrella) recarga el feed desde la página 1.

---

## 4 · Publicar post (`publicar.svg`) · HU-02, HU-03

| # | Elemento | Qué hace | Historia |
|---|---|---|---|
| 1 | **Volver** | Cierra la pantalla sin publicar. Se deshabilita mientras se envía. | HU-02 esc. 3 |
| 2 | Campo de **texto** | Mensaje del post o del borrador. | HU-02 · HU-03 |
| 3 | Botón **Guardar borrador** | Guarda el texto en Room sin publicarlo. | HU-03 esc. 1 |
| 4 | Botón **Publicar** | Llama a `POST /me/tuits` y vuelve al feed. | HU-02 esc. 1 |
| 5 | Botones **deshabilitados** | Si el campo está vacío o solo tiene espacios no se puede guardar ni publicar. | HU-02 esc. 2 · HU-03 esc. 4 |
| 6 | Confirmación **"Borrador guardado"** | Aviso inferior (snackbar), como en el wireframe. Al volver a entrar se recupera el texto. | HU-03 esc. 1 y 2 |
| 7 | Error al publicar | Aviso inferior (snackbar) con el mensaje de red. El texto escrito **no se pierde**. | HU-02 esc. 4 |
| 8 | Envío en curso | Spinner + "Publicando..." y **Volver** deshabilitado, para no publicar dos veces. | HU-02 esc. 3 |

Al publicar con éxito, el borrador se borra y la próxima vez la pantalla aparece vacía
(HU-03 esc. 3).

---

## 5 · Detalle de post (`detalle.svg`) · HU-09, HU-10, HU-11

| # | Elemento | Qué hace | Historia |
|---|---|---|---|
| 1 | **Volver** | Regresa al feed. | HU-09 |
| 2 | **Post original** | Muestra el post completo con su like. | HU-09 esc. 1 · HU-11 |
| 3 | Título **Respuestas (n)** | Indica cuántas respuestas tiene el post. | HU-09 esc. 1 |
| 4 | **Respuesta** | Cada respuesta se muestra como una tarjeta, con su propio like. | HU-09 · HU-11 |
| 5 | Campo **Responder...** | Escribe la respuesta. | HU-10 esc. 1 |
| 6 | Botón **Responder** | Envía la respuesta (`POST /me/tuits/{id}/replies`). Deshabilitado si está vacío. | HU-10 esc. 1 y 2 |
| 7 | Estado **sin respuestas** | "Todavía no hay respuestas. ¡Respondé vos!". | HU-09 esc. 2 |
| 8 | Estado **error** | "No se pudo cargar el post." | HU-09 esc. 3 |
| 9 | Botón **Reintentar** | Vuelve a cargar el post y sus respuestas. | HU-09 esc. 3 |
| 10 | Estado **cargando** | Spinner centrado mientras llegan el post y sus respuestas. | HU-09 esc. 1 |

Si falla el envío de una respuesta, se muestra el error y **el texto escrito no se pierde**
(HU-10 esc. 3).

---

## 6 · Perfil (`perfil.svg`) · HU-06

| # | Elemento | Qué hace | Historia |
|---|---|---|---|
| 1 | **Volver** | Regresa al feed. | HU-06 |
| 2 | **Avatar** | Imagen actual del usuario (se recarga si cambia la URL). | HU-06 esc. 1 |
| 3 | Campo **Nombre** | Editable. | HU-06 esc. 2 |
| 4 | Campo **Email** | Se ve pero **no se puede editar** (fondo gris, `enabled = false`). | HU-06 esc. 3 |
| 5 | Campo **URL del avatar** | Editable. Si queda vacío no se envía. | HU-06 esc. 2 |
| 6 | Campo **Contraseña nueva (opcional)** | Si se completa, se envía en el `PUT`; si queda vacía, no se cambia. | HU-06 esc. 4 |
| 7 | Botón **Guardar** | Llama a `PUT /me/profile` con el nombre y el avatar (la API exige los dos) y la contraseña solo si se escribió. | HU-06 esc. 2 |
| 8 | Botón **Salir** | Cierra sesión y borra borrador, favoritos y token. | Funcionalidad opcional |
| 9 | Confirmación **"Perfil actualizado."** | Aviso verde debajo del botón. | HU-06 esc. 2 |
| 10 | Error al guardar | Mensaje rojo. **Los cambios siguen en el formulario.** | HU-06 esc. 5 |
| 11 | Estado **cargando** | Spinner mientras se pide el perfil con `GET /me/profile`. | HU-06 esc. 1 |

---

## 7 · Usuarios favoritos (`favoritos.svg`) · HU-08

| # | Elemento | Qué hace | Historia |
|---|---|---|---|
| 1 | **Volver** | Regresa al feed. | HU-08 |
| 2 | **Avatar + nombre** | Datos del autor guardado (`author` y `avatar_url` del post). | HU-08 esc. 1 |
| 3 | Botón **Quitar** | Elimina al autor de la base local. | HU-08 esc. 3 |
| 4 | Estado **lista vacía** | Explica cómo agregar favoritos desde el feed. | HU-08 esc. 2 |
| 5 | **Quitar** (marco 3) | Al tocarlo, el autor desaparece de la lista. | HU-08 esc. 3 |
| 6 | Aviso de persistencia | La baja queda guardada: no vuelve al reabrir la app. | HU-08 esc. 3 |
| 7 | Estado **error de lectura** | Avisa si falló la lectura de Room; si ya había lista, se sigue mostrando. | HU-08 |

La lista se lee de **Room**, así que se ve completa aunque no haya internet (HU-08 esc. 4).

---

## Relación con los requerimientos

| Requerimiento | Wireframes |
|---|---|
| R1 · Feed | `feed.svg` |
| R2 · Publicar | `publicar.svg` |
| R3 · Borrador | `publicar.svg` (marco 2) |
| R4 · Registro y login | `registro.svg`, `login.svg` |
| R5 · Perfil | `perfil.svg` |
| R6 · Usuarios favoritos | `favoritos.svg`, `feed.svg` (elemento 5) |
| R7 · Respuestas | `detalle.svg` |
| R8 · Likes | `feed.svg` (elemento 4), `detalle.svg` (elementos 2 y 4) |
