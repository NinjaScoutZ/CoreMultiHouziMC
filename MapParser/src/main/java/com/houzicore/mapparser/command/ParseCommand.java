package com.houzicore.mapparser.command;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.houzicore.mapparser.MapParserPlugin;
import com.houzicore.mapparser.MapData;
import com.houzicore.mapparser.MarkerScanner;
import com.houzicore.mapparser.Parse;
import com.houzicore.arcade.GameMapRequirements;
import com.houzicore.arcade.GameMapRequirements.GameReqs;
import com.houzicore.arcade.GameMapRequirements.MarkerReq;
import com.houzicore.arcade.GameMapRequirements.TeamReq;
import com.houzicore.shared.common.util.C;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParseCommand implements CommandExecutor, TabCompleter {

    private final MapParserPlugin plugin;

    public ParseCommand(MapParserPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.isOp()) {
            player.sendMessage(C.cRed + "Only OPs can parse maps!");
            return true;
        }

        // ── /parse check [radius] ──
        if (args.length > 0 && args[0].equalsIgnoreCase("check")) {
            int radius = 200;
            if (args.length > 1) {
                try { radius = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            }
            runDryCheck(player, radius);
            return true;
        }

        // ── /parse [radius] ── (destructive parse)
        Location parseLoc = player.getLocation();
        World world = parseLoc.getWorld();

        if (world.getName().equals("world_lobby")) {
            player.sendMessage(C.cRed + "Cannot parse Lobby.");
            return true;
        }

        MapData data = plugin.getData(world.getName());

        if (data.MapName.equals("null") || data.MapCreator.equals("null") || data.MapGameType.name().equals("None")) {
            player.sendMessage(C.cRed + "Map Name/Author/GameType are not set!");
            player.sendMessage(C.cYellow + "Use: /map name <name>");
            player.sendMessage(C.cYellow + "Use: /map author <author>");
            player.sendMessage(C.cYellow + "Use: /map gametype <gametype>");
            return true;
        }

        if (plugin.getCurrentParse() != null) {
            player.sendMessage(C.cRed + "A parse is already in progress!");
            return true;
        }

        int size = 400;
        if (args.length > 0) {
            try { size = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        }

        // Save the world before parsing
        world.save();

        // Start Parse
        plugin.setCurrentParse(new Parse(plugin, world, parseLoc, plugin.getData(world.getName()), size));
        return true;
    }

    // ─── Dry Run Check ───────────────────────────────────────────────

    private void runDryCheck(Player player, int radius) {
        World world = player.getWorld();

        player.sendMessage("");
        player.sendMessage(C.cGold + "═══ Parse Dry Run ═══ " + C.cGray + "(radius: " + radius + ")");
        player.sendMessage(C.cYellow + "Scanning markers in " + world.getName() + "...");

        // Run scan (synchronous — we're only scanning a radius, not the full world)
        MarkerScanner.ScanResult result = MarkerScanner.scan(world, player.getLocation(), radius);

        // ── Map Info ──
        MapData data = plugin.getData(world.getName());
        player.sendMessage("");
        player.sendMessage(C.cGold + "📋 Map Config:");
        player.sendMessage(formatStatus("  Name", data.MapName, !data.MapName.equals("null")));
        player.sendMessage(formatStatus("  Author", data.MapCreator, !data.MapCreator.equals("null")));
        player.sendMessage(formatStatus("  GameType", data.MapGameType != null ? data.MapGameType.GetName() : "None", data.MapGameType != null && !data.MapGameType.name().equals("None")));

        // ── Corners ──
        player.sendMessage("");
        player.sendMessage(formatStatus("📍 Corners", String.valueOf(result.cornersFound()), result.cornersFound() == 2));

        // ── Validation against Requirements ──
        boolean reqsMet = true;
        if (data.MapGameType != null && GameMapRequirements.hasRequirements(data.MapGameType)) {
            GameReqs reqs = GameMapRequirements.getRequirements(data.MapGameType);
            player.sendMessage("");
            player.sendMessage(C.cGold + "🛡️ Game Requirements: " + C.cWhite + reqs.gameDescription());

            // Check Teams
            for (TeamReq team : reqs.teams()) {
                int found = result.teamSpawns().getOrDefault(team.color(), List.of()).size();
                boolean ok = found >= team.minSpawns();
                if (!ok) reqsMet = false;
                player.sendMessage(formatStatus("  Team " + team.teamName() + " (" + team.color() + ")", found + " / " + team.minSpawns(), ok));
            }

            // Check Data Locs
            if (!reqs.dataLocs().isEmpty()) {
                player.sendMessage(C.cGray + "  Data Locations:");
                for (MarkerReq mReq : reqs.dataLocs()) {
                    int found = result.dataLocs().getOrDefault(mReq.color(), List.of()).size();
                    boolean ok = !mReq.required() || found >= mReq.minCount();
                    if (!ok) reqsMet = false;
                    player.sendMessage(formatStatus("    " + mReq.color(), found + " (need " + mReq.minCount() + ") " + C.cGray + "- " + mReq.description(), ok));
                }
            }

            // Check Custom Locs
            if (!reqs.customLocs().isEmpty()) {
                player.sendMessage(C.cGray + "  Custom Locations:");
                for (MarkerReq mReq : reqs.customLocs()) {
                    int found;
                    if (mReq.color().startsWith("(")) {
                        // Wildcard — count ALL custom locs as potential matches
                        // Used by SpeedBuilders where each build has a unique name
                        found = result.customLocs().values().stream().mapToInt(List::size).sum();
                    } else {
                        found = result.customLocs().getOrDefault(mReq.color(), List.of()).size();
                    }
                    boolean ok = !mReq.required() || found >= mReq.minCount();
                    if (!ok) reqsMet = false;
                    player.sendMessage(formatStatus("    \"" + mReq.color() + "\"", found + " (need " + mReq.minCount() + ") " + C.cGray + "- " + mReq.description(), ok));
                }
            }
            
            // Check for extraneous markers not requested by the game type
            for (String c : result.dataLocs().keySet()) {
                if (reqs.dataLocs().stream().noneMatch(r -> r.color().equals(c))) {
                    player.sendMessage(C.cRed + "  ⚠ Extra Data Loc found: " + c + " (Game doesn't need this)");
                }
            }

        } else {
            player.sendMessage("");
            player.sendMessage(C.cRed + "⚠ No strict requirements defined for " + (data.MapGameType != null ? data.MapGameType.name() : "this gametype") + "!");
            player.sendMessage(C.cGray + "  (Will only check basic bounds and team spawns)");
            
            // Check Teams if no requirements defined
            player.sendMessage("");
            if (result.teamSpawns().isEmpty()) {
                player.sendMessage(C.cRed + "👥 Team Spawns: ❌ NONE FOUND");
                reqsMet = false;
            } else {
                player.sendMessage(C.cGold + "👥 Team Spawns:");
                for (Map.Entry<String, List<Location>> entry : result.teamSpawns().entrySet()) {
                    player.sendMessage(C.cAqua + "  " + entry.getKey() + C.cGray + " × " + C.cWhite + entry.getValue().size());
                }
            }
        }

        // ── Warnings ──
        if (!result.warnings().isEmpty()) {
            player.sendMessage("");
            player.sendMessage(C.cRed + "⚠ Warnings:");
            for (String warning : result.warnings()) {
                player.sendMessage(C.cRed + "  • " + warning);
            }
        }

        // ── Summary ──
        player.sendMessage("");
        int total = result.totalMarkers();
        boolean allGood = result.warnings().isEmpty()
                && result.cornersFound() == 2
                && reqsMet
                && !data.MapName.equals("null")
                && !data.MapCreator.equals("null")
                && data.MapGameType != null
                && !data.MapGameType.name().equals("None");

        if (allGood) {
            player.sendMessage(C.cGreen + "✅ Ready to parse! " + C.cGray + "(" + total + " markers) " + C.cWhite + "Run: /parse " + radius);
        } else {
            player.sendMessage(C.cRed + "❌ Not ready — fix warnings/requirements above before parsing.");
        }
        player.sendMessage("");
    }

    private String formatStatus(String label, String value, boolean ok) {
        String icon = ok ? C.cGreen + "✓" : C.cRed + "✗";
        return icon + C.cYellow + " " + label + ": " + (ok ? C.cWhite : C.cRed) + value;
    }

    // ─── Tab Completion ──────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return List.of("check", "200", "400", "600").stream()
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
