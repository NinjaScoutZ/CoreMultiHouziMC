package com.houzicore.mapparser.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.mapparser.MapParserPlugin;
import com.houzicore.mapparser.MapData;
import com.houzicore.mapparser.MarkerScanner;
import com.houzicore.arcade.GameMapRequirements;
import com.houzicore.arcade.GameMapRequirements.GameReqs;
import com.houzicore.arcade.GameMapRequirements.MarkerReq;
import com.houzicore.arcade.GameMapRequirements.TeamReq;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.scoreboard.ScoreboardSidebar;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapParserScoreboard implements org.bukkit.event.Listener {

    private final MapParserPlugin plugin;
    private final Map<UUID, ScoreboardSidebar> boards = new HashMap<>();

    public MapParserScoreboard(MapParserPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    @org.bukkit.event.EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    public void addPlayer(Player player) {
        ScoreboardSidebar board = ScoreboardManager.getInstance().createSidebar();
        board.addPlayer(player);
        board.title(LegacyComponentSerializer.legacySection().deserialize(C.cGold + C.Bold + "Map Parser"));
        boards.put(player.getUniqueId(), board);
    }

    public void removePlayer(Player player) {
        ScoreboardSidebar board = boards.remove(player.getUniqueId());
        if (board != null) {
            board.close();
        }
    }

    private void startTask() {
        // Run every 2 seconds (40 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isOp()) continue;

                    ScoreboardSidebar board = boards.get(player.getUniqueId());
                    if (board == null) {
                        addPlayer(player);
                        board = boards.get(player.getUniqueId());
                    }

                    updateBoard(player, board);
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    private void updateBoard(Player player, ScoreboardSidebar board) {
        World world = player.getWorld();

        // Don't show in lobby or if parse is in progress
        if (world.getName().equals("world_lobby") || plugin.getCurrentParse() != null) {
            updateLines(board, List.of(
                    "",
                    C.cRed + "Waiting for map...",
                    ""
            ));
            return;
        }

        MapData data = plugin.getData(world.getName());
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(C.cYellow + "Map: " + C.cWhite + (data.MapName.equals("null") ? "None" : data.MapName));
        lines.add(C.cYellow + "Game: " + C.cWhite + (data.MapGameType != null ? data.MapGameType.GetName() : "None"));
        lines.add("");

        // Perform a quick scan (radius 150 is usually enough for a quick live check)
        // This is synchronous but lightweight enough for a few ops on a builder server
        MarkerScanner.ScanResult result = MarkerScanner.scan(world, player.getLocation(), 150);

        lines.add(C.cGold + C.Bold + "Requirements:");

        if (data.MapGameType == null || !GameMapRequirements.hasRequirements(data.MapGameType)) {
            lines.add(C.cGray + "No strict rules.");
            lines.add(formatLine("Corners", result.cornersFound(), 2));
        } else {
            GameReqs reqs = GameMapRequirements.getRequirements(data.MapGameType);
            
            lines.add(formatLine("Corners", result.cornersFound(), 2));

            for (TeamReq team : reqs.teams()) {
                int found = result.teamSpawns().getOrDefault(team.color(), List.of()).size();
                lines.add(formatLine("Team " + team.teamName(), found, team.minSpawns()));
            }

            for (MarkerReq mReq : reqs.dataLocs()) {
                if (!mReq.required() && mReq.minCount() == 0) continue; // Skip optional without strict min
                int found = result.dataLocs().getOrDefault(mReq.color(), List.of()).size();
                lines.add(formatLine(mReq.color() + " Loc", found, mReq.minCount()));
            }

            for (MarkerReq mReq : reqs.customLocs()) {
                if (!mReq.required() && mReq.minCount() == 0) continue;
                int found = result.customLocs().getOrDefault(mReq.color(), List.of()).size();
                lines.add(formatLine("Loc '" + mReq.color() + "'", found, mReq.minCount()));
            }
        }

        lines.add("");
        lines.add(C.cGray + "Scan radius: 150");

        updateLines(board, lines);
    }

    private void updateLines(ScoreboardSidebar board, List<String> lines) {
        for (int i = 0; i < lines.size() && i < ScoreboardSidebar.MAX_LINES; i++) {
            board.line(i, LegacyComponentSerializer.legacySection().deserialize(lines.get(i)));
        }
        for (int i = lines.size(); i < ScoreboardSidebar.MAX_LINES; i++) {
            board.line(i, Component.empty());
        }
    }

    private String formatLine(String label, int found, int required) {
        boolean ok = found >= required;
        String color = ok ? C.cGreen : C.cRed;
        String icon = ok ? "✔" : "✘";
        return color + icon + " " + C.cWhite + label + ": " + color + found + "/" + required;
    }

    public void cleanup() {
        for (ScoreboardSidebar board : boards.values()) {
            board.close();
        }
        boards.clear();
    }
}
