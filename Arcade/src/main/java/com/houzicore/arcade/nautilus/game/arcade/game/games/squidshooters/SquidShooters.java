package com.houzicore.arcade.nautilus.game.arcade.game.games.squidshooters;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.core.disguise.disguises.DisguiseSquid;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.combat.CombatComponent;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.SoloGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.squidshooters.kit.KitRetroSquid;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class SquidShooters extends SoloGame
{

	private static final int KILLS_TO_WIN = 20;
	private static final String[] DESCRIPTION =
			{
					"Hold " + C.cYellow + "Sneak" + C.cWhite + " to swim.",
					"Stay in the Water.",
					"You cannot swim when hit.",
					"First squid to " + C.cYellow + KILLS_TO_WIN + C.cWhite + " kills wins."
			};

	private final Map<Player, Integer> _kills = new HashMap<>();
	private final Set<Player> _inAir = new HashSet<>();

	public SquidShooters(ArcadeManager manager)
	{
		super(manager, GameType.SquidShooter, new Kit[]
				{
						new KitRetroSquid(manager),
				}, DESCRIPTION);

		PrepareFreeze = false;
		DamageTeamSelf = true;
		DeathOut = false;
		WorldTimeSet = 6000;
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		Scoreboard.Reset();
		Scoreboard.WriteBlank();
		Scoreboard.Write(C.cYellow + C.Bold + "Kills");
		_kills.keySet().stream()
				.sorted(Comparator.comparing(player -> _kills.getOrDefault(player, 0)).reversed())
				.limit(10)
				.forEach(player -> Scoreboard.Write(_kills.getOrDefault(player, 0) + " " + C.cGreen + player.getName()));
		Scoreboard.Draw();
	}

	@EventHandler
	public void playerTeleportIn(PlayerPrepareTeleportEvent event)
	{
		Player player = event.GetPlayer();

		DisguiseSquid disguise = new DisguiseSquid(player);
		Manager.GetDisguise().disguise(disguise);
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int line = 0;

			@Override
			public void run()
			{
				UtilTextMiddle.display(null, DESCRIPTION[line], 10, 50, 10, UtilServer.getPlayers());

				if (++line == DESCRIPTION.length)
				{
					cancel();
				}
			}
		}, 20, 50);
	}

	@EventHandler
	public void updateMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (!player.isSneaking() || player.hasPotionEffect(PotionEffectType.SLOWNESS))
			{
				continue;
			}

			Vector direction = player.getLocation().getDirection();
			boolean inAirPast = _inAir.contains(player);
			boolean inAirNow = !UtilEnt.inWater(player);

			// Entering water
			if (inAirPast && !inAirNow)
			{
				_inAir.remove(player);
			}
			// Leaving water
			else if (!inAirPast && inAirNow)
			{
				_inAir.add(player);
			}
			// Already in air
			else if (inAirNow)
			{
				continue;
			}
			// Already in water
			else
			{
				direction.multiply(0.5);
			}

			// If players are sneaking on the edge of a block and looking down (positive pitch).
			// They won't have downwards velocity applied to them. So for one tick we give them a
			// slight vertical velocity to fix this.
			if (direction.getY() < 0 && UtilEnt.onBlock(player))
			{
				direction.setY(0.01);
			}

			UtilAction.velocity(player, direction, 1, false, 0, 0, 1.2, false);
		}
	}

	@EventHandler
	public void updateHunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (UtilEnt.inWater(player))
			{
				UtilPlayer.hunger(player, 2);
			}
			else
			{
				UtilPlayer.hunger(player, -2);

				if (player.getFoodLevel() == 0)
				{
					Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.SUFFOCATION, 2, false, true, true, GetName(), "Suffocation");
				}
			}

			if (!player.hasPotionEffect(PotionEffectType.WATER_BREATHING))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerDeath(CombatDeathEvent event)
	{
		CombatComponent killer = event.GetLog().GetKiller();

		if (killer == null)
		{
			return;
		}

		Player killerPlayer = UtilPlayer.searchExact(killer.GetName());

		if (killerPlayer == null)
		{
			return;
		}

		_kills.compute(killerPlayer, (k, v) -> _kills.getOrDefault(k, 0) + 1);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_kills.remove(player);
		_inAir.remove(player);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (!IsLive() || event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			event.SetCancelled("Squid Melee Attack");
			return;
		}

		Player damagee = event.GetDamageePlayer();

		if (damagee != null)
		{
			damagee.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 0, false, false), true);
		}
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		List<Player> alive = GetPlayers(true);

		if (alive.isEmpty())
		{
			SetState(GameState.End);
			return;
		}

		boolean end = alive.size() == 1;

		for (int kills : _kills.values())
		{
			if (kills >= KILLS_TO_WIN)
			{
				end = true;
				break;
			}
		}

		if (!end)
		{
			return;
		}

		List<Player> places = alive.stream()
				.sorted(Comparator.comparing(player -> _kills.getOrDefault(player, 0)).reversed())
				.collect(Collectors.toList());

		places.forEach(player -> AddGems(player, 10, "Participation", false, false));

		if (!places.isEmpty())
		{
			AddGems(places.get(0), 20, "1st Place", false, false);
		}
		if (places.size() > 1)
		{
			AddGems(places.get(1), 15, "2nd Place", false, false);
		}
		if (places.size() > 2)
		{
			AddGems(places.get(2), 10, "3rd Place", false, false);
		}

		AnnounceEnd(places);
		SetState(GameState.End);
	}
}

