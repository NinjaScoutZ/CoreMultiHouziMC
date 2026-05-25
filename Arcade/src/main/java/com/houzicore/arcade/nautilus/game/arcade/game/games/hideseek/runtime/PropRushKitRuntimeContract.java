package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public final class PropRushKitRuntimeContract
{
    private final String kitKey;
    private final Role role;
    private final PropRushAbilityDefinition activeAbility;
    private final Material primaryWeapon;
    private final ArmorProfile armorProfile;
    private final List<SlottedItem> supplementalItems;
    private final List<PotionSpec> persistentEffects;
    private final List<String> passivePerkKeys;

    private PropRushKitRuntimeContract(Builder builder)
    {
        this.kitKey = builder.kitKey;
        this.role = builder.role;
        this.activeAbility = builder.activeAbility;
        this.primaryWeapon = builder.primaryWeapon;
        this.armorProfile = builder.armorProfile;
        this.supplementalItems = Collections.unmodifiableList(new ArrayList<SlottedItem>(builder.supplementalItems));
        this.persistentEffects = Collections.unmodifiableList(new ArrayList<PotionSpec>(builder.persistentEffects));
        this.passivePerkKeys = Collections.unmodifiableList(new ArrayList<String>(builder.passivePerkKeys));
    }

    public static Builder builder(String kitKey, Role role)
    {
        return new Builder(kitKey, role);
    }

    public String getKitKey()
    {
        return kitKey;
    }

    public Role getRole()
    {
        return role;
    }

    public PropRushAbilityDefinition getActiveAbility()
    {
        return activeAbility;
    }

    public Material getPrimaryWeapon()
    {
        return primaryWeapon;
    }

    public ArmorProfile getArmorProfile()
    {
        return armorProfile;
    }

    public List<SlottedItem> getSupplementalItems()
    {
        return supplementalItems;
    }

    public List<PotionSpec> getPersistentEffects()
    {
        return persistentEffects;
    }

    public List<String> getPassivePerkKeys()
    {
        return passivePerkKeys;
    }

    public enum Role
    {
        HIDER,
        HUNTER
    }

    public enum ArmorProfile
    {
        NONE,
        IRON,
        LEATHER,
        DIAMOND
    }

    public static final class SlottedItem
    {
        private final int slot;
        private final Material material;
        private final int amount;

        private SlottedItem(int slot, Material material, int amount)
        {
            this.slot = slot;
            this.material = material;
            this.amount = amount;
        }

        public int getSlot()
        {
            return slot;
        }

        public Material getMaterial()
        {
            return material;
        }

        public int getAmount()
        {
            return amount;
        }
    }

    public static final class PotionSpec
    {
        private final PotionEffectType type;
        private final int amplifier;

        private PotionSpec(PotionEffectType type, int amplifier)
        {
            this.type = type;
            this.amplifier = amplifier;
        }

        public PotionEffectType getType()
        {
            return type;
        }

        public int getAmplifier()
        {
            return amplifier;
        }
    }

    public static final class Builder
    {
        private final String kitKey;
        private final Role role;
        private PropRushAbilityDefinition activeAbility;
        private Material primaryWeapon;
        private ArmorProfile armorProfile = ArmorProfile.NONE;
        private final List<SlottedItem> supplementalItems = new ArrayList<SlottedItem>();
        private final List<PotionSpec> persistentEffects = new ArrayList<PotionSpec>();
        private final List<String> passivePerkKeys = new ArrayList<String>();

        private Builder(String kitKey, Role role)
        {
            this.kitKey = kitKey;
            this.role = role;
        }

        public Builder activeAbility(PropRushAbilityDefinition activeAbility)
        {
            this.activeAbility = activeAbility;
            return this;
        }

        public Builder primaryWeapon(Material primaryWeapon)
        {
            this.primaryWeapon = primaryWeapon;
            return this;
        }

        public Builder armorProfile(ArmorProfile armorProfile)
        {
            this.armorProfile = armorProfile;
            return this;
        }

        public Builder addSupplementalItem(int slot, Material material, int amount)
        {
            this.supplementalItems.add(new SlottedItem(slot, material, amount));
            return this;
        }

        public Builder addPersistentEffect(PotionEffectType type, int amplifier)
        {
            this.persistentEffects.add(new PotionSpec(type, amplifier));
            return this;
        }

        public Builder passivePerkKeys(String... passivePerkKeys)
        {
            if (passivePerkKeys == null)
            {
                return this;
            }

            for (String passivePerkKey : passivePerkKeys)
            {
                if (passivePerkKey != null && !passivePerkKey.trim().isEmpty())
                {
                    this.passivePerkKeys.add(passivePerkKey);
                }
            }
            return this;
        }

        public PropRushKitRuntimeContract build()
        {
            return new PropRushKitRuntimeContract(this);
        }
    }
}
