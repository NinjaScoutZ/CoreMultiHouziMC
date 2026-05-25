package com.houzicore.shared.core.packethandler;

import java.util.HashSet;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.NautHashMap;

public class PacketHandler extends MiniPlugin {
	private final NautHashMap<Player, PacketVerifier> _playerVerifierMap = new NautHashMap<>();
	private final HashSet<IPacketHandler> _packetHandlers = new HashSet<>();

	public PacketHandler(JavaPlugin plugin) {
		super("PacketHandler", plugin);
	}

	public void addPacketHandler(IPacketHandler packetHandler) {
		_packetHandlers.add(packetHandler);
		for (final PacketVerifier verifier : _playerVerifierMap.values()) {
			verifier.addPacketHandler(packetHandler);
		}
	}

	public HashSet<IPacketHandler> getPacketHandlers() {
		return _packetHandlers;
	}

	public PacketVerifier getPacketVerifier(Player player) {
		return _playerVerifierMap.get(player);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		_playerVerifierMap.put(event.getPlayer(), new PacketVerifier(event.getPlayer()));
		for (final IPacketHandler packetHandler : _packetHandlers) {
			_playerVerifierMap.get(event.getPlayer()).addPacketHandler(packetHandler);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		_playerVerifierMap.remove(event.getPlayer()).clearHandlers();
	}

	public void removePacketHandler(IPacketHandler packetHandler) {
		_packetHandlers.remove(packetHandler);
		for (final PacketVerifier verifier : _playerVerifierMap.values()) {
			verifier.removePacketHandler(packetHandler);
		}
	}
}
