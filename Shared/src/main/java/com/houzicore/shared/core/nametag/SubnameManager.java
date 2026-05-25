package com.houzicore.shared.core.nametag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class SubnameManager extends MiniPlugin {

    private CoreClientManager _clientManager;
    private java.util.function.Function<Player, Component> _subnameProvider = null;

    public void setSubnameProvider(java.util.function.Function<Player, Component> provider) {
        this._subnameProvider = provider;
    }

    public SubnameManager(org.bukkit.plugin.java.JavaPlugin plugin, CoreClientManager clientManager) {
        super("Subname Manager", plugin);
        _clientManager = clientManager;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = player.getScoreboard();
            if (sb == null) continue;

            Objective obj = sb.getObjective("subname_display");
            
            // Re-create gracefully if missing or misconfigured
            if (obj == null) {
                obj = sb.registerNewObjective("subname_display", "dummy", Component.empty());
                obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
            }
            
            // Sync all online players' badges onto this player's scoreboard view
            for (Player target : Bukkit.getOnlinePlayers()) {
                CoreClient targetClient = _clientManager.Get(target);
                if (targetClient == null || targetClient.GetRank() == null) continue;

                Score score = obj.getScore(target.getName());
                
                Component badgeComponent;
                if (_subnameProvider != null) {
                    badgeComponent = _subnameProvider.apply(target);
                } else {
                    // Scoreboard/below-name context: plain rank text only, no ObjectContents
                    badgeComponent = com.houzicore.shared.core.chat.Chat.buildPlainRankPrefix(targetClient.GetRank().name());
                    if (badgeComponent.equals(Component.empty())) {
                        String rawTag = targetClient.GetRank().GetTag(false, false).replace("&", "§");
                        badgeComponent = LegacyComponentSerializer.legacySection().deserialize(rawTag);
                    }
                }
                
                try {
                	// Paper 1.21.1: Replace the integer score completely with the badge text
                	score.numberFormat(NumberFormat.fixed(badgeComponent));
                	
                	// Set name to empty so ONLY the number format (badge format) is displayed
                	score.customName(Component.empty());
                } catch (Exception ex) {
                	// Fallback for older versions or if method name changed
                }
                
                score.setScore(0);
            }
            
            // Clean up offline players from this scoreboard memory
            for (String entry : sb.getEntries()) {
            	if (Bukkit.getPlayerExact(entry) == null) {
            		sb.resetScores(entry);
            	}
            }
        }
    }
}
