package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek;

import com.houzicore.arcade.nautilus.game.arcade.game.modules.CompassModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime.PropRushKitLoadoutService;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.List;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.object.ObjectContents;

import com.houzicore.shared.common.util.UtilAction;

import org.bukkit.attribute.Attribute;
import org.bukkit.EntityEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextTop;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.chat.PlayerHeadUtil;
import com.houzicore.shared.core.chat.SpriteUtil;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.visibility.VisibilityManager;
//import com.houzicore.shared.combat.DeathMessageType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.TeamGame;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam.PlayerState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.CreatureForm;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.Form;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.*;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.NullKit;
import com.houzicore.arcade.nautilus.game.arcade.stats.BadHiderStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.HunterKillerStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.HunterOfTheYearStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.MeowStatTracker;
import com.houzicore.shared.core.lang.LangManager;

@SuppressWarnings("deprecation")
public class HideSeek extends TeamGame
{
    public static class MeowEvent extends PlayerEvent
    {
        private static final HandlerList handlers = new HandlerList();

        public static HandlerList getHandlerList()
        {
            return handlers;
        }

        @Override
        public HandlerList getHandlers()
        {
            return getHandlerList();
        }

        public MeowEvent(Player who)
        {
            super(who);
        }
    }

    public static class PlayerChangeFormEvent extends PlayerEvent
    {
        private static final HandlerList handlers = new HandlerList();

        public static HandlerList getHandlerList()
        {
            return handlers;
        }

        @Override
        public HandlerList getHandlers()
        {
            return getHandlerList();
        }

        private final Form _form;

        public PlayerChangeFormEvent(Player who, Form form)
        {
            super(who);

            _form = form;
        }

        public Form getForm()
        {
            return _form;
        }
    }

    public static class PlayerSolidifyEvent extends PlayerEvent
    {
        private static final HandlerList handlers = new HandlerList();

        public static HandlerList getHandlerList()
        {
            return handlers;
        }

        @Override
        public HandlerList getHandlers()
        {
            return getHandlerList();
        }

        public PlayerSolidifyEvent(Player who)
        {
            super(who);
        }
    }

    public static class WardenSentry
    {
        private final Player owner;
        private final Location location;
        private long expireAt;
        private long nextPulseAt;

        public WardenSentry(Player owner, Location location, long expireAt, long nextPulseAt)
        {
            this.owner = owner;
            this.location = location;
            this.expireAt = expireAt;
            this.nextPulseAt = nextPulseAt;
        }
    }

    private GameTeam _hiders;
    private GameTeam _seekers;

    public enum Phase { PREP, HUNT, PANIC, CHAOS }
    private enum RoundModifier { NONE, BLACKOUT, HEAVY_PROPS, DOUBLE_UTILITY }
    private Phase _phase = Phase.PREP;
    private RoundModifier _roundModifier = RoundModifier.NONE;
    private long _phaseStartTime = 0;
    
    private long _prepTime = 25000;
    private long _huntTime = 125000;
    private long _panicTime = 90000;
    private long _chaosTime = 120000;

    private HashMap<Player, Form> _forms = new HashMap<Player, Form>();
    private HashMap<Creature, Location> _mobs = new HashMap<Creature, Location>();
    
    private HashMap<Player, Double> _survivalPoints = new HashMap<Player, Double>();
    private HashMap<Player, Float> _nerve = new HashMap<Player, Float>();
    public HashMap<org.bukkit.block.Block, Long> _decoys = new HashMap<>();
    public HashMap<org.bukkit.entity.LivingEntity, Long> _decoyMobs = new HashMap<>();
    private int _lastCountdown = -1;

    // Premium Rework Fields
    private long _gameStartTime = 0;
    private HashMap<UUID, Integer> _compassUses = new HashMap<>();
    private HashMap<UUID, Long> _compassCooldown = new HashMap<>();
    private HashMap<UUID, Integer> _dashUses = new HashMap<>();
    private HashMap<UUID, Long> _dashCooldown = new HashMap<>();
    private HashMap<UUID, Integer> _sixthSenseUses = new HashMap<>();
    private HashMap<UUID, java.util.LinkedList<Location>> _footprints = new HashMap<>();
    private HashMap<UUID, Integer> _perfectDisguiseStacks = new HashMap<>();
    private HashMap<UUID, Location> _disconnectedPlayers = new HashMap<>();
    private HashMap<UUID, org.bukkit.entity.ArmorStand> _disconnectedStatues = new HashMap<>();
    private HashMap<UUID, Integer> _disconnectedPoints = new HashMap<>();
    private HashMap<UUID, String> _spectatorBets = new HashMap<>();
    private HashMap<UUID, Boolean> _betPlaced = new HashMap<>();
    private long _nextEventTime = 0;
    private HashMap<UUID, Long> _rejoinInvulUntil = new HashMap<>();
    private HashMap<UUID, Long> _hiderCaughtTime = new HashMap<>();
    private HashMap<UUID, Integer> _hunterKills = new HashMap<>();
    private HashMap<UUID, Integer> _closeCalls = new HashMap<>();
    private HashMap<UUID, String> _hiderMaxGrade = new HashMap<>();
    private int _initialHiderCount = 0;
    private HashMap<org.bukkit.entity.Item, Long> _mysteryBoxes = new HashMap<>();
    private int _nerveTicks = 0;
    
    // AFK System
    private HashMap<Player, Location> _lastHiderBlockLoc = new HashMap<>();
    private HashMap<Player, Long> _lastHiderMoveTime = new HashMap<>();
    private HashMap<Player, Long> _afkFlareRecharge = new HashMap<>();
    private static final long AFK_THRESHOLD = 45_000L;
    private static final long AFK_FLARE_COOLDOWN = 30_000L;
    
    // Bonus
    private boolean _lastStandActive = false;
    private HashSet<Player> _adrenalineActive = new HashSet<>();
    
    // Brutal Hit Tracker (Hunter -> Set of Hiders hit)
    private HashMap<Player, HashSet<Player>> _brutalHits = new HashMap<>();

    // Scoring System
    private HashMap<Player, Integer> _points = new HashMap<>();
    private HashMap<Player, Long> _tauntCooldown = new HashMap<>();
    private HashSet<Player> _tauntPending = new HashSet<>();
    private HashMap<Player, Long> _fireworkCooldown = new HashMap<>();
    private static final long TAUNT_COOLDOWN_MS = 30_000L;
    private static final long FIREWORK_COOLDOWN_MS = 20_000L;
    private static final long SURVIVAL_TICK_MS = 30_000L;
    private HashMap<Player, Long> _lastSurvivalPoints = new HashMap<>();

    private ArrayList<Material> _allowedBlocks;
    private ArrayList<Material> _rareBlocks;
    private ArrayList<Material> _tinyBlocks;
    private ArrayList<EntityType> _allowedEnts;

    private java.util.HashMap<String, Long> _hitCooldowns = new java.util.HashMap<>();
    private java.util.HashSet<java.util.UUID> _morphGuards = new java.util.HashSet<>();

    // Prop Rush - Scrap Objective
    private HashMap<org.bukkit.entity.Item, Long> _scrapItems = new HashMap<>();
    private long _lastScrapSpawn = 0;

    // Prop Rush - Exorcist trace tracking
    public static final long EXORCIST_TRACE_WINDOW_MS = 12000L;
    public static final double EXORCIST_PURGE_RADIUS = 8.0;
    public static final double EXORCIST_REVEAL_RADIUS = 12.0;
    public HashMap<Player, Long> _recentHiderSkillUse = new HashMap<>();

    // Prop Rush - Remaining kit wave state
    private static final long BOMB_BUG_TRAP_DURATION_MS = 12000L;
    private static final long MIMIC_ILLUSION_DURATION_MS = 8000L;
    private static final long WARDEN_SENTRY_DURATION_MS = 30000L;
    private static final long WARDEN_SENTRY_PULSE_MS = 3000L;
    private static final double WARDEN_SENTRY_RADIUS = 6.5;
    public HashMap<Block, Long> _bombBugBlocks = new HashMap<>();
    public HashMap<org.bukkit.entity.LivingEntity, Long> _bombBugMobs = new HashMap<>();
    public ArrayList<WardenSentry> _wardenSentries = new ArrayList<>();

    // Prop Rush - Preview gap wave
    private static final long PERFECT_PLACEMENT_WINDOW_MS = 20000L;
    private static final double PERFECT_PLACEMENT_MOVE_BUDGET = 1.15;
    private static final long TERMINAL_CHANNEL_MS = 4000L;
    private static final long TERMINAL_DISRUPTION_MS = 15000L;
    private static final long TERMINAL_BLIND_MS = 6000L;
    private static final long HUNTER_RELAY_COOLDOWN_MS = 25000L;
    private static final long PANIC_JUMP_WINDOW_MS = 3000L;
    private static final int PANIC_JUMP_HIT_THRESHOLD = 2;
    private HashMap<Player, Location> _prepAnchors = new HashMap<>();
    private HashSet<Player> _perfectPlacementBonus = new HashSet<>();
    private HashMap<Player, Integer> _panicJumpHits = new HashMap<>();
    private HashMap<Player, Long> _panicJumpWindow = new HashMap<>();
    private ArrayList<Location> _terminalLocations = new ArrayList<>();
    private HashMap<Player, Location> _terminalChannel = new HashMap<>();
    private HashMap<Player, Long> _terminalChannelUntil = new HashMap<>();
    private HashSet<String> _usedTerminals = new HashSet<>();
    public long _terminalDisruptionUntil = 0;
    private long _terminalBlindUntil = 0;
    private long _hunterRelayCooldownUntil = 0;
    private boolean _mapOpen = false;
    private boolean _mapTight = false;

    // Prop Rush - Panic / Danger Zone
    private static final long DANGER_ZONE_INTERVAL_MS = 30000L;
    private static final long DANGER_ZONE_DURATION_MS = 6000L;
    private static final double DANGER_ZONE_RADIUS = 6.5;
    private Location _dangerZoneCenter;
    private long _dangerZoneActiveUntil = 0;
    private long _nextDangerZoneAt = 0;
    private long _nextChaosPulseAt = 0;
    private HashSet<Player> _dangerZoneWarned = new HashSet<>();

    // UpdateEvent-driven delayed actions
    private HashMap<Player, Long> _pendingInitialDisguise = new HashMap<>();
    private HashMap<Player, Long> _pendingHiderItems = new HashMap<>();
    private HashMap<Player, Long> _pendingLastStandSound = new HashMap<>();
    private HashMap<Player, Long> _pendingScannerPulseFeedback = new HashMap<>();
    private HashMap<Player, Boolean> _pendingScannerPulseFound = new HashMap<>();
    private HashMap<Player, Long> _pendingBountyDashCheck = new HashMap<>();
    private HashMap<Player, Long> _pendingHunterRespawnKit = new HashMap<>();
    private HashMap<Player, Long> _pendingTauntReward = new HashMap<>();
    private HashMap<com.houzicore.shared.core.hologram.Hologram, Long> _temporaryHolograms = new HashMap<>();

    public HideSeek(ArcadeManager manager)
    {
        super(manager, GameType.PropRush,

        new Kit[]
            {
                    new KitChameleon(manager),
                    new KitGhost(manager),
                    new KitTrickster(manager),
                    new KitBombBug(manager),
                    new KitLocksmith(manager),
                    new KitMimic(manager),
                    new KitTracker(manager),
                    new KitDestroyer(manager),
                    new KitTrapper(manager),
                    new KitBloodhound(manager),
                    new KitSaboteur(manager),
                    new KitBountyHunter(manager),
                    new KitExorcist(manager),
                    new KitFalconer(manager),
                    new KitWarden(manager)
            },

        // EN
        new String[]
            {
                    com.houzicore.shared.common.util.C.cGray + "Hide as blocks or animals and",
                    com.houzicore.shared.common.util.C.cGray + "survive against the Hunters in",
                    com.houzicore.shared.common.util.C.cGray + "this classic game of Block Hunt!"
            },
        // TH
        new String[]
            {
                    com.houzicore.shared.common.util.C.cGray + "ซ่อนตัวเป็นบล็อกหรือสัตว์ อัปเกรดอาวุธ",
                    com.houzicore.shared.common.util.C.cGray + "เพื่อเอาชีวิตรอดและต่อสู้พลิกสถานการณ์",
                    com.houzicore.shared.common.util.C.cGray + "จากฝั่งคนหา (Hunters)!"
            });

        this.DamageSelf = false;

        // Register Prop Rush Specific Traits
        manager.getTraitManager().registerTrait(new com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits.TraitLightweight());
        manager.getTraitManager().registerTrait(new com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits.TraitSilentFeet());
        manager.getTraitManager().registerTrait(new com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits.TraitBrutalHit());
        manager.getTraitManager().registerTrait(new com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits.TraitUtilityBelt());
        this.DeathOut = false;
        this.HungerSet = 20;
        this.PrepareFreeze = false;
        
        for (Kit kit : GetKits())
        {
            if (kit instanceof KitTracker || kit instanceof KitDestroyer || kit instanceof KitTrapper || 
                kit instanceof KitBloodhound || kit instanceof KitSaboteur || kit instanceof KitBountyHunter ||
                kit instanceof KitExorcist || kit instanceof KitFalconer || kit instanceof KitWarden)
            {
                kit.setDisplayColor(org.bukkit.ChatColor.RED);
            }
            else
            {
                kit.setDisplayColor(org.bukkit.ChatColor.AQUA);
            }
        }

        _allowedBlocks = new ArrayList<Material>();
        _allowedBlocks.add(Material.BOOKSHELF);
        _allowedBlocks.add(Material.CRAFTING_TABLE);
        _allowedBlocks.add(Material.FURNACE);
        _allowedBlocks.add(Material.MELON);
        _allowedBlocks.add(Material.CAULDRON);
        _allowedBlocks.add(Material.HAY_BLOCK);
        
        _rareBlocks = new ArrayList<Material>();
        _rareBlocks.add(Material.TNT);
        _rareBlocks.add(Material.ANVIL);
        _rareBlocks.add(Material.JUKEBOX);
        _rareBlocks.add(Material.BREWING_STAND);
        
        _tinyBlocks = new ArrayList<Material>();
        _tinyBlocks.add(Material.FLOWER_POT);
        _tinyBlocks.add(Material.CAKE);
        _tinyBlocks.add(Material.SKELETON_SKULL);

        _allowedEnts = new ArrayList<EntityType>();
        _allowedEnts.add(EntityType.PIG);
        _allowedEnts.add(EntityType.COW);
        _allowedEnts.add(EntityType.CHICKEN);
        _allowedEnts.add(EntityType.SHEEP);
        _allowedEnts.add(EntityType.CAT);
        _allowedEnts.add(EntityType.RABBIT);
        _allowedEnts.add(EntityType.PARROT);
        _allowedEnts.add(EntityType.FOX);

        Manager.GetExplosion().SetRegenerate(true);
        Manager.GetExplosion().SetTNTSpread(false);

        
        // [WOW] Spectator Compass Refinement (Task 21)
        // Removed CompassModule (Rework: Hunter rely on KitTracker or intuition)
        registerStatTrackers(new HunterKillerStatTracker(this), new MeowStatTracker(this),
                new HunterOfTheYearStatTracker(this), new BadHiderStatTracker(this),
                new com.houzicore.arcade.nautilus.game.arcade.stats.DistanceTraveledStatTracker(this, "DistanceTraveled"),
                new com.houzicore.arcade.nautilus.game.arcade.stats.SurviveLowHealthStatTracker(this),
                new com.houzicore.arcade.nautilus.game.arcade.stats.FireworkTauntStatTracker(this),
                new com.houzicore.arcade.nautilus.game.arcade.stats.FirstBloodStatTracker(this),
                new com.houzicore.arcade.nautilus.game.arcade.stats.CrowdControlStatTracker(this));
        com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.init(manager.getPlugin());
    }





    public Material GetItemEquivilent(Material mat)
    {
        if (mat == Material.CAULDRON)
            return Material.CAULDRON;
        if (mat == Material.FLOWER_POT)
            return Material.FLOWER_POT;
        if (mat == Material.CAKE)
            return Material.CAKE;

        return mat;
    }

    @Override
    public void ParseData()
    {
        int i = 0;

        for (ArrayList<Location> locs : WorldData.GetAllCustomLocs().values())
        {
            for (Location loc : locs)
            {
                if (Math.random() > 0.25)
                    continue;

                if (loc.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR)
                    continue;

                loc.getBlock().setType(Material.AIR);
                i++;
            }
        }


        for (Location loc : WorldData.GetDataLocs("BLACK"))
            loc.getBlock().setType(Material.BARRIER);

    }

    @EventHandler
    public void CustomTeamGeneration(GameStateChangeEvent event)
    {
        if (event.GetState() != GameState.Recruit)
            return;

        if (GetTeamList().isEmpty())
            return;

        // Match teams by their ORIGINAL color assigned by GameManager
        // GameManager maps "BLUE" world data -> ChatColor.AQUA, "RED" -> ChatColor.RED
        // HashMap iteration order is non-deterministic, so we CANNOT use get(0)/get(1)
        for (GameTeam team : GetTeamList())
        {
            if (team.GetColor() == ChatColor.AQUA)
                _hiders = team;
            else if (team.GetColor() == ChatColor.RED)
                _seekers = team;
        }

        // Fallback if map uses different color scheme
        if (_hiders == null) _hiders = GetTeamList().get(0);
        if (_seekers == null) _seekers = GetTeamList().size() > 1 ? GetTeamList().get(1) : GetTeamList().get(0);

        _hiders.SetColor(ChatColor.AQUA);
        _hiders.SetName("Hiders");

        _seekers.SetColor(ChatColor.RED);
        _seekers.SetName("Hunters");

        // Clear stale kit restrictions from GameManager's initial call, then re-apply
        for (GameTeam team : GetTeamList())
            team.GetRestrictedKits().clear();
        RestrictKits();
    }

    @Override
    public void RestrictKits()
    {
        for (Kit kit : GetKits())
        {
            for (GameTeam team : GetTeamList())
            {
                boolean isHunterKit = (kit instanceof KitTracker || kit instanceof KitDestroyer || kit instanceof KitTrapper ||
                        kit instanceof KitBloodhound || kit instanceof KitSaboteur || kit instanceof KitBountyHunter ||
                        kit instanceof KitExorcist || kit instanceof KitFalconer || kit instanceof KitWarden);

                if (team.GetColor() == ChatColor.RED)
                {
                    if (!isHunterKit)
                        team.GetRestrictedKits().add(kit);
                }
                else
                {
                    if (isHunterKit)
                        team.GetRestrictedKits().add(kit);
                }
            }
        }
    }



    // ===================================================================
    // CENTRALIZED REVEAL SYSTEM
    // ===================================================================
    // All "reveal" abilities MUST route through revealHider() so that:
    //  1. The player entity becomes visible to hunters (GLOWING on a hidden
    //     entity has no visual effect — this is the core fix).
    //  2. Solidify state is broken first (pops them out of the fake block).
    //  3. A scheduled task re-conceals the hider and re-applies the BlockDisplay
    //     disguise after the reveal window expires.
    // ===================================================================

    /**
     * Reveals a hider to ALL hunters for durationTicks.
     */
    public void revealHider(Player hider, int durationTicks)
    {
        revealHider(hider, durationTicks, null);
    }

