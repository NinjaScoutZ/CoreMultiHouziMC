package com.houzicore.shared.core.gadget.types;

import java.util.ArrayList;
import java.util.Iterator;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.event.GadgetActivateEvent;
import com.houzicore.shared.core.gadget.event.GadgetBlockEvent;
import com.houzicore.shared.core.gadget.gadgets.SongData;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.common.util.UtilMath;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class MusicGadget extends Gadget {
	private final Material _material;
	private final long _duration;

	private final ArrayList<SongData> _songs = new ArrayList<>();

	@SuppressWarnings("deprecation")
	private static Material idToMaterial(int id) {
		for (Material m : Material.values()) {
			if (!m.isLegacy() && m.ordinal() == id) return m;
		}
		return Material.AIR;
	}

	public MusicGadget(GadgetManager manager, String name, String[] desc, int cost, int id, long duration) {
		super(manager, GadgetType.MusicDisc, name, desc, cost, idToMaterial(id), (byte) 0);

		_material = idToMaterial(id);
		_duration = duration;
	}

	public MusicGadget(GadgetManager manager, String name, String[] desc, int cost, Material material, long duration) {
		super(manager, GadgetType.MusicDisc, name, desc, cost, material, (byte) 0);

		_material = material;
		_duration = duration;
	}

	public boolean canPlayAt(Location location) {
		if (!_songs.isEmpty())
			return false;

		// for (SongData data : _songs)
		// {
		// if (UtilMath.offset(data.Block.getLocation(), location) < 48)
		// {
		// return false;
		// }
		// }

		return true;
	}

	@Override
	public void DisableCustom(Player player) {

	}

	@Override
	public void Enable(Player player) {
		final GadgetActivateEvent gadgetEvent = new GadgetActivateEvent(player, this);
		Bukkit.getServer().getPluginManager().callEvent(gadgetEvent);

		if (gadgetEvent.isCancelled()) {
			UtilPlayer.message(player, F.main("Inventory", GetName() + " is not enabled."));
			return;
		}

		if (_active.contains(player)) {
			UtilPlayer.message(player, F.main("Music", com.houzicore.shared.core.lang.LangManager.get().get(player, "music.already_playing")));
			return;
		}

		// Near Portal
		for (final Block block : UtilBlock.getInRadius(player.getLocation(), 3).keySet()) {
			Material type = block.getType();
			if ((type == Material.NETHER_PORTAL || type == Material.END_PORTAL || type == Material.END_GATEWAY)
					&& UtilMath.offset(player.getLocation(), block.getLocation()) < 8) {
				UtilPlayer.message(player, F.main("Music", com.houzicore.shared.core.lang.LangManager.get().get(player, "music.near_portal")));
				return;
			}
		}

		// Invalid Location
		final Block block = player.getLocation().getBlock();
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (!UtilBlock.solid(block.getRelative(x, -1, z))) {
					UtilPlayer.message(player, F.main("Music", com.houzicore.shared.core.lang.LangManager.get().get(player, "music.cannot_place")));
					return;
				}
			}
		}

		// Near Parkour
		final ArrayList<Block> blocks = new ArrayList<>();
		blocks.add(block);
		final GadgetBlockEvent gadgetBlockEvent = new GadgetBlockEvent(this, blocks);
		Bukkit.getServer().getPluginManager().callEvent(gadgetBlockEvent);

		if (gadgetEvent.isCancelled()) {
			UtilPlayer.message(player, F.main("Music", com.houzicore.shared.core.lang.LangManager.get().get(player, "music.cannot_place")));
			return;
		}

		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);

		_songs.add(new SongData(player.getLocation().getBlock(), _duration));
		Sound discSound = getDiscSound();
		if (discSound != null) {
			player.getWorld().playSound(player.getLocation(), discSound, 3.0f, 1.0f);
		}
	}

	@Override
	public void EnableCustom(Player player) {

	}

	@EventHandler
	public void gadgetBlockChange(GadgetBlockEvent event) {
		for (final Iterator<Block> iterator = event.getBlocks().iterator(); iterator.hasNext();) {
			final Block block = iterator.next();

			for (final SongData data : _songs) {
				if (data.Block.equals(block)) {
					iterator.remove();
					break;
				}
			}
		}
	}

	@EventHandler
	public void Update(UpdateEvent event) {
		if (event.getType() != UpdateType.FASTER)
			return;

		final Iterator<SongData> songIterator = _songs.iterator();

		while (songIterator.hasNext()) {
			final SongData song = songIterator.next();

			if (song.update()) {
				songIterator.remove();
			}
		}
	}

	private Sound getDiscSound() {
		switch (_material) {
			case MUSIC_DISC_13: return Sound.MUSIC_DISC_13;
			case MUSIC_DISC_CAT: return Sound.MUSIC_DISC_CAT;
			case MUSIC_DISC_BLOCKS: return Sound.MUSIC_DISC_BLOCKS;
			case MUSIC_DISC_CHIRP: return Sound.MUSIC_DISC_CHIRP;
			case MUSIC_DISC_FAR: return Sound.MUSIC_DISC_FAR;
			case MUSIC_DISC_MALL: return Sound.MUSIC_DISC_MALL;
			case MUSIC_DISC_MELLOHI: return Sound.MUSIC_DISC_MELLOHI;
			case MUSIC_DISC_STAL: return Sound.MUSIC_DISC_STAL;
			case MUSIC_DISC_STRAD: return Sound.MUSIC_DISC_STRAD;
			case MUSIC_DISC_WARD: return Sound.MUSIC_DISC_WARD;
			case MUSIC_DISC_11: return Sound.MUSIC_DISC_11;
			case MUSIC_DISC_WAIT: return Sound.MUSIC_DISC_WAIT;
			case MUSIC_DISC_PIGSTEP: return Sound.MUSIC_DISC_PIGSTEP;
			case MUSIC_DISC_OTHERSIDE: return Sound.MUSIC_DISC_OTHERSIDE;
			case MUSIC_DISC_5: return Sound.MUSIC_DISC_5;
			case MUSIC_DISC_RELIC: return Sound.MUSIC_DISC_RELIC;
			case MUSIC_DISC_CREATOR: return Sound.MUSIC_DISC_CREATOR;
			case MUSIC_DISC_PRECIPICE: return Sound.MUSIC_DISC_PRECIPICE;
			default: return null;
		}
	}
}
