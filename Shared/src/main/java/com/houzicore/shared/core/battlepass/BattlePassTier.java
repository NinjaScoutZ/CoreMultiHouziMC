package com.houzicore.shared.core.battlepass;

public enum BattlePassTier {
	TIER_1(1, 400, 100, null, 250, null),
	TIER_2(2, 600, 100, null, 250, null),
	TIER_3(3, 800, 100, null, 250, null),
	TIER_4(4, 1100, 100, null, 250, null),
	TIER_5(5, 1400, 200, "Spray Cat", 500, null),
	TIER_6(6, 1800, 150, null, 300, null),
	TIER_7(7, 2300, 150, null, 300, null),
	TIER_8(8, 2900, 150, null, 300, null),
	TIER_9(9, 3600, 150, null, 300, null),
	TIER_10(10, 4400, 300, null, 800, "Fox Morph"),
	TIER_11(11, 5300, 200, null, 350, null),
	TIER_12(12, 6300, 200, null, 350, null),
	TIER_13(13, 7400, 200, null, 350, null),
	TIER_14(14, 8600, 200, null, 350, null),
	TIER_15(15, 9900, 400, "Particle Cherry", 1000, null),
	TIER_16(16, 11300, 250, null, 400, null),
	TIER_17(17, 12800, 250, null, 400, null),
	TIER_18(18, 14400, 250, null, 400, null),
	TIER_19(19, 16100, 250, null, 400, null),
	TIER_20(20, 17900, 500, null, 1500, "Buzzy Bee"),
	TIER_21(21, 19800, 300, null, 500, null),
	TIER_22(22, 21800, 300, null, 500, null),
	TIER_23(23, 23900, 300, null, 500, null),
	TIER_24(24, 26100, 300, null, 500, null),
	TIER_25(25, 28400, 600, "Spray Houzi Logo", 2000, null),
	TIER_26(26, 30800, 350, null, 600, null),
	TIER_27(27, 33300, 350, null, 600, null),
	TIER_28(28, 35900, 350, null, 600, null),
	TIER_29(29, 38600, 350, null, 600, null),
	TIER_30(30, 41400, 1000, "Parrot Morph", 5000, "Particle DragonBreath");

	private final int _tier;
	private final int _requiredXp;
	private final int _essenceReward;
	private final String _cosmeticReward;
	private final int _premiumEssenceReward;
	private final String _premiumCosmeticReward;

	private BattlePassTier(int tier, int requiredXp, int freeEssence, String freeCosmetic, int premiumEssence, String premiumCosmetic) {
		_tier = tier;
		_requiredXp = requiredXp;
		_essenceReward = freeEssence;
		_cosmeticReward = freeCosmetic;
		_premiumEssenceReward = premiumEssence;
		_premiumCosmeticReward = premiumCosmetic;
	}

	public int getTier() { return _tier; }
	public int getRequiredXp() { return _requiredXp; }
	public int getEssenceReward() { return _essenceReward; }
	public String getCosmeticReward() { return _cosmeticReward; }
	public int getPremiumEssenceReward() { return _premiumEssenceReward; }
	public String getPremiumCosmeticReward() { return _premiumCosmeticReward; }

	public static BattlePassTier[] getTiers() {
		return values();
	}
}
