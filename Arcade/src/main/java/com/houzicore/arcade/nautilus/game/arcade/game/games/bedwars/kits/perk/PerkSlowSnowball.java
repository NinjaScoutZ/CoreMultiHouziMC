package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits.perk;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkSlowSnowball extends Perk
{

	private static final ItemStack SNOW_BALL = new ItemBuilder(Material.SNOWBALL)
			.setTitle(C.cPurple + C.Bold + "Frosting Balls")
			.build();
	private static final int MAX = 3;

	public PerkSlowSnowball()
	{
		super("Frosting", new String[] { "Snowballs slow enemies" });
	}

	@EventHandler
	public void updateGain(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !Manager.GetGame().IsLive())
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player) || !Kit.HasKit(player) || UtilInv.contains(player, SNOW_BALL.getType(), (byte) -1, MAX) || !Recharge.Instance.use(player, "Snowball Give", 6000, false, false))
			{
				continue;
			}

			player.getInventory().addItem(SNOW_BALL);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void snowballDamage(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);
		Projectile projectile = event.GetProjectile();

		if (damagee == null || damager == null || !Kit.HasKit(damager) || !(projectile instanceof Snowball))
		{
			return;
		}

		Manager.GetCondition().Factory().Slow(GetName(), damagee, damager, 2.5, 1, false, true, false, false);
		event.AddMod(damager.getName(), GetName(), 1, true);
		event.AddKnockback(GetName(), 0.5);
	}

	@EventHandler
	public void disallowMovement(InventoryClickEvent event)
	{
		UtilInv.DisallowMovementOf(event, null, SNOW_BALL.getType(), (byte) -1, false);
	}

	@EventHandler
	public void disallowDrop(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (!Kit.HasKit(player) || event.getItemDrop().getItemStack().getType() != SNOW_BALL.getType())
		{
			return;
		}

		event.setCancelled(true);
	}
}
