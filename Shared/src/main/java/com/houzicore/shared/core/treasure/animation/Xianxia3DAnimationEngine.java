package com.houzicore.shared.core.treasure.animation;

import com.houzicore.shared.core.cosmetic.ui.CosmeticUITemplate;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.DisplayPart;
import com.houzicore.shared.core.displayentity.ModelAnimation;
import com.houzicore.shared.core.treasure.Treasure;
import com.houzicore.shared.core.treasure.TreasureType;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardRarity;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.C;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Xianxia3DAnimationEngine extends Animation {

    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final String QI_PREFIX = "<gradient:#ffcc00:#ff5555><bold>☯ พิธีหลอมโอสถเก้าชั้นฟ้า ☯</bold></gradient>";
    private static final float[] PENTATONIC_PITCHES = { 0.5f, 0.5946f, 0.7071f, 0.8409f, 1.0f, 1.1892f };
    private static final Material[] TEASER_MATERIALS = {
        Material.GHAST_TEAR, Material.MAGMA_CREAM, Material.BLAZE_POWDER, 
        Material.GLOWSTONE_DUST, Material.NETHER_WART, Material.REDSTONE, 
        Material.AMETHYST_SHARD, Material.PRISMARINE_SHARD, Material.DRAGON_BREATH, 
        Material.RABBIT_FOOT, Material.GLOW_BERRIES, Material.HEART_OF_THE_SEA
    };

    private final Treasure _treasure;
    private final DisplayEntityManager _displayEntityManager;
    private final Location _cauldronLoc;
    private final Location _mouthLoc;
    
    private DisplayModel _cauldronModel;
    private TextDisplay _hologramBoard;
    private ItemDisplay _rewardDisplay;
    private ItemDisplay _divineJackpotDisplay;
    
    private final List<TeaserItem> _teaserItems = new ArrayList<>();
    private final Random _random = new Random();
    
    private int _elapsedTicks = 0;
    private int _pentatonicIndex = 0;
    private boolean _rewardGiven = false;
    private RewardData _mainRewardData;

    private static class TeaserItem {
        final ItemDisplay display;
        int age = 0;

        TeaserItem(ItemDisplay display) {
            this.display = display;
        }
    }

    public Xianxia3DAnimationEngine(Treasure treasure, com.houzicore.shared.core.hologram.HologramManager hm, DisplayEntityManager dem) {
        super(treasure);
        _treasure = treasure;
        _displayEntityManager = dem;
        _cauldronLoc = _treasure.getCenterBlock().getLocation().add(0.5, 0.35, 0.5); // visual bottom sits on ground
        _mouthLoc = _cauldronLoc.clone().add(0, 0.8, 0); // Center mouth level (Y+1.15)
    }

    @Override
    protected void tick() {
        Player player = _treasure.getPlayer();
        if (player == null || !player.isOnline()) {
            finish();
            return;
        }

        _elapsedTicks++;

        // 7-Phase Timeline Dispatcher (Total 240 Ticks = 12 Seconds)
        if (_elapsedTicks <= 60) {
            tickPhaseIgnition(_elapsedTicks);
        } else if (_elapsedTicks <= 90) {
            tickPhaseTeasers(_elapsedTicks);
        } else if (_elapsedTicks <= 120) {
            tickPhaseAccelerate(_elapsedTicks);
        } else if (_elapsedTicks <= 140) {
            tickPhaseColorHint(_elapsedTicks);
        } else if (_elapsedTicks <= 190) {
            tickPhaseDramaticPause(_elapsedTicks);
        } else if (_elapsedTicks <= 210) {
            tickPhaseExplosion(_elapsedTicks);
        } else if (_elapsedTicks <= 240) {
            tickPhaseCollection(_elapsedTicks);
        }

        // Trail of Essence: Runs across Phase 1-4 (Ticks 1-140)
        if (_elapsedTicks <= 140) {
            spawnTrailOfEssence(_elapsedTicks);
        }

        // Update active teasers (floating and rotation physics)
        updateTeasers();

        // End of the grand timeline
        if (_elapsedTicks >= 240) {
            finish();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Phase Implementations
    // ────────────────────────────────────────────────────────────────────────

    private void tickPhaseIgnition(int tick) {
        if (tick == 1) {
            // Spawn cauldron model using HEAD transform to snap pivot perfectly to center
            DisplayPart cauldronVisual = DisplayPart.item(Material.CAULDRON)
                    .itemTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.HEAD)
                    .scale(1.15f, 1.15f, 1.15f)
                    .brightness(15, 15);

            _cauldronModel = new DisplayModel("treasure_active_" + _treasure.getCenterBlock().hashCode(),
                    java.util.Collections.singletonList(cauldronVisual));
            _cauldronModel.setAnimation(ModelAnimation.rotateY(2.5f));
            
            // Add interaction hitbox box to detect right-clicks during brewing
            _cauldronModel.addInteractionBox(0.0, -0.35, 0.0, 1.8f, 1.8f);
            
            _displayEntityManager.addModel(_cauldronModel);
            _cauldronModel.spawn(_cauldronLoc);

            Player player = _treasure.getPlayer();
            player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.6f, 0.9f);
            player.playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 0.9f);
        }

        // Spawn fire/smoke particles under the cauldron
        Location bottom = _cauldronLoc.clone().add(0, -0.35, 0);
        bottom.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, bottom, 1, 0.1, 0.05, 0.1, 0.01);
        if (tick % 12 == 0) {
            Player player = _treasure.getPlayer();
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.6f, 0.9f);
            }
        }
    }

    private void tickPhaseTeasers(int tick) {
        Player player = _treasure.getPlayer();
        if (tick == 61) {
            player.playSound(player.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 0.8f);
            _mouthLoc.getWorld().spawnParticle(Particle.RAID_OMEN, _mouthLoc, 40, 0.15, 0.15, 0.15, 0.04);
            if (_cauldronModel != null) {
                _cauldronModel.setAnimation(ModelAnimation.rotateY(10f));
            }
        }

        // Spawn a random Chinese alchemy ingredient every 6 ticks
        if ((tick - 60) % 6 == 0) {
            Material material = TEASER_MATERIALS[_random.nextInt(TEASER_MATERIALS.length)];
            double angle = _random.nextDouble() * Math.PI * 2;
            double radius = 0.2 + _random.nextDouble() * 0.2;
            Location spawnAt = _mouthLoc.clone().add(
                    Math.cos(angle) * radius,
                    0.7 + _random.nextDouble() * 0.2, // Spawn higher up
                    Math.sin(angle) * radius);

            ItemDisplay display = spawnAt.getWorld().spawn(spawnAt, ItemDisplay.class, entity -> {
                entity.setItemStack(new ItemStack(material));
                entity.setItemDisplayTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.GROUND);
                Vector3f scale = new Vector3f(0.33f, 0.33f, 0.33f); // Symmetrical premium scale
                entity.setTransformation(new Transformation(new Vector3f(0, 0, 0), new Quaternionf(), scale, new Quaternionf()));
            });

            _teaserItems.add(new TeaserItem(display));
            player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 1.2f);
        }

        Location bottom = _cauldronLoc.clone().add(0, -0.35, 0);
        bottom.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, bottom, 1, 0.1, 0.05, 0.1, 0.01);
    }

    private void tickPhaseAccelerate(int tick) {
        Player player = _treasure.getPlayer();
        if (tick == 91) {
            player.playSound(player.getLocation(), Sound.BLOCK_MOSS_BREAK, 1.0f, 1.1f);
            _mouthLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, _mouthLoc, 45, 0.2, 0.2, 0.2, 0.06);
            if (_cauldronModel != null) {
                _cauldronModel.setAnimation(ModelAnimation.rotateY(24f));
            }
        }

        // Accelerating note block chimes (Interval: 6 ticks)
        if ((tick - 91) % 6 == 0) {
            float pitch = PENTATONIC_PITCHES[_pentatonicIndex % PENTATONIC_PITCHES.length];
            _pentatonicIndex++;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, pitch);
        }

        spawnTierSpecificParticles(_treasure.getTreasureType(), _cauldronLoc);
    }

    private void tickPhaseColorHint(int tick) {
        Player player = _treasure.getPlayer();
        if (tick == 121) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.1f);
            _mouthLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, _mouthLoc, 50, 0.25, 0.25, 0.25, 0.08);
            if (_cauldronModel != null) {
                _cauldronModel.setAnimation(ModelAnimation.rotateY(42f));
            }
        }

        // Accelerating note block chimes (Interval: 3 ticks)
        if ((tick - 121) % 3 == 0) {
            float pitch = PENTATONIC_PITCHES[_pentatonicIndex % PENTATONIC_PITCHES.length];
            _pentatonicIndex++;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, pitch);
            if (_treasure.getTreasureType() == TreasureType.DIVINE) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, pitch * 1.5f);
            }
        }

        // Bubble color hint particles at the mouth of the cauldron
        Reward mainReward = _treasure.getRewards()[0];
        RewardRarity rarity = mainReward.getRarity();
        switch (rarity) {
            case OTHER, COMMON, UNCOMMON -> 
                _mouthLoc.getWorld().spawnParticle(Particle.CLOUD, _mouthLoc, 4, 0.15, 0.05, 0.15, 0.02);
            case RARE -> 
                _mouthLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, _mouthLoc, 4, 0.15, 0.05, 0.15, 0.05);
            case LEGENDARY -> 
                _mouthLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, _mouthLoc, 6, 0.15, 0.05, 0.15, 0.08);
            case MYTHICAL -> {
                _mouthLoc.getWorld().spawnParticle(Particle.END_ROD, _mouthLoc, 4, 0.15, 0.05, 0.15, 0.05);
                _mouthLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, _mouthLoc, 4, 0.15, 0.05, 0.15, 0.03);
            }
        }
    }

    private void tickPhaseDramaticPause(int tick) {
        Player player = _treasure.getPlayer();
        if (tick == 141) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 0.8f);
        }

        // Purple gas brewing stand pitch ramp phase
        _mouthLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, _mouthLoc, 3, 0.1, 0.1, 0.1, 0.02);
        if ((tick - 141) % 4 == 0) {
            float pitchRamp = 0.7f + ((tick - 141) * 0.023f);
            player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.8f, pitchRamp);
        }
    }

    private void tickPhaseExplosion(int tick) {
        if (tick == 191) {
            // Remove active cauldron model
            if (_cauldronModel != null) {
                _cauldronModel.remove();
                _displayEntityManager.removeModel(_cauldronModel);
                _cauldronModel = null;
            }

            // Grant rewards to account
            grantRewards();

            Reward mainReward = _treasure.getRewards()[0];
            RewardRarity rarity = mainReward.getRarity();
            CosmeticRarity cosmeticRarity = mapRarity(rarity);
            String rColor = CosmeticUITemplate.getRarityHexColor(cosmeticRarity);
            
            String rewardName = _mainRewardData != null 
                    ? org.bukkit.ChatColor.stripColor(_mainRewardData.getFriendlyName())
                    : "Unknown Reward";

            Location floatingLoc = _cauldronLoc.clone().add(0, 1.1, 0); // Float at Y+2.25

            // Explosion particles and soundscapes
            playRarityExplosionEffects(_mouthLoc, rarity);

            // Strike lightning for legendary/mythic
            if (rarity == RewardRarity.LEGENDARY || rarity == RewardRarity.MYTHICAL) {
                _cauldronLoc.getWorld().strikeLightningEffect(_cauldronLoc);
            }

            // Divine Jackpot gold block base
            if (_treasure.getTreasureType() == TreasureType.DIVINE) {
                _divineJackpotDisplay = _mouthLoc.getWorld().spawn(_treasure.getCenterBlock().getLocation().add(0.5, 0.8, 0.5), ItemDisplay.class, entity -> {
                    entity.setItemStack(new ItemStack(Material.GOLD_BLOCK));
                    Vector3f scale = new Vector3f(1.6f, 1.6f, 1.6f);
                    entity.setTransformation(new Transformation(new Vector3f(0f, 0f, 0f), new Quaternionf(), scale, new Quaternionf()));
                });
            }

            // Spawn floating text board
            String colorOpen = rColor.startsWith("#") ? "color:" + rColor : rColor;
            _hologramBoard = _mouthLoc.getWorld().spawn(_mouthLoc.clone().add(0, 1.2, 0), TextDisplay.class, textEnt -> {
                textEnt.text(mm.deserialize("<bold>" + QI_PREFIX + "</bold>\n<bold><" + colorOpen + ">" + rewardName + "</" + colorOpen + "></bold>"));
                textEnt.setBillboard(TextDisplay.Billboard.CENTER);
                textEnt.setShadowed(true);
                textEnt.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            });

            // Announce in chat
            broadcastReward(rewardName, cosmeticRarity, rColor);

            // Spawn floating item display
            _rewardDisplay = floatingLoc.getWorld().spawn(floatingLoc, ItemDisplay.class, entity -> {
                ItemStack stack = _mainRewardData != null ? _mainRewardData.getDisplayItem() : null;
                if (stack == null) {
                    stack = new ItemStack(Material.NETHER_STAR);
                }
                entity.setItemStack(stack);
                entity.setItemDisplayTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.GROUND);
                Vector3f scale = new Vector3f(1.2f, 1.2f, 1.2f);
                entity.setTransformation(new Transformation(new Vector3f(0f, 0f, 0f), new Quaternionf(), scale, new Quaternionf()));
            });
        }

        // Keep item slowly spinning
        if (tick >= 191 && tick <= 210) {
            if (_rewardDisplay != null && _rewardDisplay.isValid()) {
                float angle = (float) Math.toRadians((tick - 191) * 4.0);
                Quaternionf rot = new Quaternionf().rotateY(angle);
                Transformation trans = _rewardDisplay.getTransformation();
                _rewardDisplay.setTransformation(new Transformation(trans.getTranslation(), rot, trans.getScale(), trans.getRightRotation()));
            }
        }
    }

    private void tickPhaseCollection(int tick) {
        Location floatingLoc = _cauldronLoc.clone().add(0, 1.1, 0); // Y+2.25

        if (tick < 235) {
            // Hover in air and spawn rarity aura
            if (_rewardDisplay != null && _rewardDisplay.isValid()) {
                double hover = Math.sin((tick - 210) * 0.15) * 0.15;
                _rewardDisplay.teleport(floatingLoc.clone().add(0, hover, 0));
                
                float angle = (float) Math.toRadians((tick - 191) * 3.0);
                Quaternionf rot = new Quaternionf().rotateY(angle);
                Transformation trans = _rewardDisplay.getTransformation();
                _rewardDisplay.setTransformation(new Transformation(trans.getTranslation(), rot, trans.getScale(), trans.getRightRotation()));
            }

            Reward mainReward = _treasure.getRewards()[0];
            spawnRarityAura(floatingLoc, mainReward.getRarity(), tick);

        } else {
            // LERP towards player's chest (ticks 235-240)
            Player player = _treasure.getPlayer();
            double progress = (tick - 234) / 6.0; // Fast Lerp in 6 ticks

            Location target = player.getLocation().add(0, 1.2, 0);
            Location current = lerp(floatingLoc, target, progress);

            if (_rewardDisplay != null && _rewardDisplay.isValid()) {
                float angle = (float) Math.toRadians((tick - 191) * 15.0);
                Quaternionf rot = new Quaternionf().rotateY(angle);
                
                float currentScale = 1.2f - (float) (1.1f * progress);
                Vector3f scaleVec = new Vector3f(currentScale, currentScale, currentScale);
                
                _rewardDisplay.setTransformation(new Transformation(new Vector3f(0, 0, 0), rot, scaleVec, new Quaternionf()));
                _rewardDisplay.teleport(current);
            }

            current.getWorld().spawnParticle(Particle.GLOW, current, 1, 0.05, 0.05, 0.05, 0.01);

            if (tick == 239) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.5f);
                player.getWorld().spawnParticle(Particle.COMPOSTER, player.getLocation().add(0, 1.0, 0), 10, 0.3, 0.3, 0.3, 0.1);
                
                if (_rewardDisplay != null && _rewardDisplay.isValid()) {
                    _rewardDisplay.remove();
                    _rewardDisplay = null;
                }
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Particles and Sound Helpers
    // ────────────────────────────────────────────────────────────────────────

    private void spawnTrailOfEssence(int tick) {
        Player player = _treasure.getPlayer();
        if (player == null || !player.isOnline()) return;

        int interval = (tick >= 100) ? 2 : 4;
        if (tick % interval == 0) {
            Location start = player.getLocation().add(0, 1.2, 0);
            Location end = _cauldronLoc.clone().add(0, 0.2, 0);
            org.bukkit.util.Vector dir = end.toVector().subtract(start.toVector());
            double dist = dir.length();
            if (dist > 0.1) {
                dir.normalize();
                int steps = 5;
                for (int i = 0; i < steps; i++) {
                    double ratio = (double) i / (double) steps;
                    Location pt = start.clone().add(dir.clone().multiply(dist * ratio));
                    pt.getWorld().spawnParticle(Particle.ENCHANT, pt, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private void spawnTierSpecificParticles(TreasureType tier, Location loc) {
        switch (tier) {
            case OLD -> loc.getWorld().spawnParticle(Particle.CLOUD, loc, 2, 0.1, 0.1, 0.1, 0.01);
            case ANCIENT -> {
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 2, 0.1, 0.05, 0.1, 0.02);
                loc.getWorld().spawnParticle(Particle.CHERRY_LEAVES, loc, 1, 0.2, 0.1, 0.2, 0.01);
            }
            case MYTHICAL -> {
                loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 2, 0.1, 0.1, 0.1, 0.03);
                loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0.1, 0.05, 0.1, 0.01);
            }
            case IMMORTAL -> {
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 3, 0.2, 0.1, 0.2, 0.04);
                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0.1, 0.1, 0.1, 0.02);
            }
            case DIVINE -> {
                loc.getWorld().spawnParticle(Particle.WITCH, loc, 5, 0.3, 0.2, 0.3, 0.05);
                loc.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, loc, 2, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    private void playRarityExplosionEffects(Location loc, RewardRarity rarity) {
        Player player = _treasure.getPlayer();
        if (player == null) return;

        switch (rarity) {
            case OTHER, COMMON, UNCOMMON -> {
                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 10, 0.2, 0.2, 0.2, 0.05);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.2f);
            }
            case RARE -> {
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 20, 0.3, 0.3, 0.3, 0.1);
                loc.getWorld().spawnParticle(Particle.LAVA, loc, 5, 0.2, 0.2, 0.2, 0.05);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
            }
            case LEGENDARY -> {
                loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1);
                loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 60, 0.4, 0.4, 0.4, 0.15);
                loc.getWorld().spawnParticle(Particle.FIREWORK, loc, 25, 0.3, 0.3, 0.3, 0.1);
                
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.0f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.15f);
            }
            case MYTHICAL -> {
                loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1);
                loc.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, loc, 50, 0.5, 0.5, 0.5, 0.2);
                loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 50, 0.4, 0.4, 0.4, 0.15);
                
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.9f);
                player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.2f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }

    private void spawnRarityAura(Location loc, RewardRarity rarity, int tick) {
        switch (rarity) {
            case OTHER, COMMON, UNCOMMON -> {
                if (tick % 8 == 0) {
                    loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0.1, 0.1, 0.1, 0.01);
                }
            }
            case RARE -> {
                if (tick % 4 == 0) {
                    loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 2, 0.15, 0.15, 0.15, 0.02);
                }
            }
            case LEGENDARY -> {
                double angle = tick * 0.15;
                double yOffset = Math.sin(tick * 0.1) * 0.4;
                Location pLoc = loc.clone().add(Math.cos(angle) * 0.5, yOffset, Math.sin(angle) * 0.5);
                loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, pLoc, 1, 0, 0, 0, 0);
            }
            case MYTHICAL -> {
                double angle = tick * 0.2;
                double cos = Math.cos(angle) * 0.6;
                double sin = Math.sin(angle) * 0.6;
                Location pLoc1 = loc.clone().add(cos, sin, 0);
                Location pLoc2 = loc.clone().add(0, cos, sin);
                Location pLoc3 = loc.clone().add(sin, 0, cos);
                loc.getWorld().spawnParticle(Particle.END_ROD, pLoc1, 1, 0, 0, 0, 0);
                loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, pLoc2, 1, 0, 0, 0, 0);
                loc.getWorld().spawnParticle(Particle.GLOW, pLoc3, 1, 0, 0, 0, 0);
            }
        }
    }

    private void updateTeasers() {
        for (int i = _teaserItems.size() - 1; i >= 0; i--) {
            TeaserItem teaser = _teaserItems.get(i);
            teaser.age++;
            
            if (teaser.display == null || !teaser.display.isValid()) {
                _teaserItems.remove(i);
                continue;
            }

            // Descend downward into the cauldron and rotate
            Location current = teaser.display.getLocation().add(0, -0.04, 0);
            current.setYaw(current.getYaw() + 15.0f);
            teaser.display.teleport(current);

            // Dissolve after 15 ticks (approx 0.6 blocks total descent)
            if (teaser.age >= 15) {
                current.getWorld().spawnParticle(Particle.SMOKE, current, 3, 0.1, 0.1, 0.1, 0.01);
                teaser.display.remove();
                _teaserItems.remove(i);
            }
        }
    }

    private Location lerp(Location start, Location end, double fraction) {
        double x = start.getX() + (end.getX() - start.getX()) * fraction;
        double y = start.getY() + (end.getY() - start.getY()) * fraction;
        double z = start.getZ() + (end.getZ() - start.getZ()) * fraction;
        return new Location(start.getWorld(), x, y, z, start.getYaw(), start.getPitch());
    }

    private CosmeticRarity mapRarity(RewardRarity rarity) {
        if (rarity == null) return CosmeticRarity.COMMON;
        return switch (rarity) {
            case OTHER, COMMON -> CosmeticRarity.COMMON;
            case UNCOMMON -> CosmeticRarity.RARE;
            case RARE -> CosmeticRarity.EPIC;
            case LEGENDARY -> CosmeticRarity.LEGENDARY;
            case MYTHICAL -> CosmeticRarity.MYTHIC;
        };
    }

    private void grantRewards() {
        if (_rewardGiven) return;
        _rewardGiven = true;
        Player player = _treasure.getPlayer();
        if (player != null) {
            Reward[] rewards = _treasure.getRewards();
            if (rewards.length > 0) {
                _mainRewardData = rewards[0].giveReward("Treasure", player);
                for (int i = 1; i < rewards.length; i++) {
                    rewards[i].giveReward("Treasure", player);
                }
            }
        }
    }

    private void broadcastReward(String rewardName, CosmeticRarity rarity, String colorCode) {
        Player player = _treasure.getPlayer();
        if (player == null) return;

        boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
        String colorOpen = colorCode.startsWith("#") ? "color:" + colorCode : colorCode;
        String msg = isThai
            ? QI_PREFIX + " <gray>เตาหลอมโอสถปะทุออก! คุณบำเพ็ญเพียรสำเร็จ ได้รับ:</gray> <" + colorOpen + "><bold>[" + rewardName + "]</bold></" + colorOpen + ">"
            : QI_PREFIX + " <gray>The alchemy cauldron bursts! You have refined:</gray> <" + colorOpen + "><bold>[" + rewardName + "]</bold></" + colorOpen + ">";
        player.sendMessage(mm.deserialize(msg));

        if (rarity == CosmeticRarity.EPIC || rarity == CosmeticRarity.LEGENDARY || rarity == CosmeticRarity.MYTHIC) {
            for (Player oPlayer : UtilServer.getPlayers()) {
                boolean oIsThai = com.houzicore.shared.core.lang.LangManager.get().isThai(oPlayer);
                String annMsg = oIsThai
                    ? QI_PREFIX + " <gold>" + player.getName() + "</gold> <gray>ได้บำเพ็ญตบะธรรมทลวงเขตแดนสำเร็จ ได้รับสมบัติล้ำค่า:</gray> <" + colorOpen + "><bold>[" + rewardName + "]</bold></" + colorOpen + ">"
                    : QI_PREFIX + " <gold>" + player.getName() + "</gold> <gray>has broken through their tribulation, refining:</gray> <" + colorOpen + "><bold>[" + rewardName + "]</bold></" + colorOpen + ">";
                oPlayer.sendMessage(mm.deserialize(annMsg));
            }
        }
    }

    private void cleanupEntities() {
        for (TeaserItem item : _teaserItems) {
            if (item.display != null && item.display.isValid()) {
                item.display.remove();
            }
        }
        _teaserItems.clear();

        if (_cauldronModel != null) {
            _cauldronModel.remove();
            _displayEntityManager.removeModel(_cauldronModel);
            _cauldronModel = null;
        }

        if (_hologramBoard != null && _hologramBoard.isValid()) {
            _hologramBoard.remove();
            _hologramBoard = null;
        }

        if (_rewardDisplay != null && _rewardDisplay.isValid()) {
            _rewardDisplay.remove();
            _rewardDisplay = null;
        }

        if (_divineJackpotDisplay != null && _divineJackpotDisplay.isValid()) {
            _divineJackpotDisplay.remove();
            _divineJackpotDisplay = null;
        }
    }

    @Override
    protected void onFinish() {
        cleanupEntities();

        if (!_rewardGiven) {
            grantRewards();
        }
    }
}
