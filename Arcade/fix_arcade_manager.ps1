$filePath = "E:\Houzicore\Code\Arcade\src\main\java\com\houzicore\arcade\ArcadeManager.java"
$lines = Get-Content -Path $filePath
$newLines = @()
$newLines += $lines[0..152]
$newLines += $lines[306..($lines.Length - 1)]
Set-Content -Path $filePath -Value $newLines
Write-Host "File saved, length: $($newLines.Length)"
