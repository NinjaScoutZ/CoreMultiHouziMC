package com.houzicore.shared.common.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Snow;

public class IdUtil {

    public static Material getMaterial(int id) {
        return getMaterial(id, (byte) 0);
    }

    public static Material getMaterial(int id, byte data) {
        switch (id) {
            case 0: return Material.AIR;
            case 1: return Material.STONE;
            case 2: return Material.GRASS_BLOCK;
            case 3: return Material.DIRT;
            case 4: return Material.COBBLESTONE;
            case 5: return woodMaterial(data, "PLANKS");
            case 7: return Material.BEDROCK;
            case 8: return Material.WATER;
            case 9: return Material.WATER;
            case 10: return Material.LAVA;
            case 11: return Material.LAVA;
            case 12: return Material.SAND;
            case 13: return Material.GRAVEL;
            case 14: return Material.GOLD_ORE;
            case 15: return Material.IRON_ORE;
            case 16: return Material.COAL_ORE;
            case 17: return woodMaterial(data, "LOG");
            case 18: return woodMaterial(data, "LEAVES");
            case 19: return Material.SPONGE;
            case 20: return Material.GLASS;
            case 21: return Material.LAPIS_ORE;
            case 22: return Material.LAPIS_BLOCK;
            case 23: return Material.DISPENSER;
            case 24: return Material.SANDSTONE;
            case 25: return Material.NOTE_BLOCK;
            case 26: return Material.RED_BED;
            case 27: return Material.POWERED_RAIL;
            case 28: return Material.DETECTOR_RAIL;
            case 29: return Material.STICKY_PISTON;
            case 30: return Material.COBWEB;
            case 31: return Material.SHORT_GRASS;
            case 32: return Material.DEAD_BUSH;
            case 33: return Material.PISTON;
            case 34: return Material.PISTON_HEAD;
            case 35: return dyedMaterial(data, "WOOL");
            case 43: return data == 6 ? Material.NETHER_BRICKS : Material.SMOOTH_STONE;
            case 44: return data == 6 ? Material.NETHER_BRICK_SLAB : Material.STONE_SLAB;
            case 45: return Material.BRICKS;
            case 46: return Material.TNT;
            case 47: return Material.BOOKSHELF;
            case 48: return Material.MOSSY_COBBLESTONE;
            case 49: return Material.OBSIDIAN;
            case 50: return Material.TORCH;
            case 51: return Material.FIRE;
            case 52: return Material.SPAWNER;
            case 53: return Material.OAK_STAIRS;
            case 54: return Material.CHEST;
            case 55: return Material.REDSTONE_WIRE;
            case 56: return Material.DIAMOND_ORE;
            case 57: return Material.DIAMOND_BLOCK;
            case 58: return Material.CRAFTING_TABLE;
            case 78: return Material.SNOW;
            case 79: return Material.ICE;
            case 80: return Material.SNOW_BLOCK;
            case 85: return Material.OAK_FENCE;
            case 91: return Material.JACK_O_LANTERN;
            case 95: return dyedMaterial(data, "STAINED_GLASS");
            case 98: return stoneBrickMaterial(data);
            case 112: return Material.NETHER_BRICKS;
            case 114: return Material.NETHER_BRICK_STAIRS;
            case 126: return woodMaterial(data, "SLAB");
            case 131: return Material.TRIPWIRE_HOOK;
            case 159: return dyedMaterial(data, "TERRACOTTA");
            case 160: return dyedMaterial(data, "STAINED_GLASS_PANE");
            case 171: return dyedMaterial(data, "CARPET");
            case 174: return Material.PACKED_ICE;
            case 175: return Material.SUNFLOWER;
            case 262: return Material.ARROW;
        }
        for (Material material : Material.values()) {
            if (!material.isLegacy() && material.ordinal() == id) {
                return material;
            }
        }
        return Material.STONE; // Default fallback
    }

    public static BlockData getBlockData(int id, byte data) {
        BlockData blockData = getMaterial(id, data).createBlockData();

        if (id == 78 && blockData instanceof Snow snow) {
            snow.setLayers(Math.max(1, Math.min(snow.getMaximumLayers(), data + 1)));
        }

        return blockData;
    }

