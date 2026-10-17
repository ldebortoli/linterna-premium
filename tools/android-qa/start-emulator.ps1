param([ValidateRange(5554, 5680)][int]$Port = 5556)
$ErrorActionPreference = 'Stop'
if ($Port % 2) { throw 'Use an even emulator console port.' }
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
$adb = Join-Path $sdk 'platform-tools/adb.exe'
$emulator = Join-Path $sdk 'emulator/emulator.exe'
$serial = "emulator-$Port"
$env:PREMIUM_QA_SERIAL = $serial
$devices = (& $adb devices) -join "`n"
if ($devices -notmatch "(?m)^$serial\s") {
    $compilers = Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object { $_.CommandLine -match 'GradleDaemon|KotlinCompileDaemon|GradleWrapperMain' }
    if ($compilers) { throw 'Finish the builds and close owned compiler daemons before starting QA. This script never closes user processes.' }
    $freeKb = (Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory
    if ($freeKb -lt 5GB / 1KB) { throw 'QA needs at least 5 GiB of available host RAM before launch.' }
    $avdRoot = if ($env:ANDROID_AVD_HOME) { $env:ANDROID_AVD_HOME } else { Join-Path $env:USERPROFILE '.android/avd' }
    $config = Join-Path $avdRoot 'Premium_QA_API36.avd/config.ini'
    if (-not (Test-Path -LiteralPath $config)) { throw 'Create Premium_QA_API36 with the AOSP API36 default x86_64 image; see docs/VALIDACION_ANDROID.md.' }
    $output = Join-Path $projectRoot 'reports/android-qa/harness'
    New-Item -ItemType Directory -Force $output | Out-Null
    Copy-Item -LiteralPath $config -Destination (Join-Path $output 'avd-config.before.ini')
    $content = [IO.File]::ReadAllText($config)
    foreach ($pair in @(@('hw.ramSize','3072'), @('hw.cpu.ncore','2'), @('hw.gpu.enabled','yes'), @('hw.gpu.mode','swiftshader'), @('hw.lcd.width','720'), @('hw.lcd.height','1280'), @('hw.lcd.density','320'))) {
        $pattern = '(?m)^' + [regex]::Escape($pair[0]) + '=.*$'
        $content = [regex]::Replace($content, $pattern, $pair[0] + '=' + $pair[1])
    }
    [IO.File]::WriteAllText($config, $content, [Text.UTF8Encoding]::new($false))
    Start-Process -FilePath $emulator -ArgumentList @('-avd','Premium_QA_API36','-port',"$Port",'-no-window','-no-audio','-no-boot-anim','-no-snapshot','-gpu','swiftshader') -WindowStyle Hidden -RedirectStandardOutput (Join-Path $output 'emulator.log') -RedirectStandardError (Join-Path $output 'emulator-error.log') | Out-Null
}
python (Join-Path $PSScriptRoot 'prepare-emulator.py')
if ($LASTEXITCODE -ne 0) { throw 'QA emulator preparation failed; inspect its evidence before retrying.' }
