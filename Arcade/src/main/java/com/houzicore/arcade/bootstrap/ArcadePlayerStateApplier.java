package com.houzicore.arcade.bootstrap;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

import com.houzicore.shared.api.PlayerStateApplier;
import com.houzicore.shared.api.context.ContextPolicy;
import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.PlayerContextService;
import com.houzicore.shared.api.loadout.LoadoutProfile;
import com.houzicore.shared.api.loadout.LoadoutService;
import com.houzicore.shared.api.loadout.SharedLoadoutProfiles;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

public class ArcadePlayerStateApplier implements PlayerStateApplier {

    private final PlayerContextService contextService;
    private final ContextPolicyRegistry policyRegistry;
    private final LoadoutService loadoutService;

    public ArcadePlayerStateApplier(PlayerContextService contextService, ContextPolicyRegistry policyRegistry, LoadoutService loadoutService) {
        this.contextService = contextService;
        this.policyRegistry = policyRegistry;
        this.loadoutService = loadoutService;
    }

    @Override
    public void applyContextState(Player player, PlayerContextId contextId) {
        ContextPolicy policy = policyRegistry.find(contextId).orElse(null);
        if (policy == null) return;

        LoadoutProfile profile = policy.loadoutProfile();

        if (contextId == PlayerContextId.ARCADE_LIVE) {
            applyArcadeLive(player);
            return;
        }

        if (contextId == PlayerContextId.ARCADE_DEAD) {
            applyArcadeDead(player);
            return;
        }

        cleanState(player);

        if (!profile.equals(SharedLoadoutProfiles.EMPTY_LOADOUT)) {
            loadoutService.apply(player, profile);
        }

        if (contextId == PlayerContextId.ARCADE_LOBBY || profile.equals(SharedLoadoutProfiles.ARCADE_LOBBY)) {
            applyArcadeLobbyFlags(player);
        } else if (contextId == PlayerContextId.ARCADE_SPECTATOR || profile.equals(SharedLoadoutProfiles.ARCADE_SPECTATOR)) {
            applyArcadeSpectatorFlags(player);
        } else if (contextId == PlayerContextId.ARCADE_POSTGAME || profile.equals(SharedLoadoutProfiles.ARCADE_POSTGAME)) {
            applyArcadePostgameFlags(player);
        }

        UtilInv.Update(player);
    }

    @Override
    public void refreshState(Player player) {
        PlayerContextId contextId = contextService.getCurrentContextId(player);
        applyContextState(player, contextId);
    }

    @Override
    public void cleanState(Player player) {
        if (player.isDead()) {
            return;
        }

        loadoutService.clear(player);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFlySpeed(0.1F);
        player.setInvisible(false);
        player.setCollidable(true);
        player.removeMetadata("spectator", org.bukkit.plugin.java.JavaPlugin.getPlugin(com.houzicore.arcade.Arcade.class));
        player.setLevel(0);
        player.setExp(0f);
        player.setSprinting(false);
        player.setSneaking(false);
        player.setSaturation(3f);
        player.setExhaustion(0f);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.eject();
        player.leaveVehicle();
        player.setArrowsInBody(0);
        
        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void applyRestoredArcadeLobbyState(Player player) {
        if (player.isDead()) {
            return;
        }

        applyArcadeLobbyFlags(player);
        UtilInv.Update(player);
    }

    /**
     * Applies the ARCADE_LIVE state to a player.
     * Note: This method intentionally BYPASSES cleanState(player) and does not touch inventory.
     * During ARCADE_LIVE, game/kit logic is the sole authority for inventory and custom states.
     * We only enforce basic combat-readiness flags here.
     */
    private void applyArcadeLive(Player player) {
        if (player.isDead()) {
            return;
        }

        // Preserve the live loadout/game state that Prepare or game logic already established.
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setInvisible(false);
        player.setCollidable(true);
        player.setFireTicks(0);
        player.setFallDistance(0);
    }

    private void applyArcadeDead(Player player) {
        if (player.isDead()) {
            return;
        }

        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFireTicks(0);
    }

    private void applyArcadeLobbyFlags(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(false);
        player.getInventory().setHeldItemSlot(0);
    }

    private void applyArcadeSpectatorFlags(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvisible(true);
        player.setCollidable(false);
        player.getInventory().setHeldItemSlot(0);
        player.setMetadata("spectator", new org.bukkit.metadata.FixedMetadataValue(org.bukkit.plugin.java.JavaPlugin.getPlugin(com.houzicore.arcade.Arcade.class), true));
    }

    private void applyArcadePostgameFlags(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(false);
        player.getInventory().setHeldItemSlot(0);
    }

    public List<ItemStack> buildArcadeLobbyItems(Player player) {
        ItemStack profileHead = new ItemBuilder(Material.PLAYER_HEAD)
                .setTitle(HouziColorParser.parse("&bProfile"))
                .setPlayerHead(player.getName())
                .build();

        ItemStack hubClock = ItemStackFactory.Instance.CreateStack(Material.CLOCK, (byte) 0, 1,
                HouziColorParser.parse("&aReturn to Hub"),
                new String[]{
                        "",
                        org.bukkit.ChatColor.RESET + "Click while holding this",
                        org.bukkit.ChatColor.RESET + "to return to the Hub."
                });

        return Arrays.asList(profileHead, null, null, null, null, null, null, null, hubClock);
    }

    public List<ItemStack> buildArcadeSpectatorItems(Player player) {
        return Arrays.asList(
                new ItemBuilder(Material.COMPASS)
                        .setTitle(HouziColorParser.parse("&aTeleporter"))
                        .build(),
                null, null, null, null, null, null, null,
                new ItemBuilder(Material.RED_BED)
                        .setTitle(HouziColorParser.parse("&cReturn to Hub"))
                        .build()
        );
    }

    public List<ItemStack> buildArcadePostgameItems(Player player) {
        return Arrays.asList(
                new ItemBuilder(Material.PAPER)
                        .setTitle(HouziColorParser.parse("&6Match Summary"))
                        .build(),
                null, null, null, null, null, null, null,
                ItemStackFactory.Instance.CreateStack(Material.CLOCK, (byte) 0, 1,
                        HouziColorParser.parse("&aReturn to Hub"),
                        new String[]{
                                "",
                                org.bukkit.ChatColor.RESET + "Click while holding this",
                                org.bukkit.ChatColor.RESET + "to return to the Hub."
                        })
        );
    }
}
