package com.houzicore.shared.core.achievement;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.command.StatsCommand;
import com.houzicore.shared.core.achievement.ui.AchievementMenu;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.shared.core.stats.event.StatChangeEvent;
import com.houzicore.shared.achievement.AchievementDefinition;
import com.houzicore.shared.achievement.AchievementRegistry;
import com.houzicore.shared.achievement.AchievementTier;
import com.houzicore.shared.achievement.AchievementType;
import com.houzicore.shared.achievement.GameCategory;

public class AchievementManager extends MiniPlugin {
	private final StatsManager _statsManager;

	private final AchievementMenu _shop;
	private final int _interfaceSlot = 7;
	private boolean _giveInterfaceItem = false;

	private final NautHashMap<String, NautHashMap<Achievement, AchievementLog>> _log = new NautHashMap<>();

	private boolean _shopEnabled = true;

	private final DonationManager _donationManager;

	public AchievementManager(StatsManager statsManager, CoreClientManager clientManager,
			DonationManager donationManager) {
		super("Achievement Manager", statsManager.getPlugin());

		_statsManager = statsManager;
		_donationManager = donationManager;
		_shop = new AchievementMenu(this, _statsManager, clientManager, donationManager, "Achievement");

		registerAchievements();
	}

	private void registerAchievements() {
		// Global
		AchievementRegistry.register(AchievementDefinition.builder()
				.id("Global.FirstLogin")
				.nameEN("First Login").nameTH("การเข้าสู่ระบบครั้งแรก")
				.descEN("Login to the server for the first time").descTH("เข้าสู่เซิร์ฟเวอร์เป็นครั้งแรก")
				.type(AchievementType.ONE_TIME).points(500)
				.category(GameCategory.GENERAL).build());

		AchievementRegistry.register(AchievementDefinition.builder()
				.id("Global.Wins")
				.nameEN("First Win").nameTH("ชัยชนะครั้งแรก")
				.descEN("Win your first game").descTH("ชนะเกมเป็นครั้งแรก")
				.type(AchievementType.ONE_TIME).points(500)
				.category(GameCategory.GENERAL).build());

		// Skywars
		AchievementRegistry.register(AchievementDefinition.builder()
				.id("Skywars.Wins")
				.nameEN("Sky King").nameTH("ราชาแห่งท้องฟ้า")
				.descEN("Win 30 Games of Skywars").descTH("ชนะเกม Skywars 30 ครั้ง")
				.type(AchievementType.TIERED)
				.category(GameCategory.SKYWARS)
				.tier(new AchievementTier(1, 10, 500))
				.tier(new AchievementTier(2, 20, 1000))
				.tier(new AchievementTier(3, 30, 2000))
				.build());

		AchievementRegistry.register(AchievementDefinition.builder()
				.id("Skywars.BombPickups")
				.nameEN("TNT Hoarder").nameTH("นักสะสมระเบิด")
				.descEN("Pickup 100 Super Throwing TNTs").descTH("เก็บระเบิด Super Throwing TNT ครบ 100 ลูก")
				.type(AchievementType.ONE_TIME).points(250)
				.category(GameCategory.SKYWARS).build());
	}

	@Override
	public void addCommands() {
		addCommand(new StatsCommand(this));
	}

	public void clearLog(Player player) {
		_log.remove(player.getName());
	}

	public AchievementData get(Player player, Achievement type) {
		return get(player.getName(), type);
	}

	public AchievementData get(String playerName, Achievement type) {
		int exp = 0;

		for (final String stat : type.getStats()) {
			exp += _statsManager.Get(playerName).getStat(stat);
		}

		return type.getLevelData(exp);
	}

	public NautHashMap<Achievement, AchievementLog> getLog(Player player) {
		return _log.remove(player.getName());
	}

	public String getHouziLevel(Player sender, Rank rank) {
		return Achievement.getExperienceString(getHouziLevelNumber(sender, rank)) + " " + ChatColor.RESET;
	}

	public int getHouziLevelNumber(Player sender, Rank rank) {
		int level = get(sender, Achievement.GLOBAL_HOUZI_LEVEL).getLevel();

		if (sender.getName().equalsIgnoreCase("B2_mp"))
			return 101;

		if (rank.Has(Rank.MODERATOR)) {
			level = Math.max(level, 5);
		}
		if (rank.Has(Rank.SNR_MODERATOR)) {
			level = Math.max(level, 15);
		}
		if (rank.Has(Rank.JNR_DEV)) {
			level = Math.max(level, 25);
		}
		if (rank.Has(Rank.ADMIN)) {
			level = Math.max(level, 30 + get(sender, Achievement.GLOBAL_GEM_HUNTER).getLevel());
		}
		if (rank.Has(Rank.OWNER)) {
			level = Math.max(level, 50 + get(sender, Achievement.GLOBAL_GEM_HUNTER).getLevel());
		}

		if (sender.getName().equalsIgnoreCase("Phinary")) {
			level = -level;
		}

		return level;
	}

	public void giveInterfaceItem(Player player) {
		if (!UtilGear.isMat(player.getInventory().getItem(_interfaceSlot), Material.PLAYER_HEAD)) {
			final ItemStack item = ItemStackFactory.Instance.CreateStack(Material.PLAYER_HEAD, (byte) 3, 1,
					ChatColor.RESET + C.cGreen + "/stats");
			final SkullMeta meta = (SkullMeta) item.getItemMeta();
			meta.setOwner(player.getName());
			item.setItemMeta(meta);

			player.getInventory().setItem(_interfaceSlot, item);

			UtilInv.Update(player);
		}
	}

