package com.houzicore.shared;

import java.util.Iterator;
import java.util.Map.Entry;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.packethandler.IPacketHandler;
import com.houzicore.shared.core.packethandler.PacketHandler;
import com.houzicore.shared.core.packethandler.PacketInfo;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Replay extends MiniPlugin implements IPacketHandler {
	private final NautHashMap<PacketInfo, Long> _packetList = new NautHashMap<>();
	private long _startTime = 0;
	private long _replayTime = 0;
	private boolean _replay = false;
	private final long _speed = 20;

	public Replay(JavaPlugin plugin, PacketHandler packetHandler) {
		super("Replay", plugin);
	}

	@Override
	public void handle(PacketInfo packetInfo) {
		if (_replay) {
			packetInfo.setCancelled(true);
			return;
		}

		if (_startTime == 0) {
			_startTime = System.currentTimeMillis();
		}

		_packetList.put(packetInfo, System.currentTimeMillis() - _startTime);

		// write out packets?
		if (packetInfo.isCancelled())
			return;
	}

	@EventHandler
	public void interact(PlayerInteractEvent event) {
		if (event.getItem().getType() == Material.COBWEB) {
			event.getPlayer().setItemInHand(new ItemStack(Material.STICK, 1));
			_replay = true;
			_replayTime = System.currentTimeMillis();
		}
	}

	@EventHandler
	public void replay(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK || !_replay)
			return;

		for (final Iterator<Entry<PacketInfo, Long>> entryIterator = _packetList.entrySet().iterator(); entryIterator
				.hasNext();) {
			final Entry<PacketInfo, Long> entry = entryIterator.next();

			if (System.currentTimeMillis() + _speed - _replayTime > entry.getValue()) {
				entry.getKey().getVerifier().bypassProcess(entry.getKey().getPacket());
				entryIterator.remove();
			}
		}
	}
}
