package com.houzicore.shared.core.common.block.schematic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.houzicore.shared.core.common.block.DataLocationMap;
import com.houzicore.shared.core.common.block.schematic.Nbt.Tag;

public class Schematic {
    private final short _width;
    private final short _height;
    private final short _length;
    private final short[] _blocks;
    private final byte[] _blockData;
    private final Vector _weOffset;
    private final Map<BlockVector, Map<String, Tag>> _tileEntities;
    private final List<Tag> _entities;

    public Schematic(short width, short height, short length, short[] blocks, byte[] blockData, Vector worldEditOffset, Map<BlockVector, Map<String, Tag>> tileEntities, List<Tag> entities) {
        _width = width;
        _height = height;
        _length = length;
        _blocks = blocks;
        _blockData = blockData;
        _weOffset = worldEditOffset;
        _tileEntities = tileEntities;
        _entities = entities;
    }

    public Schematic(short width, short height, short length, short[] blocks, byte[] blockData, Vector worldEditOffset, Map<BlockVector, Map<String, Tag>> tileEntities) {
        this(width, height, length, blocks, blockData, worldEditOffset, tileEntities, new ArrayList<>());
    }

    public Schematic(short width, short height, short length, short[] blocks, byte[] blockData, Vector worldEditOffset) {
        this(width, height, length, blocks, blockData, worldEditOffset, new HashMap<>());
    }

    public Schematic(short width, short height, short length, short[] blocks, byte[] blockData) {
        this(width, height, length, blocks, blockData, null);
    }

    public Schematic(Schematic schematic) {
        this(schematic.getWidth(), schematic.getHeight(), schematic.getLength(), schematic.getBlocks(), schematic.getBlockData(), schematic.getWorldEditOffset(), schematic.getTileEntities(), schematic.getEntities());
    }

    public SchematicData paste(Location originLocation) {
        return paste(originLocation, false);
    }

    public SchematicData paste(Location originLocation, boolean ignoreAir) {
        return paste(originLocation, ignoreAir, false);
    }

    public SchematicData paste(Location originLocation, boolean ignoreAir, boolean worldEditOffset) {
        return paste(originLocation, ignoreAir, worldEditOffset, true);
    }

    public SchematicData paste(Location originLocation, boolean ignoreAir, boolean worldEditOffset, boolean quickSet) {
        if (worldEditOffset && hasWorldEditOffset()) {
            originLocation = originLocation.clone().add(_weOffset);
        }
        DataLocationMap locationMap = new DataLocationMap();
        SchematicData output = new SchematicData(locationMap, originLocation.getWorld());

        int startX = originLocation.getBlockX();
        int startY = originLocation.getBlockY();
        int startZ = originLocation.getBlockZ();

        World world = originLocation.getWorld();

        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {
                for (int z = 0; z < _length; z++) {
                    int index = getIndex(x, y, z);
                    int materialId = Math.abs(_blocks[index]);

                    if (ignoreAir && materialId == 0) {
                        continue;
                    } else if (materialId == 147) { // Gold plate
                        if (addDataWool(locationMap, true, originLocation, x, y - 1, z))
                            continue;
                    } else if (materialId == 148) { // Iron plate
                        if (addDataWool(locationMap, false, originLocation, x, y - 1, z))
                            continue;
                    } else if (materialId == 19) { // Sponge
                        if (addSpongeLocation(locationMap, originLocation, x, y + 1, z))
                            continue;
                    } else if (materialId == 35) { // Wool
                        int aboveIndex = getIndex(x, y + 1, z);
                        if (hasIndex(aboveIndex)) {
                            int aboveId = Math.abs(_blocks[aboveIndex]);
                            if (aboveId == 147 || aboveId == 148)
                                continue;
                        }
                        int belowIndex = getIndex(x, y - 1, z);
                        if (hasIndex(belowIndex)) {
                            if (Math.abs(_blocks[belowIndex]) == 19)
                                continue;
                        }
                    }

                    Block block = world.getBlockAt(startX + x, startY + y, startZ + z);
                    var blockData = com.houzicore.shared.common.util.IdUtil.getBlockData(materialId, _blockData[index]);
                    block.setBlockData(blockData, false);

                    BlockVector bv = new BlockVector(x, y, z);
                    output.getBlocksRaw().add(bv);
                }
            }
        }

        return output;
    }

    private boolean addDataWool(DataLocationMap map, boolean gold, Location origin, int x, int y, int z) {
        int index = getIndex(x, y, z);
        if (hasIndex(index)) {
            int materialId = Math.abs(_blocks[index]);
            if (materialId == 35) {
                byte data = _blockData[index];
                DyeColor color = DyeColor.getByWoolData(data);
                if (color != null) {
                    if (gold) {
                        map.addGoldLocation(color, origin.clone().add(x, y, z));
                    } else {
                        map.addIronLocation(color, origin.clone().add(x, y, z));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean addSpongeLocation(DataLocationMap map, Location origin, int x, int y, int z) {
        int index = getIndex(x, y, z);
        if (hasIndex(index)) {
            int materialId = Math.abs(_blocks[index]);
            if (materialId == 35) {
                byte data = _blockData[index];
                DyeColor color = DyeColor.getByWoolData(data);
                if (color != null) {
                    map.addSpongeLocation(color, origin.clone().add(x, y - 1, z));
                    return true;
                }
            }
        }
        return false;
    }

    public Schematic rotate180() {
        int area = _length * _width;

        for (int height = 0; height < _height; height++) {
            int startIndex = height * area;
            int endIndex = (int) (startIndex + area / 2D);

            for (int lower = startIndex; lower <= endIndex; lower++) {
                int upper = endIndex - lower;

                short temp = _blocks[lower];
                byte tempData = _blockData[lower];

                _blocks[lower] = _blocks[upper];
                _blocks[upper] = temp;

                _blockData[lower] = _blockData[upper];
                _blockData[upper] = tempData;
            }
        }

        _tileEntities.keySet().forEach(blockVector -> {
            blockVector.setX(-blockVector.getX());
            blockVector.setZ(-blockVector.getZ());
        });

        return this;
    }

    public boolean hasWorldEditOffset() {
        return _weOffset != null;
    }

    public Vector getWorldEditOffset() {
        if (!hasWorldEditOffset()) return null;
        return _weOffset.clone();
    }

    public int getSize() {
        return _blocks.length;
    }

    public int getIndex(int x, int y, int z) {
        return y * _width * _length + z * _width + x;
    }

    public boolean hasIndex(int index) {
        return index < _blocks.length && index >= 0;
    }

    public Short getBlock(int x, int y, int z) {
        int idx = getIndex(x, y, z);
        if (idx >= _blocks.length || idx < 0) return null;
        return _blocks[idx];
    }

    public Byte getData(int x, int y, int z) {
        int idx = getIndex(x, y, z);
        if (idx >= _blocks.length || idx < 0) return null;
        return _blockData[idx];
    }

    public short getWidth() { return _width; }
    public short getHeight() { return _height; }
    public short getLength() { return _length; }
    public short[] getBlocks() { return _blocks; }
    public byte[] getBlockData() { return _blockData; }
    public List<Tag> getEntities() { return _entities; }
    public Map<BlockVector, Map<String, Tag>> getTileEntities() { return _tileEntities; }

    @Override
    public String toString() {
        return String.format("Schematic [width: %d, length: %d, height: %d, blockLength: %d, blockDataLength: %d]", _width, _length, _height, _blocks.length, _blockData.length);
    }
}
