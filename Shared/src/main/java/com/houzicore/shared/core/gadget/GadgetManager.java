package com.houzicore.shared.core.gadget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.core.cosmetic.CosmeticProgression;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.event.GadgetCollideEntityEvent;
import com.houzicore.shared.core.gadget.gadgets.*;
import com.houzicore.shared.core.gadget.gadgets.item.ItemFleshHook;
import com.houzicore.shared.core.gadget.gadgets.item.ItemFreezeCannon;
import com.houzicore.shared.core.gadget.types.AuraGadget;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.core.gadget.types.MorphGadget;
import com.houzicore.shared.core.gadget.types.MusicGadget;
import com.houzicore.shared.core.gadget.types.OutfitGadget;
import com.houzicore.shared.core.gadget.types.OutfitGadget.ArmorSlot;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.mount.MountManager;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.projectile.ProjectileManager;

public class GadgetManager extends MiniPlugin {
	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;
	private final InventoryManager _inventoryManager;
	private final PetManager _petManager;
	private final PreferencesManager _preferencesManager;
	private final DisguiseManager _disguiseManager;
	private final BlockRestore _blockRestore;
	private final ProjectileManager _projectileManager;
	private final com.houzicore.shared.api.feature.FeatureGate _featureGate;

	private NautHashMap<GadgetType, List<Gadget>> _gadgets;

	private final NautHashMap<Player, Long> _lastMove = new NautHashMap<>();
	private final NautHashMap<Player, NautHashMap<GadgetType, Gadget>> _playerActiveGadgetMap = new NautHashMap<>();
	private final NautHashMap<Player, NautHashMap<GadgetType, Gadget>> _suspendedGadgets = new NautHashMap<>();

	private boolean _hideParticles = false;
	private int _activeItemSlot = 3;

