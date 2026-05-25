package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.modes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.event.BedRotEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.general.BedwarsPlayerModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.island.BedwarsIslandModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.BedwarsSpecialItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items.BedwarsDeployPlatform;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items.BedwarsIceBridge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items.BedwarsSafeTeleport;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items.BedwarsSheep;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items.BedwarsWall;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsResource;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopItemType;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.trap.BedwarsBearTrap;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.trap.BedwarsTNTTrap;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.ui.BedwarsResourcePage;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.chest.ChestLootPool;

public class OPBedwars extends Bedwars
{

	private final PotionEffect SUGAR_BUFF = new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false);

	public OPBedwars(ArcadeManager manager)
	{
		super(manager);
 
		AllowParticles = false;
		CanAddStats = false;
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		getBedwarsTeamModule().getBedwarsTeams().values().forEach(bedTeam ->
		{
			for (Block block : UtilBlock.getSurrounding(bedTeam.getBed().getBlock(), false))
			{
				if (block.getType() == Material.AIR)
				{
					getBedwarsPlayerModule().getPlacedBlocks().add(block);
					block.setType(Material.OBSIDIAN);
				}
			}

			bedTeam.getUpgrades().entrySet().forEach(entry -> entry.setValue(entry.getKey().getLevels().length));
		});

		for (GameTeam team : GetTeamList())
		{
			for (Player player : team.GetPlayers(false))
			{
				UtilTextMiddle.display(team.GetColor() + C.Bold + "Sugar Rush", team.GetColor() + "Hyper" + org.bukkit.ChatColor.RESET + " items available!", 0, 50, 40, player);
				player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
			}
		}
	}

	@EventHandler
	public void updateSugarRush(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		GetTeamList().forEach(team ->
		{
			if (getBedwarsPointModule().ownedEmeraldPoints(team) > 0)
			{
				for (Player player : team.GetPlayers(true))
				{
					if (UtilPlayer.isSpectator(player))
					{
						continue;
					}

					player.addPotionEffect(SUGAR_BUFF, true);
					player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, player.getLocation().add(0, 1, 0), 3, 0.1, 0.1, 0.1, 0.01);
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_ATTACK || !hasSugarBuff(event.GetDamagerPlayer(false)))
		{
			return;
		}

		event.AddMod(GetName(), "Sugar Buff", event.GetDamage() * 0.25, true);
		event.AddKnockback(GetName(), 1.25);
	}

	@EventHandler
	public void bedRot(BedRotEvent event)
	{
		WorldBorder border = WorldData.World.getWorldBorder();
		border.setCenter(GetSpectatorLocation());
		border.setSize(Math.max(WorldData.MaxX, WorldData.MaxZ) * 2);
		border.setSize(40, TimeUnit.MINUTES.toSeconds(15));
		border.setDamageAmount(0);

		UtilTextMiddle.display(C.cRed + C.Bold + "Sugar Crash", "The border is closing in.", 0, 50, 0, UtilServer.getPlayers());

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!IsLive())
				{
					cancel();
					return;
				}

				int size = (int) border.getSize() / 2;

				WorldData.MinX = WorldData.MinZ = -size;
				WorldData.MaxX = WorldData.MaxZ = size;
			}
		}, 0, 20);
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		WorldBorder border = WorldData.World.getWorldBorder();
		border.setSize(Integer.MAX_VALUE);
	}

	@Override
	public int getGeneratorRate(BedwarsResource resource, int current)
	{
		if (resource == BedwarsResource.STAR)
		{
			return -1;
		}

		// 2 seems like a low number to increase by, but all upgrades, including Resource Generator are maxed out by default.
		return current * 2;
	}

	@Override
	public List<BedwarsItem> generateItems(BedwarsResource resource)
	{
		switch (resource)
		{
			case BRICK:
				return Arrays.asList
						(
								// Diamond Set
								new BedwarsShopItem(BedwarsShopItemType.HELMET, new ItemStack(Material.DIAMOND_HELMET), 5),
								new BedwarsShopItem(BedwarsShopItemType.CHESTPLATE, new ItemStack(Material.DIAMOND_CHESTPLATE), 8),
								new BedwarsShopItem(BedwarsShopItemType.LEGGINGS, new ItemStack(Material.DIAMOND_LEGGINGS), 6),
								new BedwarsShopItem(BedwarsShopItemType.BOOTS, new ItemStack(Material.DIAMOND_BOOTS), 5),

								// Sword
								new BedwarsShopItem(BedwarsShopItemType.SWORD, new ItemStack(Material.DIAMOND_SWORD), 5),

								// Bow
								new BedwarsShopItem(BedwarsShopItemType.BOW, new ItemStack(Material.BOW), 12),

								// Pickaxe
								new BedwarsShopItem(BedwarsShopItemType.PICKAXE, new ItemStack(Material.DIAMOND_PICKAXE), 8),

								// Axe
								new BedwarsShopItem(BedwarsShopItemType.AXE, new ItemStack(Material.DIAMOND_AXE), 3),

								// Arrow
								new BedwarsShopItem(BedwarsShopItemType.OTHER, new ItemStack(Material.ARROW, 3), 12),

								// Blocks
								// Wool
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.WHITE_WOOL, 16), 3),

								// Coloured Clay
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.WHITE_TERRACOTTA, 8), 8),

								// Wood
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.OAK_PLANKS, 8), 8),

								// End Stone
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.END_STONE, 8), 12),

								// Deploy Platform
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsDeployPlatform.ITEM_STACK, 5),

								// Walls
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsWall.ITEM_STACK, 5),

								// Emerald
								new BedwarsShopItem(BedwarsShopItemType.OTHER, new ItemStack(Material.EMERALD), 20)
						);
			case EMERALD:
				return Arrays.asList
						(
								// Obsidian
								new BedwarsShopItem(BedwarsShopItemType.BLOCK, new ItemStack(Material.OBSIDIAN), 8),

								// Shears
								new BedwarsShopItem(BedwarsShopItemType.SHEARS, new ItemStack(Material.SHEARS), 5),

								// Golden Apple
								new BedwarsShopItem(BedwarsShopItemType.OTHER, new ItemStack(Material.GOLDEN_APPLE), 8),

								// Ender pearl
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsShopModule.ENDER_PEARL, 7),

								// Rune of Holding
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsPlayerModule.RUNE_OF_HOLDING, 20),

								// Special
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsSheep.ITEM_STACK, 8),
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsIceBridge.ITEM_STACK, 10),
								new BedwarsShopItem(BedwarsShopItemType.OTHER, BedwarsSafeTeleport.ITEM_STACK, 10),

								// Traps
								new BedwarsTNTTrap(8),
								new BedwarsBearTrap(8)
						);
			default:
				return super.generateItems(resource);
		}
	}

	@Override
	public List<BedwarsSpecialItem> generateSpecialItems()
	{
		List<BedwarsSpecialItem> items = super.generateSpecialItems();
		items.add(new BedwarsIceBridge(this));
		items.add(new BedwarsSafeTeleport(this));
		return items;
	}

	@Override
	public void generateChests()
	{
		_chestLootModule.registerChestType(BedwarsIslandModule.CHEST_TYPE, new ArrayList<>(),

				new ChestLootPool()
						.addItem(new ItemBuilder(Material.DIAMOND_SWORD)
								.addEnchantment(Enchantment.KNOCKBACK, 1)
								.setUnbreakable(true)
								.build())
						.addItem(new ItemBuilder(Material.BOW)
								.addEnchantment(Enchantment.PUNCH, 1)
								.addEnchantment(Enchantment.INFINITY, 1)
								.build())
						.addItem(new ItemBuilder(Material.GOLDEN_PICKAXE)
								.setTitle(C.cGold + C.Bold + "The Golden Pickaxe")
								.setUnbreakable(true)
								.build())
						.addItem(BedwarsShopModule.ENDER_PEARL, 10, 20)
						.addItem(BedwarsWall.ITEM_STACK, 10, 20)
						.addItem(BedwarsIceBridge.ITEM_STACK, 2, 4)
						.addItem(BedwarsSafeTeleport.ITEM_STACK, 2, 4)
						.addItem(BedwarsSheep.ITEM_STACK, 2, 4)
						.addItem(new ItemStack(Material.GOLDEN_APPLE), 5, 10)

		).destroyAfterOpened(30);
	}

	@Override
	public BedwarsResourcePage getShopPage(BedwarsResource resource, Player player)
	{
		if (resource == BedwarsResource.STAR)
		{
			return new BedwarsResourcePage(getArcadeManager(), getBedwarsShopModule().getShop(), player, 27, BedwarsResource.STAR, Collections.emptyList())
			{
				@Override
				protected void buildPage()
				{
					super.buildPage();

					addButton(13, new ItemBuilder(Material.SUGAR)
							.setTitle(C.cRed + C.Bold + "Sugar Buff")
							.addLore(
									"",
									"Capture the center beacon to earn",
									"your team the following buffs:",
									"",
									" - " + C.cGreen + "Speed I" + C.cGray + " Potion Buff",
									" - +" + C.cGreen + "25%" + C.cGray + " Damage",
									" - +" + C.cGreen + "25%" + C.cGray + " Knockback"
									)
							.build(), (p, c) -> {});
				}
			};
		}

		return super.getShopPage(resource, player);
	}

	@Override
	public String GetMode()
	{
		return "Sugar Rush";
	}

	private boolean hasSugarBuff(Player player)
	{
		return player != null && player.hasPotionEffect(PotionEffectType.SPEED);
	}
}
