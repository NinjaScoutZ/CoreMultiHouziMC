package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.RuneManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.lang.PrimalGamesLang;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AirdropManager implements Listener
{
	private PrimalGames _game;
	private RuneManager _runeManager;
	private long _nextDrop = 300000; // 5 minutes in ms
	private HashSet<FallingBlock> _drops = new HashSet<FallingBlock>();
	private Random _rand = new Random();
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

	public AirdropManager(PrimalGames game, RuneManager runeManager)
	{
		_game = game;
		_runeManager = runeManager;
	}

	@EventHandler
	public void onStateChange(com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent event)
	{
		if (event.GetGame() != _game) return;
		if (event.GetState() == com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState.Prepare)
		{
			_nextDrop = 300000;
			
			for (FallingBlock fb : _drops)
			{
				if (fb.isValid()) fb.remove();
			}
			_drops.clear();
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!_game.IsLive()) return;
		if (event.getType() != UpdateType.SEC) return;

		long elapsed = System.currentTimeMillis() - _game.GetStateTime();
		if (elapsed >= _nextDrop)
		{
			_nextDrop += 300000; // next in 5 mins
			spawnAirdrop();
		}
	}

	@EventHandler
	public void onFireworks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC) return;
		if (_drops.isEmpty()) return;

		Iterator<FallingBlock> it = _drops.iterator();
		while (it.hasNext())
		{
			FallingBlock fb = it.next();
			if (!fb.isValid() || fb.isDead())
			{
				it.remove();
				continue;
			}

			// Play firework trailing
			FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(Color.RED).with(FireworkEffect.Type.BALL_LARGE).build();
			UtilFirework.playFirework(fb.getLocation(), effect);
		}
	}

	private void spawnAirdrop()
	{
		if (_game.GetSpectatorLocation() == null) return;
		Location center = _game.GetSpectatorLocation().clone();
		
		int xOffset = _rand.nextInt(100) - 50;
		int zOffset = _rand.nextInt(100) - 50;
		
		center.add(xOffset, 0, zOffset);
		center.setY(150);

		FallingBlock drop = center.getWorld().spawnFallingBlock(center, Material.CHEST.createBlockData());
		drop.setDropItem(false);
		drop.setHurtEntities(false);

		_drops.add(drop);

		PrimalGamesLang lang = PrimalGamesLang.get();
		for (Player p : _game.GetPlayers(true))
		{
			p.sendMessage(lang.get(p, "primal_games.announce.airdrop_location",
					"x", String.valueOf(center.getBlockX()),
					"z", String.valueOf(center.getBlockZ())));
			com.houzicore.shared.common.actionbar.ActionBarService.display(p, com.houzicore.shared.common.actionbar.ActionBarChannel.GAME_STATUS, LEGACY.deserialize(C.cYellow + C.Bold + "\uD83D\uDCE6 Airdrop X: " + center.getBlockX() + " Z: " + center.getBlockZ()));
			p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
		}
	}

	public long getNextDrop()
	{
		return _nextDrop;
	}

	@EventHandler
	public void onLand(EntityChangeBlockEvent event)
	{
		if (event.getEntity() instanceof FallingBlock)
		{
			FallingBlock fb = (FallingBlock) event.getEntity();
			if (_drops.contains(fb))
			{
				_drops.remove(fb);

				org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(_game.Manager.getPlugin(), new Runnable()
				{
					@Override
					public void run()
					{
						if (event.getBlock().getType() == Material.CHEST)
						{
							Chest chest = (Chest) event.getBlock().getState();
							
							_game.fillAirdropCrate(chest);
							
							if (_runeManager != null)
							{
								RuneManager.RuneType[] runes = RuneManager.RuneType.values();
								RuneManager.RuneType chosen = runes[_rand.nextInt(runes.length)];
								chest.getInventory().addItem(_runeManager.getRuneItem(chosen));
							}

							chest.getWorld().playSound(chest.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f);
						}
					}
				}, 2L);
			}
		}
	}
}
