package com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits.perks.PerkCraftman;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDummy;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class KitJumper extends Kit
{

	public KitJumper(ArcadeManager manager)
	{
		super(manager, "Jumper", KitAvailability.Free, 0, // EN
				new String[]
				{
				C.cGray + "Use your jumping abilities to leap away from trouble!"
				}, 
				// TH
				new String[]
				{
				C.cGray + "[TH] Use your jumping abilities to leap away from trouble!"
				}, 
				new Perk[]
						{
				new PerkDoubleJump("Double Jump", 1.2, 1.2, false, 6000, true),
				new PerkDummy("Feathered Boots", 
						new String[]
								{
						C.cGray + "You take no fall damage."
								}),
				new PerkCraftman()
						}, EntityType.ZOMBIE, new ItemBuilder(Material.IRON_AXE).build());
	}

	@Override
	public void GiveItems(Player player)
	{

	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!(event.getEntity() instanceof Player))
			return;
		
		if (!HasKit(((Player) event.getEntity())))
			return;
			
		if (event.getCause() == DamageCause.FALL)
		{
			event.setCancelled(true);
		}
	}
}
