package com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.challenges;

import java.util.ArrayList;
import java.util.HashMap;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.Challenge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class ChallengeHitTargets extends Challenge
{
	private HashMap<String, ArrayList<String>> _targets = new HashMap<String, ArrayList<String>>();
	private int _targetsEach;

	public ChallengeHitTargets(MineWare host)
	{
		super(host, ChallengeType.FirstComplete, "Hit the chosen players");
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player)) return;
		Player p = ((Player) event.getDamager());

		if (p == null || UtilPlayer.isSpectator(p))
			return;

		if (((Player) event.getEntity()) == null)
			return;

		if (!_targets.containsKey(p.getName()))
			return;

		event.setCancelled(true);

		String name = ((Player) event.getEntity()).getName();

		ArrayList<String> targets = _targets.get(p.getName());

		if (!targets.remove(name))
		{
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

			UtilPlayer.message(p, C.cYellow + "They are not your target! " + getMessage(p));
			return;
		}

		// displayCount(p, event.getEntity().getEyeLocation().add(0, 0.3, 0), (targets.isEmpty() ? C.cDGreen // Entity→LivingEntity cast needed
		//		: targets.size() == 1 ? C.cRed : C.cDRed) + (_targetsEach - targets.size()));

		if (targets.isEmpty())
		{
			SetCompleted(p);
		}
	}

	@Override
	public String getMessage(Player player)
	{
		return C.cYellow + "Hit the players " + C.cWhite
				+ StringUtils.join(_targets.get(player.getName()), C.cYellow + ", " + C.cWhite);
	}

	@Override
	public int getMinPlayers()
	{
		return 4;
	}

	@Override
	public ArrayList<Location> getSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (int x = -8; x <= 8; x++)
		{
			for (int z = -8; z <= 8; z++)
			{
				if (x % 2 == 0 && z % 2 == 0)
				{
					spawns.add(getCenter().clone().add(x + 0.5, 1.1, z + 0.5));
				}
			}
		}

		return spawns;
	}

	@Override
	public void cleanupRoom()
	{
		Host.DamagePvP = false;
	}

	@Override
	public void setupPlayers()
	{
		Host.DamagePvP = true;
		ArrayList<Player> players = getChallengers();

		for (Player player : players)
		{
			ArrayList<String> names = new ArrayList<String>();

			for (Player p : players)
			{
				if (p != player)
				{
					names.add(p.getName());
				}
			}

			while (names.size() > 3)
			{
				names.remove(UtilMath.r(names.size()));
			}

			_targetsEach = names.size();

			_targets.put(player.getName(), names);
		}
	}

	@Override
	public void generateRoom()
	{
		for (int x = -12; x <= 12; x++)
		{
			for (int z = -12; z <= 12; z++)
			{
				Block b = getCenter().clone().add(x, 0, z).getBlock();
				b.setType(Material.STONE_BRICKS);
				addBlock(b);

				if (Math.abs(x) > 1 && Math.abs(x) < 8 && Math.abs(z) > 1 && Math.abs(z) < 8)
				{
					for (int y = 1; y < 3; y++)
					{
						Block block = b.getRelative(0, y, 0);
						block.setType(Material.STONE_BRICKS);
						// block.setData((byte) (UtilMath.r(8) < 7 ? 0 : UtilMath.r(2) + 1)); // Block.setData removed in 1.21
						addBlock(block);
					}
				}
			}
		}
	}

}
