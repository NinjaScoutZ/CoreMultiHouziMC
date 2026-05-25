package com.houzicore.shared.core.displayentity.furniture;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.joml.Quaternionf;

import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.DisplayModelRegistry;
import com.houzicore.shared.core.displayentity.DisplayPart;
import com.houzicore.shared.core.displayentity.ModelAnimation;

/**
 * Handcrafted programmatic furniture models built from vanilla blocks.
 * <p>
 * Register all models into the DisplayModelRegistry on server boot
 * so they become available via /givefurniture command and the FurnitureManager.
 * <p>
 * Each model is carefully designed with proper scale, rotation and
 * positioning to look like real furniture in-game.
 */
public class FurnitureCatalog {

    private FurnitureCatalog() {} // Static utility

    /**
     * Registers all handcrafted furniture models into the given registry.
     */
    public static void registerAll(DisplayModelRegistry registry) {
        registry.registerModel(oakChair());
        registry.registerModel(oakTable());
        registry.registerModel(lantern());
        registry.registerModel(bookshelf());
        registry.registerModel(flowerPot());
        registry.registerModel(campfire());
        registry.registerModel(streetLamp());
        registry.registerModel(barrel());
        registry.registerModel(weaponRack());
        registry.registerModel(oakBench());

        System.out.println("[FurnitureCatalog] Registered " + 10 + " handcrafted furniture models.");
    }

    // ────────────────────────────────────────────────────────────────
    // 1. OAK CHAIR — เก้าอี้ไม้โอ๊ค
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel oakChair() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Seat (Oak Slab) ──
        parts.add(DisplayPart.centeredBlock(Material.OAK_SLAB)
                .translation(0f, 0.28f, 0f)
                .scale(0.45f, 0.08f, 0.45f));

        // ── Backrest (Oak Trapdoor) ──
        parts.add(DisplayPart.centeredBlock(Material.OAK_TRAPDOOR)
                .translation(0f, 0.52f, -0.18f)
                .scale(0.42f, 0.42f, 0.06f));

        // ── Legs (4x Oak Fence) ──
        float legX = 0.16f, legZ = 0.16f;
        float[][] legPositions = {
            { legX, 0.1f,  legZ},
            {-legX, 0.1f,  legZ},
            { legX, 0.1f, -legZ},
            {-legX, 0.1f, -legZ}
        };
        for (float[] pos : legPositions) {
            parts.add(DisplayPart.centeredBlock(Material.OAK_FENCE)
                    .translation(pos[0], pos[1], pos[2])
                    .scale(0.07f, 0.28f, 0.07f));
        }

