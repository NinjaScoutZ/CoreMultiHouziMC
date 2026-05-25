package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.HashMap;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.achievement.Achievement;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.combat.CombatComponent;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.ArcadeFormat;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.EssenceData;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam.PlayerState;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEssenceManager extends MiniPlugin implements Listener
{
	ArcadeManager Manager;

	boolean DoubleGem = true;

	public GameEssenceManager(ArcadeManager manager)
	{
		super("Game Essence", manager.getPlugin());
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}

	@EventHandler
	public void PlayerKillAward(CombatDeathEvent event)
	{
		if (!Manager.IsRewardEssence())
			return;

		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!(event.GetEvent().getEntity() instanceof Player))
			return;

		Player killed = (Player)event.GetEvent().getEntity();

		if (event.GetEvent().getEntity().getKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetEvent().getEntity().getKiller().getName());

			if (killer != null && !killer.equals(killed))
			{
				//Kill
				game.AddGems(killer, game.GetKillsGems(killer, killed, false), "Kills", true, true);

				//First Kill
				if (game.FirstKill)
				{
					game.AddGems(killer, 50, "First Blood", false, false); // Increased bounty to 50
					
					// [WOW] Task 26: First Blood Bounty Buffs
					killer.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 200, 1));
					killer.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, 100, 1));
					com.houzicore.shared.common.util.UtilParticle.PlayParticle(com.houzicore.shared.common.util.UtilParticle.ParticleType.FIREWORKS_SPARK, killer.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0.1f, 50, com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());

					game.FirstKill = false;

					game.Announce(F.main("Game", "\u00A7c\u2694 " + Manager.GetColor(killer) + killer.getName() + (LangManager.get().isThai(killer) ? " \u00A77\u0e40\u0e1b\u0e34\u0e14\u0e09\u0e32\u0e01 First Blood! \u00A7a\u00A7l[\u0e1a\u0e31\u0e1f SPEED & REGEN]" : " \u00A77scored First Blood! \u00A7a\u00A7l[SPEED & REGEN buff]")));
				}
			}
		}

		for (CombatComponent log : event.GetLog().GetAttackers())
		{
			if (event.GetEvent().getEntity().getKiller() != null && log.GetName().equals(event.GetEvent().getEntity().getKiller().getName()))
				continue;
			Player assist = UtilPlayer.searchExact(log.GetName());
			if (assist != null)
				game.AddGems(assist, game.GetKillsGems(assist, killed, true), "Kill Assists", true, true);
		}
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		if (!Manager.IsRewardEssence())
			return;

		Game game = Manager.GetGame();
		if (game == null)	return;

		RewardEssence(game, event.getPlayer(), true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerStateChange(PlayerStateChangeEvent event)
	{
		if (!Manager.IsRewardEssence())
			return;

		if (event.GetState() != PlayerState.OUT)
			return;

		if (event.GetGame().GetType() == GameType.WitherAssault || event.GetGame().GetType() == GameType.MineStrike)
			return;

		RewardEssence(event.GetGame(), event.GetPlayer(), false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void GameStateChange(GameStateChangeEvent event)
	{
		if (!Manager.IsRewardEssence())
			return;

		if (event.GetState() != GameState.Dead)
			return;

		for (Player player : UtilServer.getPlayers())
			RewardEssence(event.GetGame(), player, true);
	}

	public void RewardEssence(Game game, Player player, boolean give)
	{
		if (!Manager.IsRewardEssence()) return;

		HashMap<String, EssenceData> gems = game.GetEssence().remove(player);
		if (gems == null || gems.isEmpty()) return;

		java.util.Map<String, Double> extras = com.houzicore.shared.core.reward.math.MultiplierEngine.describeMultipliers(player);

		if (game.GemBoosterEnabled && game.GetGemBoostAmount() > 0) extras.put(game.GemBoosters.size() + " Coin Boosters", game.GetGemBoostAmount());
		
		if (game.GemHunterEnabled) {
			int gemFinder = Manager.GetAchievement().get(player.getName(), Achievement.GLOBAL_GEM_HUNTER).getLevel(); 
			if (gemFinder > 0) extras.put("Coin Hunter " + gemFinder, gemFinder * 0.25);
		}

		if (DoubleGem && game.GemDoubleEnabled && UtilPlayer.is1_8(player)) extras.put("Double Coin Weekend", 1.0);

		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);
		UtilPlayer.message(player, "");
		UtilPlayer.message(player, ArcadeFormat.Line);
		UtilPlayer.message(player, "");
		UtilPlayer.message(player, "  \u00A76\u00A7l\u2726 " + com.houzicore.shared.common.util.UtilText.toSmallCaps("reward summary"));
		UtilPlayer.message(player, "");

		int baseEarnedGems = 0;
		for (String type : gems.keySet()) {
			EssenceData data = gems.get(type);
			int earned = (int)data.Gems;
			if (earned <= 0) earned = 1;
			
			baseEarnedGems += earned;

			com.houzicore.shared.core.reward.pipeline.CoinReward reward = new com.houzicore.shared.core.reward.pipeline.CoinReward(type, earned, Manager.GetDonation(), Manager.GetStatsManager(), game.GetName());
			
			UtilPlayer.message(player, reward.getSummaryString(game.GemMultiplier));

			if (give) {
				reward.giveReward(player, game.GemMultiplier);
			}
		}
		
		int baseWithGameMult = (int) (baseEarnedGems * game.GemMultiplier);
		int addedFromExtras = 0;

		for (java.util.Map.Entry<String, Double> entry : extras.entrySet()) {
			double bonus = entry.getValue();
			int amountFromBonus = (int) (baseWithGameMult * bonus);
			if (amountFromBonus <= 0) continue;
			
			addedFromExtras += amountFromBonus;
			UtilPlayer.message(player, "  \u00A7a\u25B8 \u00A7f+" + amountFromBonus + " \u00A77Coins  \u00A78\u2022 \u00A7e" + entry.getKey() + " \u00A7a+" + (int)(bonus * 100) + "%");
		}

		if (give && addedFromExtras > 0) {
			com.houzicore.shared.core.reward.pipeline.CoinReward bonusReward = new com.houzicore.shared.core.reward.pipeline.CoinReward("Multipliers & Bonuses", addedFromExtras, Manager.GetDonation(), null, game.GetName());
			bonusReward.giveReward(player, 1.0);
		}

		UtilPlayer.message(player, "");
		if (give) {
			UtilPlayer.message(player, "  \u00A77Total: \u00A7e\u00A7l" + (Manager.GetDonation().Get(player.getName()).getCoins() + baseWithGameMult + addedFromExtras) + " \u00A77Coins");
		} else {
			UtilPlayer.message(player, "  \u00A77" + (LangManager.get().isThai(player) ? "\u0e40\u0e01\u0e21\u0e22\u0e31\u0e07\u0e14\u0e33\u0e40\u0e19\u0e34\u0e19\u0e2d\u0e22\u0e39\u0e48..." : "Game still in progress..."));
			UtilPlayer.message(player, "  \u00A77" + (LangManager.get().isThai(player) ? "\u0e04\u0e38\u0e13\u0e2d\u0e32\u0e08\u0e44\u0e14\u0e49\u0e23\u0e31\u0e1a \u00A7e\u00A7lCoins \u00A77\u0e40\u0e1e\u0e34\u0e48\u0e21\u0e40\u0e21\u0e37\u0e48\u0e2d\u0e08\u0e1a\u0e40\u0e01\u0e21" : "You may earn \u00A7e\u00A7lCoins \u00A77when the game ends."));
		}

		UtilPlayer.message(player, "");
		UtilPlayer.message(player, ArcadeFormat.Line);	
	}


}
