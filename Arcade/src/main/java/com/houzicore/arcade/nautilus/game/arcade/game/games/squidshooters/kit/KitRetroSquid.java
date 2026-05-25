package com.houzicore.arcade.nautilus.game.arcade.game.games.squidshooters.kit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.recharge.Recharge;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class KitRetroSquid extends Kit
{

	private static final String NAME = "Squid Laser";
	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemBuilder(Material.IRON_AXE)
							.setTitle(C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + NAME)
							.setUnbreakable(true)
							.build()
			};

	public KitRetroSquid(ArcadeManager manager)
	{
		super(manager, "Retro Squid", KitAvailability.Free, new String[]
				{
						C.cYellow + "Right-Click" + C.cGray + " with axe to fire " + C.cGreen + NAME,
						"Hold " + C.cYellow + "Sneak" + C.cGray + " to swim through water."
				}, new Perk[0], EntityType.SQUID, new ItemStack(Material.IRON_AXE));
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Player)
		{
			attemptLaser(event.getPlayer(), null);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (attemptLaser(event.getPlayer(), event.getClickedBlock()))
		{
			event.setCancelled(true);
		}
	}

	private boolean attemptLaser(Player player, Block block)
	{
		ItemStack itemStack = player.getItemInHand();

		if (!HasKit(player) || itemStack == null || !itemStack.getType().name().endsWith("_AXE") || UtilBlock.usable(block) || !Recharge.Instance.use(player, NAME, 1200, false, true))
		{
			return false;
		}

		Location location = player.getEyeLocation();
		location.add(location.getDirection());
		location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);

		for (int step = 0; step < 30; step++)
		{
			location.add(location.getDirection().multiply(0.3));
			UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, location, 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());

			Player closest = UtilPlayer.getClosest(location, player);

			if (closest == null || closest.getLocation().distanceSquared(location) > 4)
			{
				continue;
			}

			Location from = closest.getLocation();

			UtilPlayer.getInRadius(from, 2).forEach((hit, scale) ->
			{
				if (player.equals(hit))
				{
					return;
				}

				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
				UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, closest.getLocation().add(0, 1, 0), 0.1F, 0.1F, 0.1F, 0, 10, ViewDist.LONG, UtilServer.getPlayers());
				Manager.GetDamage().NewDamageEvent(hit, player, null, DamageCause.CUSTOM, 7 * scale, true, true, true, player.getName(), NAME);
			});

			break;
		}

		return true;
	}
}

