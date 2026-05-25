package com.houzicore.shared.core.displayentity.furniture;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.DisplayModel;

/**
 * Handles placing and breaking of BDEngine Furniture models.
 */
public class FurnitureManager implements Listener {

    private final JavaPlugin _plugin;
    private final DisplayEntityManager _displayManager;
    private final NamespacedKey _furnitureKey;
    private final NamespacedKey _functionFurnitureKey;
    private final NamespacedKey _interactKey;
    private final NamespacedKey _seatKey;
    private final NamespacedKey _hitboxKey;
    private final NamespacedKey _baseModelKey;
    private final NamespacedKey _functionNamespaceKey;
    private final NamespacedKey _functionOriginXKey;
    private final NamespacedKey _functionOriginYKey;
    private final NamespacedKey _functionOriginZKey;
    private final FurnitureHitboxConfigManager _hitboxConfigManager;

    public FurnitureManager(JavaPlugin plugin, DisplayEntityManager displayManager) {
        _plugin = plugin;
        _displayManager = displayManager;
        _furnitureKey = new NamespacedKey(plugin, "bde_furniture_id");
        _functionFurnitureKey = new NamespacedKey(plugin, "bde_function_furniture_id");
        _interactKey = new NamespacedKey("houzicore", "bde_interact");
        _seatKey = new NamespacedKey("houzicore", "bde_seat");
        _hitboxKey = new NamespacedKey("houzicore", "bde_hitbox");
        _baseModelKey = new NamespacedKey("houzicore", "base_model_id");
        _functionNamespaceKey = new NamespacedKey("houzicore", "bde_function_namespace");
        _functionOriginXKey = new NamespacedKey("houzicore", "bde_function_origin_x");
        _functionOriginYKey = new NamespacedKey("houzicore", "bde_function_origin_y");
        _functionOriginZKey = new NamespacedKey("houzicore", "bde_function_origin_z");
        _hitboxConfigManager = new FurnitureHitboxConfigManager(displayManager.getRegistry().getModelsDirectory());

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void ensureHitboxConfigDefaults() {
        _hitboxConfigManager.ensureDefaults(_displayManager.getRegistry(), _displayManager.getFunctionRuntime());
    }

    /**
     * Creates a placeable Furniture item for a specific display model.
     */
    public ItemStack createFurnitureItem(String modelId) {
        DisplayModel template = _displayManager.getRegistry().getModel(modelId);
        if (template == null) return null;

        ItemStack item = new ItemStack(resolveIconMaterial(template));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        meta.setDisplayName(com.houzicore.shared.core.lang.LangManager.get().get((Player) null, "furniture.item_name").replace("{0}", modelId));
        List<String> lore = new ArrayList<>();
        lore.add("§8──────────────────────");
        lore.add(com.houzicore.shared.core.lang.LangManager.get().get((Player) null, "furniture.lore_1"));
        lore.add(com.houzicore.shared.core.lang.LangManager.get().get((Player) null, "furniture.lore_2"));
        lore.add("");
        lore.add(com.houzicore.shared.core.lang.LangManager.get().get((Player) null, "furniture.lore_3"));
        lore.add("§8──────────────────────");
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(_furnitureKey, PersistentDataType.STRING, modelId);
        item.setItemMeta(meta);

        return item;
    }

    public ItemStack createFunctionFurnitureItem(String namespace) {
        if (_displayManager.getFunctionRuntime().getPack(namespace).isEmpty()) return null;

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        meta.setDisplayName("§bAnimated Model §8» §f" + namespace);
        List<String> lore = new ArrayList<>();
        lore.add("§8──────────────────────");
        lore.add("§7Right-click a block to place.");
        lore.add("§7This model uses BDEngine function animation.");
        lore.add("");
        lore.add("§eAnimated §7/ §aAuto Loop");
        lore.add("§8──────────────────────");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(_functionFurnitureKey, PersistentDataType.STRING, namespace);
        item.setItemMeta(meta);

        return item;
    }

    private Material resolveIconMaterial(DisplayModel template) {
        for (com.houzicore.shared.core.displayentity.DisplayPart part : template.getParts()) {
            Material material = null;
            if (part.getBlockData() != null) {
                material = part.getBlockData().getMaterial();
            } else if (part.getItemStack() != null) {
                material = part.getItemStack().getType();
            }

            if (material != null && material.isItem() && material != Material.AIR) {
                return material;
            }
        }
        return Material.ARMOR_STAND;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlaceFurniture(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || !hand.hasItemMeta()) return;

        PersistentDataContainer pdc = hand.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(_furnitureKey, PersistentDataType.STRING)
                && !pdc.has(_functionFurnitureKey, PersistentDataType.STRING)) return;

        event.setCancelled(true); // Stop normal armor stand placement

        Block clicked = event.getClickedBlock();
        BlockFace face = event.getBlockFace();
        if (clicked == null) return;

        Location placeLoc = clicked.getLocation().add(face.getDirection()).add(0.5, 0, 0.5);

        float yaw = player.getLocation().getYaw();
        placeLoc.setYaw(yaw);
        placeLoc.setPitch(0);

        if (pdc.has(_functionFurnitureKey, PersistentDataType.STRING)) {
            placeFunctionFurniture(player, hand, pdc.get(_functionFurnitureKey, PersistentDataType.STRING), placeLoc);
            return;
        }

        String modelId = pdc.get(_furnitureKey, PersistentDataType.STRING);
        DisplayModel template = _displayManager.getRegistry().getModel(modelId);
        if (template == null) return;

        // Spawn a unique copy
        String uniqueId = "furn_" + modelId + "_" + System.currentTimeMillis();
        boolean fileBacked = _displayManager.getRegistry().getFileBackedModelIds().contains(modelId);
        FurnitureHitboxProfile profile = fileBacked
                ? _hitboxConfigManager.loadStatic(modelId, FurnitureFootprint.fromModel(template))
                : FurnitureHitboxProfile.defaults(FurnitureFootprint.fromModel(template));
        float placementYaw = profile.shouldRotateVisual() ? snapYaw(yaw, profile.getYawSnapDegrees()) : 0;
        placeLoc.setYaw(placementYaw);
        DisplayModel model = template.copyRotated(uniqueId, placementYaw);

        if (fileBacked && profile.shouldLiftToGround()) {
            liftOriginToGround(placeLoc, FurnitureFootprint.fromModel(model));
        }

        // Determine hitbox strategy:
        // - If a hand-edited hitbox YAML exists on disk → use its explicit config
        // - Otherwise → auto-scan the model geometry for per-part tight hitboxes
        boolean hasCustomHitboxConfig = fileBacked && _hitboxConfigManager.hasCustomFile(modelId);
        if (hasCustomHitboxConfig) {
            applyConfiguredHitboxes(profile, model, placementYaw);
        } else {
            applyFallbackHitboxes(model);
        }

        // Spawn FIRST — auxiliary entities only exist after spawn()!
        model.spawn(placeLoc);
        _displayManager.addModel(model);

        // NOW embed the base model ID into interaction entities for drop-on-break
        for (org.bukkit.entity.Entity aux : model.getAuxiliaryEntities()) {
            PersistentDataContainer auxPdc = aux.getPersistentDataContainer();
            if (auxPdc.has(_interactKey, PersistentDataType.STRING)
                    || auxPdc.has(_seatKey, PersistentDataType.STRING)
                    || auxPdc.has(_hitboxKey, PersistentDataType.STRING)) {
                auxPdc.set(_baseModelKey, PersistentDataType.STRING, modelId);
            }
        }

        // Consume item
        if (player.getGameMode() != GameMode.CREATIVE) {
            hand.setAmount(hand.getAmount() - 1);
        }

        player.playSound(placeLoc, Sound.BLOCK_WOOD_PLACE, 1.0f, 1.0f);
        UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, placeLoc.clone().add(0, 0.5, 0), 0.3f, 0.3f, 0.3f, 0.1f, 10, ViewDist.NORMAL, UtilServer.getPlayers());
    }

    private void placeFunctionFurniture(Player player, ItemStack hand, String namespace, Location placeLoc) {
        FurnitureFootprint footprint = FurnitureFootprint.fromFunction(_displayManager.getFunctionRuntime().estimateFootprint(namespace));
        FurnitureHitboxProfile profile = _hitboxConfigManager.loadFunction(namespace, footprint);
        float placementYaw = profile.shouldRotateVisual() ? snapYaw(placeLoc.getYaw(), profile.getYawSnapDegrees()) : 0;
        Location modelLoc = placeLoc.clone();
        modelLoc.setYaw(placementYaw);
        modelLoc.setPitch(0);
        if (profile.shouldLiftToGround()) {
            liftOriginToGround(modelLoc, footprint);
        }

        boolean spawned = _displayManager.getFunctionRuntime().spawnAndPlayLoop(namespace, modelLoc);
        if (!spawned) {
            player.sendMessage(F.main("Furniture", "§cCould not spawn animated model: " + namespace));
            return;
        }

        String uniqueId = "bdefn_" + namespace + "_" + System.currentTimeMillis();
        if (profile.isInteractionEnabled()) {
            double[] offset = rotateOffset(profile.getInteractionOffsetX(), profile.getInteractionOffsetZ(), placementYaw);
            Interaction interaction = (Interaction) placeLoc.getWorld().spawnEntity(
                    modelLoc.clone().add(offset[0], profile.getInteractionOffsetY(), offset[1]),
                    org.bukkit.entity.EntityType.INTERACTION
            );
            interaction.setInteractionWidth(profile.getInteractionWidth());
            interaction.setInteractionHeight(profile.getInteractionHeight());
            interaction.setResponsive(true);
            interaction.setPersistent(false);
            tagFunctionHelper(interaction, uniqueId, namespace, modelLoc, _interactKey);
        }
        if (profile.isCollisionsEnabled()) {
            for (FurnitureHitboxProfile.Collision collision : profile.getCollisions()) {
                double[] offset = rotateOffset(collision.offsetX(), collision.offsetZ(), placementYaw);
                Shulker shulker = spawnCollisionHelper(modelLoc, offset[0], collision.offsetY(), offset[1], collision.size());
                tagFunctionHelper(shulker, uniqueId, namespace, modelLoc, _hitboxKey);
            }
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            hand.setAmount(hand.getAmount() - 1);
        }

        player.playSound(modelLoc, Sound.BLOCK_WOOD_PLACE, 1.0f, 1.0f);
        UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, modelLoc.clone().add(0, 0.5, 0), 0.3f, 0.3f, 0.3f, 0.1f, 10, ViewDist.NORMAL, UtilServer.getPlayers());
    }

    private void applyFallbackHitboxes(DisplayModel model) {
        if (!model.getInteractions().isEmpty()) return;

        // Auto-scan: compute per-part AABB, merge clusters, produce tight zones
        List<AutoHitboxScanner.InteractionZone> zones = AutoHitboxScanner.scan(model);
        for (AutoHitboxScanner.InteractionZone zone : zones) {
            model.addInteractionBox(zone.offsetX, zone.offsetY, zone.offsetZ, zone.width, zone.height);
        }
    }

    private void applyConfiguredHitboxes(FurnitureHitboxProfile profile, DisplayModel model, float yaw) {
        model.getInteractions().clear();
        model.getHitboxes().clear();

        if (profile.isInteractionEnabled()) {
            double[] offset = rotateOffset(profile.getInteractionOffsetX(), profile.getInteractionOffsetZ(), yaw);
            model.addInteractionBox(
                    offset[0],
                    profile.getInteractionOffsetY(),
                    offset[1],
                    profile.getInteractionWidth(),
                    profile.getInteractionHeight()
            );
        }

        if (profile.isCollisionsEnabled()) {
            for (FurnitureHitboxProfile.Collision collision : profile.getCollisions()) {
                double[] offset = rotateOffset(collision.offsetX(), collision.offsetZ(), yaw);
                model.addSolidHitbox(offset[0], collision.offsetY(), offset[1], collision.size());
            }
        }
    }

    private void liftOriginToGround(Location origin, FurnitureFootprint footprint) {
        double bottomY = footprint.bottomY();
        if (bottomY < 0) {
            origin.add(0, -bottomY, 0);
        }
    }

    private void tagFunctionHelper(Entity helper, String uniqueId, String namespace, Location origin, NamespacedKey helperTypeKey) {
        PersistentDataContainer pdc = helper.getPersistentDataContainer();
        pdc.set(helperTypeKey, PersistentDataType.STRING, uniqueId);
        pdc.set(_baseModelKey, PersistentDataType.STRING, namespace);
        pdc.set(_functionNamespaceKey, PersistentDataType.STRING, namespace);
        pdc.set(_functionOriginXKey, PersistentDataType.DOUBLE, origin.getX());
        pdc.set(_functionOriginYKey, PersistentDataType.DOUBLE, origin.getY());
        pdc.set(_functionOriginZKey, PersistentDataType.DOUBLE, origin.getZ());
    }

    private Shulker spawnCollisionHelper(Location origin, double offsetX, double offsetY, double offsetZ, double size) {
        Shulker shulker = (Shulker) origin.getWorld().spawnEntity(
                origin.clone().add(offsetX, offsetY, offsetZ),
                org.bukkit.entity.EntityType.SHULKER
        );
        shulker.setInvisible(true);
        shulker.setSilent(true);
        shulker.setAI(false);
        shulker.setGravity(false);
        shulker.setInvulnerable(true);
        shulker.setPersistent(false);
        shulker.customName(null);
        shulker.setCustomNameVisible(false);
        if (shulker.getAttribute(org.bukkit.attribute.Attribute.SCALE) != null) {
            shulker.getAttribute(org.bukkit.attribute.Attribute.SCALE).setBaseValue(size);
        }
        return shulker;
    }

    private void removeFunctionHelpers(Entity source, String uniqueId) {
        if (uniqueId == null || source.getWorld() == null) {
            source.remove();
            return;
        }

        for (Entity entity : source.getWorld().getEntities()) {
            PersistentDataContainer pdc = entity.getPersistentDataContainer();
            String interactId = pdc.get(_interactKey, PersistentDataType.STRING);
            String hitboxId = pdc.get(_hitboxKey, PersistentDataType.STRING);
            if (uniqueId.equals(interactId) || uniqueId.equals(hitboxId)) {
                entity.remove();
            }
        }
    }

    private Location readFunctionOrigin(Entity helper) {
        PersistentDataContainer pdc = helper.getPersistentDataContainer();
        Double x = pdc.get(_functionOriginXKey, PersistentDataType.DOUBLE);
        Double y = pdc.get(_functionOriginYKey, PersistentDataType.DOUBLE);
        Double z = pdc.get(_functionOriginZKey, PersistentDataType.DOUBLE);
        if (x == null || y == null || z == null || helper.getWorld() == null) {
            return helper.getLocation();
        }
        return new Location(helper.getWorld(), x, y, z);
    }

    private String getHelperUniqueId(PersistentDataContainer pdc) {
        if (pdc.has(_interactKey, PersistentDataType.STRING)) {
            return pdc.get(_interactKey, PersistentDataType.STRING);
        }
        if (pdc.has(_hitboxKey, PersistentDataType.STRING)) {
            return pdc.get(_hitboxKey, PersistentDataType.STRING);
        }
        return null;
    }

    private static double[] rotateOffset(double x, double z, float yawDegrees) {
        if (Math.abs(yawDegrees) < 0.0001f) {
            return new double[] { x, z };
        }
        double radians = Math.toRadians(-yawDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new double[] {
                x * cos + z * sin,
                -x * sin + z * cos
        };
    }

    private static float snapYaw(float yaw, double snapDegrees) {
        if (snapDegrees <= 0) {
            return yaw;
        }
        return (float) (Math.round(yaw / snapDegrees) * snapDegrees);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Break furniture via left-click (EntityDamageByEntityEvent on Interaction entity).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakFurniture(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Interaction) && !(event.getEntity() instanceof Shulker)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        event.setCancelled(true); // Always cancel damage to furniture helper entities

        handleFurnitureBreak(event.getEntity(), player);
    }

    /**
     * Alternative break path: Interaction entities on some Paper versions
     * fire PlayerInteractAtEntityEvent instead of EntityDamageByEntityEvent.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractBreakFurniture(org.bukkit.event.player.PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;
        // Only break on sneak + right-click to prevent accidental destruction
        if (!event.getPlayer().isSneaking()) return;

        handleFurnitureBreak(interaction, event.getPlayer());
    }

    private void handleFurnitureBreak(Entity furnitureHelper, Player player) {
        PersistentDataContainer pdc = furnitureHelper.getPersistentDataContainer();

        if (!pdc.has(_baseModelKey, PersistentDataType.STRING)) return;

        String baseModelId = pdc.get(_baseModelKey, PersistentDataType.STRING);

        if (pdc.has(_functionNamespaceKey, PersistentDataType.STRING)) {
            String namespace = pdc.get(_functionNamespaceKey, PersistentDataType.STRING);
            String uniqueId = getHelperUniqueId(pdc);
            Location origin = readFunctionOrigin(furnitureHelper);
            _displayManager.getFunctionRuntime().deletePackEntities(namespace, origin);
            removeFunctionHelpers(furnitureHelper, uniqueId);
            if (player.getGameMode() != GameMode.CREATIVE) {
                origin.getWorld().dropItemNaturally(origin, createFunctionFurnitureItem(namespace));
            }
            player.playSound(origin, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.5f);
            return;
        }

        // Find the model this interaction belongs to
        String uniqueId = null;
        if (pdc.has(_interactKey, PersistentDataType.STRING)) {
            uniqueId = pdc.get(_interactKey, PersistentDataType.STRING);
        } else if (pdc.has(_seatKey, PersistentDataType.STRING)) {
            uniqueId = pdc.get(_seatKey, PersistentDataType.STRING);
        } else if (pdc.has(_hitboxKey, PersistentDataType.STRING)) {
            uniqueId = pdc.get(_hitboxKey, PersistentDataType.STRING);
        }

        if (uniqueId == null) return;

        DisplayModel active = _displayManager.getModel(uniqueId);
        if (active != null) {
            // Remove model
            _displayManager.removeModel(uniqueId);

            // Drop the item
            Location dropLoc = furnitureHelper.getLocation();
            if (player.getGameMode() != GameMode.CREATIVE) {
                dropLoc.getWorld().dropItemNaturally(dropLoc, createFurnitureItem(baseModelId));
            }

            player.playSound(dropLoc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.5f);
            UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, dropLoc, 0.5f, 0.5f, 0.5f, 0f, 10, ViewDist.NORMAL, UtilServer.getPlayers());
        }
    }
}

