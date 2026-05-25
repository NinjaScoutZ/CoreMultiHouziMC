package com.houzicore.mapparser.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.houzicore.mapparser.MapParserPlugin;
import com.houzicore.mapparser.MapData;
import com.houzicore.arcade.GameType;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.GameMapRequirements;
import com.houzicore.arcade.GameMapRequirements.GameReqs;
import com.houzicore.arcade.GameMapRequirements.MarkerReq;
import com.houzicore.arcade.GameMapRequirements.TeamReq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MapCommand implements CommandExecutor, TabCompleter {

    private final MapParserPlugin plugin;

    private static final List<String> SUB_COMMANDS = List.of(
            "name", "author", "gametype", "tp", "list", "info", "tool", "guide"
    );

    public MapCommand(MapParserPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.isOp()) {
            player.sendMessage(C.cRed + "Only OPs can configure maps!");
            return true;
        }

        World world = player.getWorld();
        MapData data = plugin.getData(world.getName());

        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            sendMapInfo(player, world, data);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "name" -> {
                if (args.length < 2) { player.sendMessage(C.cRed + "Usage: /map name <name>"); return true; }
                String value = joinArgs(args, 1);
                data.MapName = value;
                player.sendMessage(C.cGreen + "✅ Map name set to: " + C.cWhite + value);
            }
            case "author" -> {
                if (args.length < 2) { player.sendMessage(C.cRed + "Usage: /map author <name>"); return true; }
                String value = joinArgs(args, 1);
                data.MapCreator = value;
                player.sendMessage(C.cGreen + "✅ Map author set to: " + C.cWhite + value);
            }
            case "gametype" -> {
                if (args.length < 2) {
                    player.sendMessage(C.cRed + "Usage: /map gametype <type>");
                    showGameTypes(player);
                    return true;
                }
                try {
                    GameType type = GameType.valueOf(args[1]);
                    data.MapGameType = type;
                    player.sendMessage(C.cGreen + "✅ GameType set to: " + C.cWhite + type.GetName()
                            + C.cGray + " (" + type.name() + ")");
                } catch (IllegalArgumentException e) {
                    player.sendMessage(C.cRed + "❌ Unknown GameType: " + args[1]);
                    showGameTypes(player);
                }
            }
            case "tp" -> {
                if (args.length < 2) { player.sendMessage(C.cRed + "Usage: /map tp <world>"); return true; }
                teleportToWorld(player, args[1]);
            }
            case "list" -> {
                player.sendMessage(C.cGold + "── Loaded Worlds ──");
                for (World w : Bukkit.getWorlds()) {
                    MapData md = plugin.getData(w.getName());
                    String gameTag = (md.MapGameType == null) ? C.cGray + "(unset)" : C.cAqua + md.MapGameType.GetName();
                    player.sendMessage(C.cYellow + " • " + C.cWhite + w.getName() + " " + gameTag);
                }
            }
            case "tool" -> {
                plugin.getToolManager().openToolMenu(player);
            }
            case "guide" -> {
                sendBuildGuide(player, data);
            }
            default -> player.sendMessage(C.cRed + "Unknown subcommand. Use: /map " + String.join("|", SUB_COMMANDS));
        }

        return true;
    }

    // ─── Tab Completion ──────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(SUB_COMMANDS, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("gametype")) {
                return filterStartsWith(
                        Arrays.stream(GameType.values())
                                .map(GameType::name)
                                .collect(Collectors.toList()),
                        args[1]
                );
            }
            if (sub.equals("tp")) {
                return filterStartsWith(
                        Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()),
                        args[1]
                );
            }
        }

        return List.of();
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private void sendMapInfo(Player player, World world, MapData data) {
        player.sendMessage("");
        player.sendMessage(C.cGold + "═══ Map Info: " + C.cWhite + world.getName() + C.cGold + " ═══");
        player.sendMessage(C.cYellow + "  Name:     " + C.cWhite + data.MapName);
        player.sendMessage(C.cYellow + "  Author:   " + C.cWhite + data.MapCreator);
        player.sendMessage(C.cYellow + "  GameType: " + C.cWhite + (data.MapGameType != null ? data.MapGameType.GetName() : "None")
                + C.cGray + " (" + (data.MapGameType != null ? data.MapGameType.name() : "None") + ")");
        player.sendMessage("");
        player.sendMessage(C.cGray + "  /map name <name>        " + C.cDGray + "Set map name");
        player.sendMessage(C.cGray + "  /map author <name>      " + C.cDGray + "Set author");
        player.sendMessage(C.cGray + "  /map gametype <type>    " + C.cDGray + "Set game type");
        player.sendMessage(C.cGray + "  /map tp <world>         " + C.cDGray + "Teleport to world");
        player.sendMessage(C.cGray + "  /map list               " + C.cDGray + "List loaded worlds");
        player.sendMessage(C.cGray + "  /map tool               " + C.cDGray + "Open Builder GUI Tools");
        player.sendMessage(C.cGray + "  /map guide              " + C.cDGray + "Show build guide for current game type");
        player.sendMessage("");
    }

    private void showGameTypes(Player player) {
        player.sendMessage(C.cYellow + "Available GameTypes:");
        StringBuilder sb = new StringBuilder();
        for (GameType t : GameType.values()) {
            if (sb.length() > 0) sb.append(C.cGray + ", ");
            sb.append(C.cWhite).append(t.name());
        }
        player.sendMessage(sb.toString());
    }

    private void teleportToWorld(Player player, String targetWorld) {
        World w = Bukkit.getWorld(targetWorld);
        if (w == null) {
            java.io.File worldFolder = new java.io.File(targetWorld);
            if (worldFolder.exists() && worldFolder.isDirectory()) {
                player.sendMessage(C.cYellow + "Loading world " + targetWorld + "...");
                w = Bukkit.createWorld(new org.bukkit.WorldCreator(targetWorld));
            } else {
                player.sendMessage(C.cRed + "World '" + targetWorld + "' not found!");
                return;
            }
        }
        if (w != null) {
            player.teleport(w.getSpawnLocation());
            player.sendMessage(C.cGreen + "✅ Teleported to: " + w.getName());
        }
    }

    private String joinArgs(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString().trim();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }

    // ─── Build Guide ─────────────────────────────────────────────────

    private void sendBuildGuide(Player player, MapData data) {
        if (data.MapGameType == null || data.MapGameType.name().equals("None") || data.MapGameType.name().equals("Event")) {
            player.sendMessage(C.cRed + "❌ Set a GameType first: /map gametype <type>");
            showGameTypes(player);
            return;
        }

        GameType type = data.MapGameType;

        player.sendMessage("");
        player.sendMessage(C.cGold + "╔═══════════════════════════════════════════╗");
        player.sendMessage(C.cGold + "║ " + C.cWhite + C.Bold + "BUILD GUIDE: " + C.cAqua + C.Bold + type.GetName());
        player.sendMessage(C.cGold + "╚═══════════════════════════════════════════╝");

        if (!GameMapRequirements.hasRequirements(type)) {
            player.sendMessage(C.cRed + "⚠ " + type.GetName() + " ไม่มีข้อกำหนดเฉพาะ — ใช้แค่ corners + team spawns พื้นฐาน");
            sendBasicGuide(player);
            return;
        }

        GameReqs reqs = GameMapRequirements.getRequirements(type);

        // ── Description ──
        player.sendMessage("");
        player.sendMessage(C.cGold + "📋 " + C.cWhite + reqs.gameDescription());

        // ── Step 1: Corners ──
        player.sendMessage("");
        player.sendMessage(C.cGold + "━━━━━ " + C.cYellow + C.Bold + "ขั้นตอนที่ 1: กำหนดขอบเขต" + C.cGold + " ━━━━━");
        player.sendMessage(C.cWhite + "  วาง " + C.cAqua + "Gold Pressure Plate" + C.cWhite + " บน " + C.cAqua + "White Wool" + C.cWhite + " ที่มุมทแยง 2 จุด");
        player.sendMessage(C.cGray + "  → กำหนดพื้นที่แผนที่ (MinX/MaxX/MinZ/MaxZ)");
        player.sendMessage(C.cGray + "  → Y เท่ากัน = ใช้ world min/max height");
        player.sendMessage(C.cGray + "  → Y ต่างกัน = ใช้ min/max Y จาก corners");

        // ── Step 2: Team Spawns ──
        if (!reqs.teams().isEmpty()) {
            player.sendMessage("");
            player.sendMessage(C.cGold + "━━━━━ " + C.cYellow + C.Bold + "ขั้นตอนที่ 2: จุดเกิดทีม" + C.cGold + " ━━━━━");
            player.sendMessage(C.cGray + "  ใช้ " + C.cAqua + "Gold Pressure Plate" + C.cGray + " บน " + C.cAqua + "Colored Wool" + C.cGray + " (สีตามทีม)");
            player.sendMessage("");
            for (TeamReq team : reqs.teams()) {
                ChatColor cc = getWoolChatColor(team.color());
                player.sendMessage("  " + cc + "■ " + C.cWhite + team.teamName()
                        + C.cGray + " — " + cc + team.color() + " Wool"
                        + C.cGray + " × " + C.cWhite + team.minSpawns() + "+ จุด");
            }
        }

        // ── Step 3: Data Locs ──
        if (!reqs.dataLocs().isEmpty()) {
            player.sendMessage("");
            player.sendMessage(C.cGold + "━━━━━ " + C.cYellow + C.Bold + "ขั้นตอนที่ 3: Data Locations" + C.cGold + " ━━━━━");
            player.sendMessage(C.cGray + "  ใช้ " + C.cAqua + "Iron Pressure Plate" + C.cGray + " บน " + C.cAqua + "Colored Wool");
            player.sendMessage("");
            for (MarkerReq req : reqs.dataLocs()) {
                String icon = req.required() ? C.cRed + "★ " : C.cGray + "☆ ";
                String tag = req.required() ? C.cRed + "[จำเป็น]" : C.cGray + "[ไม่บังคับ]";
                ChatColor cc = getWoolChatColor(req.color());
                player.sendMessage("  " + icon + cc + req.color() + " Wool " + tag);
                if (req.minCount() > 1) {
                    player.sendMessage("    " + C.cGold + "ต้องมีอย่างน้อย: " + C.cWhite + req.minCount() + " จุด");
                }
                wrapDescription(player, req.description());
            }
        }

        // ── Step 4: Custom Locs ──
        if (!reqs.customLocs().isEmpty()) {
            player.sendMessage("");
            player.sendMessage(C.cGold + "━━━━━ " + C.cYellow + C.Bold + "ขั้นตอนที่ 4: Custom Locations" + C.cGold + " ━━━━━");
            player.sendMessage(C.cGray + "  ใช้ " + C.cAqua + "Sign" + C.cGray + " วางบน " + C.cAqua + "Sponge" + C.cGray + " (Flood-Fill ทุกทิศ)");
            player.sendMessage(C.cGray + "  เขียนชื่อบน Sign — ระบบจะอ่านทุกบรรทัดต่อกัน");
            player.sendMessage("");
            for (MarkerReq req : reqs.customLocs()) {
                String icon = req.required() ? C.cRed + "★ " : C.cGray + "☆ ";
                String tag = req.required() ? C.cRed + "[จำเป็น]" : C.cGray + "[ไม่บังคับ]";
                player.sendMessage("  " + icon + C.cAqua + "\"" + req.color() + "\" " + tag);
                if (req.minCount() > 1) {
                    player.sendMessage("    " + C.cGold + "ต้องมีอย่างน้อย: " + C.cWhite + req.minCount() + " จุด");
                }
                wrapDescription(player, req.description());
            }
        }

        // ── Step 5: Finalize ──
        player.sendMessage("");
        player.sendMessage(C.cGold + "━━━━━ " + C.cYellow + C.Bold + "ขั้นตอนสุดท้าย: ตรวจสอบ & Parse" + C.cGold + " ━━━━━");
        player.sendMessage(C.cWhite + "  1. ตั้ง map info:  " + C.cGray + "/map name <ชื่อ>  /map author <ผู้สร้าง>");
        player.sendMessage(C.cWhite + "  2. Dry-run check: " + C.cGray + "/parse check");
        player.sendMessage(C.cWhite + "  3. Parse จริง:    " + C.cGray + "/parse <radius>");
        player.sendMessage("");
        player.sendMessage(C.cGold + "💡 " + C.cYellow + "Tips:");
        player.sendMessage(C.cGray + "  • ใบไม้ทุกแบบจะถูกตั้งค่า persistent=true อัตโนมัติ");
        player.sendMessage(C.cGray + "  • Markers ทั้งหมดจะถูกลบเป็น AIR หลัง parse");
        player.sendMessage(C.cGray + "  • Sponge ที่เชื่อมต่อกันจะถูก flood-fill เป็น custom area");
        player.sendMessage(C.cGray + "  • ใช้ /parse check ก่อน parse จริงเสมอ!");
        player.sendMessage("");
    }

    private void sendBasicGuide(Player player) {
        player.sendMessage("");
        player.sendMessage(C.cYellow + "Basic Map Requirements:");
        player.sendMessage(C.cWhite + "  ① " + C.cAqua + "2× Corners" + C.cGray + " — Gold Pressure Plate on White Wool (มุมทแยง)");
        player.sendMessage(C.cWhite + "  ② " + C.cAqua + "Team Spawns" + C.cGray + " — Gold Pressure Plate on Colored Wool");
        player.sendMessage(C.cWhite + "  ③ " + C.cAqua + "Set info" + C.cGray + " — /map name, /map author, /map gametype");
        player.sendMessage(C.cWhite + "  ④ " + C.cAqua + "Check" + C.cGray + " — /parse check");
        player.sendMessage("");
    }

    private void wrapDescription(Player player, String desc) {
        // Split long descriptions into ~55-char lines for readability
        final int maxLen = 55;
        String remaining = desc;
        while (remaining.length() > maxLen) {
            int split = remaining.lastIndexOf(' ', maxLen);
            if (split <= 0) split = maxLen;
            player.sendMessage("    " + C.cGray + remaining.substring(0, split));
            remaining = remaining.substring(split).trim();
        }
        if (!remaining.isEmpty()) {
            player.sendMessage("    " + C.cGray + remaining);
        }
    }

    private ChatColor getWoolChatColor(String woolColor) {
        return switch (woolColor.toUpperCase()) {
            case "RED" -> ChatColor.RED;
            case "BLUE" -> ChatColor.BLUE;
            case "GREEN" -> ChatColor.GREEN;
            case "YELLOW" -> ChatColor.YELLOW;
            case "LIGHT_BLUE" -> ChatColor.AQUA;
            case "PINK" -> ChatColor.LIGHT_PURPLE;
            case "GRAY", "LIGHT_GRAY", "SILVER" -> ChatColor.GRAY;
            case "ORANGE" -> ChatColor.GOLD;
            case "CYAN" -> ChatColor.DARK_AQUA;
            case "PURPLE" -> ChatColor.DARK_PURPLE;
            case "BLACK" -> ChatColor.DARK_GRAY;
            case "BROWN" -> ChatColor.DARK_RED;
            case "LIME" -> ChatColor.GREEN;
            case "WHITE" -> ChatColor.WHITE;
            default -> ChatColor.WHITE;
        };
    }
}
