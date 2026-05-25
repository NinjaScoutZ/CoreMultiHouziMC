package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashSet;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkConstructor extends Perk
{
	private int _max = 0;
	private double _time = 0;
	
	private Material _type;
	private String _name = "";
	
	public PerkConstructor(String perkName, double time, int max, Material mat, String name, boolean visible) 
	{
		super(perkName, new String[] 
				{
				C.cGray + "Receive 1 " + ItemStackFactory.Instance.GetName(mat, (byte)0, false) + " every " + time + " seconds. Maximum of " + max + ".",
				}, visible);
		
		_time = time;
		_max = max;
		_type = mat;
		
		if (name == null)
			_name = ItemStackFactory.Instance.GetName(mat, (byte)0, false);
		else 
			_name = name;
	}

	@EventHandler
	public void Construct(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(cur))
				continue;
			
			if (!Manager.GetGame().IsAlive(cur))
				continue;
			
			if (Manager.isSpectator(cur))
				continue;

			if (!Recharge.Instance.use(cur, GetName(), (long) (_time * 1000), false, false))
				continue;

			if (UtilInv.contains(cur, _type, (byte)0, _max))
				continue;

			//Add
			byte data = 0;
			if (_type == Material.WHITE_WOOL)
			{
				GameTeam team = Manager.GetGame().GetTeam(cur);
				if (team != null)
					data = team.GetColorData();
				
				if (UtilInv.contains(cur, _type, data, _max))
					continue;
				
				cur.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.WHITE_WOOL, team.GetColorData(), 1));
				return;
			}
	
			cur.getInventory().addItem(ItemStackFactory.Instance.CreateStack(_type, data, 1, F.item(_name)));

			cur.playSound(cur.getLocation(), Sound.ENTITY_ITEM_PICKUP, 2f, 1f);
		}
	}

	@EventHandler
	public void Drop(PlayerDropItemEvent event)
	{
		if (!UtilInv.IsItem(event.getItemDrop().getItemStack(), _type, (byte)0))
			return;

		//Cancel
		event.setCancelled(true);

		//Inform
		UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot drop " + F.item(_name) + "."));
	}

	@EventHandler
	public void DeathRemove(PlayerDeathEvent event)
	{	
		HashSet<org.bukkit.inventory.ItemStack> remove = new HashSet<org.bukkit.inventory.ItemStack>();

		for (org.bukkit.inventory.ItemStack item : event.getDrops())
			if (UtilInv.IsItem(item, _type, (byte)0))
				remove.add(item);

		for (org.bukkit.inventory.ItemStack item : remove)
			event.getDrops().remove(item);
	}

	@EventHandler
	public void InvClick(InventoryClickEvent event)
	{
		UtilInv.DisallowMovementOf(event, _name, _type, (byte)0, true);
	}
}
