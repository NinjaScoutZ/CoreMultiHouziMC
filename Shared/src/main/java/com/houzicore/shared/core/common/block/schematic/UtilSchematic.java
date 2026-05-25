package com.houzicore.shared.core.common.block.schematic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.bukkit.Location;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class UtilSchematic {

    public static Schematic loadSchematic(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return loadSchematic(fis);
        }
    }

    public static Schematic loadSchematic(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            return loadSchematic(bis);
        }
    }

    public static Schematic loadSchematic(InputStream input) throws IOException {
        Nbt.NBTInputStream nbtStream = new Nbt.NBTInputStream(new GZIPInputStream(input));
        Nbt.NamedTag rootTag = nbtStream.readNamedTag();
        nbtStream.close();

        if (!rootTag.getName().equalsIgnoreCase("Schematic")) {
            return null;
        }

        Nbt.CompoundTag schematicTag = (Nbt.CompoundTag) rootTag.getTag();
        Map<String, Nbt.Tag> schematic = schematicTag.getValue();

        short width = getChildTag(schematic, "Width", Nbt.ShortTag.class).getValue();
        short height = getChildTag(schematic, "Height", Nbt.ShortTag.class).getValue();
        short length = getChildTag(schematic, "Length", Nbt.ShortTag.class).getValue();

        byte[] blockId = getChildTag(schematic, "Blocks", Nbt.ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];
        short[] blocks = new short[blockId.length];
        byte[] blockData = getChildTag(schematic, "Data", Nbt.ByteArrayTag.class).getValue();

        Vector weOffset = null;
        if (schematic.containsKey("WEOffsetX") && schematic.containsKey("WEOffsetY") && schematic.containsKey("WEOffsetZ")) {
            int x = getChildTag(schematic, "WEOffsetX", Nbt.IntTag.class).getValue();
            int y = getChildTag(schematic, "WEOffsetY", Nbt.IntTag.class).getValue();
            int z = getChildTag(schematic, "WEOffsetZ", Nbt.IntTag.class).getValue();
            weOffset = new Vector(x, y, z);
        }

        if (schematic.containsKey("AddBlocks")) {
            addId = getChildTag(schematic, "AddBlocks", Nbt.ByteArrayTag.class).getValue();
        }

        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= addId.length) {
                blocks[index] = (short) (blockId[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
        }

        Map<BlockVector, Map<String, Nbt.Tag>> tileEntitiesMap = new HashMap<>();
        if (schematic.containsKey("TileEntities")) {
            List<Nbt.Tag> tileEntities = getChildTag(schematic, "TileEntities", Nbt.ListTag.class).getValue();
            for (Nbt.Tag tag : tileEntities) {
                if (!(tag instanceof Nbt.CompoundTag)) {
                    continue;
                }
                Nbt.CompoundTag t = (Nbt.CompoundTag) tag;
                int x = 0, y = 0, z = 0;
                Map<String, Nbt.Tag> values = new HashMap<>();

                for (Map.Entry<String, Nbt.Tag> entry : t.getValue().entrySet()) {
                    if (entry.getValue() instanceof Nbt.IntTag) {
                        if (entry.getKey().equals("x")) {
                            x = ((Nbt.IntTag) entry.getValue()).getValue();
                        } else if (entry.getKey().equals("y")) {
                            y = ((Nbt.IntTag) entry.getValue()).getValue();
                        } else if (entry.getKey().equals("z")) {
                            z = ((Nbt.IntTag) entry.getValue()).getValue();
                        }
                    }
                    values.put(entry.getKey(), entry.getValue());
                }
                tileEntitiesMap.put(new BlockVector(x, y, z), values);
            }
        }

        List<Nbt.Tag> entityTags = new ArrayList<>();
        if (schematic.containsKey("Entities")) {
            entityTags = getChildTag(schematic, "Entities", Nbt.ListTag.class).getValue();
        }

        return new Schematic(width, height, length, blocks, blockData, weOffset, tileEntitiesMap, entityTags);
    }

    private static <T extends Nbt.Tag> T getChildTag(Map<String, Nbt.Tag> items, String key, Class<T> expected) {
        Nbt.Tag tag = items.get(key);
        if (tag == null) {
            throw new IllegalArgumentException("Tag not found: " + key);
        }
        return expected.cast(tag);
    }

    // Stubs for write/create methods to maintain API compatibility with legacy dependencies if any

    public static byte[] getBytes(Schematic schematic) {
        return new byte[0];
    }

    public static void writeBytes(Schematic schematic, OutputStream output) {
    }

    public static Schematic createSchematic(Location locA, Location locB) {
        return null;
    }

    public static Schematic createSchematic(Location locA, Location locB, Vector worldEditOffset) {
        return null;
    }
}
