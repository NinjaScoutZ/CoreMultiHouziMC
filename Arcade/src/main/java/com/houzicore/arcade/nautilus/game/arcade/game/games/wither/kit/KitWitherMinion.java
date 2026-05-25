package com.houzicore.arcade.nautilus.game.arcade.game.games.wither.kit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

public class KitWitherMinion extends Kit
{
	public KitWitherMinion(ArcadeManager manager)
	{
		super(manager, "Wither", KitAvailability.Free,

		new String[]
		{
			""
		},

		new Perk[]
		{
				new PerkWitherArrows(), new PerkWitherAttack(),
				new PerkWitherMinion(), new PerkWitherCompassScent()
		}, EntityType.WITHER, null);

	}

	// @Override
	// public void SpawnCustom(LivingEntity ent)
	// {
	// ent.setMaxHealth(300);
	// ent.setHealth(300);
	//
	// DisguiseWither disguise = new DisguiseWither(ent);
	// disguise.SetName(C.cYellow + "Wither");
	// disguise.SetCustomNameVisible(true);
	// Manager.GetDisguise().getService().apply(player, request);
	// }

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(
				ItemStackFactory.Instance.CreateStack(Material.GOLDEN_SWORD,
						(byte) 0, 1, C.cYellow + C.Bold + "Left-Click"
								+ C.cWhite + C.Bold + " - " + C.cGreen + C.Bold
								+ "Wither Skull"));

		player.getInventory().addItem(
				ItemStackFactory.Instance.CreateStack(Material.DIAMOND_SWORD,
						(byte) 0, 1, C.cYellow + C.Bold + "Right-Click"
								+ C.cWhite + C.Bold + " - " + C.cGreen + C.Bold
								+ "Skeletal Minions"));

		player.getInventory().addItem(
				ItemStackFactory.Instance
						.CreateStack(Material.COMPASS, (byte) 0, 1, C.cYellow
								+ C.Bold + "Human Finder X-9000"));

		// Disguise
		com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			"WITHER",
			true,
			false,
			false
		);

		if (Manager.GetGame().GetTeam(player) != null) {
					// + player.getName());

		Manager.GetDisguise().getService().apply(player, request);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void witherDamageCancel(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player player = ((Player) event.getEntity());
		if (player == null)
			return;

		if (HasKit(player))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void witherMeleeCancel(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player player = ((Player) event.getDamager());
		if (player == null)
			return;

		if (!HasKit(player))
			return;

		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void witherFlight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!HasKit(player))
				continue;

			if (player.isFlying())
				continue;

			player.setAllowFlight(true);
			player.setFlying(true);
		}
	}
}
