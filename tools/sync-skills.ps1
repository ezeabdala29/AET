<#
.SYNOPSIS
    Mirror AETHER skills and installed third-party skills into every agent skills directory.

.DESCRIPTION
    Sources
      1. ./skills/<name>/SKILL.md      canonical AETHER process skills (source of truth)
      2. ./.agents/skills/<name>/      third-party skills installed by `npx skills add`

    Targets
      .opencode/skills   (OpenCode)
      .codex/skills      (Codex)
      .agents/skills     (Antigravity) - written only when it is missing a skill

    A skill directory is mirrored recursively, so a skill that ships a `rules/` or
    `references/` folder arrives intact. Third-party skills are never modified, only copied.

.PARAMETER Check
    Report what would change without writing anything.

.EXAMPLE
    pwsh tools/sync-skills.ps1
    pwsh tools/sync-skills.ps1 -Check
#>
[CmdletBinding()]
param(
    [switch]$Check
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

$canonicalSource = Join-Path $root 'skills'
$thirdPartySource = Join-Path $root '.agents/skills'

$targets = @(
    (Join-Path $root '.opencode/skills'),
    (Join-Path $root '.codex/skills'),
    (Join-Path $root '.agents/skills')
)

function Get-SkillDirs {
    param([string]$Path)

    if (-not (Test-Path $Path)) { return @() }

    Get-ChildItem -Path $Path -Directory | Where-Object {
        (Test-Path (Join-Path $_.FullName 'SKILL.md')) -or
        (Test-Path (Join-Path $_.FullName 'AGENTS.md'))
    }
}

$skills = @()
$skills += Get-SkillDirs -Path $canonicalSource
$skills += Get-SkillDirs -Path $thirdPartySource

if (-not $skills) {
    throw "No skill directories found under $canonicalSource or $thirdPartySource"
}

$synced = 0
$skipped = 0
$wouldSync = 0

foreach ($target in $targets) {
    $targetFull = [System.IO.Path]::GetFullPath($target)

    foreach ($skill in $skills) {
        $destDir = Join-Path $target $skill.Name

        # Never copy a directory onto itself or into its own subtree.
        $destFull = [System.IO.Path]::GetFullPath($destDir)
        $srcFull = [System.IO.Path]::GetFullPath($skill.FullName)
        if ($destFull -eq $srcFull -or $destFull.StartsWith($srcFull + [System.IO.Path]::DirectorySeparatorChar)) {
            $skipped++
            continue
        }

        $inSync = (Test-Path $destDir) -and
            (-not (Get-ChildItem -Path $skill.FullName -Recurse -File |
                Where-Object {
                    $rel = $_.FullName.Substring($skill.FullName.Length).TrimStart('\', '/')
                    $candidate = Join-Path $destDir $rel
                    (-not (Test-Path $candidate)) -or
                        ((Get-FileHash $_.FullName).Hash -ne (Get-FileHash $candidate).Hash)
                }))

        if ($inSync) {
            $skipped++
            continue
        }

        if ($Check) {
            Write-Output "would sync $($skill.Name) -> $target"
            $wouldSync++
            continue
        }

        if (Test-Path $destDir) { Remove-Item $destDir -Recurse -Force }
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
        Copy-Item -Path (Join-Path $skill.FullName '*') -Destination $destDir -Recurse -Force
        $synced++
    }
}

if ($Check) {
    Write-Output "check complete: $($skills.Count) skill(s) known, $wouldSync change(s) pending, $skipped in sync or self-copy"
}
else {
    Write-Output "synced $synced skill(s), $skipped already in sync or self-copy, across $($targets.Count) target(s)"
}
