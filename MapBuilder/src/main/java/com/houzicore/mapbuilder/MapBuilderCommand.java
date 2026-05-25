package com.houzicore.mapbuilder;

import com.houzicore.mapbuilder.domain.MapPointDefinition;
import com.houzicore.mapbuilder.session.BuilderSessionState;
import com.houzicore.mapbuilder.template.MapTemplateRegistry;
import com.houzicore.mapbuilder.tool.FinishToolHandler;
import com.houzicore.mapbuilder.tool.UndoRedoToolHandler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /mb — MapBuilder command.
 *
 * Subcommands:
 *   create  <GameType> <MapName>
 *   edit    <GameType> <MapName>
 *   import  <GameType> <MapName>
 *   open       — re-open dashboard / wands
 *   wand       — re-give tool set
 *   undo       — undo last action
 *   redo       — redo last undone action
 *   deselect   — clear point selection
 *   select <pointType>  — power-user: select by exportKey or displayName
 *   validate   — print validation report
 *   export     — validate + export
 *   cancel     — cancel session
 *   help
 */
public class MapBuilderCommand implements CommandExecutor, TabCompleter {

    private static final String PERM = "mapbuilder.admin";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!player.hasPermission(PERM)) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length == 0) { sendHelp(player); return true; }

        String sub = args[0].toLowerCase();

        switch (sub) {

            // ── Session lifecycle ──────────────────────────────────────────
            case "create" -> {
                String[] parsed = parseArgs(args);
                if (parsed == null) { player.sendMessage(ChatColor.RED + "Usage: /mb create <GameType> <MapName>"); return true; }
                MapBuilderPlugin.getInstance().startSession(player, parsed[0], parsed[1], com.houzicore.mapbuilder.session.EditMode.SANDBOX);
            }
            case "import" -> {
                String[] parsed = parseArgs(args);
                if (parsed == null) { player.sendMessage(ChatColor.RED + "Usage: /mb import <GameType> <MapName>"); return true; }
                WorldImporter.importMap(player, parsed[0], parsed[1]);
            }
            case "edit" -> {
                String[] parsed = parseArgs(args);
                if (parsed == null) { player.sendMessage(ChatColor.RED + "Usage: /mb edit <GameType> <MapName>"); return true; }
                WorldImporter.editCurrentWorld(player, parsed[0], parsed[1]);
            }
            case "cancel" -> MapBuilderPlugin.getInstance().endSession(player, false, false);
            case "rollback" -> {
                File dir = null;
                MapSession activeSession = MapBuilderPlugin.getInstance().getSession(player);

                // 1. Target Resolution
                if (activeSession != null) {
                    dir = activeSession.getWorldConfigFile().getParentFile();
                } else if (args.length >= 3 && args[1].equalsIgnoreCase("Sandbox")) {
                    String mapName = args[2];
                    File pluginDir = MapBuilderPlugin.getInstance().getDataFolder();
                    dir = new File(pluginDir, "sandbox_exports" + File.separator + mapName);
                } else {
                    dir = player.getWorld().getWorldFolder();
                }

                File datBak = new File(dir, "WorldConfig.dat.bak");
                File schemaBak = new File(dir, "schema.json.bak");

                // 2. Safety Check
                if (!datBak.exists() || !schemaBak.exists()) {
                    player.sendMessage(ChatColor.RED + "Rollback blocked: Both WorldConfig.dat.bak and schema.json.bak must exist. Found incomplete backups in " + dir.getName());
                    return true;
                }

                try {
                    File dat = new File(dir, "WorldConfig.dat");
                    File schema = new File(dir, "schema.json");

                    // 3. Restore files
                    java.nio.file.Files.copy(datBak.toPath(), dat.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    java.nio.file.Files.copy(schemaBak.toPath(), schema.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    // 4. Reload active session if matching
                    if (activeSession != null && activeSession.getWorldConfigFile().getParentFile().equals(dir)) {
                        activeSession.clearSession();
                        if (com.houzicore.mapbuilder.VisualManager.getInstance() != null) {
                            com.houzicore.mapbuilder.VisualManager.getInstance().clearVisuals(player.getUniqueId());
                        }
                        WorldImporter.loadFilesIntoSession(player, activeSession, player.getLocation());
                        com.houzicore.mapbuilder.bootstrap.MapBuilderBootstrap.getInstance()
                                .getPlayerStateApplier().applyContextState(player, com.houzicore.shared.api.context.PlayerContextId.MAP_EDIT);
                    }

                    player.sendMessage(ChatColor.GREEN + "Rollback successful! Both configuration files restored from .bak for " + dir.getName());
                } catch (IOException e) {
                    player.sendMessage(ChatColor.RED + "Rollback failed during file operations: " + e.getMessage());
                }
            }

            case "open", "dashboard" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                com.houzicore.mapbuilder.gui.DashboardGui.getInstance().open(player);
            }
            case "wand", "tools" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                com.houzicore.mapbuilder.bootstrap.MapBuilderBootstrap.getInstance()
                        .getPlayerStateApplier().applyContextState(player, com.houzicore.shared.api.context.PlayerContextId.MAP_EDIT);
                player.sendMessage(ChatColor.GREEN + "Tools re-given.");
            }

            // ── Selection ──────────────────────────────────────────────────
            case "deselect" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                s.getState().deselectPoint();
                s.getState().clearPendingRegion();
                player.sendMessage(ChatColor.GRAY + "Selection cleared.");
            }
            case "select" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                if (args.length < 2) { player.sendMessage(ChatColor.RED + "Usage: /mb select <exportKey>"); return true; }
                String key = args[1];
                MapPointDefinition def = MapPointDefinition.fromExportKey(key);
                if (def == null) {
                    // Try matching display name (case-insensitive)
                    String lower = key.toLowerCase();
                    for (MapPointDefinition d : MapPointDefinition.values()) {
                        if (d.displayName.toLowerCase().contains(lower)) { def = d; break; }
                    }
                }
                if (def == null) { player.sendMessage(ChatColor.RED + "Unknown point type: " + key); return true; }
                s.getState().selectPoint(def);
                player.sendMessage(ChatColor.GREEN + "Selected: §e" + def.displayName);
            }

            // ── Undo / Redo ──
            case "undo" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                UndoRedoToolHandler.performUndo(player, s);
            }
            case "redo" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                UndoRedoToolHandler.performRedo(player, s);
            }

            // ── Status ──
            case "status" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                player.sendMessage(ChatColor.GOLD + "── MapBuilder Status ──");
                player.sendMessage(ChatColor.YELLOW + "Mode: " + ChatColor.WHITE + s.getMode().name());
                player.sendMessage(ChatColor.YELLOW + "Edit World: " + ChatColor.WHITE + s.getEditWorldName());
                player.sendMessage(ChatColor.YELLOW + "Export Target: " + ChatColor.WHITE + s.getWorldConfigFile().getParentFile().getAbsolutePath());
                File datBak = new File(s.getWorldConfigFile().getPath() + ".bak");
                File schemaBak = new File(s.getWorldConfigFile().getParentFile(), "schema.json.bak");
                player.sendMessage(ChatColor.YELLOW + "Backup WorldConfig.dat: " + ChatColor.WHITE + (datBak.exists() ? "Yes" : "No"));
                player.sendMessage(ChatColor.YELLOW + "Backup schema.json: " + ChatColor.WHITE + (schemaBak.exists() ? "Yes" : "No"));

                int totalPoints = s.getDataPoints().values().stream().mapToInt(java.util.List::size).sum();
                player.sendMessage(ChatColor.YELLOW + "Points Placed: " + ChatColor.WHITE + totalPoints + " (across " + s.getDataPoints().size() + " types)");
            }

            // ── Cleanup ──
            case "cleanup" -> {
                if (com.houzicore.mapbuilder.VisualManager.getInstance() != null) {
                    com.houzicore.mapbuilder.VisualManager.getInstance().clearVisuals(player.getUniqueId());
                }
                int count = 0;
                for (org.bukkit.entity.Entity e : player.getWorld().getEntities()) {
                    if (e.getScoreboardTags().contains("houzicore_mapbuilder_visual")) {
                        e.remove();
                        count++;
                    }
                }
                player.sendMessage(ChatColor.GREEN + "Force cleared " + count + " active/orphaned visual entities.");
            }

            // ── Validate ──
            case "validate" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                com.houzicore.mapbuilder.service.ValidationReport report = com.houzicore.mapbuilder.service.ValidationService.validate(s);
                player.sendMessage(ChatColor.GOLD + "── Validation: " + s.getMapName() + " ──");

                if (report.hasWarnings()) {
                    player.sendMessage(ChatColor.YELLOW + "⚠ Warnings:");
                    report.getWarnings().forEach(w -> player.sendMessage(ChatColor.GRAY + " - " + w));
                }

                if (!report.hasErrors()) {
                    player.sendMessage(ChatColor.GREEN + "All checks passed — ready to export!");
                } else {
                    player.sendMessage(ChatColor.RED + "§lErrors found (Export Blocked):");
                    report.getErrors().forEach(e -> player.sendMessage(ChatColor.RED + " ✗ " + e));
                    player.sendMessage(ChatColor.RED + "Fix issues before exporting (or use --force).");
                }
            }

            // ── Properties (MB-09D) ─────────────────────────────────────────
            case "props" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                java.util.Map<String, String> props = s.getProperties();
                player.sendMessage(ChatColor.GOLD + "── Session Properties (" + props.size() + ") ──");
                if (props.isEmpty()) {
                    player.sendMessage(ChatColor.GRAY + "(none)");
                } else {
                    props.forEach((k, v) -> player.sendMessage(
                            ChatColor.YELLOW + k + ChatColor.WHITE + " = " + ChatColor.AQUA + v));
                }
            }
            case "setprop" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /mb setprop <KEY> <VALUE>");
                    return true;
                }
                String propKey = args[1].toUpperCase();
                // Join all remaining args so multi-word values work (e.g. "my label")
                String propVal = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                // Block exact reserved keys AND structured prefixes (e.g. TEAM_NAME:RED, DATA_NAME:X)
                if (com.houzicore.mapbuilder.schema.MapSchema.isReservedKey(propKey)) {
                    player.sendMessage(ChatColor.RED + "'" + propKey + "' uses a reserved key or prefix and cannot be used as a property.");
                    return true;
                }
                s.setProperty(propKey, propVal);
                player.sendMessage(ChatColor.GREEN + "Set property: " + ChatColor.YELLOW + propKey
                        + ChatColor.WHITE + " = " + ChatColor.AQUA + propVal);
            }

            case "delprop" -> {
                MapSession s = requireSession(player);
                if (s == null) return true;
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /mb delprop <KEY>");
                    return true;
                }
                String propKey = args[1].toUpperCase();
                boolean removed = s.deleteProperty(propKey);
                if (removed) {
                    player.sendMessage(ChatColor.GREEN + "Removed property: " + ChatColor.YELLOW + propKey);
                } else {
                    player.sendMessage(ChatColor.RED + "Property '" + propKey + "' not found in session.");
                }
            }

            // ── Display Model Imports ──────────────────────────────────────
            case "importmodel", "bdimport" -> {
                handleModelImport(player, args);
            }

            // ── Export ──
            case "export" -> {
                boolean force = args.length >= 2 && args[1].equalsIgnoreCase("--force");
                MapBuilderPlugin.getInstance().endSession(player, true, force);
            }

            // ── Admin/dev ───────────────────────────────────────────────────
            case "sandbox", "adminbuilder" ->
                MapBuilderPlugin.getInstance().startSession(player, "Sandbox", "AdminWorld");

            default -> sendHelp(player);
        }
        return true;
    }

    private String[] parseArgs(String[] args) {
        if (args.length < 3) return null;
        String mapName = args[args.length - 1];
        String gameType = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
        return new String[]{gameType, mapName};
    }

    private void handleModelImport(Player player, String[] args) {
        com.houzicore.shared.core.displayentity.DisplayEntityManager manager =
                MapBuilderPlugin.getInstance().getDisplayEntityManager();

        if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
            java.util.List<String> ids = manager.getRegistry().getModels().stream()
                    .map(com.houzicore.shared.core.displayentity.DisplayModel::getId)
                    .sorted()
                    .toList();
            player.sendMessage(ChatColor.GOLD + "Display Models (" + ids.size() + "): "
                    + ChatColor.WHITE + String.join(", ", ids.stream().limit(30).toList()));
            return;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("reload")) {
            manager.reloadRegistry();
            player.sendMessage(ChatColor.GREEN + "Reloaded display models. Loaded: "
                    + manager.getRegistry().getModels().size());
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /mb importmodel <id> <url|fileName> [--force]");
            player.sendMessage(ChatColor.GRAY + "Local files must be in: "
                    + manager.getRegistry().getModelsDirectory().getAbsolutePath());
            return;
        }

        String modelId = args[1];
        String source = args[2];
        boolean force = Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("--force"));

        player.sendMessage(ChatColor.YELLOW + "Importing display model '" + modelId + "'...");
        MapBuilderPlugin plugin = MapBuilderPlugin.getInstance();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                com.houzicore.shared.core.displayentity.DisplayModelImporter.SourceContent content =
                        com.houzicore.shared.core.displayentity.DisplayModelImporter.loadSource(manager, source);

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    try {
                        com.houzicore.shared.core.displayentity.DisplayModelImporter.ImportResult result =
                                com.houzicore.shared.core.displayentity.DisplayModelImporter.importModelFromText(
                                        manager, modelId, content.text(), source, content.sourceDescription(), force);
                        player.sendMessage(ChatColor.GREEN + "Imported model: " + ChatColor.YELLOW
                                + result.modelId() + ChatColor.GRAY + " (" + result.parts() + " parts)");
                        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
                        if (session != null) {
                            session.getState().selectDisplayModel(result.modelId());
                            player.sendMessage(ChatColor.AQUA + "Selected imported model for Display Tool.");
                        }
                    } catch (IOException e) {
                        player.sendMessage(ChatColor.RED + "Model import failed: " + e.getMessage());
                    }
                });
            } catch (IOException e) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "Model import failed: " + e.getMessage()));
            }
        });
    }


    private MapSession requireSession(Player player) {
        MapSession s = MapBuilderPlugin.getInstance().getSession(player);
        if (s == null) player.sendMessage(ChatColor.RED + "No active session. Use /mb create, /mb edit, or /mb import.");
        return s;
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission(PERM)) return out;

        if (args.length == 1) {
            List<String> subs = Arrays.asList(
                    "create","import","edit","rollback","wand","undo","redo","deselect",
                    "select","validate","export","cancel","help","status","cleanup",
                    "props","setprop","delprop","importmodel","bdimport");
            for (String s : subs) if (s.startsWith(args[0].toLowerCase())) out.add(s);
        } else if (args.length == 2 && "select".equalsIgnoreCase(args[0])) {
            String q = args[1].toLowerCase();
            for (MapPointDefinition def : MapPointDefinition.values()) {
                if (def.exportKey.toLowerCase().contains(q)
                        || def.displayName.toLowerCase().contains(q)) {
                    out.add(def.exportKey);
                }
            }
        } else if (args.length >= 2 && ("create".equalsIgnoreCase(args[0]) || "edit".equalsIgnoreCase(args[0]) || "import".equalsIgnoreCase(args[0]))) {
            String baseCmd = args[0].toLowerCase();
            File mapsDir = getMapsDirectory();
            java.util.List<String> validGameTypes = new ArrayList<>();
            if (mapsDir != null) {
                File[] dirs = mapsDir.listFiles(File::isDirectory);
                if (dirs != null) for (File d : dirs) validGameTypes.add(d.getName());
            }
            for (String gt : MapTemplateRegistry.getInstance().getGameTypes()) {
                if (!validGameTypes.contains(gt)) validGameTypes.add(gt);
            }

            String lastArg = args[args.length - 1];
            String[] previousArgs = Arrays.copyOfRange(args, 1, args.length - 1);
            String previousJoined = String.join(" ", previousArgs);

            boolean isMapNameContext = false;
            for (String gt : validGameTypes) {
                if (gt.equalsIgnoreCase(previousJoined)) {
                    isMapNameContext = true;
                    break;
                }
            }

            if (isMapNameContext) {
                if (mapsDir != null) {
                    File gameDir = new File(mapsDir, previousJoined);
                    if (gameDir.isDirectory()) {
                        File[] files = gameDir.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                String n = f.getName();
                                if (n.endsWith(".zip")) {
                                    n = n.replace(".zip", "");
                                }
                                if (n.toLowerCase().startsWith(lastArg.toLowerCase())) {
                                    if (!out.contains(n)) out.add(n);
                                }
                            }
                        }
                    }
                }

                if ("edit".equals(baseCmd) && sender instanceof Player player) {
                    String worldName = player.getWorld().getName();
                    if (worldName.toLowerCase().startsWith(lastArg.toLowerCase())) {
                        if (!out.contains(worldName)) out.add(worldName);
                    }
                }
            } else {
                String currentJoined = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
                for (String gt : validGameTypes) {
                    if (gt.toLowerCase().startsWith(currentJoined)) {
                        String[] gtParts = gt.split(" ");
                        int currentArgIndex = args.length - 2;
                        if (currentArgIndex >= 0 && currentArgIndex < gtParts.length) {
                             String suggestion = gtParts[currentArgIndex];
                             if (!out.contains(suggestion)) {
                                 out.add(suggestion);
                             }
                        }
                    }
                }
            }
        } else if (args.length == 2 && ("importmodel".equalsIgnoreCase(args[0]) || "bdimport".equalsIgnoreCase(args[0]))) {
            for (String option : Arrays.asList("list", "reload")) {
                if (option.startsWith(args[1].toLowerCase())) out.add(option);
            }
        }
        return out;
    }

    private File getMapsDirectory() {
        for (File path : new File[]{new File("../../Maps"), new File("../Maps"), new File("Maps")}) {
            if (path.exists() && path.isDirectory()) return path;
        }
        return null;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "══ MapBuilder Help ══");
        player.sendMessage(ChatColor.YELLOW + "/mb create <GameType> <Name>" + ChatColor.WHITE + " — Start blank sandbox session");
        player.sendMessage(ChatColor.YELLOW + "/mb import <GameType> <Name>" + ChatColor.WHITE + " — Import existing zip map");
        player.sendMessage(ChatColor.YELLOW + "/mb edit <GameType> <Name>"   + ChatColor.WHITE + " — Edit current world in-place (Lobby/Hub)");
        player.sendMessage(ChatColor.YELLOW + "/mb rollback [Sandbox <Map>]" + ChatColor.WHITE + " — Revert current world or specific Sandbox from backups");
        player.sendMessage(ChatColor.YELLOW + "/mb wand" + ChatColor.WHITE + "        — Re-give all tools");
        player.sendMessage(ChatColor.YELLOW + "/mb undo / redo"  + ChatColor.WHITE + " — Undo/redo last action");
        player.sendMessage(ChatColor.YELLOW + "/mb deselect"     + ChatColor.WHITE + " — Clear point selection");
        player.sendMessage(ChatColor.YELLOW + "/mb select <key>" + ChatColor.WHITE + " — Quick-select a point type");
        player.sendMessage(ChatColor.YELLOW + "/mb props"          + ChatColor.WHITE + " — List current session properties");
        player.sendMessage(ChatColor.YELLOW + "/mb setprop <K> <V>" + ChatColor.WHITE + " — Set a custom property (e.g. DISGUISE_TYPE BAT)");
        player.sendMessage(ChatColor.YELLOW + "/mb delprop <K>"    + ChatColor.WHITE + " — Remove a property from the session");
        player.sendMessage(ChatColor.YELLOW + "/mb importmodel <id> <url|file>" + ChatColor.WHITE + " — Import/select a BDEngine display model");
        player.sendMessage(ChatColor.YELLOW + "/mb status"       + ChatColor.WHITE + " — View session status");
        player.sendMessage(ChatColor.YELLOW + "/mb cleanup"      + ChatColor.WHITE + " — Forcibly clear stuck visuals");
        player.sendMessage(ChatColor.YELLOW + "/mb validate"     + ChatColor.WHITE + " — Check map completeness");
        player.sendMessage(ChatColor.YELLOW + "/mb export"       + ChatColor.WHITE + " — Validate + save WorldConfig.dat (use --force to bypass)");
        player.sendMessage(ChatColor.YELLOW + "/mb cancel"       + ChatColor.WHITE + " — Cancel session without saving");
    }
}
