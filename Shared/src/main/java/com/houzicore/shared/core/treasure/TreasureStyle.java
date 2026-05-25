package com.houzicore.shared.core.treasure;

import org.bukkit.Sound;

import com.houzicore.shared.common.util.UtilParticle.ParticleType;

/**
 * Per-tier visual/sound configuration for Treasure Chests.
 *
 * Tier design intent:
 *   OLD      — subtle, quick, low drama        (1,000 coins)
 *   ANCIENT  — mid-intensity, fire theme       (5,000 coins)
 *   MYTHICAL — full spectacle, portal/soul     (10,000 coins)
 */
public enum TreasureStyle {

    OLD(
        ParticleType.EXPLODE,
        ParticleType.EXPLODE,
        ParticleType.ENCHANTMENT_TABLE,
        Sound.BLOCK_CHEST_OPEN,
        Sound.BLOCK_CHEST_OPEN,
        0,
        14,
        12,
        Sound.BLOCK_CHEST_OPEN,
        1
    ),

    ANCIENT(
        ParticleType.FLAME,
        ParticleType.LAVA,
        ParticleType.MOB_SPELL,
        Sound.BLOCK_LAVA_POP,
        Sound.BLOCK_LAVA_POP,
        5,
        22,
        18,
        Sound.BLOCK_LAVA_POP,
        1
    ),

    LEGENDARY(
        ParticleType.ENCHANTMENT_TABLE,
        ParticleType.RED_DUST,
        ParticleType.WITCH_MAGIC,
        Sound.UI_TOAST_CHALLENGE_COMPLETE,
        Sound.BLOCK_ANVIL_USE,
        7,
        26,
        24,
        Sound.UI_TOAST_CHALLENGE_COMPLETE,
        2
    ),

    MYTHICAL(
        ParticleType.HAPPY_VILLAGER,
        ParticleType.LARGE_EXPLODE,
        ParticleType.INSTANT_SPELL,
        Sound.BLOCK_PORTAL_TRAVEL,
        Sound.BLOCK_ENDER_CHEST_OPEN,
        9,
        30,
        30,
        Sound.BLOCK_ENDER_CHEST_OPEN,
        2
    ),

    ILLUMINATED(
        ParticleType.RED_DUST,
        ParticleType.FIREWORKS_SPARK,
        ParticleType.ENCHANTMENT_TABLE,
        Sound.ENTITY_ILLUSIONER_CAST_SPELL,
        Sound.BLOCK_BEACON_ACTIVATE,
        10,
        35,
        40,
        Sound.BLOCK_BEACON_POWER_SELECT,
        3
    );

    private final ParticleType _secondaryParticle;
    private final ParticleType _chestSpawnParticle;
    private final ParticleType _hoverParticle;
    private final Sound _sound;
    private final Sound _chestSpawnSound;
    private final int _spawnHeight;
    private final int _suspenseTicks;
    private final int _openBurstIntensity;
    private final Sound _openImpactSound;
    private final int _trailCount;

    TreasureStyle(ParticleType secondaryParticle, ParticleType chestSpawnParticle,
                  ParticleType hoverParticle, Sound sound, Sound chestSpawnSound,
                  int spawnHeight, int suspenseTicks, int openBurstIntensity,
                  Sound openImpactSound, int trailCount) {
        _secondaryParticle  = secondaryParticle;
        _chestSpawnParticle = chestSpawnParticle;
        _hoverParticle      = hoverParticle;
        _sound              = sound;
        _chestSpawnSound    = chestSpawnSound;
        _spawnHeight        = spawnHeight;
        _suspenseTicks      = suspenseTicks;
        _openBurstIntensity = openBurstIntensity;
        _openImpactSound    = openImpactSound;
        _trailCount         = trailCount;
    }

    public ParticleType getChestSpawnParticle()  { return _chestSpawnParticle; }
    public Sound        getChestSpawnSound()      { return _chestSpawnSound; }
    public ParticleType getHoverParticle()        { return _hoverParticle; }
    public ParticleType getSecondaryParticle()    { return _secondaryParticle; }
    public Sound        getSound()                { return _sound; }

    /** Height (blocks) from which the chest ArmorStand falls. 0 = instant ground pop. */
    public int  getSpawnHeight()          { return _spawnHeight; }
    /** Duration of pre-open suspense phase in ticks. */
    public int  getSuspenseTicks()        { return _suspenseTicks; }
    /** Fibonacci-sphere nova particle count at chest open (perf-capped ≤200). */
    public int  getOpenBurstIntensity()   { return _openBurstIntensity; }
    /** Primary sound played the instant the chest pops open. */
    public Sound getOpenImpactSound()     { return _openImpactSound; }
    /** Number of vortex spiral arms (arms = trailCount × 2) and fall-trail density. */
    public int  getTrailCount()           { return _trailCount; }
}