    /**
     * Reveals a hider.
     * @param hider          the hider to reveal
     * @param durationTicks  how long the reveal lasts (ticks; 20 = 1 s)
     * @param specificHunter if non-null, reveal only to this hunter;
     *                       if null, reveal to every seeker
     */
    public void revealHider(Player hider, int durationTicks, Player specificHunter)
    {
        if (hider == null || !hider.isOnline()) return;

        Form form = _forms.get(hider);

        // --- 1. Break solidify so the hider is no longer invisible ---
        if (form instanceof com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm blockForm
                && blockForm.GetBlock() != null)
        {
            blockForm.SolidifyRemove();
        }

        // --- 2. Determine viewers ---
        final java.util.List<Player> viewers;
        if (specificHunter != null)
        {
            viewers = java.util.Collections.singletonList(specificHunter);
        }
        else
        {
            viewers = new java.util.ArrayList<>(_seekers.GetPlayers(true));
        }

        // --- 3. Show player entity to viewers so GLOWING is visible ---
        for (Player hunter : viewers)
        {
            if (hunter.isOnline())
                hunter.showEntity(Manager.getPlugin(), hider);
        }

        // --- 4. Apply GLOWING ---
        hider.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.GLOWING, durationTicks, 0, false, true));

        // --- 5. Reveal beacon particles + sound ---
        UtilParticle.PlayParticle(ParticleType.END_ROD,
                hider.getLocation().add(0, 1, 0),
                0.4f, 0.6f, 0.4f, 0.03f, 18, ViewDist.NORMAL, UtilServer.getPlayers());
        hider.getWorld().playSound(hider.getLocation(),
                Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.6f);

        // --- 6. Store disguise request to re-apply after reveal ends ---
        final com.houzicore.shared.api.disguise.DisguiseRequest storedRequest =
                (form instanceof com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm)
                        ? ((com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm) form).getDisguiseRequest()
                        : null;

        // --- 7. Schedule re-concealment ---
        org.bukkit.Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), () ->
        {
            if (!hider.isOnline() || !IsAlive(hider)) return;

            hider.removePotionEffect(org.bukkit.potion.PotionEffectType.GLOWING);

            // Re-hide player entity from viewers
            for (Player hunter : viewers)
            {
                if (hunter.isOnline())
                    hunter.hideEntity(Manager.getPlugin(), hider);
            }

            // Re-apply BlockDisplay disguise so the hider's block still shows
            Form currentForm = _forms.get(hider);
            if (currentForm instanceof com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm
                    && storedRequest != null)
            {
                Manager.GetDisguise().getService().apply(hider, storedRequest);
            }
        }, durationTicks);
    }


    @Deprecated
    private boolean isThai(Player player)
    {
        return player != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(player));
    }

    @Deprecated
    private String pr(Player player, String key, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers)
    {
        return com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.get().getFallback(player, key, resolvers);
    }

    private String tr(Player player, String english, String thai)
    {
        return isThai(player) ? thai : english;
    }

    public void sendPropRushNoticeKey(Player player, String key, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers)
    {
        sendPropRushNotice(player, pr(player, key, resolvers));
    }

    public void sendPropRushMessageKey(Player player, String key, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers)
    {
        if (player == null)
            return;

        UtilTextBottom.display(ActionBarChannel.GAME_EVENT, C.cAqua + "\u26a1 " + C.cWhite + pr(player, key, resolvers), player);
    }

    private String formatPropRushDetail(Player player, String keyHeadline, String keyDetail)
    {
        return C.cAqua + pr(player, keyHeadline) + C.cGray + ": " + C.cWhite + pr(player, keyDetail);
    }

    @Deprecated
    private String formatPropRushDetail(Player player, String headlineEn, String headlineTh, String detailEn, String detailTh)
    {
        return C.cAqua + tr(player, headlineEn, headlineTh) + C.cGray + ": " + C.cWhite + tr(player, detailEn, detailTh);
    }

    private String getPropRushThemeLabel()
    {
        return C.cAqua + C.Bold + "BLOCK HUNT";
    }

    private String getPropRushChatPrefix()
    {
        return getPropRushThemeLabel() + C.cGray + " | ";
    }

    private void sendPropRushNotice(Player player, String message)
    {
        if (player == null)
            return;

        UtilTextBottom.display(ActionBarChannel.GAME_EVENT, C.cAqua + "\u26a1 " + C.cWhite + message, player);
    }

    private void sendPropRushSummary(Player player, String headline, String detail)
    {
        if (player == null)
            return;

        UtilTextBottom.display(ActionBarChannel.GAME_EVENT,
                C.cAqua + headline + C.cGray + " \u00bb " + C.cWhite + detail, player);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_IN, 0.5f, 1.2f);
    }

    private void sendPropRushSummary(Player player, String headlineEn, String headlineTh, String detailEn, String detailTh)
    {
        if (player == null)
            return;

        String headline = tr(player, headlineEn, headlineTh);
        String detail = tr(player, detailEn, detailTh);
        UtilTextBottom.display(ActionBarChannel.GAME_EVENT,
                C.cAqua + headline + C.cGray + " \u00bb " + C.cWhite + detail, player);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_IN, 0.5f, 1.2f);
    }

    private void announcePropRushSummary(String headline, String detail)
    {
        for (Player player : GetPlayers(true))
        {
            com.houzicore.shared.common.util.UtilTextMiddle.display(
                    C.cAqua + C.Bold + headline,
                    C.cWhite + detail,
                    10, 60, 20, player);
        }
    }

    private void announcePropRushSummary(String headlineEn, String headlineTh, String detailEn, String detailTh)
    {
        for (Player player : GetPlayers(true))
        {
            String headline = tr(player, headlineEn, headlineTh);
            String detail = tr(player, detailEn, detailTh);
            com.houzicore.shared.common.util.UtilTextMiddle.display(
                    C.cAqua + C.Bold + headline,
                    C.cWhite + detail,
                    10, 60, 20, player);
        }
    }

    private String getKitCompactReminder(Player player, Kit kit)
    {
        if (kit == null || kit.getLanguageKey() == null || kit.getLanguageKey().trim().isEmpty())
        {
            return pr(player, "prop_rush.summary.kit_compact.default");
        }

        return pr(player, "prop_rush.summary.kit_compact." + kit.getLanguageKey());
    }

    private void sendKitGameplaySummary(Player player)
    {
        if (player == null)
            return;

        Kit kit = GetKit(player);
        if (kit == null)
            return;

        sendPropRushSummary(player,
                pr(player, "prop_rush.summary.kit_label"),
                pr(player, "prop_rush.summary.kit_label"),
                kit.GetName(player) + C.cGray + " • " + C.cWhite + getKitCompactReminder(player, kit),
                kit.GetName(player) + C.cGray + " • " + C.cWhite + getKitCompactReminder(player, kit));
    }

	public void GiveHiderItems(Player player)
	{
		PropRushKitLoadoutService.applyHiderLoadout(player, GetKit(player));
	}
	public void GiveSeekerItems(Player player)
	{
		PropRushKitLoadoutService.applyHunterLoadout(player, GetKit(player));
	}
    @EventHandler
    public void InitialDisguise(PlayerPrepareTeleportEvent event)
    {
        if (_hiders.HasPlayer(event.GetPlayer().getName(), true))
        {
            if (GetKit(event.GetPlayer()) != null)
            {
                Form form = new BlockForm(this, event.GetPlayer(), _allowedBlocks.get(UtilMath.r(_allowedBlocks.size())));

                _forms.put(event.GetPlayer(), form);

                // Delay via UpdateEvent so the disguise applies after teleport and initial packet churn settle.
                _pendingInitialDisguise.put(event.GetPlayer(), System.currentTimeMillis() + 500L);
                
            }
        }
    }

    private void applyPropTierHealth(Player player, Form form) {
        double maxHp = 20.0;
        if (form instanceof com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm) {
            Material mat = ((com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm)form).GetMaterial();
            if (_rareBlocks != null && _rareBlocks.contains(mat)) {
                maxHp = 40.0;
            } else if (_tinyBlocks != null && _tinyBlocks.contains(mat)) {
                maxHp = 10.0;
            } else {
                maxHp = 20.0;
            }
        } else if (form instanceof com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.CreatureForm) {
            maxHp = 15.0;
        }
        
        com.houzicore.arcade.nautilus.game.arcade.kit.traits.Trait equippedTrait = Manager.getTraitManager().getEquippedTrait(player, GetKit(player));
        if (equippedTrait != null && equippedTrait.getKey().equals("hideseek_lightweight")) {
            maxHp = Math.max(2.0, maxHp - 4.0);
        }

        if (_roundModifier == RoundModifier.HEAVY_PROPS && form instanceof com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm)
        {
            Material mat = ((com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm)form).GetMaterial();
            if (_tinyBlocks == null || !_tinyBlocks.contains(mat))
                maxHp += 6.0;
        }
        
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHp);
        player.setHealth(maxHp);
    }

    @EventHandler
    public void FixInvisibilityBug(PlayerPrepareTeleportEvent event)
    {
        // Self-healing: Clear persistent 'setInvisible(true)' state from the buggy BlockDisplay era
        event.GetPlayer().setInvisible(false);
    }

    @EventHandler
    public void ChangeDisguise(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null)
            return;

        Player player = event.getPlayer();

        if (player.getInventory().getHeldItemSlot() != 8)
            return;

        if (!isMorphTool(player.getInventory().getItemInMainHand()))
            return;

        // Cancel the event so the morph item is never consumed or placed
        event.setCancelled(true);

        if (_morphGuards.contains(player.getUniqueId()))
            return;

        if (!_allowedBlocks.contains(event.getClickedBlock().getType()))
        {
            UtilPlayer.message(
                    player,
                    F.main("Game",
                            "You cannot morph into "
                                    + F.elem(ItemStackFactory.Instance
                                            .GetName(event.getClickedBlock().getType(), (byte) 0, false) + " Block") + "."));
            return;
        }

        if (!useVanillaItemCooldown(player, "Change Form", 6000, Material.SLIME_BALL))
            return;

        Material newMat = event.getClickedBlock().getType();

        _morphGuards.add(player.getUniqueId());
        Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
            _morphGuards.remove(player.getUniqueId());
        }, 1L);

        // 1. Tell engine to replace atomically
        Manager.GetDisguise().getEngine().replaceDisguise(player, newMat);

        // 2. Wrap state
        Form form = new BlockForm(this, player, newMat);
        _forms.put(player, form);
        form.applyUsingExistingEngineState();
        
        // Fix Skin Refreshing Bug by re-sending player profiles
        // Block Hunt: Use hideEntity/showEntity to refresh visuals without touching Tab List
        Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
            for (org.bukkit.entity.Player p : com.houzicore.shared.common.util.UtilServer.getPlayers()) {
                if (!p.equals(player) && p.canSee(player)) {
                    p.hideEntity(Manager.getPlugin(), player);
                    p.showEntity(Manager.getPlugin(), player);
                }
            }
        }, 5L);
        
        applyPropTierHealth(player, form);
        
        // Morphing Sound & Particles (superrule.md pattern)
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.2f);
        UtilParticle.PlayParticle(ParticleType.MAGIC_CRIT, player.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0f, 20, ViewDist.NORMAL, UtilServer.getPlayers());

        Bukkit.getPluginManager().callEvent(new PlayerChangeFormEvent(player, form));
    }

    @EventHandler
    public void ChangeDisguise(PlayerInteractEntityEvent event)
    {
        if (event.getRightClicked() == null)
            return;

        Player player = event.getPlayer();

        if (player.getInventory().getHeldItemSlot() != 8)
            return;

        if (!isMorphTool(player.getInventory().getItemInMainHand()))
            return;

        if (_morphGuards.contains(player.getUniqueId()))
            return;

        if (!_allowedEnts.contains(event.getRightClicked().getType()))
        {
            UtilPlayer.message(player,
                    F.main("Game", "You cannot morph into " + F.elem(UtilEnt.getName(event.getRightClicked())) + "."));
            return;
        }

        if (!useVanillaItemCooldown(player, "Change Form", 6000, Material.SLIME_BALL))
            return;

        EntityType newType = event.getRightClicked().getType();

        _morphGuards.add(player.getUniqueId());
        Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
            _morphGuards.remove(player.getUniqueId());
        }, 1L);

        // 1. Tell engine to replace atomically
        CreatureAllowOverride = true;
        try {
            Manager.GetDisguise().getEngine().replaceDisguise(player, newType);
        } finally {
            CreatureAllowOverride = false;
        }

        // Block Hunt: Delayed viewer refresh for creature disguises.
        // Paper 1.21.x may bundle SPAWN_PLAYER packets, so a second hide/show cycle
        // ensures all viewers get the mob rewrite.
        Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
            if (!player.isOnline()) return;
            for (org.bukkit.entity.Player p : UtilServer.getPlayers()) {
                if (!p.equals(player)) {
                    p.hideEntity(Manager.getPlugin(), player);
                    p.showEntity(Manager.getPlugin(), player);
                }
            }
            Manager.GetDisguise().getEngine().forceRelistInTablist(player);
        }, 10L);

        // 2. Wrap state
        Form form = new CreatureForm(this, player, newType);
        _forms.put(player, form);
        form.applyUsingExistingEngineState();
        
        applyPropTierHealth(player, form);
        
        // Morphing Sound & Particles (superrule.md pattern)
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.2f);
        UtilParticle.PlayParticle(ParticleType.MAGIC_CRIT, player.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0f, 20, ViewDist.NORMAL, UtilServer.getPlayers());

        Bukkit.getPluginManager().callEvent(new PlayerChangeFormEvent(player, form));
    }

    @EventHandler
    public void ChangeDisguise(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player))
            return;
            
        Player player = (Player) event.getDamager();

        if (player.getInventory().getHeldItemSlot() != 8)
            return;

        if (!isMorphTool(player.getInventory().getItemInMainHand()))
            return;

        if (_morphGuards.contains(player.getUniqueId()))
            return;

        if (!_allowedEnts.contains(event.getEntity().getType()))
        {
            UtilPlayer.message(player,
                    F.main("Game", "You cannot morph into " + F.elem(UtilEnt.getName(event.getEntity())) + "."));
            return;
        }

        if (!useVanillaItemCooldown(player, "Change Form", 6000, Material.SLIME_BALL))
            return;

        EntityType newType = event.getEntity().getType();

        _morphGuards.add(player.getUniqueId());
        Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
            _morphGuards.remove(player.getUniqueId());
        }, 1L);

        // 1. Tell engine to replace atomically
        CreatureAllowOverride = true;
        try {
            Manager.GetDisguise().getEngine().replaceDisguise(player, newType);
        } finally {
            CreatureAllowOverride = false;
        }

        // Block Hunt: Delayed viewer refresh for creature disguises.
        Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
            if (!player.isOnline()) return;
            for (org.bukkit.entity.Player p : UtilServer.getPlayers()) {
                if (!p.equals(player)) {
                    // Use hideEntity/showEntity for creature disguises to ensure proper visibility
                    p.hideEntity(Manager.getPlugin(), player);
                    p.showEntity(Manager.getPlugin(), player);
                }
            }
            Manager.GetDisguise().getEngine().forceRelistInTablist(player);
        }, 10L);

        // 2. Wrap state
        Form form = new CreatureForm(this, player, newType);
        _forms.put(player, form);
        form.applyUsingExistingEngineState();
        
        applyPropTierHealth(player, form);
        
        // Morphing Sound & Particles (superrule.md pattern)
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.2f);
        UtilParticle.PlayParticle(ParticleType.MAGIC_CRIT, player.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0f, 20, ViewDist.NORMAL, UtilServer.getPlayers());

        Bukkit.getPluginManager().callEvent(new PlayerChangeFormEvent(player, form));
    }

	// Legacy falling block handlers removed due to native BlockDisplay migration

    @EventHandler
    public void SolidifyUpdate(UpdateEvent event)
    {
        if (!IsLive())
            return;

        if (event.getType() != UpdateType.TICK)
            return;

        for (Form form : _forms.values())
        {
            if (form instanceof BlockForm bForm)
            {
                bForm.SolidifyUpdate();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void HiderDamage(EntityDamageEvent event)
    {
        if (!IsLive())
            return;

        if (event.isCancelled())
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        Player hider = (Player) event.getEntity();

        if (!_hiders.HasPlayer(hider))
            return;

        // Bypass immunity if it's a recursive hit
        if (hider.hasMetadata("recursive_disguise_hit")) {
            return;
        }

        if (_rejoinInvulUntil.containsKey(hider.getUniqueId()) && System.currentTimeMillis() < _rejoinInvulUntil.get(hider.getUniqueId()))
        {
            event.setCancelled(true);
            return;
        }

        // Block Hunt: Cancel damage during solid-break immunity window (1.5s after solid broke)
        if (Manager.GetDisguise().getEngine().isSolidBreakImmune(hider))
        {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Unified handler for processing a hit on a hiding player (either directly or via block interaction).
     * Calculates weapon damage, enforces hit cooldown, applies damage, and handles visuals.
     */
    private void handleDisguiseHit(Player attacker, Player hider, String source)
    {
        if (attacker == null || hider == null || !attacker.isOnline() || !hider.isOnline() || attacker.equals(hider))
            return;

        if (_rejoinInvulUntil.containsKey(hider.getUniqueId()) && System.currentTimeMillis() < _rejoinInvulUntil.get(hider.getUniqueId()))
            return;

        // Block Hunt: Skip damage during solid-break immunity window
        if (Manager.GetDisguise().getEngine().isSolidBreakImmune(hider))
            return;

        if (!_seekers.HasPlayer(attacker))
            return;

        if (!_hiders.HasPlayer(hider))
            return;

        // 250ms hit cooldown per attacker-target pair to prevent double damage
        String pairKey = attacker.getUniqueId().toString() + ":" + hider.getUniqueId().toString();
        long now = System.currentTimeMillis();
        if (_hitCooldowns.containsKey(pairKey) && now - _hitCooldowns.get(pairKey) < 250) {
            return;
        }
        _hitCooldowns.put(pairKey, now);

        double damage = 1.0;
        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (item != null) {
            String matName = item.getType().name();
            if (matName.contains("SWORD")) damage = 6.0;
            else if (matName.contains("AXE")) damage = 5.0;
        }

        // Apply metadata tag to bypass cooldown on the recursive event
        hider.setMetadata("recursive_disguise_hit", new org.bukkit.metadata.FixedMetadataValue(Manager.getPlugin(), true));
        // Temporarily remove hit cooldown to allow proper knockback
        int previousNoDamageTicks = hider.getNoDamageTicks();
        hider.setNoDamageTicks(0);
        hider.damage(damage, attacker);
        // Restore original noDamageTicks after damage is applied
        hider.setNoDamageTicks(previousNoDamageTicks);

        // Add proper knockback based on weapon type and direction from attacker to hider
        org.bukkit.util.Vector knockbackDir = hider.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
        double knockbackStrength = item != null && item.getType().name().contains("SWORD") ? 0.5 : 0.3;
        hider.setVelocity(knockbackDir.multiply(knockbackStrength).setY(0.4));

        // Note: NativeDisguiseEngine.breakSolidify() is still called independently when solid,
        // so we don't need to manually break it here.

        UtilParticle.PlayParticle(
                ParticleType.CRIT,
                hider.getLocation().add(0, 0.5, 0),
                0.5f, 0.5f, 0.5f, 0.1f, 15,
                ViewDist.NORMAL,
                UtilServer.getPlayers()
        );
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void HiderHitEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event)
    {
        if (!IsLive()) return;

        if (!(event.getDamager() instanceof Player)) return;
        Player hunter = (Player) event.getDamager();

        if (!_seekers.HasPlayer(hunter)) return;

        // 1. Direct hit on a Player (e.g., Creature disguise or recursive damage call)
        if (event.getEntity() instanceof Player) {
            Player hiderToHit = (Player) event.getEntity();
            if (_hiders.HasPlayer(hiderToHit)) {
                // Check recursive disguise hit metadata to bypass cooldown
                if (hiderToHit.hasMetadata("recursive_disguise_hit")) {
                    final Player finalHider = hiderToHit;
                    Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), () -> {
                        if (finalHider.isOnline()) {
                            finalHider.removeMetadata("recursive_disguise_hit", Manager.getPlugin());
                        }
                    }, 1L);
                    // Apply proper knockback for recursive hit
                    org.bukkit.util.Vector knockbackDir = hiderToHit.getLocation().toVector().subtract(hunter.getLocation().toVector()).normalize();
                    double knockbackStrength = hunter.getInventory().getItemInMainHand() != null 
                        && hunter.getInventory().getItemInMainHand().getType().name().contains("SWORD") ? 0.5 : 0.3;
                    hiderToHit.setVelocity(knockbackDir.multiply(knockbackStrength).setY(0.4));
                    UtilParticle.PlayParticle(ParticleType.CRIT, hiderToHit.getLocation().add(0, 0.5, 0), 0.5f, 0.5f, 0.5f, 0.1f, 15, ViewDist.NORMAL, UtilServer.getPlayers());
                    return;
                }

                if (_rejoinInvulUntil.containsKey(hiderToHit.getUniqueId()) && System.currentTimeMillis() < _rejoinInvulUntil.get(hiderToHit.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }

                // Block Hunt: Skip damage during solid-break immunity window
                if (Manager.GetDisguise().getEngine().isSolidBreakImmune(hiderToHit)) {
                    event.setCancelled(true);
                    return;
                }
                String pairKey = hunter.getUniqueId().toString() + ":" + hiderToHit.getUniqueId().toString();
                long now = System.currentTimeMillis();
                
                // If on cooldown, it might be the recursive hider.damage() from handleDisguiseHit.
                // We MUST let it pass through without cancelling so the damage applies!
                if (_hitCooldowns.containsKey(pairKey) && now - _hitCooldowns.get(pairKey) < 250) {
                    return;
                }
                
                // Fresh direct hit
                _hitCooldowns.put(pairKey, now);
                
                double damage = 1.0;
                ItemStack item = hunter.getInventory().getItemInMainHand();
                if (item != null) {
                    String matName = item.getType().name();
                    if (matName.contains("SWORD")) damage = 6.0;
                    else if (matName.contains("AXE")) damage = 5.0;
                }
                
                event.setDamage(damage);
                // Apply proper knockback based on weapon type and direction
                org.bukkit.util.Vector knockbackDir = hiderToHit.getLocation().toVector().subtract(hunter.getLocation().toVector()).normalize();
                double knockbackStrength = item != null && item.getType().name().contains("SWORD") ? 0.5 : 0.3;
                hiderToHit.setVelocity(knockbackDir.multiply(knockbackStrength).setY(0.4));
                UtilParticle.PlayParticle(ParticleType.CRIT, hiderToHit.getLocation().add(0, 0.5, 0), 0.5f, 0.5f, 0.5f, 0.1f, 15, ViewDist.NORMAL, UtilServer.getPlayers());
            }
        } 
        // 2. Hit on a proxy entity (e.g., Interaction entity for Block disguise)
        else {
            Player hiderToHit = null;
            com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData fakeData = 
                Manager.GetDisguise().getEngine().getByFakeEntityId(event.getEntity().getEntityId());
            if (fakeData != null) {
                hiderToHit = org.bukkit.Bukkit.getPlayer(fakeData.getPlayerUUID());
            }
            if (hiderToHit == null) {
                hiderToHit = Manager.GetDisguise().getEngine().getRealPlayerByEntityId(event.getEntity().getEntityId());
            }
            if (hiderToHit != null && _hiders.HasPlayer(hiderToHit)) {
                event.setCancelled(true);
                handleDisguiseHit(hunter, hiderToHit, "HiderHitEntity");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void HiderHitBridge(PlayerInteractEvent event)
    {
        if (!IsLive())
            return;

        if (event.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_BLOCK &&
            event.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_AIR)
            return;

        Player hunter = event.getPlayer();

        if (!_seekers.HasPlayer(hunter))
            return;

        Player hiderToHit = null;

        // 1. If it's a block click, check if we hit a currently solid block form → break solid ONLY, no damage
        if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null)
        {
            org.bukkit.block.Block clickedBlock = event.getClickedBlock();
            com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData graceData = 
                Manager.GetDisguise().getEngine().getGraceHitDisguiseAt(
                    clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ(), clickedBlock.getWorld().getName()
                );

            for (Player hider : _forms.keySet())
            {
                Form form = _forms.get(hider);
                if (form instanceof BlockForm)
                {
                    BlockForm bForm = (BlockForm) form;
                    if (bForm.GetBlock() != null && bForm.GetBlock().getLocation().equals(clickedBlock.getLocation()))
                    {
                        // Block Hunt: Break solid state first
                        bForm.SolidifyRemove();
                        hunter.playSound(hunter.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
                        UtilParticle.PlayParticle(ParticleType.CRIT, clickedBlock.getLocation().add(0.5, 0.5, 0.5), 0.5f, 0.5f, 0.5f, 0.1f, 15, ViewDist.NORMAL, UtilServer.getPlayers());
                        event.setCancelled(true);
                        
                        // After breaking solid, apply damage to the hider
                        handleDisguiseHit(hunter, hider, "HiderHitBridge-Solid");
                        return;
                    }
                }
            }

            // 2. If no solid block form found, check the grace hit window (0.75s after breaking solidify)
            if (graceData != null) {
                hiderToHit = Bukkit.getPlayer(graceData.getPlayerUUID());
            }
        }

        if (hiderToHit != null) {
            handleDisguiseHit(hunter, hiderToHit, "HiderHitBridge");
            event.setCancelled(true);
            return;
        }

        // 3. Raycast check for moving block hiders (both air click and block click)
        Location eyeLoc = hunter.getEyeLocation();
        Vector rayStart = eyeLoc.toVector();
        Vector rayDir = eyeLoc.getDirection().normalize();
        double maxReach = 3.8;

        for (Player hider : _forms.keySet())
        {
            Form form = _forms.get(hider);
            if (form instanceof BlockForm)
            {
                BlockForm bForm = (BlockForm) form;
                if (bForm.GetBlock() == null) // ONLY check hiders who are NOT solidified (moving block)
                {
                    Location hLoc = hider.getLocation();
                    if (hLoc.getWorld() == hunter.getWorld() && hLoc.distanceSquared(eyeLoc) <= 16.0) // Euclidean distance filter
                    {
                        com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData disguiseData = 
                            Manager.GetDisguise().getEngine().getDisguise(hider);
                        double blockWidth = (disguiseData != null) ? disguiseData.getBlockWidth() : 1.0;
                        double blockHeight = (disguiseData != null) ? disguiseData.getBlockHeight() : 1.0;
                        double padding = 0.05;
                        double halfWidth = blockWidth / 2.0;
                        Vector boxMin = new Vector(hLoc.getX() - (halfWidth + padding), hLoc.getY() - padding, hLoc.getZ() - (halfWidth + padding));
                        Vector boxMax = new Vector(hLoc.getX() + (halfWidth + padding), hLoc.getY() + blockHeight + padding, hLoc.getZ() + (halfWidth + padding));

                        if (intersectsAABB(eyeLoc, rayDir, boxMin, boxMax, maxReach))
                        {
                            handleDisguiseHit(hunter, hider, "HiderHitBridge-Raycast");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        if (event.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_BLOCK)
            return;

        // PROP RUSH REWORK: Spam-Click Punishment!
        org.bukkit.block.Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.getType().isSolid() || clicked.isPassable()) return;
        
        // Don't punish if it's a decoy block
        for (org.bukkit.block.Block decoy : _decoys.keySet()) {
            if (decoy.getLocation().equals(clicked.getLocation())) {
                return;
            }
        }
        if (hunter.getGameMode() == org.bukkit.GameMode.SURVIVAL || hunter.getGameMode() == org.bukkit.GameMode.ADVENTURE) {
            if (IsLive()) {
                double maxHp = hunter.getMaxHealth();
                // Kill if 0.5 heart left to trigger DeathEvent, else subtract 1.0 safely
                if (hunter.getHealth() <= 1.0) {
                    hunter.setHealth(0);
                } else {
                    hunter.setHealth(hunter.getHealth() - 1.0);
                    hunter.playEffect(EntityEffect.HURT); // Small invisible hit tick simulation without calling damage()
                }
                
                // Deduct score/points
                int currentPts = _points.getOrDefault(hunter, 0);
                _points.put(hunter, Math.max(0, currentPts - 5));

                hunter.playSound(hunter.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
                hunter.playSound(hunter.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                hunter.playSound(hunter.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 0.8f);
                UtilParticle.PlayParticle(ParticleType.CRIT, hunter.getLocation().add(0, 1.5, 0), 0.2f, 0.2f, 0.2f, 0, 3, ViewDist.NORMAL, hunter);
                // Hint message on first hit
                if (hunter.getHealth() == maxHp - 1.0) {
                    sendPropRushNoticeKey(hunter, "prop_rush.notice.wrong_block_hit");
                }
            }
        }
    }

    private boolean intersectsAABB(Location eye, Vector dir, Vector min, Vector max, double maxDist)
    {
        double tMin = 0.0;
        double tMax = maxDist;
        double[] origin = {eye.getX(), eye.getY(), eye.getZ()};
        double[] direction = {dir.getX(), dir.getY(), dir.getZ()};
        double[] minArr = {min.getX(), min.getY(), min.getZ()};
        double[] maxArr = {max.getX(), max.getY(), max.getZ()};

        for (int i = 0; i < 3; i++)
        {
            if (Math.abs(direction[i]) < 1E-8)
            {
                if (origin[i] < minArr[i] || origin[i] > maxArr[i])
                    return false;
            }
            else
            {
                double invD = 1.0 / direction[i];
                double t0 = (minArr[i] - origin[i]) * invD;
                double t1 = (maxArr[i] - origin[i]) * invD;
                if (invD < 0.0)
                {
                    double temp = t0; t0 = t1; t1 = temp;
                }
                tMin = Math.max(tMin, t0);
                tMax = Math.min(tMax, t1);
                if (tMax < tMin)
                    return false;
            }
        }
        return true;
    }

    /**
     * Cancel right-click interactions on solidified disguise blocks to prevent
     * the client from receiving the real block data (AIR) and causing a visual flicker.
     * This complements the BLOCK_CHANGE packet interception in NativeDisguisePacketListener.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void SolidBlockRightClickProtect(PlayerInteractEvent event)
    {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getClickedBlock() == null)
            return;

        if (!IsLive())
            return;

        org.bukkit.block.Block clickedBlock = event.getClickedBlock();

        // Check NativeDisguiseEngine for a solidified disguise at this location
        com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData solidData =
            Manager.GetDisguise().getEngine().getSolidifiedDisguiseAt(
                clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ(),
                clickedBlock.getWorld().getName()
            );

        if (solidData != null) {
            event.setCancelled(true);
            return;
        }

        // Also check legacy BlockForm positions
        for (Player hider : _forms.keySet())
        {
            Form form = _forms.get(hider);
            if (form instanceof BlockForm)
            {
                BlockForm bForm = (BlockForm) form;
                if (bForm.GetBlock() != null && bForm.GetBlock().getLocation().equals(clickedBlock.getLocation()))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void AnimalSpawn(GameStateChangeEvent event)
    {
        if (event.GetState() != GameState.Prepare)
            return;

        this.CreatureAllowOverride = true;

        for (Location loc : WorldData.GetDataLocs("WHITE"))
            _mobs.put(loc.getWorld().spawn(loc, Sheep.class), loc);

        for (Location loc : WorldData.GetDataLocs("PINK"))
            _mobs.put(loc.getWorld().spawn(loc, Pig.class), loc);

        for (Location loc : WorldData.GetDataLocs("YELLOW"))
            _mobs.put(loc.getWorld().spawn(loc, Chicken.class), loc);

        for (Location loc : WorldData.GetDataLocs("BROWN"))
            _mobs.put(loc.getWorld().spawn(loc, Cow.class), loc);

        this.CreatureAllowOverride = false;
    }







    @EventHandler
    public void HiderTimeGems(UpdateEvent event)
    {
        if (GetState() != GameState.Live || _phase == Phase.PREP)
            return;

        if (event.getType() != UpdateType.SEC)
            return;

        for (Player player : _hiders.GetPlayers(true))
        {
            double mult = 1.0;
            Form form = _forms.get(player);
            if (form instanceof BlockForm && ((BlockForm)form).GetBlock() != null)
            {
                Material mat = ((BlockForm)form).GetBlock().getType();
                if (_rareBlocks != null && _rareBlocks.contains(mat)) mult = 2.0;
                else if (_tinyBlocks != null && _tinyBlocks.contains(mat)) mult = 3.0; 
            }

            double pts = _survivalPoints.getOrDefault(player, 0.0) + (1.0 * mult);
            _survivalPoints.put(player, pts);

            if (Math.random() < 0.2) 
                this.AddGems(player, 0.25 * mult, "Prop Survival", true, true);
        }
    }

    @EventHandler
    public void UpdateSeekers(UpdateEvent event)
    {
        if (!IsLive())
            return;

        if (event.getType() != UpdateType.FAST)
            return;

        int req = Math.max(1, GetPlayers(true).size() / 5);

        while (_seekers.GetPlayers(true).size() < req && _hiders.GetPlayers(true).size() > 0)
        {
            if (_hiders.GetPlayers(true).isEmpty()) return;
            Player player = _hiders.GetPlayers(true).get(UtilMath.r(_hiders.GetPlayers(true).size()));
            SetSeeker(player, true);
        }

        if (_phase == Phase.PANIC)
        {
            for (Player seeker : _seekers.GetPlayers(true))
                seeker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 30, 0, false, true));
        }
        
        if (_phase == Phase.PANIC || _phase == Phase.CHAOS) {
            if (_hiders.GetPlayers(true).size() <= 2) {
                for (Player hider : _hiders.GetPlayers(true)) {
                    if (_adrenalineActive.add(hider)) {
                        sendPropRushSummary(hider, "Adrenaline", "อะดรีนาลีน",
                                "Late game pressure is rising. Play faster and keep rotating.",
                                "เกมช่วงท้ายเริ่มกดดันขึ้นแล้ว รีบเล่นและเปลี่ยนตำแหน่งต่อเนื่อง");
                        hider.playSound(hider.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1f, 1.3f);
                    }

                    hider.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 40, 0, false, true));
                }
            } else {
                _adrenalineActive.clear();
            }
        } else {
            _adrenalineActive.clear();
        }

        // Last Stand check
        if (_phase == Phase.HUNT || _phase == Phase.PANIC || _phase == Phase.CHAOS) {
            ArrayList<Player> hiderPlayers = _hiders.GetPlayers(true);
            if (hiderPlayers.size() == 1 && !_lastStandActive) {
                _lastStandActive = true;
                Player survivor = hiderPlayers.get(0);
                survivor.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 600, 0));
                
                sendPropRushSummary(survivor, "Last Stand", "คนสุดท้าย",
                        "You are the final prop. Survive the remaining time.",
                        "คุณคือพร็อพคนสุดท้าย รอดให้ครบเวลาที่เหลือ");
                _pendingLastStandSound.put(survivor, System.currentTimeMillis() + 250L);
            }
        }
    }



    @EventHandler
    public void DisableAutoHealthRegen(org.bukkit.event.entity.EntityRegainHealthEvent event)
    {
        if (GetState() != GameState.Live || _phase == Phase.PREP)
            return;

        if (!(event.getEntity() instanceof Player player))
            return;

        if (!isPropRushSide(player))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void PlayerDeath(PlayerQuitEvent event)
    {
        removeWardenSentries(event.getPlayer());
        clearPendingPlayerState(event.getPlayer());
        Form form = _forms.remove(event.getPlayer());
        if (form != null)
            form.Remove();
    }

    @EventHandler
    public void RevealerTNTExplode(org.bukkit.event.entity.EntityExplodeEvent event)
    {
        if (event.getEntityType() != org.bukkit.entity.EntityType.TNT)
            return;

        event.blockList().clear();

        for (Player hider : _hiders.GetPlayers(true))
        {
            if (UtilMath.offset(event.getLocation(), hider.getLocation()) < 8.0)
            {
                Form form = _forms.get(hider);
                if (form instanceof BlockForm)
                {
                    BlockForm bForm = (BlockForm) form;
                    bForm.SolidifyRemove();
                }

                hider.setFireTicks(100);
                revealHider(hider, 100);
                UtilPlayer.message(hider, F.main("Game", "You were revealed by a " + F.elem("Revealer TNT") + "!"));
                hider.getWorld().playSound(hider.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1f, 1f);
            }
        }
    }



    @EventHandler
    public void PlayerDeath(PlayerDeathEvent event)
    {
        Player dead = event.getEntity();
        if (_hiders.HasPlayer(dead))
        {
            Player killer = dead.getKiller();
            if (killer != null && _seekers.HasPlayer(killer))
            {
                int pts = 10;
                if (dead.hasPotionEffect(org.bukkit.potion.PotionEffectType.GLOWING))
                {
                    pts += 5; // Bonus for killing glowing/flared hiders
                    UtilPlayer.message(killer, C.cGold + C.Bold + "+5 Bonus Points! You killed a Revealed Hider!");
                }
                _points.put(killer, _points.getOrDefault(killer, 0) + pts);
                _hunterKills.put(killer.getUniqueId(), _hunterKills.getOrDefault(killer.getUniqueId(), 0) + 1);
                UtilPlayer.message(killer, C.cGreen + C.Bold + "+" + pts + " Points! You killed a Hider!");
            }
            SetSeeker(dead, false);
        }
    }

    public void SetSeeker(Player player, boolean forced)
    {
        GameTeam pastTeam = GetTeam(player);
        if (pastTeam != null && pastTeam.equals(_hiders))
        {
            pastTeam.SetPlacement(player, PlayerState.OUT);
            if (!_hiderCaughtTime.containsKey(player.getUniqueId()))
            {
                _hiderCaughtTime.put(player.getUniqueId(), System.currentTimeMillis() - _gameStartTime);
            }
        }

        _adrenalineActive.remove(player);
        _dangerZoneWarned.remove(player);
        removeWardenSentries(player);
        clearPendingPlayerState(player);

        SetPlayerTeam(player, _seekers, true);

        Manager.GetDisguise().undisguise(player);

        // Remove Form
        Form form = _forms.remove(player);
        if (form != null)
            form.Remove();

        // Final failsafe to fix the vanilla show-player tablist glitch specifically for dying players
        for (Player other : UtilServer.getPlayers())
        {
            if (!other.equals(player)) {
                other.showPlayer(Manager.getPlugin(), player);
            }
        }

        // Default dead Hiders back onto Tracker regardless of roster growth.
        Kit trackerKit = null;
        for (Kit kit : GetKits())
        {
            if (kit instanceof KitTracker)
            {
                trackerKit = kit;
                break;
            }
        }

        if (trackerKit != null)
        {
            SetKit(player, trackerKit, false);
            trackerKit.ApplyKit(player);
        }

        // Refresh
        VisibilityManager.Instance.refreshPlayerToAll(player);
        
        if (forced)
        {
            AddGems(player, 10, "Forced Seeker", false, false);

            Announce(F.main("Game",
                    F.elem(_hiders.GetColor() + player.getName()) + " was moved to " + F.elem(C.cRed + C.Bold + "Hunters") + "."));

            player.getWorld().strikeLightningEffect(player.getLocation());

            player.damage(1000);
        }

        UtilPlayer.message(player, C.cRed + C.Bold + "You are now a Hunter!");

        player.eject();
        player.leaveVehicle();
        player.teleport(_seekers.GetSpawn());
        player.setExp(0.99f);
    }

    @Override
    public void EndCheck()
    {
        if (!IsLive())
            return;

        if (GetPlayers(true).isEmpty())
        {
            SetState(GameState.End);
            return;
        }

        if (_hiders.GetPlayers(true).isEmpty())
        {
            // Calculate Points and Sort
            ArrayList<Player> places = new ArrayList<>(GetPlayers(false));
            places.sort((p1, p2) -> Integer.compare(
                _points.getOrDefault(p2, 0),
                _points.getOrDefault(p1, 0)
            ));

            AnnounceEnd(places);

            // Gems
            if (places.size() >= 1)
                AddGems(places.get(0), 30, "1st Place", false, false);

            if (places.size() >= 2)
                AddGems(places.get(1), 20, "2nd Place", false, false);

            if (places.size() >= 3)
                AddGems(places.get(2), 10, "3rd Place", false, false);

            for (Player player : GetPlayers(false))
            {
                if (player.isOnline())
                {
                    // Everyone else gets participation
                    if (!places.contains(player) || places.indexOf(player) >= 3)
                    {
                        AddGems(player, 5, "Participation", false, false);
                    }
                }
            }

            SetState(GameState.End);
        }
    }

    @Override
    public double GetKillsGems(Player killer, Player killed, boolean assist)
    {
        if (_hiders.HasPlayer(killed))
        {
            if (!assist)
                return 4;
            else
                return 1;
        }

        if (!assist)
            return 1;

        return 0;
    }

    @EventHandler
    public void CleanFormsOnEnd(GameStateChangeEvent event)
    {
        if (event.GetState() == GameState.End)
        {
            payoutBets();
            displayPostMatchCeremony();
        }
        if (event.GetState() == GameState.Dead)
        {
            for (Form form : _forms.values())
            {
                form.Remove();
            }
            _forms.clear();
            _hiderPaths.clear();
            _camperLocation.clear();
            _camperTime.clear();
        }
    }

    @EventHandler
    public void AnnounceHideTime(GameStateChangeEvent event)
    {
        if (event.GetState() != GameState.Live)
            return;

        announcePropRushSummary(pr(null, "prop_rush.phase.prep"), pr(null, "prop_rush.phase.prep"),
                "25s to hide!",
                "ซ่อนตัวใน 25 วินาที!");
        _phaseStartTime = System.currentTimeMillis();
        _gameStartTime = System.currentTimeMillis();
        _initialHiderCount = _hiders.GetPlayers(true).size();
        _nextEventTime = System.currentTimeMillis() + 120000L;
        _phase = Phase.PREP;
        resetPhasePressureState();
        refreshTerminalLocations();
        refreshMapProfile();
        rollRoundModifier();
        enforceTeamCompositionRules();
        announcePropRushSummary("Modifier", "ม็อดรอบ",
                getModifierName(), getModifierNameThai());

        // Reset scoring
        _points.clear();
        _tauntCooldown.clear();
        _tauntPending.clear();
        _fireworkCooldown.clear();
        _lastSurvivalPoints.clear();
        long now = System.currentTimeMillis();
        for (Player hider : _hiders.GetPlayers(true))
        {
            _lastSurvivalPoints.put(hider, now);
            _prepAnchors.put(hider, hider.getLocation().clone());
        }
        for (Player seeker : _seekers.GetPlayers(true))
            _points.put(seeker, 0);
        
        // Hider items are delayed slightly so early live-state kit application does not wipe them.
        long giveItemsAt = System.currentTimeMillis() + 100L;
        for (Player hider : _hiders.GetPlayers(true))
        {
            _pendingHiderItems.put(hider, giveItemsAt);
        }
    }

    private void resetPhasePressureState()
    {
        _lastCountdown = -1;
        _lastStandActive = false;
        _dangerZoneCenter = null;
        _dangerZoneActiveUntil = 0;
        _nextDangerZoneAt = 0;
        _nextChaosPulseAt = 0;
        _dangerZoneWarned.clear();
        _adrenalineActive.clear();
        _recentHiderSkillUse.clear();
        _pendingInitialDisguise.clear();
        _pendingHiderItems.clear();
        _pendingLastStandSound.clear();
        _pendingScannerPulseFeedback.clear();
        _pendingScannerPulseFound.clear();
        _pendingBountyDashCheck.clear();
        _pendingHunterRespawnKit.clear();
        _pendingTauntReward.clear();
        _temporaryHolograms.clear();
        for (Block bombBlock : _bombBugBlocks.keySet())
        {
            bombBlock.setType(Material.AIR);
        }
        for (org.bukkit.entity.LivingEntity bombMob : _bombBugMobs.keySet())
        {
            if (bombMob != null && bombMob.isValid())
                bombMob.remove();
        }
        _bombBugBlocks.clear();
        _bombBugMobs.clear();
        _wardenSentries.clear();
        _prepAnchors.clear();
        _perfectPlacementBonus.clear();
        _panicJumpHits.clear();
        _panicJumpWindow.clear();
        _terminalLocations.clear();
        _terminalChannel.clear();
        _terminalChannelUntil.clear();
        _usedTerminals.clear();
        _terminalDisruptionUntil = 0;
        _terminalBlindUntil = 0;
        _hunterRelayCooldownUntil = 0;
        _mapOpen = false;
        _mapTight = false;
        _roundModifier = RoundModifier.NONE;

        if (WorldData != null && WorldData.World != null)
        {
            org.bukkit.WorldBorder border = WorldData.World.getWorldBorder();
            if (_seekers != null && _seekers.GetSpawn() != null)
                border.setCenter(_seekers.GetSpawn());
            border.setSize(150);
            border.setDamageAmount(0.0);
            border.setDamageBuffer(0.5);
            border.setWarningDistance(8);
            border.setWarningTime(6);
            WorldData.World.setTime(6000);
            WorldData.World.setStorm(false);
        }
    }

    private long getPhaseDuration()
    {
        if (_phase == Phase.PREP)
            return _prepTime;
        if (_phase == Phase.HUNT)
            return _huntTime;
        if (_phase == Phase.PANIC)
            return _panicTime;

        return _chaosTime;
    }

    private void beginPanicPhase()
    {
        _phaseStartTime = System.currentTimeMillis();
        _phase = Phase.PANIC;
        _lastCountdown = -1;
        clearDangerZone();
        _nextDangerZoneAt = System.currentTimeMillis() + 10000L;

        announcePropRushSummary(pr(null, "prop_rush.phase.panic"), pr(null, "prop_rush.phase.panic"),
                "Hunters charge 50% faster!",
                "ผู้หาชาร์จสกิลไวขึ้น 50%!");

        for (Player hider : _hiders.GetPlayers(true))
        {
            hider.playSound(hider.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 0.8f);
            sendPropRushSummary(hider, "Hider Objective", "เป้าหมายฝ่ายแอบ",
                    "Rotate early, cross the zone safely, and steal Gold on the move.",
                    "ย้ายก่อนโซนปิด วิ่งข้ามอย่างปลอดภัย และแย่ง Gold ระหว่างทาง");
        }

        for (Player seeker : _seekers.GetPlayers(true))
        {
            seeker.playSound(seeker.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 0.8f);
            sendPropRushSummary(seeker, "Hunter Objective", "เป้าหมายฝ่ายหา",
                    "Force rotates, camp relay lanes, and punish anyone diving for Gold.",
                    "บังคับให้ย้ายตำแหน่ง ยึดเลน Relay และลงโทษคนที่ดิ่งมาเก็บ Gold");
        }
    }

    private void beginChaosPhase()
    {
        _phaseStartTime = System.currentTimeMillis();
        _phase = Phase.CHAOS;
        _lastCountdown = -1;
        clearDangerZone();
        _nextChaosPulseAt = System.currentTimeMillis() + 5000L;

        announcePropRushSummary(pr(null, "prop_rush.phase.chaos"), pr(null, "prop_rush.phase.chaos"),
                "Chaos Phase!",
                "โกลาหล! แต้มมหาศาล!");

        for (Player player : UtilServer.getPlayers())
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);

        for (Player hider : _hiders.GetPlayers(true))
        {
            sendPropRushSummary(hider, "Hider Objective", "เป้าหมายฝ่ายแอบ",
                    "Survive inside the ring and contest Gold only when the lane is clear.",
                    "เอาตัวรอดในวง และแย่ง Gold เฉพาะตอนที่เลนโล่งจริง");
        }

        for (Player seeker : _seekers.GetPlayers(true))
        {
            sendPropRushSummary(seeker, "Hunter Objective", "เป้าหมายฝ่ายหา",
                    "Collapse the ring, use Relay windows, and cut every escape line.",
                    "บีบวง ใช้จังหวะ Relay และปิดทุกทางหนี");
        }

        org.bukkit.World world = WorldData.World;
        world.getWorldBorder().setCenter(_seekers.GetSpawn());
        world.getWorldBorder().setSize(150);
        world.getWorldBorder().setSize(12, Math.max(8L, (_chaosTime / 1000) - 4L));
        world.getWorldBorder().setDamageAmount(8.0);
        world.getWorldBorder().setDamageBuffer(0.0);
        world.getWorldBorder().setWarningDistance(12);
        world.getWorldBorder().setWarningTime(10);
    }

    private void clearDangerZone()
    {
        _dangerZoneCenter = null;
        _dangerZoneActiveUntil = 0;
        _dangerZoneWarned.clear();
    }

    private void markHiderSkillUse(Player player)
    {
        if (player == null || !_hiders.HasPlayer(player))
            return;

        _recentHiderSkillUse.put(player, System.currentTimeMillis());
    }

    private boolean isDecoySpaceClear(Location loc)
    {
        if (loc == null)
            return false;

        Block target = loc.getBlock();
        Material type = target.getType();
        return (type == Material.AIR || type == Material.WATER) &&
                !_decoys.containsKey(target) && !_bombBugBlocks.containsKey(target);
    }

    private boolean spawnTimedFormDecoy(Form form, Location loc, long expireAt)
    {
        if (form == null || loc == null || !isDecoySpaceClear(loc))
            return false;

        if (form instanceof BlockForm)
        {
            Material decoyMat = ((BlockForm) form).GetMaterial();
            Block target = loc.getBlock();
            target.setType(decoyMat);
            _decoys.put(target, expireAt);
            return true;
        }

        if (form instanceof CreatureForm)
        {
            org.bukkit.entity.LivingEntity mob = (org.bukkit.entity.LivingEntity) loc.getWorld().spawnEntity(loc, ((CreatureForm) form).GetEntityType());
            mob.setAI(false);
            mob.setCustomName("DecoyMob");
            mob.setCustomNameVisible(false);
            _decoyMobs.put(mob, expireAt);
            return true;
        }

        return false;
    }

    private boolean spawnBombTrap(Form form, Location loc, long expireAt)
    {
        if (form == null || loc == null || !isDecoySpaceClear(loc))
            return false;

        if (form instanceof BlockForm)
        {
            Material decoyMat = ((BlockForm) form).GetMaterial();
            Block target = loc.getBlock();
            target.setType(decoyMat);
            _bombBugBlocks.put(target, expireAt);
            return true;
        }

        if (form instanceof CreatureForm)
        {
            org.bukkit.entity.LivingEntity mob = (org.bukkit.entity.LivingEntity) loc.getWorld().spawnEntity(loc, ((CreatureForm) form).GetEntityType());
            mob.setAI(false);
            mob.setCustomName("BombBugTrap");
            mob.setCustomNameVisible(false);
            _bombBugMobs.put(mob, expireAt);
            return true;
        }

        return false;
    }

    private void detonateBombTrap(Player hunter, Location loc)
    {
        if (hunter == null || loc == null)
            return;

        hunter.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.9f, 1.2f);
        UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, loc.clone().add(0, 0.6, 0), 0.4f, 0.4f, 0.4f, 0f, 14, ViewDist.NORMAL, UtilServer.getPlayers());
        UtilParticle.PlayParticle(ParticleType.FLAME, loc.clone().add(0, 0.4, 0), 0.5f, 0.2f, 0.5f, 0.03f, 18, ViewDist.NORMAL, UtilServer.getPlayers());
        hunter.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0));

        Vector knock = hunter.getLocation().toVector().subtract(loc.toVector());
        if (knock.lengthSquared() < 0.01)
            knock = new Vector(0, 0.45, 0);
        else
            knock = knock.normalize().multiply(0.9).setY(0.35);
        hunter.setVelocity(knock);
        sendPropRushMessageKey(hunter, "prop_rush.feedback.bomb_bug_detonated");
    }

    private boolean isOpenPlayerSpace(Location loc)
    {
        if (loc == null)
            return false;

        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        Block below = loc.clone().subtract(0, 1, 0).getBlock();
        return !feet.getType().isSolid() && !head.getType().isSolid() && below.getType().isSolid();
    }

    private Location findLocksmithExit(Player player)
    {
        Location origin = player.getLocation().clone();
        Vector dir = origin.getDirection().clone().setY(0).normalize();
        if (dir.lengthSquared() == 0)
            return null;

        for (double wallStep = 0.8; wallStep <= 2.2; wallStep += 0.35)
        {
            Location wall = origin.clone().add(dir.clone().multiply(wallStep));
            if (!wall.getBlock().getType().isSolid())
                continue;

            for (double exitStep = 1.0; exitStep <= 2.6; exitStep += 0.35)
            {
                Location candidate = wall.clone().add(dir.clone().multiply(exitStep));
                candidate.setY(origin.getY());
                candidate.setYaw(origin.getYaw());
                candidate.setPitch(origin.getPitch());

                if (isOpenPlayerSpace(candidate))
                    return candidate;

                Location raised = candidate.clone().add(0, 1, 0);
                if (isOpenPlayerSpace(raised))
                    return raised;
            }

            break;
        }

        return null;
    }

    public boolean hasOpenSky(Location loc)
    {
        return loc.getWorld().getHighestBlockYAt(loc) <= loc.getBlockY() + 1;
    }

    public void removeWardenSentries(Player owner)
    {
        Iterator<WardenSentry> it = _wardenSentries.iterator();
        while (it.hasNext())
        {
            WardenSentry sentry = it.next();
            if (sentry.owner == owner)
            {
                UtilParticle.PlayParticle(ParticleType.CLOUD, sentry.location.clone().add(0, 0.4, 0), 0.3f, 0.2f, 0.3f, 0f, 8, ViewDist.NORMAL, UtilServer.getPlayers());
                it.remove();
            }
        }
    }

    public Location getWardenSentryPlacement(Player player)
    {
        Vector dir = player.getLocation().getDirection().clone().setY(0).normalize();
        if (dir.lengthSquared() == 0)
            dir = new Vector(1, 0, 0);

        Location candidate = player.getLocation().clone().add(dir.multiply(1.75));
        candidate = candidate.getBlock().getLocation().add(0.5, 0.15, 0.5);

        if (!candidate.getBlock().getType().isSolid() && candidate.clone().subtract(0, 1, 0).getBlock().getType().isSolid())
            return candidate;

        return player.getLocation().getBlock().getLocation().add(0.5, 0.15, 0.5);
    }

    public int getAbilityCooldown(Player player, int baseCooldown, boolean utilityAffected)
    {
        int cooldown = baseCooldown;

        if (utilityAffected)
        {
            com.houzicore.arcade.nautilus.game.arcade.kit.traits.Trait trait = Manager.getTraitManager().getEquippedTrait(player, GetKit(player));
            if (trait != null && trait.getKey().equals("hideseek_utilitybelt"))
                cooldown -= 2000;
        }

        if (_phase == Phase.PANIC && _hiders.HasPlayer(player))
            cooldown = (int) Math.round(cooldown * 0.9);

        if (_roundModifier == RoundModifier.DOUBLE_UTILITY && utilityAffected)
            cooldown = (int) Math.round(cooldown * 0.7);

        return Math.max(3000, cooldown);
    }

    public int getRevealDurationTicks(int baseTicks)
    {
        if (_roundModifier == RoundModifier.DOUBLE_UTILITY)
            return Math.max(40, (int) Math.round(baseTicks * 0.8));

        return baseTicks;
    }

    private String getLocationKey(Location loc)
    {
        if (loc == null)
            return "";

        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private void refreshTerminalLocations()
    {
        _terminalLocations.clear();

        if (WorldData == null)
            return;

        for (java.util.Map.Entry<String, ArrayList<Location>> entry : WorldData.GetAllCustomLocs().entrySet())
        {
            String key = entry.getKey() == null ? "" : entry.getKey().toLowerCase();
            if (key.contains("terminal") || key.contains("console"))
            {
                _terminalLocations.addAll(entry.getValue());
            }
        }

        if (_terminalLocations.isEmpty())
            _terminalLocations.addAll(WorldData.GetDataLocs("PURPLE"));
    }

    private void refreshMapProfile()
    {
        int samples = 0;
        int openSamples = 0;
        double closestSpawnDist = Double.MAX_VALUE;
        ArrayList<Location> samplesLoc = new ArrayList<>();

        if (_hiders != null)
            samplesLoc.addAll(_hiders.GetSpawns());
        if (_seekers != null)
            samplesLoc.addAll(_seekers.GetSpawns());

        for (Location loc : samplesLoc)
        {
            samples++;
            if (hasOpenSky(loc))
                openSamples++;
        }

        ArrayList<Location> hiderSpawns = _hiders != null ? _hiders.GetSpawns() : new ArrayList<Location>();
        for (int i = 0; i < hiderSpawns.size(); i++)
        {
            for (int j = i + 1; j < hiderSpawns.size(); j++)
            {
                closestSpawnDist = Math.min(closestSpawnDist, UtilMath.offset(hiderSpawns.get(i), hiderSpawns.get(j)));
            }
        }

        _mapOpen = samples > 0 && ((double) openSamples / (double) samples) >= 0.5;
        _mapTight = closestSpawnDist != Double.MAX_VALUE && closestSpawnDist < 18.0;
    }

    private void rollRoundModifier()
    {
        ArrayList<RoundModifier> pool = new ArrayList<>();
        pool.add(RoundModifier.NONE);
        pool.add(RoundModifier.BLACKOUT);
        pool.add(RoundModifier.HEAVY_PROPS);
        pool.add(RoundModifier.DOUBLE_UTILITY);

        _roundModifier = pool.get(UtilMath.r(pool.size()));

        if (_roundModifier == RoundModifier.BLACKOUT && WorldData != null && WorldData.World != null)
        {
            WorldData.World.setTime(18000);
            WorldData.World.setStorm(false);
        }
    }

    private String getModifierName()
    {
        if (_roundModifier == RoundModifier.BLACKOUT)
            return "Blackout";
        if (_roundModifier == RoundModifier.HEAVY_PROPS)
            return "Heavy Props";
        if (_roundModifier == RoundModifier.DOUBLE_UTILITY)
            return "Double Utility";

        return "Standard";
    }

    private String getModifierNameThai()
    {
        if (_roundModifier == RoundModifier.BLACKOUT)
            return "ไฟดับ";
        if (_roundModifier == RoundModifier.HEAVY_PROPS)
            return "พร็อพหนัก";
        if (_roundModifier == RoundModifier.DOUBLE_UTILITY)
            return "สกิลเสริมสองเท่า";

        return "ปกติ";
    }

    private String translateSwapReason(String reason)
    {
        if (reason == null)
            return "";
        if (reason.equals("team composition cap"))
            return "เกินลิมิตคิทในทีม";
        if (reason.equals("scan-heavy trio guardrail"))
            return "ทีมสแกนหนักเกินกำหนด";
        if (reason.equals("tight-map bounty limit"))
            return "แมพแคบจำกัด Bounty Hunter";
        return reason;
    }

    private String getLocalizedSwapReason(Player player, String reason)
    {
        if (reason == null || reason.isEmpty())
            return "";

        if (reason.equals("team composition cap"))
            return pr(player, "prop_rush.notice.kit_swap_reason.team_composition_cap");
        if (reason.equals("scan-heavy trio guardrail"))
            return pr(player, "prop_rush.notice.kit_swap_reason.scan_heavy_trio_guardrail");
        if (reason.equals("tight-map bounty limit"))
            return pr(player, "prop_rush.notice.kit_swap_reason.tight_map_bounty_limit");

        return reason;
    }

    private String translateCountdownSub(String sub)
    {
        if (sub == null)
            return "";
        if (sub.equals("Hunters releasing soon..."))
            return "ฝ่ายหากำลังถูกปล่อยตัว...";
        if (sub.equals("Panic is approaching..."))
            return "เฟสกดดันกำลังจะมา...";
        if (sub.equals("Chaos is approaching..."))
            return "เฟสโกลาหลกำลังจะมา...";
        if (sub.equals("Time is running out!"))
            return "เวลาใกล้หมดแล้ว!";
        return sub;
    }

    private String getCountdownSubtitle(Player player, String sub)
    {
        if ("Hunters releasing soon...".equals(sub))
            return pr(player, "prop_rush.countdown.hunters_release");
        if ("Panic is approaching...".equals(sub))
            return pr(player, "prop_rush.countdown.panic_approaching");
        if ("Chaos is approaching...".equals(sub))
            return pr(player, "prop_rush.countdown.chaos_approaching");
        if ("Time is running out!".equals(sub))
            return pr(player, "prop_rush.countdown.time_running_out");

        return sub;
    }

    private Location getNearestTerminal(Location loc)
    {
        Location best = null;
        double bestDist = Double.MAX_VALUE;

        for (Location terminal : _terminalLocations)
        {
            double dist = UtilMath.offset(loc, terminal);
            if (dist < bestDist)
            {
                best = terminal;
                bestDist = dist;
            }
        }

        return best;
    }

    private double getWardenSentryRadius()
    {
        return _mapOpen ? Math.max(4.5, WARDEN_SENTRY_RADIUS - 1.5) : WARDEN_SENTRY_RADIUS;
    }

    public long getWardenSentryDuration()
    {
        return _mapOpen ? WARDEN_SENTRY_DURATION_MS - 8000L : WARDEN_SENTRY_DURATION_MS;
    }

    public double getFalconerRevealRadius()
    {
        return _mapOpen ? 24.0 : 18.0;
    }

    private double getHunterRelayRevealRadius()
    {
        return _mapOpen ? 22.0 : 16.0;
    }

    private boolean isPropRushSide(Player player)
    {
        return player != null && (_hiders.HasPlayer(player.getName(), true) || _seekers.HasPlayer(player.getName(), true));
    }

    private void restoreHealthFromGold(Player player, double amount)
    {
        if (player == null || amount <= 0)
            return;

        double nextHealth = Math.min(player.getMaxHealth(), player.getHealth() + amount);
        player.setHealth(nextHealth);
    }

    private void activateTerminal(Player player, Location terminal)
    {
        long now = System.currentTimeMillis();
        _usedTerminals.add(getLocationKey(terminal));
        _terminalDisruptionUntil = now + TERMINAL_DISRUPTION_MS;
        _terminalBlindUntil = now + TERMINAL_BLIND_MS;
        _hunterRelayCooldownUntil = Math.max(_hunterRelayCooldownUntil, now + 12000L);
        clearDangerZone();
        _nextDangerZoneAt = now + DANGER_ZONE_INTERVAL_MS + 8000L;
        _points.put(player, _points.getOrDefault(player, 0) + 10);

        for (Player seeker : _seekers.GetPlayers(true))
        {
            seeker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 80, 0, false, true));
            sendPropRushNoticeKey(seeker, "prop_rush.notice.terminal_interference_blind");
        }

        for (Player other : GetPlayers(true))
        {
            other.playSound(terminal, Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.6f);
        }

        UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, terminal.clone().add(0, 1, 0), 0.6f, 0.8f, 0.6f, 0f, 30, ViewDist.NORMAL, UtilServer.getPlayers());
        sendPropRushNoticeKey(player, "prop_rush.notice.terminal_hacked_reset");
        for (Player other : GetPlayers(true))
        {
            sendPropRushNoticeKey(other, "prop_rush.notice.terminal_activated_broadcast",
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("player", player.getName()));
        }
    }

    private void activateHunterRelay(Player player, Location terminal)
    {
        long now = System.currentTimeMillis();
        _hunterRelayCooldownUntil = now + HUNTER_RELAY_COOLDOWN_MS;
        _points.put(player, _points.getOrDefault(player, 0) + 8);

        int revealed = 0;
        for (Player hider : _hiders.GetPlayers(true))
        {
            if (UtilMath.offset(terminal, hider.getLocation()) > getHunterRelayRevealRadius())
                continue;

            revealHider(hider, getRevealDurationTicks(80));
            hider.playSound(hider.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.5f);
            revealed++;
        }

        for (Player other : GetPlayers(true))
        {
            other.playSound(terminal, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
        }

        UtilParticle.PlayParticle(ParticleType.END_ROD, terminal.clone().add(0, 1.0, 0), 0.5f, 0.8f, 0.5f, 0.02f, 18, ViewDist.NORMAL, UtilServer.getPlayers());
        sendPropRushNoticeKey(player,
                revealed > 0 ? "prop_rush.notice.search_relay_online_found" : "prop_rush.notice.search_relay_online_empty");

        for (Player seeker : _seekers.GetPlayers(true))
        {
            if (seeker.equals(player))
                continue;

            sendPropRushNoticeKey(seeker, "prop_rush.notice.search_relay_broadcast",
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("player", player.getName()));
        }
    }

    private Kit getLeastRepresentedReplacement(Player player)
    {
        GameTeam team = GetTeam(player);
        if (team == null)
            return null;

        Kit current = GetKit(player);
        Kit best = null;
        int bestCount = Integer.MAX_VALUE;

        for (Kit kit : GetKits())
        {
            boolean hunterKit = (kit instanceof KitTracker || kit instanceof KitDestroyer || kit instanceof KitTrapper ||
                    kit instanceof KitBloodhound || kit instanceof KitSaboteur || kit instanceof KitBountyHunter ||
                    kit instanceof KitExorcist || kit instanceof KitFalconer || kit instanceof KitWarden);

            if (team == _seekers && !hunterKit)
                continue;
            if (team == _hiders && hunterKit)
                continue;
            if (current != null && current.GetName().equals(kit.GetName()))
                continue;

            int count = 0;
            for (Player teammate : team.GetPlayers(false))
            {
                Kit teammateKit = GetKit(teammate);
                if (teammateKit != null && teammateKit.GetName().equals(kit.GetName()))
                    count++;
            }

            if (count < bestCount)
            {
                best = kit;
                bestCount = count;
            }
        }

        return best;
    }

    private void forceKitSwap(Player player, String reason)
    {
        Kit replacement = getLeastRepresentedReplacement(player);
        if (replacement == null)
            return;

        for (int slot = 0; slot < 9; slot++)
            player.getInventory().setItem(slot, new ItemStack(Material.AIR));
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));
        SetKit(player, replacement, false);
        replacement.ApplyKit(player);

        if (_hiders.HasPlayer(player))
            GiveHiderItems(player);
        else if (_seekers.HasPlayer(player))
            GiveSeekerItems(player);

        sendPropRushNoticeKey(player, "prop_rush.notice.kit_adjusted",
                net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("kit", replacement.GetName(player)),
                net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("reason", getLocalizedSwapReason(player, reason)));
    }

    private void enforceDuplicateCap(GameTeam team, int limit)
    {
        HashMap<String, Integer> counts = new HashMap<>();

        for (Player player : new ArrayList<>(team.GetPlayers(false)))
        {
            Kit kit = GetKit(player);
            if (kit == null)
                continue;

            int next = counts.getOrDefault(kit.GetName(), 0) + 1;
            counts.put(kit.GetName(), next);

            if (next > limit)
                forceKitSwap(player, "team composition cap");
        }
    }

    private void enforceTeamCompositionRules()
    {
        enforceDuplicateCap(_seekers, 2);
        enforceDuplicateCap(_hiders, 3);

        boolean hasTracker = false;
        boolean hasBloodhound = false;
        boolean hasWarden = false;
        int bountyHunters = 0;

        for (Player player : _seekers.GetPlayers(false))
        {
            Kit kit = GetKit(player);
            if (kit == null)
                continue;

            hasTracker |= kit instanceof KitTracker;
            hasBloodhound |= kit instanceof KitBloodhound;
            hasWarden |= kit instanceof KitWarden;
            if (kit instanceof KitBountyHunter)
                bountyHunters++;
        }

        if (hasTracker && hasBloodhound && hasWarden)
        {
            for (Player player : _seekers.GetPlayers(false))
            {
                if (GetKit(player) instanceof KitWarden)
                {
                    forceKitSwap(player, "scan-heavy trio guardrail");
                    break;
                }
            }
        }

        if (_mapTight && bountyHunters > 1)
        {
            int kept = 0;
            for (Player player : _seekers.GetPlayers(false))
            {
                if (GetKit(player) instanceof KitBountyHunter)
                {
                    kept++;
                    if (kept > 1)
                        forceKitSwap(player, "tight-map bounty limit");
                }
            }
        }
    }

    private boolean isDangerZoneActive()
    {
        return _dangerZoneCenter != null && System.currentTimeMillis() < _dangerZoneActiveUntil;
    }

    private long getDangerZoneTimeRemaining()
    {
        return Math.max(0L, _dangerZoneActiveUntil - System.currentTimeMillis());
    }

    private long getNextDangerZoneTime()
    {
        return Math.max(0L, _nextDangerZoneAt - System.currentTimeMillis());
    }

    private Location pickDangerZoneCenter()
    {
        ArrayList<Location> anchors = WorldData.GetDataLocs("LIME");
        if (!anchors.isEmpty())
        {
            Location anchor = anchors.get(UtilMath.r(anchors.size()));
            return anchor.clone().add(0.5, 0.5, 0.5);
        }

        for (int attempt = 0; attempt < 10; attempt++)
        {
            Location sample = WorldData.GetRandomXZ();
            Block highest = sample.getWorld().getHighestBlockAt(sample);

            if (highest == null || highest.getType() == Material.AIR || !highest.getType().isSolid())
                continue;

            Location candidate = highest.getLocation().add(0.5, 1, 0.5);

            if (_seekers != null && _seekers.GetSpawn() != null && UtilMath.offset(candidate, _seekers.GetSpawn()) < 12)
                continue;

            return candidate;
        }

        if (!_hiders.GetPlayers(true).isEmpty())
            return _hiders.GetPlayers(true).get(UtilMath.r(_hiders.GetPlayers(true).size())).getLocation().clone();

        return _seekers != null ? _seekers.GetSpawn() : null;
    }

    private void activateDangerZone()
    {
        Location center = pickDangerZoneCenter();
        if (center == null)
            return;

        _dangerZoneCenter = center;
        _dangerZoneActiveUntil = System.currentTimeMillis() + DANGER_ZONE_DURATION_MS;
        _dangerZoneWarned.clear();
        _nextDangerZoneAt = System.currentTimeMillis() + DANGER_ZONE_INTERVAL_MS;

        center.getWorld().strikeLightningEffect(center);

        for (Player player : UtilServer.getPlayers())
        {
            player.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 0.6f);
        }

        for (Player hider : _hiders.GetPlayers(true))
        {
            sendPropRushNoticeKey(hider, "prop_rush.notice.danger_zone_active");
        }
    }

    private void clearPendingPlayerState(Player player)
    {
        _recentHiderSkillUse.remove(player);
        _prepAnchors.remove(player);
        _perfectPlacementBonus.remove(player);
        _panicJumpHits.remove(player);
        _panicJumpWindow.remove(player);
        _terminalChannel.remove(player);
        _terminalChannelUntil.remove(player);
        _pendingInitialDisguise.remove(player);
        _pendingHiderItems.remove(player);
        _pendingLastStandSound.remove(player);
        _pendingScannerPulseFeedback.remove(player);
        _pendingScannerPulseFound.remove(player);
        _pendingBountyDashCheck.remove(player);
        _pendingHunterRespawnKit.remove(player);
        _pendingTauntReward.remove(player);
    }

    @EventHandler
    public void PerfectPlacementUpdate(UpdateEvent event)
    {
        if (!IsLive() || _phase != Phase.PREP || event.getType() != UpdateType.FAST)
            return;

        long elapsed = System.currentTimeMillis() - _phaseStartTime;

        for (Player hider : _hiders.GetPlayers(true))
        {
            Location anchor = _prepAnchors.get(hider);
            if (anchor == null || _perfectPlacementBonus.contains(hider))
                continue;

            if (elapsed < PERFECT_PLACEMENT_WINDOW_MS)
            {
                if (UtilMath.offset(anchor, hider.getLocation()) > PERFECT_PLACEMENT_MOVE_BUDGET)
                    _prepAnchors.remove(hider);
                continue;
            }

            _perfectPlacementBonus.add(hider);
            _points.put(hider, _points.getOrDefault(hider, 0) + 1);
            sendPropRushNoticeKey(hider, "prop_rush.notice.perfect_placement");
        }
    }

    @EventHandler
    public void HunterPrepPhaseVisualsUpdate(UpdateEvent event)
    {
        if (!IsLive() || _phase != Phase.PREP)
            return;

        if (event.getType() == UpdateType.TICK)
        {
            // Barrier smoke particles
            for (Location loc : WorldData.GetDataLocs("BLACK"))
            {
                Location center = loc.clone().add(0.5, 0.5, 0.5);
                loc.getWorld().spawnParticle(org.bukkit.Particle.LARGE_SMOKE, center, 2, 0.25, 0.25, 0.25, 0.01);
                loc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, center, 3, 0.25, 0.25, 0.25, 0.01);
            }

            // Hunter blindness/darkness effect
            for (Player hunter : _seekers.GetPlayers(true))
            {
                hunter.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 40, 0, true, false, false));
                hunter.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS, 40, 0, true, false, false));
            }
        }
    }

    @EventHandler
    public void TerminalChannelUpdate(UpdateEvent event)
    {
        if (!IsLive() || event.getType() != UpdateType.FAST)
            return;

        long now = System.currentTimeMillis();
        Iterator<Player> it = _terminalChannel.keySet().iterator();
        while (it.hasNext())
        {
            Player player = it.next();
            Location terminal = _terminalChannel.get(player);
            long finishAt = _terminalChannelUntil.getOrDefault(player, 0L);

            if (player == null || !player.isOnline() || !_hiders.HasPlayer(player) || terminal == null)
            {
                _terminalChannelUntil.remove(player);
                it.remove();
                continue;
            }

            if (UtilMath.offset(player.getLocation(), terminal) > 2.6 || !player.isSneaking())
            {
                sendPropRushNoticeKey(player, "prop_rush.notice.terminal_channel_interrupted");
                _terminalChannelUntil.remove(player);
                it.remove();
                continue;
            }

            long remaining = Math.max(0L, finishAt - now);
            com.houzicore.shared.common.util.UtilTextBottom.display(com.houzicore.shared.common.actionbar.ActionBarChannel.GAME_STATUS, C.cAqua + "⚙ " + C.cWhite + "Hacking Terminal: " + C.cAqua + (remaining / 1000L + 1) + "s", player);

            if (now >= finishAt)
            {
                _terminalChannelUntil.remove(player);
                it.remove();
                activateTerminal(player, terminal);
            }
        }
    }

    @EventHandler
    public void TerminalEffectUpdate(UpdateEvent event)
    {
        if (!IsLive() || event.getType() != UpdateType.FAST)
            return;

        if (System.currentTimeMillis() < _terminalBlindUntil)
        {
            for (Player seeker : _seekers.GetPlayers(true))
                seeker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 30, 0, false, true));
        }
    }

    @EventHandler
    public void Timer(UpdateEvent event)
    {
        if (GetState() != GameState.Live)
            return;

        if (event.getType() != UpdateType.TICK)
            return;

        long elapsed = System.currentTimeMillis() - _phaseStartTime;

        long totalTime = getPhaseDuration();

        long tLeft = totalTime - elapsed;
        int sec = (int)(tLeft / 1000);
        boolean shouldBroadcastCountdown = sec > 0 && (sec == 10 || sec == 5 || sec <= 3);
        if (shouldBroadcastCountdown && sec != _lastCountdown) {
            _lastCountdown = sec;
            String sub = "Time is running out!";
            if (_phase == Phase.PREP) sub = "Hunters releasing soon...";
            if (_phase == Phase.HUNT) sub = "Panic is approaching...";
            if (_phase == Phase.PANIC) sub = "Chaos is approaching...";
            
            for (Player p : UtilServer.getPlayers()) {
                sendPropRushSummary(p, "Timer", "เวลา",
                        sec + "s" + C.cGray + " • " + sub,
                        sec + " วิ" + C.cGray + " • " + getCountdownSubtitle(p, sub));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            }
        }

        if (_phase == Phase.PREP)
        {
            long timeLeft = _prepTime - elapsed;
            if (timeLeft <= 0)
            {
                _phaseStartTime = System.currentTimeMillis();
                _phase = Phase.HUNT;

                // Seeker Items
                for (Player seeker : _seekers.GetPlayers(true))
                {
                    seeker.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
                    seeker.removePotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS);
                    GetKit(seeker).ApplyKit(seeker);
                    GiveSeekerItems(seeker);
                }

                // Remove Barrier
                for (Location loc : WorldData.GetDataLocs("BLACK"))
                    loc.getBlock().setType(Material.AIR);

                announcePropRushSummary(pr(null, "prop_rush.phase.hunt"), pr(null, "prop_rush.phase.hunt"),
                        "Hunters released!",
                        "เริ่มการค้นหา!");

                for (Player hider : _hiders.GetPlayers(true))
                {
                    sendPropRushSummary(hider, "Hider Objective", "เป้าหมายฝ่ายแอบ",
                            "Hack Terminals and collect Gold to restore HP.",
                            "แฮ็ก Terminal และเก็บ Gold เพื่อฟื้นพลังชีวิต");
                }

                for (Player seeker : _seekers.GetPlayers(true))
                {
                    sendPropRushSummary(seeker, "Hunter Objective", "เป้าหมายฝ่ายหา",
                            "Trigger Search Relays and deny Gold pickups.",
                            "เปิด Search Relay และตัด Gold ไม่ให้อีกฝ่ายเก็บ");
                }
                
                // Play sound for all
                for (Player player : UtilServer.getPlayers())
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            }
        }
        else if (_phase == Phase.HUNT)
        {
            long timeLeft = _huntTime - elapsed;
            if (timeLeft <= 0)
            {
                beginPanicPhase();
            }
        }
        else if (_phase == Phase.PANIC)
        {
            long timeLeft = _panicTime - elapsed;
            if (timeLeft <= 0)
            {
                beginChaosPhase();
            }
        }
        else if (_phase == Phase.CHAOS)
        {
            long timeLeft = _chaosTime - elapsed;

            if (timeLeft <= 0)
            {
                WriteScoreboard();

                // Calculate Points and Sort
                ArrayList<Player> places = new ArrayList<>(GetPlayers(false));
                places.sort((p1, p2) -> Integer.compare(
                    _points.getOrDefault(p2, 0),
                    _points.getOrDefault(p1, 0)
                ));


                AnnounceEnd(places);

                // Gems
                if (places.size() >= 1)
                    AddGems(places.get(0), 30, "1st Place", false, false);
                if (places.size() >= 2)
                    AddGems(places.get(1), 20, "2nd Place", false, false);
                if (places.size() >= 3)
                    AddGems(places.get(2), 10, "3rd Place", false, false);

                for (Player player : GetPlayers(false))
                {
                    if (player.isOnline())
                    {
                        // Everyone else gets participation
                        if (!places.contains(player) || places.indexOf(player) >= 3)
                        {
                            AddGems(player, 5, "Participation", false, false);
                        }
                    }
                }

                SetState(GameState.End);
            }
        }
    }

    @Override
    @EventHandler
    public void ScoreboardUpdate(UpdateEvent event)
    {
        if (event.getType() != UpdateType.FAST)
            return;

        if (GetState() != GameState.Live && GetState() != GameState.End)
            return;

        WriteScoreboard();
    }

    @EventHandler
    public void PhasePressure(UpdateEvent event)
    {
        if (!IsLive())
            return;

        long now = System.currentTimeMillis();

        if (event.getType() == UpdateType.SEC)
        {
            if (_phase == Phase.PANIC)
            {
                if (isDangerZoneActive() && now >= _dangerZoneActiveUntil)
                {
                    clearDangerZone();
                }

                if (!isDangerZoneActive() && _nextDangerZoneAt > 0 && now >= _nextDangerZoneAt)
                {
                    activateDangerZone();
                }
            }
            else if (_phase == Phase.CHAOS && _nextChaosPulseAt > 0 && now >= _nextChaosPulseAt)
            {
                for (Player hider : _hiders.GetPlayers(true))
                {
                    revealHider(hider, 80);
                    hider.playSound(hider.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1f);
                }

                _nextChaosPulseAt = now + 25000L;
            }
        }

        if (event.getType() != UpdateType.FAST)
            return;

        if (_phase == Phase.PANIC && isDangerZoneActive())
        {
            for (int i = 0; i < 360; i += 20)
            {
                double radians = Math.toRadians(i);
                double x = Math.cos(radians) * DANGER_ZONE_RADIUS;
                double z = Math.sin(radians) * DANGER_ZONE_RADIUS;
                Location point = _dangerZoneCenter.clone().add(x, 0.2, z);
                UtilParticle.PlayParticle(ParticleType.RED_DUST, point, 0.05f, 0.05f, 0.05f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
                UtilParticle.PlayParticle(ParticleType.FLAME, point.clone().add(0, 0.3, 0), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
            }

            UtilParticle.PlayParticle(ParticleType.FLAME, _dangerZoneCenter.clone().add(0, 0.5, 0), 0.6f, 0.2f, 0.6f, 0.01f, 4, ViewDist.NORMAL, UtilServer.getPlayers());

            for (Player hider : _hiders.GetPlayers(true))
            {
                Location flatPlayer = hider.getLocation().clone();
                flatPlayer.setY(_dangerZoneCenter.getY());

                if (UtilMath.offset(flatPlayer, _dangerZoneCenter) <= DANGER_ZONE_RADIUS)
                {
                    revealHider(hider, 60);

                    if (_dangerZoneWarned.add(hider))
                    {
                        sendPropRushNoticeKey(hider, "prop_rush.notice.danger_zone_caught");
                        hider.playSound(hider.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.8f, 1.2f);
                    }
                }
            }
        }
    }

    @EventHandler
    public void DelayedActions(UpdateEvent event)
    {
        if (event.getType() != UpdateType.TICK)
            return;

        long now = System.currentTimeMillis();

        Iterator<java.util.Map.Entry<Player, Long>> hiderItemIt = _pendingHiderItems.entrySet().iterator();
        while (hiderItemIt.hasNext())
        {
            java.util.Map.Entry<Player, Long> entry = hiderItemIt.next();
            if (now < entry.getValue())
                continue;

            Player hider = entry.getKey();
            hiderItemIt.remove();

            if (hider != null && hider.isOnline() && _hiders.HasPlayer(hider))
                GiveHiderItems(hider);
        }

        Iterator<java.util.Map.Entry<Player, Long>> disguiseIt = _pendingInitialDisguise.entrySet().iterator();
        while (disguiseIt.hasNext())
        {
            java.util.Map.Entry<Player, Long> entry = disguiseIt.next();
            if (now < entry.getValue())
                continue;

            Player player = entry.getKey();
            disguiseIt.remove();

            if (player == null || !player.isOnline())
                continue;

            Form form = _forms.get(player);
            if (form == null)
                continue;

            form.Apply();
        
        // Block Hunt: Use hideEntity/showEntity to refresh visuals without touching Tab List
        Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
            for (org.bukkit.entity.Player p : com.houzicore.shared.common.util.UtilServer.getPlayers()) {
                if (!p.equals(player) && p.canSee(player)) {
                    p.hideEntity(Manager.getPlugin(), player);
                    p.showEntity(Manager.getPlugin(), player);
                }
            }
        }, 5L);
            applyPropTierHealth(player, form);
            Bukkit.getPluginManager().callEvent(new PlayerChangeFormEvent(player, form));
        }

        Iterator<java.util.Map.Entry<Player, Long>> lastStandIt = _pendingLastStandSound.entrySet().iterator();
        while (lastStandIt.hasNext())
        {
            java.util.Map.Entry<Player, Long> entry = lastStandIt.next();
            if (now < entry.getValue())
                continue;

            Player player = entry.getKey();
            lastStandIt.remove();

            if (player != null && player.isOnline())
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 1f);
        }

        Iterator<java.util.Map.Entry<Player, Long>> pulseIt = _pendingScannerPulseFeedback.entrySet().iterator();
        while (pulseIt.hasNext())
        {
            java.util.Map.Entry<Player, Long> entry = pulseIt.next();
            if (now < entry.getValue())
                continue;

            Player player = entry.getKey();
            boolean found = _pendingScannerPulseFound.getOrDefault(player, false);
            pulseIt.remove();
            _pendingScannerPulseFound.remove(player);

            if (player == null || !player.isOnline())
                continue;

            if (found)
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            else
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
        }

        Iterator<java.util.Map.Entry<Player, Long>> bountyIt = _pendingBountyDashCheck.entrySet().iterator();
        while (bountyIt.hasNext())
        {
            java.util.Map.Entry<Player, Long> entry = bountyIt.next();
            if (now < entry.getValue())
                continue;

            Player player = entry.getKey();
            bountyIt.remove();

            if (player == null || !player.isOnline() || !_seekers.HasPlayer(player))
                continue;

            for (Player hider : _hiders.GetPlayers(true))
            {
                if (UtilMath.offset(player, hider) <= 3.0)
                {
                    hider.damage(6.0, player);
                    player.getWorld().playSound(hider.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1.5f);
                    UtilParticle.PlayParticle(ParticleType.CRIT, hider.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0.1f, 15, ViewDist.NORMAL, UtilServer.getPlayers());
                    break;
                }
            }
        }

        Iterator<java.util.Map.Entry<Player, Long>> hunterRespawnIt = _pendingHunterRespawnKit.entrySet().iterator();
        while (hunterRespawnIt.hasNext())
        {
            java.util.Map.Entry<Player, Long> entry = hunterRespawnIt.next();
            if (now < entry.getValue())
                continue;

            Player player = entry.getKey();
            hunterRespawnIt.remove();

            if (player == null || !player.isOnline() || !_seekers.HasPlayer(player))
                continue;

            if (_phase == Phase.HUNT || _phase == Phase.PANIC || _phase == Phase.CHAOS)
            {
                Kit kit = GetKit(player);
                if (kit != null)
                    kit.ApplyKit(player);
                GiveSeekerItems(player);
                player.updateInventory();
            }
        }

        Iterator<java.util.Map.Entry<Player, Long>> tauntRewardIt = _pendingTauntReward.entrySet().iterator();
        while (tauntRewardIt.hasNext())
        {
            java.util.Map.Entry<Player, Long> entry = tauntRewardIt.next();
            if (now < entry.getValue())
                continue;

            Player player = entry.getKey();
            tauntRewardIt.remove();

            if (player == null || !player.isOnline())
                continue;
            if (!_hiders.HasPlayer(player))
                continue;
            if (!_tauntPending.remove(player))
                continue;

            int pts = _points.getOrDefault(player, 0) + 5;
            _points.put(player, pts);
            sendPropRushMessageKey(player, "prop_rush.feedback.taunt_survived");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
        }

        Iterator<java.util.Map.Entry<com.houzicore.shared.core.hologram.Hologram, Long>> hologramIt = _temporaryHolograms.entrySet().iterator();
        while (hologramIt.hasNext())
        {
            java.util.Map.Entry<com.houzicore.shared.core.hologram.Hologram, Long> entry = hologramIt.next();
            if (now < entry.getValue())
                continue;

            entry.getKey().stop();
            hologramIt.remove();
        }
    }

    private void WriteScoreboard()
    {
        Scoreboard.Reset();
        long elapsed = System.currentTimeMillis() - _phaseStartTime;
        long timeLeft = getPhaseTimeRemaining(elapsed);

        Scoreboard.Write(C.cYellow + "⌚ " + C.cWhite + C.Bold + pr(null, "prop_rush.scoreboard.phase"));
        Scoreboard.Write(getPhaseBoardTitle(timeLeft));
        Scoreboard.Write(" ");
        Scoreboard.Write(getCountsBoardComponentLine());
        Scoreboard.Write("  ");
        Scoreboard.Write(C.cAqua + "🎯 " + C.cWhite + C.Bold + pr(null, "prop_rush.scoreboard.objective"));
        Scoreboard.Write(getPrimaryObjectiveLine());
        Scoreboard.Write(getSecondaryObjectiveLine());
        Scoreboard.Write("   ");

        ArrayList<Player> top = new ArrayList<>(GetPlayers(false));
        top.sort((p1, p2) -> Integer.compare(_points.getOrDefault(p2, 0), _points.getOrDefault(p1, 0)));

        Player leader = null;
        if (!top.isEmpty() && _points.getOrDefault(top.get(0), 0) > 0)
        {
            leader = top.get(0);
        }

        if (leader != null)
        {
            Scoreboard.Write(Component.text()
                    .append(PlayerHeadUtil.buildInlineHead(leader, true))
                    .append(Component.text("Top Points", NamedTextColor.WHITE).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD))
                    .build());
        }
        else
        {
            Scoreboard.Write(C.cAqua + "✦ " + C.cWhite + C.Bold + "Top Points");
        }

        int count = 0;
        for (Player p : top)
        {
            int pts = _points.getOrDefault(p, 0);
            if (pts <= 0)
                continue;

            if (count == 0)
                Scoreboard.Write(formatLeaderboardComponentLine(count + 1, p, pts));
            else
                Scoreboard.Write(formatLeaderboardLine(count + 1, p.getName(), pts));
            count++;

            if (count >= 3)
                break;
        }

        while (count < 3)
        {
            Scoreboard.Write(formatEmptyLeaderboardLine(count + 1));
            count++;
        }

        Scoreboard.Draw();
    }

    private long getPhaseTimeRemaining(long elapsed)
    {
        if (_phase == Phase.PREP)
            return _prepTime - elapsed;
        if (_phase == Phase.HUNT)
            return _huntTime - elapsed;
        if (_phase == Phase.PANIC)
            return _panicTime - elapsed;
        return _chaosTime - elapsed;
    }

    private String getPhaseBoardTitle(long timeLeft)
    {
        String timer = formatScoreboardTimer(timeLeft);

        if (_phase == Phase.PREP)
            return C.cAqua + pr(null, "prop_rush.phase.prep") + C.cGray + " • " + C.cWhite + timer;
        if (_phase == Phase.HUNT)
            return C.cAqua + pr(null, "prop_rush.phase.hunt") + C.cGray + " • " + C.cWhite + timer;
        if (_phase == Phase.PANIC)
            return C.cAqua + pr(null, "prop_rush.phase.panic") + C.cGray + " • " + C.cYellow + timer;
        return C.cAqua + pr(null, "prop_rush.phase.chaos") + C.cGray + " • " + C.cRed + timer;
    }

    private String getCountsBoardLine()
    {
        return formatBoardSubLine(C.cGray + "🧊 Props " + C.cAqua + _hiders.GetPlayers(true).size()
                + C.cGray + "  •  "
                + C.cGray + "👥 Hunters " + C.cRed + _seekers.GetPlayers(true).size());
    }

    private Component getCountsBoardComponentLine()
    {
        return Component.text()
                .append(Component.text("  ", NamedTextColor.DARK_GRAY))
                .append(SpriteUtil.buildInlineSprite("blocks", "block/grass_block_side", true))
                .append(Component.text("Props ", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(_hiders.GetPlayers(true).size()), NamedTextColor.AQUA))
                .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                .append(buildSteveHead(true))
                .append(Component.text("Hunters ", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(_seekers.GetPlayers(true).size()), NamedTextColor.RED))
                .build();
    }

    private Component buildSteveHead(boolean trailingSpace)
    {
        Component head = PlayerHeadUtil.buildSteveHead(trailingSpace);
        if (!head.equals(Component.empty()))
        {
            return head;
        }

        return Component.text("☻ ", NamedTextColor.RED);
    }

    private String getLeadBoardText()
    {
        Player leader = null;
        int leaderPoints = 0;

        for (Player player : GetPlayers(false))
        {
            int points = _points.getOrDefault(player, 0);
            if (points > leaderPoints)
            {
                leader = player;
                leaderPoints = points;
            }
        }

        if (leader == null || leaderPoints <= 0)
            return "No leader yet";

        return "Lead " + trimBoardName(leader.getName(), 10) + " (" + leaderPoints + ")";
    }

    private String getPrimaryObjectiveLine()
    {
        if (_phase == Phase.PREP)
            return formatObjectiveBoardLine("objective-prep-primary", pr(null, "prop_rush.scoreboard.objective_prep_primary", net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("props", String.valueOf(_hiders.GetPlayers(true).size()))));

        if (_phase == Phase.HUNT)
        {
            if (!_terminalLocations.isEmpty())
                return formatObjectiveBoardLine("objective-hunt-terminal", "Hiders hack Terminal / Hunters trigger Relay");

            int found = Math.max(0, getInitialHiderCount() - _hiders.GetPlayers(true).size());
            return formatObjectiveBoardLine("objective-hunt-primary", pr(null, "prop_rush.scoreboard.objective_hunt_primary", net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("found", String.valueOf(found)), net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("total", String.valueOf(getInitialHiderCount()))));
        }

        if (_phase == Phase.PANIC)
        {
            if (isDangerZoneActive())
                return formatObjectiveBoardLine("objective-panic-danger", "Rotate through zone and contest Gold (" + formatScoreboardTimer(getDangerZoneTimeRemaining()) + ")");

            return formatObjectiveBoardLine("objective-panic-primary", pr(null, "prop_rush.scoreboard.objective_panic_primary", net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("time", formatScoreboardTimer(getNextDangerZoneTime()))));
        }

        return formatObjectiveBoardLine("objective-chaos-primary", pr(null, "prop_rush.scoreboard.objective_chaos_primary", net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("props", String.valueOf(_hiders.GetPlayers(true).size()))));
    }

    private String getSecondaryObjectiveLine()
    {
        if (_phase == Phase.PREP)
            return formatObjectiveBoardLine("objective-prep-secondary",
                    "Hunters release (" + formatScoreboardTimer(getPhaseTimeRemaining(System.currentTimeMillis() - _phaseStartTime)) + ") • Gold heals HP only");

        if (_phase == Phase.HUNT)
            return formatObjectiveBoardLine("objective-hunt-secondary", "Gold restores HP • Relay CD " + formatScoreboardTimer(Math.max(0L, _hunterRelayCooldownUntil - System.currentTimeMillis())));

        if (_phase == Phase.PANIC)
            return formatObjectiveBoardLine("objective-panic-secondary", "Keep moving • Gold is your only heal");

        long pulse = _nextChaosPulseAt > 0 ? Math.max(0L, _nextChaosPulseAt - System.currentTimeMillis()) : 0L;
        return formatObjectiveBoardLine("objective-chaos-secondary", "No regen • Next pulse (" + formatScoreboardTimer(pulse) + ")");
    }

    private int getInitialHiderCount()
    {
        int total = _hiders.GetPlayers(false).size();
        return Math.max(total, _hiders.GetPlayers(true).size());
    }

    private int getHighestScore()
    {
        int highest = 0;

        for (Player player : GetPlayers(false))
        {
            highest = Math.max(highest, _points.getOrDefault(player, 0));
        }

        return highest;
    }

    private String formatLeaderboardLine(int position, String name, int points)
    {
        String color = position == 1 ? C.cYellow : position == 2 ? C.cGold : C.cRed;
        return formatBoardSubLine(C.cGray + position + ". " + C.cGreen + trimBoardName(name, 10) + C.cGray + " - " + color + points);
    }

    private Component formatLeaderboardComponentLine(int position, Player player, int points)
    {
        NamedTextColor pointsColor = position == 1 ? NamedTextColor.YELLOW : position == 2 ? NamedTextColor.GOLD : NamedTextColor.RED;

        return Component.text()
                .append(Component.text("  ", NamedTextColor.DARK_GRAY))
                .append(Component.text(position + ". ", NamedTextColor.GRAY))
                .append(PlayerHeadUtil.buildInlineHead(player, true))
                .append(Component.text(trimBoardName(player.getName(), 10), NamedTextColor.GREEN))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(points), pointsColor))
                .build();
    }

    private String formatEmptyLeaderboardLine(int position)
    {
        return formatBoardSubLine(C.cGray + position + ". ---");
    }

    private String trimBoardName(String name, int maxLength)
    {
        if (name == null)
            return "";

        return name.length() > maxLength ? name.substring(0, maxLength) : name;
    }

    public boolean useVanillaItemCooldown(Player player, String ability, long cooldownMs)
    {
        Material heldMaterial = player.getInventory().getItemInMainHand() == null
                ? Material.AIR
                : player.getInventory().getItemInMainHand().getType();
        return useVanillaItemCooldown(player, ability, cooldownMs, heldMaterial);
    }

    public boolean useVanillaItemCooldown(Player player, String ability, long cooldownMs, Material cooldownMaterial)
    {
        if (!Recharge.Instance.use(player, ability, cooldownMs, false, false))
            return false;

        if (cooldownMaterial != null && cooldownMaterial != Material.AIR)
        {
            int cooldownTicks = (int)Math.max(1L, Math.round(cooldownMs / 50.0));
            player.setCooldown(cooldownMaterial, cooldownTicks);
        }

        return true;
    }

    private String formatScoreboardTimer(long millis)
    {
        long totalSeconds = Math.max(0L, millis / 1000L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;

        if (minutes > 0L)
            return minutes + "m " + seconds + "s";

        return totalSeconds + "s";
    }

    private String formatBoardSubLine(String text)
    {
        return C.cGray + "  " + text;
    }

    private String formatObjectiveBoardLine(String key, String text)
    {
        return formatBoardSubLine(C.cAqua + scrollBoardText(key, text, 24));
    }

    private String scrollBoardText(String key, String text, int maxVisibleChars)
    {
        if (text == null)
            return "";

        if (text.length() <= maxVisibleChars)
            return text;

        String padded = text + "   " + text;
        int cycle = text.length() + 3;
        int offset = (int)((System.currentTimeMillis() / 350L) % cycle);
        return padded.substring(offset, offset + maxVisibleChars);
    }

    @Override
    public String GetBossBarText()
    {
        return null; // Handled per-player via updateRoleObjectiveBossBar
    }

    @EventHandler
    public void updateRoleObjectiveBossBar(UpdateEvent event)
    {
        if (event.getType() != UpdateType.FASTER) return;
        if (GetState() != GameState.Live) return;

        double health = GetBossBarHealth();
        org.bukkit.boss.BarColor color = GetBossBarColor();
        long elapsed = System.currentTimeMillis() - _phaseStartTime;
        long phaseLeft = Math.max(0L, getPhaseTimeRemaining(elapsed) / 1000L);
        String prefix = getPropRushChatPrefix();

        for (Player p : UtilServer.getPlayers())
        {
            if (!p.isOnline()) continue;

            boolean isHunter = _seekers != null && _seekers.HasPlayer(p);
            String roleKey = isHunter ? "hunter" : "hider";
            String text = "";

            if (_phase == Phase.PREP)
            {
                text = prefix + C.cWhite + pr(p, "prop_rush.bossbar." + roleKey + ".prep") + C.cGray + " - " + C.cAqua + phaseLeft + "s";
            }
            else if (_phase == Phase.HUNT)
            {
                if (!_terminalLocations.isEmpty() && _usedTerminals.size() < _terminalLocations.size())
                    text = prefix + C.cWhite + pr(p, "prop_rush.bossbar." + roleKey + ".hunt_terminals") + C.cGray + " - " + C.cAqua + phaseLeft + "s";
                else
                    text = prefix + C.cWhite + pr(p, "prop_rush.bossbar." + roleKey + ".hunt") + C.cGray + " - " + C.cAqua + phaseLeft + "s";
            }
            else if (_phase == Phase.PANIC)
            {
                if (isDangerZoneActive())
                    text = prefix + C.cWhite + pr(p, "prop_rush.bossbar." + roleKey + ".panic_danger") + C.cGray + " - " + C.cRed + Math.max(0L, getDangerZoneTimeRemaining() / 1000L) + "s";
                else
                    text = prefix + C.cWhite + pr(p, "prop_rush.bossbar." + roleKey + ".panic") + C.cGray + " - in " + C.cYellow + Math.max(0L, getNextDangerZoneTime() / 1000L) + "s";
            }
            else
            {
                long pulse = _nextChaosPulseAt > 0 ? Math.max(0L, _nextChaosPulseAt - System.currentTimeMillis()) / 1000L : 0L;
                text = prefix + C.cWhite + pr(p, "prop_rush.bossbar." + roleKey + ".chaos") + C.cGray + " - Pulse " + C.cRed + pulse + "s" + C.cGray + " - " + C.cAqua + _hiders.GetPlayers(true).size() + " props";
            }

            UtilTextTop.displayProgress(text, health, color, p);
        }
    }

    @Override
    public double GetBossBarHealth()
    {
        long elapsed = System.currentTimeMillis() - _phaseStartTime;
        if (_phase == Phase.PREP) return Math.max(0.01, 1.0 - (double)elapsed / _prepTime);
        if (_phase == Phase.HUNT) return Math.max(0.01, 1.0 - (double)elapsed / _huntTime);
        if (_phase == Phase.PANIC) return Math.max(0.01, 1.0 - (double)elapsed / _panicTime);
        return Math.max(0.01, 1.0 - (double)elapsed / _chaosTime);
    }

    @Override
    public org.bukkit.boss.BarColor GetBossBarColor()
    {
        if (_phase == Phase.CHAOS)
            return org.bukkit.boss.BarColor.RED;

        if (_phase == Phase.PANIC && isDangerZoneActive())
            return org.bukkit.boss.BarColor.YELLOW;

        return org.bukkit.boss.BarColor.BLUE;
    }

    @EventHandler
    public void CreatureFormStabilizer(UpdateEvent event)
    {
        if (!IsLive() || event.getType() != UpdateType.FAST)
            return;

        for (java.util.Map.Entry<Player, Form> entry : _forms.entrySet())
        {
            Player player = entry.getKey();
            Form form = entry.getValue();

            if (!(form instanceof CreatureForm) || player == null || !player.isOnline())
                continue;

            org.bukkit.util.Vector velocity = player.getVelocity();
            double horizontal = Math.abs(velocity.getX()) + Math.abs(velocity.getZ());
            if (!player.isOnGround() || horizontal <= 0.0001 || horizontal >= 0.055)
                continue;

            player.setVelocity(new org.bukkit.util.Vector(0, velocity.getY(), 0));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void SolidifiedRightClickBridge(PlayerInteractEvent event)
    {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getClickedBlock() == null)
            return;

        for (Form form : _forms.values())
        {
            if (!(form instanceof BlockForm))
                continue;

            BlockForm blockForm = (BlockForm) form;
            if (blockForm.GetBlock() == null || !blockForm.GetBlock().getLocation().equals(event.getClickedBlock().getLocation()))
                continue;

            event.setCancelled(true);
            event.getPlayer().sendBlockChange(blockForm.GetBlock().getLocation(), blockForm.GetMaterial().createBlockData());
            return;
        }
    }

    @EventHandler
    public void ChaosBorderPressure(UpdateEvent event)
    {
        if (!IsLive() || _phase != Phase.CHAOS || event.getType() != UpdateType.SEC || WorldData == null || WorldData.World == null)
            return;

        org.bukkit.WorldBorder border = WorldData.World.getWorldBorder();
        double half = border.getSize() / 2.0;
        double centerX = border.getCenter().getX();
        double centerZ = border.getCenter().getZ();

        for (Player player : GetPlayers(true))
        {
            Location loc = player.getLocation();
            if (!loc.getWorld().equals(WorldData.World))
                continue;

            boolean outside = Math.abs(loc.getX() - centerX) > half || Math.abs(loc.getZ() - centerZ) > half;
            if (!outside)
                continue;

            player.damage(4.0);
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WITHER, 40, 0, false, true));
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.7f, 0.9f);
            sendPropRushSummary(player, "Border", "ขอบเขต",
                    "Return inside now. The chaos ring is no longer safe to tank.",
                    "รีบกลับเข้าวงเดี๋ยวนี้ ขอบโกลาหลยืนแช่ต่อไม่ได้แล้ว");
        }
    }

    // ----------------------------------------
    // PROP RUSH NEW ABILITIES & MECHANICS
    // ----------------------------------------

    @EventHandler
    public void HiderAbilities(PlayerInteractEvent event)
    {
        if (GetState() != GameState.Live || _phase == Phase.PREP)
            return;

        Player player = event.getPlayer();

        if (!_hiders.HasPlayer(player))
            return;

        if (!UtilEvent.isAction(event, ActionType.R))
            return;

        ItemStack item = player.getItemInHand();
        if (item == null) return;

        if (item.getType() == Material.NOTE_BLOCK)
        {
            event.setCancelled(true);
            if (!useVanillaItemCooldown(player, "Fake Sound Ping", getAbilityCooldown(player, 28000, true)))
                return;

            Location ping = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(6.0));
            ping = ping.getBlock().getLocation().add(0.5, 0.5, 0.5);

            int roll = UtilMath.r(3);
            if (roll == 0)
                ping.getWorld().playSound(ping, Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 0.9f);
            else if (roll == 1)
                ping.getWorld().playSound(ping, Sound.BLOCK_STONE_BREAK, 1f, 1.1f);
            else
                ping.getWorld().playSound(ping, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.9f, 1.4f);

            UtilParticle.PlayParticle(ParticleType.CLOUD, ping, 0.2f, 0.2f, 0.2f, 0f, 6, ViewDist.NORMAL, UtilServer.getPlayers());
            sendPropRushNoticeKey(player, "prop_rush.notice.fake_sound_ping_deployed");
            return;
        }

        if (event.getClickedBlock() != null && !player.isInsideVehicle())
        {
            Location terminal = getNearestTerminal(event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5));
            if (terminal != null && UtilMath.offset(terminal, event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5)) <= 2.0)
            {
                event.setCancelled(true);

                if (_seekers.HasPlayer(player.getName(), true))
                {
                    long relayCd = Math.max(0L, _hunterRelayCooldownUntil - System.currentTimeMillis());
                    if (System.currentTimeMillis() < _terminalDisruptionUntil)
                    {
                        sendPropRushNoticeKey(player, "prop_rush.notice.search_relay_blocked");
                        return;
                    }

                    if (relayCd > 0)
                    {
                        sendPropRushNoticeKey(player, "prop_rush.notice.search_relay_recharging",
                                net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("time", formatScoreboardTimer(relayCd)));
                        return;
                    }

                    activateHunterRelay(player, terminal);
                    return;
                }

                if (_usedTerminals.contains(getLocationKey(terminal)))
                {
                    sendPropRushNoticeKey(player, "prop_rush.notice.terminal_already_used");
                    return;
                }

                _terminalChannel.put(player, terminal);
                _terminalChannelUntil.put(player, System.currentTimeMillis() + TERMINAL_CHANNEL_MS);
                sendPropRushNoticeKey(player, "prop_rush.notice.terminal_hold_sneak");
                return;
            }
        }

        // Decoy (Actual Prop Block/Mob)
        if (item.getType() == Material.ARMOR_STAND)
        {
            Block target = player.getLocation().getBlock();
            if (target.getType() != Material.AIR && target.getType() != Material.WATER) {
                sendPropRushMessageKey(player, "prop_rush.feedback.decoy_empty_space");
                return;
            }

            if (!useVanillaItemCooldown(player, "Decoy", getAbilityCooldown(player, 20000, true)))
                return;

            Form form = _forms.get(player);
            if (form != null)
            {
                Location loc = player.getLocation();
                
                if (form instanceof BlockForm) {
                    Material decoyMat = ((BlockForm)form).GetMaterial();
                    target.setType(decoyMat);
                    _decoys.put(target, System.currentTimeMillis() + 10000);
                } else if (form instanceof CreatureForm) {
                    org.bukkit.entity.LivingEntity mob = (org.bukkit.entity.LivingEntity) loc.getWorld().spawnEntity(loc, ((CreatureForm)form).GetEntityType());
                    mob.setAI(false);
                    mob.setCustomName("DecoyMob");
                    mob.setCustomNameVisible(false);
                    _decoyMobs.put(mob, System.currentTimeMillis() + 10000);
                }
                
                player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f, 1f);
                markHiderSkillUse(player);
            }
        }
        
        // Phase Shift (Ender Pearl)
        else if (item.getType() == Material.ENDER_PEARL)
        {
            event.setCancelled(true);
            if (!useVanillaItemCooldown(player, "Phase Shift", getAbilityCooldown(player, 15000, true)))
                return;

            UtilParticle.PlayParticle(ParticleType.PORTAL, player.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            
            Location target = player.getLocation().add(player.getLocation().getDirection().multiply(4));
            target.setY(player.getLocation().getY() + 0.5);
            if (target.getBlock().getType().isSolid()) {
                target.setY(target.getBlockY() + 1); 
            }
            player.teleport(target);
            player.getWorld().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
            
            UtilParticle.PlayParticle(ParticleType.PORTAL, player.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
            markHiderSkillUse(player);
        }
        else if (item.getType() == Material.FIREWORK_STAR)
        {
            event.setCancelled(true);
            if (!useVanillaItemCooldown(player, "Bomb Shell", getAbilityCooldown(player, 22000, true)))
                return;

            Form form = _forms.get(player);
            if (form == null || !spawnBombTrap(form, player.getLocation(), System.currentTimeMillis() + BOMB_BUG_TRAP_DURATION_MS))
            {
                sendPropRushMessageKey(player, "prop_rush.feedback.bomb_shell_clear_space");
                return;
            }

            player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.7f, 1.4f);
            UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(0, 0.6, 0), 0.25f, 0.25f, 0.25f, 0.02f, 10, ViewDist.NORMAL, UtilServer.getPlayers());
            sendPropRushMessageKey(player, "prop_rush.feedback.bomb_shell_armed");
            markHiderSkillUse(player);
        }
        else if (item.getType() == Material.TRIPWIRE_HOOK)
        {
            event.setCancelled(true);
            if (!useVanillaItemCooldown(player, "Secret Passage", getAbilityCooldown(player, 25000, true)))
                return;

            Location exit = findLocksmithExit(player);
            if (exit == null)
            {
                sendPropRushMessageKey(player, "prop_rush.feedback.secret_passage_no_route");
                return;
            }

            Location from = player.getLocation().clone();
            UtilParticle.PlayParticle(ParticleType.END_ROD, from.add(0, 1, 0), 0.3f, 0.6f, 0.3f, 0.02f, 16, ViewDist.NORMAL, UtilServer.getPlayers());
            player.playSound(player.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 1.1f);
            player.teleport(exit);
            UtilParticle.PlayParticle(ParticleType.END_ROD, exit.clone().add(0, 1, 0), 0.3f, 0.6f, 0.3f, 0.02f, 16, ViewDist.NORMAL, UtilServer.getPlayers());
            player.playSound(exit, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 0.8f, 1.4f);
            markHiderSkillUse(player);
        }
        else if (item.getType() == Material.ECHO_SHARD)
        {
            event.setCancelled(true);
            if (!useVanillaItemCooldown(player, "Mirror Image", getAbilityCooldown(player, 18000, true)))
                return;

            Form form = _forms.get(player);
            if (form == null)
                return;

            Vector facing = player.getLocation().getDirection().clone().setY(0).normalize();
            if (facing.lengthSquared() == 0)
                facing = new Vector(1, 0, 0);
            Vector side = new Vector(-facing.getZ(), 0, facing.getX()).normalize();

            ArrayList<Location> candidates = new ArrayList<>();
            candidates.add(player.getLocation().clone().add(side.clone().multiply(1.25)));
            candidates.add(player.getLocation().clone().add(side.clone().multiply(-1.25)));
            candidates.add(player.getLocation().clone().add(facing.clone().multiply(-1.1)));

            int created = 0;
            long expireAt = System.currentTimeMillis() + MIMIC_ILLUSION_DURATION_MS;
            for (Location candidate : candidates)
            {
                if (spawnTimedFormDecoy(form, candidate, expireAt))
                {
                    created++;
                }

                if (created >= 2)
                    break;
            }

            if (created == 0)
            {
                sendPropRushMessageKey(player, "prop_rush.feedback.mirror_image_no_room");
                return;
            }

            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 60, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_TAKEN, 1f, 1.4f);
            UtilParticle.PlayParticle(ParticleType.CLOUD, player.getLocation().add(0, 0.5, 0), 0.4f, 0.3f, 0.4f, 0.02f, 18, ViewDist.NORMAL, UtilServer.getPlayers());
            sendPropRushMessageKey(player, "prop_rush.feedback.mirror_image_split",
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("count", String.valueOf(created)));
            markHiderSkillUse(player);
        }

        // Cat Taunt (Jukebox)
        else if (item.getType() == Material.JUKEBOX)
        {
            event.setCancelled(true);
            long now = System.currentTimeMillis();
            long lastTaunt = _tauntCooldown.getOrDefault(player, 0L);
            if (now - lastTaunt < TAUNT_COOLDOWN_MS)
            {
                long remaining = (TAUNT_COOLDOWN_MS - (now - lastTaunt)) / 1000;
                sendPropRushMessageKey(player, "prop_rush.feedback.cat_taunt_cooldown",
                        net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("time", String.valueOf(remaining)));
                return;
            }

            _tauntCooldown.put(player, now);
            _tauntPending.add(player);

            for (Player other : UtilServer.getPlayers())
            {
                if (UtilMath.offset(player, other) <= 15.0)
                    other.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
            }

            sendPropRushMessageKey(player, "prop_rush.feedback.cat_taunt_ready");
            _pendingTauntReward.put(player, now + 15000L);
        }

        // Signal Flare (Firework Rocket)
        else if (item.getType() == Material.FIREWORK_ROCKET)
        {
            event.setCancelled(true);
            long now = System.currentTimeMillis();
            long lastFlare = _fireworkCooldown.getOrDefault(player, 0L);
            if (now - lastFlare < FIREWORK_COOLDOWN_MS)
            {
                long remaining = (FIREWORK_COOLDOWN_MS - (now - lastFlare)) / 1000;
                sendPropRushMessageKey(player, "prop_rush.feedback.signal_flare_cooldown",
                        net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("time", String.valueOf(remaining)));
                return;
            }
            _fireworkCooldown.put(player, now);
            UtilFirework.playFirework(player.getLocation().add(0, 1, 0),
                FireworkEffect.builder().withColor(Color.LIME).withColor(Color.YELLOW).with(Type.STAR).withFlicker().build());
            int pts = _points.getOrDefault(player, 0) + 2;
            _points.put(player, pts);
            sendPropRushMessageKey(player, "prop_rush.feedback.signal_flare_launched");
        }
        else if (item.getType() == Material.ENDER_EYE)
        {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            int uses = _sixthSenseUses.getOrDefault(uuid, 0);
            if (uses >= 1)
            {
                player.sendMessage(C.cRed + "✗ You have already used Sixth Sense this game!");
                return;
            }
            _sixthSenseUses.put(uuid, uses + 1);

            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.5f, 2.0f);
            player.sendMessage(C.cAqua + "✨ Sixth Sense activated! You can see Hunter outlines for 10 seconds!");
            
            for (Player hunter : _seekers.GetPlayers(true))
            {
                hunter.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.GLOWING, 200, 0, false, false));
            }

            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
            player.updateInventory();
            markHiderSkillUse(player);
        }
        else if (item.getType() == Material.FEATHER)
        {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            long nextDash = _dashCooldown.getOrDefault(uuid, 0L);
            if (now < nextDash)
            {
                long remaining = (nextDash - now) / 1000 + 1;
                player.sendMessage(C.cRed + "✗ Dash is on cooldown for " + remaining + " seconds!");
                return;
            }
            
            int uses = _dashUses.getOrDefault(uuid, 0);
            if (uses >= 2)
            {
                player.sendMessage(C.cRed + "✗ No Dash charges remaining!");
                return;
            }
            
            _dashUses.put(uuid, uses + 1);
            _dashCooldown.put(uuid, now + 15000L);
            
            Vector dir = player.getLocation().getDirection().normalize();
            Location target = player.getLocation().add(dir.clone().multiply(3.0));
            
            var traceResult = player.getWorld().rayTraceBlocks(player.getEyeLocation(), dir, 3.0);
            if (traceResult != null && traceResult.getHitBlock() != null)
            {
                target = traceResult.getHitPosition().toLocation(player.getWorld());
                target.subtract(dir.clone().multiply(0.3));
            }
            
            target.setYaw(player.getLocation().getYaw());
            target.setPitch(player.getLocation().getPitch());
            
            player.teleport(target);
            player.getWorld().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            
            int remainingUses = 2 - (uses + 1);
            ItemStack newItem = item.clone();
            ItemMeta meta = newItem.getItemMeta();
            meta.setDisplayName(C.cYellow + C.Bold + "Dash Ability" + C.cWhite + " (" + remainingUses + "/2 Uses)");
            newItem.setItemMeta(meta);
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), newItem);
            player.updateInventory();
            
            markHiderSkillUse(player);
        }
    }

    @EventHandler
    public void OnGameEnd(com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent event)
    {
        if (event.GetState() != com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState.End &&
            event.GetState() != com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState.Dead)
        {
            return;
        }

        // Clean up remaining disguised Hiders who survived until the end
        for (Form form : _forms.values())
        {
            form.Remove(); // Remove calls undisguise and the critical showPlayer fix loop
        }
        _forms.clear();
        _hiderPaths.clear();
        _camperLocation.clear();
        _camperTime.clear();

        // 🧹 Premium Rework Cleanup
        _compassUses.clear();
        _compassCooldown.clear();
        _dashUses.clear();
        _dashCooldown.clear();
        _sixthSenseUses.clear();
        _footprints.clear();
        _perfectDisguiseStacks.clear();
        _disconnectedPlayers.clear();
        _disconnectedStatues.values().forEach(org.bukkit.entity.ArmorStand::remove);
        _disconnectedStatues.clear();
        _disconnectedPoints.clear();
        _spectatorBets.clear();
        _betPlaced.clear();
        _solidifiedGraded.clear();
        _rejoinInvulUntil.clear();
        _hiderCaughtTime.clear();
        _hunterKills.clear();
        _closeCalls.clear();
        _hiderMaxGrade.clear();
        _initialHiderCount = 0;
        _mysteryBoxes.keySet().forEach(org.bukkit.entity.Item::remove);
        _mysteryBoxes.clear();

        // Universal failsafe to wipe any lingering LibsDisguises Tablist wipes from Seekers/Dead players
        for (Player player : UtilServer.getPlayers())
        {
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.GLOWING);
            Manager.GetDisguise().undisguise(player);
            for (Player other : UtilServer.getPlayers())
            {
                if (!other.equals(player)) {
                    other.showPlayer(Manager.getPlugin(), player);
                }
            }
        }
    }

    private java.util.HashSet<UUID> _solidifiedGraded = new java.util.HashSet<>();

    public String getDisguiseGrade(Player hider)
    {
        Form form = _forms.get(hider);
        if (form instanceof BlockForm bForm)
        {
            Material blockType = bForm.GetMaterial();
            if (blockType == null) return "C";
            org.bukkit.block.Biome biome = hider.getLocation().getBlock().getBiome();
            
            int radius = 4;
            int matchCount = 0;
            int totalCount = 0;
            Location loc = hider.getLocation();
            int startX = loc.getBlockX() - radius;
            int endX = loc.getBlockX() + radius;
            int startY = Math.max(loc.getWorld().getMinHeight(), loc.getBlockY() - radius);
            int endY = Math.min(loc.getWorld().getMaxHeight(), loc.getBlockY() + radius);
            int startZ = loc.getBlockZ() - radius;
            int endZ = loc.getBlockZ() + radius;
            
            for (int x = startX; x <= endX; x++)
            {
                for (int y = startY; y <= endY; y++)
                {
                    for (int z = startZ; z <= endZ; z++)
                    {
                        totalCount++;
                        if (loc.getWorld().getBlockAt(x, y, z).getType() == blockType)
                        {
                            matchCount++;
                        }
                    }
                }
            }
            
            boolean appropriate = isBiomeAppropriate(biome, blockType);
            if (matchCount >= 20 && appropriate) return "S";
            if (matchCount >= 10) return "A";
            if (appropriate) return "B";
            if (isImpossibleBlock(blockType)) return "D";
            return "C";
        }
        return "B";
    }

    private boolean isBiomeAppropriate(org.bukkit.block.Biome biome, Material mat)
    {
        String bName = biome.name();
        String mName = mat.name();
        if (bName.contains("FOREST") || bName.contains("WOODS") || bName.contains("JUNGLE") || bName.contains("TAIGA")) {
            if (mName.contains("LOG") || mName.contains("LEAVES") || mName.contains("WOOD") || mName.contains("MUSHROOM") || mName.contains("MOSS") || mName.contains("DIRT") || mName.contains("GRASS")) {
                return true;
            }
        }
        if (bName.contains("DESERT") || bName.contains("BADLANDS")) {
            if (mName.contains("SAND") || mName.contains("TERRACOTTA") || mName.contains("CACTUS") || mName.contains("DEAD_BUSH")) {
                return true;
            }
        }
        if (bName.contains("OCEAN") || bName.contains("RIVER") || bName.contains("BEACH") || bName.contains("SWAMP")) {
            if (mName.contains("WATER") || mName.contains("SAND") || mName.contains("CLAY") || mName.contains("SEAGRASS") || mName.contains("KELP") || mName.contains("MUD")) {
                return true;
            }
        }
        if (bName.contains("MOUNTAIN") || bName.contains("PEAKS") || bName.contains("STONE")) {
            if (mName.contains("STONE") || mName.contains("COBBLESTONE") || mName.contains("ANDESITE") || mName.contains("DIORITE") || mName.contains("GRANITE") || mName.contains("GRAVEL") || mName.contains("ORE")) {
                return true;
            }
        }
        return mName.contains("CRAFTING_TABLE") || mName.contains("FURNACE") || mName.contains("CHEST") || mName.contains("BOOKSHELF");
    }

    private boolean isImpossibleBlock(Material mat)
    {
        String mName = mat.name();
        return mName.contains("TNT") || mName.contains("BEACON") || mName.contains("SPONGE") || mName.contains("DRAGON_EGG") || mName.contains("SPAWNER") || mName.contains("OBSIDIAN") || mName.contains("BEDROCK");
    }

    @EventHandler
    public void HandleDecoyBreak(org.bukkit.event.block.BlockDamageEvent event)
    {
        if (_bombBugBlocks.containsKey(event.getBlock()))
        {
            event.setCancelled(true);
            _bombBugBlocks.remove(event.getBlock());
            Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
            event.getBlock().setType(Material.AIR);
            if (_seekers.HasPlayer(event.getPlayer()))
            {
                detonateBombTrap(event.getPlayer(), loc);
            }
            return;
        }

        if (_decoys.containsKey(event.getBlock()))
        {
            event.setCancelled(true);
            BreakDecoyBlock(event.getBlock());
            if (_seekers.HasPlayer(event.getPlayer())) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1f, 1f);
                sendPropRushMessageKey(event.getPlayer(), "prop_rush.feedback.decoy_destroyed");
            }
        }
    }

    @EventHandler
    public void HandleDecoyInteract(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() != null && _bombBugBlocks.containsKey(event.getClickedBlock()))
        {
            Location loc = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);
            _bombBugBlocks.remove(event.getClickedBlock());
            event.getClickedBlock().setType(Material.AIR);
            if (_seekers.HasPlayer(event.getPlayer()))
            {
                detonateBombTrap(event.getPlayer(), loc);
            }
            return;
        }

        if (event.getClickedBlock() != null && _decoys.containsKey(event.getClickedBlock()))
        {
            BreakDecoyBlock(event.getClickedBlock());
            if (_seekers.HasPlayer(event.getPlayer())) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1f, 1f);
                sendPropRushMessageKey(event.getPlayer(), "prop_rush.feedback.decoy_destroyed");
            }
        }
    }

    public void BreakDecoyBlock(Block b)
    {
        _decoys.remove(b);
        b.setType(Material.AIR);
        UtilParticle.PlayParticle(ParticleType.CLOUD, b.getLocation().add(0.5, 0.5, 0.5), 0.5f, 0.5f, 0.5f, 0, 10, ViewDist.NORMAL, UtilServer.getPlayers());
    }

    @EventHandler
    public void HandleDecoyMobHit(org.bukkit.event.entity.EntityDamageByEntityEvent event)
    {
        if (event.getEntity() instanceof org.bukkit.entity.LivingEntity && _bombBugMobs.containsKey(event.getEntity()))
        {
            event.setCancelled(true);
            org.bukkit.entity.LivingEntity mob = (org.bukkit.entity.LivingEntity) event.getEntity();
            _bombBugMobs.remove(mob);
            Location loc = mob.getLocation().clone();
            mob.remove();
            if (event.getDamager() instanceof Player && _seekers.HasPlayer((Player) event.getDamager()))
            {
                detonateBombTrap((Player) event.getDamager(), loc);
            }
            return;
        }

        if (event.getEntity() instanceof org.bukkit.entity.LivingEntity && _decoyMobs.containsKey(event.getEntity()))
        {
            event.setCancelled(true);
            org.bukkit.entity.LivingEntity mob = (org.bukkit.entity.LivingEntity)event.getEntity();
            _decoyMobs.remove(mob);
            mob.remove();
            UtilParticle.PlayParticle(ParticleType.CLOUD, mob.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0, 10, ViewDist.NORMAL, UtilServer.getPlayers());
            if (event.getDamager() instanceof Player && _seekers.HasPlayer((Player)event.getDamager())) {
                ((Player)event.getDamager()).playSound(event.getDamager().getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1f, 1f);
                sendPropRushMessageKey((Player)event.getDamager(), "prop_rush.feedback.decoy_destroyed");
            }
        }
    }

    @EventHandler
    public void CleanupDecoys(UpdateEvent event)
    {
        if (event.getType() != UpdateType.SEC) return;
        long now = System.currentTimeMillis();
        
        java.util.Iterator<org.bukkit.block.Block> it1 = _decoys.keySet().iterator();
        while (it1.hasNext())
        {
            org.bukkit.block.Block b = it1.next();
            if (now > _decoys.get(b))
            {
                b.setType(Material.AIR);
                UtilParticle.PlayParticle(ParticleType.CLOUD, b.getLocation().add(0.5, 0.5, 0.5), 0.5f, 0.5f, 0.5f, 0, 10, ViewDist.NORMAL, UtilServer.getPlayers());
                it1.remove();
            }
        }

        java.util.Iterator<org.bukkit.entity.LivingEntity> it2 = _decoyMobs.keySet().iterator();
        while (it2.hasNext())
        {
            org.bukkit.entity.LivingEntity mob = it2.next();
            if (now > _decoyMobs.get(mob))
            {
                mob.remove();
                UtilParticle.PlayParticle(ParticleType.CLOUD, mob.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0, 10, ViewDist.NORMAL, UtilServer.getPlayers());
                it2.remove();
            }
        }

        Iterator<Block> bombBlockIt = _bombBugBlocks.keySet().iterator();
        while (bombBlockIt.hasNext())
        {
            Block bombBlock = bombBlockIt.next();
            if (now > _bombBugBlocks.get(bombBlock))
            {
                bombBlock.setType(Material.AIR);
                UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, bombBlock.getLocation().add(0.5, 0.5, 0.5), 0.3f, 0.3f, 0.3f, 0f, 8, ViewDist.NORMAL, UtilServer.getPlayers());
                bombBlockIt.remove();
            }
        }

        Iterator<org.bukkit.entity.LivingEntity> bombMobIt = _bombBugMobs.keySet().iterator();
        while (bombMobIt.hasNext())
        {
            org.bukkit.entity.LivingEntity bombMob = bombMobIt.next();
            if (now > _bombBugMobs.get(bombMob))
            {
                if (bombMob != null && bombMob.isValid())
                {
                    UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, bombMob.getLocation().add(0, 1, 0), 0.3f, 0.3f, 0.3f, 0f, 8, ViewDist.NORMAL, UtilServer.getPlayers());
                    bombMob.remove();
                }
                bombMobIt.remove();
            }
        }

        Iterator<WardenSentry> sentryIt = _wardenSentries.iterator();
        while (sentryIt.hasNext())
        {
            WardenSentry sentry = sentryIt.next();
            if (sentry.owner == null || !sentry.owner.isOnline() || now > sentry.expireAt)
            {
                UtilParticle.PlayParticle(ParticleType.CLOUD, sentry.location.clone().add(0, 0.4, 0), 0.3f, 0.2f, 0.3f, 0f, 8, ViewDist.NORMAL, UtilServer.getPlayers());
                sentryIt.remove();
                continue;
            }

            UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, sentry.location.clone().add(0, 0.3, 0), 0.15f, 0.05f, 0.15f, 0f, 2, ViewDist.NORMAL, UtilServer.getPlayers());

            if (now < sentry.nextPulseAt)
                continue;

            if (now < _terminalDisruptionUntil)
            {
                sentry.nextPulseAt = now + WARDEN_SENTRY_PULSE_MS;
                continue;
            }

            sentry.location.getWorld().playSound(sentry.location, Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.8f, 1.2f);
            UtilParticle.PlayParticle(ParticleType.END_ROD, sentry.location.clone().add(0, 0.4, 0), 0.3f, 0.2f, 0.3f, 0.02f, 10, ViewDist.NORMAL, UtilServer.getPlayers());

            int detected = 0;
            for (Player hider : _hiders.GetPlayers(true))
            {
                if (UtilMath.offset(sentry.location, hider.getLocation()) <= getWardenSentryRadius())
                {
                    revealHider(hider, getRevealDurationTicks(50), sentry.owner);
                    UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, hider.getLocation().add(0, 1, 0), 0.2f, 0.4f, 0.2f, 0f, 6, ViewDist.NORMAL, UtilServer.getPlayers());
                    detected++;
                }
            }

            if (detected > 0)
            {
                sendPropRushNoticeKey(sentry.owner, "prop_rush.notice.echo_sentry_detected",
                        net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("count", String.valueOf(detected)));
            }

            sentry.nextPulseAt = now + WARDEN_SENTRY_PULSE_MS;
        }

        Iterator<java.util.Map.Entry<Player, Long>> traceIt = _recentHiderSkillUse.entrySet().iterator();
        while (traceIt.hasNext())
        {
            java.util.Map.Entry<Player, Long> entry = traceIt.next();
            if (now - entry.getValue() > EXORCIST_TRACE_WINDOW_MS)
            {
                traceIt.remove();
            }
        }
    }

    @EventHandler
    public void SeekerAbilities(PlayerInteractEvent event)
    {
        if (GetState() != GameState.Live || _phase == Phase.PREP)
            return;

        Player player = event.getPlayer();

        if (!_seekers.HasPlayer(player))
            return;

        if (!UtilEvent.isAction(event, ActionType.R))
            return;

        ItemStack item = player.getItemInHand();
        if (item == null) return;

        // Scanner Pulse (Compass) — detection only, shows directional beacon toward closest hider
        if (item.getType() == Material.COMPASS)
        {
            Kit kit = GetKit(player);
            if (kit != null && kit.GetName().equalsIgnoreCase("Tracker"))
            {
                if (!useVanillaItemCooldown(player, "Scanner Pulse", getAbilityCooldown(player, 12000, true)))
                    return;

                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
                
                // Pulse ring
                for (int i = 0; i < 360; i += 15) {
                    double x = Math.cos(Math.toRadians(i)) * 15;
                    double z = Math.sin(Math.toRadians(i)) * 15;
                    UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, player.getLocation().add(x, 1, z), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
                }

                Player closestHider = null;
                double closestDist = Double.MAX_VALUE;
                for (Player hider : _hiders.GetPlayers(true))
                {
                    double dist = UtilMath.offset(player, hider);
                    if (dist <= 15.0 && dist < closestDist)
                    {
                        closestDist = dist;
                        closestHider = hider;
                    }
                }

                if (closestHider != null)
                {
                    // Directional particle trail from hunter toward closest hider
                    final Player targetHider = closestHider;
                    org.bukkit.util.Vector direction = targetHider.getLocation().toVector()
                            .subtract(player.getLocation().toVector()).normalize();
                    for (int step = 1; step <= (int) Math.min(closestDist, 15); step++)
                    {
                        Location trailLoc = player.getLocation().clone().add(direction.clone().multiply(step));
                        trailLoc.add(0, 0.8, 0);
                        UtilParticle.PlayParticle(ParticleType.CRIT, trailLoc, 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL, player);
                    }
                    _pendingScannerPulseFound.put(player, true);
                    _pendingScannerPulseFeedback.put(player, System.currentTimeMillis() + 500L);
                    sendPropRushMessageKey(player, "prop_rush.feedback.scanner_pulse_found");
                }
                else
                {
                    _pendingScannerPulseFound.put(player, false);
                    _pendingScannerPulseFeedback.put(player, System.currentTimeMillis() + 500L);
                    sendPropRushMessageKey(player, "prop_rush.feedback.scanner_pulse_none");
                }
            }
            else
            {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                long nextUse = _compassCooldown.getOrDefault(uuid, 0L);
                if (now < nextUse)
                {
                    long remaining = (nextUse - now) / 1000 + 1;
                    player.sendMessage(C.cRed + "✗ Tracker Compass is on cooldown for " + remaining + " seconds!");
                    return;
                }
                
                int uses = _compassUses.getOrDefault(uuid, 0);
                if (uses >= 3)
                {
                    player.sendMessage(C.cRed + "✗ No Tracker Compass charges remaining!");
                    return;
                }
                
                Player closestHider = null;
                double closestDist = Double.MAX_VALUE;
                for (Player hider : _hiders.GetPlayers(true))
                {
                    double dist = UtilMath.offset(player, hider);
                    if (dist < closestDist)
                    {
                        closestDist = dist;
                        closestHider = hider;
                    }
                }
                
                if (closestHider != null)
                {
                    _compassUses.put(uuid, uses + 1);
                    _compassCooldown.put(uuid, now + 30000L);
                    
                    player.setCompassTarget(closestHider.getLocation());
                    player.playSound(player.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 1f, 1.2f);
                    
                    org.bukkit.util.Vector direction = closestHider.getLocation().toVector()
                            .subtract(player.getLocation().toVector()).normalize();
                    for (int step = 1; step <= (int) Math.min(closestDist, 20); step++)
                    {
                        Location trailLoc = player.getLocation().clone().add(direction.clone().multiply(step));
                        trailLoc.add(0, 0.8, 0);
                        UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, trailLoc, 0f, 0f, 0f, 0f, 1, ViewDist.SHORT, player);
                    }
                    
                    int remainingUses = 3 - (uses + 1);
                    player.sendMessage(C.cGreen + "🧭 Tracker Compass pointed to the nearest Hider! (" + remainingUses + "/3 uses remaining)");
                    
                    ItemStack newItem = item.clone();
                    ItemMeta meta = newItem.getItemMeta();
                    meta.setDisplayName(C.cYellow + C.Bold + "Tracker Compass" + C.cWhite + " (" + remainingUses + "/3 Uses)");
                    newItem.setItemMeta(meta);
                    player.getInventory().setItem(player.getInventory().getHeldItemSlot(), newItem);
                    player.updateInventory();
                }
                else
                {
                    player.sendMessage(C.cRed + "✗ No active Hiders found on the radar!");
                }
            }
        }
        
        // Flare (Fire Charge)
        else if (item.getType() == Material.FIRE_CHARGE)
        {
            event.setCancelled(true);
            if (!useVanillaItemCooldown(player, "Flare", getAbilityCooldown(player, 20000, true)))
                return;

            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 1f);
            
            org.bukkit.entity.Snowball flare = player.launchProjectile(org.bukkit.entity.Snowball.class);
            flare.setCustomName("FlareProjectile");
            flare.setFireTicks(100);
        }
        
        // Bloodhound Sense (Bone)
        else if (item.getType() == Material.BONE)
        {
            if (!useVanillaItemCooldown(player, "Bloodhound Sense", getAbilityCooldown(player, 15000, true)))
                return;

            player.playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1f, 1f);
            
            boolean found = false;
            for (Player hider : _hiders.GetPlayers(true))
            {
                if (UtilMath.offset(player, hider) <= 10.0)
                {
                    revealHider(hider, getRevealDurationTicks(100), player);
                    hider.playSound(hider.getLocation(), Sound.ENTITY_WOLF_GROWL, 1f, 1f);
                    sendPropRushMessageKey(hider, "prop_rush.feedback.bloodhound_targeted");
                    found = true;
                    
                    // Radar ping effect
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.5f, 2.0f);
                    UtilParticle.playParticleRing(ParticleType.SCULK_SOUL, player.getLocation().add(0, 1, 0), 2.5, 30, ViewDist.NORMAL);

                    // Directional trail towards target
                    UtilParticle.playParticleLine(ParticleType.RED_DUST, player.getLocation().add(0, 1, 0), hider.getLocation().add(0, 1, 0), 20, ViewDist.NORMAL);
                    
                    // Show scent trail footprints
                    java.util.LinkedList<Location> path = _hiderPaths.get(hider);
                    if (path != null) {
                        for (Location loc : path) {
                            UtilParticle.PlayParticle(ParticleType.RED_DUST, loc.clone().add(0, 0.2, 0), 0.15f, 0.15f, 0.15f, 0f, 3, ViewDist.NORMAL, player);
                        }
                    }

                    // Bloodhound only detects one at a time to not be too OP
                    break;
                }
            }
            
            if (found) {
                sendPropRushMessageKey(player, "prop_rush.feedback.bloodhound_found");
            } else {
                sendPropRushMessageKey(player, "prop_rush.feedback.bloodhound_none");
            }
        }


        // Bounty Dash (Crossbow)
        else if (item.getType() == Material.CROSSBOW)
        {
            event.setCancelled(true);
            if (!useVanillaItemCooldown(player, "Bounty Dash", getAbilityCooldown(player, 12000, true)))
                return;

            UtilParticle.PlayParticle(ParticleType.CLOUD, player.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 0.5f);
            
            org.bukkit.util.Vector dash = player.getLocation().getDirection().normalize().multiply(2.5);
            dash.setY(0.2); // slight hop
            player.setVelocity(dash);
            
            _pendingBountyDashCheck.put(player, System.currentTimeMillis() + 250L);
        }

    }

    @EventHandler
    public void FlareHit(org.bukkit.event.entity.ProjectileHitEvent event)
    {
        if (event.getEntity() instanceof org.bukkit.entity.Snowball)
        {
            String customName = event.getEntity().getCustomName();
            if ("FlareProjectile".equals(customName))
            {
                Location loc = event.getEntity().getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2f, 1f);
                UtilParticle.PlayParticle(ParticleType.FLAME, loc, 3f, 3f, 3f, 0.1f, 100, ViewDist.MAX, UtilServer.getPlayers());

                for (Player hider : _hiders.GetPlayers(true))
                {
                    if (UtilMath.offset(loc, hider.getLocation()) <= 8.0)
                    {
                        revealHider(hider, getRevealDurationTicks(100));
                        sendPropRushMessageKey(hider, "prop_rush.feedback.flare_revealed");
                        hider.playSound(hider.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1f, 1f);
                    }
                }
            }
            else if ("SmokeBombProjectile".equals(customName))
            {
                Location loc = event.getEntity().getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1.5f);
                UtilParticle.PlayParticle(ParticleType.CAMPFIRE_COSY_SMOKE, loc, 4f, 4f, 4f, 0.05f, 200, ViewDist.MAX, UtilServer.getPlayers());

                if (event.getEntity().getShooter() instanceof Player && _seekers.HasPlayer((Player)event.getEntity().getShooter())) {
                    Kit kit = GetKit((Player)event.getEntity().getShooter());
                    if (kit != null && kit.GetName().equals("Saboteur")) {
                        for (Player hider : _hiders.GetPlayers(true))
                        {
                            if (UtilMath.offset(loc, hider.getLocation()) <= 6.0)
                            {
                                hider.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0));
                                hider.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 60, 0));
                                sendPropRushMessageKey(hider, "prop_rush.feedback.smoke_bomb_blinded");
                                hider.playSound(hider.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void NerveSystem(UpdateEvent event)
    {
        if (GetState() != GameState.Live || _phase == Phase.PREP)
            return;

        if (event.getType() != UpdateType.FAST)
            return;

        long now = System.currentTimeMillis();

        // 1. Proximity Heartbeat Check (for both Hunter and closest Hider)
        for (Player hunter : _seekers.GetPlayers(true))
        {
            Player closestHider = null;
            double closestDistSq = Double.MAX_VALUE;
            for (Player hider : _hiders.GetPlayers(true))
            {
                if (hider.getWorld() == hunter.getWorld())
                {
                    double distSq = hider.getLocation().distanceSquared(hunter.getLocation());
                    if (distSq < closestDistSq)
                    {
                        closestDistSq = distSq;
                        closestHider = hider;
                    }
                }
            }
            
            if (closestHider != null && closestDistSq < 100.0) // < 10 blocks
            {
                double dist = Math.sqrt(closestDistSq);
                boolean shouldPlay = false;
                float pitch = 0.7f;
                
                if (dist < 4.0)
                {
                    shouldPlay = true;
                    pitch = 1.4f;
                }
                else if (dist < 7.0)
                {
                    shouldPlay = (_nerveTicks % 2 == 0);
                    pitch = 1.0f;
                }
                else if (dist < 10.0)
                {
                    shouldPlay = (_nerveTicks % 4 == 0);
                    pitch = 0.7f;
                }
                
                if (shouldPlay)
                {
                    hunter.playSound(hunter.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, pitch);
                    closestHider.playSound(closestHider.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 0.8f, pitch);
                }
            }
        }

        // 2. Loop over Hiders for Nerve, Perfect Disguise Stacks, Footprints, and Actionbar
        for (Player hider : _hiders.GetPlayers(true))
        {
            Form form = _forms.get(hider);
            boolean isLocked = false;
            
            if (form instanceof BlockForm && ((BlockForm)form).GetBlock() != null)
            {
                isLocked = true;
            }
            
            if (!_nerve.containsKey(hider))
                _nerve.put(hider, 100f);

            UUID hiderUuid = hider.getUniqueId();

            // Disguise Grading
            if (isLocked)
            {
                if (!_solidifiedGraded.contains(hiderUuid))
                {
                    _solidifiedGraded.add(hiderUuid);
                    String grade = getDisguiseGrade(hider);
                    String oldGrade = _hiderMaxGrade.get(hiderUuid);
                    if (oldGrade == null || isBetterGrade(grade, oldGrade))
                    {
                        _hiderMaxGrade.put(hiderUuid, grade);
                    }
                    int gradePoints = 2;
                    ChatColor gradeColor = ChatColor.GRAY;
                    if (grade.equals("S")) { gradePoints = 25; gradeColor = ChatColor.GOLD; }
                    else if (grade.equals("A")) { gradePoints = 15; gradeColor = ChatColor.AQUA; }
                    else if (grade.equals("B")) { gradePoints = 5; gradeColor = ChatColor.GREEN; }
                    else if (grade.equals("D")) { gradePoints = 0; gradeColor = ChatColor.RED; }
                    
                    _points.put(hider, _points.getOrDefault(hider, 0) + gradePoints);
                    hider.playSound(hider.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                    com.houzicore.shared.common.util.UtilTextMiddle.display(
                        gradeColor + "GRADE " + grade + "!",
                        C.cWhite + "Solidified Match Ratio score: +" + gradePoints + " pts",
                        10, 40, 10, hider
                    );
                }
            }
            else
            {
                _solidifiedGraded.remove(hiderUuid);
            }

            // Perfect Disguise Badge check (5s near hunter = 20 stack of FAST update)
            boolean isNearHunter = false;
            for (Player hunter : _seekers.GetPlayers(true))
            {
                if (hider.getLocation().distanceSquared(hunter.getLocation()) < 25.0) // 5 blocks
                {
                    isNearHunter = true;
                    break;
                }
            }

            if (isNearHunter)
            {
                _closeCalls.put(hiderUuid, _closeCalls.getOrDefault(hiderUuid, 0) + 1);
            }

            if (isNearHunter && isLocked)
            {
                int stacks = _perfectDisguiseStacks.getOrDefault(hiderUuid, 0) + 1;
                if (stacks >= 20)
                {
                    int currentPoints = _points.getOrDefault(hider, 0) + 50;
                    _points.put(hider, currentPoints);
                    hider.playSound(hider.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
                    hider.sendMessage(C.cGold + "🎭 PERFECT DISGUISE +50 points!");
                    _perfectDisguiseStacks.put(hiderUuid, 0); // reset
                }
                else
                {
                    _perfectDisguiseStacks.put(hiderUuid, stacks);
                }
            }
            else
            {
                _perfectDisguiseStacks.put(hiderUuid, 0);
            }

            // Footprint Trail tracking
            if (!isLocked)
            {
                java.util.LinkedList<Location> footprints = _footprints.computeIfAbsent(hiderUuid, k -> new java.util.LinkedList<>());
                Location curLoc = hider.getLocation();
                if (footprints.isEmpty() || footprints.getLast().distanceSquared(curLoc) > 2.25) // 1.5 blocks
                {
                    footprints.add(curLoc.clone());
                    if (footprints.size() > 30)
                    {
                        footprints.removeFirst();
                    }
                }
            }

            // Footprint particles spawn
            java.util.LinkedList<Location> footprints = _footprints.get(hiderUuid);
            if (footprints != null)
            {
                for (Location loc : footprints)
                {
                    UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, loc.clone().add(0, 0.15, 0), 0.1f, 0.0f, 0.1f, 0.0f, 1, ViewDist.SHORT, UtilServer.getPlayers());
                }
            }

            // Nerve logic
            if (isLocked)
            {
                boolean nearSeeker = false;
                for (Player seeker : _seekers.GetPlayers(true))
                {
                    if (UtilMath.offset(hider, seeker) < 8.0)
                    {
                        nearSeeker = true;
                        break;
                    }
                }

                float nerve = _nerve.get(hider);
                float drainRate = _adrenalineActive.contains(hider) ? 1.5f : 3f;
                if (_perfectPlacementBonus.contains(hider))
                    drainRate *= 0.9f;
                
                if (nearSeeker)
                {
                    if (nerve > 0f)
                    {
                        nerve = Math.max(0f, nerve - drainRate); 
                    }
                    else
                    {
                        // Gasp!
                        hider.playSound(hider.getLocation(), Sound.ENTITY_WOLF_PANT, 1.5f, 0.5f);
                        UtilParticle.PlayParticle(ParticleType.CLOUD, hider.getLocation().add(0, 0.5, 0), 0.5f, 0.5f, 0.5f, 0.05f, 10, ViewDist.MAX, UtilServer.getPlayers());
                        nerve = _adrenalineActive.contains(hider) ? 55f : 40f; 
                        
                        hider.setVelocity(new Vector(Math.random()*0.2 - 0.1, 0.2, Math.random()*0.2 - 0.1));
                    }
                }
                else
                {
                    if (nerve < 100f)
                    {
                        nerve = Math.min(100f, nerve + 3f); 
                    }
                }
                
                _nerve.put(hider, nerve);
            }
            else 
            {
                _nerve.put(hider, 100f);
            }

            // Hider Actionbar display
            if (_terminalChannel.containsKey(hider))
            {
                continue;
            }

            String survivalStr = C.cAqua + "⌛ " + C.cWhite + "Survived: " + C.cAqua + formatSurvivalTime(System.currentTimeMillis() - _gameStartTime);
            String statusSuffix = "";
            
            if (isLocked)
            {
                float nerve = _nerve.getOrDefault(hider, 100f);
                boolean nearSeeker = false;
                for (Player seeker : _seekers.GetPlayers(true))
                {
                    if (UtilMath.offset(hider, seeker) < 8.0)
                    {
                        nearSeeker = true;
                        break;
                    }
                }
                
                if (nearSeeker || nerve < 100f)
                {
                    int bars = (int)(nerve / 10f);
                    StringBuilder barStr = new StringBuilder();
                    for (int i=0; i<10; i++) {
                        if (i < bars) barStr.append(C.cGreen).append("●");
                        else barStr.append(C.cGray).append("●");
                    }
                    statusSuffix = C.cGray + " • " + C.cRed + "Nerve: " + barStr.toString();
                }
                else
                {
                    statusSuffix = C.cGray + " • " + C.cGreen + "🔒 Solidified";
                }
            }
            else
            {
                if (form instanceof BlockForm)
                {
                    com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData disguise = Manager.GetDisguise().getEngine().getDisguise(hider);
                    if (disguise != null)
                    {
                        float progress = (float) disguise.getStillTicks() / 40.0f;
                        int percent = (int)(Math.min(0.999f, Math.max(0.0f, progress)) * 100);
                        statusSuffix = C.cGray + " • " + C.cYellow + "🧱 Solidifying: " + percent + "%";
                    }
                }
                else
                {
                    statusSuffix = C.cGray + " • " + C.cAqua + "🏃 Hiding";
                }
            }

            com.houzicore.shared.common.util.UtilTextBottom.display(
                com.houzicore.shared.common.actionbar.ActionBarChannel.GAME_STATUS,
                survivalStr + statusSuffix,
                hider
            );
        }

        // 3. Hunter Actionbar display
        for (Player hunter : _seekers.GetPlayers(true))
        {
            long searchMs = System.currentTimeMillis() - _gameStartTime;
            String timeStr = C.cAqua + "⌚ " + C.cWhite + "Search Time: " + C.cAqua + formatSurvivalTime(searchMs);
            int uses = _compassUses.getOrDefault(hunter.getUniqueId(), 0);
            int chargesLeft = 3 - uses;
            String compassSuffix = C.cGray + " • " + C.cAqua + "🧭 Radar: " + C.cWhite + chargesLeft + "/3" + C.cGray + " Chgs";
            
            com.houzicore.shared.common.util.UtilTextBottom.display(
                com.houzicore.shared.common.actionbar.ActionBarChannel.GAME_STATUS,
                timeStr + compassSuffix,
                hunter
            );
        }
    }

    @EventHandler
    public void SeekerMissHit(PlayerInteractEvent event)
    {
        if (event.isCancelled()) return;
        if (GetState() != GameState.Live || _phase == Phase.PREP) return;
        if (!_seekers.HasPlayer(event.getPlayer())) return;
        
        if (event.getAction().name().contains("LEFT") && event.getClickedBlock() != null)
        {
            org.bukkit.block.Block clicked = event.getClickedBlock();
            
            // Block Hunt: Solid hider detection removed from SeekerMissHit.
            // All solid-hit logic is handled by HiderHitBridge (EventPriority.HIGH).
            // This handler now only processes "miss" feedback (wrong block penalty).

            if (!Recharge.Instance.use(event.getPlayer(), "Seeker Hit", 500, false, false)) return;
            
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5), 0.3f, 0.3f, 0.3f, 0, 1, ViewDist.NORMAL, event.getPlayer());
        }
    }
    
    @EventHandler
    public void AfkFlareCheck(UpdateEvent event)
    {
        if (GetState() != GameState.Live || _phase == Phase.PREP) return;
        if (event.getType() != UpdateType.SEC) return;
        
        long now = System.currentTimeMillis();
        
        for (Player hider : _hiders.GetPlayers(true))
        {
            Form form = _forms.get(hider);
            boolean isLocked = (form instanceof com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm && ((com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms.BlockForm)form).GetBlock() != null);
            
            // Allow AFK if solidified
            if (isLocked) {
                _lastHiderMoveTime.put(hider, now);
                continue;
            }
            
            Location currentLoc = hider.getLocation();
            Location lastLoc = _lastHiderBlockLoc.get(hider);
            
            if (lastLoc == null || currentLoc.getBlockX() != lastLoc.getBlockX() || currentLoc.getBlockY() != lastLoc.getBlockY() || currentLoc.getBlockZ() != lastLoc.getBlockZ()) {
                // Moved block position
                _lastHiderBlockLoc.put(hider, currentLoc);
                _lastHiderMoveTime.put(hider, now);
            } else {
                // Hasn't moved block position
                long moveTime = _lastHiderMoveTime.getOrDefault(hider, now);
                if (now - moveTime >= AFK_THRESHOLD) {
                    long nextFlareTime = _afkFlareRecharge.getOrDefault(hider, 0L);
                    if (now >= nextFlareTime) {
                        // Fire AFK Flare
                        UtilFirework.playFirework(hider.getLocation().add(0, 1, 0), FireworkEffect.builder().withColor(Color.RED).withColor(Color.ORANGE).with(Type.BALL_LARGE).withFlicker().build());
                        UtilPlayer.message(hider, C.cRed + C.Bold + (com.houzicore.shared.core.lang.LangManager.get().isThai(hider) ? "⚠ คุณอยู่นิ่งนานเกินไป! ศัตรูเห็นตำแหน่งคุณแล้ว!" : "⚠ You stood still too long! Hunters see your location!"));
                        hider.playSound(hider.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                        
                        _afkFlareRecharge.put(hider, now + AFK_FLARE_COOLDOWN);
                    }
                }
            }
        }
    }

    // [WOW] Task 48: Elo/MMR Based Matchmaking Injection
    @org.bukkit.event.EventHandler
    public void BalanceTeamsOnPrepare(com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent event)
    {
        if (event.GetState() != GameState.Prepare)
            return;
            
        java.util.List<Player> allPlayers = GetPlayers(true);
        if (allPlayers.size() < 2) return;
        
        // Remove everyone from current sequential sorting
        for (Player p : allPlayers) {
            if (_hiders != null && _hiders.HasPlayer(p)) _hiders.RemovePlayer(p);
            if (_seekers != null && _seekers.HasPlayer(p)) _seekers.RemovePlayer(p);
        }
        
        // Sort by MMR (Using Wins as standard Elo baseline for Arcade)
        allPlayers.sort((p1, p2) -> {
            long mmr1 = Manager.GetStatsManager().Get(p1).getStat(GetName() + ".Wins");
            long mmr2 = Manager.GetStatsManager().Get(p2).getStat(GetName() + ".Wins");
            return Long.compare(mmr2, mmr1); // Descending, highest Elo first
        });
        
        int numSeekers = Math.max(1, allPlayers.size() / 5);
        
        for (int i = 0; i < allPlayers.size(); i++) {
            Player p = allPlayers.get(i);
            if (i < numSeekers) {
                SetPlayerTeam(p, _seekers, true);
            } else {
                SetPlayerTeam(p, _hiders, true);
            }
        }
        
        Announce(com.houzicore.shared.common.util.C.cGreen + com.houzicore.shared.common.util.C.Bold + "Teams have been balanced based on Player MMR!");
    }

    @Override
    public GameTeam ChooseTeam(Player player)
    {
        // Sequential fill for Lobby visual sorting before MMR Hook kicks in
        if (_seekers != null && CanJoinTeam(_seekers))
            return _seekers;

        if (_hiders != null)
            return _hiders;

        if (GetTeamList().size() >= 2)
        {
            _hiders = GetTeamList().get(0);
            _seekers = GetTeamList().get(1);
            if (CanJoinTeam(_seekers)) return _seekers;
            return _hiders;
        }

        return null;
    }

    @Override
    public boolean CanJoinTeam(GameTeam team)
    {
        if (team == null) return false;

        if (team.GetColor() == ChatColor.RED)
        {
            return team.GetSize() < Math.max(1, GetPlayers(true).size() / 5);
        }

        return true;
    }

    @Override
    public boolean CanThrowTNT(Location location)
    {
        for (Location loc : _seekers.GetSpawns())
            if (UtilMath.offset(loc, location) < 24)
                return false;

        return true;
    }

    // @Override // method removed
//    public DeathMessageType GetDeathMessageType()
//    {
//        return DeathMessageType.Detailed;
//    }

    @EventHandler
    public void UsableCancel(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null)
            return;

        if (UtilBlock.usable(event.getClickedBlock()))
            event.setCancelled(true);
    }

    public GameTeam getHiders()
    {
        return _hiders;
    }

    public GameTeam getSeekers()
    {
        return _seekers;
    }

    @EventHandler
    public void onGameDeadCleanup(GameStateChangeEvent event)
    {
        if (event.GetState() != GameState.Dead)
            return;

        // 1. เคลียร์ Forms (ทำลาย BlockDisplays / Disguise Entities)
        for (Form form : _forms.values()) {
            form.Remove();
        }
        _forms.clear();

        // 2. เคลียร์สัตว์ประดับด่าน (Map Entities)
        for (Creature creature : _mobs.keySet()) {
            if (creature != null && creature.isValid()) {
                creature.remove();
            }
        }
        _mobs.clear();

        // 3. เคลียร์ร่างแยกของสกิล (Decoys / ArmorStands)
        for (org.bukkit.entity.LivingEntity decoy : _decoyMobs.keySet()) {
            if (decoy != null && decoy.isValid()) {
                decoy.remove();
            }
        }
        _decoyMobs.clear();
        _decoys.clear();

        // 4. เคลียร์ตัวแปรความจำอื่นๆ เพื่อช่วย GC
        _survivalPoints.clear();
        _nerve.clear();
        _points.clear();
        _lastHiderBlockLoc.clear();
        _lastHiderMoveTime.clear();
        _afkFlareRecharge.clear();
        _tauntCooldown.clear();
        _tauntPending.clear();
        _fireworkCooldown.clear();
        _lastSurvivalPoints.clear();
        _adrenalineActive.clear();
        _prepAnchors.clear();
        _perfectPlacementBonus.clear();
        _panicJumpHits.clear();
        _panicJumpWindow.clear();
        _recentHiderSkillUse.clear();
        clearDangerZone();
        _nextDangerZoneAt = 0;
        _nextChaosPulseAt = 0;
        _terminalLocations.clear();
        _terminalChannel.clear();
        _terminalChannelUntil.clear();
        _usedTerminals.clear();
        _terminalDisruptionUntil = 0;
        _terminalBlindUntil = 0;
        _roundModifier = RoundModifier.NONE;
        _mapOpen = false;
        _mapTight = false;
        _pendingInitialDisguise.clear();
        _pendingHiderItems.clear();
        _pendingLastStandSound.clear();
        _pendingScannerPulseFeedback.clear();
        _pendingScannerPulseFound.clear();
        _pendingBountyDashCheck.clear();
        _pendingHunterRespawnKit.clear();
        _pendingTauntReward.clear();
        for (Block bombBlock : _bombBugBlocks.keySet()) {
            bombBlock.setType(Material.AIR);
        }
        for (org.bukkit.entity.LivingEntity bombMob : _bombBugMobs.keySet()) {
            if (bombMob != null && bombMob.isValid()) {
                bombMob.remove();
            }
        }
        _bombBugBlocks.clear();
        _bombBugMobs.clear();
        _wardenSentries.clear();
        for (com.houzicore.shared.core.hologram.Hologram hologram : _temporaryHolograms.keySet()) {
            hologram.stop();
        }
        _temporaryHolograms.clear();

        // 5. ลบ Holograms ลอยฟ้าทั้งหมด
        try {
            Manager.getHologramManager().removeAll();
        } catch (Exception e) {
            // Safe Catch
        }

        if (WorldData != null && WorldData.World != null)
        {
            WorldData.World.setTime(6000);
            WorldData.World.setStorm(false);
        }
    }

    private java.util.HashMap<org.bukkit.entity.Player, com.houzicore.shared.core.hologram.Hologram> _factionHolograms = new java.util.HashMap<>();

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        org.bukkit.entity.Player player = event.getEntity();
        if (IsLive() && GetPlayers(false).contains(player)) {
            // [WOW] Randomized Premium Death Particles (Task [34])
            int r = com.houzicore.shared.common.util.UtilMath.r(3);
            if (r == 0) {
                com.houzicore.shared.common.util.UtilParticle.drawTornadoFrame(player.getLocation(), com.houzicore.shared.common.util.UtilParticle.ParticleType.FIREWORKS_SPARK, 1.5, 4.0, 0);
            } else if (r == 1) {
                com.houzicore.shared.common.util.UtilParticle.drawBlackHoleFrame(player.getLocation().add(0, 1, 0), com.houzicore.shared.common.util.UtilParticle.ParticleType.LARGE_SMOKE, 3.0, 100);
            } else {
                com.houzicore.shared.common.util.UtilParticle.drawDNAFrame(player.getLocation(), com.houzicore.shared.common.util.UtilParticle.ParticleType.HAPPY_VILLAGER, com.houzicore.shared.common.util.UtilParticle.ParticleType.RED_DUST, 4.0, 0);
            }
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.2f);
        }
    }


    @org.bukkit.event.EventHandler
    public void updateFactionHolograms(com.houzicore.shared.updater.event.UpdateEvent event) {
        if (event.getType() != com.houzicore.shared.updater.UpdateType.SEC) return;
        
        if (GetState() != GameState.Prepare) {
            if (!_factionHolograms.isEmpty()) {
                for (com.houzicore.shared.core.hologram.Hologram holo : _factionHolograms.values()) {
                    holo.stop();
                }
                _factionHolograms.clear();
            }
            return;
        }

        for (org.bukkit.entity.Player player : GetPlayers(true)) {
            com.houzicore.arcade.nautilus.game.arcade.game.GameTeam team = GetTeam(player);
            if (team == null) continue;

            com.houzicore.shared.core.hologram.Hologram holo = _factionHolograms.get(player);
            if (holo == null || !holo.isInUse()) {
                String role = team.GetColor() + "[" + com.houzicore.shared.common.util.UtilText.toSmallCaps(team.GetName()) + "]";
                holo = new com.houzicore.shared.core.hologram.Hologram(Manager.getHologramManager(), player.getLocation().add(0, 2.7, 0), role);
                holo.setFollowEntity(player);
                holo.setRemoveOnEntityDeath();
                holo.start();
                _factionHolograms.put(player, holo);
            }
        }

        java.util.Iterator<java.util.Map.Entry<org.bukkit.entity.Player, com.houzicore.shared.core.hologram.Hologram>> it = _factionHolograms.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<org.bukkit.entity.Player, com.houzicore.shared.core.hologram.Hologram> entry = it.next();
            if (!entry.getKey().isOnline() || !IsAlive(entry.getKey())) {
                entry.getValue().stop();
                it.remove();
            }
        }
    }

    private java.util.List<org.bukkit.block.Block> _trapperWebs = new java.util.ArrayList<>();
    private com.houzicore.shared.common.util.NautHashMap<org.bukkit.entity.Player, org.bukkit.Location> _camperLocation = new com.houzicore.shared.common.util.NautHashMap<>();
    private com.houzicore.shared.common.util.NautHashMap<org.bukkit.entity.Player, Long> _camperTime = new com.houzicore.shared.common.util.NautHashMap<>();
    private com.houzicore.shared.common.util.NautHashMap<org.bukkit.entity.Player, java.util.LinkedList<org.bukkit.Location>> _hiderPaths = new com.houzicore.shared.common.util.NautHashMap<>();

    @org.bukkit.event.EventHandler
    public void updateAntiCamp(com.houzicore.shared.updater.event.UpdateEvent event) {
        if (event.getType() != com.houzicore.shared.updater.UpdateType.SEC || !IsLive()) return;
        
        long now = System.currentTimeMillis();
        for (org.bukkit.entity.Player p : GetPlayers(true)) {
            com.houzicore.arcade.nautilus.game.arcade.game.GameTeam team = GetTeam(p);
            if (team != null && team.GetName().equalsIgnoreCase("Hiders")) {
                org.bukkit.Location cur = p.getLocation();
                org.bukkit.Location last = _camperLocation.get(p);

                if (last == null || cur.distanceSquared(last) > 2.25) { // MOVED MORE THAN 1.5 BLOCKS
                    _camperLocation.put(p, cur);
                    _camperTime.put(p, now);
                } else {
                    long idleTime = now - (_camperTime.containsKey(p) ? _camperTime.get(p) : now);
                    
                    if (idleTime > 15000 && idleTime <= 20000) {
                        // Send warning once when entering the threshold
                        if (idleTime - 15000 < 1000) {
                            com.houzicore.shared.common.util.UtilTextMiddle.display("", C.cYellow + "⚠ You're getting warm...", 10, 40, 10, p);
                        }
                    } else if (idleTime > 20000 && idleTime <= 25000) {
                        UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, p.getLocation().add(0, 0.5, 0), 0.3f, 0.5f, 0.3f, 0.02f, 5, ViewDist.NORMAL, UtilServer.getPlayers());
                    } else if (idleTime > 25000 && idleTime <= 30000) {
                        UtilParticle.PlayParticle(ParticleType.FLAME, p.getLocation().add(0, 0.5, 0), 0.3f, 0.5f, 0.3f, 0.02f, 8, ViewDist.NORMAL, UtilServer.getPlayers());
                        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.5f);
                    } else if (idleTime > 30000) {
                        revealHider(p, 60);
                        com.houzicore.shared.common.util.UtilTextMiddle.display("", C.cRed + "⚠ You've been revealed for camping!", 10, 40, 10, p);
                        _camperTime.put(p, now); // Reset timer
                    }
                }
            } else {
                _camperLocation.remove(p);
                _camperTime.remove(p);
            }
        }
    }

    // [WOW] Task 33: Advanced Anti-Cheat Hook for Block Glitchers
    @org.bukkit.event.EventHandler
    public void PreventBlockGlitching(com.houzicore.shared.updater.event.UpdateEvent event)
    {
        if (event.getType() != com.houzicore.shared.updater.UpdateType.SEC)
            return;
            
        if (!IsLive())
            return;
            
        for (org.bukkit.entity.Player player : GetPlayers(true))
        {
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || Manager.isSpectator(player))
                continue;
                
            // Solid hiders are exempt
            Form form = _forms.get(player);
            if (form instanceof BlockForm && ((BlockForm)form).GetBlock() != null)
                continue;
                
            // Check if suffocating in occluding block
            org.bukkit.block.Block block1 = player.getLocation().getBlock();
            org.bukkit.block.Block block2 = player.getLocation().add(0, 1, 0).getBlock();
            if ((block1.getType().isSolid() && block1.getType().isOccluding()) || 
                (block2.getType().isSolid() && block2.getType().isOccluding()))
            {
                org.bukkit.Location safeLoc = block1.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0.5, 1, 0.5);
                safeLoc.setYaw(player.getLocation().getYaw());
                safeLoc.setPitch(player.getLocation().getPitch());
                player.teleport(safeLoc);
                com.houzicore.shared.common.util.UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Anti-Cheat", "§cBlock Glitching is strictly prohibited!"));
                com.houzicore.shared.common.util.UtilParticle.PlayParticle(com.houzicore.shared.common.util.UtilParticle.ParticleType.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0f, 10, com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL, com.houzicore.shared.common.util.UtilServer.getPlayers());
            }
        }
    }

    @org.bukkit.event.EventHandler
    public void onGameEndCinematics(com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent event) {
        if (event.GetState() != GameState.End) return;
        
        for (org.bukkit.entity.Player p : GetPlayers(true)) {
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 400, 255, false, false));
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST, 400, -250, false, false));
            
            com.houzicore.shared.common.util.UtilFirework.launchFirework(
                p.getLocation().add(0, 2, 0),
                org.bukkit.FireworkEffect.Type.BALL_LARGE,
                org.bukkit.Color.AQUA,
                true, true,
                new org.bukkit.util.Vector(0, 1, 0),
                1
            );
        }
    }

    // ==== HUNTER RESPAWN FIX ====
    @EventHandler(priority = EventPriority.LOW)
    public void onHunterDeathDrops(PlayerDeathEvent event) {
        if (!IsLive()) return;
        if (_seekers.HasPlayer(event.getEntity().getName(), true)) {
            event.getDrops().clear(); // Prevent duplicate hunter item drops
        }
    }

    @EventHandler
    public void onHunterRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        if (!IsLive()) return;
        final Player player = event.getPlayer();
        if (_seekers.HasPlayer(player.getName(), true)) {
            _pendingHunterRespawnKit.put(player, System.currentTimeMillis() + 250L);
        }
    }

    // ==== PROP RUSH SCRAP OBJECTIVES ====
    public void SpawnScrap() {
        ArrayList<Player> livePlayers = GetPlayers(true);
        if (livePlayers.isEmpty()) return;
        Player anchor = livePlayers.get(UtilMath.r(livePlayers.size()));
        Location spawnLoc = anchor.getLocation().add(UtilMath.rr(20, true), 10, UtilMath.rr(20, true));
        
        // Drop down to highest block
        org.bukkit.block.Block highest = spawnLoc.getWorld().getHighestBlockAt(spawnLoc);
        if (highest == null || highest.getType() == Material.AIR || !highest.getType().isSolid()) {
            return; // invalid spawn
        }
        
        spawnLoc = highest.getLocation().add(0.5, 1, 0.5);
        
        org.bukkit.entity.Item scrap = spawnLoc.getWorld().dropItem(spawnLoc, ItemStackFactory.Instance.CreateStack(Material.GOLD_INGOT, (byte) 0, 1, C.cYellow + C.Bold + "Gold Cache"));
        scrap.setPickupDelay(32767); // Cannot be picked up normally (Short.MAX_VALUE is safer than Integer.MAX_VALUE)
        scrap.setCustomNameVisible(true);
        scrap.setCustomName(C.cYellow + C.Bold + "Gold Cache");
        
        _scrapItems.put(scrap, System.currentTimeMillis());
        
        for(Player player : GetPlayers(true)) {
            sendPropRushNoticeKey(player, "prop_rush.notice.gold_cache_spawned");
        }
    }

    @EventHandler
    public void ScrapUpdate(UpdateEvent event) {
        if (!IsLive() || event.getType() != UpdateType.TICK) return;

        // Spawn roughly every 45 secs while the round is actively pressuring movement
        if ((_phase == Phase.HUNT || _phase == Phase.PANIC || _phase == Phase.CHAOS) && UtilTime.elapsed(_lastScrapSpawn, 45000) && _scrapItems.size() < 5) {
            _lastScrapSpawn = System.currentTimeMillis();
            SpawnScrap();
        }

        Iterator<org.bukkit.entity.Item> it = _scrapItems.keySet().iterator();
        while (it.hasNext()) {
            org.bukkit.entity.Item scrap = it.next();
            if (!scrap.isValid()) {
                it.remove();
                continue;
            }

            // Particles
            UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, scrap.getLocation().add(0, 0.5, 0), 0.3f, 0.3f, 0.3f, 0, 3, ViewDist.NORMAL, UtilServer.getPlayers());

            boolean claimed = false;
            for (Player player : GetPlayers(true)) {
                if (UtilMath.offset(scrap.getLocation(), player.getLocation()) >= 2.0)
                    continue;

                claimed = true;
                sendPropRushNoticeKey(player, "prop_rush.notice.gold_cache_collected");
                restoreHealthFromGold(player, 4.0);

                int pointGain = _hiders.HasPlayer(player.getName(), true) ? 15 : 10;
                _points.put(player, _points.getOrDefault(player, 0) + pointGain);

                if (_seekers.HasPlayer(player.getName(), true))
                {
                    _hunterRelayCooldownUntil = Math.max(System.currentTimeMillis(), _hunterRelayCooldownUntil - 7000L);
                }

                scrap.remove();
                it.remove();

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, scrap.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0, 10, ViewDist.NORMAL, UtilServer.getPlayers());
                break;
            }

            if (claimed) continue;
        }

        // Mystery Boxes claim loop
        Iterator<org.bukkit.entity.Item> boxIt = _mysteryBoxes.keySet().iterator();
        while (boxIt.hasNext()) {
            org.bukkit.entity.Item box = boxIt.next();
            if (!box.isValid()) {
                boxIt.remove();
                continue;
            }
            UtilParticle.PlayParticle(ParticleType.PORTAL, box.getLocation().add(0, 0.5, 0), 0.3f, 0.3f, 0.3f, 0, 5, ViewDist.NORMAL, UtilServer.getPlayers());
            for (Player player : GetPlayers(true)) {
                if (UtilMath.offset(box.getLocation(), player.getLocation()) < 2.0) {
                    box.remove();
                    boxIt.remove();
                    
                    int pointGain = 50;
                    _points.put(player, _points.getOrDefault(player, 0) + pointGain);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                    
                    for (Player p : UtilServer.getPlayers()) {
                        sendPropRushSummary(p, "Mystery Box Claimed", "เก็บกล่องปริศนาสำเร็จ", player.getName() + " claimed the Mystery Box (+50 pts)!", player.getName() + " ได้รับแต้มจากกล่องปริศนา (+50 แต้ม)!");
                    }
                    break;
                }
            }
        }
    }

    // ==== TRICKSTER ABILITIES ====
    @EventHandler(priority = EventPriority.HIGH)
    public void TricksterHit(EntityDamageByEntityEvent event) {
        if (!IsLive() || event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        
        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        if (_hiders.HasPlayer(damager) && _seekers.HasPlayer(victim)) {
            Kit kit = GetKit(damager);
            if (kit != null && kit.GetName().equals("Trickster")) {
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0));
                
                UtilParticle.PlayParticle(ParticleType.SNOWBALL_POOF, victim.getLocation().add(0, 1.5, 0), 0.5f, 0.5f, 0.5f, 0f, 10, ViewDist.NORMAL, UtilServer.getPlayers());
                victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1f, 1f);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void PanicJump(EntityDamageByEntityEvent event)
    {
        if (!IsLive() || event.isCancelled())
            return;

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))
            return;

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        if (!_hiders.HasPlayer(victim) || !_seekers.HasPlayer(damager))
            return;

        long now = System.currentTimeMillis();
        long windowStart = _panicJumpWindow.getOrDefault(victim, 0L);
        int hits = _panicJumpHits.getOrDefault(victim, 0);

        if (now - windowStart > PANIC_JUMP_WINDOW_MS)
        {
            windowStart = now;
            hits = 0;
        }

        hits++;
        _panicJumpWindow.put(victim, windowStart);
        _panicJumpHits.put(victim, hits);

        if (hits < PANIC_JUMP_HIT_THRESHOLD)
            return;

        _panicJumpHits.put(victim, 0);
        _panicJumpWindow.put(victim, now);

        if (!Recharge.Instance.use(victim, "Panic Jump", 8000, false, false))
            return;

        victim.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 40, 0, false, true));
        victim.setVelocity(victim.getVelocity().setY(Math.max(0.32, victim.getVelocity().getY())));
        victim.playSound(victim.getLocation(), Sound.ENTITY_RABBIT_JUMP, 1f, 1.4f);
        UtilParticle.PlayParticle(ParticleType.CLOUD, victim.getLocation().add(0, 0.4, 0), 0.35f, 0.15f, 0.35f, 0.02f, 10, ViewDist.NORMAL, UtilServer.getPlayers());
        sendPropRushNoticeKey(victim, "prop_rush.notice.panic_jump_triggered");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void BrutalHitTrait(EntityDamageByEntityEvent event) {
        if (!IsLive() || event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        
        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        if (_seekers.HasPlayer(damager) && _hiders.HasPlayer(victim)) {
            com.houzicore.arcade.nautilus.game.arcade.kit.traits.Trait trait = Manager.getTraitManager().getEquippedTrait(damager, GetKit(damager));
            if (trait != null && trait.getKey().equals("hideseek_brutalhit")) {
                _brutalHits.putIfAbsent(damager, new HashSet<>());
                if (!_brutalHits.get(damager).contains(victim)) {
                    _brutalHits.get(damager).add(victim);
                    event.setDamage(event.getDamage() + 3.0);
                    UtilParticle.PlayParticle(ParticleType.MAGIC_CRIT, victim.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0.1f, 15, ViewDist.NORMAL, UtilServer.getPlayers());
                    damager.playSound(damager.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
                }
            }
        }
    }
    
    @EventHandler
    public void TricksterSnowTrail(UpdateEvent event) {
        if (!IsLive() || event.getType() != UpdateType.TICK) return;
        
        for (Player p : _hiders.GetPlayers(true)) {
            Kit kit = GetKit(p);
            if (kit != null && kit.GetName().equals("Trickster")) {
                // If moving
                if (p.getVelocity().lengthSquared() > 0.01) {
                    UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, p.getLocation().add(0, 0.2, 0), 0.2f, 0.1f, 0.2f, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
                }
            }
        }
    }
    @EventHandler
    public void HiderFootprints(UpdateEvent event) {
        if (!IsLive() || event.getType() != UpdateType.FASTEST) return;
        
        for (Player p : _hiders.GetPlayers(true)) {
            if (p.isSneaking() || !p.isOnGround()) continue;
            
            if (p.getVelocity().lengthSquared() > 0.02) {
                org.bukkit.block.Block block = p.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
                if (block.getType() != org.bukkit.Material.AIR && block.getType() != org.bukkit.Material.WATER) {
                    p.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, p.getLocation().add(0, 0.1, 0), 3, 0.2, 0.0, 0.2, 0, block.getBlockData());
                }
            }
        }
    }

    @EventHandler
    public void HiderGlowingVisualFix(UpdateEvent event) {
        if (!IsLive() || event.getType() != UpdateType.TICK) return;
        
        for (Player p : _hiders.GetPlayers(true)) {
            if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.GLOWING)) {
                UtilParticle.PlayParticle(ParticleType.GLOW, p.getLocation().add(0, 1, 0), 0.4f, 0.6f, 0.4f, 0f, 3, ViewDist.LONG, UtilServer.getPlayers());
            }
        }
    }

    @EventHandler
    public void HiderPathTracking(UpdateEvent event) {
        if (!IsLive() || event.getType() != UpdateType.FASTER) return;
        
        for (Player p : _hiders.GetPlayers(true)) {
            if (!_hiderPaths.containsKey(p)) {
                _hiderPaths.put(p, new java.util.LinkedList<Location>());
            }
            java.util.LinkedList<Location> path = _hiderPaths.get(p);
            
            // Only add if moved
            if (path.isEmpty() || path.getLast().distanceSquared(p.getLocation()) > 0.25) {
                path.add(p.getLocation().clone());
                
                // Spawn footprint particle for Hunters
                UtilParticle.PlayParticle(ParticleType.RED_DUST, p.getLocation().clone().add(0, 0.1, 0), 0.1f, 0.05f, 0.1f, 0f, 1, ViewDist.NORMAL, _seekers.GetPlayers(true).toArray(new Player[0]));
                
                // Keep last 15 seconds (30 points since FASTER is 500ms)
                if (path.size() > 30) {
                    path.removeFirst();
                }
            }
        }
    }

    @EventHandler
    public void TrapperWebPlace(org.bukkit.event.player.PlayerInteractEvent event) {
        if (!IsLive()) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        if (!_seekers.HasPlayer(player)) return;
        
        org.bukkit.inventory.ItemStack item = player.getItemInHand();
        if (item != null && item.getType() == Material.COBWEB) {
            event.setCancelled(true);
            org.bukkit.block.Block clicked = event.getClickedBlock();
            org.bukkit.block.Block target = clicked.getRelative(event.getBlockFace());
            if (target.getType() == Material.AIR || target.getType() == Material.CAVE_AIR) {
                target.setType(Material.COBWEB);
                _trapperWebs.add(target);
                
                // consume item
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.setItemInHand(null);
                }
                
                player.playSound(target.getLocation(), Sound.ENTITY_SPIDER_STEP, 1f, 1f);
            }
        }
    }

    @EventHandler
    public void onGameStateChange(com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent event) {
        if (event.GetState() == com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState.Dead) {
            for (org.bukkit.block.Block b : _trapperWebs) {
                if (b.getType() == Material.COBWEB) {
                    b.setType(Material.AIR);
                }
            }
            _trapperWebs.clear();
        }
    }

    @EventHandler
    public void PlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        Form form = _forms.remove(player);
        if (form != null) {
            if (IsLive() && _hiders.HasPlayer(player)) {
                _disconnectedPlayers.put(player.getUniqueId(), player.getLocation());
                _disconnectedPoints.put(player.getUniqueId(), _points.getOrDefault(player, 0));
                
                org.bukkit.entity.ArmorStand stand = player.getWorld().spawn(player.getLocation().add(0, -0.5, 0), org.bukkit.entity.ArmorStand.class, s -> {
                    s.setVisible(false);
                    s.setMarker(true);
                    s.setGravity(false);
                    if (form instanceof BlockForm bForm) {
                        s.getEquipment().setHelmet(new ItemStack(bForm.GetMaterial()));
                    } else {
                        s.getEquipment().setHelmet(new ItemStack(Material.OAK_LOG));
                    }
                    s.setCustomName(C.cYellow + player.getName() + " (Offline)");
                    s.setCustomNameVisible(true);
                });
                _disconnectedStatues.put(player.getUniqueId(), stand);
            }
            form.Remove();
        }

        com.houzicore.shared.core.hologram.Hologram holo = _factionHolograms.remove(player);
        if (holo != null) holo.stop();

        _wardenSentries.removeIf(sentry -> sentry.owner.equals(player));

        _survivalPoints.remove(player);
        _nerve.remove(player);
        _lastHiderBlockLoc.remove(player);
        _lastHiderMoveTime.remove(player);
        _afkFlareRecharge.remove(player);
        _points.remove(player);
        _tauntCooldown.remove(player);
        _fireworkCooldown.remove(player);
        _lastSurvivalPoints.remove(player);
        _recentHiderSkillUse.remove(player);
        _prepAnchors.remove(player);
        _panicJumpHits.remove(player);
        _panicJumpWindow.remove(player);
        _terminalChannel.remove(player);
        _terminalChannelUntil.remove(player);
        _pendingInitialDisguise.remove(player);
        _pendingHiderItems.remove(player);
        _pendingLastStandSound.remove(player);
        _pendingScannerPulseFeedback.remove(player);
        _pendingScannerPulseFound.remove(player);
        _pendingBountyDashCheck.remove(player);
        _pendingHunterRespawnKit.remove(player);
        _pendingTauntReward.remove(player);
        _camperLocation.remove(player);
        _camperTime.remove(player);
        _hiderPaths.remove(player);
        
        _brutalHits.remove(player);
        for (java.util.HashSet<Player> set : _brutalHits.values()) {
            if (set != null) set.remove(player);
        }
    }

    @EventHandler
    public void PlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        if (!IsLive()) return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (_disconnectedPlayers.containsKey(uuid)) {
            Location loc = _disconnectedPlayers.remove(uuid);
            if (loc != null) {
                player.teleport(loc);
            }
            
            Integer pts = _disconnectedPoints.remove(uuid);
            if (pts != null) {
                _points.put(player, pts);
            }
            
            org.bukkit.entity.ArmorStand stand = _disconnectedStatues.remove(uuid);
            if (stand != null) {
                stand.remove();
            }
            
            SetPlayerTeam(player, _hiders, true);
            PropRushKitLoadoutService.applyHiderLoadout(player, GetKit(player));
            
            Form form = new BlockForm(this, player, _allowedBlocks.get(UtilMath.r(_allowedBlocks.size())));
            _forms.put(player, form);
            _pendingInitialDisguise.put(player, System.currentTimeMillis() + 500L);
            
            _rejoinInvulUntil.put(uuid, System.currentTimeMillis() + 3000L);
            
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
            
            UtilPlayer.message(player, C.cGreen + "Welcome back! You have 3 seconds of invulnerability shield!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpectatorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent event)
    {
        if (!IsLive()) return;
        Player player = event.getPlayer();
        String msg = event.getMessage().toLowerCase();
        
        if (!isSpectator(player)) return;

        if (msg.startsWith("/bet ")) {
            event.setCancelled(true);
            String[] args = msg.split(" ");
            if (args.length < 3) {
                UtilPlayer.message(player, C.cYellow + "Usage: /bet <hiders|hunters> <amount>");
                return;
            }
            String teamChoice = args[1].toLowerCase();
            if (!teamChoice.equals("hiders") && !teamChoice.equals("hunters")) {
                UtilPlayer.message(player, C.cRed + "Invalid team! Choose 'hiders' or 'hunters'.");
                return;
            }
            int amount = 0;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (Exception e) {
                UtilPlayer.message(player, C.cRed + "Invalid amount!");
                return;
            }
            if (amount <= 0) {
                UtilPlayer.message(player, C.cRed + "Amount must be positive!");
                return;
            }
            int currentPoints = _points.getOrDefault(player, 0);
            if (currentPoints < amount) {
                UtilPlayer.message(player, C.cRed + "You don't have enough points! (Balance: " + currentPoints + ")");
                return;
            }
            if (_betPlaced.getOrDefault(player.getUniqueId(), false)) {
                UtilPlayer.message(player, C.cRed + "You have already placed a bet!");
                return;
            }
            _spectatorBets.put(player.getUniqueId(), teamChoice + ":" + amount);
            _betPlaced.put(player.getUniqueId(), true);
            _points.put(player, currentPoints - amount);
            UtilPlayer.message(player, C.cGreen + "Successfully bet " + amount + " points on " + teamChoice.toUpperCase() + "!");
        } 
        else if (msg.startsWith("/event ")) {
            event.setCancelled(true);
            String[] args = msg.split(" ");
            if (args.length < 2) {
                UtilPlayer.message(player, C.cYellow + "Usage: /event <blockrain|spotlight|mysterybox|noisemaker|fogroll>");
                return;
            }
            int cost = 100;
            int currentPoints = _points.getOrDefault(player, 0);
            if (currentPoints < cost) {
                UtilPlayer.message(player, C.cRed + "Triggering an event costs " + cost + " points! (Balance: " + currentPoints + ")");
                return;
            }
            String eventName = args[1].toLowerCase();
            boolean success = false;
            if (eventName.equals("blockrain")) {
                triggerBlockRainEvent();
                success = true;
            } else if (eventName.equals("spotlight")) {
                triggerSpotlightEvent();
                success = true;
            } else if (eventName.equals("mysterybox")) {
                spawnMysteryBox();
                success = true;
            } else if (eventName.equals("noisemaker")) {
                triggerNoiseMakerEvent();
                success = true;
            } else if (eventName.equals("fogroll")) {
                triggerFogRollEvent();
                success = true;
            } else {
                UtilPlayer.message(player, C.cRed + "Unknown event! Choose blockrain, spotlight, mysterybox, noisemaker, or fogroll.");
            }
            
            if (success) {
                _points.put(player, currentPoints - cost);
                UtilPlayer.message(player, C.cGreen + "Triggered event " + eventName.toUpperCase() + "! Cost: " + cost + " points.");
            }
        }
    }

    private boolean isSpectator(Player player) {
        return !IsAlive(player);
    }

    @EventHandler
    public void UpdateRandomEvents(UpdateEvent event)
    {
        if (event.getType() != UpdateType.SEC || !IsLive())
            return;

        if (System.currentTimeMillis() >= _nextEventTime)
        {
            _nextEventTime = System.currentTimeMillis() + 120000L;
            triggerRandomEvent();
        }
    }

    private void triggerRandomEvent()
    {
        int r = UtilMath.r(5);
        if (r == 0) {
            triggerBlockRainEvent();
        } else if (r == 1) {
            triggerSpotlightEvent();
        } else if (r == 2) {
            spawnMysteryBox();
        } else if (r == 3) {
            triggerNoiseMakerEvent();
        } else {
            triggerFogRollEvent();
        }
    }

    private void triggerBlockRainEvent() {
        for (Player p : UtilServer.getPlayers()) {
            sendPropRushSummary(p, "EVENT: Block Rain", "อีเวนต์: บล็อกถล่ม", "Look up! Wooden logs are raining from the sky!", "ระวังหัว! บล็อกไม้กำลังตกลงมาจากฟ้า!");
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }
        for (Player p : GetPlayers(true)) {
            for (int i = 0; i < 3; i++) {
                Location loc = p.getLocation().add(UtilMath.rr(6, true), 15, UtilMath.rr(6, true));
                FallingBlock fb = loc.getWorld().spawnFallingBlock(loc, Bukkit.createBlockData(Material.OAK_LOG));
                fb.setDropItem(false);
            }
        }
    }

    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            FallingBlock fb = (FallingBlock) event.getEntity();
            if (fb.getBlockData().getMaterial() == Material.OAK_LOG) {
                event.setCancelled(true);
                event.getEntity().remove();
                fb.getWorld().playSound(fb.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
                fb.getWorld().spawnParticle(Particle.BLOCK, fb.getLocation(), 10, 0.2, 0.2, 0.2, Bukkit.createBlockData(Material.OAK_LOG));
            }
        }
    }

    private void triggerSpotlightEvent() {
        List<Player> hiders = _hiders.GetPlayers(true);
        if (!hiders.isEmpty()) {
            Player target = hiders.get(UtilMath.r(hiders.size()));
            revealHider(target, 100);
            for (Player p : UtilServer.getPlayers()) {
                sendPropRushSummary(p, "EVENT: Spotlight", "อีเวนต์: สปอตไลต์", target.getName() + " has been spotted under the light!", target.getName() + " ถูกส่องสปอตไลต์เผยตำแหน่ง!");
            }
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
            for (int y = 0; y < 15; y++) {
                target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation().add(0, y, 0), 5, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

    private void spawnMysteryBox() {
        ArrayList<Player> livePlayers = GetPlayers(true);
        if (livePlayers.isEmpty()) return;
        Player anchor = livePlayers.get(UtilMath.r(livePlayers.size()));
        Location spawnLoc = anchor.getLocation().add(UtilMath.rr(20, true), 10, UtilMath.rr(20, true));
        org.bukkit.block.Block highest = spawnLoc.getWorld().getHighestBlockAt(spawnLoc);
        if (highest == null || highest.getType() == org.bukkit.Material.AIR || !highest.getType().isSolid()) {
            return;
        }
        spawnLoc = highest.getLocation().add(0.5, 1, 0.5);
        
        org.bukkit.entity.Item box = spawnLoc.getWorld().dropItem(spawnLoc, ItemStackFactory.Instance.CreateStack(org.bukkit.Material.ENDER_CHEST, (byte) 0, 1, C.cPurple + C.Bold + "Mystery Box"));
        box.setPickupDelay(32767);
        box.setCustomNameVisible(true);
        box.setCustomName(C.cPurple + C.Bold + "Mystery Box");
        
        _mysteryBoxes.put(box, System.currentTimeMillis());
        
        for (Player p : UtilServer.getPlayers()) {
            sendPropRushSummary(p, "EVENT: Mystery Box Spawned", "อีเวนต์: กล่องปริศนาปรากฏ", "A mystery box has dropped somewhere! Go grab it!", "กล่องปริศนาตกสู่สนามแล้ว! รีบไปเก็บเร็ว!");
        }
    }

    private void triggerNoiseMakerEvent() {
        boolean madeNoise = false;
        for (Player hider : _hiders.GetPlayers(true)) {
            Form form = _forms.get(hider);
            if (form instanceof BlockForm && ((BlockForm)form).GetBlock() != null) {
                hider.getWorld().playSound(hider.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1.5f, 1.0f);
                UtilParticle.PlayParticle(ParticleType.NOTE, hider.getLocation().add(0, 1.5, 0), 0.5f, 0.5f, 0.5f, 0, 5, ViewDist.NORMAL, UtilServer.getPlayers());
                madeNoise = true;
            }
        }
        if (madeNoise) {
            for (Player p : UtilServer.getPlayers()) {
                sendPropRushSummary(p, "EVENT: Noise Maker", "อีเวนต์: ผู้ส่งเสียงดัง", "Solidified Hiders made chicken noises! Hunt them down!", "ผู้แอบที่แปลงเป็นบล็อกทำเสียงไก่ร้อง! ไล่ล่าเลย!");
                p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 1.0f, 1.0f);
            }
        }
    }

    private void triggerFogRollEvent() {
        for (Player seeker : _seekers.GetPlayers(true)) {
            seeker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS, 300, 0, false, false));
            seeker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 300, 0, false, false));
        }
        for (Player p : UtilServer.getPlayers()) {
            sendPropRushSummary(p, "EVENT: Fog Roll", "อีเวนต์: หมอกหนาทึบ", "Thick fog has limited Hunters' visibility and slowed them!", "หมอกหนาลงจัดทำให้ทัศนวิสัยของผู้ล่าลดลงและเคลื่อนที่ช้าลง!");
            p.playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 0.8f);
        }
    }

    @EventHandler
    public void UpdateDynamicBalance(UpdateEvent event)
    {
        if (event.getType() != UpdateType.SEC || !IsLive() || _initialHiderCount <= 0)
            return;

        long elapsed = System.currentTimeMillis() - _gameStartTime;
        long totalDuration = getPhaseDuration() + _huntTime + _panicTime + _chaosTime;
        double timePercent = (double) elapsed / totalDuration;
        
        int hidersAlive = _hiders.GetPlayers(true).size();
        double caughtPercent = (double) (_initialHiderCount - hidersAlive) / _initialHiderCount;
        
        if (timePercent > 0.50 && caughtPercent < 0.30)
        {
            for (Player hunter : _seekers.GetPlayers(true))
            {
                if (!hunter.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED))
                {
                    hunter.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 200, 0, false, false));
                }
            }
        }
        
        if (timePercent > 0.75 && caughtPercent < 0.50)
        {
            for (Player hunter : _seekers.GetPlayers(true))
            {
                hunter.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 200, 1, false, false));
            }
            
            if (System.currentTimeMillis() % 20000 < 1000)
            {
                List<Player> liveHiders = _hiders.GetPlayers(true);
                if (!liveHiders.isEmpty())
                {
                    Player target = liveHiders.get(UtilMath.r(liveHiders.size()));
                    revealHider(target, 60);
                    announcePropRushSummary("Pity System", target.getName() + " was revealed to balance the game!");
                }
            }
        }
    }

    private void payoutBets()
    {
        if (WinnerTeam == null) return;
        String winningTeamName = WinnerTeam.GetName().toLowerCase();
        
        for (Map.Entry<UUID, String> entry : _spectatorBets.entrySet())
        {
            UUID uuid = entry.getKey();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            
            String[] betParts = entry.getValue().split(":");
            if (betParts.length < 2) continue;
            
            String betTeam = betParts[0];
            int amount = Integer.parseInt(betParts[1]);
            
            if (betTeam.equals(winningTeamName))
            {
                int payout = amount * 2;
                _points.put(player, _points.getOrDefault(player, 0) + payout);
                UtilPlayer.message(player, C.cGreen + C.Bold + "⭐ BET WON! " + C.cWhite + "Your bet on " + betTeam.toUpperCase() + " won. Received " + payout + " points!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            }
            else
            {
                UtilPlayer.message(player, C.cRed + "✗ BET LOST! " + C.cWhite + "Your bet on " + betTeam.toUpperCase() + " lost.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        }
    }

    private void displayPostMatchCeremony()
    {
        UUID masterHiderUuid = null;
        long maxSurvivalTime = -1;
        for (Player hider : _hiders.GetPlayers(false))
        {
            long survivalTime = _hiderCaughtTime.getOrDefault(hider.getUniqueId(), System.currentTimeMillis() - _gameStartTime);
            if (survivalTime > maxSurvivalTime)
            {
                maxSurvivalTime = survivalTime;
                masterHiderUuid = hider.getUniqueId();
            }
        }
        String masterHiderName = masterHiderUuid != null ? Bukkit.getOfflinePlayer(masterHiderUuid).getName() : "None";

        UUID eagleEyeUuid = null;
        int maxKills = 0;
        for (Map.Entry<UUID, Integer> entry : _hunterKills.entrySet())
        {
            if (entry.getValue() > maxKills)
            {
                maxKills = entry.getValue();
                eagleEyeUuid = entry.getKey();
            }
        }
        String eagleEyeName = eagleEyeUuid != null ? Bukkit.getOfflinePlayer(eagleEyeUuid).getName() : "None";

        UUID closeCallUuid = null;
        int maxProximityTicks = 0;
        for (Map.Entry<UUID, Integer> entry : _closeCalls.entrySet())
        {
            if (entry.getValue() > maxProximityTicks)
            {
                maxProximityTicks = entry.getValue();
                closeCallUuid = entry.getKey();
            }
        }
        String closeCallName = closeCallUuid != null ? Bukkit.getOfflinePlayer(closeCallUuid).getName() : "None";

        UUID worstHiderUuid = null;
        long minSurvivalTime = Long.MAX_VALUE;
        for (Map.Entry<UUID, Long> entry : _hiderCaughtTime.entrySet())
        {
            if (entry.getValue() < minSurvivalTime)
            {
                minSurvivalTime = entry.getValue();
                worstHiderUuid = entry.getKey();
            }
        }
        String worstHiderName = worstHiderUuid != null ? Bukkit.getOfflinePlayer(worstHiderUuid).getName() : "None";

        UUID perfectMimicUuid = null;
        String bestGrade = "D";
        for (Map.Entry<UUID, String> entry : _hiderMaxGrade.entrySet())
        {
            if (perfectMimicUuid == null || isBetterGrade(entry.getValue(), bestGrade))
            {
                bestGrade = entry.getValue();
                perfectMimicUuid = entry.getKey();
            }
        }
        String perfectMimicName = perfectMimicUuid != null ? Bukkit.getOfflinePlayer(perfectMimicUuid).getName() : "None";

        for (Player p : UtilServer.getPlayers())
        {
            p.sendMessage(C.cBlue + C.Strike + "=============================================");
            p.sendMessage(C.cYellow + C.Bold + tr(p, "         BLOCK HUNT MATCH AWARDS             ", "         รางวัลประจำแมตช์ BLOCK HUNT          "));
            p.sendMessage(C.cBlue + C.Strike + "=============================================");
            
            p.sendMessage(C.cGreen + tr(p, "🏆 Master of Disguise (Survived Longest): ", "🏆 สุดยอดคนแอบ (รอดนานที่สุด): ") + C.cWhite + masterHiderName + C.cGray + " (" + formatSurvivalTime(maxSurvivalTime) + ")");
            p.sendMessage(C.cGreen + tr(p, "🏹 Eagle Eye (Most Kills): ", "🏹 ตาเหยี่ยว (กำจัดได้มากที่สุด): ") + C.cWhite + eagleEyeName + C.cGray + " (" + maxKills + " kills)");
            p.sendMessage(C.cGreen + tr(p, "⚡ Close Call (Near Hunters Most): ", "⚡ หวุดหวิด (อยู่ใกล้ผู้ล่าบ่อยที่สุด): ") + C.cWhite + closeCallName + C.cGray + " (" + String.format("%.1f", maxProximityTicks * 0.25) + "s)");
            p.sendMessage(C.cGreen + tr(p, "💀 Worst Hider (Caught Fastest): ", "💀 แอบกาก (โดนจับได้เร็วที่สุด): ") + C.cWhite + worstHiderName + (minSurvivalTime == Long.MAX_VALUE ? "" : C.cGray + " (" + formatSurvivalTime(minSurvivalTime) + ")"));
            p.sendMessage(C.cGreen + tr(p, "🎭 Perfect Mimic (Best Disguise Grade): ", "🎭 ก๊อปปี้เกรดเอ (พรางตัวได้เนียนที่สุด): ") + C.cWhite + perfectMimicName + C.cGray + " (Grade " + bestGrade + ")");
            
            p.sendMessage(C.cBlue + C.Strike + "=============================================");
        }
    }

    private String formatSurvivalTime(long ms)
    {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private boolean isBetterGrade(String g1, String g2) {
        String order = "SABCD";
        return order.indexOf(g1) < order.indexOf(g2);
    }

    /**
     * Creates a morph tool item (e.g. for Chameleon kit) used to swap forms.
     * Uses PersistentDataContainer to tag the item safely without relying on item names.
     */
    public ItemStack createMorphTool(Material material, String name, int amount) {
        ItemStack item = new ItemStack(material, amount);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey("houzicore", "hideseek_morph_tool"),
                org.bukkit.persistence.PersistentDataType.BYTE,
                (byte) 1
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isMorphTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey("houzicore", "hideseek_morph_tool"),
            org.bukkit.persistence.PersistentDataType.BYTE
        );
    }
}

