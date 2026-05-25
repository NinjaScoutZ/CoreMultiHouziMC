package com.houzicore.arcade;

import org.bukkit.entity.Player;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import org.jetbrains.annotations.NotNull;

public class ArcadePlaceholderExpansion extends PlaceholderExpansion {

    private final ArcadeManager manager;

    public ArcadePlaceholderExpansion(ArcadeManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return com.houzicore.shared.core.common.BrandConfig.mainServerName();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "arcade";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null || manager == null) {
            return "";
        }
        
        Game game = manager.GetGame();
        if (game == null) {
            return "";
        }

        if (identifier.equalsIgnoreCase("game_name")) {
            return game.GetName();
        }

        if (identifier.equalsIgnoreCase("team_color")) {
            GameTeam team = game.GetTeam(player);
            if (team != null) {
                return team.GetColor().toString();
            }
            return "";
        }
        
        if (identifier.equalsIgnoreCase("team_name")) {
            GameTeam team = game.GetTeam(player);
            if (team != null) {
                return team.GetName();
            }
            return "";
        }

        if (identifier.equalsIgnoreCase("rank_name")) {
            com.houzicore.shared.account.CoreClient client = manager.GetClients().Get(player);
            if (client != null && client.GetRank() != null && client.GetRank() != com.houzicore.shared.common.Rank.ALL) {
                return client.GetRank().Name + " ";
            }
            return "";
        }

        if (identifier.equalsIgnoreCase("rank_color")) {
            com.houzicore.shared.account.CoreClient client = manager.GetClients().Get(player);
            if (client != null && client.GetRank() != null) {
                String color = client.GetRank().GetColor().toString();
                // Transform native Bukkit ChatColor to MiniMessage hex equivalent for HouziExtension gradient mapping
                switch (client.GetRank().GetColor()) {
                    case RED: return "<#FF5555>";
                    case DARK_RED: return "<#AA0000>";
                    case GOLD: return "<#FFAA00>";
                    case YELLOW: return "<#FFFF55>";
                    case GREEN: return "<#55FF55>";
                    case DARK_GREEN: return "<#00AA00>";
                    case AQUA: return "<#55FFFF>";
                    case DARK_AQUA: return "<#00AAAA>";
                    case BLUE: return "<#5555FF>";
                    case DARK_BLUE: return "<#0000AA>";
                    case LIGHT_PURPLE: return "<#FF55FF>";
                    case DARK_PURPLE: return "<#AA00AA>";
                    case WHITE: return "<#FFFFFF>";
                    case GRAY: return "<#AAAAAA>";
                    case DARK_GRAY: return "<#555555>";
                    case BLACK: return "<#000000>";
                    default: return color;
                }
            }
            return "";
        }

        if (identifier.equalsIgnoreCase("kit_name")) {
            Kit kit = game.GetKit(player);
            if (kit != null) {
                return kit.GetName();
            }
            return "";
        }

        return null;
    }
}
