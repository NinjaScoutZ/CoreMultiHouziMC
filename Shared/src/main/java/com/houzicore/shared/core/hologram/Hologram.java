package com.houzicore.shared.core.hologram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.UtilPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Display.Billboard;

public class Hologram {

	public enum HologramTarget {
		BLACKLIST, WHITELIST;
	}

	private com.houzicore.shared.core.lifecycle.LifecycleOwner _owner;


	private TextDisplay _displayEntity;
	private Entity _followEntity;
	private final HologramManager _hologramManager;
	private String[] _hologramText = new String[0];
	private Location _location;
	private final HashSet<String> _playersInList = new HashSet<>();
	private boolean _removeEntityDeath;
	private HologramTarget _target = HologramTarget.BLACKLIST;
	private int _viewDistance = 70;
	protected Vector relativeToEntity;


	public Hologram(HologramManager hologramManager, Location location, String... text) {
		_hologramManager = hologramManager;
		_location = location.clone();
		setText(text);
	}

	/**
	 * Adds the player to the Hologram to be effected by Whitelist or Blacklist
	 */
	public Hologram addPlayer(Player player) {
		return addPlayer(player.getName());
	}

	/**
	 * Adds the player to the Hologram to be effected by Whitelist or Blacklist
	 */
	public Hologram addPlayer(String player) {
		_playersInList.add(player);
		return this;
	}

	/**
	 * Is there a player entry in the hologram for Whitelist and Blacklist
	 */
	public boolean containsPlayer(Player player) {
		return _playersInList.contains(player.getName());
	}

	/**
	 * Is there a player entry in the hologram for Whitelist and Blacklist
	 */
	public boolean containsPlayer(String player) {
		return _playersInList.contains(player);
	}

	protected List<Player> getNearbyPlayers() {
		final ArrayList<Player> nearbyPlayers = new ArrayList<>();

		for (final Player player : getLocation().getWorld().getPlayers()) {
			if (isVisible(player)) {
				nearbyPlayers.add(player);
			}
		}
		return nearbyPlayers;
	}

	public com.houzicore.shared.core.lifecycle.LifecycleOwner getOwner() {
		return _owner;
	}

	public Hologram setOwner(com.houzicore.shared.core.lifecycle.LifecycleOwner owner) {
		_owner = owner;
		return this;
	}


	public Entity getEntityFollowing() {
		return _followEntity;
	}

	/**
	 * Get who can see the hologram
	 *
	 * @Whitelist = Only people added can see the hologram
	 * @Blacklist = Anyone but people added can see the hologram
	 */
	public HologramTarget getHologramTarget() {
		return _target;
	}

	/**
	 * Get the hologram location
	 */
	public Location getLocation() {
		return _location.clone();
	}


	public TextDisplay getDisplayEntity() {
		return _displayEntity;
	}

	public HologramManager getManager() {
		return _hologramManager;
	}

	public org.bukkit.plugin.java.JavaPlugin getPlugin() {
		return _hologramManager.getPlugin();
	}

	protected List<Player> getPlayersTracking() {
		return new ArrayList<>(_location.getWorld().getPlayers());
	}


	public boolean isInUse() {
		return _displayEntity != null && _displayEntity.isValid();
	}


	/**
	 * Get the text in the hologram
	 */
	public String[] getText() {
		// We reverse it again as the hologram would otherwise display the text from the
		// bottom row to the top row
		final String[] reversed = new String[_hologramText.length];

		for (int i = 0; i < reversed.length; i++) {
			reversed[i] = _hologramText[reversed.length - (i + 1)];
		}

		return reversed;
	}

	/**
	 * Get the view distance the hologram is viewable from. Default is 70
	 */
	public int getViewDistance() {
		return _viewDistance;
	}


	public boolean isRemoveOnEntityDeath() {
		return _removeEntityDeath;
	}

	public boolean isVisible(Player player) {
		if (getLocation().getWorld() == player.getWorld()) {
			if (getHologramTarget() == HologramTarget.WHITELIST == containsPlayer(player)) {
				if (getLocation().distance(player.getLocation()) < getViewDistance())
					return true;
			}
		}

		return false;
	}

	private void updateEntity() {
		if (_displayEntity == null || !_displayEntity.isValid()) return;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < _hologramText.length; i++) {
			sb.append(_hologramText[i]);
			if (i < _hologramText.length - 1) sb.append("\n");
		}

		_displayEntity.text(LegacyComponentSerializer.legacySection().deserialize(sb.toString()));
	}


	/**
	 * Removes the player from the Hologram so they are no longer effected by
	 * Whitelist or Blacklist
	 */
	public Hologram removePlayer(Player player) {
		return removePlayer(player.getName());
	}

	/**
	 * Removes the player from the Hologram so they are no longer effected by
	 * Whitelist or Blacklist
	 */
	public Hologram removePlayer(String player) {
		_playersInList.remove(player);
		return this;
	}

	/**
	 * If the entity moves, the hologram will update its position to appear relative
	 * to the movement.
	 *
	 * @Please note the hologram updates every tick.
	 */
	public Hologram setFollowEntity(Entity entityToFollow) {
		_followEntity = entityToFollow;
		relativeToEntity = entityToFollow == null ? null
				: _location.clone().subtract(entityToFollow.getLocation()).toVector();

		return this;
	}

	/**
	 * Set who can see the hologram
	 *
	 * @Whitelist = Only people added can see the hologram
	 * @Blacklist = Anyone but people added can see the hologram
	 */
	public Hologram setHologramTarget(HologramTarget newTarget) {
		_target = newTarget;
		return this;
	}

	/**
	 * Sets the hologram to appear at this location
	 */
	public Hologram setLocation(Location newLocation) {
		_location = newLocation.clone();

		if (_displayEntity != null && _displayEntity.isValid()) {
			_displayEntity.teleport(_location);
		}

		if (getEntityFollowing() != null) {
			relativeToEntity = _location.clone().subtract(getEntityFollowing().getLocation()).toVector();
		}
		return this;
	}


	public Hologram setRemoveOnEntityDeath() {
		_removeEntityDeath = true;
		return this;
	}

	/**
	 * Set the hologram text
	 */
	public Hologram setText(String... newLines) {
		_hologramText = newLines;
		updateEntity();
		return this;
	}


	/**
	 * Set the distance the hologram is viewable from. Default is 70
	 */
	public Hologram setViewDistance(int newDistance) {
		_viewDistance = newDistance;
		return setLocation(getLocation());
	}

	/**
	 * Start the hologram
	 */
	public Hologram start() {
		if (!isInUse()) {
			_hologramManager.addHologram(this);

			_displayEntity = (TextDisplay) _location.getWorld().spawnEntity(_location, EntityType.TEXT_DISPLAY);
			_displayEntity.setPersistent(false); // Prevent saving to world
			_displayEntity.setBillboard(Billboard.CENTER);
			_displayEntity.setShadowed(true);
			_displayEntity.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0)); // Transparent background
			
			updateEntity();
		}
		return this;
	}


	/**
	 * Stop the hologram
	 */
	public Hologram stop() {
		if (isInUse()) {
			_hologramManager.removeHologram(this);
			_displayEntity.remove();
			_displayEntity = null;
		}
		return this;
	}


}
