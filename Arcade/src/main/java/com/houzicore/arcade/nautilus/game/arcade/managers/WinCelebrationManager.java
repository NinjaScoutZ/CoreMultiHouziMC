package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.objects.NpcName;
import de.eisi05.npc.api.objects.NpcOption;
import de.eisi05.npc.api.objects.NpcSkin;
import de.eisi05.npc.api.objects.Skin;
import de.eisi05.npc.api.wrapper.objects.WrappedComponent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilText;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

public class WinCelebrationManager implements Listener {

    public static final long CELEBRATION_START_DELAY_TICKS = 0L;
    public static final long SCENE_DURATION_TICKS = 160L;
    public static final long POSTGAME_SUMMARY_DELAY_TICKS = 200L; // End->Dead delay: 200 * 50ms = 10 seconds

    private static final long VIEW_LOCK_TICKS = SCENE_DURATION_TICKS;
    private static final int FLOOR_HALF_WIDTH = 7;
    private static final int FLOOR_DEPTH_FRONT = 4;
    private static final int FLOOR_DEPTH_BACK = 6;
    private static final double VIEWER_FRONT_DISTANCE = 6.35D;
    private static final double VIEWER_HEIGHT_OFFSET = 2.85D;

    private final ArcadeManager Manager;
    private final NautHashMap<UUID, ViewerLockState> lockedViewers = new NautHashMap<UUID, ViewerLockState>();
    private final List<PodiumShowcase> activeShowcases = new ArrayList<PodiumShowcase>();

    public WinCelebrationManager(ArcadeManager manager) {
        Manager = manager;
        Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
    }

    @EventHandler
    public void onGameEnd(GameStateChangeEvent event) {
        if (event.GetState() != GameState.End) {
            return;
        }

        Game game = Manager.GetGame();
        if (game == null) {
            return;
        }

        scheduleLater(() -> beginCelebration(game), CELEBRATION_START_DELAY_TICKS);
    }

