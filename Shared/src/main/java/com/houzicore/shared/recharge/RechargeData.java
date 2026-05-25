package com.houzicore.shared.recharge;

import com.houzicore.shared.common.util.C;

import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.util.UtilTime;

import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class RechargeData {
	public Recharge Host;

	public long Time;
	public long Recharge;

	public Player Player;
	public String Name;

	public ItemStack Item;

	public boolean DisplayForce = false;
	public boolean Countdown = false; // This will make the output a countdown, instead of a recharge.
	public boolean AttachItem;
	public boolean AttachDurability;

	public RechargeData(Recharge host, Player player, String name, ItemStack stack, long rechargeTime,
			boolean attachitem, boolean attachDurability) {
		Host = host;

		Player = player;
		Name = name;
		Item = player.getInventory().getItemInMainHand();
		Time = System.currentTimeMillis();
		Recharge = rechargeTime;

		AttachItem = attachitem;
		AttachDurability = attachDurability;
		
		// UX Standard: native GUI cooldown overlay for active ability items
		if (AttachItem && Item != null && Item.getType() != org.bukkit.Material.AIR) {
			Player.setCooldown(Item.getType(), (int) (Recharge / 50));
		}
	}

	public void debug(Player player) {
		player.sendMessage("Recharge: " + Recharge);
		player.sendMessage("Time: " + Time);
		player.sendMessage("Elapsed: " + (System.currentTimeMillis() - Time));
		player.sendMessage("Remaining: " + GetRemaining());
	}

	public long GetRemaining() {
		return Recharge - (System.currentTimeMillis() - Time);
	}

	public boolean Update() {
		if ((DisplayForce || Item != null) && Name != null && Player != null) {
			// Holding Recharge Item
			final double percent = (double) (System.currentTimeMillis() - Time) / (double) Recharge;

			if (DisplayForce || AttachItem) {
				try {
					if (DisplayForce || Item != null && UtilGear.isMat(Player.getInventory().getItemInMainHand(), Item.getType())) {
						if (!UtilTime.elapsed(Time, Recharge)) {
							// Update EXP Bar instead of ActionBar
							float expProgress = (float) percent;
							if (Countdown) expProgress = 1.0f - expProgress;
							
							// Clamp to prevent visual glitches
							if (expProgress < 0f) expProgress = 0f;
							if (expProgress > 1.0f) expProgress = 1.0f;
							
							int secondsLeft = (int) Math.ceil((Recharge - (System.currentTimeMillis() - Time)) / 1000.0);
							
							Player.setExp(expProgress);
							Player.setLevel(secondsLeft);

							// --- UI Upgrade: ActionBar Progress Bar ---
							String bar = com.houzicore.shared.common.util.UtilText.progressBar(expProgress, 10, '█', '▒');
							String timeStr = String.format("%.1fs", (Recharge - (System.currentTimeMillis() - Time)) / 1000.0f);
							String message = "§e⚡ " + Name + " §8» " + bar + " §c" + timeStr;
							UtilTextBottom.display(message, Player);

						} else {
							// Recharged/Ended
							Player.setExp(0f);
							Player.setLevel(0);
							UtilTextBottom.display("§a⚡ " + Name + " §aพร้อมใช้งาน!", Player);

							// PLING!
							if (Recharge > 4000) {
								Player.playSound(Player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.4f, 3f);
							}
						}
					}
				} catch (final Exception e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}

			if (AttachDurability && Item != null) {
				// Durability handling in 1.21.1 is different but we'll try to keep it compatible for now
				// ItemStack.setDurability is deprecated, use damageable meta if needed.
			}
		}

		return UtilTime.elapsed(Time, Recharge);
	}
}
