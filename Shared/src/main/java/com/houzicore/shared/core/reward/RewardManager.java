package com.houzicore.shared.core.reward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.reward.rewards.CoinReward;
import com.houzicore.shared.core.reward.rewards.CosmeticReward;
import com.houzicore.shared.core.reward.rewards.InventoryReward;
import com.houzicore.shared.core.reward.rewards.PetReward;
import com.houzicore.shared.core.reward.rewards.RankReward;

public class RewardManager {
	private final JavaPlugin _plugin;
	private final HashMap<RewardRarity, List<Reward>> _treasureMap;
	private final Random _random;

	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;

	private final boolean _doubleGadgetValue;

	public RewardManager(CoreClientManager clientManager, DonationManager donationManager,
			InventoryManager inventoryManager, PetManager petManager, int commonValueMin, int commonValueMax,
			int uncommonValueMin, int uncommonValueMax, int rareValueMin, int rareValueMax, int legendValueMin,
			int legendValueMax, boolean doubleGadgetValue) {
		_plugin = donationManager.getPlugin();
		_treasureMap = new HashMap<>();
		_random = new Random();

		for (final RewardRarity rarity : RewardRarity.values()) {
			_treasureMap.put(rarity, new ArrayList<Reward>());
		}

		_clientManager = clientManager;
		_donationManager = donationManager;

		_doubleGadgetValue = doubleGadgetValue;

		addCommon(donationManager, inventoryManager, petManager, commonValueMin, commonValueMax);
		addUncommon(donationManager, inventoryManager, petManager, uncommonValueMin, uncommonValueMax);
		addRare(donationManager, inventoryManager, petManager, rareValueMin, rareValueMax);
		addLegendary(donationManager, inventoryManager, petManager, legendValueMin, legendValueMax);
	}

	public void addCommon(DonationManager donationManager, InventoryManager inventoryManager, PetManager petManager,
			double minValue, double maxValue) {
		final RewardRarity rarity = RewardRarity.COMMON;

		// Coins
		addReward(new CoinReward(donationManager, (int) minValue, (int) maxValue, 1, rarity));

		// Increase Value
		if (_doubleGadgetValue) {
			minValue *= 2;
			maxValue *= 2;
		}

		// Gadgets
		addReward(new InventoryReward(inventoryManager, "Paintballs", "Paintball Gun", (int) (100 * (minValue / 500)),
				(int) (100 * (maxValue / 500)), new ItemStack(Material.GOLDEN_HORSE_ARMOR), rarity, 10));

		addReward(new InventoryReward(inventoryManager, "Fireworks", "Fireworks", (int) (50 * (minValue / 500)),
				(int) (50 * (maxValue / 500)), new ItemStack(Material.FIREWORK_ROCKET), rarity, 10));

		addReward(new InventoryReward(inventoryManager, "Melons", "Melon Launcher", (int) (50 * (minValue / 500)),
				(int) (50 * (maxValue / 500)), new ItemStack(Material.MELON), rarity, 10));

		addReward(new InventoryReward(inventoryManager, "Flesh Hooks", "Flesh Hook", (int) (40 * (minValue / 500)),
				(int) (40 * (maxValue / 500)), new ItemStack(Material.TRIPWIRE_HOOK), rarity, 10));

		addReward(new InventoryReward(inventoryManager, "Pearls", "Ethereal Pearl", (int) (30 * (minValue / 500)),
				(int) (30 * (maxValue / 500)), new ItemStack(Material.ENDER_PEARL), rarity, 10));

		addReward(new InventoryReward(inventoryManager, "Bat Swarms", "Bat Blaster", (int) (20 * (minValue / 500)),
				(int) (20 * (maxValue / 500)), new ItemStack(Material.IRON_HORSE_ARMOR), rarity, 10));

		addReward(new InventoryReward(inventoryManager, "TNT", "TNT", (int) (20 * (minValue / 500)),
				(int) (20 * (maxValue / 500)), new ItemStack(Material.TNT), rarity, 10));

		// Sprays & Win Effects (Common)
		addCosmeticReward(donationManager, "Cat Spray", "Cat Spray", Material.PAINTING, rarity, 10);
		addCosmeticReward(donationManager, "Fireworks Finale", "Fireworks Finale", Material.FIREWORK_ROCKET, rarity, 10);
	}

