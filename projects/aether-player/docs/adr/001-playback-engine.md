# ADR 001 — Playback engine: Media3 over a custom pipeline

Date: 2026-11-21
Status: accepted

## Context
The player must handle FLAC, ALAC, WAV, MP3 and AAC, expose a media session that survives
backgrounding, and run on API 24. Hi-fi output implies sample rates well above 44.1 kHz and
bit depths up to 24-bit.

## Options

**A) Media3 / ExoPlayer**
Gains: codec coverage out of the box (FLAC, ALAC, WAV via extractors), a maintained media
session and notification, `AudioSink` can be configured for high-resolution PCM, active
upstream maintenance.
Costs: less control over the output path; the audio sink negotiates format for you; a large
dependency in the APK.

**B) Custom `AudioTrack` pipeline**
Gains: full control of format negotiation and the output path, smaller dependency surface.
Costs: we must write decoders or bind to platform decoders per format, plus a media session
integration, plus device-specific quirks. Weeks of work before the first audible track.

## Decision
Option A. Configure `DefaultAudioSink` for high-resolution PCM and only fall back to a custom
path if a measured requirement cannot be met.

## Consequence
We accept Media3's opinion on output format. If a specific device will not play 24-bit/192 kHz
through Media3, that becomes a targeted follow-up, not a rewrite.

## Reversal cost
Medium. Playback is behind our own `PlaybackController` interface, so the engine can be
replaced without touching the UI or the library.
