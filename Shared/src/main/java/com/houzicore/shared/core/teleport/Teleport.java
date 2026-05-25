package com.houzicore.shared.core.teleport;

import java.util.LinkedList;
import java.util.UUID;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.teleport.command.LocateCommand;
import com.houzicore.shared.core.teleport.command.TeleportCommand;
import com.houzicore.shared.core.teleport.event.HouziTeleportEvent;
import com.houzicore.shared.core.teleport.redis.RedisLocate;
import com.houzicore.shared.core.teleport.redis.RedisLocateCallback;
import com.houzicore.shared.core.teleport.redis.RedisLocateHandler;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.account.event.ClientUnloadEvent;
import com.houzicore.shared.common.jsonchat.ChildJsonMessage;
import com.houzicore.shared.common.jsonchat.ClickEvent;
import com.houzicore.shared.common.jsonchat.HoverEvent;
import com.houzicore.shared.common.jsonchat.JsonMessage;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilWorld;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Teleport extends MiniPlugin {
	private final LinkedList<Teleporter> teleportList = new LinkedList<>();
	private final NautHashMap<String, LinkedList<Location>> _tpHistory = new NautHashMap<>();
	private final NautHashMap<UUID, BukkitRunnable> _failedRedisLocates = new NautHashMap<>();
	private final String _serverName;

	public Teleport(JavaPlugin plugin) {
		super("Teleport", plugin);

		_serverName = getPlugin().getConfig().getString("serverstatus.name");

		final RedisLocateHandler locateHandler = new RedisLocateHandler(this);

		ServerCommandManager.getInstance().registerCommandType("RedisLocate", RedisLocate.class, locateHandler);
		ServerCommandManager.getInstance().registerCommandType("RedisLocateCallback", RedisLocateCallback.class,
				locateHandler);
	}

	public void Add(Player pA, Location loc, String mA, boolean record, Player pB, String mB, String log) {
		teleportList.addLast(new Teleporter(this, pA, pB, mA, mB, loc, record, log));
	}

	@Override
	public void addCommands() {
		addCommand(new TeleportCommand(this));
		addCommand(new LocateCommand(this));
	}

	public LinkedList<Location> GetTPHistory(Player player) {
		return _tpHistory.get(player.getName());
	}

	public void handleLocateCallback(RedisLocateCallback callback) {
		final BukkitRunnable runnable = _failedRedisLocates.remove(callback.getUUID());

		if (runnable != null) {
			runnable.cancel();
		}

		final Player player = Bukkit.getPlayerExact(callback.getReceivingPlayer());

		if (player != null) {
			final ChildJsonMessage message = new JsonMessage("").extra(C.mHead + "Locate" + "> " + C.mBody + com.houzicore.shared.core.lang.LangManager.get().get(player, "locate.located") + " ["
					+ C.mElem + callback.getLocatedPlayer() + C.mBody + "] at ");

			message.add(C.cBlue + callback.getServer()).click(ClickEvent.RUN_COMMAND,
					"/server " + callback.getServer());

			message.hover(HoverEvent.SHOW_TEXT, "Teleport to " + callback.getServer());

			message.sendToPlayer(player);
		}
	}

	public void locatePlayer(final Player player, final String target) {
		final Player targetPlayer = Bukkit.getPlayerExact(target);

		if (targetPlayer != null) {
			UtilPlayer.message(player,
					F.main("Locate", C.mBody + com.houzicore.shared.core.lang.LangManager.get().get(player, "locate.same_server").replace("{0}", target)));
			return;
		}

		final RedisLocate locate = new RedisLocate(_serverName, player.getName(), target);
		final UUID uuid = locate.getUUID();

		final BukkitRunnable runnable = new BukkitRunnable() {

			@Override
			public void run() {
				_failedRedisLocates.remove(uuid);
				UtilPlayer.message(player,
						F.main("Locate", C.mBody + com.houzicore.shared.core.lang.LangManager.get().get(player, "locate.failed").replace("{0}", target)));
			}

		};

		_failedRedisLocates.put(uuid, runnable);
		runnable.runTaskLater(_plugin, 40);

		locate.publish();
	}

	public void playerToLoc(Player caller, String target, String sX, String sY, String sZ) {
		playerToLoc(caller, target, caller.getWorld().getName(), sX, sY, sZ);
	}

	public void playerToLoc(Player caller, String target, String world, String sX, String sY, String sZ) {
		final Player player = UtilPlayer.searchOnline(caller, target, true);

		if (player == null)
			return;

		try {
			int x = sX.matches(".*[0-9]") ? Integer.parseInt(sX.replace("~", "")) : 0;
			int y = sY.matches(".*[0-9]") ? Integer.parseInt(sY.replace("~", "")) : 0;
			int z = sZ.matches(".*[0-9]") ? Integer.parseInt(sZ.replace("~", "")) : 0;

			final Location pLoc = player.getLocation();

			if (sX.startsWith("~")) {
				x += pLoc.getBlockX();
			}

			if (sY.startsWith("~")) {
				y += pLoc.getBlockY();
			}

			if (sZ.startsWith("~")) {
				z += pLoc.getBlockZ();
			}

			final Location loc = new Location(Bukkit.getWorld(world), x, y, z);

			// Inform
			String mA = null;
			if (caller == player) {
				mA = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_you_to_loc").replace("{0}", UtilWorld.locToStrClean(loc)));
			} else {
				mA = F.main("Teleport",
						com.houzicore.shared.core.lang.LangManager.get().get(player, "teleport.tp_them_to_loc").replace("{0}", F.elem(caller.getName())).replace("{1}", UtilWorld.locToStrClean(loc)));
			}

			// Register
			Add(player, loc, mA, true, caller, null,
					player.getName() + " teleported to " + UtilWorld.locToStrClean(loc) + " via " + caller.getName());
		} catch (final Exception e) {
			UtilPlayer.message(caller, F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.invalid_loc").replace("{0}", sX).replace("{1}", sY).replace("{2}", sZ)));
			return;
		}
	}

	public void playerToPlayer(Player caller, String from, String to) {
		LinkedList<Player> listA = new LinkedList<>();

		// ALL
		if (from.equals("%ALL%")) {
			for (final Player cur : UtilServer.getPlayers()) {
				listA.add(cur);
				// Normal
			}
		} else {
			listA = UtilPlayer.matchOnline(caller, from, true);
		}

		// To
		final Player pB = UtilPlayer.searchOnline(caller, to, true);

		if (listA.isEmpty() || pB == null)
			return;

		if (listA.size() == 1) {
			final Player pA = listA.getFirst();

			String mA = null;
			String mB = null;

			// Inform
			if (pA.equals(caller)) {
				mA = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(pA, "teleport.tp_you_to_player").replace("{0}", F.elem(pB.getName())));
			} else if (pB.equals(caller)) {
				mA = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(pA, "teleport.tp_them_to_you").replace("{0}", F.elem(caller.getName())));
				mB = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_you_to_them").replace("{0}", F.elem(pA.getName())));
			} else {
				mA = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(pA, "teleport.tp_them_to_other").replace("{0}", F.elem(caller.getName())).replace("{1}", F.elem(pB.getName())));
				mB = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_you_to_other").replace("{0}", F.elem(pA.getName())).replace("{1}", F.elem(pB.getName())));
			}

			// Register
			Add(pA, pB.getLocation(), mA, true, caller, mB,
					pA.getName() + " teleported to " + pB.getName() + " via " + caller.getName());
			return;
		}

		boolean first = true;
		for (final Player pA : listA) {
			String mA = null;
			String mB = null;

			// Inform
			if (pA.equals(caller)) {
				mA = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(pA, "teleport.tp_you_to_player").replace("{0}", F.elem(pB.getName())));
			} else if (pB.equals(caller)) {
				mA = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(pA, "teleport.tp_them_to_you").replace("{0}", F.elem(caller.getName())));
				mB = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_you_to_them").replace("{0}", F.elem(listA.size() + " Players")));
			} else {
				mA = F.main("Teleport", com.houzicore.shared.core.lang.LangManager.get().get(pA, "teleport.tp_them_to_other").replace("{0}", F.elem(caller.getName())).replace("{1}", F.elem(pB.getName())));
				mB = F.main("Teleport",
						com.houzicore.shared.core.lang.LangManager.get().get(caller, "teleport.tp_you_to_other").replace("{0}", F.elem(listA.size() + " Players")).replace("{1}", F.elem(pB.getName())));
			}

			// Register
			if (first) {
				Add(pA, pB.getLocation(), mA, true, caller, mB,
						pA.getName() + " teleported to " + pB.getName() + " via " + caller.getName());
			} else {
				Add(pA, pB.getLocation(), mA, true, caller, null,
						pA.getName() + " teleported to " + pB.getName() + " via " + caller.getName());
			}

			first = false;
		}
	}

	public void TP(Player player, Location getLocation) {
		TP(player, getLocation, true);
	}

	public void TP(Player player, Location loc, boolean dettach) {
		// Event
		final HouziTeleportEvent event = new HouziTeleportEvent(player, loc);
		UtilServer.getServer().getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		if (dettach) {
			player.eject();
			player.leaveVehicle();
		}

		player.setFallDistance(0);
		player.setVelocity(new Vector(0, 0, 0));

		player.teleport(loc);
	}

	@EventHandler
	public void UnloadHistory(ClientUnloadEvent event) {
		_tpHistory.remove(event.GetName());
	}

	@EventHandler
	public void update(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		if (teleportList.isEmpty())
			return;

		teleportList.removeFirst().doTeleport();
	}
}
