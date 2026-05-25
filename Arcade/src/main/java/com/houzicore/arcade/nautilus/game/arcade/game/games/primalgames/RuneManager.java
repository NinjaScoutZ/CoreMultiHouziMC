package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.common.util.UtilInv;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class RuneManager implements Listener
{
	private PrimalGames _game;

	public RuneManager(PrimalGames game)
	{
		_game = game;
	}

	public enum RuneType 
	{
		VAMPIRIC(Material.RED_DYE, C.cRed + C.Bold + "Vampiric Rune", C.cRed + "[Vampiric]"),
		VENOM(Material.SPIDER_EYE, C.cGreen + C.Bold + "Venom Rune", C.cGreen + "[Venom]"),
		FROST(Material.SNOWBALL, C.cAqua + C.Bold + "Frost Rune", C.cAqua + "[Frost]"),
		FLAME(Material.BLAZE_POWDER, C.cGold + C.Bold + "Flame Rune", C.cGold + "[Flame]");

		public Material mat;
		public String itemName;
		public String loreAdd;

		RuneType(Material mat, String itemName, String loreAdd) 
		{
			this.mat = mat;
			this.itemName = itemName;
			this.loreAdd = loreAdd;
		}
	}

	public ItemStack getRuneItem(RuneType type)
	{
		return new ItemBuilder(type.mat)
				.setTitle(type.itemName)
				.addLore(C.cGray + "Drag and drop this onto a")
				.addLore(C.cGray + "Sword, Axe, or Bow to enchant it!")
				.build();
	}

	@EventHandler
	public void onRuneApply(InventoryClickEvent event)
	{
		if (!_game.IsLive())
			return;

		if (!(event.getWhoClicked() instanceof Player)) return;
		Player player = (Player) event.getWhoClicked();
		if (!_game.IsAlive(player))
			return;

		ItemStack cursor = event.getCursor();
		ItemStack current = event.getCurrentItem();

		if (cursor == null || cursor.getType() == Material.AIR) return;
		if (current == null || current.getType() == Material.AIR) return;

		// Check if target is a weapon
		String typeName = current.getType().name();
		if (!typeName.contains("SWORD") && !typeName.contains("AXE") && !typeName.contains("BOW")) 
		{
			return;
		}

		RuneType appliedRune = null;

		for (RuneType type : RuneType.values())
		{
			if (cursor.getType() == type.mat)
			{
				ItemMeta meta = cursor.getItemMeta();
				if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(type.itemName))
				{
					appliedRune = type;
					break;
				}
			}
		}

		if (appliedRune == null) return;

		// We have a match! Apply rune to weapon
		event.setCancelled(true);

		ItemMeta wepMeta = current.getItemMeta();
		List<String> lore = wepMeta.hasLore() ? wepMeta.getLore() : new ArrayList<String>();
		
		// Prevent stacking same rune
		if (lore.contains(appliedRune.loreAdd))
		{
			player.sendMessage(C.cRed + "This weapon already has the " + appliedRune.itemName + C.cRed + "!");
			return;
		}

		lore.add(appliedRune.loreAdd);
		wepMeta.setLore(lore);
		current.setItemMeta(wepMeta);

		// Consume 1 rune
		if (cursor.getAmount() > 1)
		{
			cursor.setAmount(cursor.getAmount() - 1);
			event.getView().setCursor(cursor);
		}
		else
		{
			event.getView().setCursor(null);
		}

		player.sendMessage(C.cGreen + "You applied a " + appliedRune.itemName + C.cGreen + " to your weapon.");
		player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onRuneDamage(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled()) return;
		if (!_game.IsLive()) return;

		Player attacker = null;

		if (event.getDamager() instanceof Player)
		{
			attacker = (Player) event.getDamager();
		}
		else if (event.getDamager() instanceof Arrow)
		{
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player)
			{
				attacker = (Player) arrow.getShooter();
			}
		}

		if (attacker == null) return;
		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity target = (LivingEntity) event.getEntity();

		ItemStack weapon = attacker.getInventory().getItemInMainHand();
		if (weapon == null || !weapon.hasItemMeta() || !weapon.getItemMeta().hasLore()) return;

		List<String> lore = weapon.getItemMeta().getLore();

		if (lore.contains(RuneType.VAMPIRIC.loreAdd))
		{
			double health = attacker.getHealth() + 1.0;
			attacker.setHealth(Math.min(attacker.getMaxHealth(), health));
			// Optional particle
			attacker.getWorld().spawnParticle(org.bukkit.Particle.HEART, attacker.getLocation().add(0, 2, 0), 1, 0.2, 0.2, 0.2, 0);
		}

		if (lore.contains(RuneType.VENOM.loreAdd))
		{
			target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
		}

		if (lore.contains(RuneType.FROST.loreAdd))
		{
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
		}

		if (lore.contains(RuneType.FLAME.loreAdd))
		{
			target.setFireTicks(60);
		}
	}
}