	public void addLegendary(DonationManager donationManager, InventoryManager inventoryManager, PetManager petManager,
			double minValue, double maxValue) {
		final RewardRarity rarity = RewardRarity.LEGENDARY;

		// Coins
		addReward(new CoinReward(donationManager, (int) minValue, (int) maxValue, 25, RewardRarity.LEGENDARY));

		// Mounts
		addCosmeticReward(donationManager, "Infernal Horror", "Infernal Horror", Material.BONE, rarity, 33);

		// Morphs
		addCosmeticReward(donationManager, "Bat Morph", "Bat Morph", Material.PLAYER_HEAD, rarity, 25);
		addCosmeticReward(donationManager, "Block Morph", "Block Morph", Material.EMERALD_BLOCK, rarity, 20);

		// Particles
		addCosmeticReward(donationManager, "Shadow Walk Particles", "Shadow Walk", Material.LEATHER_BOOTS, rarity, 33);
		addCosmeticReward(donationManager, "Enchanted Particles", "Enchanted", Material.BOOK, rarity, 25);
		addCosmeticReward(donationManager, "Flame Rings Particles", "Flame Rings", Material.BLAZE_POWDER, rarity, 17);
		addCosmeticReward(donationManager, "Rain Cloud Particles", "Rain Cloud", Material.LAPIS_LAZULI, rarity, 13);
		addCosmeticReward(donationManager, "Blood Helix Particles", "Blood Helix", Material.REDSTONE, rarity, 10);
		addCosmeticReward(donationManager, "Emerald Twirl Particles", "Green Ring", Material.EMERALD, rarity, 8);
		addCosmeticReward(donationManager, "Flame Fairy Particles", "Flame Fairy", Material.APPLE, rarity, 4);
		addCosmeticReward(donationManager, "Heart Particles", "I Heart You", Material.BLAZE_POWDER, rarity, 2);

		// Banners, Auras, Sprays, Win Effects (Legendary / Mythic)
		addCosmeticReward(donationManager, "Champion Banner", "Champion Banner", Material.ORANGE_BANNER, rarity, 10);
		addCosmeticReward(donationManager, "Skull Spray", "Skull Spray", Material.PAINTING, rarity, 15);
		addCosmeticReward(donationManager, "Crystal Aura", "Crystal Aura", Material.END_CRYSTAL, rarity, 10);
		addCosmeticReward(donationManager, "Shadow Aura", "Shadow Aura", Material.COAL_BLOCK, rarity, 5); // Mythic tier in shop, Legendary drop
		addCosmeticReward(donationManager, "Dragon Rise", "Dragon Rise", Material.DRAGON_EGG, rarity, 5); // Mythic tier in shop, Legendary drop
	}

	public void addRare(DonationManager donationManager, InventoryManager inventoryManager, PetManager petManager,
			double minValue, double maxValue) {
		final RewardRarity rarity = RewardRarity.RARE;

		// Coins
		addReward(new CoinReward(donationManager, (int) minValue, (int) maxValue, 100, RewardRarity.RARE));

		// Mounts
		addCosmeticReward(donationManager, "Mount Mule", "Mount Mule", Material.HAY_BLOCK, rarity, 200);
		addCosmeticReward(donationManager, "Minecart Mount", "Minecart", Material.MINECART, rarity, 100);
		addCosmeticReward(donationManager, "Slime Mount", "Slime Mount", Material.SLIME_BALL, rarity, 67);
		addCosmeticReward(donationManager, "Glacial Steed", "Glacial Steed", Material.SNOWBALL, rarity, 50);

		// Morphs
		addCosmeticReward(donationManager, "Cow Morph", "Cow Morph", Material.LEATHER, rarity, 167);
		addCosmeticReward(donationManager, "Villager Morph", "Villager Morph", Material.EMERALD, rarity, 83);
		addCosmeticReward(donationManager, "Chicken Morph", "Chicken Morph", Material.FEATHER, rarity, 50);
		addCosmeticReward(donationManager, "Enderman Morph", "Enderman Morph", Material.ENDER_PEARL, rarity, 33);

		// Gadgets
		addReward(new InventoryReward(inventoryManager, "Coin Party Bomb", "Coin Party Bomb", 1, 1,
				new ItemStack(Material.SUNFLOWER), rarity, 100));

		// Costumes
		addCosmeticReward(donationManager, "Rave Hat", "Rave Hat", Material.LEATHER_HELMET, rarity, 30);
		addCosmeticReward(donationManager, "Rave Shirt", "Rave Shirt", Material.LEATHER_CHESTPLATE, rarity, 30);
		addCosmeticReward(donationManager, "Rave Pants", "Rave Pants", Material.LEATHER_LEGGINGS, rarity, 30);
		addCosmeticReward(donationManager, "Rave Boots", "Rave Boots", Material.LEATHER_BOOTS, rarity, 30);
		addCosmeticReward(donationManager, "Space Helmet", "Space Helmet", Material.GLASS, rarity, 50);
		addCosmeticReward(donationManager, "Space Jacket", "Space Jacket", Material.GOLDEN_CHESTPLATE, rarity, 50);
		addCosmeticReward(donationManager, "Space Pants", "Space Pants", Material.GOLDEN_LEGGINGS, rarity, 50);
		addCosmeticReward(donationManager, "Space Boots", "Space Boots", Material.GOLDEN_BOOTS, rarity, 50);

		// Sprays, Auras, Win Effects (Rare / Epic)
		addCosmeticReward(donationManager, "Star Spray", "Star Spray", Material.PAINTING, rarity, 50);
		addCosmeticReward(donationManager, "Heart Spray", "Heart Spray", Material.PAINTING, rarity, 30); // Epic tier in shop
		addCosmeticReward(donationManager, "Cherry Aura", "Cherry Aura", Material.CHERRY_LEAVES, rarity, 50);
		addCosmeticReward(donationManager, "Flame Aura", "Flame Aura", Material.BLAZE_POWDER, rarity, 30); // Epic tier in shop
		addCosmeticReward(donationManager, "Lightning Storm", "Lightning Storm", Material.LIGHTNING_ROD, rarity, 30); // Epic tier in shop
	}

