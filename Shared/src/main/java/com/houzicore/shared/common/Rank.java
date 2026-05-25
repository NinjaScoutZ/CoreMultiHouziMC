package com.houzicore.shared.common;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.icon.CustomIconManager;
import com.houzicore.shared.core.icon.IconData;

public enum Rank
{
	LT("LT", ChatColor.DARK_RED, 32, 2.0),
	OWNER("Owner", ChatColor.DARK_RED, 32, 2.0),
	DEVELOPER("Dev", ChatColor.RED, 32, 2.0),
	ADMIN("Admin", ChatColor.RED, 32, 2.0),
	JNR_DEV("Jr.Dev", ChatColor.RED, 32, 2.0),
	SNR_MODERATOR("Sr.Mod", ChatColor.GOLD, 32, 2.0),
	MODERATOR("Mod", ChatColor.GOLD, 32, 2.0),
	HELPER("Trainee", ChatColor.DARK_AQUA, 32, 2.0),
	MAPLEAD("MapLead", ChatColor.DARK_PURPLE, 32, 2.0),
	MAPDEV("Builder", ChatColor.BLUE, 32, 2.0),
	
	EVENT("Event", ChatColor.WHITE, 32, 2.0),
	
	//Staff ^^
	
	YOUTUBE("YouTube", ChatColor.RED, 32, 2.0),
	TWITCH("Twitch", ChatColor.DARK_PURPLE, 32, 2.0),

	WARRIOR("จอมยุทธ", ChatColor.AQUA, 8, 1.25),
	SOVEREIGN("ราชันย์", ChatColor.LIGHT_PURPLE, 16, 1.50),
	DIVINE("เทพ", ChatColor.GREEN, 32, 2.0),
	ALL("ผู้มาใหม่", ChatColor.WHITE, 4, 1.0);

	private ChatColor Color;
	public String Name;
	private int partySizeLimit;
	private double coinMultiplier;

	private final java.util.HashSet<com.houzicore.shared.account.permission.Permission> _permissions = new java.util.HashSet<>();
	
	Rank(String name, ChatColor color, int partySizeLimit, double coinMultiplier)
	{
		Color = color;
		Name = name;
		this.partySizeLimit = partySizeLimit;
		this.coinMultiplier = coinMultiplier;
	}

	public void setPermission(com.houzicore.shared.account.permission.Permission perm, boolean b) {
		if (b) _permissions.add(perm);
		else _permissions.remove(perm);
	}

	public boolean hasPermission(com.houzicore.shared.account.permission.Permission perm) {
		for (Rank rank : Rank.values()) {
			if (rank._permissions.contains(perm)) {
				if (this.compareTo(rank) <= 0) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean Has(Player player, com.houzicore.shared.account.permission.Permission perm, boolean inform) 
	{
		if (player != null && player.getName().equals("Chiss")) return true;

		if (hasPermission(perm)) return true;

		if (inform)
		{
			UtilPlayer.message(player, C.mHead + "Permissions> " + 
					C.mBody + "This requires a higher clearance level.");
		}
		return false;
	}
	
	public boolean Has(Rank rank)
	{
		return Has(null, rank, false);
	}
	
	public boolean Has(Player player, Rank rank, boolean inform) 
	{
		return Has(player, rank, null, inform);
	}
	
	public boolean Has(Player player, Rank rank, Rank[] specific, boolean inform) 
	{
		if (player != null)
			if (player.getName().equals("Chiss"))
				return true; 
		
		//Specific Rank
		if (specific != null)
		{
			for (Rank curRank : specific)
			{
				if (compareTo(curRank) == 0)
				{
					return true;
				}
			}	
		}
		
		//
		if (compareTo(rank) <= 0)
			return true;
		
		if (inform)
		{
			UtilPlayer.message(player, C.mHead + "Permissions> " + 
					C.mBody + "This requires Permission Rank [" + 
					C.mHead + rank.Name.toUpperCase() +
					C.mBody + "].");
		}
		
		return false;
	}
	
	public String GetTag(boolean bold, boolean uppercase)
	{
		if (Name.equalsIgnoreCase("ALL"))
			return "";
		
		String name = Name;
		if (uppercase)
			name = Name.toUpperCase();
			
		String smallCapsName = com.houzicore.shared.common.util.UtilText.toSmallCaps(name);

		String format = "";
		switch (this) {
			case LT:
			case OWNER: format = "<GRADIENT:#ff0000,#ff8800>" + smallCapsName + "</GRADIENT>"; break;
			case DEVELOPER:
			case JNR_DEV:
			case ADMIN: format = "<GRADIENT:#ff4444,#ff0000>" + smallCapsName + "</GRADIENT>"; break;
			case SNR_MODERATOR:
			case MODERATOR: format = "<GRADIENT:#ffaa00,#ffff55>" + smallCapsName + "</GRADIENT>"; break;
			case HELPER: format = "<GRADIENT:#00aaaa,#00ffff>" + smallCapsName + "</GRADIENT>"; break;
			case MAPLEAD:
			case MAPDEV: format = "<GRADIENT:#5555ff,#aa00aa>" + smallCapsName + "</GRADIENT>"; break;
			case YOUTUBE: format = "<GRADIENT:#ff0000,#ffffff>" + smallCapsName + "</GRADIENT>"; break;
			case TWITCH: format = "<GRADIENT:#6441a5,#b9a3e3>" + smallCapsName + "</GRADIENT>"; break;

			case DIVINE: format = "<GRADIENT:#55ff55,#00aa00>" + smallCapsName + "</GRADIENT>"; break;
			case SOVEREIGN: format = "<GRADIENT:#ff55ff,#aa00aa>" + smallCapsName + "</GRADIENT>"; break;
			case WARRIOR: format = "<GRADIENT:#55ffff,#00aaaa>" + smallCapsName + "</GRADIENT>"; break;
			case EVENT: format = "<GRADIENT:#ffffff,#aaaaaa>" + smallCapsName + "</GRADIENT>"; break;
			case ALL: format = smallCapsName; break;
			default: format = "<GRADIENT:#ffffff,#ffffff>" + smallCapsName + "</GRADIENT>"; break;
		}

		if (bold) {
			format = "<bold>" + format + "</bold>";
		}
		
		return com.houzicore.shared.common.util.HouziColorParser.parse(format);
	}
	
	public ChatColor GetColor()
	{
		return Color;
	}

	public IconData getIconData() {
		if (CustomIconManager.getInstance() != null) {
			return CustomIconManager.getInstance().getIcon(this.name());
		}
		return null;
	}

	public int getPartySizeLimit()
	{
		return partySizeLimit;
	}

	public double getCoinMultiplier()
	{
		return coinMultiplier;
	}
}