    public static int getTypeId(Material material) {
        if (material == null) return 0;
        String name = material.name();
        if (name.endsWith("_WOOL")) return 35;
        if (name.endsWith("_STAINED_GLASS")) return 95;
        if (name.endsWith("_TERRACOTTA")) return 159;
        if (name.endsWith("_STAINED_GLASS_PANE")) return 160;
        if (name.endsWith("_CARPET")) return 171;
        switch (material) {
            case AIR: return 0;
            case STONE: return 1;
            case GRASS_BLOCK: return 2;
            case DIRT: return 3;
            case COBBLESTONE: return 4;
            case OAK_PLANKS: return 5;
            case BEDROCK: return 7;
            case WATER: return 8;
            case LAVA: return 10;
            case SAND: return 12;
            case GRAVEL: return 13;
            case GOLD_ORE: return 14;
            case IRON_ORE: return 15;
            case COAL_ORE: return 16;
            case OAK_LOG: return 17;
            case OAK_LEAVES: return 18;
            case SPONGE: return 19;
            case GLASS: return 20;
            case COBWEB: return 30;
            case WHITE_WOOL: return 35;
            case ORANGE_WOOL: return 35;
            case MAGENTA_WOOL: return 35;
            case LIGHT_BLUE_WOOL: return 35;
            case YELLOW_WOOL: return 35;
            case LIME_WOOL: return 35;
            case PINK_WOOL: return 35;
            case GRAY_WOOL: return 35;
            case LIGHT_GRAY_WOOL: return 35;
            case CYAN_WOOL: return 35;
            case PURPLE_WOOL: return 35;
            case BLUE_WOOL: return 35;
            case BROWN_WOOL: return 35;
            case GREEN_WOOL: return 35;
            case RED_WOOL: return 35;
            case BLACK_WOOL: return 35;
            case STONE_SLAB: return 44;
            case SNOW: return 78;
            case ICE: return 79;
            case SNOW_BLOCK: return 80;
            case OAK_FENCE: return 85;
            case JACK_O_LANTERN: return 91;
            case WHITE_STAINED_GLASS: return 95;
            case STONE_BRICKS: return 98;
            case MOSSY_STONE_BRICKS: return 98;
            case CRACKED_STONE_BRICKS: return 98;
            case CHISELED_STONE_BRICKS: return 98;
            case NETHER_BRICKS: return 112;
            case NETHER_BRICK_STAIRS: return 114;
            case OAK_SLAB: return 126;
            case TRIPWIRE_HOOK: return 131;
            case WHITE_TERRACOTTA: return 159;
            case ORANGE_TERRACOTTA: return 159;
            case MAGENTA_TERRACOTTA: return 159;
            case LIGHT_BLUE_TERRACOTTA: return 159;
            case YELLOW_TERRACOTTA: return 159;
            case LIME_TERRACOTTA: return 159;
            case PINK_TERRACOTTA: return 159;
            case GRAY_TERRACOTTA: return 159;
            case LIGHT_GRAY_TERRACOTTA: return 159;
            case CYAN_TERRACOTTA: return 159;
            case PURPLE_TERRACOTTA: return 159;
            case BLUE_TERRACOTTA: return 159;
            case BROWN_TERRACOTTA: return 159;
            case GREEN_TERRACOTTA: return 159;
            case RED_TERRACOTTA: return 159;
            case BLACK_TERRACOTTA: return 159;
            case WHITE_STAINED_GLASS_PANE: return 160;
            case WHITE_CARPET: return 171;
            case PACKED_ICE: return 174;
            case SUNFLOWER: return 175;
            case ARROW: return 262;
        }
        return material.ordinal(); // Modern fallback for code that already stores enum ordinals.
    }

    public static int getTypeId(Block block) {
        if (block == null) return 0;
        return getTypeId(block.getType());
    }

    public static int getTypeId(ItemStack item) {
        if (item == null) return 0;
        return getTypeId(item.getType());
    }

    public static byte getData(Block block) {
        if (block == null) return 0;

        if (block.getBlockData() instanceof Snow snow) {
            return (byte) Math.max(0, snow.getLayers() - 1);
        }

        return getData(block.getType());
    }

    public static byte getData(ItemStack item) {
        if (item == null) return 0;
        return getData(item.getType());
    }

    public static byte getData(Material material) {
        if (material == null) return 0;

        Byte colorData = colorData(material.name());
        if (colorData != null) {
            return colorData;
        }

        switch (material) {
            case WHITE_WOOL:
            case WHITE_TERRACOTTA:
            case WHITE_STAINED_GLASS:
            case WHITE_STAINED_GLASS_PANE:
            case WHITE_CARPET:
                return 0;
            case ORANGE_WOOL:
            case ORANGE_TERRACOTTA:
                return 1;
            case MAGENTA_WOOL:
            case MAGENTA_TERRACOTTA:
                return 2;
            case LIGHT_BLUE_WOOL:
            case LIGHT_BLUE_TERRACOTTA:
                return 3;
            case YELLOW_WOOL:
            case YELLOW_TERRACOTTA:
                return 4;
            case LIME_WOOL:
            case LIME_TERRACOTTA:
                return 5;
            case PINK_WOOL:
            case PINK_TERRACOTTA:
                return 6;
            case GRAY_WOOL:
            case GRAY_TERRACOTTA:
                return 7;
            case LIGHT_GRAY_WOOL:
            case LIGHT_GRAY_TERRACOTTA:
                return 8;
            case CYAN_WOOL:
            case CYAN_TERRACOTTA:
                return 9;
            case PURPLE_WOOL:
            case PURPLE_TERRACOTTA:
                return 10;
            case BLUE_WOOL:
            case BLUE_TERRACOTTA:
                return 11;
            case BROWN_WOOL:
            case BROWN_TERRACOTTA:
                return 12;
            case GREEN_WOOL:
            case GREEN_TERRACOTTA:
                return 13;
            case RED_WOOL:
            case RED_TERRACOTTA:
                return 14;
            case BLACK_WOOL:
            case BLACK_TERRACOTTA:
                return 15;
            case MOSSY_STONE_BRICKS:
                return 1;
            case CRACKED_STONE_BRICKS:
                return 2;
            case CHISELED_STONE_BRICKS:
                return 3;
            case NETHER_BRICK_SLAB:
                return 6;
            default:
                return 0;
        }
    }

