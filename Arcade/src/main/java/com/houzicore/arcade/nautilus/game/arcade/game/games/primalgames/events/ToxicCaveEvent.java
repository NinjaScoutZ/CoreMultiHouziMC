package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.lang.PrimalGamesLang;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ToxicCaveEvent implements Listener
{
	private PrimalGames _game;
	private boolean _warned = false;
	private boolean _active = false;
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

	public ToxicCaveEvent(PrimalGames game)
	{
		_game = game;
	}

	@EventHandler
	public void onStateChange(GameStateChangeEvent event)
	{
		if (event.GetGame() != _game) return;
		if (event.GetState() == GameState.Prepare)
		{
			_warned = false;
			_active = false;
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!_game.IsLive()) return;
		if (event.getType() != UpdateType.SEC) return;

		long elapsed = System.currentTimeMillis() - _game.GetStateTime();
		PrimalGamesLang lang = PrimalGamesLang.get();

		if (!_warned && elapsed > 840000)
		{
			_warned = true;
			for (Player p : _game.GetPlayers(true))
			{
				p.sendMessage(lang.get(p, "primal_games.announce.toxic_cave_warning"));
				p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
			}
		}

		if (!_active && elapsed > 900000)
		{
			_active = true;
			for (Player p : _game.GetPlayers(true))
			{
				p.sendMessage(lang.get(p, "primal_games.announce.toxic_cave_active"));
				p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
			}
		}
	}

	@EventHandler
	public void onDamageUpdate(UpdateEvent event)
	{
		if (!_game.IsLive()) return;
		if (!_active) return;
		if (event.getType() != UpdateType.SEC) return;

		PrimalGamesLang lang = PrimalGamesLang.get();

		for (Player p : _game.GetPlayers(true))
		{
			if (!_game.IsAlive(p)) continue;

			if (p.getLocation().getY() < 50)
			{
				p.damage(1.0);
				p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0, false, false, false));
				com.houzicore.shared.common.actionbar.ActionBarService.display(p, com.houzicore.shared.common.actionbar.ActionBarChannel.GAME_STATUS, LEGACY.deserialize(lang.get(p, "primal_games.announce.toxic_cave_actionbar")));
			}
		}
	}
}
