# Feature: Audiophile Library, Bit-Perfect Engine & Folders

Status: In Progress
Author: Gentle AI Orchestrator

## Specs

- S1: Solicitud de permisos en runtime según versión de Android (READ_EXTERNAL_STORAGE en Android 12/SDK 32, READ_MEDIA_AUDIO en 33+) con fallback explicativo si se deniega.
- S2: Escaneo y lectura de pistas locales desde MediaStore / File System, obteniendo metadatos completos: título, artista, álbum, duración, ruta de archivo, mime type, sample rate y bit depth donde esté disponible.
- S3: Navegación dual en interfaz: Pestaña "Canciones" (lista completa con búsqueda) y Pestaña "Carpetas" (explorador de directorios locales con audio).
- S4: Motor de reproducción audiófilo con soporte Bit-Perfect (ExoPlayer/AudioTrack configurado con flags de audio sin resampling ni efectos de procesamiento del sistema, soporte para audio en alta resolución 24-bit/FLAC/WAV/ALAC).
- S5: Pantalla de "Ajustes Audiófilo": Bypass de mixer/DSP del sistema (bit-perfect mode), buffer size configurable, información técnica en tiempo real (Sample Rate: e.g. 44.1kHz / 96kHz / 192kHz, Bit Depth: 16-bit / 24-bit, Formato: FLAC/MP3).
- S6: Icono de aplicación estilizado (vectorial adaptive icon) con estética Hi-Fi/Aether dark.

## Tasks

| ID | Spec | Description | Route | Commit |
| --- | --- | --- | --- | --- |
| T1 | S6 | Crear iconos adaptativos Hi-Fi (vectorial foreground/background) y registrarlos en manifest | inline | pending |
| T2 | S1, S2 | Implementar modelo de datos `Track`, `AudioFolder`, scanner de `MediaStore` con soporte de extracción de metadatos de audio | inline | pending |
| T3 | S4, S5 | Implementar `AudioPlayerEngine` con Media3/ExoPlayer optimizado para bit-perfect (AudioAttributes, AudioTrack float/pcm24 bit, bypass de DSP) | inline | pending |
| T4 | S3, S5 | Implementar UI en Jetpack Compose con Tabs (Canciones, Carpetas, Reproductor Ahora Suena con detalles técnicos Hi-Fi, y Menú de Ajustes Audiófilo) | inline | pending |
| T5 | S1-S6 | Compilar APK, verificar en dispositivo Samsung S10e y validar funcionamiento | inline | pending |

## Log

- L1: "no me sale nada me dice aether player scaffold- no playback yet y un fondo blanco con letras negras no me sale ni la lista de todas las canciones ni las carpetas fijate eso arreglalo ya que no me carga no me sirve la aplicacion asi"
- L2: "si y ponele un icono bonito a la app quiero que implementes todos los ajustes que debe tener una aplicacion para un purista del audio como soy yo que se pueda reproducir canciones en bit perfect"
