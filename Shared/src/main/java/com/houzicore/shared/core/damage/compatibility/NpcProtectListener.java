package com.houzicore.shared.core.damage.compatibility;

import com.houzicore.shared.core.npc.NpcManager;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NpcProtectListener implements Listener
{
	private NpcManager _npcManager;
	
	public NpcProtectListener(NpcManager npcManager)
	{
		_npcManager = npcManager;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void CustomDamage(CustomDamageEvent event)
	{
    	if (_npcManager != null && event.GetDamageeEntity() != null && _npcManager.getNpcByEntity(event.GetDamageeEntity()) != null)
    		event.SetCancelled("NPC");
	}
}
