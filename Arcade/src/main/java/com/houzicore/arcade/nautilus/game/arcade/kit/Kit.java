package com.houzicore.arcade.nautilus.game.arcade.kit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.achievement.Achievement;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.arcade.ArcadeFormat;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerKitGiveEvent;

public abstract class Kit implements Listener
{
	public ArcadeManager Manager;

	protected String _kitName;
	protected String _languageKey;
	protected String[] _kitDesc;
	protected String[] _kitDescTh;
	protected KitAvailability _kitAvailability;
	protected int _cost;
	
	protected Perk[] _kitPerks;
	
	protected EntityType _entityType;
	protected ItemStack _itemInHand;
	
	protected Material _displayItem;
	
	protected Achievement[] _achivementCategory;
	
	protected ChatColor _displayColor;

	public Kit(ArcadeManager manager, com.houzicore.shared.core.game.kit.GameKit gameKit, Perk[] kitPerks)
	{
		this(manager, gameKit.getDisplayName(), KitAvailability.Free, gameKit.getCost(), gameKit.getDescription(), kitPerks, EntityType.PLAYER, null);
	}

	public Kit(ArcadeManager manager, String name, KitAvailability kitAvailability, String[] kitDesc, Perk[] kitPerks, EntityType entityType, ItemStack itemInHand)
	{
		this(manager, name, kitAvailability, 2000, kitDesc, kitDesc, kitPerks, entityType, itemInHand);
	}

	public Kit(ArcadeManager manager, String name, KitAvailability kitAvailability, int cost, String[] kitDesc, Perk[] kitPerks, EntityType entityType, ItemStack itemInHand)
	{
		this(manager, name, kitAvailability, cost, kitDesc, kitDesc, kitPerks, entityType, itemInHand);
	}

	public Kit(ArcadeManager manager, String name, KitAvailability kitAvailability, String[] kitDescEn, String[] kitDescTh, Perk[] kitPerks, EntityType entityType, ItemStack itemInHand)
	{
		this(manager, name, kitAvailability, 2000, kitDescEn, kitDescTh, kitPerks, entityType, itemInHand);
	}

	public Kit(ArcadeManager manager, String name, KitAvailability kitAvailability, int cost, String[] kitDescEn, String[] kitDescTh, Perk[] kitPerks, EntityType entityType, ItemStack itemInHand)
	{
		Manager = manager;

		_kitName = name;
		_kitDesc = kitDescEn;
		_kitDescTh = kitDescTh;
		_kitPerks = kitPerks;

		for (Perk perk : _kitPerks)
			perk.SetHost(this);
		
		_kitAvailability = kitAvailability;
		_cost = cost;
		
		_entityType = entityType;
		_itemInHand = itemInHand;
		
		_displayItem = Material.BOOK;
		if (itemInHand != null)
			_displayItem = itemInHand.getType();
	}

	public String GetFormattedName()
	{
		ChatColor color = _displayColor != null ? _displayColor : GetAvailability().GetColor();
		return color + "§l" + _kitName;
	}

	public String GetFormattedName(Player player)
	{
		ChatColor color = _displayColor != null ? _displayColor : GetAvailability().GetColor();
		String name = _kitName;
		if (_languageKey != null) {
			try {
				com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang lang = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.ensureInitialized(Manager.getPlugin());
				if (lang != null) {
					String key = "prop_rush.kit." + _languageKey + ".name";
					name = lang.getString(player, key, _kitName);
				}
			} catch(Throwable t) {}
		}
		return color + "§l" + name;
	}
	
	public void setDisplayColor(ChatColor color)
	{
		_displayColor = color;
	}
	
	public ChatColor getDisplayColor()
	{
		return _displayColor;
	}
	
	public String GetName()
	{	
		return _kitName;
	}

	public String GetName(Player player)
	{	
		if (_languageKey != null) {
			try {
				com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang lang = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.ensureInitialized(Manager.getPlugin());
				if (lang != null) {
					String key = "prop_rush.kit." + _languageKey + ".name";
					return lang.getString(player, key, _kitName);
				}
			} catch(Throwable t) {}
		}
		return _kitName;
	}

	public void setLanguageKey(String key)
	{
		_languageKey = key;
	}

	public String getLanguageKey()
	{
		return _languageKey;
	}

	public ItemStack GetItemInHand()
	{
		return _itemInHand;
	}
	
	public KitAvailability GetAvailability()
	{
		return _kitAvailability;
	}
	
	public String[] GetDesc()
	{
		return _kitDesc;
	}

	public String[] GetDesc(org.bukkit.entity.Player player)
	{
		if (_languageKey != null) {
			try {
				com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang lang = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.ensureInitialized(Manager.getPlugin());
				if (lang != null) {
					String key = "prop_rush.kit." + _languageKey + ".desc";
					String[] translation = lang.getStringList(player, key, _kitDesc);
					if (translation != null && translation.length > 0) return translation;
				}
			} catch(Throwable t) {}
		}

		if (com.houzicore.shared.core.lang.LangManager.get() != null
				&& com.houzicore.shared.core.lang.LangManager.get().isThai(player)
				&& _kitDescTh != null)
		{
			return _kitDescTh;
		}
		return _kitDesc;
	}
	
