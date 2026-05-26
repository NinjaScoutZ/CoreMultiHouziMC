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
    $statusLines = git status --porcelain -uno
    $files = @()

    foreach ($line in $statusLines) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }
        if ($line.StartsWith(" D") -or $line.StartsWith("D")) {
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

    if (-not (Test-Path -LiteralPath $baselineDir)) {
        New-Item -ItemType Directory -Path $baselineDir -Force | Out-Null
    }

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

function Save-Baseline {
    param(
        [string]$BaselinePath,
        [string]$TaskFilePath,
        [hashtable]$StateMap
    )

    $repoRoot = (git rev-parse --show-toplevel).Trim()
    $payload = [ordered]@{
        schema = 1
        repo_root = $repoRoot
        task_file = [IO.Path]::GetFullPath((Join-Path $repoRoot $TaskFilePath))
        captured_at = (Get-Date).ToString("o")
        files = @(
            $StateMap.GetEnumerator() |
                Sort-Object Name |
                ForEach-Object {
                    [ordered]@{
                        path = $_.Name
                        state = $_.Value
                    }
                }
        )
    }

    $payload | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $BaselinePath -Encoding UTF8
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
$requiredSections = @(
    "Objective",
    "Scope",
    "Non-Goals",
    "Allowed Write Paths",
    "Acceptance",
    "Verification"
)

$missingSections = @()
foreach ($section in $requiredSections) {
    if ((Get-SectionLines -Lines $taskLines -Header $section).Count -eq 0) {
        $missingSections += $section
    }
}

$allowedWritePaths = Get-BulletItems (Get-SectionLines -Lines $taskLines -Header "Allowed Write Paths")
$ignoredWorktreePaths = Get-BulletItems (Get-SectionLines -Lines $taskLines -Header "Ignore Existing Worktree Paths")
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

$restrictedPrefixes = @(
    Get-YamlList -FilePath $repoBoundaryFile -Key "generated_read_only"
    Get-YamlList -FilePath $repoBoundaryFile -Key "runtime_read_only"
    Get-YamlList -FilePath $repoBoundaryFile -Key "temporary_artifacts"
    Get-YamlList -FilePath $repoBoundaryFile -Key "deployment_outputs"
) | Select-Object -Unique

$topLevelCounts = @{}
foreach ($file in $changedFiles) {
    $root = ($file -split '[\\/]', 2)[0]
    if (-not $topLevelCounts.ContainsKey($root)) {
        $topLevelCounts[$root] = 0
    }
    $topLevelCounts[$root]++
}

$ambientOutsideAllowed = @()
if ($allowedWritePaths.Count -gt 0) {
    foreach ($file in $changedFiles) {
        if (-not (Test-PathAllowed -Path $file -AllowedPrefixes $allowedWritePaths)) {
            $ambientOutsideAllowed += $file
        }
    }
}

$ambientRestrictedTouches = @()
foreach ($file in $changedFiles) {
    foreach ($prefix in $restrictedPrefixes) {
        if ($file.StartsWith($prefix) -and -not (Test-PathAllowed -Path $file -AllowedPrefixes $allowedWritePaths)) {
            $ambientRestrictedTouches += $file
            break
        }
    }
}

$baselinePath = Get-BaselinePath -TaskFilePath $TaskFile
$baselineStateMap = Get-ChangedFileStateMap -Paths $changedFiles
Save-Baseline -BaselinePath $baselinePath -TaskFilePath $TaskFile -StateMap $baselineStateMap

Write-Host "Preflight summary"
Write-Host "Task file: $TaskFile"
Write-Host "Changed files in worktree: $($changedFiles.Count)"
Write-Host "Baseline snapshot: $baselinePath"

if ($topLevelCounts.Count -gt 0) {
    Write-Host ""
    Write-Host "Top-level change distribution"
    foreach ($entry in $topLevelCounts.GetEnumerator() | Sort-Object Value -Descending) {
        Write-Host (" - {0}: {1}" -f $entry.Key, $entry.Value)
    }
}

$issueCount = 0

if ($TaskFile -like "*TEMPLATE.md") {
    Write-Warning "You are using the template file. Copy it to tasks/T-xxx.md before real task execution."
    $issueCount++
}

if ($missingSections.Count -gt 0) {
    Write-List -Title "Missing required task sections" -Items $missingSections
    $issueCount++
}

if ($allowedWritePaths.Count -eq 0) {
    Write-Warning "Allowed Write Paths is empty. Define explicit write scope before coding."
    $issueCount++
}

if ($ambientOutsideAllowed.Count -gt 0) {
    Write-List -Title "Ambient dirty files outside Allowed Write Paths (captured into baseline)" -Items $ambientOutsideAllowed
}

if ($ambientRestrictedTouches.Count -gt 0) {
    Write-List -Title "Ambient dirty files in generated, deploy, or runtime areas (captured into baseline)" -Items $ambientRestrictedTouches
}

if ($changedFiles.Count -gt 150) {
    Write-Warning "Worktree is very noisy. Narrow the task boundary and verify changed files carefully."
}

if ($issueCount -gt 0) {
    Write-Error "Preflight failed with $issueCount blocking issue(s)."
}

Write-Host ""
Write-Host "Preflight passed."
