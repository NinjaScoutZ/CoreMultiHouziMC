package com.houzicore.shared.core.treasure;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.shared.core.reward.RewardType;

public enum TreasureType {
	// 5 เกรดหลักวิถีเซียนที่เปิดแสดงผลในระบบล็อบบี้ส่วนกลาง
	OLD("<gray>", "หยกดวงจิตศิษย์สายนอก", "Outer Soul Jade", RewardType.OldChest, Material.CAULDRON, TreasureStyle.OLD, 1000),
	ANCIENT("<gradient:#ff5500:#ffaa00>", "คัมภีร์ลี้ลับแดนปราณสวรรค์", "Mystic Heaven Scripture", RewardType.AncientChest, Material.CAULDRON, TreasureStyle.ANCIENT, 3000),
	MYTHICAL("<gradient:#db00ff:#7200ff>", "เตาหลอมโอสถเก้าจักรพรรดิ", "Nine Emperors Cauldron", RewardType.MythicalChest, Material.CAULDRON, TreasureStyle.MYTHICAL, 8000),
	IMMORTAL("<gradient:#00c6ff:#0072ff>", "ศิลาจารึกมรดกเต๋าอมตะ", "Immortal Tao Stele", RewardType.OldChest, Material.ANVIL, TreasureStyle.LEGENDARY, 15000),
	DIVINE("<rainbow><bold>", "เนตรดวงดาราจุติเก้าชั้นฟ้า", "Nine Skies Divine Eye", RewardType.MythicalChest, Material.BEACON, TreasureStyle.MYTHICAL, 30000),

	// Hidden/Legacy Entries (คงไว้เพื่อกระเป๋าเงินผู้เล่นเดิมและ LvlManager เคลียร์ความปลอดภัย)
	LEGENDARY("§6", "Legendary Chest (Legacy)", "Legendary Chest", RewardType.LegendaryChest, Material.CHEST, TreasureStyle.LEGENDARY, 7500),
	ILLUMINATED("§d", "Illuminated Chest (Legacy)", "Illuminated Chest", RewardType.IlluminatedChest, Material.SEA_LANTERN, TreasureStyle.ILLUMINATED, 20000);

	private final String _colorPrefix;
	private final String _thaiName;
	private final String _engName;
	private final RewardType _rewardType;
	private final Material _material;
	private final TreasureStyle _treasureStyle;
	private final int _costCoins;

	TreasureType(String colorPrefix, String thaiName, String engName,
			RewardType rewardType, Material material, TreasureStyle treasureStyle, int costCoins) {
		_colorPrefix = colorPrefix;
		_thaiName = thaiName;
		_engName = engName;
		_rewardType = rewardType;
		_material = material;
		_treasureStyle = treasureStyle;
		_costCoins = costCoins;
	}

	public String getName() {
		return getFormattedName(_engName);
	}

	public String getDisplayName(Player player) {
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		return getFormattedName(isThai ? _thaiName : _engName);
	}

	public String getDisplayName(boolean isThai) {
		return getFormattedName(isThai ? _thaiName : _engName);
	}

	private String getFormattedName(String text) {
		if (_colorPrefix.startsWith("§")) {
			return _colorPrefix + text;
		}
		if (_colorPrefix.contains("gradient")) {
			return _colorPrefix + text + "</gradient>";
		}
		if (_colorPrefix.contains("rainbow")) {
			return _colorPrefix + text + "</bold></rainbow>";
		}
		return _colorPrefix + text + "</" + _colorPrefix.replace("<", "").replace(">", "") + ">";
	}

	public String getDisplay(boolean isThai) {
		return isThai ? _thaiName : _engName;
	}

	public String getPlainName(Player player) {
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		return isThai ? _thaiName : _engName;
	}

	public String getItemName() {
		return _engName;
	}

	public Material getMaterial() { return _material; }
	public RewardType getRewardType() { return _rewardType; }
	public TreasureStyle getStyle() { return _treasureStyle; }
	public int getCostCoins() { return _costCoins; }
	public int getCostForQty(int qty) { return _costCoins * qty; }

	public List<String> getRarityOddsLore(boolean isThai) {
		List<String> lines = new ArrayList<>();
		String frame = "<dark_gray> ▪ </dark_gray>";
		
		if (isThai) {
			lines.add(" <gray>📊 โอกาสบรรลุมรรคผลรางวัล:</gray>");
			switch (this) {
				case OLD -> {
					lines.add(frame + "<gray>เกรดสามัญทั่วไป (COMMON): <green>75%</green></gray>");
					lines.add(frame + "<gray>เกรดวัตถุมงคลหายาก (RARE): <aqua>25%</aqua></gray>");
				}
				case ANCIENT -> {
					lines.add(frame + "<gray>เกรดวัตถุมงคลหายาก (RARE): <aqua>60%</aqua></gray>");
					lines.add(frame + "<gray>เกรดสมบัติล้ำค่าแดนใต้ (EPIC): <purple>40%</purple></gray>");
				}
				case MYTHICAL -> {
					lines.add(frame + "<gray>เกรดสมบัติล้ำค่าแดนใต้ (EPIC): <purple>70%</purple></gray>");
					lines.add(frame + "<gray>เกรดวัตถุในตำนานเซียน (LEGENDARY): <gold>30%</gold></gray>");
				}
				case IMMORTAL -> {
					lines.add(frame + "<gray>เกรดวัตถุในตำนานเซียน (LEGENDARY): <gold>80%</gold></gray>");
					lines.add(frame + "<gray>เกรดสมบัติมรดกอมตะ (MYTHIC): <light_purple>20%</light_purple></gray>");
				}
				case DIVINE -> {
					lines.add(frame + "<gray>เกรดสมบัติมรดกอมตะ (MYTHIC): <light_purple>85%</light_purple></gray>");
					lines.add(frame + "<gray>เกรดจุติเนตรเก้าชั้นฟ้า (JACKPOT): <rainbow><bold>15%</bold></rainbow></gray>");
				}
			}
		} else {
			lines.add(" <gray>📊 Breakthrough Odds:</gray>");
			switch (this) {
				case OLD -> {
					lines.add(frame + "<gray>Common Tier: <green>75%</green></gray>");
					lines.add(frame + "<gray>Rare Tier: <aqua>25%</aqua></gray>");
				}
				case ANCIENT -> {
					lines.add(frame + "<gray>Rare Tier: <aqua>60%</aqua></gray>");
					lines.add(frame + "<gray>Epic Tier: <purple>40%</purple></gray>");
				}
				case MYTHICAL -> {
					lines.add(frame + "<gray>Epic Tier: <purple>70%</purple></gray>");
					lines.add(frame + "<gray>Legendary Tier: <gold>30%</gold></gray>");
				}
				case IMMORTAL -> {
					lines.add(frame + "<gray>Legendary Tier: <gold>80%</gold></gray>");
					lines.add(frame + "<gray>Mythic Tier: <light_purple>20%</light_purple></gray>");
				}
				case DIVINE -> {
					lines.add(frame + "<gray>Mythic Tier: <light_purple>85%</light_purple></gray>");
					lines.add(frame + "<gray>Divine Jackpot: <rainbow><bold>15%</bold></rainbow></gray>");
				}
			}
		}
		return lines;
	}
}
