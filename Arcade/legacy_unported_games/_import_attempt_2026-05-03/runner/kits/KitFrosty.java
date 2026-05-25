package com.houzicore.arcade.nautilus.game.arcade.game.games.runner.kits;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;

import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkConstructor;

public class KitFrosty extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkConstructor("Frost Balls", 0.5, 16, Material.SNOW_BALL, "Snowball", true)
			};

	public KitFrosty(ArcadeManager manager)
	{
		super(manager, GameKit.RUNNER_FROSTY, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}

	@EventHandler
	public void SnowballHit(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null || !(event.GetProjectile() instanceof Snowball))
		{
			return;
		}

		event.SetKnockback(false);
		Manager.GetCondition().Factory().Slow("Snowball Slow", event.GetDamageeEntity(), (LivingEntity) event.GetProjectile().getShooter(), 2, 2, false, false, true, false);
	}
}

