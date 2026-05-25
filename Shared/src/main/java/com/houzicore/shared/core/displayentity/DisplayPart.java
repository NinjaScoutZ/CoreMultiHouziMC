package com.houzicore.shared.core.displayentity;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * A single element within a {@link DisplayModel}.
 * Stores data describing one BlockDisplay, ItemDisplay, or TextDisplay entity.
 */
public class DisplayPart {

    public enum PartType { BLOCK, ITEM, TEXT }

    private final PartType _type;

    // Block-specific
    private org.bukkit.block.data.BlockData _blockData;

    // Item-specific
    private ItemStack _itemStack;
    private ItemDisplay.ItemDisplayTransform _itemTransform = ItemDisplay.ItemDisplayTransform.GROUND;

    // Text-specific
    private String _text;
    private Color _textBackgroundColor;

    // Transformation (shared)
    private Vector3f _translation = new Vector3f(0, 0, 0);
    private Vector3f _scale = new Vector3f(1, 1, 1);
    private Quaternionf _leftRotation = new Quaternionf();
    private Quaternionf _rightRotation = new Quaternionf();
    private float[] _matrixTransformation;

    // Visual settings
    private Integer _brightnessBlock;    // null = use world light
    private Integer _brightnessSky;
    private Display.Billboard _billboard;

    // ── Constructors ──────────────────────────────────

    private DisplayPart(PartType type) {
        _type = type;
    }

    /** Create a BlockDisplay part. */
    public static DisplayPart block(Material mat) {
        DisplayPart part = new DisplayPart(PartType.BLOCK);
        part._blockData = org.bukkit.Bukkit.createBlockData(mat);
        return part;
    }

    public static DisplayPart block(org.bukkit.block.data.BlockData data) {
        DisplayPart part = new DisplayPart(PartType.BLOCK);
        part._blockData = data;
        return part;
    }

    /** Create a BlockDisplay part that automatically offsets translation by -scale/2 to maintain center-alignment natively. */
    public static DisplayPart centeredBlock(Material mat) {
        DisplayPart part = new DisplayPart(PartType.BLOCK);
        part._blockData = org.bukkit.Bukkit.createBlockData(mat);
        part.centerBlock();
        return part;
    }

    public static DisplayPart centeredBlock(org.bukkit.block.data.BlockData data) {
        DisplayPart part = new DisplayPart(PartType.BLOCK);
        part._blockData = data;
        part.centerBlock();
        return part;
    }

    /** Create an ItemDisplay part. */
    public static DisplayPart item(ItemStack stack) {
        DisplayPart part = new DisplayPart(PartType.ITEM);
        part._itemStack = stack.clone();
        return part;
    }

    /** Create an ItemDisplay part from Material. */
    public static DisplayPart item(Material mat) {
        return item(new ItemStack(mat));
    }

    /** Create a TextDisplay part. */
    public static DisplayPart text(String legacyText) {
        DisplayPart part = new DisplayPart(PartType.TEXT);
        part._text = legacyText;
        return part;
    }

    // ── Builder methods ──────────────────────────────

    public DisplayPart translation(float x, float y, float z) {
        _translation = new Vector3f(x, y, z);
        return this;
    }

    public DisplayPart translation(Vector3f vec) {
        _translation = new Vector3f(vec);
        return this;
    }

    public DisplayPart scale(float x, float y, float z) {
        _scale = new Vector3f(x, y, z);
        return this;
    }

    public DisplayPart scale(float uniform) {
        return scale(uniform, uniform, uniform);
    }

    public DisplayPart scale(Vector3f vec) {
        _scale = new Vector3f(vec);
        return this;
    }

    public DisplayPart leftRotation(float x, float y, float z, float w) {
        _leftRotation = new Quaternionf(x, y, z, w);
        return this;
    }

    public DisplayPart leftRotation(Quaternionf q) {
        _leftRotation = new Quaternionf(q);
        return this;
    }

    public DisplayPart rightRotation(float x, float y, float z, float w) {
        _rightRotation = new Quaternionf(x, y, z, w);
        return this;
    }

    public DisplayPart rightRotation(Quaternionf q) {
        _rightRotation = new Quaternionf(q);
        return this;
    }

    public DisplayPart matrixTransformation(float[] rowMajorMatrix) {
        if (rowMajorMatrix == null || rowMajorMatrix.length != 16) {
            throw new IllegalArgumentException("Display transformation matrix must contain exactly 16 floats.");
        }
        _matrixTransformation = rowMajorMatrix.clone();
        return this;
    }

