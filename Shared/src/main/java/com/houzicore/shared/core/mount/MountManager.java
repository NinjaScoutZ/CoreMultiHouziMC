package com.houzicore.shared.core.mount;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.core.cosmetic.CosmeticProgression;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.mount.types.*;

public class MountManager extends MiniPlugin {
	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;
	private final BlockRestore _blockRestore;
	private final DisguiseManager _disguiseManager;
	private final com.houzicore.shared.api.feature.FeatureGate _featureGate;

	private List<Mount<?>> _types;
	private final NautHashMap<Player, Mount<?>> _playerActiveMountMap = new NautHashMap<>();
	private final NautHashMap<Player, Mount<?>> _suspendedMounts = new NautHashMap<>();

	public MountManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager,
			BlockRestore blockRestore, DisguiseManager disguiseManager, com.houzicore.shared.api.feature.FeatureGate featureGate) {
		super("Mount Manager", plugin);

		_clientManager = clientManager;
		_donationManager = donationManager;
		_blockRestore = blockRestore;
		_disguiseManager = disguiseManager;
		_featureGate = featureGate;

		CreateGadgets();
	}

	private void CreateGadgets() {
		_types = new ArrayList<>();

		_types.add(new MountUndead(this));
		_types.add(new MountFrost(this));
		_types.add(new MountMule(this));
		_types.add(new MountDragon(this));
		_types.add(new MountSlime(this));
		_types.add(new MountCart(this));
		_types.add(new MountNightmareSteed(this));
		// _types.add(new MountSheep(this));
		_types.add(new MountBee(this));
		_types.add(new MountStrider(this));
		_types.add(new MountPhantom(this));

		for (Mount<?> mount : _types) {
			mount.setEssenceCost(CosmeticProgression.getPrice(CosmeticProgression.getShopRarity(mount)));
		}

		_types.sort(CosmeticProgression.mountComparator());
	}

	@EventHandler
	public void death(PlayerDeathEvent event) {
		_playerActiveMountMap.remove(event.getEntity());
	}

	// Disallows two mounts active
	public void DeregisterAll(Player player) {
		for (final Mount<?> mount : _types) {
			mount.Disable(player);
		}
	}

	public void DisableAll() {
		for (final Mount<?> mount : _types) {
			for (final Player player : UtilServer.getPlayers()) {
				mount.Disable(player);
			}
		}
	}

	public Mount<?> getActive(Player player) {
		return _playerActiveMountMap.get(player);
	}

	public BlockRestore getBlockRestore() {
		return _blockRestore;
	}

	public CoreClientManager getClientManager() {
		return _clientManager;
	}

	public DisguiseManager getDisguiseManager() {
		return _disguiseManager;
	}

	public DonationManager getDonationManager() {
		return _donationManager;
	}

	public List<Mount<?>> getMounts() {
		return _types;
	}

	public String getActiveMountName(Player player) {
		Mount<?> mount = _playerActiveMountMap.get(player);
		return mount == null ? null : mount.GetName();
	}

	public Mount<?> findMount(String name) {
		if (name == null) {
			return null;
		}

		for (Mount<?> mount : _types) {
			if (mount != null && mount.GetName().equalsIgnoreCase(name)) {
				return mount;
			}
		}

		return null;
	}

	@EventHandler
	public void HorseInteract(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Horse))
			return;

		boolean found = false;
		for (final Mount mount : _playerActiveMountMap.values()) {
			if (mount.GetActive().containsValue(event.getRightClicked())) {
				found = true;
				break;
			}
		}

		if (!found)
			return;

		final Player player = event.getPlayer();
		final Horse horse = (Horse) event.getRightClicked();

		if (horse.getOwner() == null || !horse.getOwner().equals(player)) {
			UtilPlayer.message(player, F.main("Mount", com.houzicore.shared.core.lang.LangManager.get().get(event.getPlayer(), "mount.not_yours")));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void LeashDropCancel(ItemSpawnEvent event) {
		if (event.getEntity().getItemStack().getType() == Material.LEAD) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void quit(PlayerQuitEvent event) {
		_playerActiveMountMap.remove(event.getPlayer());
		_suspendedMounts.remove(event.getPlayer());
	}

	public void removeActive(Player player) {
		_playerActiveMountMap.remove(player);
	}

	public void setActive(Player player, Mount<?> mount) {
		_playerActiveMountMap.put(player, mount);
	}

	@EventHandler
	public void onUpdate(com.houzicore.shared.updater.event.UpdateEvent event) {
		// Destructive continuous cleanup moved to Context-Driven explicit suspend/resume orchestration.
	}

	public void suspend(Player player) {
		Mount<?> mount = _playerActiveMountMap.get(player);
		if (mount != null) {
			_suspendedMounts.put(player, mount);
			mount.Disable(player);
		}
	}

	public void resume(Player player) {
		Mount<?> mount = _suspendedMounts.remove(player);
		if (mount != null) {
			mount.Enable(player);
		}
	}
}
