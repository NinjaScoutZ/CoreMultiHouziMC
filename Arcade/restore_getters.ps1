$diffLines = Get-Content -Path "E:\Houzicore\arcade_diff.txt"
$gettersBlock = $diffLines[311..442] | ForEach-Object { 
    if ($_.Length -ge 1) { $_.Substring(1) } else { "" }
}

$amPath = "E:\Houzicore\Code\Arcade\src\main\java\com\houzicore\arcade\ArcadeManager.java"
$amLines = Get-Content -Path $amPath
$newAmLines = @()
$newAmLines += $amLines[0..($amLines.Length - 2)]
$newAmLines += $gettersBlock
$newAmLines += $amLines[$amLines.Length - 1]

Set-Content -Path $amPath -Value $newAmLines
Write-Host "Restored $($gettersBlock.Length) getter lines successfully."
