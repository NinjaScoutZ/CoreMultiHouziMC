package com.houzicore.shared.core.creature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.creature.command.MobCommand;
import com.houzicore.shared.core.creature.event.CreatureSpawnCustomEvent;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Creature extends MiniPlugin {
	private boolean _spawnForce = false;
	private boolean _disableCustom = false;

	public Creature(JavaPlugin plugin) {
		super("Creature", plugin);
	}

	@Override
	public void addCommands() {
		addCommand(new MobCommand(this));
	}

	public void AddEntityName(LivingEntity ent, String name) {
		if (ent == null)
			return;

		UtilEnt.GetEntityNames().put(ent, name);
	}

	@EventHandler
	public void CustomCreeperExplode(EntityExplodeEvent event) {
		if (_disableCustom)
			return;

		if (!(event.getEntity() instanceof Creeper))
			return;

		final HashMap<Player, Double> players = UtilPlayer.getInRadius(event.getEntity().getLocation(), 8d);
		for (final Player cur : players.keySet()) {
			final Vector vec = UtilAlg.getTrajectory(event.getEntity().getLocation(), cur.getLocation());
			UtilAction.velocity(cur, vec, 1 + 2 * players.get(cur), false, 0, 0.5 + 1 * players.get(cur), 2, true);
		}
	}

	@EventHandler
	public void Death(EntityDeathEvent event) {
		if (_disableCustom)
			return;

		event.setDroppedExp(0);
		final List<ItemStack> drops = event.getDrops();

		if (event.getEntityType() == EntityType.PLAYER) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 1));
		} else {
			drops.clear();
		}

		// Animals
		if (event.getEntityType() == EntityType.CHICKEN) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.CHICKEN, 1));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.FEATHER, 2 + UtilMath.r(5)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 1));
		}

		else if (event.getEntityType() == EntityType.COW) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BEEF, 1 + UtilMath.r(4)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.LEATHER, 2 + UtilMath.r(3)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 3 + UtilMath.r(4)));
		}

		if (event.getEntityType() == EntityType.MOOSHROOM) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BEEF, 1 + UtilMath.r(4)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.RED_MUSHROOM, 2 + UtilMath.r(3)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 3 + UtilMath.r(4)));
		}

		else if (event.getEntityType() == EntityType.OCELOT) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BEEF, 1 + UtilMath.r(2)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.COD, 2 + UtilMath.r(7)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 1 + UtilMath.r(2)));
		}

		else if (event.getEntityType() == EntityType.PIG) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.PORKCHOP, 1 + UtilMath.r(2)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 2 + UtilMath.r(2)));
		}

		else if (event.getEntityType() == EntityType.SHEEP) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BEEF, 1 + UtilMath.r(3)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.WHITE_WOOL, 1 + UtilMath.r(4)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 2 + UtilMath.r(3)));
		}

		else if (event.getEntityType() == EntityType.VILLAGER) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 2 + UtilMath.r(3)));
		}

		// Monsters
		else if (event.getEntityType() == EntityType.BLAZE) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BLAZE_ROD, 1));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 6 + UtilMath.r(7)));
		}

		else if (event.getEntityType() == EntityType.CAVE_SPIDER) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.COBWEB, 2 + UtilMath.r(3)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.SPIDER_EYE, 1));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 6 + UtilMath.r(7)));
		}

		else if (event.getEntityType() == EntityType.CREEPER) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.COAL, 6 + UtilMath.r(13)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 12 + UtilMath.r(13)));
		}

		else if (event.getEntityType() == EntityType.ENDERMAN) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.ENDER_PEARL, 1));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 12 + UtilMath.r(13)));
		}

		else if (event.getEntityType() == EntityType.GHAST) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.GHAST_TEAR, 1));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 36 + UtilMath.r(37)));
			for (int i = 0; i < 5 + UtilMath.r(11); i++) {
				drops.add(ItemStackFactory.Instance.CreateStack(Material.EMERALD, 1));
			}
		}

		else if (event.getEntityType() == EntityType.IRON_GOLEM) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.IRON_INGOT, 2 + UtilMath.r(3)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 12 + UtilMath.r(13)));
		}

		else if (event.getEntityType() == EntityType.MAGMA_CUBE) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.MAGMA_CREAM, 1));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 1 + UtilMath.r(2)));
		}

		else if (event.getEntityType() == EntityType.ZOMBIFIED_PIGLIN) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.COOKED_PORKCHOP, 1 + UtilMath.r(2)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.ROTTEN_FLESH, 1 + UtilMath.r(2)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 2 + UtilMath.r(2)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.ARROW, 1 + UtilMath.r(12)));
			if (UtilMath.r(100) > 90) {
				drops.add(ItemStackFactory.Instance.CreateStack(Material.GOLDEN_SWORD, 1));
			}
		}

		else if (event.getEntityType() == EntityType.SILVERFISH) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 1 + UtilMath.r(2)));
		}

		else if (event.getEntityType() == EntityType.SKELETON) {
			if (!(event.getEntity() instanceof WitherSkeleton)) {
				drops.add(ItemStackFactory.Instance.CreateStack(Material.ARROW, 4 + UtilMath.r(5)));
				drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 12 + UtilMath.r(13)));
			} else {
				drops.add(ItemStackFactory.Instance.CreateStack(Material.ARROW, 4 + UtilMath.r(10)));
				drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 12 + UtilMath.r(26)));
			}

		}

		else if (event.getEntityType() == EntityType.SLIME) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 1 + UtilMath.r(2)));
		}

		else if (event.getEntityType() == EntityType.SPIDER) {
			drops.add(ItemStackFactory.Instance.CreateStack(Material.COBWEB, 2 + UtilMath.r(3)));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.SPIDER_EYE, 1));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 6 + UtilMath.r(7)));
		}

		else if (event.getEntityType() == EntityType.ZOMBIE) {
			event.getDrops().add(ItemStackFactory.Instance.CreateStack(Material.ROTTEN_FLESH, 1));
			drops.add(ItemStackFactory.Instance.CreateStack(Material.BONE, 6 + UtilMath.r(7)));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void eggThrow(PlayerEggThrowEvent event) {
		if (_spawnForce)
			return;

		event.setHatching(false);
	}

	public void SetDisableCustomDrops(boolean var) {
		_disableCustom = var;
	}

	public void SetForce(boolean force) {
		_spawnForce = force;
	}

	@EventHandler
	public void Spawn(CreatureSpawnEvent event) {
		if (_disableCustom)
			return;

		if (event.getEntity() instanceof LivingEntity) {
			event.getEntity().setCanPickupItems(false);
		}

		if (_spawnForce)
			return;

		// Useless Laggy Squids
		if (event.getEntityType() == EntityType.SQUID) {
			event.setCancelled(true);
			return;
		}

		final CreatureSpawnCustomEvent customEvent = new CreatureSpawnCustomEvent(event.getLocation());

		_plugin.getServer().getPluginManager().callEvent(customEvent);

		if (customEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
	}

	public Entity SpawnEntity(Location location, EntityType entityType) {
		_spawnForce = true;
		final Entity entity = location.getWorld().spawnEntity(location, entityType);
		_spawnForce = false;

		return entity;
	}

	@EventHandler
	public void UpdateEntityNames(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST)
			return;

		final HashSet<Entity> remove = new HashSet<>();

		for (final Entity ent : UtilEnt.GetEntityNames().keySet())
			if (ent.isDead() || !ent.isValid()) {
				remove.add(ent);
			}

		for (final Entity ent : remove) {
			UtilEnt.GetEntityNames().remove(ent);
		}
	}
}