	public Perk[] GetPerks()
	{
		return _kitPerks;
	}

	public boolean HasKit(Player player)
	{
		if (Manager.GetGame() == null)
			return false;

		return Manager.GetGame().HasKit(player, this);
	}	
	
	public void ApplyKit(final Player player)
	{
		// Eject from vehicles (boats, minecarts) to prevent inventory desync
		if (player.isInsideVehicle())
			player.leaveVehicle();
		player.eject();
		
		UtilInv.Clear(player);
		
		for (Perk perk : _kitPerks)
			perk.Apply(player);
		
		UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				if (!player.isOnline()) return;
				
				GiveItemsCall(player);
				UtilInv.Update(player);
				
				// Update tablist with Kit Name
				org.bukkit.ChatColor teamColor = null;
				if (Manager.GetGame() != null)
				{
					com.houzicore.arcade.nautilus.game.arcade.game.GameTeam team = Manager.GetGame().GetTeam(player);
					if (team != null)
						teamColor = team.GetColor();
				}
				String gameName = "Waiting...";
				String mapName = "Unknown";
				if (Manager.GetGame() != null)
				{
					gameName = Manager.GetGame().GetName();
					if (Manager.GetGame().WorldData != null && Manager.GetGame().WorldData.MapName != null) {
						mapName = Manager.GetGame().WorldData.MapName;
					}
				}
				com.houzicore.shared.TablistFix.setSuffix(player, net.kyori.adventure.text.Component.text(" [" + GetName() + "]", net.kyori.adventure.text.format.NamedTextColor.GRAY));
				com.houzicore.shared.TablistFix.updateTablist(player, Manager.GetClients(), teamColor, null, gameName, mapName);
			}
		}, 1L);
	}
	
	public void GiveItemsCall(Player player)
	{
		GiveItems(player);
		
		//Event
		PlayerKitGiveEvent kitEvent = new PlayerKitGiveEvent(Manager.GetGame(), this, player);
		UtilServer.getServer().getPluginManager().callEvent(kitEvent);
	}
	
	public abstract void GiveItems(Player player);
	
	public Entity SpawnEntity(Location loc)
	{
		EntityType type = _entityType;
		if (type == EntityType.PLAYER)
			type = EntityType.ZOMBIE;
			
		
		if (Manager.GetGame() != null)
			Manager.GetGame().CreatureAllowOverride = true;
			
		LivingEntity entity = (LivingEntity) Manager.GetCreature().SpawnEntity(loc, type);

		if (Manager.GetGame() != null)
			Manager.GetGame().CreatureAllowOverride = false;

		entity.setRemoveWhenFarAway(false);
		entity.setCustomNameVisible(false);
		entity.getEquipment().setItemInHand(_itemInHand);
		entity.setInvulnerable(true);
		entity.setGravity(false);
		
		if ((GetName().contains("Wither") || GetName().contains("Alpha")) && type == EntityType.SKELETON)
		{
			// In 1.21+, Wither Skeletons are EntityType.WITHER_SKELETON — re-spawn as the correct type
		entity.remove();
		type = EntityType.WITHER_SKELETON;
		Manager.GetGame().CreatureAllowOverride = true;
		entity = (LivingEntity) Manager.GetCreature().SpawnEntity(loc, type);
		Manager.GetGame().CreatureAllowOverride = false;
		}

		UtilEnt.Vegetate(entity);
		UtilEnt.silence(entity, true);
		UtilEnt.ghost(entity, true, false);
		entity.addScoreboardTag("ArcadeLobbyNPC");

		SpawnCustom(entity); 

		return entity;
	}

	public void SpawnCustom(LivingEntity ent) { }

	public void DisplayDesc(Player player) 
	{
		for (int i=0 ; i<3 ; i++)
			UtilPlayer.message(player, "");
		
		UtilPlayer.message(player, ArcadeFormat.Line);

		UtilPlayer.message(player, "§aKit - §f§l" + GetName(player));
		
		//Desc
		for (String line : GetDesc(player))
		{
			UtilPlayer.message(player, C.cGray + "  " + line);
		}

		//Perk Descs
		for (Perk perk : GetPerks())
		{
			if (!perk.IsVisible())
				continue;
			
			UtilPlayer.message(player, "");
			UtilPlayer.message(player, C.cWhite + C.Bold + perk.GetName(player));
			for (String line : perk.GetDesc(player))
			{
				UtilPlayer.message(player, C.cGray + "  " + line);
			}
		}
		
		UtilPlayer.message(player, ArcadeFormat.Line);

	}

	public int GetCost() 
	{
		return _cost;
	}

	public Material getDisplayMaterial()
	{
		return _displayItem;
	}

	public void Deselected(Player player) { }
	
	public void Selected(Player player) { }

	public void setEntityType(EntityType entityType)
	{
		_entityType = entityType;
	}
	
	public void setAchievementRequirements(Achievement[] category)
	{
		_achivementCategory = category;
	}
	
	public Achievement[] getAchievementRequirement()
	{
		return _achivementCategory;
	}
	
	public boolean hasKitsUnlocked(Player player)
	{
		return Manager.hasKitsUnlocked(player);
	}
}