	public void addReward(Reward reward) {
		final RewardRarity rarity = reward.getRarity();

		final List<Reward> treasureList = _treasureMap.get(rarity);

		treasureList.add(reward);
	}

	public void addUncommon(DonationManager donationManager, InventoryManager inventoryManager, PetManager petManager,
			double minValue, double maxValue) {
		final RewardRarity rarity = RewardRarity.UNCOMMON;

		// Coins
		addReward(new CoinReward(donationManager, (int) minValue, (int) maxValue, 250, RewardRarity.UNCOMMON));

		// Increase Value
		if (_doubleGadgetValue) {
			minValue *= 2;
			maxValue *= 2;
		}

		// Gadgets
		addReward(new InventoryReward(inventoryManager, "Paintballs", "Paintball Gun", (int) (100 * (minValue / 500)),
				(int) (100 * (maxValue / 500)), new ItemStack(Material.GOLDEN_HORSE_ARMOR), rarity, 250));

		addReward(new InventoryReward(inventoryManager, "Fireworks", "Fireworks", (int) (50 * (minValue / 500)),
				(int) (50 * (maxValue / 500)), new ItemStack(Material.FIREWORK_ROCKET), rarity, 250));

		addReward(new InventoryReward(inventoryManager, "Melons", "Melon Launcher", (int) (50 * (minValue / 500)),
				(int) (50 * (maxValue / 500)), new ItemStack(Material.MELON), rarity, 250));

		addReward(new InventoryReward(inventoryManager, "Flesh Hooks", "Flesh Hook", (int) (40 * (minValue / 500)),
				(int) (40 * (maxValue / 500)), new ItemStack(Material.TRIPWIRE_HOOK), rarity, 250));

		addReward(new InventoryReward(inventoryManager, "Pearls", "Ethereal Pearl", (int) (30 * (minValue / 500)),
				(int) (30 * (maxValue / 500)), new ItemStack(Material.ENDER_PEARL), rarity, 250));

		addReward(new InventoryReward(inventoryManager, "Bat Swarms", "Bat Blaster", (int) (20 * (minValue / 500)),
				(int) (20 * (maxValue / 500)), new ItemStack(Material.IRON_HORSE_ARMOR), rarity, 250));

		addReward(new InventoryReward(inventoryManager, "TNT", "TNT", (int) (20 * (minValue / 500)),
				(int) (20 * (maxValue / 500)), new ItemStack(Material.TNT), rarity, 250));

		// Pets
		addReward(new PetReward(petManager, inventoryManager, donationManager, "Cow Pet", "Cow", EntityType.COW, rarity,
				500));
		addReward(new PetReward(petManager, inventoryManager, donationManager, "Sheep Pet", "Sheep", EntityType.SHEEP,
				rarity, 333));
		addReward(new PetReward(petManager, inventoryManager, donationManager, "Mooshroom Pet", "Mooshroom",
				EntityType.MOOSHROOM, rarity, 200));
		addReward(new PetReward(petManager, inventoryManager, donationManager, "Pig Pet", "Pig", EntityType.PIG, rarity,
				200));
		addReward(new PetReward(petManager, inventoryManager, donationManager, "Ocelot Pet", "Cat", EntityType.OCELOT,
				rarity, 167));
		addReward(new PetReward(petManager, inventoryManager, donationManager, "Chicken Pet", "Chicken",
				EntityType.CHICKEN, rarity, 143));
		addReward(new PetReward(petManager, inventoryManager, donationManager, "Wolf Pet", "Dog", EntityType.WOLF,
				rarity, 125));

		// Music Discs
		addCosmeticReward(donationManager, "13 Disc", "13 Disc", Material.MUSIC_DISC_13, rarity, 25);
		addCosmeticReward(donationManager, "Cat Disc", "Cat Disc", Material.MUSIC_DISC_CAT, rarity, 25);
		addCosmeticReward(donationManager, "Blocks Disc", "Blocks Disc", Material.MUSIC_DISC_BLOCKS, rarity, 25);
		addCosmeticReward(donationManager, "Chirp Disc", "Chirp Disc", Material.MUSIC_DISC_CHIRP, rarity, 25);
		addCosmeticReward(donationManager, "Far Disc", "Far Disc", Material.MUSIC_DISC_FAR, rarity, 25);
		addCosmeticReward(donationManager, "Mall Disc", "Mall Disc", Material.MUSIC_DISC_MALL, rarity, 25);
		addCosmeticReward(donationManager, "Mellohi Disc", "Mellohi Disc", Material.MUSIC_DISC_MELLOHI, rarity, 25);
		addCosmeticReward(donationManager, "Stal Disc", "Stal Disc", Material.MUSIC_DISC_STAL, rarity, 25);
		addCosmeticReward(donationManager, "Strad Disc", "Strad Disc", Material.MUSIC_DISC_STRAD, rarity, 25);
		addCosmeticReward(donationManager, "Ward Disc", "Ward Disc", Material.MUSIC_DISC_WARD, rarity, 25);
		addCosmeticReward(donationManager, "11 Disc", "11 Disc", Material.MUSIC_DISC_11, rarity, 25);
		addCosmeticReward(donationManager, "Wait Disc", "Wait Disc", Material.MUSIC_DISC_WAIT, rarity, 25);
	}

