package com.houzicore.shared.core.packethandler;

import org.bukkit.entity.Player;

public class PacketInfo {
	private final Player _player;
	private final Object _packet;
	private final PacketVerifier _verifier;

	private boolean _cancelled = false;

	public PacketInfo(Player player, Object packet, PacketVerifier verifier) {
		_player = player;
		_packet = packet;
		_verifier = verifier;
	}

	public Object getPacket() {
		return _packet;
	}

	public Player getPlayer() {
		return _player;
	}

	public PacketVerifier getVerifier() {
		return _verifier;
	}

	public boolean isCancelled() {
		return _cancelled;
	}

	public void setCancelled(boolean cancel) {
		_cancelled = cancel;
	}
}
