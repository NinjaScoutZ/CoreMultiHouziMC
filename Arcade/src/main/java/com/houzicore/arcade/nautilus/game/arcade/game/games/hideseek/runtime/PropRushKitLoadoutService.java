package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public final class PropRushKitLoadoutService
{
    private static final Set<PotionEffectType> MANAGED_EFFECTS = new HashSet<PotionEffectType>(
            Arrays.asList(PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.JUMP_BOOST));

    private PropRushKitLoadoutService()
    {
    }

    public static PropRushKitRuntimeContract resolveContract(Kit kit)
    {
        return PropRushKitRuntimeContracts.resolve(kit);
    }

    public static boolean hasLiveContract(Kit kit)
    {
        return PropRushKitRuntimeContracts.hasLiveContract(kit);
    }

    public static boolean isHiderKit(Kit kit)
    {
        return PropRushKitRuntimeContracts.isHiderKit(kit);
    }

    public static boolean isHunterKit(Kit kit)
    {
        return PropRushKitRuntimeContracts.isHunterKit(kit);
    }

    public static boolean sameKit(Kit first, Kit second)
    {
        return PropRushKitRuntimeContracts.sameKit(first, second);
    }

    public static boolean hasKitKey(Kit kit, String kitKey)
    {
        return PropRushKitRuntimeContracts.hasKitKey(kit, kitKey);
    }

    public static void applyHiderLoadout(Player player, Kit kit)
    {
        clearManagedPotionEffects(player);

        if (player == null)
        {
            return;
        }

        for (PropRushAbilityDefinition sharedAbility : PropRushKitRuntimeContracts.sharedHiderAbilities())
        {
            player.getInventory().setItem(sharedAbility.getSlot(), sharedAbility.createItem(player));
        }

        PropRushKitRuntimeContract contract = resolveContract(kit);
        if (contract != null && contract.getActiveAbility() != null)
        {
            player.getInventory().setItem(contract.getActiveAbility().getSlot(), contract.getActiveAbility().createItem(player));
        }

        applyPersistentEffects(player, contract);
    }

    public static void applyHunterLoadout(Player player, Kit kit)
    {
        clearManagedPotionEffects(player);

        if (player == null)
        {
            return;
        }

        PropRushKitRuntimeContract contract = resolveContract(kit);
        if (contract == null)
        {
            return;
        }

        if (contract.getPrimaryWeapon() != null)
        {
            player.getInventory().setItem(0, new ItemStack(contract.getPrimaryWeapon()));
        }

        if (contract.getActiveAbility() != null)
        {
            player.getInventory().setItem(contract.getActiveAbility().getSlot(), contract.getActiveAbility().createItem(player));
        }

        for (PropRushKitRuntimeContract.SlottedItem slottedItem : contract.getSupplementalItems())
        {
            player.getInventory().setItem(slottedItem.getSlot(),
                    new ItemStack(slottedItem.getMaterial(), slottedItem.getAmount()));
        }

        player.getInventory().setItem(PropRushAbilityDefinition.TRACKER_COMPASS.getSlot(), PropRushAbilityDefinition.TRACKER_COMPASS.createItem(player));

        applyArmor(player, contract.getArmorProfile());
        applyPersistentEffects(player, contract);
    }

    private static void applyArmor(Player player, PropRushKitRuntimeContract.ArmorProfile armorProfile)
    {
        if (armorProfile == null || armorProfile == PropRushKitRuntimeContract.ArmorProfile.NONE)
        {
            return;
        }

        if (armorProfile == PropRushKitRuntimeContract.ArmorProfile.LEATHER)
        {
            player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
            return;
        }

        if (armorProfile == PropRushKitRuntimeContract.ArmorProfile.DIAMOND)
        {
            player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            return;
        }

        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
    }

    private static void applyPersistentEffects(Player player, PropRushKitRuntimeContract contract)
    {
        if (player == null || contract == null)
        {
            return;
        }

        for (PropRushKitRuntimeContract.PotionSpec spec : contract.getPersistentEffects())
        {
            player.addPotionEffect(new PotionEffect(spec.getType(), Integer.MAX_VALUE, spec.getAmplifier()));
        }
    }

    private static void clearManagedPotionEffects(Player player)
    {
        if (player == null)
        {
            return;
        }

        for (PotionEffectType effectType : MANAGED_EFFECTS)
        {
            player.removePotionEffect(effectType);
        }
    }
}