	private void addCosmeticReward(DonationManager donationManager, String displayName, String unlockId, Material material,
			RewardRarity rarity, int weight) {
		addReward(new CosmeticReward(donationManager, displayName, unlockId, new ItemStack(material), rarity, weight));
	}

	public Reward[] getRewards(Player player, RewardType type) {
		int currentReward = 0;
		final Reward[] rewards = new Reward[4];
		boolean hasUncommon = false;
		boolean canGiveMythical = true;
		int failCount = 0;

		while (currentReward < 4) {
			final Reward reward = nextReward(player, rewards, currentReward == 3 && !hasUncommon, type,
					canGiveMythical);

			if (reward == null) {
				failCount++;
				if (failCount > 50) {
					// Fallback if no rewards can be given
					rewards[currentReward] = new CoinReward(_donationManager, 50, 100, 1, RewardRarity.COMMON);
					currentReward++;
				}
				continue;
			}

			if (reward.getRarity().ordinal() >= RewardRarity.UNCOMMON.ordinal()) {
				hasUncommon = true;
			}

			// Only allow 1 Mythical
			if (reward.getRarity().ordinal() >= RewardRarity.MYTHICAL.ordinal()) {
				canGiveMythical = false;
			}

			rewards[currentReward] = reward;
			currentReward++;
		}

		// Swap the last reward with another one, this makes the uncommon added at the
		// end of some chests seem more random
		final int slotToSwitch = _random.nextInt(4);
		if (slotToSwitch != 3) {
			final Reward thirdReward = rewards[3];
			final Reward otherReward = rewards[slotToSwitch];

			rewards[3] = otherReward;
			rewards[slotToSwitch] = thirdReward;
		}

		return rewards;
	}

	// private Reward nextReward(Player player, Reward[] excludedRewards)
	// {
	// return nextReward(player, excludedRewards, false, isChestOpening);
	// }

	public Reward nextReward(Player player, Reward[] excludedRewards, boolean requiresUncommon, RewardType type,
			boolean canGiveMythical) {
		RewardRarity rarity = type.generateRarity(requiresUncommon);

		// Dont give Rank Upgrade if already has Legend
		if (rarity == RewardRarity.MYTHICAL) {
			if (!canGiveMythical || _clientManager.Get(player).GetRank().Has(Rank.DIVINE)) {
				rarity = RewardRarity.LEGENDARY;
			} else
				return new RankReward(_clientManager, 0, rarity);
		}

		final List<Reward> treasureList = _treasureMap.get(rarity);

		int totalWeight = 0;
		final ArrayList<Reward> possibleRewards = new ArrayList<>();
		for (final Reward treasure : treasureList) {
			boolean isExcluded = false;

			if (excludedRewards != null) {
				for (int i = 0; i < excludedRewards.length && !isExcluded; i++) {
					if (excludedRewards[i] != null && excludedRewards[i].equals(treasure)) {
						isExcluded = true;
					}
				}
			}

			if ((player == null || treasure.canGiveReward(player)) && !isExcluded) {
				possibleRewards.add(treasure);
				totalWeight += treasure.getWeight();
			}
		}

		if (totalWeight > 0) {
			final int weight = _random.nextInt(totalWeight);
			int currentWeight = 0;

			for (final Reward reward : possibleRewards) {
				currentWeight += reward.getWeight();

				if (weight <= currentWeight)
					return reward;
			}
		}

		return null;
	}
}
