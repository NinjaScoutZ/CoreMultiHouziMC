package com.houzicore.arcade.nautilus.game.arcade.managers;

import com.houzicore.shared.core.achievement.Achievement;
import com.houzicore.shared.core.achievement.AchievementData;
import com.houzicore.shared.core.achievement.AchievementLog;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.arcade.ArcadeFormat;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.shared.core.lang.LangManager;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ArcadeAchievementChatRenderer implements Listener
{
	ArcadeManager Manager;

	public ArcadeAchievementChatRenderer(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}	

	//Ensure that past achievement progress is ignored
	@EventHandler
	public void clearAchievementLog(PlayerJoinEvent event)
	{
		Manager.GetAchievement().clearLog(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void processAchievementLog(final GameStateChangeEvent event)
	{
		if (!Manager.IsRewardAchievements())
			return;

		if (event.GetState() != GameState.Dead)
			return;

		UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				for (Player player : UtilServer.getPlayers())
				{
					displayAchievementLog(player, event.GetGame(), Manager.GetAchievement().getLog(player));
				}
			}
		}, 120);
		//Delay after Gems
	}

	public void displayAchievementLog(final Player player, Game game, NautHashMap<Achievement, AchievementLog> log)
	{
		if (!Manager.IsRewardAchievements())
			return;
		
		if (log == null)
			return;
		
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);

		UtilPlayer.message(player, "");
		UtilPlayer.message(player, ArcadeFormat.Line);
		UtilPlayer.message(player, "");

		UtilPlayer.message(player, "  \u00A7d\u00A7l\u2726 " + com.houzicore.shared.common.util.UtilText.toSmallCaps("achievement progress"));
		
		int out = 0;

		//Display
		for (final Achievement type : log.keySet())
		{
			
			
			AchievementData data = Manager.GetAchievement().get(player, type);
			
			String nameLevel = F.elem(C.cGold + C.Bold + type.getLangName(player));
			if (type.getMaxLevel() > 1)
				nameLevel = F.elem(C.cGold + C.Bold + type.getLangName(player) + " " + ChatColor.RESET + C.cYellow + data.getLevel() + C.cGold +  "/" + C.cYellow + type.getMaxLevel());
			
			String progress = F.elem(C.cGreen + "+" + log.get(type).Amount);
			
			boolean displayDesc = true;
			 
			//Completed Achievement
			if (data.getLevel() >= type.getMaxLevel())
			{
				//Finishing for the first time 
				if (!Manager.GetTaskManager().hasCompletedTask(player, type.getName()))
				{
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, nameLevel + "   " + F.elem(C.cAqua + C.Bold + LangManager.get().get(player, "achievement.chat.complete", "Completed!")) +
							"   " + F.elem(C.cGreen + C.Bold + "+" + type.getGemReward() + " Essence"));
					
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
					
				}
				else
				{
					//Display nothing because already complete bro :O
					displayDesc = false;
				}
			}
			//Multi-Level Achievements
			else if (log.get(type).LevelUp)
			{
				UtilPlayer.message(player, "");
				UtilPlayer.message(player, nameLevel + "   " + progress +
						"   " + F.elem(C.cAqua + C.Bold + LangManager.get().get(player, "achievement.chat.levelup", "Leveled Up!")));
				
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
			}
			else
			{
				//Single Level 
				if (type.getMaxLevel() == 1)
				{
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, nameLevel + "   " + progress +
							"   " + F.elem(ChatColor.YELLOW + "" + (data.getExpNextLevel() - data.getExpRemainder()) + " " + LangManager.get().get(player, "achievement.chat.tocomplete", "to complete")));
				}
				else
				{
					//Multi Level - Almost Finished
					if (data.getLevel() == type.getMaxLevel() - 1)
					{
						UtilPlayer.message(player, "");
						UtilPlayer.message(player, nameLevel + "   " + progress +
								"   " + F.elem(ChatColor.YELLOW + "" + (data.getExpNextLevel() - data.getExpRemainder()) + " " + LangManager.get().get(player, "achievement.chat.tocomplete", "to complete")));
					}
					//Multi Level - Many levels to go
					else
					{ { }
						UtilPlayer.message(player, "");
						UtilPlayer.message(player, nameLevel + "   " + progress +
								"   " + F.elem(ChatColor.YELLOW + "" + (data.getExpNextLevel() - data.getExpRemainder()) + " " + LangManager.get().get(player, "achievement.chat.tonextlevel", "to next level")));
					}
				}
			}
			
			if (displayDesc)
				for (String desc : type.getLangDesc(player))
				{ 
					UtilPlayer.message(player, desc);
					out++;
				}
				
			
			out++;
		}
		
		while (out < 5)
		{
			//UtilPlayer.message(player, "");
			out++;
		}
			
		UtilPlayer.message(player, "");
		UtilPlayer.message(player, ArcadeFormat.Line);	
	}
}
