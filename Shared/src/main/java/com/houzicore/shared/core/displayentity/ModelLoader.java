package com.houzicore.shared.core.displayentity;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

/**
 * Loader for parsing BDEngine export data into {@link DisplayModel} objects.
 * Supports:
 * <ul>
 *   <li>BDEngine JSON array format (from web editor export)</li>
 *   <li>Simplified JSON files stored on the server</li>
 * </ul>
 * <p>
 * Uses Gson which is already included in HouziCore's Shared dependencies.
 */
public class ModelLoader {

    private ModelLoader() {} // Static utility

    // ── From BDEngine JSON ───────────────────────────

    /**
     * Parse a BDEngine-exported JSON string into a DisplayModel.
     * <p>
     * BDEngine exports a JSON array of display entity definitions.
     * Each element has: type, block/item data, and transformation values.
     * <p>
     * Example minimal format:
     * <pre>
     * [
     *   {
     *     "type": "block_display",
     *     "block": "minecraft:oak_planks",
     *     "translation": [0, 0, 0],
     *     "scale": [1, 1, 1],
     *     "left_rotation": [0, 0, 0, 1],
     *     "right_rotation": [0, 0, 0, 1]
     *   },
     *   ...
     * ]
     * </pre>
     *
     * @param modelId  Unique ID for the resulting model
     * @param json     The JSON string from BDEngine
     * @return A DisplayModel ready to spawn
     */
    public static DisplayModel fromJson(String modelId, String json) {
        // Handle Base64 GZIP fallback for .bdengine project files
        if (json.startsWith("H4sI") || !json.trim().startsWith("{") && !json.trim().startsWith("[")) {
            try {
                byte[] decoded = Base64.getDecoder().decode(json.trim());
                try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(decoded));
                     InputStreamReader reader = new InputStreamReader(gis, java.nio.charset.StandardCharsets.UTF_8)) {
                    
                    StringBuilder sb = new StringBuilder();
                    char[] buffer = new char[8192];
                    int read;
                    while ((read = reader.read(buffer)) != -1) {
                        sb.append(buffer, 0, read);
                    }
                    json = sb.toString();
                }
            } catch (Exception e) {
                // If it fails, fallback to passing the raw json string
            }
        }

        JsonElement el = JsonParser.parseString(json);

        List<DisplayPart> parts = new ArrayList<>();
        collectParts(el, parts, new org.joml.Matrix4f(), false);

        if (parts.isEmpty()) {
            throw new IllegalArgumentException("No valid display parts found in JSON for model: " + modelId);
        }

        return new DisplayModel(modelId, parts);
    }

    /**
     * Recursively collect DisplayParts from any JSON structure.
     * Handles:
     * - BDEngine project format (array of collections with "children")
     * - BDEngine export format (flat array of display entities)
     * - Wrapped formats ("parts", "entities" keys)
     */
    private static void collectParts(JsonElement el, List<DisplayPart> parts, org.joml.Matrix4f parentMatrix, boolean hasMatrixTransform) {
        if (el.isJsonArray()) {
            for (JsonElement child : el.getAsJsonArray()) {
                collectParts(child, parts, parentMatrix, hasMatrixTransform);
            }
        } else if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();

            org.joml.Matrix4f currentMatrix = new org.joml.Matrix4f(parentMatrix);
            boolean currentHasMatrixTransform = hasMatrixTransform;
            // Parse transformation if it's a nested node
            if (obj.has("transforms")) {
                JsonArray arr = obj.getAsJsonArray("transforms");
                if (arr.size() == 16) {
                    org.joml.Matrix4f localMat = matrixFromRowMajorArray(arr);
                    currentMatrix.mul(localMat);
                    currentHasMatrixTransform = true;
                }
            }

            // BDEngine project: collection node with children
            if (obj.has("isCollection") && obj.get("isCollection").getAsBoolean()) {
                if (obj.has("children")) {
                    collectParts(obj.get("children"), parts, currentMatrix, currentHasMatrixTransform);
                }
                return;
            }

            // Wrapped export format
            if (obj.has("parts")) {
                collectParts(obj.get("parts"), parts, currentMatrix, currentHasMatrixTransform);
                return;
            }
            if (obj.has("entities")) {
                collectParts(obj.get("entities"), parts, currentMatrix, currentHasMatrixTransform);
                return;
            }

            // Actual display element
            DisplayPart part = parsePart(obj, currentMatrix, currentHasMatrixTransform);
            if (part != null) {
                parts.add(part);
            }
        }
    }

    /**
     * Load a model from a JSON file on disk.
     *
     * @param modelId  Unique ID for the resulting model
     * @param file     The JSON file
     * @return A DisplayModel ready to spawn
     */
    public static DisplayModel fromFile(String modelId, File file) {
        try (Reader reader = new FileReader(file)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[8192];
            int read;
            while ((read = reader.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
            return fromJson(modelId, sb.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load model from file: " + file.getAbsolutePath(), e);
        }
    }

    // ── Programmatic builder ─────────────────────────

    /**
     * Build a simple single-block display model programmatically.
     */
    public static DisplayModel singleBlock(String modelId, Material material) {
        return new DisplayModel(modelId, DisplayPart.block(material));
    }

    /**
     * Build a simple single-item display model programmatically.
     */
    public static DisplayModel singleItem(String modelId, Material material) {
        return new DisplayModel(modelId, DisplayPart.item(material));
    }

    /**
     * Build a simple rotating item showcase.
     */
    public static DisplayModel rotatingItem(String modelId, Material material, float degreesPerTick) {
        DisplayModel model = new DisplayModel(modelId, DisplayPart.item(material));
        model.setAnimation(ModelAnimation.rotateY(degreesPerTick));
        return model;
    }

    // ── Internal Parsing ─────────────────────────────

    private static DisplayPart parsePart(JsonObject obj, org.joml.Matrix4f globalMat, boolean hasMatrixTransform) {
        String type;
        if (obj.has("type")) {
            type = obj.get("type").getAsString().toLowerCase();
        } else {
            // BDStudio project files use boolean flags
            if (obj.has("isItemDisplay") && obj.get("isItemDisplay").getAsBoolean()) type = "item_display";
            else if (obj.has("isTextDisplay") && obj.get("isTextDisplay").getAsBoolean()) type = "text_display";
            else type = "block_display"; // Default and isBlockDisplay
        }

        DisplayPart part;

        switch (type) {
            case "block_display": {
                // BDEngine export uses 'block', project uses 'name'
                String blockId = getString(obj, "block", getString(obj, "name", "minecraft:stone"));
                if (!blockId.contains(":")) {
                    blockId = "minecraft:" + blockId;
                }
                
                org.bukkit.block.data.BlockData data;
                try {
                    data = org.bukkit.Bukkit.createBlockData(blockId);
                } catch (IllegalArgumentException e) {
                    data = org.bukkit.Bukkit.createBlockData(Material.STONE);
                }
                part = DisplayPart.block(data);
                break;
            }

            case "item_display": {
                String itemId = getString(obj, "item", getString(obj, "name", "minecraft:stone"));
                Material mat = parseMaterial(itemId);
                
                // BDEngine models use custom-textured player heads as building blocks.
                // The tagHead.Value field contains a Base64-encoded Mojang skin profile.
                if (mat == Material.PLAYER_HEAD && obj.has("tagHead")) {
                    ItemStack headItem = createTexturedHead(obj.getAsJsonObject("tagHead"));
                    part = DisplayPart.item(headItem);
                    // BDEngine uses HEAD transform for player heads to render as 3D blocks
                    part.itemTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.NONE);
                } else {
                    part = DisplayPart.item(mat);
                }
                break;
            }

            case "text_display": {
                String text = getString(obj, "text", getString(obj, "name", ""));
                part = DisplayPart.text(text);
                break;
            }

            default:
                return null;
        }

        // Apply transformations
        org.joml.Matrix4f mat = new org.joml.Matrix4f(globalMat);

        Vector3f translation = new Vector3f();
        mat.getTranslation(translation);

        Vector3f scale = new Vector3f();
        mat.getScale(scale);

        // Bukkit natively supports mirroring (negative scales). 
        // JOML getScale() returns absolute scales. We check determinant to restore left-hand mirroring natively.
        if (mat.determinant() < 0) {
            scale.x = -scale.x;
            // Mirroring causes a non-orthogonal matrix. Cancel reflection on the matrix before extracting rotation.
            mat.scale(-1f, 1f, 1f);
        }

        org.joml.Quaternionf leftRot = new org.joml.Quaternionf();
        mat.getUnnormalizedRotation(leftRot);
        leftRot.normalize(); // Must explicitly normalize because matrices with scaling causes distortion

        // Construct final Minecraft Transformation
        org.bukkit.util.Transformation transform = new org.bukkit.util.Transformation(
            translation, leftRot, scale, new org.joml.Quaternionf()
        );

        part.translation(translation).scale(scale).leftRotation(leftRot);
        if (hasMatrixTransform) {
            // BDEngine project files already store the final Minecraft matrix.
            // Keep it intact so non-uniform rotations and baked offsets do not drift.
            part.matrixTransformation(toMinecraftRowMajor(mat));
        }

        // Brightness
        if (obj.has("brightness")) {
            JsonObject br = obj.getAsJsonObject("brightness");
            int block = br.has("block") ? br.get("block").getAsInt() : 15;
            int sky = br.has("sky") ? br.get("sky").getAsInt() : 15;
            part.brightness(block, sky);
        }

        return part;
    }

    private static org.joml.Matrix4f matrixFromRowMajorArray(JsonArray arr) {
        return new org.joml.Matrix4f(
            arr.get(0).getAsFloat(), arr.get(4).getAsFloat(), arr.get(8).getAsFloat(), arr.get(12).getAsFloat(),
            arr.get(1).getAsFloat(), arr.get(5).getAsFloat(), arr.get(9).getAsFloat(), arr.get(13).getAsFloat(),
            arr.get(2).getAsFloat(), arr.get(6).getAsFloat(), arr.get(10).getAsFloat(), arr.get(14).getAsFloat(),
            arr.get(3).getAsFloat(), arr.get(7).getAsFloat(), arr.get(11).getAsFloat(), arr.get(15).getAsFloat()
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
     * Creates a PLAYER_HEAD ItemStack with a custom skin texture from BDEngine's tagHead data.
     * BDEngine stores the Mojang skin profile Base64 in tagHead.Value.
     */
    private static ItemStack createTexturedHead(JsonObject tagHead) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        String textureValue = null;
        
        if (tagHead.has("Value")) {
            textureValue = tagHead.get("Value").getAsString();
        }
        
        if (textureValue != null && !textureValue.isEmpty()) {
            try {
                org.bukkit.inventory.meta.SkullMeta meta = 
                    (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
                if (meta != null) {
                    // Use Paper's PlayerProfile API to set the skin texture
                    com.destroystokyo.paper.profile.PlayerProfile profile = 
                        org.bukkit.Bukkit.createProfile(java.util.UUID.randomUUID(), "");
                    profile.getProperties().add(
                        new com.destroystokyo.paper.profile.ProfileProperty("textures", textureValue)
                    );
                    meta.setPlayerProfile(profile);
                    head.setItemMeta(meta);
                }
            } catch (Exception e) {
                // Graceful fallback: return plain player head if texture fails
                System.out.println("[ModelLoader] Failed to apply head texture: " + e.getMessage());
            }
        }
        return head;
    }

    private static Material parseMaterial(String id) {
        // Strip parameters and namespace
        String cleaned = id.replaceAll("\\[.*?\\]", "").replace("minecraft:", "").toUpperCase();
        try {
            return Material.valueOf(cleaned);
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }

    private static Vector3f parseVector3f(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        if (el.isJsonArray()) {
            JsonArray arr = el.getAsJsonArray();
            return new Vector3f(
                    arr.size() > 0 ? arr.get(0).getAsFloat() : 0,
                    arr.size() > 1 ? arr.get(1).getAsFloat() : 0,
                    arr.size() > 2 ? arr.get(2).getAsFloat() : 0
            );
        } else if (el.isJsonObject()) {
            JsonObject v = el.getAsJsonObject();
            return new Vector3f(
                    v.has("x") ? v.get("x").getAsFloat() : 0,
                    v.has("y") ? v.get("y").getAsFloat() : 0,
                    v.has("z") ? v.get("z").getAsFloat() : 0
            );
        }
        return new Vector3f(0, 0, 0);
    }

    private static Quaternionf parseQuaternion(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        if (el.isJsonArray()) {
            JsonArray arr = el.getAsJsonArray();
            return new Quaternionf(
                    arr.size() > 0 ? arr.get(0).getAsFloat() : 0,
                    arr.size() > 1 ? arr.get(1).getAsFloat() : 0,
                    arr.size() > 2 ? arr.get(2).getAsFloat() : 0,
                    arr.size() > 3 ? arr.get(3).getAsFloat() : 1
            );
        } else if (el.isJsonObject()) {
            JsonObject q = el.getAsJsonObject();
            return new Quaternionf(
                    q.has("x") ? q.get("x").getAsFloat() : 0,
                    q.has("y") ? q.get("y").getAsFloat() : 0,
                    q.has("z") ? q.get("z").getAsFloat() : 0,
                    q.has("w") ? q.get("w").getAsFloat() : 1
            );
        }
        return new Quaternionf();
    }

    private static String getString(JsonObject obj, String key, String defaultVal) {
        return obj.has(key) ? obj.get(key).getAsString() : defaultVal;
    }
}
