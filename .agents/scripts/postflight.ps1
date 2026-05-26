param(
    [string]$TaskFile = "tasks/TEMPLATE.md"
)

$ErrorActionPreference = "Stop"

function Get-SectionLines {
    param(
        [string[]]$Lines,
        [string]$Header
    )

    $start = -1
    for ($i = 0; $i -lt $Lines.Count; $i++) {
        if ($Lines[$i].Trim() -eq "## $Header") {
            $start = $i + 1
            break
        }
    }

    if ($start -lt 0) {
        return @()
    }

    $result = @()
    for ($j = $start; $j -lt $Lines.Count; $j++) {
        if ($Lines[$j] -match '^##\s+') {
            break
        }
        $result += $Lines[$j]
    }

    return $result
}

function Get-BulletItems {
    param([string[]]$Lines)

    $items = @()
    foreach ($line in $Lines) {
        if ($line -match '^\s*-\s+(.+)$') {
            $value = $Matches[1].Trim()
            $value = $value.Trim('`')
            if (-not [string]::IsNullOrWhiteSpace($value)) {
                $items += $value
            }
        }
    }

    return $items
}

function Get-ChangedFiles {
    $statusLines = git status --porcelain --untracked-files=all
    $files = @()

    foreach ($line in $statusLines) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }

        $path = $line.Substring(3).Trim()
        if ($path -match '\s+->\s+') {
            $path = ($path -split '\s+->\s+', 2)[1].Trim()
        }

        if ($path.StartsWith('"') -and $path.EndsWith('"')) {
            $path = $path.Substring(1, $path.Length - 2)
            $path = [regex]::Replace($path, '\\([0-7]{3})', {
                param($match)
                [char][Convert]::ToInt32($match.Groups[1].Value, 8)
            })
            $path = $path.Replace('\"', '"').Replace('\\', '\')
        }

        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $files += $path
        }
    }

    return $files
}

function Get-BaselinePath {
    param([string]$TaskFilePath)

    $repoRoot = (git rev-parse --show-toplevel).Trim()
    $resolvedTaskFile = [IO.Path]::GetFullPath((Join-Path $repoRoot $TaskFilePath))
    $key = "$repoRoot`n$resolvedTaskFile"
    $hashBytes = [System.Security.Cryptography.SHA256]::Create().ComputeHash([System.Text.Encoding]::UTF8.GetBytes($key))
    $hash = -join ($hashBytes | ForEach-Object { $_.ToString("x2") })
    $baselineDir = Join-Path ([IO.Path]::GetTempPath()) "houzicore-agent-baselines"

    return (Join-Path $baselineDir "$hash.json")
}

function Get-FileState {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return "__MISSING__"
    }

    $item = Get-Item -LiteralPath $Path
    if ($item.PSIsContainer) {
        return "__DIRECTORY__"
    }

    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Get-ChangedFileStateMap {
    param([string[]]$Paths)

    $map = @{}
    foreach ($path in ($Paths | Sort-Object -Unique)) {
        $map[$path] = Get-FileState -Path $path
    }

    return $map
}

function Read-BaselineStateMap {
    param([string]$BaselinePath)

    if (-not (Test-Path -LiteralPath $BaselinePath)) {
        return $null
    }

    $payload = Get-Content -LiteralPath $BaselinePath -Raw | ConvertFrom-Json
    $map = @{}
    foreach ($entry in $payload.files) {
        $map[$entry.path] = $entry.state
    }

    return $map
}

function Get-DeltaFiles {
    param(
        [hashtable]$BaselineMap,
        [hashtable]$CurrentMap
    )

    $allPaths = @(
        $BaselineMap.Keys
        $CurrentMap.Keys
    ) | Sort-Object -Unique

    $delta = @()
    foreach ($path in $allPaths) {
        $before = "__CLEAN__"
        if ($BaselineMap.ContainsKey($path)) {
            $before = $BaselineMap[$path]
        }

        $after = "__CLEAN__"
        if ($CurrentMap.ContainsKey($path)) {
            $after = $CurrentMap[$path]
        }

        if ($before -ne $after) {
            $delta += $path
        }
    }

    return $delta
}

function Get-YamlList {
    param(
        [string]$FilePath,
        [string]$Key
    )

    if (-not (Test-Path -LiteralPath $FilePath)) {
        return @()
    }

    $lines = Get-Content -LiteralPath $FilePath
    $items = @()
    $capturing = $false

    foreach ($line in $lines) {
        if (-not $capturing) {
            if ($line -match "^\s*$([regex]::Escape($Key))\s*:\s*$") {
                $capturing = $true
            }
            continue
        }

        if ($line -match '^\s{2}[A-Za-z0-9_]+\s*:\s*$' -or $line -match '^[A-Za-z0-9_]+\s*:\s*$') {
            break
        }

        if ($line -match '^\s*-\s+(.+)$') {
            $value = $Matches[1].Trim()
            $value = $value.Trim("'").Trim('"').Trim('`')
            if (-not [string]::IsNullOrWhiteSpace($value)) {
                $items += $value
            }
        }
    }

    return $items
}

