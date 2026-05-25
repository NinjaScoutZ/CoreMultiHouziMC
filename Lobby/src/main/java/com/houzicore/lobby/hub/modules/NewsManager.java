package com.houzicore.lobby.hub.modules;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.gadget.gadgets.MorphWither;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.mount.Mount;
import com.houzicore.shared.core.mount.types.MountDragon;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;

public class NewsManager extends MiniPlugin
{
	public HubManager Manager;
	
	public NewsManager(HubManager manager)
	{
		super("News Manager", manager.getPlugin());
		Manager = manager;
	}
	
	@EventHandler
	public void DragonBarUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

        String text = com.houzicore.shared.core.announce.AnnounceManager.getInstance().getCurrentScrollText(null);
        if (text == null) {
            text = com.houzicore.shared.core.common.BrandConfig.mainServerName().toUpperCase();
        }

		for (Creature pet : Manager.getPetManager().getPets())
		{
		    if (pet instanceof Wither)
		    {
		        pet.setCustomName(text);
		    }
		}
		
		for (Mount mount : Manager.GetMount().getMounts())
		{
			if (mount instanceof MountDragon)
			{
				((MountDragon)mount).SetName(text);
			}
		}
		
		for (Gadget gadget : Manager.GetGadget().getGadgets(GadgetType.Morph))
		{
			if (gadget instanceof MorphWither)
			{
				((MorphWither)gadget).setWitherData(text, 1.0);
			}
		}
	}
}
