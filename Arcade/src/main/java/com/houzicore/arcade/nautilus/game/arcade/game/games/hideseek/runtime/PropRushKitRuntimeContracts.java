package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public final class PropRushKitRuntimeContracts
{
    private static final Map<String, PropRushKitRuntimeContract> BY_KIT_KEY;
    private static final List<String> DISABLED_KIT_KEYS;

    static
    {
        Map<String, PropRushKitRuntimeContract> byKitKey = new LinkedHashMap<String, PropRushKitRuntimeContract>();

        register(byKitKey, PropRushKitRuntimeContract.builder("chameleon", PropRushKitRuntimeContract.Role.HIDER)
                .activeAbility(PropRushAbilityDefinition.DECOY)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("ghost", PropRushKitRuntimeContract.Role.HIDER)
                .activeAbility(PropRushAbilityDefinition.PHASE_SHIFT)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("bomb_bug", PropRushKitRuntimeContract.Role.HIDER)
                .activeAbility(PropRushAbilityDefinition.BOMB_SHELL)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("locksmith", PropRushKitRuntimeContract.Role.HIDER)
                .activeAbility(PropRushAbilityDefinition.SECRET_PASSAGE)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("mimic", PropRushKitRuntimeContract.Role.HIDER)
                .activeAbility(PropRushAbilityDefinition.MIRROR_IMAGE)
                .build());

        register(byKitKey, PropRushKitRuntimeContract.builder("tracker", PropRushKitRuntimeContract.Role.HUNTER)
                .primaryWeapon(Material.IRON_SWORD)
                .armorProfile(PropRushKitRuntimeContract.ArmorProfile.IRON)
                .activeAbility(PropRushAbilityDefinition.SCANNER_PULSE)
                .passivePerkKeys("double_jump")
                .addPersistentEffect(PotionEffectType.JUMP_BOOST, 0)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("destroyer", PropRushKitRuntimeContract.Role.HUNTER)
                .primaryWeapon(Material.IRON_AXE)
                .armorProfile(PropRushKitRuntimeContract.ArmorProfile.IRON)
                .activeAbility(PropRushAbilityDefinition.FLARE)
                .addPersistentEffect(PotionEffectType.SLOWNESS, 0)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("trapper", PropRushKitRuntimeContract.Role.HUNTER)
                .primaryWeapon(Material.IRON_SWORD)
                .armorProfile(PropRushKitRuntimeContract.ArmorProfile.LEATHER)
                .passivePerkKeys("speed_ii")
                .addPersistentEffect(PotionEffectType.SPEED, 1)
                .addSupplementalItem(1, Material.BOW, 1)
                .addSupplementalItem(2, Material.COBWEB, 3)
                .addSupplementalItem(9, Material.ARROW, 10)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("bloodhound", PropRushKitRuntimeContract.Role.HUNTER)
                .primaryWeapon(Material.IRON_SWORD)
                .armorProfile(PropRushKitRuntimeContract.ArmorProfile.IRON)
                .activeAbility(PropRushAbilityDefinition.BLOODHOUND_SENSE)
                .addPersistentEffect(PotionEffectType.SPEED, 0)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("saboteur", PropRushKitRuntimeContract.Role.HUNTER)
                .primaryWeapon(Material.IRON_SWORD)
                .armorProfile(PropRushKitRuntimeContract.ArmorProfile.IRON)
                .activeAbility(PropRushAbilityDefinition.SMOKE_BOMB)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("bounty_hunter", PropRushKitRuntimeContract.Role.HUNTER)
                .primaryWeapon(Material.IRON_AXE)
                .armorProfile(PropRushKitRuntimeContract.ArmorProfile.DIAMOND)
                .activeAbility(PropRushAbilityDefinition.BOUNTY_DASH)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("exorcist", PropRushKitRuntimeContract.Role.HUNTER)
                .primaryWeapon(Material.IRON_SWORD)
                .armorProfile(PropRushKitRuntimeContract.ArmorProfile.IRON)
                .activeAbility(PropRushAbilityDefinition.PURGE_PULSE)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("falconer", PropRushKitRuntimeContract.Role.HUNTER)
                .primaryWeapon(Material.IRON_SWORD)
                .armorProfile(PropRushKitRuntimeContract.ArmorProfile.IRON)
                .activeAbility(PropRushAbilityDefinition.SKY_SWEEP)
                .build());
        register(byKitKey, PropRushKitRuntimeContract.builder("warden", PropRushKitRuntimeContract.Role.HUNTER)
                .primaryWeapon(Material.IRON_SWORD)
                .armorProfile(PropRushKitRuntimeContract.ArmorProfile.IRON)
                .activeAbility(PropRushAbilityDefinition.ECHO_SENTRY)
                .build());

        BY_KIT_KEY = Collections.unmodifiableMap(byKitKey);
        DISABLED_KIT_KEYS = Collections.unmodifiableList(List.of("trickster"));
    }

    private PropRushKitRuntimeContracts()
    {
    }

    public static PropRushKitRuntimeContract resolve(Kit kit)
    {
        if (kit == null || kit.getLanguageKey() == null)
        {
            return null;
        }

        return BY_KIT_KEY.get(kit.getLanguageKey());
    }

    public static boolean hasLiveContract(Kit kit)
    {
        return resolve(kit) != null;
    }

    public static boolean hasKitKey(Kit kit, String kitKey)
    {
        return kit != null && kitKey != null && kitKey.equals(kit.getLanguageKey());
    }

    public static boolean isHiderKit(Kit kit)
    {
        PropRushKitRuntimeContract contract = resolve(kit);
        return contract != null && contract.getRole() == PropRushKitRuntimeContract.Role.HIDER;
    }

    public static boolean isHunterKit(Kit kit)
    {
        PropRushKitRuntimeContract contract = resolve(kit);
        return contract != null && contract.getRole() == PropRushKitRuntimeContract.Role.HUNTER;
    }

    public static boolean sameKit(Kit first, Kit second)
    {
        if (first == second)
        {
            return true;
        }

        if (first == null || second == null)
        {
            return false;
        }

        if (first.getLanguageKey() != null && second.getLanguageKey() != null)
        {
            return first.getLanguageKey().equals(second.getLanguageKey());
        }

        return first.GetName().equalsIgnoreCase(second.GetName());
    }

    public static Collection<PropRushKitRuntimeContract> all()
    {
        return BY_KIT_KEY.values();
    }

    public static Set<String> liveKitKeys()
    {
        return new LinkedHashSet<String>(BY_KIT_KEY.keySet());
    }

    public static Set<String> livePerkKeys()
    {
        Set<String> perkKeys = new LinkedHashSet<String>();

        for (PropRushKitRuntimeContract contract : BY_KIT_KEY.values())
        {
            if (contract.getActiveAbility() != null && contract.getActiveAbility().isPerkBacked())
            {
                perkKeys.add(contract.getActiveAbility().getKey());
            }
            perkKeys.addAll(contract.getPassivePerkKeys());
        }

        return perkKeys;
    }

    public static List<String> disabledKitKeys()
    {
        return DISABLED_KIT_KEYS;
    }

    public static List<String> disabledPerkKeys()
    {
        return Collections.unmodifiableList(List.of("trickery", "blinding_strike"));
    }

    public static List<PropRushAbilityDefinition> sharedHiderAbilities()
    {
        List<PropRushAbilityDefinition> sharedAbilities = new ArrayList<PropRushAbilityDefinition>();
        sharedAbilities.add(PropRushAbilityDefinition.FAKE_SOUND_PING);
        sharedAbilities.add(PropRushAbilityDefinition.PROP_SWAP);
        sharedAbilities.add(PropRushAbilityDefinition.CAT_TAUNT);
        sharedAbilities.add(PropRushAbilityDefinition.SIGNAL_FLARE);
        sharedAbilities.add(PropRushAbilityDefinition.HIDER_SNOWBALL);
        sharedAbilities.add(PropRushAbilityDefinition.SIXTH_SENSE);
        sharedAbilities.add(PropRushAbilityDefinition.DASH);
        return sharedAbilities;
    }

    private static void register(Map<String, PropRushKitRuntimeContract> byKitKey, PropRushKitRuntimeContract contract)
    {
        byKitKey.put(contract.getKitKey(), contract);
    }
}