function Test-PathAllowed {
    param(
        [string]$Path,
        [string[]]$AllowedPrefixes
    )

    foreach ($prefix in $AllowedPrefixes) {
        $normalized = $prefix.Trim().Trim('`')
        if ([string]::IsNullOrWhiteSpace($normalized)) {
            continue
        }

        $normalized = $normalized.TrimEnd('/', '\')
        if ($Path -eq $normalized) {
            return $true
        }

        if ($Path.StartsWith("$normalized/") -or $Path.StartsWith("$normalized\")) {
            return $true
        }
    }

    return $false
}

function Write-List {
    param(
        [string]$Title,
        [string[]]$Items,
        [int]$Limit = 50
    )

    Write-Host ""
    Write-Host $Title
    $visibleItems = $Items
    if ($Items.Count -gt $Limit) {
        $visibleItems = $Items | Select-Object -First $Limit
    }

    foreach ($item in $visibleItems) {
        Write-Host " - $item"
    }

    if ($Items.Count -gt $visibleItems.Count) {
        Write-Host (" - ... and {0} more" -f ($Items.Count - $visibleItems.Count))
    }
}

if (-not (Test-Path -LiteralPath $TaskFile)) {
    Write-Error "Task file not found: $TaskFile"
}

$taskLines = Get-Content -LiteralPath $TaskFile
$allowedWritePaths = Get-BulletItems (Get-SectionLines -Lines $taskLines -Header "Allowed Write Paths")
$ignoredWorktreePaths = Get-BulletItems (Get-SectionLines -Lines $taskLines -Header "Ignore Existing Worktree Paths")
$acceptanceItems = Get-BulletItems (Get-SectionLines -Lines $taskLines -Header "Acceptance")
$verificationItems = Get-BulletItems (Get-SectionLines -Lines $taskLines -Header "Verification")
$repoBoundaryFile = ".agent/contracts/repo_boundaries.yaml"
$globalIgnoredWorktreePaths = @(
    Get-YamlList -FilePath $repoBoundaryFile -Key "generated_read_only"
    Get-YamlList -FilePath $repoBoundaryFile -Key "runtime_read_only"
    Get-YamlList -FilePath $repoBoundaryFile -Key "temporary_artifacts"
) | Select-Object -Unique
$changedFiles = Get-ChangedFiles

if ($globalIgnoredWorktreePaths.Count -gt 0) {
    $changedFiles = @(
        $changedFiles | Where-Object {
            (-not (Test-PathAllowed -Path $_ -AllowedPrefixes $globalIgnoredWorktreePaths)) -or
            (Test-PathAllowed -Path $_ -AllowedPrefixes $allowedWritePaths)
        }
    )
}

if ($ignoredWorktreePaths.Count -gt 0) {
    $changedFiles = @(
        $changedFiles | Where-Object {
            -not (Test-PathAllowed -Path $_ -AllowedPrefixes $ignoredWorktreePaths)
        }
    )
}

$baselinePath = Get-BaselinePath -TaskFilePath $TaskFile
$baselineStateMap = Read-BaselineStateMap -BaselinePath $baselinePath
$currentStateMap = Get-ChangedFileStateMap -Paths $changedFiles
$deltaFiles = @()

if ($null -ne $baselineStateMap) {
    $deltaFiles = Get-DeltaFiles -BaselineMap $baselineStateMap -CurrentMap $currentStateMap
} else {
    $deltaFiles = @($changedFiles)
}

$outsideAllowed = @()
if ($allowedWritePaths.Count -gt 0) {
    foreach ($file in $deltaFiles) {
        if (-not (Test-PathAllowed -Path $file -AllowedPrefixes $allowedWritePaths)) {
            $outsideAllowed += $file
        }
    }
}

$restrictedPrefixes = @(
    Get-YamlList -FilePath $repoBoundaryFile -Key "generated_read_only"
    Get-YamlList -FilePath $repoBoundaryFile -Key "runtime_read_only"
    Get-YamlList -FilePath $repoBoundaryFile -Key "temporary_artifacts"
    Get-YamlList -FilePath $repoBoundaryFile -Key "deployment_outputs"
) | Select-Object -Unique

$restrictedTouches = @()
foreach ($file in $deltaFiles) {
    foreach ($prefix in $restrictedPrefixes) {
        if ($file.StartsWith($prefix) -and -not (Test-PathAllowed -Path $file -AllowedPrefixes $allowedWritePaths)) {
            $restrictedTouches += $file
            break
        }
    }
}

$textExtensions = @(".java", ".md", ".yml", ".yaml", ".ps1", ".xml", ".properties")
$patternRules = @(
    [pscustomobject]@{
        Id = "raw-disguise-api"
        Regex = '\bDisguiseAPI\.'
        AllowedPrefixes = @(
            "Code/Shared/src/main/java/com/houzicore/shared/core/disguise"
        )
    },
    [pscustomobject]@{
        Id = "raw-inventory-wipe"
        Regex = 'UtilInv\.Clear\('
        AllowedPrefixes = @(
            "Code/Lobby/src/main/java/com/houzicore/lobby/hub/bootstrap",
            "Code/Arcade/src/main/java/com/houzicore/arcade/bootstrap",
            "Code/MapBuilder/src/main/java/com/houzicore/mapbuilder/bootstrap",
            "Code/Shared/src/main/java/com/houzicore/shared/core/loadout",
            "Code/Shared/src/main/java/com/houzicore/shared/core/snapshot"
        )
    },
    [pscustomobject]@{
        Id = "legacy-timed-loop"
        Regex = 'BukkitRunnable|runTaskLater|runTaskTimer'
        AllowedPrefixes = @()
    },
    [pscustomobject]@{
        Id = "raw-inventory-clear"
        Regex = 'getInventory\(\)\.(clear|setContents)\('
        AllowedPrefixes = @(
            "Code/Lobby/src/main/java/com/houzicore/lobby/hub/bootstrap",
            "Code/Arcade/src/main/java/com/houzicore/arcade/bootstrap",
            "Code/MapBuilder/src/main/java/com/houzicore/mapbuilder/bootstrap",
            "Code/Shared/src/main/java/com/houzicore/shared/core/loadout",
            "Code/Shared/src/main/java/com/houzicore/shared/core/snapshot"
        )
    },
    [pscustomobject]@{
        Id = "raw-actionbar-api"
        Regex = '\bsendActionBar\('
        AllowedPrefixes = @(
            "Code/Shared/src/main/java/com/houzicore/shared/common/actionbar"
        )
    },
    [pscustomobject]@{
        Id = "implicit-actionbar-display"
        Regex = 'UtilTextBottom\.display\(\s*(?!ActionBarChannel|com\.houzicore\.shared\.common\.actionbar\.ActionBarChannel)'
        AllowedPrefixes = @(
            "Code/Shared/src/main/java/com/houzicore/shared/common/util/UtilTextBottom.java",
            "Code/Arcade/src/main/java/com/houzicore/arcade/nautilus/game/arcade/game/games"
        )
    },
    [pscustomobject]@{
        Id = "implicit-actionbar-progress"
        Regex = 'UtilTextBottom\.displayProgress\(\s*(?!ActionBarChannel|com\.houzicore\.shared\.common\.actionbar\.ActionBarChannel)'
        AllowedPrefixes = @(
            "Code/Shared/src/main/java/com/houzicore/shared/common/util/UtilTextBottom.java",
            "Code/Arcade/src/main/java/com/houzicore/arcade/nautilus/game/arcade/game/games"
        )
    },
    [pscustomobject]@{
        Id = "explicit-legacy-actionbar-channel"
        Regex = 'ActionBarChannel\.LEGACY'
        AllowedPrefixes = @(
            "Code/Shared/src/main/java/com/houzicore/shared/common/util/UtilTextBottom.java"
        )
    }
)

$patternHits = @()
foreach ($file in $deltaFiles) {
    if (-not $file.StartsWith("Code/")) {
        continue
    }

    $extension = [IO.Path]::GetExtension($file)
    if ($textExtensions -notcontains $extension) {
        continue
    }

    if (-not (Test-Path -LiteralPath $file)) {
        continue
    }

    $content = Get-Content -LiteralPath $file -Raw
    foreach ($rule in $patternRules) {
        if ($content -match $rule.Regex) {
            $isAllowed = $false
            if ($rule.AllowedPrefixes.Count -gt 0) {
                $isAllowed = Test-PathAllowed -Path $file -AllowedPrefixes $rule.AllowedPrefixes
            }

            if (-not $isAllowed) {
                $patternHits += "$($rule.Id): $file"
            }
        }
    }
}

Write-Host "Postflight summary"
Write-Host "Task file: $TaskFile"
Write-Host "Changed files in worktree: $($changedFiles.Count)"
Write-Host "Task delta since preflight baseline: $($deltaFiles.Count)"
Write-Host "Baseline snapshot: $baselinePath"

$issueCount = 0

if ($allowedWritePaths.Count -eq 0) {
    Write-Warning "Allowed Write Paths is empty. Postflight cannot validate scope reliably."
    $issueCount++
}

if ($outsideAllowed.Count -gt 0) {
    Write-List -Title "Changed files outside Allowed Write Paths" -Items $outsideAllowed
    $issueCount++
}

if ($restrictedTouches.Count -gt 0) {
    Write-List -Title "Changed files in generated, deploy, or runtime areas" -Items $restrictedTouches
    $issueCount++
}

if ($patternHits.Count -gt 0) {
    Write-List -Title "Potential forbidden-pattern hits" -Items $patternHits
    $issueCount++
}

if ($acceptanceItems.Count -eq 0) {
    Write-Warning "Acceptance checklist is empty."
    $issueCount++
}

if ($verificationItems.Count -eq 0) {
    Write-Warning "Verification section is empty."
    $issueCount++
}

if ($null -eq $baselineStateMap) {
    Write-Warning "No preflight baseline found. Postflight is evaluating the full filtered worktree instead of only task delta."
}

if ($issueCount -gt 0) {
    Write-Error "Postflight found $issueCount blocking issue(s)."
}

Write-Host ""
Write-Host "Postflight passed."
