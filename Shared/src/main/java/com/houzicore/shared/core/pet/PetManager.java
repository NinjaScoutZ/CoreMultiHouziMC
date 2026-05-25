package com.houzicore.shared.core.pet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import com.houzicore.shared.MiniClientPlugin;
import com.houzicore.shared.core.pet.repository.PetRepository;
import com.houzicore.shared.core.pet.types.CustomWither;
import com.houzicore.shared.core.cosmetic.CosmeticProgression;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.donation.DonationManager;
// Replaced CustomWither and Navigation with Paper Native API

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import com.google.gson.Gson;
// CraftWorld unused
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PetManager extends MiniClientPlugin<PetClient> {
	private static Object _petOwnerSynch = new Object();
	private static Object _petRenameSynch = new Object();

	private final com.houzicore.shared.core.creature.Creature _creatureModule;
	private final PetRepository _repository;
	private final PetFactory _petFactory;
	private final NautHashMap<String, Creature> _activePetOwners;
	private final NautHashMap<String, Integer> _failedAttempts;
	private final NautHashMap<String, EntityType> _suspendedPets = new NautHashMap<>();

	private final NautHashMap<String, EntityType> _petOwnerQueue = new NautHashMap<>();
	private final NautHashMap<String, String> _petRenameQueue = new NautHashMap<>();
	private final DonationManager _donationManager;
	private final CoreClientManager _clientManager;
	private final com.houzicore.shared.api.feature.FeatureGate _featureGate;

	private final NautHashMap<String, PetState> _petStates = new NautHashMap<>();
	private final NautHashMap<String, Location> _ownerLastLocation = new NautHashMap<>();
	private final NautHashMap<String, Long> _ownerIdleStart = new NautHashMap<>();
	private final NautHashMap<String, Long> _petActionBlockUntil = new NautHashMap<>();

	public PetManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager,
			DisguiseManager disguiseManager, com.houzicore.shared.core.creature.Creature creatureModule, BlockRestore restore,
			String webAddress, com.houzicore.shared.api.feature.FeatureGate featureGate) {
		super("Pet Manager", plugin);

		_creatureModule = creatureModule;
		_repository = new PetRepository(webAddress);
		_petFactory = new PetFactory(_repository);
		_donationManager = donationManager;
		_clientManager = clientManager;
		_featureGate = featureGate;

		_activePetOwners = new NautHashMap<>();
		_failedAttempts = new NautHashMap<>();
	}

	public void AddPetOwner(Player player, EntityType entityType, Location location) {
		if (_activePetOwners.containsKey(player.getName())) {
			if (_activePetOwners.get(player.getName()).getType() != entityType) {
				RemovePet(player, true);
			} else
				return;
		}

		Creature pet;

		if (entityType == EntityType.WITHER) {
			_creatureModule.SetForce(true);
			pet = (Creature) location.getWorld().spawn(location, org.bukkit.entity.Wither.class);
			pet.setSilent(true);
			_creatureModule.SetForce(false);

			final Entity silverfish = _creatureModule.SpawnEntity(location, EntityType.SILVERFISH);
			UtilEnt.Vegetate(silverfish, true);
			((LivingEntity) silverfish)
					.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
			pet.addPassenger(silverfish);
		} else {
			pet = (Creature) _creatureModule.SpawnEntity(location, entityType);
		}

		// Named Pet — enhanced with rarity-colored nametag
		String customName = Get(player).GetPets().get(entityType);
		boolean hasCustomName = customName != null && customName.length() > 0;
		String displayName = hasCustomName ? customName : getPetDisplayName(entityType);
		pet.setCustomNameVisible(true);
		pet.setCustomName(buildPetNametag(player.getName(), displayName, entityType));

		if (pet instanceof Zombie) {
			((Zombie) pet).setBaby(true);
			pet.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
			pet.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 0));
			UtilEnt.silence(pet, true);
		} else if (pet instanceof Villager) {
			((Villager) pet).setBaby();
			((Villager) pet).setAgeLock(true);
		}
		/*
		 * else if (pet instanceof Sheep) { DisguiseWither disguise = new
		 * DisguiseWither(pet); disguise.s(350);
		 * 
		 * _disguiseManager.disguise(disguise); UtilEnt.silence(pet, true); }
		 */

		_activePetOwners.put(player.getName(), pet);
		_failedAttempts.put(player.getName(), 0);

		if (pet instanceof Ageable) {
			((Ageable) pet).setBaby();
			((Ageable) pet).setAgeLock(true);
		}

		UtilEnt.Vegetate(pet);

		_petStates.put(player.getName(), PetState.FOLLOWING);
		_ownerLastLocation.put(player.getName(), player.getLocation());
		_ownerIdleStart.put(player.getName(), System.currentTimeMillis());
	}

	/**
	 * Build a rarity-colored nametag for a pet entity.
	 * Format: §8[§7Pet§8] {RarityColor}{Owner}'s {PetName}
	 * Inspired by Swofty HypixelSkyBlock pet display pattern.
	 */
	private String buildPetNametag(String ownerName, String petName, EntityType entityType) {
		CosmeticRarity rarity = CosmeticProgression.getPetRarity(petName);
		String rarityColor = rarity.getColor().toString();
		return org.bukkit.ChatColor.DARK_GRAY + "[" + org.bukkit.ChatColor.GRAY + "Pet"
				+ org.bukkit.ChatColor.DARK_GRAY + "] " + rarityColor + ownerName + "'s " + petName;
	}

	/**
	 * Get a display-friendly name from an EntityType (e.g. WITHER -> "Widder").
	 */
	private static String getPetDisplayName(EntityType type) {
		if (type == EntityType.WITHER) return "Widder";
		String name = type.name();
		return name.charAt(0) + name.substring(1).toLowerCase().replace("_", " ");
	}

	public void addPetOwnerToQueue(String playerName, EntityType entityType) {
		synchronized (_petOwnerSynch) {
			_petOwnerQueue.put(playerName, entityType);
		}
	}

	@Override
	protected PetClient AddPlayer(String player) {
		return new PetClient();
	}

	public void addRenamePetToQueue(String playerName, String petName) {
		synchronized (_petRenameSynch) {
			_petRenameQueue.put(playerName, petName);
		}
	}

	public void DisableAll() {
		for (final Player player : UtilServer.getPlayers()) {
			RemovePet(player, true);
		}
	}

	public Creature getActivePet(String name) {
		return _activePetOwners.get(name);
	}

	public PetFactory GetFactory() {
		return _petFactory;
	}

	public Creature GetPet(Player player) {
		return _activePetOwners.get(player.getName());
	}

	public Collection<Creature> getPets() {
		return _activePetOwners.values();
	}

	public PetRepository GetRepository() {
		return _repository;
	}

	public EntityType getActivePetType(Player player) {
		if (player == null) {
			return null;
		}

		Creature pet = _activePetOwners.get(player.getName());
		return pet == null ? null : pet.getType();
	}

	public boolean hasActivePet(String name) {
		return _activePetOwners.containsKey(name);
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Creature && _activePetOwners.containsValue((Creature) event.getEntity())) {
			if (event.getCause() == DamageCause.VOID) {
				String playerName = null;

				for (final Entry<String, Creature> entry : _activePetOwners.entrySet()) {
					if (entry.getValue() == event.getEntity()) {
						playerName = entry.getKey();
					}
				}

				if (playerName != null) {
					final Player player = Bukkit.getPlayerExact(playerName);

					if (player != null && player.isOnline()) {
						RemovePet(player, true);
					}
				}
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.getEntity() instanceof Creature && _activePetOwners.containsValue((Creature) event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPetInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Creature) {
			Creature pet = (Creature) event.getRightClicked();
			if (_activePetOwners.containsValue(pet)) {
				event.setCancelled(true); // Prevent default interactions

				Player player = event.getPlayer();
				if (pet.equals(_activePetOwners.get(player.getName()))) {
					if (player.isSneaking()) {
						// Petted
						pet.getWorld().spawnParticle(org.bukkit.Particle.HEART, pet.getLocation().add(0, 1.2, 0), 3, 0.4, 0.4, 0.4, 0);
						pet.getWorld().playSound(pet.getLocation(), org.bukkit.Sound.ENTITY_WOLF_PANT, 1F, 1.2F);
						
						// Block movement for 5 seconds to enjoy pets
						_petActionBlockUntil.put(player.getName(), System.currentTimeMillis() + 5000);
						_petStates.put(player.getName(), PetState.IDLE);
					} else {
						// Mount
						pet.addPassenger(player);
						_petStates.put(player.getName(), PetState.MOUNTED);
					}
				}
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		final Rank rank = _clientManager.Get(p).GetRank();
		if (rank == Rank.DIVINE || rank == Rank.ADMIN || rank == Rank.DEVELOPER || rank == Rank.OWNER) {
			_donationManager.Get(p.getName()).AddUnknownSalesPackagesOwned("Widder");
		}

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
			int accountId = _clientManager.Get(p).getAccountId();
			com.houzicore.shared.core.pet.repository.token.ClientPetToken token = _repository.LoadClientPets(accountId);
			Bukkit.getScheduler().runTask(getPlugin(), () -> {
				if (p.isOnline()) {
					Get(p).Load(token);
				}
			});
		});
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		RemovePet(event.getPlayer(), true);
		_suspendedPets.remove(event.getPlayer().getName());
	}

	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST)
			return;

		int xDiff;
		int yDiff;
		int zDiff;

		final Iterator<String> ownerIterator = _activePetOwners.keySet().iterator();

		while (ownerIterator.hasNext()) {
			final String playerName = ownerIterator.next();
			final Player owner = Bukkit.getPlayer(playerName);

			if (owner == null || !owner.isOnline()) {
				continue;
			}

			final Creature pet = _activePetOwners.get(playerName);
			final Location petSpot = pet.getLocation();
			final Location ownerSpot = owner.getLocation();

			// State and idle handling
			Location lastSpot = _ownerLastLocation.get(playerName);
			if (lastSpot == null || !lastSpot.getWorld().equals(ownerSpot.getWorld()) || lastSpot.distanceSquared(ownerSpot) > 0.1) {
				_ownerLastLocation.put(playerName, ownerSpot);
				_ownerIdleStart.put(playerName, System.currentTimeMillis());
				if ((_petStates.containsKey(playerName) ? _petStates.get(playerName) : PetState.FOLLOWING) == PetState.SLEEPING) {
					_petStates.put(playerName, PetState.FOLLOWING); // Wake up
				}
			}

			long idleTime = System.currentTimeMillis() - (_ownerIdleStart.containsKey(playerName) ? _ownerIdleStart.get(playerName) : System.currentTimeMillis());
			long blockTime = _petActionBlockUntil.containsKey(playerName) ? _petActionBlockUntil.get(playerName) : 0L;
			PetState currentState = _petStates.containsKey(playerName) ? _petStates.get(playerName) : PetState.FOLLOWING;

			if (owner.getVehicle() != null && owner.getVehicle().equals(pet)) {
				currentState = PetState.MOUNTED;
				_petStates.put(playerName, PetState.MOUNTED);
			} else if (currentState == PetState.MOUNTED) {
				currentState = PetState.FOLLOWING;
				_petStates.put(playerName, PetState.FOLLOWING);
			}

			if (currentState != PetState.MOUNTED) {
				if (System.currentTimeMillis() < blockTime) {
					currentState = PetState.IDLE;
				} else if (idleTime > 15000) {
					currentState = PetState.SLEEPING;
					_petStates.put(playerName, PetState.SLEEPING);
				} else if (idleTime > 2000) {
					currentState = PetState.IDLE;
				} else {
					currentState = PetState.FOLLOWING;
					_petStates.put(playerName, PetState.FOLLOWING);
				}
			}

			// Handle States Behaviors
			if (currentState == PetState.SLEEPING) {
				if (Math.random() > 0.4) {
					pet.getWorld().spawnParticle(org.bukkit.Particle.NOTE, pet.getLocation().add(0, 1.2, 0), 1, 0.4, 0.4, 0.4, 1);
				}
				if (pet instanceof org.bukkit.entity.Sittable) {
					((org.bukkit.entity.Sittable) pet).setSitting(true);
				}
				continue; // Skip movement
			} else if (currentState == PetState.IDLE) {
				if (pet instanceof org.bukkit.entity.Sittable) {
					((org.bukkit.entity.Sittable) pet).setSitting(false);
				}
				continue; // Wait for owner to move
			} else if (currentState == PetState.MOUNTED) {
				// Glide logic for flyers
				if (pet.getType() == EntityType.ENDER_DRAGON || pet.getType() == EntityType.PHANTOM || pet.getType() == EntityType.BAT || pet.getType() == EntityType.PARROT) {
					if (!pet.isOnGround()) {
						org.bukkit.util.Vector direction = owner.getLocation().getDirection().multiply(0.8);
						direction.setY(direction.getY() * 0.5 - 0.1); // Slow fall logic
						pet.setVelocity(direction);
					}
				}
				continue;
			}

			if (pet instanceof org.bukkit.entity.Sittable) {
				((org.bukkit.entity.Sittable) pet).setSitting(false);
			}

			xDiff = Math.abs(petSpot.getBlockX() - ownerSpot.getBlockX());
			yDiff = Math.abs(petSpot.getBlockY() - ownerSpot.getBlockY());
			zDiff = Math.abs(petSpot.getBlockZ() - ownerSpot.getBlockZ());

			if (xDiff + yDiff + zDiff > 4) {

				int xIndex = -1;
				int zIndex = -1;
				Block targetBlock = ownerSpot.getBlock().getRelative(xIndex, -1, zIndex);
				while (targetBlock.isEmpty() || targetBlock.isLiquid()) {
					if (xIndex < 2) {
						xIndex++;
					} else if (zIndex < 2) {
						xIndex = -1;
						zIndex++;
					} else
						return;

					targetBlock = ownerSpot.getBlock().getRelative(xIndex, -1, zIndex);
				}

				float speed = 0.9f;
				if (pet instanceof Villager) {
					speed = 0.6f;
				}

				if (_failedAttempts.get(playerName) > 4) {
					pet.teleport(owner);
					_failedAttempts.put(playerName, 0);
				} else if (!((org.bukkit.entity.Mob) pet).getPathfinder().moveTo(new Location(pet.getWorld(), targetBlock.getX(), targetBlock.getY() + 1, targetBlock.getZ()), speed)) {
					if (pet.getFallDistance() == 0) {
						_failedAttempts.put(playerName, _failedAttempts.get(playerName) + 1);
					}
				} else {
					_failedAttempts.put(playerName, 0);
				}
			}
		}
	}

	@EventHandler
	public void preventWolfBone(PlayerInteractEntityEvent event) {
		if (event.getPlayer().getItemInHand().getType() == Material.BONE) {
			event.setCancelled(true);
			event.getPlayer().updateInventory();
		}
	}

	@EventHandler
	public void processQueues(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		synchronized (_petOwnerSynch) {
			for (final String playerName : _petOwnerQueue.keySet()) {
				final Player player = Bukkit.getPlayerExact(playerName);

				if (player != null && player.isOnline()) {
					AddPetOwner(player, _petOwnerQueue.get(playerName), player.getLocation());
				}
			}

			_petOwnerQueue.clear();
		}

		synchronized (_petRenameQueue) {
			for (final String playerName : _petRenameQueue.keySet()) {
				final Player player = Bukkit.getPlayerExact(playerName);

				if (player != null && player.isOnline()) {
					Creature pet = getActivePet(playerName);
					if (pet != null) {
						String newName = _petRenameQueue.get(playerName);
						pet.setCustomNameVisible(true);
						pet.setCustomName(buildPetNametag(playerName, newName, pet.getType()));
					}
				}
			}

			_petRenameQueue.clear();
		}
	}

	public void RemovePet(final Player player, boolean removeOwner) {
		if (_activePetOwners.containsKey(player.getName())) {
			final Creature pet = _activePetOwners.get(player.getName());
			
			// Remove all passengers (e.g. Silverfish under Wither)
			if (!pet.getPassengers().isEmpty()) {
				for (org.bukkit.entity.Entity passenger : pet.getPassengers()) {
					passenger.remove();
				}
				pet.eject();
			}
			
			pet.remove();

			if (removeOwner) {
				_activePetOwners.remove(player.getName());
				_petStates.remove(player.getName());
				_ownerLastLocation.remove(player.getName());
				_ownerIdleStart.remove(player.getName());
				_petActionBlockUntil.remove(player.getName());
			}
		}
	}

	public void suspend(Player player) {
		String name = player.getName();
		if (!_activePetOwners.containsKey(name)) return;
		
		Creature pet = _activePetOwners.get(name);
		if (pet != null) {
			_suspendedPets.put(name, pet.getType());
			RemovePet(player, true);
		}
	}

	public void resume(Player player) {
		String name = player.getName();
		EntityType type = _suspendedPets.remove(name);
		if (type != null) {
			AddPetOwner(player, type, player.getLocation());
		}
	}
}
