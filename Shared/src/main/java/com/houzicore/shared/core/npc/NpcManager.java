package com.houzicore.shared.core.npc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import com.houzicore.shared.database.tables.records.NpcsRecord;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
////import org.bukkit.craftbukkit.v1_21_R1.entity.CraftCreature;
// CraftLivingEntity removed (NMS not needed)
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
// Replaced NMS persistent metadata with setPersistent()

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.creature.Creature;
import com.houzicore.shared.core.creature.event.CreatureKillEntitiesEvent;
import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.npc.command.NpcCommand;
import com.houzicore.shared.core.npc.event.NpcDamageByEntityEvent;
import com.houzicore.shared.core.npc.event.NpcInteractEntityEvent;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.shared.database.Tables;
import org.jooq.Result;
import org.jooq.impl.DSL;

import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.hologram.Hologram;

public class NpcManager extends MiniPlugin {
	private static final String HOLOGRAM_CLICK = "§b§lCLICK TO PLAY";

	private static String itemStackToYaml(ItemStack stack) {
		if (stack == null || stack.getType() == Material.AIR)
			return null;
		else {
			final YamlConfiguration configuration = new YamlConfiguration();
			configuration.set("stack", stack);
			return configuration.saveToString();
		}
	}

	private static ItemStack yamlToItemStack(String yaml) {
		if (yaml == null)
			return null;
		else {
			try {
				final YamlConfiguration configuration = new YamlConfiguration();
				configuration.loadFromString(yaml);
				return configuration.getItemStack("stack");
			} catch (final InvalidConfigurationException e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);

				return null;
			}
		}
	}

	private final Creature _creature;
	private final HologramManager _hologramManager;
	private final List<Npc> _npcs = new ArrayList<>();
	final Map<UUID, Npc> _npcMap = new HashMap<>();
	private final Set<UUID> _npcDeletingPlayers = new HashSet<>();

	public List<Npc> getNpcs() {
		return _npcs;
	}

	private final Set<UUID> _adminBuilders = new HashSet<>();

	public boolean isBuilder(Player player) {
		return _adminBuilders.contains(player.getUniqueId());
	}

	public boolean toggleAdminBuilder(Player player) {
		if (_adminBuilders.contains(player.getUniqueId())) {
			_adminBuilders.remove(player.getUniqueId());
			return false;
		} else {
			_adminBuilders.add(player.getUniqueId());
			return true;
		}
	}

	public NpcManager(JavaPlugin plugin, Creature creature) {
		super("NpcManager", plugin);

		_creature = creature;
		_hologramManager = new HologramManager(plugin);

		_plugin.getServer().getScheduler().scheduleSyncRepeatingTask(_plugin, new Runnable() {
			@Override
			public void run() {
				updateNpcLocations();
			}
		}, 0L, 5L);

		_plugin.getServer().getPluginManager().registerEvents(this, _plugin);

		try {
			loadNpcs();
		} catch (final SQLException e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void addCommands() {
		addCommand(new NpcCommand(this));
		addCommand(new com.houzicore.shared.core.npc.command.AdminBuilderCommand(this));
	}

	public Entity addNpc(Player player, EntityType entityType, double radius, boolean adult, String name,
			String entityMeta) throws SQLException {
		try (Connection connection = DBPool.ACCOUNT.getConnection()) {
			final String helmet = itemStackToYaml(player.getInventory().getHelmet());
			final String chestplate = itemStackToYaml(player.getInventory().getChestplate());
			final String leggings = itemStackToYaml(player.getInventory().getLeggings());
			final String boots = itemStackToYaml(player.getInventory().getBoots());
			final String inHand = itemStackToYaml(player.getInventory().getItemInHand());

			boolean isBuilder = isBuilder(player);
			final NpcsRecord npcsRecord = DSL.using(connection).newRecord(Tables.npcs);

			if (isBuilder) {
				npcsRecord.setServer(getServerName());
				npcsRecord.setName(name);
				npcsRecord.setWorld(player.getWorld().getName());
				npcsRecord.setX(player.getLocation().getX());
				npcsRecord.setY(player.getLocation().getY());
				npcsRecord.setZ(player.getLocation().getZ());
				npcsRecord.setRadius(radius);
				npcsRecord.setEntityType(entityType.name());
				npcsRecord.setAdult(adult);
				npcsRecord.setHelmet(helmet);
				npcsRecord.setChestplate(chestplate);
				npcsRecord.setLeggings(leggings);
				npcsRecord.setBoots(boots);
				npcsRecord.setInHand(inHand);
				npcsRecord.setEntityMeta(entityMeta);

				try {
					npcsRecord.insert();
					com.houzicore.shared.common.util.UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("NpcManager", com.houzicore.shared.core.lang.LangManager.get().get(player, "npc.saved_auto").replace("{0}", name)));
				} finally {
					npcsRecord.detach();
				}
			} else {
				com.houzicore.shared.common.util.UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("NpcManager", com.houzicore.shared.core.lang.LangManager.get().get(player, "npc.spawned_temp")));
			}

			final Npc npc = new Npc(this, npcsRecord);
			_npcs.add(npc);

			return spawnNpc(npc);
		}
	}

	public void clearNpcs(boolean deleteFromDatabase) throws SQLException {
		if (deleteFromDatabase) {
			final String serverType = getServerName();

			try (Connection connection = DBPool.ACCOUNT.getConnection()) {
				DSL.using(connection).delete(Tables.npcs).where(Tables.npcs.server.eq(serverType));//.execute();
			}
		}

		for (Npc npc : _npcs) {
			if (npc.getHologram() != null) npc.getHologram().stop();
		}

		for (final World world : Bukkit.getWorlds()) {
			for (final LivingEntity entity : world.getEntitiesByClass(LivingEntity.class)) {
				if (isNpc(entity)) {
					entity.remove();
				}
			}
		}

		_npcs.clear();
		_npcMap.clear();
	}

	public boolean deleteNpc(Entity entity) throws SQLException {
		final Npc npc = getNpcByEntity(entity);

		if (npc != null) {
			if (npc.getHologram() != null) npc.getHologram().stop();

			try (Connection connection = DBPool.ACCOUNT.getConnection()) {
				npc.getDatabaseRecord().attach(DSL.using(connection).configuration());
				npc.getDatabaseRecord().delete();

				entity.remove();
				_npcMap.remove(entity.getUniqueId());
				_npcs.remove(npc);

				return true;
			} finally {
				npc.getDatabaseRecord().detach();
			}
		}

		return false;
	}

	public Npc getNpcByEntity(Entity entity) {
		if (entity == null)
			return null;

		return getNpcByEntityUUID(entity.getUniqueId());
	}

	private Npc getNpcByEntityUUID(UUID uuid) {
		if (uuid == null)
			return null;

		return _npcMap.get(uuid);
	}

	public String getServerName() {
		String serverName = getPlugin().getClass().getSimpleName();

		if (Bukkit.getMotd() != null && Bukkit.getMotd().equalsIgnoreCase("test")) {
			serverName += "-Test";
		}

		return serverName;
	}

	public void help(Player caller) {
		help(caller, null);
	}

	public void help(Player caller, String message) {
		UtilPlayer.message(caller, F.main(_moduleName, "Commands List:"));
		UtilPlayer.message(caller,
				F.help("/npc add <type> [radius] [adult] [name]", "Create a new NPC.", Rank.DEVELOPER));
		UtilPlayer.message(caller, F.help("/npc del ", com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.del_help"), Rank.DEVELOPER));
		UtilPlayer.message(caller, F.help("/npc home", com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.home_help"), Rank.DEVELOPER));
		UtilPlayer.message(caller, F.help("/npc clear", com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.clear_help"), Rank.DEVELOPER));
		UtilPlayer.message(caller, F.help("/npc refresh", com.houzicore.shared.core.lang.LangManager.get().get(caller, "npc.refresh_help"), Rank.DEVELOPER));

		if (message != null) {
			UtilPlayer.message(caller, F.main(_moduleName, ChatColor.RED + message));
		}
	}

	public boolean isDetachedNpc(LivingEntity entity) {
		return !isNpc(entity) &&
				(entity.getScoreboardTags().contains("houzicore_server_npc") ||
				(entity.getCustomName() != null && entity.getCustomName().startsWith(ChatColor.RESET.toString())));
	}

	public boolean isNpc(Entity entity) {
		return getNpcByEntity(entity) != null;
	}

	public void loadNpcs() throws SQLException {
		final String serverType = getServerName();

		try (Connection connection = DBPool.ACCOUNT.getConnection()) {
			final Result<NpcsRecord> result = DSL.using(connection).selectFrom(Tables.npcs)
					.where(Tables.npcs.server.eq(serverType)).fetch();

			for (final NpcsRecord record : result) {
				record.detach();

				final Npc npc = new Npc(this, record);
				_npcs.add(npc);

				if (npc.getChunk().isLoaded()) {
					spawnNpc(npc);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent event) {
		for (final Entity entity : event.getChunk().getEntities()) {
			if (entity instanceof LivingEntity) {
				final Npc npc = getNpcByEntity(entity);
				if (npc != null) {
					UtilEnt.silence(entity, true);
					UtilEnt.ghost(entity, true, false);

					if (npc.getDatabaseRecord().getRadius() == 0) {
						UtilEnt.Vegetate(entity);
						UtilEnt.ghost(entity, true, false);
					}
				}

				if (isDetachedNpc((LivingEntity) entity)) {
					entity.remove();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCreatureKillEntities(CreatureKillEntitiesEvent event) {
		for (final Iterator<Entity> entityIterator = event.GetEntities().iterator(); entityIterator.hasNext();) {
			if (isNpc(entityIterator.next())) {
				entityIterator.remove();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event) {
		if (isNpc(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (isNpc(event.getEntity())) {
			if (event.getEntity() instanceof LivingEntity && event.getDamager() instanceof LivingEntity) {
				final NpcDamageByEntityEvent npcEvent = new NpcDamageByEntityEvent((LivingEntity) event.getEntity(),
						(LivingEntity) event.getDamager());

				Bukkit.getServer().getPluginManager().callEvent(npcEvent);
			}

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof LivingEntity && isNpc(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		final Npc npc = getNpcByEntity(event.getEntity());

		if (npc != null) {
			npc.setEntity(null);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		if (isNpc(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onNpcDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player))
			return;

		final Player player = (Player) event.getDamager();

		final Npc npc = getNpcByEntity(event.getEntity());
		if (npc == null)
			return;

		if (npc.getInfo() == null)
			return;

		if (!Recharge.Instance.use(player, buildNpcRechargeKey(npc, "Info Click"), 2000, false, false))
			return;

		player.sendMessage(npc.getInfo());

		player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
	}

	@EventHandler
	public void onNpcInteract(PlayerInteractEntityEvent event) {
		final Npc npc = getNpcByEntity(event.getRightClicked());
		if (npc == null)
			return;

		if (npc.getInfo() == null)
			return;

		if (!Recharge.Instance.use(event.getPlayer(), buildNpcRechargeKey(npc, "Info Click"), 2000, false,
				false))
			return;

		event.getPlayer().sendMessage(npc.getInfo());

		event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof LivingEntity) {
			if (_npcDeletingPlayers.remove(event.getPlayer().getUniqueId())) {
				try {
					if (deleteNpc(event.getRightClicked())) {
						event.getPlayer().sendMessage(F.main(getName(), "Deleted npc."));
					} else {
						event.getPlayer()
								.sendMessage(F.main(getName(), "Failed to delete npc.  That one isn't in the list."));
					}
				} catch (final SQLException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			} else if (isNpc(event.getRightClicked())) {
				final NpcInteractEntityEvent npcEvent = new NpcInteractEntityEvent(
						(LivingEntity) event.getRightClicked(), event.getPlayer());

				Bukkit.getServer().getPluginManager().callEvent(npcEvent);

				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.SEC)
			return;

		if (Bukkit.getOnlinePlayers().isEmpty())
			return;

		for (final World world : Bukkit.getWorlds()) {
			for (final LivingEntity livingEntity : world.getEntitiesByClass(LivingEntity.class)) {
				if (isDetachedNpc(livingEntity)) {
					livingEntity.remove();
				}
			}
		}

		for (final Npc npc : _npcs) {
			if (npc.getEntity() != null && !npc.getEntity().isValid() && npc.getChunk().isLoaded()) {
				spawnNpc(npc);
			}
		}
	}

	@EventHandler
	public void onUpdateNpcMessage(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player player : Bukkit.getOnlinePlayers()) {
			for (final Npc npc : _npcs) {
				if (npc.getInfo() == null) {
					continue;
				}

				if (npc.getInfoRadiusSquared() == null) {
					continue;
				}

				if (npc.getDatabaseRecord().getInfoDelay() == null) {
					continue;
				}

				if (npc.getEntity() == null) {
					continue;
				}

				if (npc.getEntity().getWorld() != player.getWorld()) {
					continue;
				}

				if (npc.getEntity().getLocation().distanceSquared(player.getLocation()) > npc.getInfoRadiusSquared()) {
					continue;
				}

				if (!Recharge.Instance.use(player, buildNpcRechargeKey(npc, "Info"),
						npc.getDatabaseRecord().getInfoDelay(), false, false)) {
					continue;
				}

				player.sendMessage(npc.getInfo());

				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
			}
		}
	}

	@EventHandler
	public void onUpdateNpcHologram(UpdateEvent event) {
		if (event.getType() != UpdateType.FASTER) return;
		
		for (Npc npc : _npcs) {
			if (npc.getHologram() != null && npc.getHologram().isInUse() && npc.getEntity() != null) {
				String displayName = npc.getDisplayName();
				if (displayName == null || displayName.isEmpty()) {
					continue;
				}

				String stripedName = ChatColor.stripColor(displayName);
				String finalName = ChatColor.YELLOW + com.houzicore.shared.common.util.C.Bold + stripedName.toUpperCase(Locale.ROOT);

				if (npc.getExtraHologramLine() != null) {
					npc.getHologram().setText(
						finalName,
						npc.getExtraHologramLine(),
						HOLOGRAM_CLICK
					);
				} else {
					npc.getHologram().setText(
						finalName,
						HOLOGRAM_CLICK
					);
				}
			}
		}
	}

	public void prepDeleteNpc(Player admin) {
		_npcDeletingPlayers.add(admin.getUniqueId());
	}

	public Entity spawnNpc(Npc npc) {
		final LivingEntity entity = (LivingEntity) _creature.SpawnEntity(npc.getLocation(),
				EntityType.valueOf(npc.getDatabaseRecord().getEntityType()));

		entity.setCustomNameVisible(false);
		entity.setCustomName(null);

		if (npc.getDisplayName() != null) {
			Hologram hologram = new Hologram(_hologramManager, entity.getLocation().add(0, entity.getEyeHeight() + 1.2, 0),
					ChatColor.YELLOW + com.houzicore.shared.common.util.C.Bold + ChatColor.stripColor(npc.getDisplayName()).toUpperCase(Locale.ROOT));
			hologram.setFollowEntity(entity);
			hologram.start();
			npc.setHologram(hologram);
		}

		entity.setCanPickupItems(false);
		entity.setRemoveWhenFarAway(false);
		entity.setPersistent(false);
		entity.addScoreboardTag("houzicore_server_npc");

		if (entity instanceof Ageable) {
			if (npc.getDatabaseRecord().getAdult()) {
				((Ageable) entity).setAdult();
			} else {
				((Ageable) entity).setBaby();
			}

			((Ageable) entity).setAgeLock(true);
		}
		if (entity instanceof Zombie) {
			((Zombie) entity).setBaby(!npc.getDatabaseRecord().getAdult());
		}
		if (entity instanceof Slime && npc.getDatabaseRecord().getEntityMeta() != null) {
			((Slime) entity).setSize(Integer.parseInt(npc.getDatabaseRecord().getEntityMeta()));
		}
		if (entity instanceof Skeleton && npc.getDatabaseRecord().getEntityMeta() != null) {
			// In 1.21+, Wither Skeletons are a separate EntityType (WITHER_SKELETON).
			// NPC entity type was already resolved from the database; setSkeletonType() no longer exists.
		}
		if (entity instanceof Villager && npc.getDatabaseRecord().getEntityMeta() != null) {
			((Villager) entity)
					.setProfession(Villager.Profession.valueOf(npc.getDatabaseRecord().getEntityMeta().toUpperCase()));
		}

		if (entity instanceof org.bukkit.entity.Creature) {
			((org.bukkit.entity.Creature) entity).setTarget(null);
		}

		if (shouldHardFreeze(npc)) {
			applyHardFreezeState(entity);
			Location facingLocation = buildFacingLocation(npc);
			if (needsHardFreezeCorrection(entity, facingLocation)) {
				entity.teleport(facingLocation);
			} else {
				entity.setRotation(facingLocation.getYaw(), facingLocation.getPitch());
			}
			entity.setVelocity(new Vector(0, 0, 0));
		}

		if (npc.getDatabaseRecord().getHelmet() != null) {
			entity.getEquipment().setHelmet(yamlToItemStack(npc.getDatabaseRecord().getHelmet()));
		}
		if (npc.getDatabaseRecord().getChestplate() != null) {
			entity.getEquipment().setChestplate(yamlToItemStack(npc.getDatabaseRecord().getChestplate()));
		}
		if (npc.getDatabaseRecord().getLeggings() != null) {
			entity.getEquipment().setLeggings(yamlToItemStack(npc.getDatabaseRecord().getLeggings()));
		}
		if (npc.getDatabaseRecord().getBoots() != null) {
			entity.getEquipment().setBoots(yamlToItemStack(npc.getDatabaseRecord().getBoots()));
		}
		if (npc.getDatabaseRecord().getInHand() != null) {
			entity.getEquipment().setItemInHand(yamlToItemStack(npc.getDatabaseRecord().getInHand()));
		}

		npc.setEntity(entity);

		return entity;
	}

	public void teleportNpcsHome() {
		for (final World world : Bukkit.getWorlds()) {
			for (final LivingEntity entity : world.getEntitiesByClass(LivingEntity.class)) {
				final Npc npc = getNpcByEntity(entity);
				if (npc == null) {
					continue;
				}

				if (!entity.getLocation().getChunk().isLoaded()) {
					continue;
				}

				if (!entity.isDead() && entity.isValid()) {
					final Location location = shouldHardFreeze(npc) ? buildFacingLocation(npc) : npc.getLocation().clone();
					if (!shouldHardFreeze(npc)) {
						location.setPitch(entity.getLocation().getPitch());
						location.setYaw(entity.getLocation().getYaw());
					}
					entity.teleport(location);
					entity.setVelocity(new Vector(0, 0, 0));

					npc.setFailedAttempts(0);
				}
			}
		}
	}

	private void updateNpcLocations() {
		for (final World world : Bukkit.getWorlds()) {
			for (final LivingEntity entity : world.getEntitiesByClass(LivingEntity.class)) {
				final Npc npc = getNpcByEntity(entity);
				if (npc == null) {
					continue;
				}

				entity.setTicksLived(1);
				entity.setPersistent(false);
				UtilEnt.silence(entity, true);

				if (!entity.getLocation().getChunk().isLoaded()) {
					continue;
				}

				if (!entity.isDead() && entity.isValid()) {
					boolean hardFreeze = shouldHardFreeze(npc);

					if (hardFreeze) {
						applyHardFreezeState(entity);
					}

					for (final ItemStack armor : entity.getEquipment().getArmorContents()) {
						if (armor != null && armor.getType() != Material.AIR) {
							armor.setDurability((short) 0);
						}
					}

					if (hardFreeze) {
						final Location location = buildFacingLocation(npc);
						if (needsHardFreezeCorrection(entity, location)) {
							entity.teleport(location);
						} else {
							entity.setRotation(location.getYaw(), location.getPitch());
						}
						entity.setVelocity(new Vector(0, 0, 0));
						npc.setFailedAttempts(0);
					} else if (npc.getFailedAttempts() >= 10) {
						final Location location = npc.getLocation().clone();
							location.setPitch(entity.getLocation().getPitch());
							location.setYaw(entity.getLocation().getYaw());
						entity.teleport(location);
						entity.setVelocity(new Vector(0, 0, 0));
						npc.setFailedAttempts(0);
					} else if (!npc.isInRadius(entity.getLocation()) && npc.getEntity() instanceof org.bukkit.entity.Creature) {
						npc.returnToPost();
						npc.incrementFailedAttempts();
					} else if (npc.getEntity() instanceof org.bukkit.entity.Creature) {
						if (npc.isReturning()) {
							npc.clearGoals();
						}

						npc.setFailedAttempts(0);
					}
				}
			}
		}
	}

	private boolean shouldHardFreeze(Npc npc) {
		if (npc == null || npc.getDatabaseRecord() == null) {
			return false;
		}

		if (npc.getDatabaseRecord().getRadius() == 0) {
			return true;
		}

		String serverName = getServerName().toLowerCase();
		if (serverName.contains("hub") || serverName.contains("lobby"))
			return true;

		String name = npc.getDatabaseRecord().getName();
		return name != null && (name.equalsIgnoreCase("Minigame Selector") || name.equalsIgnoreCase("Quick Menu"));
	}

	private Location buildFacingLocation(Npc npc) {
		Location location = npc.getLocation().clone();
		Location target = location.getWorld() != null ? location.getWorld().getSpawnLocation() : location.clone();
		Vector direction = target.toVector().subtract(location.toVector());
		direction.setY(0);
		if (direction.lengthSquared() > 0.0001D) {
			location.setDirection(direction);
		}
		location.setPitch(0f);
		return location;
	}

	private void applyHardFreezeState(LivingEntity entity) {
		UtilEnt.Vegetate(entity);
		UtilEnt.silence(entity, true);
		UtilEnt.ghost(entity, true, false);
		entity.setAI(false);
		entity.setCollidable(false);
		entity.setCanPickupItems(false);

		if (entity instanceof org.bukkit.entity.Creature) {
			((org.bukkit.entity.Creature) entity).setTarget(null);
		}

		if (entity instanceof org.bukkit.entity.Mob) {
			((org.bukkit.entity.Mob) entity).setAware(false);
		}
	}

	private boolean needsHardFreezeCorrection(LivingEntity entity, Location desiredLocation) {
		if (!entity.getWorld().equals(desiredLocation.getWorld())) {
			return true;
		}

		if (entity.getLocation().distanceSquared(desiredLocation) > 0.1D) {
			return true;
		}

		return yawDifference(entity.getLocation().getYaw(), desiredLocation.getYaw()) > 2.5f
				|| Math.abs(entity.getLocation().getPitch() - desiredLocation.getPitch()) > 2.5f;
	}

	private float yawDifference(float first, float second) {
		float delta = Math.abs(first - second) % 360.0f;
		return delta > 180.0f ? 360.0f - delta : delta;
	}

	private String buildNpcRechargeKey(Npc npc, String suffix) {
		return npc.getIdentityKey() + " " + suffix;
	}
}
