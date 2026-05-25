package com.houzicore.lobby.hub.tutorial;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilPlayer;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class TutorialData 
{
	public Player Player;

	public TutorialPhase Phase;
	public int PhaseStep;
	
	public int TextStep;
	
	public long Sleep;
	
	public TutorialData(Player player, TutorialPhase phase)
	{
		Player = player;
		Phase = phase;
		
		TextStep = 0;
		PhaseStep = 0;
		
		Sleep = System.currentTimeMillis() + 3000;
	}

	public boolean Update() 
	{
		if (!Player.getLocation().equals(Phase.Location))
			Player.teleport(Phase.Location);
		
		if (System.currentTimeMillis() < Sleep)
			return false;
		
		//Next Phase
		if (TextStep >= Phase.getText(Player).length)
		{
			PhaseStep++;
			Sleep = System.currentTimeMillis() + 2000;
			
			return true;
		}
		
		//Display Text
		String text = Phase.getText(Player)[TextStep];
		
		UtilPlayer.message(Player, " ");
		UtilPlayer.message(Player, " ");
		UtilPlayer.message(Player, " ");
		UtilPlayer.message(Player, "\u00A78\u00A7m                                                    ");
		UtilPlayer.message(Player, "\u00A76\u00A7l\u2726 \u00A7e\u00A7l" + Phase.getHeader(Player));
		UtilPlayer.message(Player, " ");
		
		for (int i=0 ; i<=TextStep ; i++)
			UtilPlayer.message(Player, "  " + Phase.getText(Player)[i]);
		
		for (int i=TextStep ; i<=5 ; i++)
			UtilPlayer.message(Player, " ");
		
		UtilPlayer.message(Player, "\u00A78\u00A7m                                                    ");
		
		if (text.length() > 0)
		{
			Player.playSound(Player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1.5f);
			Sleep = System.currentTimeMillis() + 1000 + (50*text.length());
		}
		else
		{
			Sleep = System.currentTimeMillis() + 600;
		}
			
		TextStep++;
			
		return false;
	}
	
	public void SetNextPhase(TutorialPhase phase)
	{
		Phase = phase;
		TextStep = 0;
		Player.teleport(Phase.Location);
	}
}