    public static void setTypeIdAndData(Block block, int id, byte data, boolean applyPhysics) {
        if (block == null) return;
        block.setBlockData(getBlockData(id, data), applyPhysics);
    }

    public static void setTypeIdAndData(Block block, Material material, BlockData blockData, boolean applyPhysics) {
        if (block == null) return;
        if (blockData != null) {
            block.setBlockData(blockData, applyPhysics);
        } else {
            block.setType(material, applyPhysics);
        }
    }

    public static short getTypeId(org.bukkit.entity.EntityType entityType) {
        if (entityType == null) return 0;
        // In 1.21, entity type IDs are no longer used. Return 0 as fallback.
        return 0;
    }

    /**
     * Returns the spawn egg Material for a given EntityType in 1.21+.
     * In modern Minecraft, each entity has its own spawn egg material (e.g. PIG_SPAWN_EGG).
     */
    public static Material getSpawnEggMaterial(org.bukkit.entity.EntityType entityType) {
        if (entityType == null) return Material.BAT_SPAWN_EGG;
        try {
            // Try to find ENTITY_SPAWN_EGG material directly
            String eggName = entityType.name() + "_SPAWN_EGG";
            return Material.valueOf(eggName);
        } catch (IllegalArgumentException e) {
            return Material.BAT_SPAWN_EGG; // Default fallback
        }
    }

    private static Material stoneBrickMaterial(byte data) {
        switch (data) {
            case 1: return Material.MOSSY_STONE_BRICKS;
            case 2: return Material.CRACKED_STONE_BRICKS;
            case 3: return Material.CHISELED_STONE_BRICKS;
            default: return Material.STONE_BRICKS;
        }
    }

    private static Material dyedMaterial(byte data, String suffix) {
        final String color;
        switch (data) {
            case 1: color = "ORANGE"; break;
            case 2: color = "MAGENTA"; break;
            case 3: color = "LIGHT_BLUE"; break;
            case 4: color = "YELLOW"; break;
            case 5: color = "LIME"; break;
            case 6: color = "PINK"; break;
            case 7: color = "GRAY"; break;
            case 8: color = "LIGHT_GRAY"; break;
            case 9: color = "CYAN"; break;
            case 10: color = "PURPLE"; break;
            case 11: color = "BLUE"; break;
            case 12: color = "BROWN"; break;
            case 13: color = "GREEN"; break;
            case 14: color = "RED"; break;
            case 15: color = "BLACK"; break;
            default: color = "WHITE"; break;
        }

        try {
            return Material.valueOf(color + "_" + suffix);
        } catch (IllegalArgumentException ex) {
            return Material.WHITE_WOOL;
        }
    }

    private static Material woodMaterial(byte data, String suffix) {
        final String wood;
        switch (data & 3) {
            case 1: wood = "SPRUCE"; break;
            case 2: wood = "BIRCH"; break;
            case 3: wood = "JUNGLE"; break;
            default: wood = "OAK"; break;
        }

        try {
            return Material.valueOf(wood + "_" + suffix);
        } catch (IllegalArgumentException ex) {
            return Material.OAK_PLANKS;
        }
    }

    private static Byte colorData(String materialName) {
        if (!materialName.endsWith("_WOOL") && !materialName.endsWith("_STAINED_GLASS")
                && !materialName.endsWith("_TERRACOTTA") && !materialName.endsWith("_STAINED_GLASS_PANE")
                && !materialName.endsWith("_CARPET")) {
            return null;
        }

        if (materialName.startsWith("WHITE_")) return 0;
        if (materialName.startsWith("ORANGE_")) return 1;
        if (materialName.startsWith("MAGENTA_")) return 2;
        if (materialName.startsWith("LIGHT_BLUE_")) return 3;
        if (materialName.startsWith("YELLOW_")) return 4;
        if (materialName.startsWith("LIME_")) return 5;
        if (materialName.startsWith("PINK_")) return 6;
        if (materialName.startsWith("GRAY_")) return 7;
        if (materialName.startsWith("LIGHT_GRAY_")) return 8;
        if (materialName.startsWith("CYAN_")) return 9;
        if (materialName.startsWith("PURPLE_")) return 10;
        if (materialName.startsWith("BLUE_")) return 11;
        if (materialName.startsWith("BROWN_")) return 12;
        if (materialName.startsWith("GREEN_")) return 13;
        if (materialName.startsWith("RED_")) return 14;
        if (materialName.startsWith("BLACK_")) return 15;
        return null;
    }
}
