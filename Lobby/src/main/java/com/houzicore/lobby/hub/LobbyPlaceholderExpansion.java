package com.houzicore.lobby.hub;

import org.bukkit.entity.Player;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

public class LobbyPlaceholderExpansion extends PlaceholderExpansion {

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
        return "";
    }
}
