package com.houzicore.lobby.hub.ui.profile;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.friend.FriendManager;
import com.houzicore.shared.core.friend.FriendStatusType;
import com.houzicore.shared.core.friend.data.FriendStatus;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.core.plugin.PluginRegistry;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.stats.PlayerStats;
import com.houzicore.lobby.hub.HubManager;

public class ProfilePage extends ShopPageBase<HubManager, ProfileShop> 
{
	public ProfilePage(HubManager plugin, ProfileShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player) 
	{
		super(plugin, shop, clientManager, donationManager, com.houzicore.shared.common.util.UtilText.toSmallCaps(com.houzicore.shared.core.lang.LangManager.get().get(player, "gui.profile.title")), player, 54);
		buildPage();
	}

	@Override
	protected void buildPage() 
	{
		boolean thai = com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer());
		String glassName = "§bEssence: §a" + getDonationManager().Get(getPlayer().getName()).GetEssence();
		for (int i = 0; i < 54; i++) 
		{
			setItem(i, new ShopItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, glassName, new String[]{}, 1, false));
		}

		CoreClient client = getClientManager().Get(getPlayer());
		PlayerStats stats = getPlugin().GetStats().Get(getPlayer());
		long gamesPlayed = stats.getStat("Global.GamesPlayed");
		long totalWins = stats.getStat("Global.Wins");
		long totalKills = stats.getStat("Global.Kills");
		long timeInGame = stats.getStat("Global.TimeInGame") / 3600;
		long essenceEarned = stats.getStat("Global.GemsEarned");
		long totalXp = stats.getStat("Global.XP");
		int level = getPlugin().getLevelManager() != null ? getPlugin().getLevelManager().getLevel(getPlayer()) : 1;
		int progress = getPlugin().getLevelManager() != null ? Math.round(getPlugin().getLevelManager().getProgress(getPlayer()) * 100f) : 0;
		int onlineFriends = 0;
		int totalFriends = 0;
		FriendManager friendManager = null;
		try
		{
			friendManager = PluginRegistry.require(FriendManager.class);
		}
		catch (Exception ignored)
		{
		}
		if (friendManager != null && friendManager.Get(getPlayer()) != null)
		{
			for (FriendStatus friend : friendManager.Get(getPlayer()).getFriends())
			{
				if (friend.Status != FriendStatusType.Accepted)
					continue;

				totalFriends++;
				if (friend.Online)
					onlineFriends++;
			}
		}
		Party party = getPlugin().getPartyManager().getPartyByPlayer(getPlayer());
		String partyStatus = party == null
				? (thai ? "Solo" : "Solo")
				: (thai ? "อยู่ปาร์ตี้ " + party.GetPlayersOnline().size() + " คน" : "In party with " + party.GetPlayersOnline().size() + " players");

		ArrayList<String> heroLore = new ArrayList<>();
		heroLore.add(" ");
		heroLore.add(C.cGray + (thai ? "ภาพรวมบัญชีของคุณ" : "Your account overview"));
		heroLore.add(C.cDGray + C.Strike + "──────────────────────");
		heroLore.add(F.value(thai ? "ชื่อ" : "Name", getPlayer().getName()));
		heroLore.add(F.value(thai ? "แรงค์" : "Rank", client.GetRank().GetTag(true, true)));
		heroLore.add(F.value(thai ? "เลเวล" : "Level", "" + level));
		heroLore.add(F.value(thai ? "ความคืบหน้า" : "Progress", progress + "%"));
		heroLore.add(C.cDGray + C.Strike + "──────────────────────");
		setItem(13, new ShopItem(Material.PLAYER_HEAD, C.cGold + C.Bold + (thai ? "โปรไฟล์หลัก" : "Profile Summary"), heroLore.toArray(new String[0]), 1, false));

		ArrayList<String> generalLore = sectionLore(
				(thai ? "ประวัติการเล่นโดยรวม" : "Your overall play history"),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.general.games_played"), gamesPlayed),
				line(thai ? "ชนะทั้งหมด" : "Total Wins", totalWins),
				line(thai ? "คิลทั้งหมด" : "Total Kills", totalKills),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.general.time_in_game"), timeInGame + (thai ? " ชม." : "h")),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.general.essence_earned"), essenceEarned));
		setItem(11, new ShopItem(Material.COMPASS, C.cYellow + C.Bold + (thai ? "ภาพรวม" : "Overview"), generalLore.toArray(new String[0]), 1, false));

		ArrayList<String> progressionLore = sectionLore(
				(thai ? "ความคืบหน้า " + com.houzicore.shared.core.common.BrandConfig.mainServerName() + " ของคุณ" : "Your " + com.houzicore.shared.core.common.BrandConfig.mainServerName() + " progression"),
				line(thai ? "เลเวลปัจจุบัน" : "Current Level", level),
				line(thai ? "XP ทั้งหมด" : "Total XP", totalXp),
				line(thai ? "ความคืบหน้าในเลเวล" : "Level Progress", progress + "%"));
		setItem(15, new ShopItem(Material.EXPERIENCE_BOTTLE, C.cGreen + C.Bold + (thai ? "ความก้าวหน้า" : "Progression"), progressionLore.toArray(new String[0]), 1, false));

		long sgWins = stats.getStat("Survival Primal Game.Wins");
		long skWins = stats.getStat("Skywars.Wins");
		long masterWins = stats.getStat("Master Builders.Wins");
		long bridgeWins = stats.getStat("Bridges.Wins");
		long uhcWins = stats.getStat("UHC.Wins");
		
		ArrayList<String> arcadeLore = sectionLore(
				(thai ? "สถิติของเกมหลักในเครือ" : "Your flagship arcade results"),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.arcade.sg_wins"), sgWins),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.arcade.sk_wins"), skWins),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.arcade.master_wins"), masterWins),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.arcade.bridge_wins"), bridgeWins),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.arcade.uhc_wins"), uhcWins));
		setItem(29, new ShopItem(Material.NETHER_STAR, C.cYellow + C.Bold + (thai ? "อาเขต" : "Arcade"), arcadeLore.toArray(new String[0]), 1, false));

		ArrayList<String> accLore = sectionLore(
				(thai ? "ข้อมูลบัญชีและเศรษฐกิจ" : "Account and economy details"),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.account.rank"), client.GetRank().GetTag(true, true)),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.account.essence"), getDonationManager().Get(getPlayer().getName()).GetEssence()),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.account.coins"), getDonationManager().Get(getPlayer().getName()).getCoins()));
		setItem(31, new ShopItem(Material.LAPIS_LAZULI, C.cYellow + C.Bold + (thai ? "บัญชี" : "Account"), accLore.toArray(new String[0]), 1, false));

		ArrayList<String> socialLore = sectionLore(
				(thai ? "เครือข่ายเพื่อนและปาร์ตี้ของคุณ" : "Your social and party presence"),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.social.friends"), totalFriends),
				line(thai ? "เพื่อนออนไลน์" : "Online Friends", onlineFriends),
				line(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "profile.social.party"), partyStatus));
		setItem(33, new ShopItem(Material.PLAYER_HEAD, C.cYellow + C.Bold + (thai ? "สังคม" : "Social"), socialLore.toArray(new String[0]), 1, false));

		setItem(49, new ShopItem(Material.EMERALD, C.cGreen + C.Bold + (thai ? "พร้อมลุยแล้ว" : "Ready to Play"),
				sectionLore(
						thai ? "ดูภาพรวมเสร็จแล้ว ไปเลือกเกมหรือเปิดของตกแต่งต่อได้เลย" : "You are ready to jump back into minigames, cosmetics, or treasure.",
						line(thai ? "เซิร์ฟเวอร์" : "Server", "Lobby"),
						line(thai ? "สถานะ" : "Status", thai ? "พร้อมใช้งาน" : "Ready"))
						.toArray(new String[0]), 1, false));
	}

	private ArrayList<String> sectionLore(String subtitle, String... lines)
	{
		ArrayList<String> lore = new ArrayList<>();
		lore.add(" ");
		lore.add(C.cGray + subtitle);
		lore.add(C.cDGray + C.Strike + "──────────────────────");
		for (String line : lines)
			lore.add(line);
		lore.add(C.cDGray + C.Strike + "──────────────────────");
		return lore;
	}

	private String line(String label, Object value)
	{
		return C.cGray + label + ": " + ChatColor.RESET + C.cWhite + value;
	}
}
