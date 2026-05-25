package com.houzicore.lobby.hub.modules;

import org.bukkit.event.EventHandler;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.scoreboard.ScoreboardData;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.achievement.AchievementManager;
import com.houzicore.shared.core.level.LvlManager;

public class HubScoreboardManager extends MiniPlugin
{
	private ScoreboardManager _scoreboardManager;
	private String _serverName;
	private com.houzicore.shared.core.friend.FriendManager _friendManager;
	private AchievementManager _achievementManager;
	private CoreClientManager _clientManager;
	private LvlManager _lvlManager;
	
	public HubScoreboardManager(HubManager manager, CoreClientManager clientManager, DonationManager donationManager, com.houzicore.shared.core.friend.FriendManager friendManager, AchievementManager achievementManager)
	{
		super("Hub Scoreboard Manager", manager.getPlugin());
		
		_scoreboardManager = new ScoreboardManager(manager.getPlugin(), clientManager, donationManager);
		_friendManager = friendManager;
		_achievementManager = achievementManager;
		_clientManager = clientManager;
		_lvlManager = manager.getLevelManager();
		
		init();
	}
	
	private String getServerName()
	{
		if (_serverName == null)
			_serverName = getPlugin().getConfig().getString("serverstatus.name");
		
		return _serverName;
	}

