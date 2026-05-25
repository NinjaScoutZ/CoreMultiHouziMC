package com.houzicore.lobby.hub.tutorial;

import com.houzicore.shared.common.util.UtilAlg;

import org.bukkit.Location;

public class TutorialPhase 
{
	public Location Location;
	public String HeaderEN;
	public String HeaderTH;
	public String[] TextEN;
	public String[] TextTH;

	public TutorialPhase(Location player, Location target, String header, String[] text) 
	{
		this(player, target, header, header, text, text);
	}

	public TutorialPhase(Location player, Location target, String headerEN, String headerTH, String[] textEN, String[] textTH) 
	{
		Location = player;
		
		Location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(player, target)));
		Location.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(player, target)));
		
		HeaderEN = headerEN;
		HeaderTH = headerTH;
		TextEN = textEN;
		TextTH = textTH;
	}

	public String getHeader(org.bukkit.entity.Player player) {
		return com.houzicore.shared.core.lang.LangManager.get().isThai(player) && HeaderTH != null ? HeaderTH : HeaderEN;
	}

	public String[] getText(org.bukkit.entity.Player player) {
		return com.houzicore.shared.core.lang.LangManager.get().isThai(player) && TextTH != null ? TextTH : TextEN;
	}
}
