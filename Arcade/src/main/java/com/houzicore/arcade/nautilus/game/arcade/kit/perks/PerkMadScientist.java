package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.Arrays;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.disguise.disguises.DisguiseBase;
import com.houzicore.shared.core.disguise.disguises.DisguiseBlaze;
import com.houzicore.shared.core.disguise.disguises.DisguiseCow;
import com.houzicore.shared.core.disguise.disguises.DisguiseInsentient;
import com.houzicore.shared.core.disguise.disguises.DisguisePig;
import com.houzicore.shared.core.disguise.disguises.DisguisePigZombie;
import com.houzicore.shared.core.disguise.disguises.DisguiseSkeleton;
import com.houzicore.shared.core.disguise.disguises.DisguiseSlime;
import com.houzicore.shared.core.disguise.disguises.DisguiseSpider;
import com.houzicore.shared.core.disguise.disguises.DisguiseZombie;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PerkMadScientist extends Perk implements IThrown
{

	private NautHashMap<String, Creature> _activeKitHolders = new NautHashMap<String, Creature>();
	
	public PerkMadScientist(ArcadeManager manager)
	{
		super("Mad Scientist", new String[]
		{
				"Recieve 1 Egg every 90 seconds! (Max 3)",
				"Eggs spawn a loyal minion to fight for you",
		});
	}

	@EventHandler
	public void eggSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (Manager.GetGame() == null)
			return;
		
		if (!UtilTime.elapsed(Manager.GetGame().GetStateTime(), 60000))
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;

			if (!Manager.GetGame().IsAlive(player))
				continue;

			if(UtilInv.contains(player, "", Material.ZOMBIE_SPAWN_EGG, (byte) 0, 3)) {
				continue;
			}
			
			if (!Recharge.Instance.use(player, "Egg Spawn", 90000, false, false))
			{
				continue;
			}
			
			else
			{
				player.getInventory().addItem(
						ItemStackFactory.Instance.CreateStack(
								Material.ZOMBIE_SPAWN_EGG, (byte) 0, 1, C.cYellow
										+ C.Bold + "Click To Throw",
								Arrays.asList("")));
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 2f,
						1f);
			}
		}
	}

	@EventHandler
	public void throwEgg(PlayerInteractEvent e)
	{

		Player player = e.getPlayer();

		if (player.getItemInHand().getType() != Material.ZOMBIE_SPAWN_EGG)
		{
			return;
		}

		if (!Kit.HasKit(player))
			return;

		e.setCancelled(true);

		org.bukkit.entity.Item ent = player.getWorld().dropItem(
				player.getEyeLocation(),
				ItemStackFactory.Instance.CreateStack(player.getItemInHand()
						.getType()));
		UtilAction.velocity(ent, player.getLocation().getDirection(), 1.5,
				false, 0, 0.2, 10, false);

// Manager.getDamager().AddThrow(ent, player, this, -1, true, true,
// true, false, 0.6f);

		UtilInv.remove(player, Material.ZOMBIE_SPAWN_EGG, (byte) 0, 1);

	}

	@EventHandler
	public void onTargetzombie(EntityTargetLivingEntityEvent e)
	{

		if (!(e.getEntity() instanceof Zombie))
		{
			return;
		}

		if (!(e.getTarget() instanceof Player))
		{
			return;
		}

		Zombie zombie = (Zombie) e.getEntity();
		Player targetPlayer = (Player) e.getTarget();

		String name = ChatColor
				.stripColor(zombie.getCustomName().split("'")[0]);

		if (targetPlayer.getName().equalsIgnoreCase(name))
		{
			e.setCancelled(true);
		}

	}


	@EventHandler
	public void onOwnerDeath(PlayerDeathEvent e)
	{

		String playerName = e.getEntity().getName();

		if (_activeKitHolders.containsKey(playerName))
		{
			Creature zombie = _activeKitHolders.get(playerName);
			zombie.remove();
			_activeKitHolders.remove(playerName);
		}

	}
	
	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{

		if (target != null)
		{
			Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null,
					DamageCause.CUSTOM, 1.5D, true, false, false,
					UtilEnt.getName(data.GetThrower()), GetName());

		}

		spawnMobs(data);
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		spawnMobs(data);

	}

	@Override
	public void Expire(ProjectileUser data)
	{

		spawnMobs(data);
	}

	public void spawnMobs(ProjectileUser data)
	{
		data.GetThrown()
				.getWorld()
				.playSound(data.GetThrown().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f,
						1.6f);

		UtilParticle.PlayParticle(ParticleType.EXPLODE, data.GetThrown()
				.getLocation(), 0F, 0F, 0F, 1F, 5, ViewDist.SHORT, UtilServer
				.getPlayers());

		data.GetThrown().remove();

		Manager.GetGame().CreatureAllowOverride = true;
		
		Zombie zombie = (Zombie) data.GetThrown().getWorld()
				.spawn(data.GetThrown().getLocation(), Zombie.class);
		zombie.setRemoveWhenFarAway(false);
		zombie.setMaxHealth(10.0D);
		zombie.setHealth(10.0D);
		UtilEnt.silence(zombie, true);
		
		//Name
		zombie.setCustomName(C.cAqua + UtilEnt.getName(data.GetThrower()) + "'s Minion");
		zombie.setCustomNameVisible(true);
		
		Manager.GetGame().CreatureAllowOverride = false;

	}

}
