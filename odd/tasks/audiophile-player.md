# AETHER Player — local Hi-Fi library, playback UI, and Mix
Branch: `feat-audiophile-player` · Delivery: `feature-branch-chain` · Runner: Gradle 8.9 wrapper (`:app:testDebugUnitTest`, `:app:assembleDebug`)
Objective: finish the local audiophile player while prioritizing an unprocessed playback path over crossfade, then verify, commit, build, and update the Samsung S10e.

## Specs

- S1: Request media access by Android version: `READ_EXTERNAL_STORAGE` through API 32 and `READ_MEDIA_AUDIO` from API 33, with an explanatory denial fallback. Original scope: “Solicitud de permisos en runtime según versión de Android (READ_EXTERNAL_STORAGE en Android 12/SDK 32, READ_MEDIA_AUDIO en 33+) con fallback explicativo si se deniega.”
- S2: Scan local MediaStore audio and show only measured technical metadata; do not estimate sample rate, bit depth, or bitrate and present estimates as source facts. Original scope: “obteniendo metadatos completos: título, artista, álbum, duración, ruta de archivo, mime type, sample rate y bit depth donde esté disponible.”
- S3: Preserve the existing “Canciones” and “Carpetas” local-library views and search.
- S4: Preserve the requested hi-fi playback goal: “Motor de reproducción audiófilo con soporte Bit-Perfect (ExoPlayer/AudioTrack configurado con flags de audio sin resampling ni efectos de procesamiento del sistema, soporte para audio en alta resolución 24-bit/FLAC/WAV/ALAC).” Report platform/device limitations honestly; never claim strict bit-perfect unless the output route confirms it.
- S5: Keep audio settings truthful and observable. Original: “Pantalla de ‘Ajustes Audiófilo’: Bypass de mixer/DSP del sistema (bit-perfect mode), buffer size configurable, información técnica en tiempo real (Sample Rate: e.g. 44.1kHz / 96kHz / 192kHz, Bit Depth: 16-bit / 24-bit, Formato: FLAC/MP3).”
- S6: Preserve the adaptive Hi-Fi app icon.
- S7: Redesign the UI using the requested modern Discover/player/navigation format. Original exact design brief: “Diseña la interfaz de usuario (UI) para una aplicación móvil de reproducción de música en Android, inspirada en el diseño moderno de la última versión de Huawei Music (estilo HarmonyOS). Utiliza una estética minimalista y limpia con tarjetas de esquinas muy redondeadas (border-radius de 20px), amplio espacio en blanco y tipografía sans-serif moderna con jerarquía marcada. Incluye: 1. Pantalla principal (Descubrir): Barra de búsqueda superior redondeada, un carrusel destacado de álbumes/playlists, sección de 'Escuchado recientemente' con portadas cuadradas redondeadas y accesos rápidos a géneros o mezclas diarias. 2. Reproductor a pantalla completa: Fondo con desenfoque gaussiano dinámico (glassmorphism) extraído de los colores de la portada del álbum actual, carátula grande centrada con sombra suave, barra de progreso minimalista con tiempos, controles de reproducción limpios (anterior, pausa/play destacado, siguiente), botones de favorito, letra sincronizada y modo aleatorio. 3. Navegación inferior: Barra flotante o fija semitransparente con 3 o 4 pestañas con iconos lineales (Descubrir, Mi Música, Explorar/DJ, Perfil) y un mini-reproductor persistente justo encima de la barra de navegación. Usa una paleta de colores neutros claros (blanco humo y gris muy suave para tarjetas) con un color de acento vibrante (rojo coral o azul eléctrico) para los botones activos, e incluye también la variante en modo oscuro (Dark Mode) con tonos gris carbón profundo.” The UI is Spanish because the existing app UI is Spanish.
- S8: The Mix is local-only. User decision: “si confirmo que sea en local”. User priority: “prioriza el bit perfect”. Therefore preserve continuous/gapless queue playback and do not crossfade while claiming Bit-Perfect. Show BPM/key only when verified in available local metadata; otherwise show unavailable, never fabricate. Assumption: read existing local metadata tags only; do not infer BPM/key from audio samples. No collaborative/cloud playlist work in this delivery.
- S9: Include the requested non-destructive options and a user-confirmed delete flow. Original menu scope includes: “Ajustes”, “Añadir a Marcadores”, “Buscar relacionados”, “Mostrar pantalla repr. de casete”, “Info. detallada de la canción”, “Visualizar letras”, “Eliminar”, “Guía de uso”. Deletion must require the platform/user confirmation and remain API 24 compatible.
- S10: Commit the verified implementation and build/install the APK for the user’s Samsung S10e only after review gates and explicit ADB session authorization.

## Tasks

