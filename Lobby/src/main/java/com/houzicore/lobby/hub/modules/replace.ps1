$path = "E:\Houzicore\Code\Lobby\src\main\java\com\houzicore\lobby\hub\modules\WorldManager.java"
$content = Get-Content $path -Raw
$content = $content -replace "(?<=public void BlockBreak\(BlockBreakEvent event\)\s*\{\s*if \(event\.getPlayer\(\)\.getGameMode\(\) == GameMode\.CREATIVE\)\s*return;\s*)event\.setCancelled\(true\);", "if (Manager.getArenaManager() != null && Manager.getArenaManager().isPlayerInMatch(event.getPlayer())) return; event.setCancelled(true);"
$content = $content -replace "(?<=public void BlockPlace\(BlockPlaceEvent event\)\s*\{\s*if \(event\.getPlayer\(\)\.getGameMode\(\) == GameMode\.CREATIVE\)\s*return;\s*)event\.setCancelled\(true\);", "if (Manager.getArenaManager() != null && Manager.getArenaManager().isPlayerInMatch(event.getPlayer())) return; event.setCancelled(true);"
Set-Content $path $content
