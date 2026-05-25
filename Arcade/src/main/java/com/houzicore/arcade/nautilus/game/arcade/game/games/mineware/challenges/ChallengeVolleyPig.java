package com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.challenges;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.IdUtil;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.Challenge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;

public class ChallengeVolleyPig extends Challenge
{
	private long _redSide;
	private long _blueSide;
	private Entity _pig;

	public ChallengeVolleyPig(MineWare host)
	{
		super(host, ChallengeType.FirstComplete, "Keep the pig on the other side and stack up the time!");
	}

	@Override
	public ArrayList<Location> getSpawns()
	{
		ArrayList<Location> locations = new ArrayList<Location>();

		for (int x = -5; x <= 5; x++)
		{
			for (int z = -9; z <= 9; z++)
			{
				if (z == 0)
					continue;

				locations.add(getCenter().add(x + 0.5, 1.1, z + 0.5));
			}
		}

		return locations;
	}

	@Override
	public void cleanupRoom()
	{
		_pig.remove();
		Host.DamagePvE = false;
	}

	@Override
	public void setupPlayers()
	{
		for (Player player : getChallengers())
		{
			player.getInventory().setItem(0, new ItemBuilder(Material.STICK).addEnchantment(Enchantment.KNOCKBACK, 1).build());
		}

		Host.DamagePvE = true;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onMove(PlayerMoveEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()))
			return;

		Location from = event.getFrom().clone();
		from.setY(0);

		Block bFrom = from.getBlock();

		if (IdUtil.getTypeId(bFrom) != 159)
			return;

		Location to = event.getTo().clone();
		to.setY(0);

		Block bTo = to.getBlock();

		if (bTo.getType() == Material.AIR || IdUtil.getData(bTo) != IdUtil.getData(bFrom))
		{
			setLost(event.getPlayer());
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (event.getEntity() != _pig || event.getCause() == DamageCause.FALL)
			return;

		// event.AddMult("Reduce damage", null, 0.001, false); // not migrated
		((Damageable) _pig).setHealth(((Damageable) _pig).getMaxHealth());
	}

	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_pig.isValid())
		{
			Location loc = _pig.getLocation();

			if (Math.abs(loc.getZ()) > 0.05)
			{

				loc.setY(0);
				Block block = loc.getBlock();

				if (block.getType() != Material.AIR)
				{
					if (loc.getZ() < 0)
					{
						_blueSide += 50;
					}
					else
					{
						_redSide += 50;
					}
				}
				else
				{
					_pig.remove();
				}
			}
		}

		displayProgress();

		if (!_pig.isValid() || UtilTime.elapsed(StartTime, 30000) || _redSide > 10000 || _blueSide > 10000)
		{
			Duration = 0; // Instant game over

			for (Player player : getChallengers())
			{
				Location loc = player.getLocation();
				loc.setY(0);
				Block block = loc.getBlock();

				if (IdUtil.getTypeId(block) == 159)
				{
					if (IdUtil.getData(block) == (_redSide > _blueSide ? 11 : 14))
					{
						SetCompleted(player);
					}
				}
			}
		}
	}

	private void displayProgress()
	{
		double red = _redSide / 10000D;
		double blue = _blueSide / 10000D;
		// Generate Bar
		int bars = 24;
		boolean redFirst = red < blue;
		String progressBar = (redFirst ? C.cRed : C.cBlue) + "";
		int colorChange = 0;

		for (int i = 0; i < bars; i++)
		{
			float d = (float) i / (float) bars;

			if (colorChange == 0 && d >= (redFirst ? red : blue))
			{
				progressBar += (redFirst ? C.cBlue : C.cRed);
				colorChange = 1;
			}

			if (colorChange != 2 && d >= Math.max(red, blue))
			{
				progressBar += C.cWhite;
				colorChange = 2;
			}

			progressBar += "▌";
		}

		UtilTextBottom.display(progressBar, UtilServer.getPlayers());
	}

	@Override
	public void generateRoom()
	{
		Host.CreatureAllowOverride = true;

		_pig = getCenter().getWorld().spawnEntity(getCenter().add(0, 1, 0), EntityType.PIG);
		UtilEnt.Vegetate(_pig);

		Host.CreatureAllowOverride = false;

		for (int x = -6; x <= 6; x++)
		{
			for (int z = -10; z <= 10; z++)
			{
				for (int y = 0; y <= 3; y++)
				{
					Block b = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0 || Math.abs(x) == 6 || Math.abs(z) == 10)
					{
						b.setType(z == 0 ? Material.WHITE_STAINED_GLASS
								: IdUtil.getMaterial(159, (byte) (z < 0 ? 11 : z > 0 ? 14 : 0)));
						addBlock(b);
					}
				}
			}
		}
	}

}
