package com.houzicore.lobby.hub.bootstrap;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.houzicore.shared.api.PlayerStateApplier;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.PlayerContextService;
import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.ContextPolicy;
import com.houzicore.shared.api.loadout.LoadoutProfile;
import com.houzicore.shared.api.loadout.SharedLoadoutProfiles;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.api.loadout.LoadoutService;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.lang.LangManager;

import java.util.List;

public class LobbyPlayerStateApplier implements PlayerStateApplier {

    private final PlayerContextService contextService;
    private final ContextPolicyRegistry policyRegistry;
    private final LoadoutService loadoutService;

    public LobbyPlayerStateApplier(PlayerContextService contextService, ContextPolicyRegistry policyRegistry, LoadoutService loadoutService) {
        this.contextService = contextService;
        this.policyRegistry = policyRegistry;
        this.loadoutService = loadoutService;
    }

    @Override
    public void applyContextState(Player player, PlayerContextId contextId) {
        cleanState(player);

        ContextPolicy policy = policyRegistry.find(contextId).orElse(null);
        if (policy == null) return;

        LoadoutProfile profile = policy.loadoutProfile();

        // Unconditionally route inventory to LoadoutService (replaces manual logic)
        loadoutService.apply(player, profile);

        // Handle context-specific ad-hoc attributes not governed by inventory
        if (profile.equals(SharedLoadoutProfiles.LOBBY_MAIN)) {
            player.setAllowFlight(true);
            player.getInventory().setHeldItemSlot(0);
        } else if (profile.equals(SharedLoadoutProfiles.LOBBY_ARENA_DUEL)
            || profile.equals(SharedLoadoutProfiles.LOBBY_FISHING)
            || profile.equals(SharedLoadoutProfiles.LOBBY_FARM)
            || profile.equals(SharedLoadoutProfiles.LOBBY_PARKOUR)
            || profile.equals(SharedLoadoutProfiles.LOBBY_ACTIVITY)) {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.getInventory().setHeldItemSlot(0);
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
        loadoutService.clear(player);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFallDistance(0f);
        player.setFireTicks(0);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);
        player.setGameMode(GameMode.SURVIVAL);
        player.setExp(0f);
    }

    public List<ItemStack> buildLobbyMainItems(Player player) {
        // Slot 0: Game Compass
        ItemStack gameMenu = ItemStackFactory.Instance.CreateStack(Material.COMPASS, (byte)0, 1,
            HouziColorParser.parse(LangManager.get().get(player, "hub.item.game_menu")),
            new String[]{
                HouziColorParser.parse(LangManager.get().get(player, "hub.item.game_menu.lore1")),
                HouziColorParser.parse(LangManager.get().get(player, "hub.item.game_menu.lore2"))
            });

        // Slot 1: Profile Head
        ItemStack profileHead = new ItemBuilder(Material.PLAYER_HEAD)
            .setTitle(HouziColorParser.parse(LangManager.get().get(player, "hub.item.profile")))
            .setPlayerHead(player.getName())
            .build();

        // Slot 2: Cosmetics Chest
        ItemStack cosmeticChest = ItemStackFactory.Instance.CreateStack(Material.CHEST, (byte)0, 1,
            HouziColorParser.parse(LangManager.get().get(player, "hub.item.cosmetic")),
            new String[]{
                HouziColorParser.parse(LangManager.get().get(player, "hub.item.cosmetic.lore1")),
                HouziColorParser.parse(LangManager.get().get(player, "hub.item.cosmetic.lore2"))
            });

        // Slot 7: Preferences
        ItemStack prefsItem = ItemStackFactory.Instance.CreateStack(Material.COMPARATOR, (byte)0, 1,
            HouziColorParser.parse(LangManager.get().get(player, "hub.item.prefs")),
            new String[]{
                HouziColorParser.parse(LangManager.get().get(player, "hub.item.prefs.lore1")),
                HouziColorParser.parse(LangManager.get().get(player, "hub.item.prefs.lore2"))
            });

        // Slot 8: Lobby Clock
        ItemStack lobbyClock = ItemStackFactory.Instance.CreateStack(Material.CLOCK, (byte)0, 1,
            HouziColorParser.parse(LangManager.get().get(player, "hub.item.lobby_menu")),
            new String[]{
                HouziColorParser.parse(LangManager.get().get(player, "hub.item.lobby_menu.lore1")),
                HouziColorParser.parse(LangManager.get().get(player, "hub.item.lobby_menu.lore2"))
            });

        // Sparse list: list index = hotbar slot. Slots 3–6 are empty (null).
        List<ItemStack> items = new java.util.ArrayList<>(java.util.Collections.nCopies(9, null));
        items.set(0, gameMenu);
        items.set(1, profileHead);
        items.set(2, cosmeticChest);
        items.set(7, prefsItem);
        items.set(8, lobbyClock);
        return items;
    }