| ID | Spec | Description | Files | Route | Status | Risk | Commit / proof |
| --- | --- | --- | --- | --- | --- | --- | --- |
| T1 | S6 | Adaptive Hi-Fi app icon | `app/src/main/res/drawable/*`, manifest | inline | complete | none | `54d4bee` |
| T2 | S1-S2 | Permissions and MediaStore scanner | `MainActivity.kt`, `AndroidManifest.xml`, `AudioScanner.kt`, `Track.kt` | inline | partial: source values are estimated | item 2 | `54d4bee`, `e5efa44` |
| T3 | S4-S5 | Media3 engine and audio settings | `AudioPlayerEngine.kt`, `Track.kt`, `MainScreen.kt` | inline | partial: route-level Bit-Perfect is not confirmed by Media3/Android | item 4 | `54d4bee`, `d211d69`, `43048d7` (native RDD approved and acknowledged) |
| T4 | S3,S5 | Songs/folders UI and settings | `MainScreen.kt`, `MainViewModel.kt` | inline | complete | none | `e5efa44`, `d211d69` |
| T5 | S1-S6 | Baseline build/device proof | Gradle wrapper, app | inline | partial: Gradle build passed; tests NO-SOURCE; device proof pending | item 5 | build observed 2026-11-21 |
| T6 | S7 | Discover, full player, navigation, favorites and options UI | `MainScreen.kt`, `MainViewModel.kt`, `CONTEXT.md` | inline | partial: delete and synchronized-lyrics controls remain missing | item 4 | `:app:testDebugUnitTest` and `:app:assembleDebug` pass 2026-11-21; next UI slice pending |
| T7 | S2,S4,S8 | Accurate track format metadata and local gapless Mix, preserving the no-crossfade decision | `AudioScanner.kt`, `Track.kt`, `AudioPlayerEngine.kt`, UI, unit tests | inline | partial: source tags are not queried; queue tests/device output proof remain pending | item 4 | `43048d7`; tests/build pass 2026-11-21; RDD approved and acknowledged |
| T8 | S9 | Safe current-track delete action with system/user confirmation and API 24 fallback | `MainActivity.kt`, `AndroidManifest.xml`, `MainViewModel.kt`, `MainScreen.kt` | inline | pending | items 1, 2 | pending |
| T9 | S4-S9 | Tests, ADR/context docs, security and integration verification; targeted independent recheck | `app/src/test`, `docs/adr/`, `CONTEXT.md` | inline | partial: metadata tests and ADR done; full spec/security/device proof still pending | items 1, 2, 4 | `43048d7`; JVM tests and debug build pass 2026-11-21 |
| T10 | S10 | Native RDD review, Conventional Commit, final build, and authorized S10e install | repo commit, APK, ADB | inline | pending | item 5 | pending |

## Log

- L1: “no me sale nada me dice aether player scaffold- no playback yet y un fondo blanco con letras negras no me sale ni la lista de todas las canciones ni las carpetas fijate eso arreglalo ya que no me carga no me sirve la aplicacion asi”
- L2: “si y ponele un icono bonito a la app quiero que implementes todos los ajustes que debe tener una aplicacion para un purista del audio como soy yo que se pueda reproducir canciones en bit perfect”
- L3 (2026-11-21): “analiza lo que ya esta hecho y utiliza este formato para la interfaz 
Deslice la pantalla (hacia arriba/hacia abajo/hacia la izquierda/hacia la derecha).
    Puntee en el minirreproductor para mostrar la pantalla de reproducción.

Ilustración que muestra los elementos de la pantalla de reproducción. La barra de estado se encuentra en la parte superior de la pantalla. La barra de navegación se encuentra en la parte inferior de la pantalla. En el área entre las barras, aparecen los elementos siguientes. Desde la parte superior, la información de la pista actual y los botones de operaciones de reproducción.

    Calidad de sonido (códec, frecuencia de muestreo, velocidad de bits/velocidad de bits de cuantización)/icono de audio de alta resolución

    Portada

    Tiempo transcurrido de la pista actual

    Barra de escala de tiempo

    Título de la pista/Nombre del artista/Título del álbum

    Botón de orden aleatorio / botón Anterior / botón Reproducción (Pausa) / botón Siguiente / botón Repetir

    Botón de biblioteca

    Botón de opciones

    Número de pista/Número total de pistas

    Longitud de la pista

    Botón de favoritos

    Puntee en este botón para añadir la pista actual a los favoritos o para quitar la pista de los favoritos.

Menú de opciones

Puntee en (Opciones) para mostrar el menú de opciones.

Los elementos visualizados pueden variar dependiendo de los ajustes.

Ajustes
    Seleccione el elemento para mostrar el menú de ajustes de “Reproductor de música”.
Añadir a Marcadores
    Seleccione el elemento para añadir la pista actual a los marcadores.
Buscar relacionados
    Seleccione el elemento para buscar contenido relacionado con la pista actual, tal como pistas del mismo álbum y álbumes del mismo artista.
Mostrar pantalla repr. de casete
    Cuando active el elemento, la pantalla de reproducción cambiará a una animación de una cinta de casete que gira. El tipo de cinta de casete visualizado varía de acuerdo con la pista que está siendo reproducida. 
Info. detallada de la canción
    Seleccione el elemento para mostrar los detalles de la pista actual.
Visualizar letras
    Seleccione el elemento para visualizar la letra de la pista actual.
Eliminar
    Seleccione el elemento para eliminar la pista actual.
