$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$output = Join-Path $projectRoot 'reports/android-qa/harness'
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
$androidJar = Join-Path $sdk 'platforms/android-36/android.jar'
$buildTools = Join-Path $sdk 'build-tools/36.0.0'
function Invoke-Checked($program, $arguments) {
    & $program @arguments
    if ($LASTEXITCODE -ne 0) { throw "QA helper build failed: $program" }
}
New-Item -ItemType Directory -Force (Join-Path $output 'classes'), (Join-Path $output 'dex') | Out-Null
Invoke-Checked "$env:JAVA_HOME/bin/javac.exe" @('-encoding', 'UTF-8', '-source', '17', '-target', '17', '-cp', $androidJar, '-d', (Join-Path $output 'classes'), (Join-Path $PSScriptRoot 'Probe.java'))
Invoke-Checked "$env:JAVA_HOME/bin/jar.exe" @('cf', (Join-Path $output 'classes.jar'), '-C', (Join-Path $output 'classes'), '.')
Invoke-Checked (Join-Path $buildTools 'd8.bat') @('--min-api', '24', '--lib', $androidJar, '--output', (Join-Path $output 'dex'), (Join-Path $output 'classes.jar'))
Invoke-Checked (Join-Path $buildTools 'aapt2.exe') @('link', '-I', $androidJar, '--manifest', (Join-Path $PSScriptRoot 'AndroidManifest.xml'), '-o', (Join-Path $output 'probe.apk'))
Invoke-Checked "$env:JAVA_HOME/bin/jar.exe" @('uf', (Join-Path $output 'probe.apk'), '-C', (Join-Path $output 'dex'), 'classes.dex')
Invoke-Checked (Join-Path $buildTools 'apksigner.bat') @('sign', '--ks', (Join-Path $env:USERPROFILE '.android/debug.keystore'), '--ks-pass', 'pass:android', (Join-Path $output 'probe.apk'))
Write-Output 'Built the local QA helper; no product APK or external service was changed.'
