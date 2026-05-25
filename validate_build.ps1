# ═══════════════════════════════════════════════════════════════
# HouziCore Pre-Deploy Validator
# ═══════════════════════════════════════════════════════════════
# Validates JAR integrity BEFORE deploying to server.
# Catches: corrupted JARs, missing shaded libs, stale bytecode.
#
# Usage:
#   .\validate_build.ps1           # Validate all modules
#   .\validate_build.ps1 -Module Shared  # Validate specific module
# ═══════════════════════════════════════════════════════════════

param(
    [string]$Module = "all",
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"
$script:errors = 0
$script:warnings = 0

function Write-Status {
    param([string]$Icon, [string]$Color, [string]$Message)
    Write-Host "$Icon " -ForegroundColor $Color -NoNewline
    Write-Host $Message
}

function Write-Pass    { param([string]$Msg) Write-Status "✓" "Green"  $Msg }
function Write-Fail    { param([string]$Msg) Write-Status "✗" "Red"    $Msg; $script:errors++ }
function Write-Warn    { param([string]$Msg) Write-Status "⚠" "Yellow" $Msg; $script:warnings++ }

function Test-JarIntegrity {
    param(
        [string]$JarPath,
        [string]$ModuleName,
        [string[]]$RequiredLibs = @()
    )

    Write-Host ""
    Write-Host "━━━ $ModuleName ━━━" -ForegroundColor Cyan

    # 1. Check file exists
    if (-not (Test-Path $JarPath)) {
        Write-Fail "$ModuleName JAR not found at: $JarPath"
        return
    }

    # 2. Check file size (>100KB = valid)
    $size = (Get-Item $JarPath).Length
    $sizeMB = [math]::Round($size / 1MB, 2)
    if ($size -lt 100KB) {
        Write-Fail "$ModuleName JAR is only $size bytes — likely CORRUPTED! (wildcard copy bug)"
        return
    }
    Write-Pass "$ModuleName JAR size: ${sizeMB}MB"

    # 3. Check MANIFEST.MF exists
    try {
        $manifest = & jar tf $JarPath 2>$null | Select-String "MANIFEST.MF"
        if ($manifest) {
            Write-Pass "MANIFEST.MF present"
        } else {
            Write-Fail "MANIFEST.MF missing — JAR may be incomplete"
        }
    } catch {
        Write-Warn "Could not inspect JAR (jar command not in PATH?)"
    }

    # 4. Check critical shaded libraries
    foreach ($lib in $RequiredLibs) {
        try {
            $found = & jar tf $JarPath 2>$null | Select-String $lib
            if ($found) {
                Write-Pass "Shaded lib '$lib' found"
            } else {
                Write-Warn "'$lib' NOT found in JAR — may cause NoClassDefFoundError at runtime"
            }
        } catch {
            Write-Warn "Could not check for '$lib'"
        }
    }

    # 5. Check for stale target directories
    $targetDir = Split-Path $JarPath -Parent
    $originalJar = Get-ChildItem -Path $targetDir -Filter "original-*.jar" -ErrorAction SilentlyContinue
    if ($originalJar) {
        if ($Verbose) {
            Write-Warn "Stale 'original-*.jar' found in target/ — run 'mvn clean' if issues occur"
        }
    }
}

function Test-BuildTimestamp {
    param([string]$JarPath, [string]$ModuleName)

    if (-not (Test-Path $JarPath)) { return }

    $lastWrite = (Get-Item $JarPath).LastWriteTime
    $age = (Get-Date) - $lastWrite

    if ($age.TotalHours -gt 24) {
        Write-Warn "$ModuleName JAR is $([math]::Round($age.TotalHours, 1)) hours old — consider rebuilding"
    } elseif ($age.TotalMinutes -gt 60) {
        if ($Verbose) {
            Write-Pass "$ModuleName built $([math]::Round($age.TotalMinutes, 0)) minutes ago"
        }
    } else {
        Write-Pass "$ModuleName built $([math]::Round($age.TotalMinutes, 0)) minutes ago (fresh)"
    }
}

# ═══════════════════════════════════════════════════════════════
# MAIN
# ═══════════════════════════════════════════════════════════════

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor DarkCyan
Write-Host "║  HouziCore Pre-Deploy Validator        ║" -ForegroundColor DarkCyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor DarkCyan

$basePath = "E:\Houzicore\Code"

$modules = @{
    "Shared" = @{
        Jar = "$basePath\Shared\target\houzicore-shared-1.21.11.jar"
        Libs = @("adventure", "packetevents")
    }
    "Arcade" = @{
        Jar = "$basePath\Arcade\target\houzicore-arcade-1.21.11.jar"
        Libs = @("fastboard", "adventure")
    }
    "Lobby" = @{
        Jar = "$basePath\Lobby\target\houzicore-lobby-1.21.11.jar"
        Libs = @("fastboard", "adventure")
    }
}

if ($Module -eq "all") {
    foreach ($mod in $modules.Keys) {
        $config = $modules[$mod]
        Test-JarIntegrity -JarPath $config.Jar -ModuleName $mod -RequiredLibs $config.Libs
        Test-BuildTimestamp -JarPath $config.Jar -ModuleName $mod
    }
} else {
    if ($modules.ContainsKey($Module)) {
        $config = $modules[$Module]
        Test-JarIntegrity -JarPath $config.Jar -ModuleName $Module -RequiredLibs $config.Libs
        Test-BuildTimestamp -JarPath $config.Jar -ModuleName $Module
    } else {
        Write-Fail "Unknown module: $Module. Valid: $($modules.Keys -join ', ')"
    }
}

# Summary
Write-Host ""
Write-Host "━━━ Summary ━━━" -ForegroundColor Cyan
if ($script:errors -gt 0) {
    Write-Host "  $($script:errors) ERROR(s), $($script:warnings) WARNING(s)" -ForegroundColor Red
    Write-Host "  ❌ DO NOT DEPLOY — fix errors first!" -ForegroundColor Red
    Write-Host ""
    exit 1
} elseif ($script:warnings -gt 0) {
    Write-Host "  0 errors, $($script:warnings) WARNING(s)" -ForegroundColor Yellow
    Write-Host "  ⚠ Deploy with caution." -ForegroundColor Yellow
    Write-Host ""
    exit 0
} else {
    Write-Host "  All checks passed!" -ForegroundColor Green
    Write-Host "  ✅ Safe to deploy." -ForegroundColor Green
    Write-Host ""
    exit 0
}
