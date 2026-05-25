package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.lang.PrimalGamesLang;

public class WanderingTraderManager implements Listener
{
	private PrimalGames _game;
	private RuneManager _runeManager;
	private List<org.bukkit.entity.LivingEntity> _traders = new ArrayList<org.bukkit.entity.LivingEntity>();
	private Random _rand = new Random();

	public WanderingTraderManager(PrimalGames game, RuneManager runeManager)
	{
		_game = game;
		_runeManager = runeManager;
	}

	@EventHandler
	public void onStart(GameStateChangeEvent event)
	{
		if (event.GetGame() != _game) return;
		
		if (event.GetState() == GameState.Prepare)
		{
			// Clean up old
			for (org.bukkit.entity.LivingEntity v : _traders)
			{
				if (v.isValid()) v.remove();
			}
			_traders.clear();
		}
		else if (event.GetState() == GameState.Live)
		{
			// Spawn 4 traders at random locations near center
			Location center = _game.GetSpectatorLocation() != null ? _game.GetSpectatorLocation() : _game.WorldData.World.getSpawnLocation();
			
			for (int i = 0; i < 4; i++)
			{
				Location loc = center.clone().add(_rand.nextInt(160) - 80, 0, _rand.nextInt(160) - 80);
				loc.setY(loc.getWorld().getHighestBlockYAt(loc) + 1);

				org.bukkit.entity.WanderingTrader trader = (org.bukkit.entity.WanderingTrader) loc.getWorld().spawnEntity(loc, EntityType.WANDERING_TRADER);
				trader.setCustomName(PrimalGamesLang.get().get(null, "primal_games.trader.name"));
				trader.setCustomNameVisible(true);
				trader.setAI(false);
				trader.setInvulnerable(true);

				_traders.add(trader);
			}
		}
	}

	@EventHandler
	public void onTraderInteract(PlayerInteractEntityEvent event)
	{
		if (!_game.IsLive()) return;
		if (event.getRightClicked() instanceof org.bukkit.entity.WanderingTrader)
		{
			org.bukkit.entity.WanderingTrader v = (org.bukkit.entity.WanderingTrader) event.getRightClicked();
			if (_traders.contains(v))
			{
				event.setCancelled(true);
				openTraderMenu(event.getPlayer());
			}
		}
	}

	@EventHandler
	public void onTraderDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof org.bukkit.entity.WanderingTrader)
		{
			if (_traders.contains((org.bukkit.entity.WanderingTrader) event.getEntity()))
			{
				event.setCancelled(true);
			}
		}
	}

	private void openTraderMenu(Player player)
	{
		Merchant merchant = Bukkit.createMerchant(PrimalGamesLang.get().get(player, "primal_games.trader.menu_title"));
		List<MerchantRecipe> recipes = new ArrayList<MerchantRecipe>();

		// Trade 1: 4 Gold Ingot -> 1 Random Rune
		RuneManager.RuneType[] runes = RuneManager.RuneType.values();
		RuneManager.RuneType chosen = runes[_rand.nextInt(runes.length)];
		ItemStack runeItem = _runeManager.getRuneItem(chosen);

		MerchantRecipe recipe1 = new MerchantRecipe(runeItem, 999);
		recipe1.addIngredient(new ItemStack(Material.GOLD_INGOT, 4));
		recipes.add(recipe1);

		// Trade 2: 2 Gold Ingot -> 1 Bandage
		ItemStack bandage = _game.buildBandageItem(player);

		MerchantRecipe recipe2 = new MerchantRecipe(bandage, 999);
		recipe2.addIngredient(new ItemStack(Material.GOLD_INGOT, 2));
		recipes.add(recipe2);

		// Trade 3: 8 Gold Ingot -> 1 Diamond Sword
		MerchantRecipe recipe3 = new MerchantRecipe(new ItemStack(Material.DIAMOND_SWORD, 1), 999);
		recipe3.addIngredient(new ItemStack(Material.GOLD_INGOT, 8));
		recipes.add(recipe3);

		merchant.setRecipes(recipes);
		player.openMerchant(merchant, true);
	}
}
