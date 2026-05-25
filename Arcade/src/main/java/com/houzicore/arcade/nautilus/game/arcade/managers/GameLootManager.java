package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardManager;
import com.houzicore.shared.core.reward.RewardRarity;
import com.houzicore.shared.core.reward.RewardType;
import com.houzicore.shared.core.reward.rewards.InventoryReward;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

public class GameLootManager implements Listener
{
	private ArcadeManager Manager;
	
	private RewardManager _rewardManager; 
	
	private HashSet<Player> _players = new HashSet<Player>();
	
	private long _startTime = 0;
	
	public GameLootManager(ArcadeManager manager, PetManager petManager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
		
		_rewardManager = new RewardManager(Manager.GetClients(), Manager.GetDonation(), Manager.getInventoryManager(), petManager,
				100, 250,
				500, 1000,
				1500, 2500,
				6000, 12000,
				false);
		
		//Chest
		_rewardManager.addReward(new InventoryReward(Manager.getInventoryManager(), "Old Chest", "Old Chest", 1, 1,
				new ItemStack(Material.CHEST), RewardRarity.COMMON, 2));
		
		_rewardManager.addReward(new InventoryReward(Manager.getInventoryManager(), "Ancient Chest", "Ancient Chest", 1, 1,
				new ItemStack(Material.CHEST), RewardRarity.UNCOMMON, 40));
		
		_rewardManager.addReward(new InventoryReward(Manager.getInventoryManager(), "Mythical Chest", "Mythical Chest", 1, 1,
				new ItemStack(Material.CHEST), RewardRarity.UNCOMMON, 1));
	}
	
	@EventHandler
	public void registerPlayers(GameStateChangeEvent event)
	{
		if (!Manager.IsRewardItems())
			return;

		if (event.GetState() != GameState.Live)
			return;
		
		_players.clear();
		
		int requirement = (int)((double)event.GetGame().Manager.GetPlayerFull() * 0.5d);
		
		event.GetGame().CanGiveLoot = (double)event.GetGame().GetPlayers(true).size() >= requirement;
		
		if (!event.GetGame().CanGiveLoot)
		{
			event.GetGame().Announce(C.Bold + "Game Loot Disabled. Requires " + requirement + " Players.", event.GetGame().PlaySoundGameStart);
			return;
		}
			
		for (Player player : event.GetGame().GetPlayers(true))
			_players.add(player);
		
		_startTime = System.currentTimeMillis();
	}
	
	@EventHandler
	public void unregisterPlayer(PlayerQuitEvent event)
	{
		_players.remove(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void giveLoot(final GameStateChangeEvent event)
	{
		if (!Manager.IsRewardItems())
			return;

		if (event.GetState() != GameState.Dead)
			return;

		UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				int rewardsGiven = 0; 
				
				for (Player player : _players)
				{
					if (giveReward(player, false))
						rewardsGiven++;
				}
				
				if (rewardsGiven == 0 && !_players.isEmpty())
				{
					giveReward(UtilAlg.Random(_players), true);
				}

				_players.clear();
			}
		}, 240);
		//Delay after Achievements
	}
	
	public boolean giveReward(Player player, boolean force)
	{
		if (!force)
		{
			double chance = Math.min(0.5, 0.1 + (System.currentTimeMillis() - _startTime)/3600000d);
			
			if (Manager.GetClients().Get(player).GetRank().Has(Rank.WARRIOR))
			{
				if (Manager.GetClients().Get(player).GetRank().Has(Rank.SOVEREIGN))
				{
					if (Manager.GetClients().Get(player).GetRank().Has(Rank.DIVINE))
					{
						chance *= 1.6;
					}
					else
					{
						chance *= 1.4;
					}
				}
				else
				{
					chance *= 1.2;
				}
			}
			
			if (Math.random() > chance)
				return false;
		}
				
		Reward reward = _rewardManager.nextReward(player, null, false, RewardType.GameLoot, true);
		
		RewardData rewardData = reward.giveReward("GameLoot", player);
		
		String outputName = reward.getRarity().getColor() + rewardData.getFriendlyName();
		
		String rarityName = "";
		if (reward.getRarity() != RewardRarity.COMMON)
			rarityName = " " + reward.getRarity().getColor() + reward.getRarity().getName();
		
		//Log
		
		//Self Display
		com.houzicore.shared.common.util.UtilTextBottom.display(com.houzicore.shared.common.actionbar.ActionBarChannel.REWARD, C.cGreen + "Game Loot » " + C.cWhite + "You found " + outputName, player);
		player.sendMessage(F.main("Loot", "You found " + rarityName + " " + outputName));
		//if (reward.getRarity() == RewardRarity.COMMON)
		//	UtilPlayer.message(player, F.main("Loot", "You found " + rarityName + outputName));
		
		Random _random = new Random();
		
		//Announce
		//if (reward.getRarity() != RewardRarity.COMMON)
		{
			com.houzicore.shared.common.util.UtilServer.broadcast(F.main("Loot", F.name(player.getName()) + " found" + rarityName + " " + outputName));
		}
		
		//Effect
		if (reward.getRarity() == RewardRarity.UNCOMMON)
		{
			FireworkEffect effect = FireworkEffect.builder().withColor(Color.fromRGB(_random.nextInt(255), _random.nextInt(255), _random.nextInt(255)))
					.withFade(Color.fromRGB(_random.nextInt(255), _random.nextInt(255), _random.nextInt(255)))
					.with(FireworkEffect.Type.STAR)
					.build();

			UtilFirework.playFirework(player.getEyeLocation(), effect);
		}
		else if (reward.getRarity() == RewardRarity.RARE)
		{
			FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.YELLOW).withFade(Color.WHITE).build();
			UtilFirework.playFirework(player.getEyeLocation(), effect);
			
			player.getWorld().playSound(player.getEyeLocation().add(0.5, 0.5, 0.5), Sound.ENTITY_WITHER_SPAWN, 5F, 1.2F);
		}
		else if (reward.getRarity() == RewardRarity.LEGENDARY)
		{
			FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.RED).withFade(Color.BLACK).build();
			UtilFirework.playFirework(player.getEyeLocation(), effect);
			
			player.getWorld().playSound(player.getEyeLocation().add(0.5, 0.5, 0.5), Sound.ENTITY_ENDER_DRAGON_DEATH, 10F, 2.0F);
		}
		
		return true;
	}
	

}