	private void init()
	{
		ScoreboardData data = _scoreboardManager.getData("default", true);

		// ── Player Info Section ──────────────────────────────────────

		data.writeEmpty();

		// Rank
		data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
			@Override
			public java.util.ArrayList<String> GetLines(ScoreboardManager manager, org.bukkit.entity.Player player) {
				java.util.ArrayList<String> output = new java.util.ArrayList<>();
				boolean isThai = LangManager.get().isThai(player);
				com.houzicore.shared.account.CoreClient client = manager.getClients().Get(player);
				
				if (client != null && client.GetRank() != null) {
					String tag;
					if (client.GetRank() == com.houzicore.shared.common.Rank.ALL) {
						tag = "§7Player";
					} else {
						tag = "<texture:" + client.GetRank().name() + ">";
					}
					output.add(" §e👑 " + (isThai ? "ยศ" : "Rank") + ": " + tag);
				} else {
					output.add(" §e👑 " + (isThai ? "ยศ" : "Rank") + ": §7...");
				}
				return output;
			}
		});

		// Level + XP Progress Bar
		data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
			@Override
			public java.util.ArrayList<String> GetLines(ScoreboardManager manager, org.bukkit.entity.Player player) {
				java.util.ArrayList<String> output = new java.util.ArrayList<>();
				boolean isThai = LangManager.get().isThai(player);
				
				int level = _lvlManager != null ? _lvlManager.getLevel(player) : player.getLevel();
				float progress = _lvlManager != null ? _lvlManager.getProgress(player) : player.getExp();
				
				int totalBars = 10;
				int filled = Math.round(progress * totalBars);
				StringBuilder bar = new StringBuilder("§a");
				for (int i = 0; i < totalBars; i++) {
					if (i == filled) bar.append("§8");
					bar.append(i < filled ? "■" : "□");
				}
				
				output.add(" §a⬆ " + (isThai ? "เลเวล" : "Level") + ": §f" + level + " " + bar.toString());
				return output;
			}
		});

		data.writeEmpty();

		// Essence
		data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
			@Override
			public java.util.ArrayList<String> GetLines(ScoreboardManager manager, org.bukkit.entity.Player player) {
				java.util.ArrayList<String> output = new java.util.ArrayList<>();
				boolean isThai = LangManager.get().isThai(player);
				com.houzicore.shared.core.donation.Donor donation = manager.getDonation().Get(player);
				
				if (donation != null) {
					output.add(" §b💎 " + (isThai ? "เอสเซนส์" : "Essence") + ": §f" + com.houzicore.shared.core.scoreboard.ScoreboardFormatUtil.formatCurrency(donation.GetBalance(com.houzicore.shared.common.CurrencyType.Essence)));
				} else {
					output.add(" §b💎 " + (isThai ? "เอสเซนส์" : "Essence") + ": §7...");
				}
				return output;
			}
		});

		// Coins
		data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
			@Override
			public java.util.ArrayList<String> GetLines(ScoreboardManager manager, org.bukkit.entity.Player player) {
				java.util.ArrayList<String> output = new java.util.ArrayList<>();
				boolean isThai = LangManager.get().isThai(player);
				com.houzicore.shared.core.donation.Donor donation = manager.getDonation().Get(player);
				
				if (donation != null) {
					output.add(" §6🪙 " + (isThai ? "เหรียญ" : "Coins") + ": §f" + com.houzicore.shared.core.scoreboard.ScoreboardFormatUtil.formatCurrency(donation.GetBalance(com.houzicore.shared.common.CurrencyType.Coins)));
				} else {
					output.add(" §6🪙 " + (isThai ? "เหรียญ" : "Coins") + ": §7...");
				}
				return output;
			}
		});

		data.writeEmpty();

		// ── Social Section ──────────────────────────────────────────

		// Online
		data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
			@Override
			public java.util.ArrayList<String> GetLines(ScoreboardManager manager, org.bukkit.entity.Player player) {
				java.util.ArrayList<String> output = new java.util.ArrayList<>();
				boolean isThai = LangManager.get().isThai(player);
				output.add(" §f🌐 " + (isThai ? "ออนไลน์" : "Online") + ": §a" + com.houzicore.shared.common.util.UtilServer.getPlayers().length);
				return output;
			}
		});

		// Friends
		data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
			@Override
			public java.util.ArrayList<String> GetLines(ScoreboardManager manager, org.bukkit.entity.Player player) {
				java.util.ArrayList<String> output = new java.util.ArrayList<>();
				boolean isThai = LangManager.get().isThai(player);
				long onlineFriends = 0;
				if (_friendManager != null) {
					onlineFriends = _friendManager.Get(player).getFriends().stream()
						.filter(f -> f.Status == com.houzicore.shared.core.friend.FriendStatusType.Accepted && f.Online)
						.count();
				}
				output.add(" §f👥 " + (isThai ? "เพื่อนออนไลน์" : "Friends") + ": §a" + onlineFriends);
				return output;
			}
		});

		data.writeEmpty();

		data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
			@Override
			public java.util.ArrayList<String> GetLines(ScoreboardManager manager, org.bukkit.entity.Player player) {
				java.util.ArrayList<String> output = new java.util.ArrayList<>();
				boolean isThai = LangManager.get().isThai(player);
                
                String requestedGroup = com.houzicore.lobby.hub.server.ServerManager.Instance != null ? com.houzicore.lobby.hub.server.ServerManager.Instance.getPlayerRequests().get(player) : null;
                if (requestedGroup != null) {
                    output.add(org.bukkit.ChatColor.LIGHT_PURPLE + C.Bold + "⚡ " + (isThai ? "คำขอเซิร์ฟเวอร์" : "Request"));
                    
                    String statusStr = isThai ? "§eกำลังสร้างสภาพแวดล้อม..." : "§eProvisioning...";
                    java.util.HashSet<com.houzicore.lobby.hub.server.ServerInfo> servers = com.houzicore.lobby.hub.server.ServerManager.Instance.getServerKeyInfoMap().get(requestedGroup);
                    if (servers != null) {
                        for (com.houzicore.lobby.hub.server.ServerInfo info : servers) {
                            if (info.Status == com.houzicore.lobby.hub.server.ServerStatusType.STARTING) {
                                statusStr = isThai ? "§aพร้อมแล้ว!" : "§aReady!";
                                break;
                            }
                        }
                    }
                    output.add(C.cWhite + statusStr);
                } else {
                    output.add(org.bukkit.ChatColor.LIGHT_PURPLE + C.Bold + "\ud83d\udce2 " + (isThai ? "\u0e1b\u0e23\u0e30\u0e01\u0e32\u0e28" : "Announce"));
                    com.houzicore.shared.core.announce.AnnounceManager am = com.houzicore.shared.core.announce.AnnounceManager.getInstance();
                    if (am == null) {
                        output.add(C.cGray + LangManager.get().getOrDefault(player, "loading.data", isThai ? "กำลังโหลดข้อมูล..." : "Loading..."));
                    } else {
                        String text = am.getCurrentScrollText(player);
                        if (text != null && !text.isEmpty()) {
                            output.add(C.cWhite + text);
                        } else {
                            output.add(C.cGray + LangManager.get().getOrDefault(player, "announce.none", isThai ? "ไม่มีข่าวสารในขณะนี้" : "No active news"));
                        }
                    }
                }
				return output;
			}
		});

		data.writeEmpty();

		// Date + Server name
		data.addElement(new com.houzicore.shared.core.scoreboard.elements.ScoreboardElement() {
			@Override
			public java.util.ArrayList<String> GetLines(ScoreboardManager manager, org.bukkit.entity.Player player) {
				java.util.ArrayList<String> output = new java.util.ArrayList<>();
				String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
				String srv = getServerName() != null ? getServerName() : "Lobby";
				output.add(" §8" + date + " §7| §e" + srv);
				return output;
			}
		});
	}
	
	private int debugTicks = 0;

	@EventHandler
	public void drawUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FAST) {
			debugTicks++;
			if (debugTicks % 10 == 0) { // print every 5 seconds
				org.bukkit.Bukkit.getLogger().info("[SB-DEBUG] HubScoreboardManager.drawUpdate() FAST fired, count: " + debugTicks);
			}
			_scoreboardManager.draw();
		}
	}
}