    public DisplayPart rotateAroundY(float yawDegrees) {
        if (Math.abs(yawDegrees) < 0.0001f) {
            return this;
        }

        float radians = (float) Math.toRadians(-yawDegrees);
        if (_matrixTransformation != null) {
            org.joml.Matrix4f mat = matrixFromMinecraftRowMajor(_matrixTransformation);
            org.joml.Matrix4f rotated = new org.joml.Matrix4f().rotationY(radians).mul(mat);
            _matrixTransformation = toMinecraftRowMajor(rotated);
            return this;
        }

        _translation.rotateY(radians);
        Quaternionf yawRotation = new Quaternionf().rotateY(radians);
        _leftRotation = yawRotation.mul(_leftRotation, new Quaternionf());
        return this;
    }

    public DisplayPart brightness(int blockLight, int skyLight) {
        _brightnessBlock = blockLight;
        _brightnessSky = skyLight;
        return this;
    }

    public DisplayPart billboard(Display.Billboard billboard) {
        _billboard = billboard;
        return this;
    }

    public DisplayPart textBackgroundColor(Color color) {
        _textBackgroundColor = color;
        return this;
    }

    private boolean _centerBlock = false;

    public DisplayPart itemTransform(ItemDisplay.ItemDisplayTransform transform) {
        _itemTransform = transform;
        return this;
    }

    /**
     * Tells this part to center its geometry around its translation.
     * Minecraft BlockDisplays render from the corner (0,0,0). Calling this applies a local offset 
     * equal to -scale/2 on all axes when generating the spawn command, centering the block visually.
     * Use this for programmatic models designed around center coordinates.
     */
    public DisplayPart centerBlock() {
        _centerBlock = true;
        return this;
    }

    // ── Getters ──────────────────────────────────────

    public PartType getType() { return _type; }
    public org.bukkit.block.data.BlockData getBlockData() { return _blockData; }
    public ItemStack getItemStack() { return _itemStack; }
    public String getText() { return _text; }
    public Vector3f getTranslation() { return new Vector3f(_translation); }
    public Vector3f getScale() { return new Vector3f(_scale); }
    public Quaternionf getLeftRotation() { return new Quaternionf(_leftRotation); }
    public Quaternionf getRightRotation() { return new Quaternionf(_rightRotation); }

    // ── Spawn ────────────────────────────────────────

    /**
     * Spawn the actual Display entity in the world at the given origin.
     * <p>
     * Uses raw Minecraft /summon command dispatch instead of Paper's Transformation API,
     * because Paper's setTransformation() does not properly sync transformation data
     * to clients — confirmed by debug logs showing correct values server-side but
     * broken rendering client-side.
     * <p>
     * This is the same approach used by the BlockDisplayCreator reference plugin
     * and identical to how Command Blocks spawn display entities (which works perfectly).
     */
    public Display spawn(Location origin) {
        Location spawnLoc = origin.clone();
        org.bukkit.World world = spawnLoc.getWorld();

        // Build the /summon command with full NBT data
        String entityType;
        String nbt;

        // Common transformation NBT
        String transformNbt = _centerBlock ? buildCenteredTransformationNbt() : buildTransformationNbt();
        String brightnessNbt = buildBrightnessNbt();

        switch (_type) {
            case BLOCK: {
                entityType = "minecraft:block_display";
                String blockName = _blockData != null ? _blockData.getAsString() : "minecraft:stone";
                // BlockData.getAsString() returns e.g. "minecraft:oak_slab[type=bottom]"
                // We need to split into Name and Properties for the block_state compound
                String blockStateNbt = buildBlockStateNbt(blockName);
                nbt = String.format("{block_state:%s,%s%s}", 
                    blockStateNbt, transformNbt,
                    brightnessNbt.isEmpty() ? "" : "," + brightnessNbt);
                break;
            }

            case ITEM: {
                entityType = "minecraft:item_display";
                String itemId = _itemStack != null ? _itemStack.getType().getKey().toString() : "minecraft:stone";
                // Check for player head with custom texture
                String itemNbt = buildItemNbt(itemId);
                String itemTransformNbt = "";
                if (_itemTransform != null && _itemTransform != ItemDisplay.ItemDisplayTransform.GROUND) {
                    itemTransformNbt = ",item_display:\"" + _itemTransform.name().toLowerCase() + "\"";
                }
                nbt = String.format("{item:%s,%s%s%s}",
                    itemNbt, transformNbt, itemTransformNbt,
                    brightnessNbt.isEmpty() ? "" : "," + brightnessNbt);
                break;
            }

            case TEXT: {
                entityType = "minecraft:text_display";
                String safeText = _text != null ? _text.replace("\"", "\\\"") : "";
                String billboardStr = _billboard != null ? _billboard.name().toLowerCase() : "center";
                nbt = String.format("{text:'{\"text\":\"%s\"}',%s,billboard:\"%s\"%s}",
                    safeText, transformNbt, billboardStr,
                    brightnessNbt.isEmpty() ? "" : "," + brightnessNbt);
                break;
            }

            default:
                throw new IllegalStateException("Unknown PartType: " + _type);
        }

        // Build the full summon command
        String command = String.format("summon %s %.4f %.4f %.4f %s",
            entityType, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(), nbt);

        // Remember entity count before summoning
        java.util.List<org.bukkit.entity.Entity> entitiesBefore = new java.util.ArrayList<>(
            world.getNearbyEntities(spawnLoc, 0.5, 0.5, 0.5));

        // Execute via server console — Minecraft's own NBT parser handles everything
        org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);

