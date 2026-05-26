package com.houzicore.shared.core.treasure.animation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.joml.Vector3f;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.DisplayPart;
import com.houzicore.shared.core.displayentity.ModelAnimation;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardRarity;
import com.houzicore.shared.core.treasure.Treasure;
import com.houzicore.shared.core.treasure.TreasureType;
import com.houzicore.shared.core.lang.LangManager;

public class CarouselAnimation extends Animation {

    private final HologramManager _hologramManager;
    private final DisplayEntityManager _displayEntityManager;
    private final Location _center;
    private final List<Reward> _rewards;
    private final TreasureType _treasureType;
    
    private final List<DisplayModel> _floatingItems = new ArrayList<>();
    private final List<Hologram> _activeHolograms = new ArrayList<>();
    
    // State machine within the animation
    private int _revealIndex = 0;
    private int _grantedCount = 0;
    private int _stateTicks = 0;
    private CarouselState _state = CarouselState.SPAWNING;

    private enum CarouselState {
        SPAWNING,
        SPINNING,
        REVEALING,
        FINISHED
    }

    public CarouselAnimation(Treasure treasure, HologramManager hologramManager, DisplayEntityManager displayEntityManager, List<Reward> rewards) {
        super(treasure);
        _hologramManager = hologramManager;
        _displayEntityManager = displayEntityManager;
        _rewards = rewards;
        _treasureType = treasure.getTreasureType();
        _center = treasure.getCenterBlock().getLocation().add(0.5, 0, 0.5);
    }

    @Override
    protected void onFinish() {
        // Always grant remaining rewards, even if player just disconnected.
        // giveReward() persists to DB; it's safe to call during PlayerQuitEvent.
        Player player = getTreasure().getPlayer();
        if (player != null) {
            grantRemainingRewards(player);
        }

        for (DisplayModel model : _floatingItems) {
            if (model != null) {
                model.remove();
                _displayEntityManager.removeModel(model);
            }
        }
        _floatingItems.clear();

        for (Hologram holo : _activeHolograms) {
            if (holo != null) {
                holo.stop();
            }
        }
        _activeHolograms.clear();
    }

    /**
     * Grants all rewards that haven't been given yet.
     * Called during cleanup (e.g. player disconnect mid-animation).
     */
    public void grantRemainingRewards(Player player) {
        for (int i = _grantedCount; i < _rewards.size(); i++) {
            _rewards.get(i).giveReward("Treasure", player);
        }
        _grantedCount = _rewards.size();
    }

    @Override
    protected void tick() {
        Player player = getTreasure().getPlayer();
        if (player == null || !player.isOnline()) {
            finish();
            return;
        }

        int t = getTicks();
        _stateTicks++;

        switch (_state) {
            case SPAWNING:
                handleSpawning(t);
                break;
            case SPINNING:
                handleSpinning(t);
                break;
            case REVEALING:
                handleRevealing(t);
                break;
            case FINISHED:
                if (_stateTicks > 20) {
                    finish(); // 1 second after last reveal
                }
                break;
        }
    }

    // ── 2018 Mineplex-style constants ──
    // All items use the same uniform scale so every reward looks identical in size
    private static final float ITEM_SCALE = 0.8f;
    private static final double CAROUSEL_RADIUS = 2.5;
    private static final double ITEM_Y_OFFSET = 1.5;

    // ── Tier-based color palettes ──
    // Each tier has its own pair of colors for DUST_COLOR_TRANSITION
    private org.bukkit.Color[] getTierColors() {
        if (_treasureType == null) return new org.bukkit.Color[]{ org.bukkit.Color.WHITE, org.bukkit.Color.AQUA };
        return switch (_treasureType) {
            case OLD      -> new org.bukkit.Color[]{ org.bukkit.Color.fromRGB(100, 255, 150), org.bukkit.Color.fromRGB(200, 255, 200) }; // Green-White
            case ANCIENT   -> new org.bukkit.Color[]{ org.bukkit.Color.fromRGB(255, 100, 0),  org.bukkit.Color.fromRGB(255, 200, 50)  }; // Fire Orange→Gold
            case LEGENDARY -> new org.bukkit.Color[]{ org.bukkit.Color.fromRGB(255, 215, 0),  org.bukkit.Color.fromRGB(255, 255, 150) }; // Gold→Pale Gold
            case MYTHICAL  -> new org.bukkit.Color[]{ org.bukkit.Color.fromRGB(170, 0, 255),  org.bukkit.Color.fromRGB(80, 180, 255)  }; // Violet→Sky
            case ILLUMINATED -> new org.bukkit.Color[]{ org.bukkit.Color.fromRGB(255, 230, 100), org.bukkit.Color.fromRGB(255, 255, 255) }; // Holy Gold→White
            case IMMORTAL -> new org.bukkit.Color[]{ org.bukkit.Color.fromRGB(0, 198, 255), org.bukkit.Color.fromRGB(0, 114, 255) }; // Light Blue→Blue
            case DIVINE -> new org.bukkit.Color[]{ org.bukkit.Color.fromRGB(255, 255, 255), org.bukkit.Color.fromRGB(255, 215, 0) }; // White→Gold
            default -> new org.bukkit.Color[]{ org.bukkit.Color.WHITE, org.bukkit.Color.AQUA };
        };
    }

