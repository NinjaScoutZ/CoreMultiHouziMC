package com.houzicore.shared.common.actionbar;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class ActionBarService
{
	private static final Map<UUID, Lease> LEASES = new ConcurrentHashMap<UUID, Lease>();

	private ActionBarService() {}

	public static boolean display(Player player, ActionBarChannel channel, Component component)
	{
		if (channel == null)
			return false;

		return display(player, channel, component, channel.getDefaultTtlMs());
	}

	public static boolean display(Player player, ActionBarChannel channel, Component component, long ttlMs)
	{
		if (player == null || channel == null || !player.isOnline())
			return false;

		long now = System.currentTimeMillis();
		UUID uuid = player.getUniqueId();
		Lease current = getCurrentLease(uuid, now, true);

		if (current != null && current.expiresAt > now && current.channel.getPriority() > channel.getPriority())
			return false;

		Component rendered = component == null ? Component.empty() : component;
		LEASES.put(uuid, new Lease(channel, now + Math.max(0, ttlMs), summarize(rendered)));
		player.sendActionBar(rendered);
		return true;
	}

	public static void clear(Player player)
	{
		clear(player, null);
	}

	public static void clear(Player player, ActionBarChannel channel)
	{
		if (player == null)
			return;

		UUID uuid = player.getUniqueId();
		Lease current = getCurrentLease(uuid, System.currentTimeMillis(), true);
		if (channel != null && current != null && current.expiresAt > System.currentTimeMillis()
				&& current.channel.getPriority() > channel.getPriority())
			return;

		LEASES.remove(uuid);
		if (player.isOnline())
			player.sendActionBar(Component.empty());
	}

	public static Snapshot snapshot(Player player)
	{
		if (player == null)
			return Snapshot.empty();

		return snapshot(player.getUniqueId());
	}

	public static Snapshot snapshot(UUID playerId)
	{
		if (playerId == null)
			return Snapshot.empty();

		long now = System.currentTimeMillis();
		Lease current = getCurrentLease(playerId, now, true);
		if (current == null)
			return Snapshot.empty();

		return new Snapshot(current.channel, Math.max(0L, current.expiresAt - now), current.preview);
	}

	private static Lease getCurrentLease(UUID playerId, long now, boolean pruneExpired)
	{
		Lease current = LEASES.get(playerId);
		if (current != null && pruneExpired && current.expiresAt <= now)
		{
			LEASES.remove(playerId, current);
			current = null;
		}

		return current;
	}

	private static String summarize(Component component)
	{
		String plain = PlainTextComponentSerializer.plainText().serialize(component == null ? Component.empty() : component);
		plain = plain.replace('\n', ' ').replace('\r', ' ').trim();
		if (plain.length() > 96)
			plain = plain.substring(0, 93) + "...";
		return plain;
	}

	public static final class Snapshot
	{
		private static final Snapshot EMPTY = new Snapshot(null, 0L, "");

		private final ActionBarChannel channel;
		private final long remainingTtlMs;
		private final String preview;

		private Snapshot(ActionBarChannel channel, long remainingTtlMs, String preview)
		{
			this.channel = channel;
			this.remainingTtlMs = remainingTtlMs;
			this.preview = preview == null ? "" : preview;
		}

		public static Snapshot empty()
		{
			return EMPTY;
		}

		public boolean hasActiveLease()
		{
			return channel != null;
		}

		public ActionBarChannel getChannel()
		{
			return channel;
		}

		public int getPriority()
		{
			return channel == null ? 0 : channel.getPriority();
		}

		public long getRemainingTtlMs()
		{
			return remainingTtlMs;
		}

		public String getPreview()
		{
			return preview;
		}
	}

	private static final class Lease
	{
		private final ActionBarChannel channel;
		private final long expiresAt;
		private final String preview;

		private Lease(ActionBarChannel channel, long expiresAt, String preview)
		{
			this.channel = channel;
			this.expiresAt = expiresAt;
			this.preview = preview;
		}
	}
}