    private void beginCelebration(Game expectedGame) {
        Game game = Manager.GetGame();
        if (game == null || game != expectedGame) {
            return;
        }

        List<Player> featuredPlayers = resolveFeaturedPlayers(game);
        if (featuredPlayers.isEmpty()) {
            featuredPlayers.addAll(game.GetPlayers(true));
        }
        if (featuredPlayers.isEmpty()) {
            return;
        }

        clearActiveShowcases();

        for (Player player : UtilServer.getPlayers()) {
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        launchSceneFireworks(featuredPlayers.get(0).getLocation());
        scheduleLater(this::clearActiveShowcases, SCENE_DURATION_TICKS);
    }

    private CelebrationScene buildCelebrationScene(Location anchor) {
        CelebrationScene scene = new CelebrationScene();
        Location center = anchor.clone();
        center.setX(Math.floor(center.getX()) + 0.5);
        center.setZ(Math.floor(center.getZ()) + 0.5);
        double sceneY = Math.max(center.getWorld().getHighestBlockYAt(center) + 6.0, center.getY() + 4.0);
        center.setY(sceneY);
        center.setPitch(0f);
        center.setYaw(180f);
        scene.center = center;

        Map<Block, BlockState> restoreStates = new LinkedHashMap<Block, BlockState>();

        int baseY = center.getBlockY() - 1;
        for (int x = -FLOOR_HALF_WIDTH; x <= FLOOR_HALF_WIDTH; x++) {
            for (int z = -FLOOR_DEPTH_BACK; z <= FLOOR_DEPTH_FRONT; z++) {
                Material material = isFloorBorder(x, z) ? Material.BRICKS : Material.STONE_BRICKS;
                placeTemporaryBlock(restoreStates, center.clone().add(x, baseY - center.getY(), z), material);
            }
        }

        addTrim(restoreStates, center, baseY);

        scene.podiumSpots.add(buildPodium(restoreStates, center, 0, 3, Material.GOLD_BLOCK, "1"));
        scene.podiumSpots.add(buildPodium(restoreStates, center, 3, 2, Material.IRON_BLOCK, "2"));
        scene.podiumSpots.add(buildPodium(restoreStates, center, -3, 1, Material.COPPER_BLOCK, "3"));
        scene.podiumSpots.add(buildPodium(restoreStates, center, -6, 0, Material.EXPOSED_COPPER, "4"));
        scene.podiumSpots.add(buildPodium(restoreStates, center, 6, 0, Material.COAL_BLOCK, "5"));
        
        if (scene.podiumSpots.size() > 1) {
            scene.focus = scene.podiumSpots.get(0).clone().add(0, 0.85, 0); // Focus on winner
        } else {
            scene.focus = center.clone().add(0, 1.75, 0);
        }

        scheduleRestore(restoreStates);
        return scene;
    }

    private boolean isFloorBorder(int x, int z) {
        return Math.abs(x) == FLOOR_HALF_WIDTH || z == FLOOR_DEPTH_FRONT || z == -FLOOR_DEPTH_BACK;
    }

    private void addTrim(Map<Block, BlockState> restoreStates, Location center, int baseY) {
        for (int x = -FLOOR_HALF_WIDTH + 1; x <= FLOOR_HALF_WIDTH - 1; x++) {
            placeTemporarySlab(restoreStates, center.clone().add(x, baseY - center.getY() + 1, -FLOOR_DEPTH_BACK),
                    Slab.Type.TOP);
        }

        for (int x : new int[] { -4, -3, -2, -1, 0, 1, 2, 3, 4 }) {
            placeTemporaryStair(restoreStates, center.clone().add(x, baseY - center.getY() + 1, -2), BlockFace.SOUTH);
        }
    }

    private Location buildPodium(Map<Block, BlockState> restoreStates, Location center, int xOffset, int height,
            Material crownMaterial, String marker) {
        Location podiumBase = center.clone().add(xOffset, -1, 1);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 1; y <= height; y++) {
                    placeTemporaryBlock(restoreStates, podiumBase.clone().add(x, y, z), crownMaterial);
                }
            }
        }
        
        if (height == 0) {
            // For height 0, the podium is just flush with the floor (layer 0)
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    placeTemporaryBlock(restoreStates, podiumBase.clone().add(x, 0, z), crownMaterial);
                }
            }
        }
        placeTemporaryStair(restoreStates, podiumBase.clone().add(0, 1, -2), BlockFace.SOUTH);
        placeTemporarySlab(restoreStates, podiumBase.clone().add(-1, 1, -2), Slab.Type.TOP);
        placeTemporarySlab(restoreStates, podiumBase.clone().add(1, 1, -2), Slab.Type.TOP);

        Location badgeLoc = podiumBase.clone().add(0.5, height + 2.2, 0.5);
        ArmorStand badge = createBannerStand(badgeLoc, marker,
                crownMaterial == Material.GOLD_BLOCK ? NamedTextColor.GOLD : NamedTextColor.YELLOW);
        scheduleLater(() -> {
            if (badge != null && !badge.isDead()) {
                badge.remove();
            }
        }, SCENE_DURATION_TICKS - 10L);

        Location stand = podiumBase.clone().add(0.5, height + 1.05, 0.5);
        stand.setYaw(180f);
        stand.setPitch(0f);
        return stand;
    }

    private void spawnWinnerShowcase(List<Player> featuredPlayers, CelebrationScene scene) {
        for (int i = 0; i < featuredPlayers.size() && i < scene.podiumSpots.size(); i++) {
            Player player = featuredPlayers.get(i);
            if (player == null || !player.isOnline()) {
                continue;
            }

            PodiumShowcase showcase = createWinnerRepresentative(player, i + 1, scene.podiumSpots.get(i));
            if (showcase == null) {
                continue;
            }

            activeShowcases.add(showcase);
        }
    }

    private PodiumShowcase createWinnerRepresentative(Player player, int placement, Location stageLocation) {
        if (player == null || !player.isOnline() || stageLocation.getWorld() == null) {
            return null;
        }

        Location standLoc = stageLocation.clone();
        // NpcApi uses bottom center for location, same as ArmorStand, but player height is slightly taller than armorstand.
        // We will keep it exactly as the stageLocation
        standLoc.setYaw(180f);
        standLoc.setPitch(0f);

        NPC npc = new NPC(standLoc, NpcName.of(WrappedComponent.create(player.getName())));
        
        // Hide name tag since we already have banner
        npc.setOption(NpcOption.HIDE_NAMETAG, true);
        
        // Copy skin
        npc.setOption(NpcOption.SKIN, NpcSkin.of(Skin.fromPlayer(player)));

        // Pose for 3rd place and below
        if (placement >= 3) {
            npc.setOption(NpcOption.POSE, Pose.SITTING);
        }

        // Set equipment
        java.util.Map<EquipmentSlot, ItemStack> equipment = new java.util.HashMap<>();
        
        equipment.put(EquipmentSlot.CHEST, resolveArmorPiece(player.getInventory().getChestplate(), Material.LEATHER_CHESTPLATE, placement));
        equipment.put(EquipmentSlot.LEGS, resolveArmorPiece(player.getInventory().getLeggings(), Material.LEATHER_LEGGINGS, placement));
        equipment.put(EquipmentSlot.FEET, resolveArmorPiece(player.getInventory().getBoots(), Material.LEATHER_BOOTS, placement));
        
        ItemStack mainHand = cloneIfPresent(player.getInventory().getItemInMainHand());
        if (mainHand != null) {
            equipment.put(EquipmentSlot.HAND, mainHand);
        }
        
        ItemStack offHand = cloneIfPresent(player.getInventory().getItemInOffHand());
        if (offHand != null) {
            equipment.put(EquipmentSlot.OFF_HAND, offHand);
        }
        
        npc.setOption(NpcOption.EQUIPMENT, equipment);
        
        npc.setEnabled(true);

        return new PodiumShowcase(npc);
    }

    private ItemStack buildPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack resolveArmorPiece(ItemStack equippedPiece, Material fallbackType, int placement) {
        ItemStack equipped = cloneIfPresent(equippedPiece);
        if (equipped != null) {
            return equipped;
        }

        return createPlacementArmor(fallbackType, placement);
    }

    private ItemStack cloneIfPresent(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }

        return itemStack.clone();
    }

    private ItemStack createPlacementArmor(Material material, int placement) {
        ItemStack item = new ItemStack(material);
        if (!(item.getItemMeta() instanceof LeatherArmorMeta)) {
            return item;
        }

        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(resolvePlacementColor(placement));
        item.setItemMeta(meta);
        return item;
    }

    private Color resolvePlacementColor(int placement) {
        switch (placement) {
            case 1:
                return Color.fromRGB(240, 190, 40); // Gold
            case 2:
                return Color.fromRGB(196, 204, 212); // Iron
            case 3:
                return Color.fromRGB(196, 112, 64); // Copper
            case 4:
                return Color.fromRGB(72, 181, 140); // Emerald/Green
            case 5:
                return Color.fromRGB(60, 60, 60); // Coal/Dark Grey
            default:
                return Color.WHITE;
        }
    }

    private void stageViewersForScene(List<Player> featuredPlayers, List<Player> audience, CelebrationScene scene) {
        Location sharedViewSpot = createCenteredAudienceSpot(scene);

        for (Player player : featuredPlayers) {
            if (player == null || !player.isOnline()) {
                continue;
            }

            lockViewerToScene(player, sharedViewSpot, scene.focus);
        }

        for (Player player : audience) {
            if (player == null || !player.isOnline() || featuredPlayers.contains(player)) {
                continue;
            }

            lockViewerToScene(player, sharedViewSpot, scene.focus);
        }
    }

    private void lockViewerToScene(Player player, Location viewSpot, Location center) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Location lockedView = viewSpot.clone();
        faceLocation(lockedView, center.clone().add(0, 1.75, 0));
        ViewerLockState lockState = new ViewerLockState(player, lockedView);
        lockedViewers.put(player.getUniqueId(), lockState);

        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0f);
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setSpectatorTarget(null);
        }
        player.teleport(lockedView);
        hideLockedViewerFromScene(player);

        for (long delay = 5L; delay < VIEW_LOCK_TICKS; delay += 5L) {
            scheduleLater(() -> {
                if (!player.isOnline()) {
                    return;
                }

                ViewerLockState currentLock = lockedViewers.get(player.getUniqueId());
                if (currentLock == null) {
                    return;
                }

                Location locked = currentLock.lockedView.clone();
                // Allow free looking
                locked.setPitch(player.getLocation().getPitch());
                locked.setYaw(player.getLocation().getYaw());

                player.setVelocity(new Vector(0, 0, 0));
                player.setFallDistance(0f);
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    player.setSpectatorTarget(null);
                }
                player.teleport(locked);
            }, delay);
        }

        scheduleLater(() -> restoreViewer(player), VIEW_LOCK_TICKS);
    }

    private Location createCenteredAudienceSpot(CelebrationScene scene) {
        Location spot = scene.center.clone().add(0, VIEWER_HEIGHT_OFFSET, -VIEWER_FRONT_DISTANCE);
        spot.setYaw(0f);
        spot.setPitch(0f);
        faceLocation(spot, scene.focus);
        return spot;
    }

    private void scheduleWinCelebration(Player player, int placement, Location stageLocation) {
        showWinTitle(player, placement);
        playWinSounds(player);
        spawnPlacementBanner(player, placement, stageLocation);

        com.houzicore.shared.core.gadget.types.Gadget active = Manager.GetCosmeticManager().getGadgetManager()
                .getActive(player, com.houzicore.shared.core.gadget.types.GadgetType.WinEffect);
        if (active instanceof com.houzicore.shared.core.gadget.types.WinEffectGadget) {
            ((com.houzicore.shared.core.gadget.types.WinEffectGadget) active).playEffect(player, stageLocation);
        } else {
            for (int i = 0; i < 5; i++) {
                int finalI = i;
                scheduleLater(() -> {
                    if (stageLocation.getWorld() != null) {
                        launchFirework(stageLocation.clone().add((Math.random() - 0.5) * 3.0, 0,
                                (Math.random() - 0.5) * 2.0), finalI);
                    }
                }, i * 7L + 5L);
            }

            scheduleLater(() -> {
                if (stageLocation.getWorld() != null) {
                    stageLocation.getWorld().spawnParticle(Particle.FIREWORK, stageLocation.clone().add(0, 2.0, 0),
                            80, 1.5, 0.5, 1.5, 0.15);
                }
            }, 10L);
        }
    }

    private void showWinTitle(Player player, int placement) {
        Title.Times times = Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(3500), Duration.ofMillis(800));
        String subtitle = placement == 1 ? "Winner podium showcase!" : "Top finish secured!";
        player.showTitle(Title.title(
                Component.text(getPlacementBadge(placement) + " "
                        + UtilText.toSmallCaps(placement == 1 ? "victory!" : "top finish"),
                        NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text(subtitle, NamedTextColor.YELLOW),
                times));
    }

    private void playWinSounds(Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.0f);
        scheduleLater(() -> {
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1.2f);
            }
        }, 5L);
        scheduleLater(() -> {
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1.4f);
            }
        }, 12L);
    }

    private void launchSceneFireworks(Location center) {
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            scheduleLater(() -> launchFirework(center.clone().add((Math.random() - 0.5) * 10.0, 2.0 + Math.random(),
                    (Math.random() - 0.5) * 6.0), finalI), 10L + (i * 5L));
        }
    }

    private void launchFirework(Location loc, int variant) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(1);

        Color[] colors = { Color.YELLOW, Color.ORANGE, Color.AQUA, Color.LIME, Color.FUCHSIA, Color.WHITE };

        FireworkEffect.Builder builder = FireworkEffect.builder()
                .with(variant % 2 == 0 ? Type.STAR : Type.BALL_LARGE)
                .withColor(colors[variant % colors.length])
                .withFade(Color.WHITE)
                .withFlicker()
                .withTrail();

        meta.addEffect(builder.build());
        fw.setFireworkMeta(meta);
    }

    private List<Player> resolveFeaturedPlayers(Game game) {
        List<Player> ranked = new ArrayList<Player>();
        ranked.addAll(game.GetPlayers(true));

        NautHashMap<Player, java.util.HashMap<String, Integer>> stats = game.GetStats();
        ranked.sort(Comparator.comparingInt((Player player) -> scorePlayer(stats, player)).reversed());

        if (ranked.size() > 5) {
            return new ArrayList<Player>(ranked.subList(0, 5));
        }

        return ranked;
    }

    private int scorePlayer(NautHashMap<Player, java.util.HashMap<String, Integer>> stats, Player player) {
        if (player == null || stats == null || !stats.containsKey(player)) {
            return 0;
        }

        java.util.HashMap<String, Integer> playerStats = stats.get(player);
        int kills = playerStats.getOrDefault("Kills", 0);
        int assists = playerStats.getOrDefault("Assists", 0);
        int deaths = playerStats.getOrDefault("Deaths", 0);
        return (kills * 100) + (assists * 25) - (deaths * 10);
    }

    private void spawnPlacementBanner(Player player, int placement, Location stageLocation) {
        if (player == null || !player.isOnline()) {
            return;
        }

        NamedTextColor labelColor;
        switch (placement) {
            case 1: labelColor = NamedTextColor.GOLD; break;
            case 2: labelColor = NamedTextColor.WHITE; break;
            case 3: labelColor = NamedTextColor.GOLD; break;
            case 4: labelColor = NamedTextColor.GREEN; break;
            case 5: labelColor = NamedTextColor.DARK_GRAY; break;
            default: labelColor = NamedTextColor.YELLOW; break;
        }

        ArmorStand badge = createBannerStand(stageLocation.clone().add(0, 2.5, 0),
                getPlacementBadge(placement) + " " + getPlacementLabel(placement),
                labelColor);
        ArmorStand name = createBannerStand(stageLocation.clone().add(0, 2.2, 0), player.getName(),
                NamedTextColor.WHITE);

        int score = 0;
        Game game = Manager.GetGame();
        if (game != null) {
            score = scorePlayer(game.GetStats(), player);
        }
        ArmorStand stat = createBannerStand(stageLocation.clone().add(0, 1.9, 0), score + " Points", NamedTextColor.AQUA);

        scheduleLater(() -> {
            if (badge != null && !badge.isDead()) {
                badge.remove();
            }
            if (name != null && !name.isDead()) {
                name.remove();
            }
            if (stat != null && !stat.isDead()) {
                stat.remove();
            }
        }, SCENE_DURATION_TICKS - 10L);
    }

    private ArmorStand createBannerStand(Location location, String text, NamedTextColor color) {
        // FIX: Temporarily allow creature spawns so GameFlagManager doesn't cancel our ArmorStand
        Game currentGame = Manager.GetGame();
        boolean wasOverride = false;
        if (currentGame != null) {
            wasOverride = currentGame.CreatureAllowOverride;
            currentGame.CreatureAllowOverride = true;
        }

        ArmorStand stand;
        try {
            stand = location.getWorld().spawn(location, ArmorStand.class);
        } finally {
            if (currentGame != null) {
                currentGame.CreatureAllowOverride = wasOverride;
            }
        }

        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.customName(Component.text(text, color, TextDecoration.BOLD));
        stand.setCustomNameVisible(true);
        return stand;
    }

    private void faceLocation(Location viewer, Location focus) {
        viewer.setDirection(focus.toVector().subtract(viewer.toVector()));
    }

    private void scheduleLater(Runnable action, long delayTicks) {
        Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), action,
                delayTicks);
    }

    private void placeTemporaryBlock(Map<Block, BlockState> restoreStates, Location location, Material material) {
        Block block = location.getBlock();
        restoreStates.computeIfAbsent(block, Block::getState);
        block.setType(material, false);
    }

    private void placeTemporarySlab(Map<Block, BlockState> restoreStates, Location location, Slab.Type slabType) {
        Block block = location.getBlock();
        restoreStates.computeIfAbsent(block, Block::getState);
        block.setType(Material.STONE_BRICK_SLAB, false);
        if (block.getBlockData() instanceof Slab) {
            Slab slab = (Slab) block.getBlockData();
            slab.setType(slabType);
            block.setBlockData(slab, false);
        }
    }

    private void placeTemporaryStair(Map<Block, BlockState> restoreStates, Location location, BlockFace facing) {
        Block block = location.getBlock();
        restoreStates.computeIfAbsent(block, Block::getState);
        block.setType(Material.STONE_BRICK_STAIRS, false);
        if (block.getBlockData() instanceof Stairs) {
            Stairs stairs = (Stairs) block.getBlockData();
            stairs.setFacing(facing);
            stairs.setHalf(org.bukkit.block.data.Bisected.Half.BOTTOM);
            block.setBlockData(stairs, false);
        }
    }

    private void scheduleRestore(Map<Block, BlockState> restoreStates) {
        scheduleLater(() -> {
            for (BlockState state : restoreStates.values()) {
                state.update(true, false);
            }
        }, SCENE_DURATION_TICKS);
    }

    private Collection<Player> getCurrentPlayers() {
        return new ArrayList<Player>(Arrays.asList(UtilServer.getPlayers()));
    }

    private void clearActiveShowcases() {
        if (activeShowcases.isEmpty()) {
            return;
        }

        List<PodiumShowcase> showcases = new ArrayList<PodiumShowcase>(activeShowcases);
        activeShowcases.clear();

        for (PodiumShowcase showcase : showcases) {
            showcase.destroy();
        }
    }

    private String getPlacementBadge(int placement) {
        switch (placement) {
            case 1:
                return "🏆";
            case 2:
                return "✦";
            case 3:
                return "✧";
            default:
                return "•";
        }
    }

    private String getPlacementLabel(int placement) {
        switch (placement) {
            case 1:
                return "MVP";
            case 2:
                return "Runner Up";
            case 3:
                return "Contender";
            case 4:
                return "Top 4";
            case 5:
                return "Top 5";
            default:
                return "Finisher";
        }
    }

    private static class CelebrationScene {
        private Location center;
        private Location focus;
        private final List<Location> podiumSpots = new ArrayList<Location>();
    }

    @EventHandler
    public void onViewerMove(PlayerMoveEvent event) {
        ViewerLockState state = lockedViewers.get(event.getPlayer().getUniqueId());
        if (state == null) {
            return;
        }

        Location locked = state.lockedView.clone();
        if (event.getTo() == null || event.getFrom().distanceSquared(event.getTo()) > 0.0) {
            locked.setYaw(event.getTo().getYaw());
            locked.setPitch(event.getTo().getPitch());
            event.setTo(locked);
        }
    }

    @EventHandler
    public void onViewerQuit(PlayerQuitEvent event) {
        ViewerLockState state = lockedViewers.remove(event.getPlayer().getUniqueId());
        if (state == null) {
            return;
        }

        showLockedViewerToScene(event.getPlayer());
    }

    private void hideLockedViewerFromScene(Player player) {
        for (Player other : UtilServer.getPlayers()) {
            if (other.equals(player)) {
                continue;
            }

            other.hidePlayer(Manager.getPlugin(), player);
            player.hidePlayer(Manager.getPlugin(), other);
        }
    }

    private void showLockedViewerToScene(Player player) {
        for (Player other : UtilServer.getPlayers()) {
            if (other.equals(player)) {
                continue;
            }

            other.showPlayer(Manager.getPlugin(), player);
            player.showPlayer(Manager.getPlugin(), other);
        }
    }

    private void restoreViewer(Player player) {
        // Disabled: Keep players in spectator mode
    }

    private static class ViewerLockState {
        private final Location lockedView;
        private final GameMode originalGameMode;
        private final boolean originalAllowFlight;
        private final boolean originalFlying;
        private final float originalWalkSpeed;
        private final float originalFlySpeed;

        private ViewerLockState(Player player, Location lockedView) {
            this.lockedView = lockedView;
            this.originalGameMode = player.getGameMode();
            this.originalAllowFlight = player.getAllowFlight();
            this.originalFlying = player.isFlying();
            this.originalWalkSpeed = player.getWalkSpeed();
            this.originalFlySpeed = player.getFlySpeed();
        }
    }

    private static class PodiumShowcase {
        private final NPC npc;

        private PodiumShowcase(NPC npc) {
            this.npc = npc;
        }

        private void destroy() {
            if (npc != null) {
                try {
                    npc.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
