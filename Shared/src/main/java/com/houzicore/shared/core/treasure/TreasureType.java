package com.houzicore.shared.core.treasure;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.reward.RewardType;

public enum TreasureType {
	OLD(
		C.cYellow,
		"Old Chest",
		"ui.types.old.name",
		RewardType.OldChest,
		Material.CHEST,
		TreasureStyle.OLD,
		1000
	),
	ANCIENT(
		C.cGold,
		"Ancient Chest",
		"ui.types.ancient.name",
		RewardType.AncientChest,
		Material.TRAPPED_CHEST,
		TreasureStyle.ANCIENT,
		5000
	),
	LEGENDARY(
		C.cYellow,
		"Legendary Chest",
		"ui.types.legendary.name",
		RewardType.LegendaryChest,
		Material.ENDER_CHEST,
		TreasureStyle.LEGENDARY,
		7500
	),
	MYTHICAL(
		C.cRed,
		"Mythical Chest",
		"ui.types.mythical.name",
		RewardType.MythicalChest,
		Material.DRAGON_EGG,
		TreasureStyle.MYTHICAL,
		10000
	),
	ILLUMINATED(
		C.cPurple,
		"Illuminated Chest",
		"ui.types.illuminated.name",
		RewardType.IlluminatedChest,
		Material.SEA_LANTERN,
		TreasureStyle.ILLUMINATED,
		20000
	);

	private final String _colorPrefix;
	private final String _itemName;
	private final String _langKey;
	private final RewardType _rewardType;
	private final Material _material;
	private final TreasureStyle _treasureStyle;
	private final int _costCoins;

	TreasureType(String colorPrefix, String itemName, String langKey,
			RewardType rewardType, Material material, TreasureStyle treasureStyle,
			int costCoins) {
		_colorPrefix = colorPrefix;
		_itemName = itemName;
		_langKey = langKey;
		_rewardType = rewardType;
		_material = material;
		_treasureStyle = treasureStyle;
		_costCoins = costCoins;
	}

	private static Material resolveMaterial(String preferred, String fallback) {
		Material preferredMaterial = Material.matchMaterial(preferred);
		if (preferredMaterial != null)
			return preferredMaterial;

		Material fallbackMaterial = Material.matchMaterial(fallback);
		return fallbackMaterial != null ? fallbackMaterial : Material.CHEST;
	}

	public String getName() {
		return _colorPrefix + UtilText.toSmallCaps(_itemName);
	}

	public String getDisplayName(Player player) {
		return _colorPrefix + UtilText.toSmallCaps(TreasureLang.get(player, _langKey, _itemName));
	}

	public String getDisplayName(boolean isThai) {
		return _colorPrefix + UtilText.toSmallCaps(TreasureLang.getForLocale(isThai ? "THA" : "ENG", _langKey, _itemName));
	}

	public String getDisplay(boolean isThai) {
		return TreasureLang.getForLocale(isThai ? "THA" : "ENG", _langKey, _itemName);
	}

	public String getPlainName(Player player) {
		return com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(player, _langKey, _itemName));
	}

	public String getItemName() {
		return _itemName;
	}

	public Material getMaterial() {
		return _material;
	}

	public RewardType getRewardType() {
		return _rewardType;
	}

	public TreasureStyle getStyle() {
		return _treasureStyle;
	}

	public int getCostCoins() {
		return _costCoins;
	}

	public int getCostForQty(int qty) {
		return _costCoins * qty;
	}

	public List<String> getRarityOddsLore(boolean isThai) {
		List<String> lines = new ArrayList<>();
		double myth = _rewardType.getMythicalChance() * 100;
		double legend = (_rewardType.getLegendaryChance() - _rewardType.getMythicalChance()) * 100;
		double rare = (_rewardType.getRareChance() - _rewardType.getLegendaryChance()) * 100;
		double uncommon = (_rewardType.getUncommonChance() - _rewardType.getRareChance()) * 100;
		double common = Math.max(0, 100 - uncommon - rare - legend - myth);
		String locale = isThai ? "THA" : "ENG";

		lines.add(" §7" + TreasureLang.getForLocale(locale, "ui.odds.header", isThai ? "โอกาสได้รับ:" : "Drop odds:"));
		lines.add(String.format(
			" §f• §7%s §f%.0f%%  §e%s §f%.1f%%",
			TreasureLang.getForLocale(locale, "ui.odds.common", "Common"),
			common,
			TreasureLang.getForLocale(locale, "ui.odds.uncommon", "Uncommon"),
			uncommon
		));
		lines.add(String.format(
			" §f• §5%s §f%.1f%%  §6%s §f%.2f%%  §c%s §f%.3f%%",
			TreasureLang.getForLocale(locale, "ui.odds.rare", "Rare"),
			rare,
			TreasureLang.getForLocale(locale, "ui.odds.legendary", "Legendary"),
			legend,
			TreasureLang.getForLocale(locale, "ui.odds.mythical", "Mythical"),
			myth
		));
		return lines;
	}
}
