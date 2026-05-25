package com.houzicore.arcade.nautilus.game.arcade.game.games.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.enchantments.Enchantment;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.SoloGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.arena.events.PlayerChangeArenaEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.arena.events.RoundStartEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.arena.kits.KitGladiator;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ArenaGame extends SoloGame {

	private ArrayList<ArenaNode> allArenas;
	private ArrayList<ArenaNode> gameArenaSet;
	private HashMap<Player, ArenaNode> playerArenas;

	private RoundState roundState;
	private ArenaType furthestOutCurrent;

	public ArenaGame(ArcadeManager manager) {
		super(manager, GameType.Arena,
				new Kit[] { new KitGladiator(manager) },
				new String[] {
						"This is a 1v1 tournament!",
						"Kill and then run to the next arena!",
						"There is only one victor!"
				},
				new String[] {
						"นี่คือการประลองแบบ 1v1!",
						"ฆ่าคู่ต่อสู้แล้ววิ่งไปลานประลองถัดไป!",
						"ผู้ชนะมีเพียงหนึ่งเดียว!"
				});

		this.Damage = true;
		this.DamagePvP = true;
		this.DamageSelf = true;
		this.HungerSet = 20;

		this.BlockBreak = true;
		this.BlockPlace = true;

		this.playerArenas = new HashMap<>();
		this.roundState = RoundState.WAITING;
	}

	@Override
	public void ParseData() {
		parseArenas();
	}

	private void parseArenas() {
		allArenas = new ArrayList<>();

		for (Location mid : WorldData.GetDataLocs("RED")) allArenas.add(new ArenaNode(this, mid, ArenaType.RED));
		for (Location mid : WorldData.GetDataLocs("ORANGE")) allArenas.add(new ArenaNode(this, mid, ArenaType.ORANGE));
		for (Location mid : WorldData.GetDataLocs("YELLOW")) allArenas.add(new ArenaNode(this, mid, ArenaType.YELLOW));
		for (Location mid : WorldData.GetDataLocs("GREEN")) allArenas.add(new ArenaNode(this, mid, ArenaType.GREEN));

		for (ArenaNode a : allArenas) {
			if (a.getType() == ArenaType.GREEN) continue;

			ArrayList<Location> possible = new ArrayList<>(getAllArenaMidsOfType(getPreviousColour(a.getType())));
			a.setChild(0, getArenaByMid(UtilAlg.findClosest(a.getMid(), possible)));
			possible.remove(a.getChildAt(0).getMid());
			a.setChild(1, getArenaByMid(UtilAlg.findClosest(a.getMid(), possible)));
		}
	}

	protected void findGameArenaSet() {
		gameArenaSet = new ArrayList<>();
		GetTeamList().get(0).GetSpawns().clear();

		int neededSpawns = Math.min(GetPlayers(true).size(), 16);
		if (getArenasOfType(ArenaType.RED).isEmpty()) return;
		ArenaNode masterNode = getArenasOfType(ArenaType.RED).get(0);

		HashMap<ArenaNode, Integer> spawnsPerRoom = new HashMap<>();
		Queue<ArenaNode> queue = new LinkedList<>();
		Queue<ArenaNode> nextQueue = new LinkedList<>();
		queue.add(masterNode);

		int sum;
		boolean solved = false;

		while (!queue.isEmpty() && !solved) {
			sum = 0;
			ArrayList<ArenaNode> currentNodes = new ArrayList<>();
			while (!queue.isEmpty()) currentNodes.add(queue.poll());

			for (ArenaNode node : currentNodes) {
				sum += node.getCapacity();
				node.setUsed(true);
			}

			if (sum >= neededSpawns) {
				solved = true;
			} else {
				for (ArenaNode node : currentNodes) {
					for (int i = 0; i < node.getChildren().length; i++) {
						if (node.getChildAt(i) != null) {
							nextQueue.add(node.getChildAt(i));
							queue.add(node.getChildAt(i));
						}
					}
				}

				while (!nextQueue.isEmpty()) {
					ArenaNode node = nextQueue.poll();
					node.setUsed(true);
					sum = sum + node.getCapacity() - 1;

					if (sum >= neededSpawns) {
						solved = true;
						break;
					}
				}
			}

			if (solved) {
				masterNode.getUsageMap(spawnsPerRoom);
				for (Map.Entry<ArenaNode, Integer> entry : spawnsPerRoom.entrySet()) {
					gameArenaSet.add(entry.getKey());
				}
			}
		}

		for (ArenaNode a : gameArenaSet) {
			if (a.getCapacity() <= 0) continue;
			for (Location l : a.capacitySpawns()) {
				GetTeamList().get(0).GetSpawns().add(l);
			}
		}
	}

	@EventHandler
	public void setups(GameStateChangeEvent e) {
		if (e.GetState() == GameState.Live) {
			for (Player p : GetPlayers(true)) {
				Location closest = UtilAlg.findClosest(p.getLocation(), getAllArenaMids());
				ArenaNode arena = getArenaByMid(closest);
				if (arena != null) {
					arena.getPastPlayers().add(p);
					playerArenas.put(p, arena);
					giveLoadout(p, arena.getType());
				}
			}

			for (ArenaNode a : gameArenaSet) {
				if (!a.getPastPlayers().isEmpty()) {
					a.setState(ArenaState.WAITING);
				}
			}
		} else if (e.GetState() == GameState.Prepare) {
			findGameArenaSet();
		}
	}

	@EventHandler
	public void helpMessage(PlayerPrepareTeleportEvent e) {
		boolean isThai = LangManager.get().isThai(e.GetPlayer());
		UtilTextMiddle.display(isThai ? "§aประลองเดือด!" : "§aArena!", isThai ? "§aเอาชนะเพื่อเข้ารอบ" : "§aDefeat your opponent to advance", 20, 20 * 7, 20, e.GetPlayer());
	}

	@EventHandler
	public void roundUpdateCheck(UpdateEvent e) {
		if (!IsLive() || e.getType() != UpdateType.TICK) return;

		for (ArenaNode a : gameArenaSet) {
			if (a.getState() == ArenaState.FIGHTING) {
				if (a.getPastPlayers().size() == 1) {
					Player winner = a.getPastPlayers().get(0);
					ArenaNode nextArena = a.getParent();
					
					a.setState(ArenaState.ENDED);
					
					if (nextArena != null) {
						playerArenas.put(winner, nextArena);
						nextArena.getPastPlayers().add(winner);
						nextArena.setState(ArenaState.WAITING);
						nextArena.setDoBye(true);
						
						winner.teleport(UtilAlg.findClosest(winner.getLocation(), nextArena.getSpawns()));
						giveLoadout(winner, nextArena.getType());
						winner.setHealth(winner.getMaxHealth());
						boolean isThai = LangManager.get().isThai(winner);
						UtilTextMiddle.display(isThai ? "§aชนะ!" : "§aVictory!", isThai ? "คุณได้เข้าสู่รอบต่อไป!" : "You advanced to the next round!", 10, 60, 10, winner);
					}
				}
			}
		}

		if (roundState == RoundState.WAITING) {
			roundState = RoundState.STARTING_5;
			furthestOutCurrent = getFurthestOut();

			for (ArenaNode a : gameArenaSet) {
				if (a.getState() == ArenaState.WAITING && a.getType() == furthestOutCurrent) {
					a.setState(ArenaState.FIGHTING);
					a.setStateTime(System.currentTimeMillis());
				} else if (a.getState() == ArenaState.WAITING) {
					for (Player p : a.getPastPlayers()) {
						boolean isThai = LangManager.get().isThai(p);
						p.sendMessage(F.main("Arena", isThai ? "คุณได้ผ่านเข้ารอบโดยอัตโนมัติ (ได้บาย)!" : "You have a bye! You automatically advance."));
					}
				}
			}
			Manager.getPluginManager().callEvent(new RoundStartEvent());
		} else if (roundState == RoundState.FIGHTING) {
			for (ArenaNode a : gameArenaSet) {
				if (!(a.getState() == ArenaState.WAITING || a.getState() == ArenaState.ENDED || a.getState() == ArenaState.EMPTY)) {
					return;
				}
			}
			roundState = RoundState.WAITING;
		}
	}

	protected void giveLoadout(Player p, ArenaType type) {
		if (!GetPlayers(true).contains(p)) return;
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);

		p.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).setUnbreakable(true).addEnchantment(Enchantment.SHARPNESS, 2).build());
		p.getInventory().setItem(1, new ItemBuilder(Material.BOW).setUnbreakable(true).addEnchantment(Enchantment.POWER, 2).build());
		p.getInventory().setItem(2, new ItemBuilder(Material.FISHING_ROD).setUnbreakable(true).build());
		p.getInventory().setItem(3, new org.bukkit.inventory.ItemStack(Material.GOLDEN_APPLE, 6));
		p.getInventory().setItem(4, new ItemBuilder(Material.DIAMOND_AXE).setUnbreakable(true).build());
		p.getInventory().setItem(5, new org.bukkit.inventory.ItemStack(Material.OAK_PLANKS, 64));
		p.getInventory().setItem(8, new org.bukkit.inventory.ItemStack(Material.ARROW, 16));

		p.getInventory().setHelmet(new ItemBuilder(Material.DIAMOND_HELMET).setUnbreakable(true).addEnchantment(Enchantment.PROTECTION, 2).build());
		p.getInventory().setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE).setUnbreakable(true).addEnchantment(Enchantment.PROTECTION, 2).build());
		p.getInventory().setLeggings(new ItemBuilder(Material.DIAMOND_LEGGINGS).setUnbreakable(true).addEnchantment(Enchantment.PROTECTION, 2).build());
		p.getInventory().setBoots(new ItemBuilder(Material.IRON_BOOTS).setUnbreakable(true).addEnchantment(Enchantment.PROTECTION, 2).build());

		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
	}

	// Helpers
	public ArenaNode getArenaByMid(Location mid) {
		for (ArenaNode a : allArenas) {
			if (a.getMid().equals(mid)) return a;
		}
		return null;
	}

	public ArrayList<ArenaNode> getArenasOfType(ArenaType type) {
		ArrayList<ArenaNode> arenas = new ArrayList<>();
		if (allArenas == null) return arenas;
		for (ArenaNode a : allArenas) {
			if (a.getType() == type) arenas.add(a);
		}
		return arenas;
	}

	public ArrayList<Location> getAllArenaMids() {
		ArrayList<Location> mids = new ArrayList<>();
		if (allArenas == null) return mids;
		for (ArenaNode a : allArenas) mids.add(a.getMid());
		return mids;
	}

	public ArrayList<Location> getAllArenaMidsOfType(ArenaType type) {
		ArrayList<Location> mids = new ArrayList<>();
		for (ArenaNode a : allArenas) {
			if (a.getType() == type) mids.add(a.getMid());
		}
		return mids;
	}

	public ArenaType getPreviousColour(ArenaType old) {
		switch (old) {
			case RED: return ArenaType.ORANGE;
			case ORANGE: return ArenaType.YELLOW;
			case YELLOW: return ArenaType.GREEN;
			default: return null;
		}
	}

	private ArenaType getFurthestOut() {
		ArenaType best = null;
		for (ArenaNode a : gameArenaSet) {
			if (a.getState() != ArenaState.WAITING) continue;
			if (best == null || a.getType().furtherOut(best)) best = a.getType();
		}
		return best;
	}

	public RoundState getRoundState() {
		return roundState;
	}

	private org.bukkit.inventory.ItemStack setUnbreakable(org.bukkit.inventory.ItemStack item) {
		if (item == null) return null;
		org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setUnbreakable(true);
			item.setItemMeta(meta);
		}
		return item;
	}
}
