$ErrorActionPreference = 'Stop'
$logDir = 'C:\Users\PC\AETHER\logs'
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }
$log = "$logDir\setup-android-sdk.log"
function Say($m) { $m | Out-File $log -Encoding utf8 -Append; Write-Host $m }

"=== Android SDK Setup $(Get-Date -Format o)" | Out-File $log -Encoding utf8

# --- JAVA_HOME ---
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Say "JAVA_HOME=$env:JAVA_HOME"

$sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
$dl  = Join-Path $env:TEMP 'cmdline-tools.zip'
$url = 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip'

# --- Remove incomplete download ---
if (Test-Path $dl) {
    $size = (Get-Item $dl).Length
    if ($size -lt 100000000) {
        Say "Removing incomplete download ($size bytes)"
        Remove-Item $dl -Force
    }
}

# --- Download cmdline-tools ---
if (-not (Test-Path $dl)) {
    Say "Downloading cmdline-tools from $url"
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $url -OutFile $dl -UseBasicParsing -TimeoutSec 900
    $dlSize = (Get-Item $dl).Length
    Say "Downloaded $dlSize bytes ($([math]::Round($dlSize/1MB,1)) MB)"
}

# --- Extract cmdline-tools ---
Say "Extracting cmdline-tools..."
New-Item -ItemType Directory -Path "$sdk\cmdline-tools" -Force | Out-Null
$tmp = "$sdk\cmdline-tools\_tmp"
if (Test-Path $tmp) { Remove-Item $tmp -Recurse -Force }
Expand-Archive -Path $dl -DestinationPath $tmp -Force
if (Test-Path "$sdk\cmdline-tools\latest") { Remove-Item "$sdk\cmdline-tools\latest" -Recurse -Force }
Move-Item "$tmp\cmdline-tools" "$sdk\cmdline-tools\latest"
Remove-Item $tmp -Recurse -Force
Say "Extracted to $sdk\cmdline-tools\latest"

# --- Set env vars ---
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk
$env:PATH = "$sdk\cmdline-tools\latest\bin;$sdk\platform-tools;$env:PATH"

# --- Accept licenses ---
Say "Accepting licenses..."
$answers = (1..40 | ForEach-Object { 'y' })
$answers | & "$sdk\cmdline-tools\latest\bin\sdkmanager.bat" --licenses 2>&1 | Out-Null
Say "Licenses accepted"

# --- Install SDK packages ---
Say "Installing platform-tools, platforms;android-35, build-tools;35.0.0..."
& "$sdk\cmdline-tools\latest\bin\sdkmanager.bat" --install "platform-tools" "platforms;android-35" "build-tools;35.0.0" 2>&1 | ForEach-Object { $_.ToString() } | Out-File $log -Encoding utf8 -Append
Say "sdkmanager exit=$LASTEXITCODE"

# --- Verify ---
$checks = @{
    'cmdline-tools' = Test-Path "$sdk\cmdline-tools\latest\bin\sdkmanager.bat"
    'platform-tools' = Test-Path "$sdk\platform-tools\adb.exe"
    'android-35' = Test-Path "$sdk\platforms\android-35"
    'build-tools-35' = Test-Path "$sdk\build-tools\35.0.0"
}
foreach ($k in $checks.Keys) { Say "$k = $($checks[$k])" }

Say "=== done $(Get-Date -Format o)"
