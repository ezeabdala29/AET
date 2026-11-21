# AETHER

Workspace de orquestación multi-agente para construir `aether-player`: un reproductor de
música Android (audio de alta fidelidad, carátulas y letras resueltas por red, APK para
Android 7.0+ / API 24).

Este repositorio no es la app. Es el **harness**: los roles, las skills, los comandos y el
contrato de trabajo que los agentes (OpenCode, Codex, Antigravity) tienen que respetar antes
de tocar una sola línea del proyecto.

---

## 1. Estado

| Pieza | Estado |
| --- | --- |
| Estructura de directorios | ✅ creada |
| 7 roles de agente ejecutables | ✅ `.opencode/agent/` |
| 10 skills de proceso | ✅ `.opencode/skills/` |
| Comandos del ciclo | ✅ `.opencode/command/` |
| Contrato para Codex/Antigravity | ✅ `AGENTS.md` |
| Skills de terceros (Vercel) | ✅ `skills-lock.json` |
| Proyecto Android | ⏳ scaffold, sin código de reproducción |

---

## 2. La organización

Siete roles, un solo objetivo por rol, y una regla dura: **un rol no hace el trabajo de otro**.

| Rol | Agente | Modelo | Puede escribir |
| --- | --- | --- | --- |
| Orquestador / PM | `orchestrator` | razonamiento fuerte | ❌ |
| Explorer | `explorer` | barato y rápido | ❌ |
| Architect | `architect` | razonamiento fuerte | ✅ solo `docs/` |
| Developer | `developer` | coder | ✅ código y tests |
| QA Engineer | `qa-engineer` | coder | ✅ solo tests |
| Security Auditor | `security-auditor` | razonamiento fuerte | ❌ |
| Integrator | `integrator` | barato y rápido | ❌ |

Ver `.opencode/agent/*.md` para el contrato exacto de cada uno.

---

## 3. El ciclo obligatorio

Ningún agente empieza a escribir código por impulso. El ciclo son ocho pasos y cada uno
tiene un artefacto de salida verificable:

| # | Paso | Artefacto | Comando |
| --- | --- | --- | --- |
| 1 | Exploración | mapa de módulos, stack y dependencias | `/aether-explore` |
| 2 | Análisis | riesgos y deuda técnica detectada | `/aether-explore` |
| 3 | Planificación | archivos, pruebas y riesgos por tarea | `/aether-plan` |
| 4 | Implementación | código + tests + docs juntos | `/aether-build` |
| 5 | Integración | veredicto de compatibilidad | `/aether-review` |
| 6 | Testing | unit / integración / regresión | `/aether-test` |
| 7 | Auditoría de seguridad | permisos, dependencias, exposición | `/aether-review` |
| 8 | Informe final | qué se hizo, qué falló, qué queda | `/aether-report` |

Un paso no se puede saltar. Si el paso 3 no produce lista de archivos, el paso 4 no empieza.

---

## 4. Instalación

```bash
# 1. Skills de terceros (Vercel, React, deploy, web design)
npx skills add vercel-labs/agent-skills

# 2. Sincronizar las skills de AETHER hacia cada agente instalado
powershell -NoProfile -ExecutionPolicy Bypass -File tools/sync-skills.ps1
```

`sync-skills.ps1` copia `skills/*/SKILL.md` (fuente canónica, agnóstica de proveedor) a
`.opencode/skills/`, `.agents/skills/` (Antigravity) y `.codex/skills/` (Codex).

---

## 5. Optimizar planes free para vibecoding

La restricción real no es "cuántos mensajes me quedan", es **cuánto contexto quemás por
tarea**. Reglas que este harness impone y que valen más que cualquier truco:

1. **Exploración barata, implementación cara.** `explorer` corre con modelo rápido y devuelve
   un mapa de ≤2k tokens. El coder nunca recorre el repo a ciegas.
2. **Delegar lectura, no escritura masiva.** Cada subagente arranca con contexto limpio. Pagar
   una lectura nueva es más barato que arrastrar 80k tokens de historial.
3. **Un objetivo por sesión.** Contexto que crece = tokens que se queman en cada turno.
   Cambiar de objetivo = sesión nueva.
4. **Plan antes de código.** Un plan de 30 líneas ahorra tres iteraciones fallidas de 3k
   líneas cada una. Es aritmética, no disciplina.
5. **Repartir entre herramientas.** Codex, Antigravity y OpenCode tienen ventanas de cuota
   independientes. El mismo `AGENTS.md` los alinea: el trabajo no se re-explica.
6. **Cortar cuando falla.** Dos intentos fallidos del mismo error = cambiar de estrategia,
   no un tercer intento. Es la fuga de cuota más común.
7. **No pedir la app entera de una.** `aether-player` se construye por capacidades: playback,
   metadata, lyrics, library, UI. Una por ciclo.

---

## 6. Reglas del repo

- Nada de `Co-Authored-By` ni atribución de IA en commits.
- Conventional Commits.
- Un commit = una unidad revisable (código + tests + docs de esa unidad).
- `memory/`, `logs/` y `reports/` se versionan vacíos; su contenido es efímero.
- Los secretos nunca entran: `.gitignore` cubre `*.keystore`, `.env`, `local.properties`.