	public GadgetManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager,
			InventoryManager inventoryManager, MountManager mountManager, PetManager petManager,
			PreferencesManager preferencesManager, DisguiseManager disguiseManager, BlockRestore blockRestore,
			ProjectileManager projectileManager, com.houzicore.shared.api.feature.FeatureGate featureGate) {
		super("Gadget Manager", plugin);

		_clientManager = clientManager;
		_donationManager = donationManager;
		_inventoryManager = inventoryManager;
		_petManager = petManager;
		_preferencesManager = preferencesManager;
		_disguiseManager = disguiseManager;
		_blockRestore = blockRestore;
		_projectileManager = projectileManager;
		_featureGate = featureGate;

		CreateGadgets();
	}

	private void addGadget(Gadget gadget) {
		if (!_gadgets.containsKey(gadget.getGadgetType())) {
			_gadgets.put(gadget.getGadgetType(), new ArrayList<Gadget>());
		}

		_gadgets.get(gadget.getGadgetType()).add(gadget);
	}

	public boolean canPlaySongAt(Location location) {
		for (final Gadget gadget : _gadgets.get(GadgetType.MusicDisc)) {
			if (gadget instanceof MusicGadget) {
				if (!((MusicGadget) gadget).canPlayAt(location))
					return false;
			}
		}

		return true;
	}

	@EventHandler
	public void chissMeow(PlayerToggleSneakEvent event) {
		if (event.getPlayer().getName().equals("Chiss")) {
			if (!event.getPlayer().isSneaking()) {
				event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
			}
		}
	}

	public boolean collideEvent(Gadget gadget, Player other) {
		final GadgetCollideEntityEvent collideEvent = new GadgetCollideEntityEvent(gadget, other);

		Bukkit.getServer().getPluginManager().callEvent(collideEvent);

		return collideEvent.isCancelled();
	}

	private void CreateGadgets() {
		_gadgets = new NautHashMap<>();

		// Items
		addGadget(new ItemEtherealPearl(this));
		addGadget(new ItemFirework(this));
		addGadget(new ItemTNT(this));
		addGadget(new ItemMelonLauncher(this));
		addGadget(new ItemFleshHook(this));
		addGadget(new ItemPaintballGun(this));
		addGadget(new ItemBatGun(this));
		addGadget(new ItemFreezeCannon(this));
		addGadget(new ItemGrapplingHook(this));
		addGadget(new ItemCoinBomb(this));
		addGadget(new ItemMagicMelody(this));
		addGadget(new ItemMeteorSmash(this));
		addGadget(new ItemMobBomb(this));
		addGadget(new ItemFootball(this));
		addGadget(new ItemDuelingSword(this));
		addGadget(new ItemEssenceBomb(this));
		addGadget(new ItemPartyPopper(this));
		addGadget(new ItemFlowerGift(this));
		addGadget(new ItemSnowballVolley(this));
		addGadget(new ItemTrampoline(this));
		addGadget(new ItemFleshHook(this));
		addGadget(new ItemFreezeCannon(this));

		// Costume
		addGadget(new OutfitRaveSuit(this, "Rave Hat", -2, ArmorSlot.Helmet, Material.LEATHER_HELMET, (byte) 0));
		addGadget(new OutfitRaveSuit(this, "Rave Shirt", -2, ArmorSlot.Chest, Material.LEATHER_CHESTPLATE, (byte) 0));
		addGadget(new OutfitRaveSuit(this, "Rave Pants", -2, ArmorSlot.Legs, Material.LEATHER_LEGGINGS, (byte) 0));
		addGadget(new OutfitRaveSuit(this, "Rave Boots", -2, ArmorSlot.Boots, Material.LEATHER_BOOTS, (byte) 0));

		addGadget(new OutfitSpaceSuit(this, "Space Helmet", -2, ArmorSlot.Helmet, Material.GLASS, (byte) 0));
		addGadget(new OutfitSpaceSuit(this, "Space Jacket", -2, ArmorSlot.Chest, Material.GOLDEN_CHESTPLATE, (byte) 0));
		addGadget(new OutfitSpaceSuit(this, "Space Pants", -2, ArmorSlot.Legs, Material.GOLDEN_LEGGINGS, (byte) 0));
		addGadget(new OutfitSpaceSuit(this, "Space Boots", -2, ArmorSlot.Boots, Material.GOLDEN_BOOTS, (byte) 0));
		addGadget(new OutfitColorSuit(this, "Freeze Crown",
				new String[] { "A sharp icy crown for", "players who want a colder look." }, -2, ArmorSlot.Helmet,
				Material.LEATHER_HELMET, (byte) 0, org.bukkit.Color.fromRGB(164, 230, 255), org.bukkit.Particle.SNOWFLAKE));
		addGadget(new OutfitColorSuit(this, "Freeze Tunic",
				new String[] { "A frosted chest piece", "for a full winter set." }, -2, ArmorSlot.Chest,
				Material.LEATHER_CHESTPLATE, (byte) 0, org.bukkit.Color.fromRGB(164, 230, 255), org.bukkit.Particle.SNOWFLAKE));
		addGadget(new OutfitColorSuit(this, "Freeze Leggings",
				new String[] { "Cold, bright, and ready", "for a winter showcase." }, -2, ArmorSlot.Legs,
				Material.LEATHER_LEGGINGS, (byte) 0, org.bukkit.Color.fromRGB(164, 230, 255), org.bukkit.Particle.SNOWFLAKE));
		addGadget(new OutfitColorSuit(this, "Freeze Boots",
				new String[] { "Leave a chilly glow", "with every step." }, -2, ArmorSlot.Boots, Material.LEATHER_BOOTS,
				(byte) 0, org.bukkit.Color.fromRGB(164, 230, 255), org.bukkit.Particle.SNOWFLAKE));
		addGadget(new OutfitColorSuit(this, "Reindeer Hood",
				new String[] { "A festive brown hood", "for the full holiday look." }, -2, ArmorSlot.Helmet,
				Material.LEATHER_HELMET, (byte) 0, org.bukkit.Color.fromRGB(123, 77, 50), org.bukkit.Particle.HEART));
		addGadget(new OutfitColorSuit(this, "Reindeer Jacket",
				new String[] { "Warm holiday style", "for crowded lobby nights." }, -2, ArmorSlot.Chest,
				Material.LEATHER_CHESTPLATE, (byte) 0, org.bukkit.Color.fromRGB(123, 77, 50), org.bukkit.Particle.HEART));
		addGadget(new OutfitColorSuit(this, "Reindeer Pants",
				new String[] { "Festive leggings with", "a cozy winter palette." }, -2, ArmorSlot.Legs,
				Material.LEATHER_LEGGINGS, (byte) 0, org.bukkit.Color.fromRGB(123, 77, 50), org.bukkit.Particle.HEART));
		addGadget(new OutfitColorSuit(this, "Reindeer Boots",
				new String[] { "A sturdy festive finish", "for the set." }, -2, ArmorSlot.Boots,
				Material.LEATHER_BOOTS, (byte) 0, org.bukkit.Color.fromRGB(123, 77, 50), org.bukkit.Particle.HEART));
		addGadget(new OutfitColorSuit(this, "Emerald Cap",
				new String[] { "Bright green style", "with a lucky sparkle." }, -2, ArmorSlot.Helmet,
				Material.LEATHER_HELMET, (byte) 0, org.bukkit.Color.fromRGB(32, 168, 86), org.bukkit.Particle.HAPPY_VILLAGER));
		addGadget(new OutfitColorSuit(this, "Emerald Tunic",
				new String[] { "A rich green chest piece", "made for parade energy." }, -2, ArmorSlot.Chest,
				Material.LEATHER_CHESTPLATE, (byte) 0, org.bukkit.Color.fromRGB(32, 168, 86), org.bukkit.Particle.HAPPY_VILLAGER));
		addGadget(new OutfitColorSuit(this, "Emerald Leggings",
				new String[] { "Lucky green leggings", "for the full set." }, -2, ArmorSlot.Legs,
				Material.LEATHER_LEGGINGS, (byte) 0, org.bukkit.Color.fromRGB(32, 168, 86), org.bukkit.Particle.HAPPY_VILLAGER));
		addGadget(new OutfitColorSuit(this, "Emerald Boots",
				new String[] { "Step through the hub", "with a brighter palette." }, -2, ArmorSlot.Boots,
				Material.LEATHER_BOOTS, (byte) 0, org.bukkit.Color.fromRGB(32, 168, 86), org.bukkit.Particle.HAPPY_VILLAGER));

		// Morphs
		addGadget(new MorphVillager(this));
		addGadget(new MorphCow(this));
		addGadget(new MorphChicken(this));
		addGadget(new MorphBlock(this));
		addGadget(new MorphEnderman(this));
		addGadget(new MorphBat(this));
		// addGadget(new MorphNotch(this));
		addGadget(new MorphPumpkinKing(this));
		addGadget(new MorphPig(this));
		addGadget(new MorphCreeper(this));
		addGadget(new MorphBlaze(this));
		// addGadget(new MorphGeno(this));
		addGadget(new MorphWither(this));
		addGadget(new MorphBunny(this));
		addGadget(new MorphWarden(this));
		addGadget(new MorphParrot(this));
		addGadget(new MorphFox(this));

		// Particles
		addGadget(new ParticleFoot(this));
		addGadget(new ParticleEnchant(this));
		addGadget(new ParticleFireRings(this));
		addGadget(new ParticleRain(this));
		addGadget(new ParticleHelix(this));
		addGadget(new ParticleGreen(this));
		addGadget(new ParticleHeart(this));
		addGadget(new ParticleFairy(this));
		addGadget(new ParticleLegend(this));
		addGadget(new ParticleBlizzard(this));
		addGadget(new ParticleHalo(this));
		addGadget(new ParticleCherry(this));
		addGadget(new ParticleDragonBreath(this));
		addGadget(new ParticleSculk(this));
		addGadget(new ParticleAmethyst(this));
		addGadget(new ParticleFirefly(this));
		addGadget(new ParticleMusicNotes(this));
		addGadget(new ParticleRainbowTrail(this));
		addGadget(new ParticleJetpack(this));
		addGadget(new ParticleAngelWings(this));
		addGadget(new ParticleDemonWings(this));
		addGadget(new ParticleFrostWings(this));
		addGadget(new ParticleFoxTail(this));
		addGadget(new ParticleWolfTail(this));
		addGadget(new ParticleDeepSeaSwirl(this));
		addGadget(new ParticleYinYang(this));
		addGadget(new ParticlePhoenixWings(this));

		// Music
		addGadget(new MusicGadget(this, "13 Disc", new String[] { "" }, -2, 2256, 178000));
		addGadget(new MusicGadget(this, "Cat Disc", new String[] { "" }, -2, 2257, 185000));
		addGadget(new MusicGadget(this, "Blocks Disc", new String[] { "" }, -2, 2258, 345000));
		addGadget(new MusicGadget(this, "Chirp Disc", new String[] { "" }, -2, 2259, 185000));
		addGadget(new MusicGadget(this, "Far Disc", new String[] { "" }, -2, 2260, 174000));
		addGadget(new MusicGadget(this, "Mall Disc", new String[] { "" }, -2, 2261, 197000));
		addGadget(new MusicGadget(this, "Mellohi Disc", new String[] { "" }, -2, 2262, 96000));
		addGadget(new MusicGadget(this, "Stal Disc", new String[] { "" }, -2, 2263, 150000));
		addGadget(new MusicGadget(this, "Strad Disc", new String[] { "" }, -2, 2264, 188000));
		addGadget(new MusicGadget(this, "Ward Disc", new String[] { "" }, -2, 2265, 251000));
		// addGadget(new MusicGadget(this, "11 Disc", new String[] {""}, -2, 2266,
		// 71000));
		addGadget(new MusicGadget(this, "Wait Disc", new String[] { "" }, -2, 2267, 238000));
		addGadget(new MusicGadget(this, "Pigstep Disc", new String[] { "A louder modern lobby groove." }, -2,
				Material.MUSIC_DISC_PIGSTEP, 149000));
		addGadget(new MusicGadget(this, "Otherside Disc", new String[] { "A drifting late-night server vibe." }, -2,
				Material.MUSIC_DISC_OTHERSIDE, 195000));
		addGadget(new MusicGadget(this, "Relic Disc", new String[] { "A relic-era groove for longer hangs." }, -2,
				Material.MUSIC_DISC_RELIC, 218000));
		addGadget(new MusicGadget(this, "5 Disc", new String[] { "A darker lobby atmosphere cut." }, -2,
				Material.MUSIC_DISC_5, 178000));
		addGadget(new MusicGadget(this, "Creator Disc", new String[] { "A lighter builder-side soundtrack." }, -2,
				Material.MUSIC_DISC_CREATOR, 176000));
		addGadget(new MusicGadget(this, "Precipice Disc", new String[] { "A dramatic finale for full hubs." }, -2,
				Material.MUSIC_DISC_PRECIPICE, 242000));

		// Sprays
		addGadget(new SprayCat(this));
		addGadget(new SprayStar(this));
		addGadget(new SprayHeart(this));
		addGadget(new SpraySkull(this));
		addGadget(new SprayHouziLogo(this));
		addGadget(new SprayGGEZ(this));

		// Auras
		addGadget(new AuraFlame(this));
		addGadget(new AuraCrystal(this));
		addGadget(new AuraShadow(this));
		addGadget(new AuraCherry(this));

		// Win Effects
		addGadget(new WinEffectFireworks(this));
		addGadget(new WinEffectLightning(this));
		addGadget(new WinEffectDragonRise(this));
		addGadget(new WinEffectPartyAnimal(this));
		addGadget(new WinEffectTornado(this));
		addGadget(new WinEffectSolarFlare(this));
		addGadget(new WinEffectEarthquake(this));
		addGadget(new WinEffectLavaTrap(this));
		addGadget(new WinEffectElderGuardian(this));
		addGadget(new WinEffectPodium(this));
		addGadget(new WinEffectHalloween(this));
		addGadget(new WinEffectLoveIsABattlefield(this));
		addGadget(new WinEffectWinterWarfare(this));

		// Kill Effects
		addGadget(new KillEffectLavaFountain(this));
		addGadget(new KillEffectRainbowRing(this));
		addGadget(new KillEffectBloodBurst(this));

		// Banners
		addGadget(new BannerChampion(this));

		// Baits
		addGadget(new PremiumBait(this));

		// [WOW] Premium Config-Driven Additions
		addGadget(new DeathEffectTornado(this));
		addGadget(new GameTauntEmoji(this));
		
		// Tracers
		addGadget(new TracerHeart(this));
		addGadget(new TracerRainbow(this));
		addGadget(new TracerFrostLord(this));
		addGadget(new TracerStorm(this));
		addGadget(new TracerEnchant(this));

		// Double Jumps
		addGadget(new DoubleJumpSlime(this));
		addGadget(new DoubleJumpCupidsWings(this));
		addGadget(new DoubleJumpFirecracker(this));
		addGadget(new DoubleJumpRainbow(this));
		
		// Load from cosmetics.yml
		loadCosmeticsConfig();

		// Normalize gadget pricing to Common -> Rare -> Legends tiers.
		for (List<com.houzicore.shared.core.gadget.types.Gadget> gadgetList : _gadgets.values()) {
			for (com.houzicore.shared.core.gadget.types.Gadget gadget : gadgetList) {
				int price = CosmeticProgression.getPrice(CosmeticProgression.getShopRarity(gadget));
				gadget.setEssenceCost(price);

				if (gadget instanceof ItemGadget) {
					((ItemGadget) gadget).getAmmo().setEssenceCost(price);
				} else if (gadget instanceof com.houzicore.shared.core.gadget.types.BaitGadget) {
					((com.houzicore.shared.core.gadget.types.BaitGadget) gadget).getAmmo().setEssenceCost(price);
				}
			}

			gadgetList.sort(CosmeticProgression.gadgetComparator());
		}
	}

	private void loadCosmeticsConfig() {
		java.io.File file = new java.io.File(getPlugin().getDataFolder(), "cosmetics.yml");
		if (!file.exists()) {
			getPlugin().getDataFolder().mkdirs();
			try (java.io.InputStream in = getClass().getResourceAsStream("/cosmetics.yml")) {
				if (in != null) {
					java.nio.file.Files.copy(in, file.toPath());
				} else {
				}
			} catch (java.io.IOException e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
			}
		}
		org.bukkit.configuration.file.FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
		for (GadgetType type : _gadgets.keySet()) {
			java.util.Iterator<Gadget> iterator = _gadgets.get(type).iterator();
			while (iterator.hasNext()) {
				Gadget g = iterator.next();
				String nameKey = g.GetName().replace(" ", "");
				if (!config.getBoolean("Gadgets." + nameKey + ".Enabled", true)) {
					iterator.remove();
				}
			}
		}
	}

	@EventHandler
	public void death(PlayerDeathEvent event) {
		_lastMove.remove(event.getEntity());

		_playerActiveGadgetMap.remove(event.getEntity());
	}

	public void DisableAll() {
		for (final GadgetType gadgetType : _gadgets.keySet()) {
			for (final Gadget gadget : _gadgets.get(gadgetType)) {
				for (final Player player : UtilServer.getPlayers()) {
					gadget.Disable(player);
				}
			}
		}
	}

	public void DisableAll(Player player) {
		for (final GadgetType gadgetType : _gadgets.keySet()) {
			for (final Gadget gadget : _gadgets.get(gadgetType)) {
				gadget.Disable(player);
			}
		}
	}

	public List<String> getActiveSnapshot(Player player) {
		List<String> snapshot = new ArrayList<>();
		NautHashMap<GadgetType, Gadget> current = _playerActiveGadgetMap.get(player);
		if (current == null || current.isEmpty()) {
			return snapshot;
		}

		for (Map.Entry<GadgetType, Gadget> entry : current.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				continue;
			}

			snapshot.add(entry.getKey().name() + "|" + entry.getValue().GetName());
		}

		return snapshot;
	}

	public Gadget findGadget(GadgetType gadgetType, String name) {
		if (gadgetType == null || name == null) {
			return null;
		}

		List<Gadget> gadgets = _gadgets.get(gadgetType);
		if (gadgets == null) {
			return null;
		}

		for (Gadget gadget : gadgets) {
			if (gadget != null && gadget.GetName().equalsIgnoreCase(name)) {
				return gadget;
			}
		}

		return null;
	}

	public void suspend(Player player) {
		if (!_playerActiveGadgetMap.containsKey(player)) return;

		NautHashMap<GadgetType, Gadget> active = new NautHashMap<>();
		NautHashMap<GadgetType, Gadget> current = _playerActiveGadgetMap.get(player);
		if (current == null || current.isEmpty()) return;

		for (GadgetType type : current.keySet()) {
			active.put(type, current.get(type));
		}

		_suspendedGadgets.put(player, active);

		// Disable all EXCEPT game-compatible gadgets (e.g. Phoenix Wings)
		for (final GadgetType gadgetType : _gadgets.keySet()) {
			for (final Gadget gadget : _gadgets.get(gadgetType)) {
				if (!gadget.isGameCompatible()) {
					gadget.Disable(player);
				}
			}
		}
	}

	public void resume(Player player) {
		NautHashMap<GadgetType, Gadget> suspended = _suspendedGadgets.remove(player);
		if (suspended == null || suspended.isEmpty()) return;

		for (Gadget gadget : suspended.values()) {
			gadget.Enable(player);
		}
	}

	public Gadget getActive(Player player, GadgetType gadgetType) {
		if (!_playerActiveGadgetMap.containsKey(player)) {
			_playerActiveGadgetMap.put(player, new NautHashMap<GadgetType, Gadget>());
		}

		return _playerActiveGadgetMap.get(player).get(gadgetType);
	}

	public int getActiveItemSlot() {
		return _activeItemSlot;
	}

	public BlockRestore getBlockRestore() {
		return _blockRestore;
	}

	public CoreClientManager getClientManager() {
		return _clientManager;
	}

	public DisguiseManager getDisguiseManager() {
		return _disguiseManager;
	}

	public DonationManager getDonationManager() {
		return _donationManager;
	}

	public List<Gadget> getGadgets(GadgetType gadgetType) {
		return _gadgets.get(gadgetType);
	}

	public InventoryManager getInventoryManager() {
		return _inventoryManager;
	}

	public PetManager getPetManager() {
		return _petManager;
	}

	public PreferencesManager getPreferencesManager() {
		return _preferencesManager;
	}

	public ProjectileManager getProjectileManager() {
		return _projectileManager;
	}

	public boolean hideParticles() {
		return _hideParticles;
	}

	public boolean isMoving(Player player) {
		if (!_lastMove.containsKey(player))
			return false;

		return !UtilTime.elapsed(_lastMove.get(player), 500);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Admins and above get ALL cosmetics unlocked automatically
		if (_clientManager.Get(event.getPlayer()).GetRank().Has(Rank.ADMIN)) {
			for (final GadgetType gadgetType : _gadgets.keySet()) {
				for (final Gadget gadget : _gadgets.get(gadgetType)) {
					_donationManager.Get(event.getPlayer().getName())
							.AddUnknownSalesPackagesOwned(gadget.GetName());
				}
			}
		}
	}

	@EventHandler
	public void quit(PlayerQuitEvent event) {
		DisableAll(event.getPlayer());
		_lastMove.remove(event.getPlayer());
		_playerActiveGadgetMap.remove(event.getPlayer());
		_suspendedGadgets.remove(event.getPlayer());
	}

	public void redisplayActiveItem(Player player) {
		for (final Gadget gadget : _gadgets.get(GadgetType.Item)) {
			if (gadget instanceof ItemGadget) {
				if (gadget.IsActive(player)) {
					((ItemGadget) gadget).ApplyItem(player, false);
				}
			}
		}
	}

	public void removeActive(Player player, Gadget gadget) {
		if (!_playerActiveGadgetMap.containsKey(player)) {
			_playerActiveGadgetMap.put(player, new NautHashMap<GadgetType, Gadget>());
		}

		_playerActiveGadgetMap.get(player).remove(gadget.getGadgetType());
	}

	public void RemoveItem(Player player) {
		for (final GadgetType gadgetType : _gadgets.keySet()) {
			for (final Gadget gadget : _gadgets.get(gadgetType)) {
				if (gadget instanceof ItemGadget) {
					final ItemGadget item = (ItemGadget) gadget;

					item.RemoveItem(player);
				}
			}
		}
	}

	public void RemoveBait(Player player) {
		for (final GadgetType gadgetType : _gadgets.keySet()) {
			for (final Gadget gadget : _gadgets.get(gadgetType)) {
				if (gadget instanceof com.houzicore.shared.core.gadget.types.BaitGadget) {
					gadget.Disable(player);
				}
			}
		}
	}

	public void RemoveMorph(Player player) {
		for (final GadgetType gadgetType : _gadgets.keySet()) {
			for (final Gadget gadget : _gadgets.get(gadgetType)) {
				if (gadget instanceof MorphGadget) {
					final MorphGadget part = (MorphGadget) gadget;

					part.Disable(player);
				}
			}
		}
	}

	// Disallows two armor gadgets in same slot.
	public void RemoveOutfit(Player player, ArmorSlot slot) {
		for (final GadgetType gadgetType : _gadgets.keySet()) {
			for (final Gadget gadget : _gadgets.get(gadgetType)) {
				if (gadget instanceof OutfitGadget) {
					final OutfitGadget armor = (OutfitGadget) gadget;

					if (armor.GetSlot() == slot) {
						armor.RemoveArmor(player);
					}
				}
			}
		}
	}

	public void RemoveParticle(Player player) {
		for (final GadgetType gadgetType : _gadgets.keySet()) {
			for (final Gadget gadget : _gadgets.get(gadgetType)) {
				if (gadget instanceof ParticleGadget) {
					final ParticleGadget part = (ParticleGadget) gadget;

					part.Disable(player);
				}
			}
		}
	}

	public void setActive(Player player, Gadget gadget) {
		if (!_playerActiveGadgetMap.containsKey(player)) {
			_playerActiveGadgetMap.put(player, new NautHashMap<GadgetType, Gadget>());
		}

		_playerActiveGadgetMap.get(player).put(gadget.getGadgetType(), gadget);
	}

	public void setActiveItemSlot(int i) {
		_activeItemSlot = i;
	}

	public void setHideParticles(boolean b) {
		_hideParticles = b;
	}

	@EventHandler
	public void setMoving(PlayerMoveEvent event) {
		if (UtilMath.offset(event.getFrom(), event.getTo()) <= 0)
			return;

		_lastMove.put(event.getPlayer(), System.currentTimeMillis());
	}

	@EventHandler
	public void onUpdate(com.houzicore.shared.updater.event.UpdateEvent event) {
		// Destructive continuous cleanup moved to Context-Driven explicit suspend/resume orchestration.
	}
}
