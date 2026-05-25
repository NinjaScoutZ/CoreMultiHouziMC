package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.kits;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.core.game.kit.GameKit;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitBedwarsArcher extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkFletcher(6, 3, true)
			};

	private final Set<Entity> _piercingArrows;

	public KitBedwarsArcher(ArcadeManager manager)
	{
		super(manager, GameKit.BED_WARS_ARCHER, PERKS);

		_piercingArrows = new HashSet<>();
	}

	@Override
	public void GiveItems(Player player)
	{
	}

	@EventHandler
	public void bowShoot(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (!HasKit(player) || event.getForce() != 1)
		{
			return;
		}

		_piercingArrows.add(event.getProjectile());
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		if (!_piercingArrows.remove(event.getEntity()))
		{
			return;
		}

		Manager.runSyncLater(() ->
		{
			Block block = event.getHitBlock();

			if (block == null || !block.getType().name().contains("WOOL") || !((Bedwars) Manager.GetGame()).getBedwarsPlayerModule().getPlacedBlocks().contains(block))
			{
				return;
			}

			block.breakNaturally();
		}, 0);
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player killer = event.getEntity().getKiller();

		if (killer == null || !HasKit(killer))
		{
			return;
		}

		for (Perk perk : GetPerks())
		{
			if (perk instanceof PerkFletcher)
			{
				killer.getInventory().addItem(((PerkFletcher) perk).getItem(1));
				return;
			}
		}
	}
}
