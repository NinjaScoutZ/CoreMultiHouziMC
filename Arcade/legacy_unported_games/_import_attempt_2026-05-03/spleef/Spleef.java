package com.houzicore.arcade.nautilus.game.arcade.game.games.spleef;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.SoloGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.spleef.kits.KitArcher;
import com.houzicore.arcade.nautilus.game.arcade.game.games.spleef.kits.KitBrawler;
import com.houzicore.arcade.nautilus.game.arcade.game.games.spleef.kits.KitSnowballer;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.CompassModule;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.managers.chat.ChatStatData;
import com.houzicore.arcade.nautilus.game.arcade.stats.SpleefBlockDestroyStatTracker;

public class Spleef extends SoloGame
{

	private static final String[] DESCRIPTION =
			{
					C.cGreen + "Punch Blocks" + C.Reset + " to break them!",
					"You gain " + C.cRed + "Hunger" + C.Reset + " when breaking blocks.",
					C.cYellow + "Last Player" + C.Reset + " alive wins!"
			};

	public Spleef(ArcadeManager manager)
	{
		super(manager, GameType.Spleef, new Kit[]
				{
						new KitSnowballer(manager),
						new KitBrawler(manager),
						new KitArcher(manager)
				}, DESCRIPTION);

		DamagePvP = false;
		WorldWaterDamage = 4;
		PrepareFreeze = false;
		NightVision = true;

		registerStatTrackers(
				new SpleefBlockDestroyStatTracker(this)
		);

		registerChatStats(
				DamageTaken,
				DamageDealt,
				BlankLine,
				new ChatStatData("SpleefBlocks", "Blocks Broken", true),
				BlankLine,
				new ChatStatData("kit", "Kit", true)
		);

		new CompassModule()
				.register(this);
	}

	@EventHandler
	public void snowballHit(ProjectileHitEvent event)
	{
		if (!IsLive() || !(event.getEntity() instanceof Snowball))
		{
			return;
		}

		Snowball ball = (Snowball) event.getEntity();

		if (ball.getShooter() == null || !(ball.getShooter() instanceof Player))
		{
			return;
		}

		Location location = ball.getLocation().add(ball.getVelocity().multiply(0.8));
		Block block = location.getBlock();

		if (block.getType() == Material.AIR)
		{
			Block closest = null;
			double closestDist = Double.MAX_VALUE;

			for (Block other : UtilBlock.getSurrounding(block, true))
			{
				if (other.getType() == Material.AIR)
				{
					continue;
				}

				double dist = UtilMath.offsetSquared(location, other.getLocation().add(0.5, 0.5, 0.5));

				if (closest == null || dist < closestDist)
				{
					closest = other;
					closestDist = dist;
				}
			}

			if (closest != null)
			{
				block = closest;
			}
		}

		blockFade(block, (Player) ball.getShooter(), false);
	}

	@EventHandler
	public void arrowHit(ProjectileHitEvent event)
	{
		if (!IsLive() || !(event.getEntity() instanceof Arrow))
		{
			return;
		}

		Arrow arrow = (Arrow) event.getEntity();
		double velocity = arrow.getVelocity().length();

		if (!(arrow.getShooter() instanceof Player))
		{
			return;
		}

		Player player = (Player) arrow.getShooter();

		Manager.runSyncLater(() ->
		{
			Block block = UtilEnt.getHitBlock(arrow);

			double radius = 0.5 + velocity / 1.6d;

			blockFade(block, player, false);

			for (Block other : UtilBlock.getInRadius(block.getLocation().add(0.5, 0.5, 0.5), radius).keySet())
			{
				blockFade(other, player, true);
			}

			arrow.remove();
		}, 0);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockHit(BlockDamageEvent event)
	{
		Player player = event.getPlayer();

		if (!IsLive() || UtilPlayer.isSpectator(player))
		{
			return;
		}

		Block block = event.getBlock();
		event.setCancelled(true);

		if (block.getType() == Material.BEDROCK)
		{
			return;
		}

		blockFade(block, player, false);

		//Snowball
		if (GetKit(player) instanceof KitSnowballer && !UtilInv.contains(player, Material.SNOW_BALL, (byte) 0, 16))
		{
			player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.SNOW_BALL));
		}
	}

	private void blockFade(Block block, Player player, boolean slowDamage)
	{
		if (block.getType() == Material.BEDROCK || block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
		{
			return;
		}

		//Prevent Super Hunger from Bow
		if (Recharge.Instance.use(player, GetName() + " Hunger", 50, false, false))
		{
			UtilPlayer.hunger(player, 1);
		}

		if (!slowDamage)
		{
			breakBlock(block, player);
			return;
		}

		//Wool and Stained Clay
		if (block.getType() == Material.WOOL || block.getType() == Material.STAINED_CLAY)
		{
			//Greens
			if (block.getData() == 5 || block.getData() == 13)
			{
				block.setData((byte) 4);
			}
			//Yellow
			else if (block.getData() == 4)
			{
				block.setData((byte) 14);
			}
			else
			{
				breakBlock(block, player);
			}
		}
		//Stone
		else if (block.getType() == Material.STONE)
		{
			block.setTypeId(4);
		}
		//Stone Brick
		else if (block.getType() == Material.SMOOTH_BRICK)
		{
			if (block.getData() == 0 || block.getData() == 1)
			{
				block.setData((byte) 2);
			}
			else
			{
				breakBlock(block, player);
			}
		}
		//Grass
		else if (block.getType() == Material.GRASS)
		{
			block.setTypeId(3);
		}
		//Wood Planks
		else if (block.getType() == Material.WOOD)
		{
			if (block.getData() == 1)
			{
				block.setData((byte) 0);
			}
			else if (block.getData() == 0)
			{
				block.setData((byte) 2);
			}
			else
			{
				breakBlock(block, player);
			}
		}
		//Other
		else if (block.getTypeId() != 7)
		{
			breakBlock(block, player);
		}
	}

	private void breakBlock(Block block, Player player)
	{
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
		MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
		UtilServer.CallEvent(new SpleefDestroyBlockEvent(block, player));
	}

	@EventHandler
	public void updateHunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || !IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (player.getFoodLevel() <= 0)
			{
				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.STARVATION, 1, false, true, false, GetName(), "Starvation");
				player.sendMessage(F.main("Game", C.cRedB + "Break blocks to restore hunger!"));
			}

			UtilPlayer.hunger(player, -2);
		}
	}
}

