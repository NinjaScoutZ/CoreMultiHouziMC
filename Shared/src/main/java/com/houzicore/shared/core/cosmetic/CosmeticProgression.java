package com.houzicore.shared.core.cosmetic;

import java.util.Comparator;
import java.util.Locale;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.mount.Mount;

public final class CosmeticProgression {
    public static final int COMMON_PRICE = 5000;
    public static final int RARE_PRICE = 10000;
    public static final int EPIC_PRICE = 25000;
    public static final int LEGENDS_PRICE = 50000;
    public static final int MYTHIC_PRICE = 100000;

    private CosmeticProgression() {
    }

    public static Comparator<Gadget> gadgetComparator() {
        return Comparator
                .comparingInt((Gadget gadget) -> getTierOrder(getShopRarity(gadget)))
                .thenComparingInt(gadget -> getPrice(getShopRarity(gadget)))
                .thenComparing(Gadget::GetName, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<Mount<?>> mountComparator() {
        return Comparator
                .comparingInt((Mount<?> mount) -> getTierOrder(getShopRarity(mount)))
                .thenComparingInt(mount -> getPrice(getShopRarity(mount)))
                .thenComparing(Mount::GetName, String.CASE_INSENSITIVE_ORDER);
    }

    public static int getPrice(CosmeticRarity rarity) {
        switch (normalize(rarity)) {
            case MYTHIC:
                return MYTHIC_PRICE;
            case LEGENDARY:
                return LEGENDS_PRICE;
            case EPIC:
                return EPIC_PRICE;
            case RARE:
                return RARE_PRICE;
            case COMMON:
            default:
                return COMMON_PRICE;
        }
    }

    public static CosmeticRarity getPetRarity(String petName) {
        String name = normalizeName(petName);

        if (containsAny(name, "axolotl", "sniffer", "armadillo")) {
            return CosmeticRarity.MYTHIC;
        }

        if (containsAny(name, "allay", "widder", "wither")) {
            return CosmeticRarity.LEGENDARY;
        }

        if (containsAny(name, "camel", "parrot", "panda", "turtle")) {
            return CosmeticRarity.EPIC;
        }

        if (containsAny(name, "fox", "frog", "bee", "rabbit", "cat")) {
            return CosmeticRarity.RARE;
        }

        return CosmeticRarity.COMMON;
    }

    public static CosmeticRarity getShopRarity(Gadget gadget) {
        CosmeticRarity explicit = normalize(gadget.getRarity());
        if (explicit != CosmeticRarity.COMMON) {
            return explicit;
        }

        String name = normalizeName(gadget.GetName());
        GadgetType type = gadget.getGadgetType();

        if (type == GadgetType.Item) {
            if (containsAny(name, "ethereal", "essence bomb", "mob bomb")) return CosmeticRarity.MYTHIC;
            if (containsAny(name, "tnt", "freeze cannon", "dueling sword", "meteor smash")) return CosmeticRarity.LEGENDARY;
            if (containsAny(name, "grappling", "melon launcher", "trampoline", "coin bomb", "magic melody")) return CosmeticRarity.EPIC;
            if (containsAny(name, "snowball", "bat gun", "flesh hook", "firework")) return CosmeticRarity.RARE;
            return CosmeticRarity.COMMON;
        }

        if (type == GadgetType.Particle) {
            if (containsAny(name, "angel", "demon", "frost", "dragon breath", "legend")) return CosmeticRarity.MYTHIC;
            if (containsAny(name, "jetpack", "rainbow", "amethyst", "sculk", "yin yang")) return CosmeticRarity.LEGENDARY;
            if (containsAny(name, "fire rings", "helix", "halo", "fox tail", "wolf tail")) return CosmeticRarity.EPIC;
            if (containsAny(name, "enchant", "heart", "blizzard", "firefly", "music notes", "cherry")) return CosmeticRarity.RARE;
            return CosmeticRarity.COMMON;
        }

        if (type == GadgetType.Morph) {
            if (containsAny(name, "warden")) return CosmeticRarity.MYTHIC;
            if (containsAny(name, "blaze", "wither", "pumpkin king")) return CosmeticRarity.LEGENDARY;
            if (containsAny(name, "bat", "creeper", "enderman")) return CosmeticRarity.EPIC;
            if (containsAny(name, "villager", "block", "fox", "bunny", "parrot")) return CosmeticRarity.RARE;
            return CosmeticRarity.COMMON;
        }

        if (type == GadgetType.WinEffect) {
            if (containsAny(name, "dragon rise", "elder guardian")) return CosmeticRarity.MYTHIC;
            if (containsAny(name, "solar flare", "podium")) return CosmeticRarity.LEGENDARY;
            if (containsAny(name, "earthquake", "lava trap")) return CosmeticRarity.EPIC;
            if (containsAny(name, "lightning", "tornado")) return CosmeticRarity.RARE;
            return CosmeticRarity.COMMON;
        }

        if (type == GadgetType.Spray) {
            if (containsAny(name, "houzi")) return CosmeticRarity.EPIC;
            if (containsAny(name, "ggez")) return CosmeticRarity.RARE;
            return CosmeticRarity.COMMON;
        }

        if (type == GadgetType.KillEffect) {
            if (containsAny(name, "rainbow ring")) return CosmeticRarity.LEGENDARY;
            if (containsAny(name, "lava fountain")) return CosmeticRarity.EPIC;
            if (containsAny(name, "blood burst")) return CosmeticRarity.RARE;
            return CosmeticRarity.COMMON;
        }

        if (type == GadgetType.Aura) {
            if (containsAny(name, "shadow")) return CosmeticRarity.LEGENDARY;
            if (containsAny(name, "crystal")) return CosmeticRarity.EPIC;
            if (containsAny(name, "flame", "cherry")) return CosmeticRarity.RARE;
            return CosmeticRarity.COMMON;
        }

        if (type == GadgetType.Costume) {
            if (containsAny(name, "freeze", "reindeer", "emerald")) return CosmeticRarity.EPIC;
            return CosmeticRarity.RARE;
        }

        if (type == GadgetType.MusicDisc) {
            return CosmeticRarity.RARE;
        }

        return CosmeticRarity.COMMON;
    }

    public static CosmeticRarity getShopRarity(Mount<?> mount) {
        String name = normalizeName(mount.GetName());

        if (containsAny(name, "dragon")) return CosmeticRarity.MYTHIC;
        if (containsAny(name, "phantom", "infernal")) return CosmeticRarity.LEGENDARY;
        if (containsAny(name, "glacial", "strider")) return CosmeticRarity.EPIC;
        if (containsAny(name, "bee", "slime")) return CosmeticRarity.RARE;
        
        return CosmeticRarity.COMMON;
    }

    public static int getTierOrder(CosmeticRarity rarity) {
        switch (normalize(rarity)) {
            case MYTHIC:
                return 4;
            case LEGENDARY:
                return 3;
            case EPIC:
                return 2;
            case RARE:
                return 1;
            case COMMON:
            default:
                return 0;
        }
    }

    public static CosmeticRarity normalize(CosmeticRarity rarity) {
        if (rarity == null) {
            return CosmeticRarity.COMMON;
        }

        return rarity;
    }

    private static boolean containsAny(String name, String... fragments) {
        for (String fragment : fragments) {
            if (name.contains(fragment)) {
                return true;
            }
        }

        return false;
    }

    private static String normalizeName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