	public boolean hasCategory(Player player, Achievement[] required) {
		if (required == null || required.length == 0)
			return false;

		for (final Achievement cur : required) {
			if (get(player, cur).getLevel() < cur.getMaxLevel())
				return false;
		}

		return true;
	}

	@EventHandler
	public void informLevelUp(StatChangeEvent event) {
		final Player player = UtilPlayer.searchExact(event.getPlayerName());
		if (player == null)
			return;

		// --- NEW SYSTEM (AchievementRegistry) ---
		AchievementDefinition newDef = AchievementRegistry.getById(event.getStatName());
		if (newDef != null) {
			long valBefore = event.getValueBefore();
			long valAfter = event.getValueAfter();

			if (newDef.getType() == AchievementType.ONE_TIME) {
				if (valBefore == 0 && valAfter > 0) {
					// Reward one-time achievement
					com.houzicore.shared.common.util.UtilTextMiddle.display(
							"§6§l✦ ACHIEVEMENT", 
							"§e" + newDef.getName(false), 
							10, 60, 20, 
							player);
					player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
					_donationManager.RewardEssenceLater("Achievement (" + newDef.getId() + ")", player, newDef.getTotalPoints());
				}
			} else if (newDef.getType() == AchievementType.TIERED) {
				// Find highest tier reached before and after
				int tierBefore = 0;
				int tierAfter = 0;
				
				for (AchievementTier tier : newDef.getTiers()) {
					if (valBefore >= tier.goal()) tierBefore = tier.tier();
					if (valAfter >= tier.goal()) tierAfter = tier.tier();
				}

				if (tierAfter > tierBefore) {
					final int finalTierAfter = tierAfter;

					int pointsReward = newDef.getTiers().stream().filter(t -> t.tier() == finalTierAfter).findFirst().get().points();
					com.houzicore.shared.common.util.UtilTextMiddle.display(
							"§6§l✦ ACHIEVEMENT", 
							"§e" + newDef.getName(false) + " §7Tier " + finalTierAfter, 
							10, 60, 20, 
							player);
					player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
					_donationManager.RewardEssenceLater("Achievement (" + newDef.getId() + " T" + tierAfter + ")", player, pointsReward);
				}
			}
		}

		// --- OLD SYSTEM (Enum) ---
		for (final Achievement type : Achievement.values()) {
			for (final String stat : type.getStats()) {
				if (stat.equalsIgnoreCase(event.getStatName())) {
					if (!_log.containsKey(player.getName())) {
						_log.put(player.getName(), new NautHashMap<Achievement, AchievementLog>());
					}

					AchievementData dataBefore = type.getLevelData(event.getValueBefore());
					AchievementData dataAfter = type.getLevelData(event.getValueAfter());

					// Record that achievement has leveled up
					if (dataAfter.getLevel() > dataBefore.getLevel()) {
						
						// Reward player
						int newLevel = dataAfter.getLevel();
						com.houzicore.shared.common.util.UtilTextMiddle.display(
								"§6§l✦ ACHIEVEMENT", 
								"§e" + type.getName() + " §7Lv. " + newLevel, 
								10, 60, 20, 
								player);
						player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
						int essenceReward = newLevel * 50;
						_donationManager.RewardEssenceLater("Achievement (" + type.getName() + " - " + newLevel + ")", player, essenceReward);

						// Add new
						if (!_log.get(player.getName()).containsKey(type)) {
							_log.get(player.getName()).put(type,
									new AchievementLog(event.getValueAfter() - event.getValueBefore(), true));
						}
						// Edit previous
						else {
							final AchievementLog log = _log.get(player.getName()).get(type);
							log.Amount += event.getValueAfter() - event.getValueBefore();
							log.LevelUp = true;
						}

					}
					// Record that there has been changes in this Achievement
					else if (!_log.get(player.getName()).containsKey(type)) {
						// Add new
						if (!_log.get(player.getName()).containsKey(type)) {
							_log.get(player.getName()).put(type,
									new AchievementLog(event.getValueAfter() - event.getValueBefore(), false));
						}
						// Edit previous
						else {
							final AchievementLog log = _log.get(player.getName()).get(type);
							log.Amount += event.getValueAfter() - event.getValueBefore();
						}
					}
				}
			}
		}
	}

	public void openShop(Player player) {
		_shop.attemptShopOpen(player);
	}

	public void openShop(Player player, Player target) {
		_shop.attemptShopOpen(player, target);
	}

	@EventHandler
	public void openShop(PlayerInteractEvent event) {
		if (!_shopEnabled)
			return;

		if (event.hasItem() && event.getItem().getType() == Material.PLAYER_HEAD) {
			if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
				if (ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName()).contains("/stats")) {
					event.setCancelled(true);
					openShop(event.getPlayer());
				}
			}
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		if (_giveInterfaceItem) {
			giveInterfaceItem(event.getPlayer());
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		_log.remove(event.getPlayer().getName());
	}

	public void setGiveInterfaceItem(boolean giveInterfaceItem) {
		_giveInterfaceItem = giveInterfaceItem;
	}

	public void setShopEnabled(boolean var) {
		_shopEnabled = var;
	}
}



