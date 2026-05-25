package com.houzicore.shared.core.give;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.give.commands.GiveCommand;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

public class Give extends MiniPlugin {
	public static Give Instance;

	public static void Initialize(JavaPlugin plugin) {
		Instance = new Give(plugin);
	}

	protected Give(JavaPlugin plugin) {
		super("Give Factory", plugin);
	}

	@Override
	public void addCommands() {
		addCommand(new GiveCommand(this));
	}

	public void give(Player player, String target, String itemNames, String amount, String enchants) {
		// Item
		LinkedList<Entry<Material, Byte>> itemList = new LinkedList<>();
		itemList = UtilItem.matchItem(player, itemNames, true);
		if (itemList.isEmpty())
			return;

		// Player
		LinkedList<Player> giveList = new LinkedList<>();

		if (target.equalsIgnoreCase("all")) {
			for (final Player cur : UtilServer.getPlayers()) {
				giveList.add(cur);
			}
		} else {
			giveList = UtilPlayer.matchOnline(player, target, true);
			if (giveList.isEmpty())
				return;
		}

		// Amount
		int count = 1;
		try {
			count = Integer.parseInt(amount);

			if (count < 1) {
				UtilPlayer.message(player, F.main("Give", "Invalid Amount [" + amount + "]. Defaulting to [1]."));
				count = 1;
			}
		} catch (final Exception e) {
			UtilPlayer.message(player, F.main("Give", "Invalid Amount [" + amount + "]. Defaulting to [1]."));
		}

		// Enchants
		final HashMap<Enchantment, Integer> enchs = new HashMap<>();
		if (enchants.length() > 0) {
			for (final String cur : enchants.split(",")) {
				try {
					final String[] tokens = cur.split(":");
					enchs.put(Enchantment.getByName(tokens[0]), Integer.parseInt(tokens[1]));
				} catch (final Exception e) {
					UtilPlayer.message(player, F.main("Give", "Invalid Enchantment [" + cur + "]."));
				}
			}
		}

		// Create
		String givenList = "";
		for (final Player cur : giveList) {
			givenList += cur.getName() + " ";
		}
		if (givenList.length() > 0) {
			givenList = givenList.substring(0, givenList.length() - 1);
		}

		for (final Entry<Material, Byte> curItem : itemList) {
			for (final Player cur : giveList) {
				final ItemStack stack = ItemStackFactory.Instance.CreateStack(curItem.getKey(), curItem.getValue(),
						count);

				// Enchants
				stack.addUnsafeEnchantments(enchs);

				// Give
				if (UtilInv.insert(cur, stack)) {
					// Inform
					if (!cur.equals(player)) {
						UtilPlayer.message(cur,
								F.main("Give",
										"You received "
												+ F.item(count + " "
														+ ItemStackFactory.Instance.GetName(curItem.getKey(),
																curItem.getValue(), false))
												+ " from " + F.elem(player.getName()) + "."));
					}
				}
			}

			if (target.equalsIgnoreCase("all")) {
				UtilPlayer
						.message(
								player, F
										.main("Give",
												"You gave "
														+ F.item(count + " "
																+ ItemStackFactory.Instance.GetName(curItem.getKey(),
																		curItem.getValue(), false))
														+ " to " + F.elem("ALL"))
										+ ".");
			} else if (giveList.size() > 1) {
				UtilPlayer.message(player,
						F.main("Give",
								"You gave "
										+ F.item(count + " "
												+ ItemStackFactory.Instance.GetName(curItem.getKey(),
														curItem.getValue(), false))
										+ " to " + F.elem(givenList) + "."));
			} else {
				UtilPlayer.message(player,
						F.main("Give",
								"You gave "
										+ F.item(count + " "
												+ ItemStackFactory.Instance.GetName(curItem.getKey(),
														curItem.getValue(), false))
										+ " to " + F.elem(giveList.getFirst().getName()) + "."));
			}
		}
	}

	public void help(Player player) {
		UtilPlayer.message(player, F.main("Give", "Commands List;"));
	}

	public void parseInput(Player player, String[] args) {
		if (args.length == 0) {
			help(player);
		} else if (args.length == 1) {
			give(player, player.getName(), args[0], "1", "");
		} else if (args.length == 2) {
			give(player, args[0], args[1], "1", "");
		} else if (args.length == 3) {
			give(player, args[0], args[1], args[2], "");
		} else {
			give(player, args[0], args[1], args[2], args[3]);
		}
	}
}