    // Per-item accent colors (used for individual item orbits)
    private static final org.bukkit.Color[][] ORBIT_PAIRS = {
        { org.bukkit.Color.fromRGB(0, 255, 200),  org.bukkit.Color.fromRGB(0, 180, 255)  }, // Teal→Blue
        { org.bukkit.Color.fromRGB(255, 170, 0),   org.bukkit.Color.fromRGB(255, 85, 0)   }, // Amber→Orange
        { org.bukkit.Color.fromRGB(170, 0, 255),   org.bukkit.Color.fromRGB(255, 80, 200)  }, // Violet→Pink
        { org.bukkit.Color.fromRGB(0, 200, 255),   org.bukkit.Color.fromRGB(100, 255, 200) }, // Sky→Mint
        { org.bukkit.Color.fromRGB(255, 85, 170),  org.bukkit.Color.fromRGB(255, 200, 100) }, // Pink→Peach
        { org.bukkit.Color.fromRGB(85, 255, 85),   org.bukkit.Color.fromRGB(200, 255, 50)  }, // Lime→Yellow-Green
    };

    private void handleSpawning(int t) {
        if (t == 1) {
            _center.getWorld().playSound(_center, Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.0f, 0.8f);
            UtilParticle.PlayParticle(Particle.PORTAL, _center.clone().add(0, 1, 0), 1.0f, 1.0f, 1.0f, 0.1f, 100, UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
        }

        // Pre-spawn ground ring builds up (ticks 5–25)
        if (t >= 5 && t < 26 && t % 2 == 0) {
            double intensity = (t - 5) / 21.0; // 0→1
            int ringPoints = 16;
            for (int p = 0; p < ringPoints; p++) {
                double a = (p * Math.PI * 2) / ringPoints + (t * 0.15);
                Location ringLoc = _center.clone().add(Math.cos(a) * CAROUSEL_RADIUS * intensity, 0.1, Math.sin(a) * CAROUSEL_RADIUS * intensity);
                ringLoc.getWorld().spawnParticle(Particle.ENCHANT, ringLoc, 1, 0.05, 0.05, 0.05, 0.02);
            }
        }

        if (t == 30) {
            // Spawn the mystery items in a circle — each tier uses its own chest material
            Material mysteryMaterial = _treasureType != null ? _treasureType.getMaterial() : Material.CHEST;
            for (int i = 0; i < _rewards.size(); i++) {
                double angle = (i * Math.PI * 2) / _rewards.size();
                Location spawnLoc = _center.clone().add(Math.cos(angle) * CAROUSEL_RADIUS, ITEM_Y_OFFSET, Math.sin(angle) * CAROUSEL_RADIUS);

                DisplayPart itemPart = DisplayPart.item(new ItemStack(mysteryMaterial))
                        .itemTransform(ItemDisplay.ItemDisplayTransform.GROUND)
                        .scale(new Vector3f(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE));
                
                DisplayModel model = new DisplayModel("carousel_item_" + i + "_" + System.currentTimeMillis(), itemPart);
                model.setAnimation(ModelAnimation.rotateY(5f));
                model.setTeleportDuration(1);
                
                _displayEntityManager.spawnModel(model, spawnLoc);
                _floatingItems.add(model);
                
                spawnLoc.getWorld().playSound(spawnLoc, Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
                // Spawn burst — column of particles rising from ground
                for (double y = 0; y < 2.0; y += 0.3) {
                    Location burstLoc = spawnLoc.clone().add(0, y - ITEM_Y_OFFSET, 0);
                    burstLoc.getWorld().spawnParticle(Particle.PORTAL, burstLoc, 3, 0.1, 0.1, 0.1, 0.08);
                    burstLoc.getWorld().spawnParticle(Particle.END_ROD, burstLoc, 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
            _state = CarouselState.SPINNING;
            _stateTicks = 0;
        }
    }

    private void handleSpinning(int t) {
        // High-fidelity ease-out rotation: starts fast, slows down.
        double p = Math.min(_stateTicks / 80.0, 1.0);
        double easeOut = 1.0 - Math.pow(1.0 - p, 3.0);
        double globalAngleOffset = easeOut * Math.PI * 3; // 1.5 full rotations
        
        for (int i = 0; i < _floatingItems.size(); i++) {
            DisplayModel model = _floatingItems.get(i);
            if (model == null || !model.isSpawned()) continue;

            double angle = (i * Math.PI * 2) / _rewards.size() + globalAngleOffset;
            // Premium bobbing effect
            double yOffset = Math.sin((_stateTicks + (i * 15)) * 0.15) * 0.25;
            
            Location newLoc = _center.clone().add(Math.cos(angle) * CAROUSEL_RADIUS, ITEM_Y_OFFSET + yOffset, Math.sin(angle) * CAROUSEL_RADIUS);
            model.teleport(newLoc);
            
            // ── Color-Transitioning orbit trail (unique per item, smooth shift) ──
            if (_stateTicks % 2 == 0) {
                org.bukkit.Color[] pair = ORBIT_PAIRS[i % ORBIT_PAIRS.length];
                Particle.DustTransition transition = new Particle.DustTransition(pair[0], pair[1], 0.9f);
                // Micro-orbit: 2 transitioning dust particles circling the item
                for (int o = 0; o < 2; o++) {
                    double orbitAngle = (_stateTicks * 0.25) + (o * Math.PI);
                    double orbitR = 0.4;
                    Location dustLoc = newLoc.clone().add(
                        Math.cos(orbitAngle) * orbitR,
                        Math.sin(orbitAngle * 0.7) * 0.2,
                        Math.sin(orbitAngle) * orbitR
                    );
                    dustLoc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                        dustLoc, 1, 0, 0, 0, 0, transition);
                }
                // Sparkly firework trail behind the item
                newLoc.getWorld().spawnParticle(Particle.FIREWORK, newLoc, 1, 0.08, 0.08, 0.08, 0.01);
            }
            
            // ── End Rod tail (every 3 ticks, subtle glowing trail) ──
            if (_stateTicks % 3 == 0) {
                newLoc.getWorld().spawnParticle(Particle.END_ROD, newLoc, 1, 0.05, 0.15, 0.05, 0.005);
            }
        }

        // ── Ground ring — rotating enchantment circle beneath the carousel ──
        drawGroundRing(_stateTicks);

        if (_stateTicks > 80) {
            // After 3 seconds of spinning, start revealing
            _state = CarouselState.REVEALING;
            _stateTicks = -1;
            _revealIndex = 0;
        }
    }

    private void handleRevealing(int t) {
        if (_revealIndex >= _rewards.size()) {
            _state = CarouselState.FINISHED;
            _stateTicks = 0;
            return;
        }

        // Each reveal takes 40 ticks (2 seconds) — snappy and satisfying
        int localTick = _stateTicks % 40;
        DisplayModel activeModel = _floatingItems.get(_revealIndex);
        Reward activeReward = _rewards.get(_revealIndex);

        if (localTick == 0) {
            // 1. Swap the mystery chest with the actual reward IN-PLACE (at its orbital position)
            Player player = getTreasure().getPlayer();
            Location revealLoc = activeModel.getOrigin() != null
                    ? activeModel.getOrigin()
                    : _center.clone().add(0, ITEM_Y_OFFSET, 0);

            RewardData rewardData = activeReward.getFakeRewardData(player);
            DisplayPart revealedPart = DisplayPart.item(rewardData.getDisplayItem())
                    .itemTransform(ItemDisplay.ItemDisplayTransform.GROUND)
                    .scale(new Vector3f(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE));

            activeModel.remove();
            _displayEntityManager.removeModel(activeModel);

            DisplayModel newActiveModel = new DisplayModel(activeModel.getId(), revealedPart);
            newActiveModel.setAnimation(ModelAnimation.rotateY(3f));
            newActiveModel.setTeleportDuration(3);

            _displayEntityManager.spawnModel(newActiveModel, revealLoc);
            _floatingItems.set(_revealIndex, newActiveModel);

            // Burst particles at the reveal point
            revealLoc.getWorld().spawnParticle(Particle.FIREWORK, revealLoc, 8, 0.2, 0.2, 0.2, 0.1);
            for (int s = 0; s < 6; s++) {
                double sa = (s * Math.PI * 2) / 6;
                Location burstLoc = revealLoc.clone().add(Math.cos(sa) * 0.8, 0, Math.sin(sa) * 0.8);
                burstLoc.getWorld().spawnParticle(Particle.SOUL, burstLoc, 1, 0, 0, 0, 0.04);
            }
            revealLoc.getWorld().playSound(revealLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 1.3f);

        } else if (localTick == 5) {
            // 2. Grant the reward and show effects
            RewardData rewardData = activeReward.giveReward("Treasure", getTreasure().getPlayer());
            _grantedCount++;
            RewardRarity rarity = activeReward.getRarity();

            DisplayModel currentModel = _floatingItems.get(_revealIndex);
            Location modelLoc = currentModel.getOrigin();

            playRarityEffects(modelLoc, rarity);
            broadcastLoot(rewardData, rarity);

            // Show Hologram above the item
            String rawName = org.bukkit.ChatColor.stripColor(rewardData.getFriendlyName());
            String legacyColor = rarity != null ? rarity.getColor() : C.cWhite;
            String rarityName = rarity != null ? rarity.getName() : "Reward";

            Hologram holo = new Hologram(_hologramManager, modelLoc.clone().add(0, 0.8, 0),
                    HouziColorParser.parse(legacyColor + rarityName),
                    HouziColorParser.parse(legacyColor + rawName));
            holo.start();
            _activeHolograms.add(holo);

        } else if (localTick > 5 && localTick < 25) {
            // 3. Gentle glow around the revealed item
            DisplayModel currentModel = _floatingItems.get(_revealIndex);
            Location modelLoc = currentModel != null ? currentModel.getOrigin() : null;
            if (modelLoc != null && localTick % 4 == 0) {
                org.bukkit.Color[] tierC = getTierColors();
                Particle.DustTransition glow = new Particle.DustTransition(tierC[0], tierC[1], 1.1f);
                modelLoc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                    modelLoc, 3, 0.3, 0.3, 0.3, 0, glow);
            }

        } else if (localTick == 25) {
            // 4. Fade out — quick ascend and dissolve
            DisplayModel currentModel = _floatingItems.get(_revealIndex);
            currentModel.setTeleportDuration(6);
            currentModel.teleport(currentModel.getOrigin().clone().add(0, 2.5, 0));

            Location origin = currentModel.getOrigin();
            origin.getWorld().spawnParticle(Particle.END_ROD, origin, 3, 0.1, 0.3, 0.1, 0.02);

            if (_revealIndex < _activeHolograms.size()) {
                Hologram holo = _activeHolograms.get(_revealIndex);
                if (holo != null) holo.stop();
            }

        } else if (localTick == 38) {
            // 5. Remove and advance
            DisplayModel currentModel = _floatingItems.get(_revealIndex);
            currentModel.remove();
            _displayEntityManager.removeModel(currentModel);
            _revealIndex++;
        }
        
        // Continue spinning the UNREVEALED items slowly in the background with particles
        double globalAngleOffset = _stateTicks * 0.02;
        for (int i = _revealIndex + 1; i < _rewards.size(); i++) {
            DisplayModel backgroundModel = _floatingItems.get(i);
            if (backgroundModel == null || !backgroundModel.isSpawned()) continue;

            double angle = (i * Math.PI * 2) / _rewards.size() + globalAngleOffset;
            double yOffset = Math.sin((_stateTicks + (i * 10)) * 0.1) * 0.2;
            Location newLoc = _center.clone().add(Math.cos(angle) * CAROUSEL_RADIUS, ITEM_Y_OFFSET + yOffset, Math.sin(angle) * CAROUSEL_RADIUS);
            
            backgroundModel.setTeleportDuration(1);
            backgroundModel.teleport(newLoc);
            
            // Dim transitioning orbit on background items
            if (_stateTicks % 4 == 0) {
                org.bukkit.Color[] pair = ORBIT_PAIRS[i % ORBIT_PAIRS.length];
                Particle.DustTransition dim = new Particle.DustTransition(pair[0], pair[1], 0.6f);
                newLoc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                    newLoc, 1, 0.15, 0.15, 0.15, 0, dim);
            }
        }

        // Keep drawing the ground ring during reveals too
        drawGroundRing(_stateTicks);
    }

    /**
     * Draws a rotating enchantment/dust ring on the ground beneath the carousel.
     * Two interleaved rings spin in opposite directions for a magical effect.
     */
    private void drawGroundRing(int tick) {
        if (tick % 2 != 0) return; // Every other tick to save performance
        
        int ringPoints = 24;
        double innerRadius = CAROUSEL_RADIUS - 0.5;
        double outerRadius = CAROUSEL_RADIUS + 0.3;
        
        for (int p = 0; p < ringPoints; p++) {
            // Outer ring — spins clockwise (enchantment particles)
            double a1 = (p * Math.PI * 2) / ringPoints + (tick * 0.06);
            Location outer = _center.clone().add(Math.cos(a1) * outerRadius, 0.05, Math.sin(a1) * outerRadius);
            outer.getWorld().spawnParticle(Particle.ENCHANT, outer, 1, 0.02, 0.02, 0.02, 0.01);
            
            // Inner ring — tier-colored dust transition, spins counter-clockwise
            if (p % 3 == 0) {
                double a2 = (p * Math.PI * 2) / ringPoints - (tick * 0.04);
                Location inner = _center.clone().add(Math.cos(a2) * innerRadius, 0.08, Math.sin(a2) * innerRadius);
                org.bukkit.Color[] tierC = getTierColors();
                Particle.DustTransition ringTransition = new Particle.DustTransition(tierC[0], tierC[1], 0.7f);
                inner.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                    inner, 1, 0, 0, 0, 0, ringTransition);
            }
        }
    }

    private void playRarityEffects(Location loc, RewardRarity rarity) {
        if (rarity == RewardRarity.COMMON) {
            loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, 1F, 1.2F);
            UtilParticle.PlayParticle(Particle.CLOUD, loc, 0.3F, 0.3F, 0.3F, 0.05F, 10, UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
        } else if (rarity == RewardRarity.UNCOMMON) {
            loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1.2F);
            UtilParticle.PlayParticle(Particle.HAPPY_VILLAGER, loc, 0.4F, 0.4F, 0.4F, 0.1F, 15, UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
        } else if (rarity == RewardRarity.RARE) {
            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.2F);
            UtilParticle.PlayParticle(Particle.ENCHANT, loc, 0.5F, 0.5F, 0.5F, 0.1F, 30, UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
        } else if (rarity == RewardRarity.LEGENDARY) {
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1.15F);
            loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8F, 1.2F);
            loc.getWorld().spawnParticle(Particle.FIREWORK, loc, 20, 0.3, 0.4, 0.3, 0.18);
            loc.getWorld().strikeLightningEffect(loc);
        } else if (rarity == RewardRarity.MYTHICAL) {
            loc.getWorld().playSound(loc, Sound.BLOCK_PORTAL_TRAVEL, 1.3F, 1.2F);
            loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6F, 1.6F);
            loc.getWorld().spawnParticle(Particle.PORTAL, loc, 40, 0.5, 0.5, 0.5, 0.2);
            loc.getWorld().strikeLightningEffect(loc);
        }
    }

    private void broadcastLoot(RewardData rewardData, RewardRarity rarity) {
        if (rarity != RewardRarity.LEGENDARY && rarity != RewardRarity.MYTHICAL && rarity != RewardRarity.RARE) return;
        
        Player player = getTreasure().getPlayer();
        String rarityString = rarity.getName();
        String colorCode = rarity.getColor();
        
        for (Player oPlayer : UtilServer.getPlayers()) {
            oPlayer.sendMessage(F.main("Treasure", LangManager.get().get(oPlayer, "treasure.found_announce")
                    .replace("{0}", F.name(player.getName()))
                    .replace("{1}", colorCode)
                    .replace("{2}", rarityString)
                    .replace("{3}", rewardData.getFriendlyName())));
        }
    }
}
