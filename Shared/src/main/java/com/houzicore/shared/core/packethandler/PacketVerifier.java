package com.houzicore.shared.core.packethandler;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class PacketVerifier {
	private Player _owner;

	private final List<IPacketHandler> _packetHandlers = new ArrayList<>();

	public PacketVerifier(Player owner) {
		_owner = owner;
	}

	public void addPacketHandler(IPacketHandler packetHandler) {
		_packetHandlers.add(packetHandler);
	}

	public void bypassProcess(Object packet) {
        // NMS bypass process removed
	}

	public void clearHandlers() {
		_packetHandlers.clear();
	}

	public void Deactivate() {
		_owner = null;
	}

	public void process(Object packet) {
        // NMS packet send removed
	}

	public void removePacketHandler(IPacketHandler packetHandler) {
		_packetHandlers.remove(packetHandler);
	}

	public boolean verify(Object o) {
		final PacketInfo packetInfo = new PacketInfo(_owner, o, this);

		for (final IPacketHandler handler : _packetHandlers) {
			handler.handle(packetInfo);
		}

		return !packetInfo.isCancelled();
	}
}
