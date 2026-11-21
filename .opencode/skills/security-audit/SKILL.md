---
name: security-audit
description: Identify vulnerabilities and insecure configuration in an Android app. Trigger before release, after adding a permission or network call, when handling user-supplied URLs, or on "is this safe".
---

# Security Audit

Goal: find what would actually hurt a user running this APK, ranked by exploitability.

## Android-specific checklist

**Manifest**
- Every `<uses-permission>` justified. `READ_MEDIA_AUDIO` (33+) vs `READ_EXTERNAL_STORAGE`
  (≤32) — both declared with the right `maxSdkVersion` split.
- No `android:exported="true"` without a signature permission or a documented reason.
- `android:allowBackup` — does the user want their library metadata in cloud backup?

**Network**
- `cleartextTrafficPermitted` — must be false unless a documented LAN case exists.
- Cover art and lyrics URLs are **user/remote-controlled input**. Validate scheme is https,
  cap response size, set timeouts, never render remote HTML.
- No trusting all certificates, no disabled hostname verification.

**Storage**
- What is persisted in Room/prefs, and is anything sensitive.
- Scoped storage; no paths outside the app sandbox.

**Secrets and supply chain**
- No API key in source, `build.gradle.kts` or committed `local.properties`.
- Dependency versions with known CVEs.
- Gradle verification / pinned versions where practical.

## Output

```
CRITICAL: <finding> — <path:line> — <exploit path>
HIGH / MEDIUM: ...
CLEARED: <area — nothing found>
```

## Rules

- Evidence or it is not a finding.
- Rank by real reachability on a local player, not by checklist length.
- Report only. Fixes go through `developer`.