Guía de uso
    Seleccione el elemento para ver la guía de funcionamiento.

Sugerencia

    Arrastre la barra de escala de tiempo para cambiar la posición de reproducción dentro de una pista.

    Mientras arrastre la barra de línea de tiempo, el tiempo transcurrido del tema actual aparecerá sobre la portada.
    Si transcurre un cierto periodo de tiempo sin operaciones de botones o de pantalla en la pantalla de reproducción, la visualización cambiará a la pantalla de la cinta de casete. Esto ocurre cuando se selecciona la pantalla de reproducción mientras hay una marca de verificación en la casilla para [Mostrar pantalla repr. de casete]. Para realizar operaciones de pantalla, puntee en la pantalla de la cinta de casete. La visualización cambiará temporalmente de nuevo a la pantalla de reproducción normal. A continuación, puede realizar operaciones de pantalla.
    Aunque haya una marca de verificación en la casilla para [Mostrar pantalla repr. de casete], la pantalla de reproducción no cambiará a la pantalla de casete mientras se muestre el elemento [Info. detallada de la canción] o [Visualizar letras].

    También puede activar o desactivar la función [Mostrar pantalla repr. de casete] desde el menú de cualquier pantalla de “Reproductor de música”. Toque los elementos del menú en el orden siguiente.

    (Opciones) － [Ajustes] － [Reproducir en pantalla casete].
    También puede ejecutar [Buscar relacionados] punteando el título de la pista, el nombre del artista o el título del álbum en la pantalla de reproducción en “Reproductor de música”. quiero que copies la funcion de mix en playlist de spotify
• Qué hace: Permite enlazar canciones dentro de tus listas de reproducción o listas colaborativas con transiciones continuas, mostrando datos útiles para mezclar como tempo (BPM) y armonía/tonalidad.
• Cómo usarlo: Abre una playlist propia o colaborativa y activa la opción de mezcla/transición para ajustar el estilo entre temas o dejar que se aplique automáticamente. 
y "Diseña la interfaz de usuario (UI) para una aplicación móvil de reproducción de música en Android, inspirada en el diseño moderno de la última versión de Huawei Music (estilo HarmonyOS). Utiliza una estética minimalista y limpia con tarjetas de esquinas muy redondeadas (border-radius de 20px), amplio espacio en blanco y tipografía sans-serif moderna con jerarquía marcada. Incluye:
1. Pantalla principal (Descubrir): Barra de búsqueda superior redondeada, un carrusel destacado de álbumes/playlists, sección de 'Escuchado recientemente' con portadas cuadradas redondeadas y accesos rápidos a géneros o mezclas diarias.
2. Reproductor a pantalla completa: Fondo con desenfoque gaussiano dinámico (glassmorphism) extraído de los colores de la portada del álbum actual, carátula grande centrada con sombra suave, barra de progreso minimalista con tiempos, controles de reproducción limpios (anterior, pausa/play destacado, siguiente), botones de favorito, letra sincronizada y modo aleatorio.
3. Navegación inferior: Barra flotante o fija semitransparente con 3 o 4 pestañas con iconos lineales (Descubrir, Mi Música, Explorar/DJ, Perfil) y un mini-reproductor persistente justo encima de la barra de navegación.
Usa una paleta de colores neutros claros (blanco humo y gris muy suave para tarjetas) con un color de acento vibrante (rojo coral o azul eléctrico) para los botones activos, e incluye también la variante en modo oscuro (Dark Mode) con tonos gris carbón profundo.”'
- L4 (2026-11-21): “si haz lo que te pedi”
- L5 (2026-11-21): “hace los commits y compila la aplicacion con comandos adb para se me actualice en mi s10e”
- L6 (2026-11-21): “si confirmo que sea en local”
- L7 (2026-11-21): “prioriza el bit perfect”
- L8: The current user approved an independent verifier with the available model `gpt-5.6-terra` after `gpt-6.1-sol` was unavailable; the verifier found local Mix, deletion, and technical-metadata gaps. Preserve its exact findings in the session summary; no source change was made in response yet.
- L9: Initial native review assess classified the queue change as high-risk item 4 and selectorless STATUS offered START, but no START or candidate review lifecycle was initiated.
- L10 (2026-11-21): Recorded the user's Bit-Perfect priority in `projects/aether-player/docs/adr/002-bit-perfect-priority.md`: local Mix remains gapless, without crossfade; never claim strict bit-perfect unless the output route confirms it.
- L11 (2026-11-21): User chose `feature-branch-chain` for commit/delivery organization.
- L12 (2026-11-21): Engram task mirror pending; project detection is ambiguous and the user selection `aether` is not an absolute workspace path, so no project-scoped write was attempted.
- L13 (2026-11-21): Created `43048d7` (`feat(audio): prioritize unprocessed playback and measured metadata`); `:app:testDebugUnitTest` and `:app:assembleDebug` passed. Its native review was approved and acknowledged. Reviewer advisory R2-001 notes that measured rate/bit-depth metadata without a filename extension can produce a leading separator in `Track.kt`; this is non-blocking and remains follow-up work.