        // Find the newly spawned entity
        Display entity = null;
        for (org.bukkit.entity.Entity e : world.getNearbyEntities(spawnLoc, 0.5, 0.5, 0.5)) {
            if (e instanceof Display && !entitiesBefore.contains(e)) {
                entity = (Display) e;
                entity.setPersistent(false);
                break;
            }
        }

        // Fallback: if entity not found via proximity, search wider
        if (entity == null) {
            for (org.bukkit.entity.Entity e : world.getNearbyEntities(spawnLoc, 2.0, 2.0, 2.0)) {
                if (e instanceof Display && !entitiesBefore.contains(e)) {
                    entity = (Display) e;
                    entity.setPersistent(false);
                    break;
                }
            }
        }

        if (entity == null) {
            System.out.println("[BDEngine] WARNING: Could not find spawned entity after summon command!");
            // Last resort: spawn a dummy entity so we don't NPE
            entity = world.spawn(spawnLoc, BlockDisplay.class, e -> {
                e.setBlock(org.bukkit.Bukkit.createBlockData(Material.STONE));
                e.setPersistent(false);
            });
        }

        return entity;
    }

    /**
     * Builds the transformation NBT compound tag.
     * Format: transformation:{translation:[x,y,z],left_rotation:[x,y,z,w],scale:[x,y,z],right_rotation:[x,y,z,w]}
     */
    private String buildTransformationNbt() {
        if (_matrixTransformation != null) {
            return buildMatrixTransformationNbt(_matrixTransformation);
        }
        return String.format(java.util.Locale.US,
            "transformation:{translation:[%.6ff,%.6ff,%.6ff],left_rotation:[%.6ff,%.6ff,%.6ff,%.6ff],scale:[%.6ff,%.6ff,%.6ff],right_rotation:[%.6ff,%.6ff,%.6ff,%.6ff]}",
            _translation.x, _translation.y, _translation.z,
            _leftRotation.x, _leftRotation.y, _leftRotation.z, _leftRotation.w,
            _scale.x, _scale.y, _scale.z,
            _rightRotation.x, _rightRotation.y, _rightRotation.z, _rightRotation.w
        );
    }

    private String buildMatrixTransformationNbt(float[] matrix) {
        return String.format(java.util.Locale.US,
            "transformation:[%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff,%.6ff]",
            matrix[0], matrix[1], matrix[2], matrix[3],
            matrix[4], matrix[5], matrix[6], matrix[7],
            matrix[8], matrix[9], matrix[10], matrix[11],
            matrix[12], matrix[13], matrix[14], matrix[15]
        );
    }

    private static org.joml.Matrix4f matrixFromMinecraftRowMajor(float[] matrix) {
        return new org.joml.Matrix4f(
            matrix[0], matrix[4], matrix[8], matrix[12],
            matrix[1], matrix[5], matrix[9], matrix[13],
            matrix[2], matrix[6], matrix[10], matrix[14],
            matrix[3], matrix[7], matrix[11], matrix[15]
        );
    }

    private static float[] toMinecraftRowMajor(org.joml.Matrix4f mat) {
        return new float[] {
            mat.m00(), mat.m10(), mat.m20(), mat.m30(),
            mat.m01(), mat.m11(), mat.m21(), mat.m31(),
            mat.m02(), mat.m12(), mat.m22(), mat.m32(),
            mat.m03(), mat.m13(), mat.m23(), mat.m33()
        };
    }

    /**
     * Builds the transformation NBT compound tag with translation offset by -scale/2.
     * Used for programmatic furniture blocks which are designed with center-coordinates.
     */
    private String buildCenteredTransformationNbt() {
        return String.format(java.util.Locale.US,
            "transformation:{translation:[%.6ff,%.6ff,%.6ff],left_rotation:[%.6ff,%.6ff,%.6ff,%.6ff],scale:[%.6ff,%.6ff,%.6ff],right_rotation:[%.6ff,%.6ff,%.6ff,%.6ff]}",
            _translation.x - (_scale.x / 2f), 
            _translation.y - (_scale.y / 2f), 
            _translation.z - (_scale.z / 2f),
            _leftRotation.x, _leftRotation.y, _leftRotation.z, _leftRotation.w,
            _scale.x, _scale.y, _scale.z,
            _rightRotation.x, _rightRotation.y, _rightRotation.z, _rightRotation.w
        );
    }

    /**
     * Builds brightness NBT if brightness is set.
     */
    private String buildBrightnessNbt() {
        if (_brightnessBlock != null && _brightnessSky != null) {
            return String.format(java.util.Locale.US, "brightness:{block:%d,sky:%d}", _brightnessBlock, _brightnessSky);
        }
        return "";
    }

    /**
     * Builds block_state NBT from a BlockData string.
     * Input: "minecraft:oak_slab[type=bottom]" or "minecraft:stone"
     * Output: {Name:"minecraft:oak_slab",Properties:{type:"bottom"}} or {Name:"minecraft:stone"}
     */
    private String buildBlockStateNbt(String blockDataStr) {
        int bracketIdx = blockDataStr.indexOf('[');
        if (bracketIdx >= 0) {
            String name = blockDataStr.substring(0, bracketIdx);
            String props = blockDataStr.substring(bracketIdx + 1, blockDataStr.length() - 1);
            StringBuilder propNbt = new StringBuilder();
            propNbt.append("Properties:{");
            String[] pairs = props.split(",");
            for (int i = 0; i < pairs.length; i++) {
                String[] kv = pairs[i].split("=", 2);
                if (i > 0) propNbt.append(",");
                propNbt.append(kv[0]).append(":\"").append(kv[1]).append("\"");
            }
            propNbt.append("}");
            return String.format("{Name:\"%s\",%s}", name, propNbt);
        }
        return String.format("{Name:\"%s\"}", blockDataStr);
    }

    /**
     * Builds item NBT for item_display.
     */
    private String buildItemNbt(String itemId) {
        // Check for player_head with custom texture
        if (_itemStack != null && _itemStack.getType() == Material.PLAYER_HEAD) {
            org.bukkit.inventory.meta.ItemMeta meta = _itemStack.getItemMeta();
            if (meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
                org.bukkit.profile.PlayerProfile profile = skullMeta.getOwnerProfile();
                if (profile != null) {
                    // For textured heads, just use the basic item id
                    return String.format("{id:\"%s\",count:1}", itemId);
                }
            }
        }
        return String.format("{id:\"%s\",count:1}", itemId);
    }

    /**
     * Create a deep copy of this part.
     */
    public DisplayPart copy() {
        DisplayPart c = new DisplayPart(_type);
        c._blockData = _blockData != null ? _blockData.clone() : null;
        c._itemStack = _itemStack != null ? _itemStack.clone() : null;
        c._itemTransform = _itemTransform;
        c._text = _text;
        c._textBackgroundColor = _textBackgroundColor;
        c._translation = new Vector3f(_translation);
        c._scale = new Vector3f(_scale);
        c._leftRotation = new Quaternionf(_leftRotation);
        c._rightRotation = new Quaternionf(_rightRotation);
        c._matrixTransformation = _matrixTransformation != null ? _matrixTransformation.clone() : null;
        c._brightnessBlock = _brightnessBlock;
        c._brightnessSky = _brightnessSky;
        c._billboard = _billboard;
        c._centerBlock = _centerBlock;
        return c;
    }
}