        DisplayModel model = new DisplayModel("oak_chair", parts);
        model.addSeat(0, 0.3, 0, 0.5f, 0.5f);
        model.addSolidHitbox(0, 0, 0, 0.5);
        model.addInteractionBox(0, 0.3, 0, 0.6f, 0.8f);
        return model;
    }

    // ────────────────────────────────────────────────────────────────
    // 2. OAK TABLE — โต๊ะไม้โอ๊ค
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel oakTable() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Tabletop (Oak Planks) ──
        parts.add(DisplayPart.centeredBlock(Material.OAK_PLANKS)
                .translation(0f, 0.48f, 0f)
                .scale(0.75f, 0.06f, 0.55f));

        // ── Table edge trim (Dark Oak Slab for contrast) ──
        parts.add(DisplayPart.centeredBlock(Material.DARK_OAK_SLAB)
                .translation(0f, 0.44f, 0f)
                .scale(0.78f, 0.03f, 0.58f));

        // ── Center support (Stripped Oak Log) ──
        parts.add(DisplayPart.centeredBlock(Material.STRIPPED_OAK_LOG)
                .translation(0f, 0.22f, 0f)
                .scale(0.12f, 0.36f, 0.12f));

        // ── Base plate (Oak Slab) ──
        parts.add(DisplayPart.centeredBlock(Material.OAK_SLAB)
                .translation(0f, 0.02f, 0f)
                .scale(0.40f, 0.04f, 0.40f));

        DisplayModel model = new DisplayModel("oak_table", parts);
        model.addSolidHitbox(0, 0, 0, 0.8);
        model.addInteractionBox(0, 0.4, 0, 1.0f, 0.8f);
        return model;
    }

    // ────────────────────────────────────────────────────────────────
    // 3. HANGING LANTERN — โคมไฟแขวน (เรืองแสง)
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel lantern() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Chain (Iron Bars) ──
        parts.add(DisplayPart.centeredBlock(Material.IRON_BARS)
                .translation(0f, 0.7f, 0f)
                .scale(0.06f, 0.35f, 0.06f));

        // ── Lantern body (Shroomlight for glow block) ──
        parts.add(DisplayPart.centeredBlock(Material.SHROOMLIGHT)
                .translation(0f, 0.38f, 0f)
                .scale(0.22f, 0.25f, 0.22f)
                .brightness(15, 15));

        // ── Lantern frame (Dark Oak Trapdoor x4 sides) ──
        // Front
        parts.add(DisplayPart.centeredBlock(Material.DARK_OAK_TRAPDOOR)
                .translation(0f, 0.38f, 0.12f)
                .scale(0.22f, 0.25f, 0.02f)
                .brightness(12, 15));
        // Back
        parts.add(DisplayPart.centeredBlock(Material.DARK_OAK_TRAPDOOR)
                .translation(0f, 0.38f, -0.12f)
                .scale(0.22f, 0.25f, 0.02f)
                .brightness(12, 15));
        // Left
        parts.add(DisplayPart.centeredBlock(Material.DARK_OAK_TRAPDOOR)
                .translation(0.12f, 0.38f, 0f)
                .scale(0.02f, 0.25f, 0.22f)
                .brightness(12, 15));
        // Right
        parts.add(DisplayPart.centeredBlock(Material.DARK_OAK_TRAPDOOR)
                .translation(-0.12f, 0.38f, 0f)
                .scale(0.02f, 0.25f, 0.22f)
                .brightness(12, 15));

        // ── Cap (Iron Block tiny) ──
        parts.add(DisplayPart.centeredBlock(Material.IRON_BLOCK)
                .translation(0f, 0.52f, 0f)
                .scale(0.15f, 0.03f, 0.15f));

        // ── Bottom tip ──
        parts.add(DisplayPart.centeredBlock(Material.IRON_BLOCK)
                .translation(0f, 0.24f, 0f)
                .scale(0.08f, 0.04f, 0.08f));

        DisplayModel model = new DisplayModel("lantern", parts);
        model.addInteractionBox(0, 0.4, 0, 0.5f, 0.8f);
        return model;
    }

    // ────────────────────────────────────────────────────────────────
    // 4. FANCY BOOKSHELF — ชั้นหนังสือประดับ
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel bookshelf() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Back panel (Spruce Planks) ──
        parts.add(DisplayPart.centeredBlock(Material.SPRUCE_PLANKS)
                .translation(0f, 0.5f, -0.22f)
                .scale(0.65f, 1.0f, 0.04f));

        // ── Shelves (3 levels) ──
        float[] shelfHeights = {0.05f, 0.38f, 0.72f};
        for (float h : shelfHeights) {
            parts.add(DisplayPart.centeredBlock(Material.DARK_OAK_SLAB)
                    .translation(0f, h, 0f)
                    .scale(0.65f, 0.05f, 0.35f));
        }

        // ── Top (dark oak) ──
        parts.add(DisplayPart.centeredBlock(Material.DARK_OAK_PLANKS)
                .translation(0f, 1.0f, 0f)
                .scale(0.68f, 0.06f, 0.38f));

        // ── Side panels ──
        parts.add(DisplayPart.centeredBlock(Material.SPRUCE_PLANKS)
                .translation(0.31f, 0.5f, 0f)
                .scale(0.04f, 1.0f, 0.35f));
        parts.add(DisplayPart.centeredBlock(Material.SPRUCE_PLANKS)
                .translation(-0.31f, 0.5f, 0f)
                .scale(0.04f, 1.0f, 0.35f));

        // ── Books (colored wool/concrete as book spines, 2 rows) ──
        Material[] bookColors = {
            Material.RED_WOOL, Material.BLUE_WOOL, Material.GREEN_WOOL,
            Material.BROWN_WOOL, Material.YELLOW_WOOL
        };
        float startX = -0.22f;
        // Bottom shelf books
        for (int i = 0; i < bookColors.length; i++) {
            parts.add(DisplayPart.centeredBlock(bookColors[i])
                    .translation(startX + i * 0.11f, 0.22f, -0.04f)
                    .scale(0.08f, 0.26f, 0.20f));
        }
        // Middle shelf books (different arrangement)
        Material[] bookColors2 = {
            Material.PURPLE_WOOL, Material.ORANGE_WOOL, Material.CYAN_WOOL, Material.WHITE_WOOL
        };
        for (int i = 0; i < bookColors2.length; i++) {
            parts.add(DisplayPart.centeredBlock(bookColors2[i])
                    .translation(startX + i * 0.13f + 0.02f, 0.55f, -0.04f)
                    .scale(0.10f, 0.26f, 0.20f));
        }

        DisplayModel model = new DisplayModel("bookshelf", parts);
        model.addSolidHitbox(0, 0, 0, 0.7);
        model.addInteractionBox(0, 0.5, 0, 0.8f, 1.2f);
        return model;
    }

    // ────────────────────────────────────────────────────────────────
    // 5. FLOWER POT — กระถางดอกไม้
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel flowerPot() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Pot base (Terracotta) ──
        parts.add(DisplayPart.centeredBlock(Material.TERRACOTTA)
                .translation(0f, 0.08f, 0f)
                .scale(0.30f, 0.04f, 0.30f));

        // ── Pot body (Terracotta, wider at top) ──
        parts.add(DisplayPart.centeredBlock(Material.TERRACOTTA)
                .translation(0f, 0.16f, 0f)
                .scale(0.28f, 0.12f, 0.28f));
        parts.add(DisplayPart.centeredBlock(Material.TERRACOTTA)
                .translation(0f, 0.28f, 0f)
                .scale(0.32f, 0.10f, 0.32f));

        // ── Pot rim (Brown Terracotta) ──
        parts.add(DisplayPart.centeredBlock(Material.BROWN_TERRACOTTA)
                .translation(0f, 0.35f, 0f)
                .scale(0.35f, 0.03f, 0.35f));

        // ── Soil (Coarse Dirt) ──
        parts.add(DisplayPart.centeredBlock(Material.COARSE_DIRT)
                .translation(0f, 0.34f, 0f)
                .scale(0.28f, 0.04f, 0.28f));

        // ── Flowers (Azalea Leaves for greenery) ──
        parts.add(DisplayPart.centeredBlock(Material.FLOWERING_AZALEA_LEAVES)
                .translation(0f, 0.50f, 0f)
                .scale(0.38f, 0.28f, 0.38f));

        // ── Small flower accent ──
        parts.add(DisplayPart.centeredBlock(Material.PINK_PETALS)
                .translation(0.05f, 0.62f, 0.05f)
                .scale(0.15f, 0.10f, 0.15f));

        DisplayModel model = new DisplayModel("flower_pot", parts);
        model.addInteractionBox(0, 0.3, 0, 0.5f, 0.7f);
        return model;
    }

    // ────────────────────────────────────────────────────────────────
    // 6. CAMPFIRE SET — กองไฟ (เรืองแสง + หมุน)
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel campfire() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Stone ring ──
        float ringR = 0.3f;
        int stones = 8;
        for (int i = 0; i < stones; i++) {
            double angle = Math.toRadians(i * 360.0 / stones);
            float sx = (float)(Math.cos(angle) * ringR);
            float sz = (float)(Math.sin(angle) * ringR);
            parts.add(DisplayPart.centeredBlock(Material.COBBLESTONE)
                    .translation(sx, 0.04f, sz)
                    .scale(0.12f, 0.08f, 0.12f));
        }

        // ── Logs (crossed) ──
        Quaternionf tilt45 = new Quaternionf().rotateY((float)Math.toRadians(45));
        Quaternionf tilt135 = new Quaternionf().rotateY((float)Math.toRadians(135));

        parts.add(DisplayPart.centeredBlock(Material.SPRUCE_LOG)
                .translation(0f, 0.07f, 0f)
                .scale(0.10f, 0.10f, 0.45f)
                .leftRotation(tilt45));
        parts.add(DisplayPart.centeredBlock(Material.SPRUCE_LOG)
                .translation(0f, 0.07f, 0f)
                .scale(0.10f, 0.10f, 0.45f)
                .leftRotation(tilt135));

        // ── Fire core (Glowstone for glow) ──
        parts.add(DisplayPart.centeredBlock(Material.GLOWSTONE)
                .translation(0f, 0.12f, 0f)
                .scale(0.15f, 0.18f, 0.15f)
                .brightness(15, 15));

        // ── Fire accents (Orange Stained Glass for translucency) ──
        parts.add(DisplayPart.centeredBlock(Material.ORANGE_STAINED_GLASS)
                .translation(0.04f, 0.20f, -0.02f)
                .scale(0.10f, 0.22f, 0.10f)
                .brightness(15, 15));
        parts.add(DisplayPart.centeredBlock(Material.YELLOW_STAINED_GLASS)
                .translation(-0.03f, 0.18f, 0.04f)
                .scale(0.08f, 0.18f, 0.08f)
                .brightness(15, 15));

        DisplayModel model = new DisplayModel("campfire_set", parts);
        model.addInteractionBox(0, 0.2, 0, 0.8f, 0.5f);
        return model;
    }

    // ────────────────────────────────────────────────────────────────
    // 7. STREET LAMP — เสาไฟถนน (สูง + เรืองแสง)
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel streetLamp() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Base plate (Stone Bricks) ──
        parts.add(DisplayPart.centeredBlock(Material.STONE_BRICKS)
                .translation(0f, 0.03f, 0f)
                .scale(0.35f, 0.06f, 0.35f));

        // ── Pole lower (Iron Block) ──
        parts.add(DisplayPart.centeredBlock(Material.IRON_BLOCK)
                .translation(0f, 0.45f, 0f)
                .scale(0.08f, 0.78f, 0.08f));

        // ── Pole upper (thinner) ──
        parts.add(DisplayPart.centeredBlock(Material.IRON_BLOCK)
                .translation(0f, 1.2f, 0f)
                .scale(0.06f, 0.72f, 0.06f));

        // ── Arm (horizontal bracket) ──
        parts.add(DisplayPart.centeredBlock(Material.IRON_BLOCK)
                .translation(0.12f, 1.55f, 0f)
                .scale(0.28f, 0.04f, 0.04f));

        // ── Lamp housing (Sea Lantern for natural glow) ──
        parts.add(DisplayPart.centeredBlock(Material.SEA_LANTERN)
                .translation(0.24f, 1.42f, 0f)
                .scale(0.18f, 0.22f, 0.18f)
                .brightness(15, 15));

        // ── Lamp cap ──
        parts.add(DisplayPart.centeredBlock(Material.IRON_BLOCK)
                .translation(0.24f, 1.55f, 0f)
                .scale(0.22f, 0.03f, 0.22f));

        DisplayModel model = new DisplayModel("street_lamp", parts);
        model.addSolidHitbox(0, 0, 0, 0.4);
        model.addInteractionBox(0, 0.8, 0, 0.6f, 1.8f);
        return model;
    }

    // ────────────────────────────────────────────────────────────────
    // 8. WINE BARREL — ถังไวน์
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel barrel() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Barrel body (Barrel block, laid on its side) ──
        Quaternionf sideRotation = new Quaternionf().rotateZ((float)Math.toRadians(90));

        parts.add(DisplayPart.centeredBlock(Material.BARREL)
                .translation(0f, 0.25f, 0f)
                .scale(0.50f, 0.65f, 0.50f)
                .leftRotation(sideRotation));

        // ── Metal bands (Iron Trapdoor rings, front & back) ──
        parts.add(DisplayPart.centeredBlock(Material.IRON_TRAPDOOR)
                .translation(0f, 0.25f, 0.20f)
                .scale(0.42f, 0.42f, 0.02f));
        parts.add(DisplayPart.centeredBlock(Material.IRON_TRAPDOOR)
                .translation(0f, 0.25f, -0.20f)
                .scale(0.42f, 0.42f, 0.02f));

        // ── Tap (Lever visual) ──
        parts.add(DisplayPart.centeredBlock(Material.DARK_OAK_BUTTON)
                .translation(0f, 0.18f, 0.27f)
                .scale(0.06f, 0.06f, 0.08f));

        // ── Stand blocks ──
        parts.add(DisplayPart.centeredBlock(Material.SPRUCE_SLAB)
                .translation(0.15f, 0.02f, 0f)
                .scale(0.08f, 0.05f, 0.45f));
        parts.add(DisplayPart.centeredBlock(Material.SPRUCE_SLAB)
                .translation(-0.15f, 0.02f, 0f)
                .scale(0.08f, 0.05f, 0.45f));

        DisplayModel model = new DisplayModel("barrel", parts);
        model.addSolidHitbox(0, 0, 0, 0.6);
        model.addInteractionBox(0, 0.25, 0, 0.7f, 0.6f);
        return model;
    }

    // ────────────────────────────────────────────────────────────────
    // 9. WEAPON RACK — ชั้นวางอาวุธ (ดาบ + ขวาน)
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel weaponRack() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Wall mount (Dark Oak Planks) ──
        parts.add(DisplayPart.centeredBlock(Material.DARK_OAK_PLANKS)
                .translation(0f, 0.5f, -0.02f)
                .scale(0.70f, 0.90f, 0.04f));

        // ── Weapon hooks (Iron fences, 2 pairs) ──
        parts.add(DisplayPart.centeredBlock(Material.IRON_BARS)
                .translation(-0.15f, 0.75f, 0.04f)
                .scale(0.04f, 0.04f, 0.06f));
        parts.add(DisplayPart.centeredBlock(Material.IRON_BARS)
                .translation(0.15f, 0.75f, 0.04f)
                .scale(0.04f, 0.04f, 0.06f));
        parts.add(DisplayPart.centeredBlock(Material.IRON_BARS)
                .translation(-0.15f, 0.35f, 0.04f)
                .scale(0.04f, 0.04f, 0.06f));
        parts.add(DisplayPart.centeredBlock(Material.IRON_BARS)
                .translation(0.15f, 0.35f, 0.04f)
                .scale(0.04f, 0.04f, 0.06f));

        // ── Displayed sword (Diamond Sword item) ──
        Quaternionf swordTilt = new Quaternionf().rotateZ((float)Math.toRadians(45));
        parts.add(DisplayPart.item(Material.DIAMOND_SWORD)
                .translation(0f, 0.72f, 0.08f)
                .scale(0.35f, 0.35f, 0.35f)
                .leftRotation(swordTilt)
                .itemTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED));

        // ── Displayed axe (Iron Axe item) ──
        Quaternionf axeTilt = new Quaternionf().rotateZ((float)Math.toRadians(-30));
        parts.add(DisplayPart.item(Material.IRON_AXE)
                .translation(0f, 0.32f, 0.08f)
                .scale(0.35f, 0.35f, 0.35f)
                .leftRotation(axeTilt)
                .itemTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED));

        // ── Frame border (Spruce fences) ──
        parts.add(DisplayPart.centeredBlock(Material.SPRUCE_FENCE)
                .translation(0f, 0.97f, 0f)
                .scale(0.72f, 0.04f, 0.06f));
        parts.add(DisplayPart.centeredBlock(Material.SPRUCE_FENCE)
                .translation(0f, 0.03f, 0f)
                .scale(0.72f, 0.04f, 0.06f));

        DisplayModel model = new DisplayModel("weapon_rack", parts);
        model.addInteractionBox(0, 0.5, 0, 0.8f, 1.1f);
        return model;
    }

    // ────────────────────────────────────────────────────────────────
    // 10. OAK BENCH — ม้านั่งยาว (2 ที่นั่ง)
    // ────────────────────────────────────────────────────────────────
    public static DisplayModel oakBench() {
        List<DisplayPart> parts = new ArrayList<>();

        // ── Seat plank (long Oak Slab) ──
        parts.add(DisplayPart.centeredBlock(Material.OAK_SLAB)
                .translation(0f, 0.30f, 0f)
                .scale(0.85f, 0.06f, 0.32f));

        // ── Seat trim (Stripped Oak for premium look) ──
        parts.add(DisplayPart.centeredBlock(Material.STRIPPED_OAK_LOG)
                .translation(0f, 0.27f, 0.15f)
                .scale(0.85f, 0.04f, 0.04f));
        parts.add(DisplayPart.centeredBlock(Material.STRIPPED_OAK_LOG)
                .translation(0f, 0.27f, -0.15f)
                .scale(0.85f, 0.04f, 0.04f));

        // ── Backrest (Oak Planks) ──
        parts.add(DisplayPart.centeredBlock(Material.OAK_PLANKS)
                .translation(0f, 0.56f, -0.14f)
                .scale(0.80f, 0.40f, 0.04f));

        // ── Backrest top rail ──
        parts.add(DisplayPart.centeredBlock(Material.STRIPPED_OAK_LOG)
                .translation(0f, 0.78f, -0.14f)
                .scale(0.85f, 0.04f, 0.06f));

        // ── Legs (4x) ──
        float bLegX = 0.35f, bLegZ = 0.08f;
        float[][] benchLegs = {
            { bLegX, 0.12f,  bLegZ},
            {-bLegX, 0.12f,  bLegZ},
            { bLegX, 0.12f, -bLegZ},
            {-bLegX, 0.12f, -bLegZ}
        };
        for (float[] pos : benchLegs) {
            parts.add(DisplayPart.centeredBlock(Material.OAK_FENCE)
                    .translation(pos[0], pos[1], pos[2])
                    .scale(0.06f, 0.24f, 0.06f));
        }

        // ── Armrests (2x side supports) ──
        parts.add(DisplayPart.centeredBlock(Material.OAK_TRAPDOOR)
                .translation(0.38f, 0.42f, 0f)
                .scale(0.06f, 0.20f, 0.28f));
        parts.add(DisplayPart.centeredBlock(Material.OAK_TRAPDOOR)
                .translation(-0.38f, 0.42f, 0f)
                .scale(0.06f, 0.20f, 0.28f));

        DisplayModel model = new DisplayModel("oak_bench", parts);
        model.addSeat(-0.2, 0.3, 0, 0.4f, 0.4f); // Left seat
        model.addSeat(0.2, 0.3, 0, 0.4f, 0.4f);   // Right seat
        model.addSolidHitbox(0, 0, 0, 0.9);
        model.addInteractionBox(0, 0.4, 0, 1.0f, 0.9f);
        return model;
    }
}
