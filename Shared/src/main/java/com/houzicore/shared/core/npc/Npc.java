package com.houzicore.shared.core.npc;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import com.houzicore.shared.database.tables.records.NpcsRecord;
import org.bukkit.entity.LivingEntity;
import com.houzicore.shared.common.util.C;

public class Npc {
	private final NpcManager _npcManager;
	private final NpcsRecord _databaseRecord;
	private final Location _location;
	private final String _displayName;
	private final String _identityKey;
	private LivingEntity _entity;
	private int _failedAttempts = 0;
	private boolean _returning = false;
	private final String[] _info;
	private final Double _infoRadiusSquared;

	private com.houzicore.shared.core.hologram.Hologram _hologram;
	private String _extraHologramLine;

	public Npc(NpcManager npcManager, NpcsRecord databaseRecord) {
		_npcManager = npcManager;
		_databaseRecord = databaseRecord;

		_location = new Location(Bukkit.getWorld(getDatabaseRecord().getWorld()), getDatabaseRecord().getX(),
				getDatabaseRecord().getY(), getDatabaseRecord().getZ());
		_displayName = getDatabaseRecord().getName() == null ? null : translateDisplayText(getDatabaseRecord().getName());
		_identityKey = buildIdentityKey();

		if (getDatabaseRecord().getInfo() == null) {
			_info = null;
		} else {
			final String[] info = getDatabaseRecord().getInfo().split("\\r?\\n");

			for (int i = 0; i < info.length; i++) {
				info[i] = translateDisplayText(info[i]);
			}

			_info = new String[info.length + 2];

			for (int i = 0; i < _info.length; i++) {
				if (i == 0 || i == _info.length - 1) {
					_info[i] = C.cGold + C.Strike + "=============================================";
				} else {
					_info[i] = info[i - 1];
				}
			}
		}

		if (getDatabaseRecord().getInfoRadius() == null) {
			_infoRadiusSquared = null;
		} else {
			_infoRadiusSquared = getDatabaseRecord().getInfoRadius() * getDatabaseRecord().getInfoRadius();
		}
	}

	public void clearGoals() {
		if (_entity instanceof org.bukkit.entity.Creature) {
			_returning = false;

			final Location entityLocation = _entity.getLocation();
			((org.bukkit.entity.Mob) _entity).getPathfinder().moveTo(entityLocation, .8d);
		}
	}

	public Chunk getChunk() {
		return getLocation().getChunk();
	}

	public NpcsRecord getDatabaseRecord() {
		return _databaseRecord;
	}

	public String getDisplayName() {
		return _displayName;
	}

	public LivingEntity getEntity() {
		return _entity;
	}

	public int getFailedAttempts() {
		return _failedAttempts;
	}

	public String[] getInfo() {
		return _info;
	}

	public Double getInfoRadiusSquared() {
		return _infoRadiusSquared;
	}

	public String getIdentityKey() {
		return _identityKey;
	}

	public Location getLocation() {
		return _location;
	}

	public NpcManager getNpcManager() {
		return _npcManager;
	}

	public double getRadius() {
		return getDatabaseRecord().getRadius();
	}

	public int incrementFailedAttempts() {
		return ++_failedAttempts;
	}

	public boolean isInRadius(Location location) {
		if (location.getWorld() != getLocation().getWorld())
			return false;

		return location.distanceSquared(getLocation()) <= getRadius() * getRadius();
	}

	public boolean isReturning() {
		return _returning;
	}

	public void returnToPost() {
		if (_entity instanceof org.bukkit.entity.Creature) {
			((org.bukkit.entity.Mob) _entity).getPathfinder().moveTo(getLocation(), .8d);

			_returning = true;
		}
	}

	public void setEntity(LivingEntity entity) {
		if (_entity != null) {
			getNpcManager()._npcMap.remove(_entity.getUniqueId());
		}

		_entity = entity;

		if (_entity != null) {
			getNpcManager()._npcMap.put(_entity.getUniqueId(), this);
		}

		if (entity != null && _hologram != null) {
			_hologram.setFollowEntity(entity);
		}
	}

	public void setFailedAttempts(int failedAttempts) {
		_failedAttempts = failedAttempts;
	}

	public void setHologram(com.houzicore.shared.core.hologram.Hologram hologram) {
		if (_hologram != null) _hologram.stop();
		_hologram = hologram;
	}

	public com.houzicore.shared.core.hologram.Hologram getHologram() {
		return _hologram;
	}

	public String getExtraHologramLine() {
		return _extraHologramLine;
	}

	public void setExtraHologramLine(String line) {
		_extraHologramLine = line;
	}

	private String buildIdentityKey() {
		if (_displayName != null && !_displayName.isEmpty()) {
			return ChatColor.stripColor(_displayName);
		}

		return String.format(Locale.ROOT, "npc@%s:%.2f:%.2f:%.2f",
				getDatabaseRecord().getWorld(),
				getDatabaseRecord().getX(),
				getDatabaseRecord().getY(),
				getDatabaseRecord().getZ());
	}

	private static String translateDisplayText(String text) {
		String translated = text;
		for (final ChatColor color : ChatColor.values()) {
			translated = translated.replace("(" + color.name().toLowerCase() + ")", color.toString());
		}
		return ChatColor.translateAlternateColorCodes('&', translated);
	}
}
