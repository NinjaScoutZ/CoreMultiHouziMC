package com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spells;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.Spell;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClickBlock;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;

public class SpellSummonWolves extends Spell implements SpellClick, SpellClickBlock
{

	private HashMap<Wolf, Long> _summonedWolves = new HashMap<Wolf, Long>();

	@Override
	public void castSpell(Player player, Block block)
	{
		block = block.getRelative(BlockFace.UP);

		if (!UtilBlock.airFoliage(block))
		{
			block = player.getLocation().getBlock();
		}

		Location loc = block.getLocation().add(0.5, 0, 0.5);

		for (int i = 0; i < getSpellLevel(player); i++)
		{
			Wizards.CreatureAllowOverride = true;

			Wolf wolf = (Wolf) player.getWorld().spawnEntity(
					loc.clone().add(new Random().nextFloat() - 0.5F, 0, new Random().nextFloat() - 0.5F), EntityType.WOLF);

			Wizards.CreatureAllowOverride = false;

			wolf.setCollarColor(DyeColor.YELLOW);
			wolf.setTamed(true);
			wolf.setOwner(player);
			wolf.setBreed(false);
			wolf.setCustomName(player.getDisplayName() + "'s Wolf");
			wolf.setRemoveWhenFarAway(false);
			wolf.setMaxHealth(0.5);
			wolf.setHealth(0.5);

			_summonedWolves.put(wolf, System.currentTimeMillis() + (30L * 1000L));
		}

		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, loc, 0.8F, 0, 0.8F, 0, 4,
				ViewDist.LONG, UtilServer.getPlayers());
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.2F, 1);
		charge(player);
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Wolf)
		{
			Wolf wolf = (Wolf) event.getDamager();
			// event.AddMult("Summoned Wolf", "Summoned Wolf", 0.3, true); // not migrated

			AnimalTamer tamer = wolf.getOwner();
			if (tamer instanceof Player)
			{
				// event.SetDamager((Player) tamer);
				// event.setKnockbackOrigin(wolf.getLocation());
			}
		}
	}

	@EventHandler
	public void onSecond(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{

			Iterator<Wolf> itel = _summonedWolves.keySet().iterator();

			while (itel.hasNext())
			{
				Wolf wolf = itel.next();
				AnimalTamer wolfOwner = wolf.getOwner();

				if (!wolf.isValid() || _summonedWolves.get(wolf) < System.currentTimeMillis() || !(wolfOwner instanceof Player)
						|| !Wizards.IsAlive((Entity) wolfOwner))
				{
					if (wolf.isValid())
					{
						// wolf.getWorld().playEffect(wolf.getLocation(), Effect.EXPLOSION_HUGE, 0); // not migrated
					}

					wolf.remove();
					itel.remove();
				}
				else
				{

					if (wolf.getTarget() == null || !wolf.getTarget().isValid() || !Wizards.IsAlive(wolf.getTarget())
							|| wolf.getTarget().getLocation().distance(wolf.getLocation()) > 16)
					{

						double dist = 0;
						Player target = null;

						for (Player player : Wizards.GetPlayers(true))
						{

							if (!player.equals(wolfOwner))
							{

								double newDist = player.getLocation().distance(wolf.getLocation());

								if (newDist < 16 && (target == null || dist > newDist))
								{
									dist = newDist;
									target = player;
								}
							}
						}

						if (target != null)
						{
							wolf.setTarget(target);
						}
						else
						{
							Location loc = ((Player) wolfOwner).getLocation();

							if (loc.distance(wolf.getLocation()) > 16)
							{
								wolf.teleport(loc);
							}
						}

					}
				}
			}

		}
	}

	@Override
	public void castSpell(Player player)
	{
		castSpell(player, player.getLocation().getBlock().getRelative(BlockFace.DOWN));
	}
}