    public List<ItemStack> buildArenaDuelItems(Player player) {
        List<ItemStack> items = new java.util.ArrayList<>(java.util.Collections.nCopies(36, null));

        items.set(0, new ItemBuilder(Material.DIAMOND_SWORD).setUnbreakable(true).addEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 2).build());
        items.set(1, new ItemBuilder(Material.BOW).setUnbreakable(true).addEnchantment(org.bukkit.enchantments.Enchantment.POWER, 2).build());
        items.set(2, new ItemBuilder(Material.FISHING_ROD).setUnbreakable(true).build());
        items.set(3, new ItemStack(Material.GOLDEN_APPLE, 6));
        items.set(4, new ItemBuilder(Material.DIAMOND_AXE).setUnbreakable(true).build());
        items.set(5, new ItemStack(Material.OAK_PLANKS, 64));
        items.set(6, new ItemStack(Material.ARROW, 16));

        items.set(7, new ItemBuilder(Material.IRON_BOOTS).setUnbreakable(true).addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 2).build());
        items.set(8, new ItemBuilder(Material.DIAMOND_LEGGINGS).setUnbreakable(true).addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 2).build());
        items.set(9, new ItemBuilder(Material.DIAMOND_CHESTPLATE).setUnbreakable(true).addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 2).build());
        items.set(10, new ItemBuilder(Material.DIAMOND_HELMET).setUnbreakable(true).addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 2).build());

        return items;
    }

    public List<ItemStack> buildFishingItems(Player player) {
        ItemStack rod = new ItemBuilder(Material.FISHING_ROD)
            .setTitle(C.cAqua + C.Bold + "🎣 Fishing Rod")
            .addLore(C.cGray + "ใช้เฉพาะในโซนตกปลา")
            .setUnbreakable(true)
            .build();

        ItemStack cosmetics = ItemStackFactory.Instance.CreateStack(Material.CHEST, (byte) 0, 1,
            org.bukkit.ChatColor.RESET + "" + C.cGreen + LangManager.get().get(player, "cosmetic.title"));

        List<ItemStack> items = new java.util.ArrayList<>(java.util.Collections.nCopies(8, null));
        items.set(0, rod);
        items.set(4, cosmetics);
        return items;
    }

    public List<ItemStack> buildFarmItems(Player player) {
        ItemStack hoe = new ItemBuilder(Material.IRON_HOE)
            .setTitle(C.cGreen + C.Bold + "🌾 Farm Hoe")
            .addLore(C.cGray + "ใช้เก็บพืชใน Farm Sim")
            .setUnbreakable(true)
            .build();

        ItemStack axe = new ItemBuilder(Material.IRON_AXE)
            .setTitle(C.cGold + C.Bold + "🪓 Farm Axe")
            .addLore(C.cGray + "ใช้ตัดวัตถุดิบใน Farm Sim")
            .setUnbreakable(true)
            .build();

        List<ItemStack> items = new java.util.ArrayList<>(java.util.Collections.nCopies(8, null));
        items.set(0, hoe);
        items.set(1, axe);
        return items;
    }
}
