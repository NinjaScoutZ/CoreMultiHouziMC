package com.houzicore.shared.core.npc.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;

/**
 * Represents a sequence of lines an NPC will say.
 */
public class DialogueSet {

    private final String id;
    private final List<String> lines = new ArrayList<>();
    private final long delayTicksBetweenLines;
    
    public DialogueSet(String id, long delayTicksBetweenLines, String... lines) {
        this.id = id;
        this.delayTicksBetweenLines = delayTicksBetweenLines;
        this.lines.addAll(Arrays.asList(lines));
    }

    public String getId() {
        return id;
    }

    /**
     * Plays the dialogue sequentially to the player.
     * @param plugin The plugin to schedule tasks on
     * @param player The player to send messages to
     * @return A future that completes when the dialogue is fully finished.
     */
    public CompletableFuture<Void> play(Plugin plugin, Player player, String npcName) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        playLine(plugin, player, npcName, 0, future);
        return future;
    }

    private void playLine(Plugin plugin, Player player, String npcName, int lineIndex, CompletableFuture<Void> future) {
        if (!player.isOnline()) {
            future.completeExceptionally(new IllegalStateException("Player logged out during dialogue"));
            return;
        }

        if (lineIndex >= lines.size()) {
            future.complete(null);
            return;
        }

        // Play the line
        String line = lines.get(lineIndex);
        UtilPlayer.message(player, "§e[NPC] §6" + npcName + "§f: " + line);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);

        // Schedule next line
        if (lineIndex == lines.size() - 1) {
            // Last line, complete immediately or add a final delay? 
            // We'll complete immediately after the last line is said.
            future.complete(null);
        } else {
            UtilServer.getServer().getScheduler().runTaskLater(plugin, () -> {
                playLine(plugin, player, npcName, lineIndex + 1, future);
            }, delayTicksBetweenLines);
        }
    }
}
