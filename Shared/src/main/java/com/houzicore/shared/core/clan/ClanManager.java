package com.houzicore.shared.core.clan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.clan.command.ClanCommand;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.core.clan.redis.ClanChatCommand;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;

public class ClanManager extends MiniPlugin implements CommandCallback {

	private static ClanManager _instance;

	private final ClanRepository _repository;
	private final CoreClientManager _clientManager;
	
	private final Map<UUID, Clan> _playerClans = new HashMap<>();

	public ClanManager(JavaPlugin plugin, CoreClientManager clientManager) {
		super("Clan Manager", plugin);
		_instance = this;
		_clientManager = clientManager;
		_repository = new ClanRepository(plugin);
		
		ServerCommandManager.getInstance().registerCommandType(ClanChatCommand.class.getSimpleName(), ClanChatCommand.class, this);
	}

	public static ClanManager getInstance() {
		return _instance;
	}

	@Override
	public void addCommands() {
		addCommand(new ClanCommand(this));
	}
	
	@Override
	public void run(ServerCommand serverCommand) {
		if (!(serverCommand instanceof ClanChatCommand)) return;
		ClanChatCommand command = (ClanChatCommand) serverCommand;
		Bukkit.getScheduler().runTask(getPlugin(), () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				Clan clan = getClan(player);
				if (clan != null && clan.getId() == command.getClanId()) {
					player.sendMessage("§bClan> §f" + command.getSenderName() + " §b" + command.getMessage());
				}
			}
		});
	}

	public void sendClanChat(Player sender, String message) {
		Clan clan = getClan(sender);
		if (clan == null) {
			UtilPlayer.message(sender, F.main("Clan", "§cYou are not in a clan."));
			return;
		}
		
		ClanChatCommand cmd = new ClanChatCommand(clan.getId(), sender.getName(), message);
		cmd.publish();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) return;

		String message = event.getMessage();
		if (message.startsWith("@c ")) {
			event.setCancelled(true);
			String chatMessage = message.substring(3).trim();
			if (!chatMessage.isEmpty()) {
				sendClanChat(event.getPlayer(), chatMessage);
			}
		}
	}
	
	public ClanRepository getRepository() {
		return _repository;
	}

	public CoreClientManager getClientManager() {
		return _clientManager;
	}

	public Clan getClan(Player player) {
		return _playerClans.get(player.getUniqueId());
	}

	public Clan getClanByName(String name) {
		return _repository.getClan(name);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
			int accountId = _clientManager.Get(player).getAccountId();
			Clan clan = _repository.getClanByAccountId(accountId);
			if (clan != null) {
				Bukkit.getScheduler().runTask(getPlugin(), () -> {
					_playerClans.put(player.getUniqueId(), clan);
				});
			}
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		_playerClans.remove(event.getPlayer().getUniqueId());
	}
	
	public void createClan(Player player, String name) {
		if (!_clientManager.Get(player).GetRank().Has(Rank.DIVINE)) {
			UtilPlayer.message(player, F.main("Clan", "§cRequires DIVINE rank to create a clan."));
			return;
		}
		
		if (getClan(player) != null) {
			UtilPlayer.message(player, F.main("Clan", "§cYou are already in a clan."));
			return;
		}

		if (name.length() < 3 || name.length() > 10) {
			UtilPlayer.message(player, F.main("Clan", "§cClan name must be between 3 and 10 characters."));
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
			Clan existing = _repository.getClan(name);
			if (existing != null) {
				Bukkit.getScheduler().runTask(getPlugin(), () -> {
					UtilPlayer.message(player, F.main("Clan", "§cThat clan name is already taken."));
				});
				return;
			}
			
			int accountId = _clientManager.Get(player).getAccountId();
			boolean success = _repository.createClan(name, accountId);
			
			Bukkit.getScheduler().runTask(getPlugin(), () -> {
				if (success) {
					UtilPlayer.message(player, F.main("Clan", "§aYou have created clan " + name + "!"));
					// Refresh their clan status
					Clan clan = _repository.getClanByAccountId(accountId);
					if (clan != null) {
						_playerClans.put(player.getUniqueId(), clan);
					}
				} else {
					UtilPlayer.message(player, F.main("Clan", "§cFailed to create clan."));
				}
			});
		});
	}

	public void joinClan(Player player, String name) {
		if (getClan(player) != null) {
			UtilPlayer.message(player, F.main("Clan", "§cYou are already in a clan."));
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
			Clan clan = _repository.getClan(name);
			if (clan == null) {
				Bukkit.getScheduler().runTask(getPlugin(), () -> {
					UtilPlayer.message(player, F.main("Clan", "§cThat clan does not exist."));
				});
				return;
			}
			
			int accountId = _clientManager.Get(player).getAccountId();
			_repository.joinClan(clan.getId(), accountId);
			
			Bukkit.getScheduler().runTask(getPlugin(), () -> {
				UtilPlayer.message(player, F.main("Clan", "§aYou have joined clan " + name + "!"));
				_playerClans.put(player.getUniqueId(), clan);
			});
		});
	}

	public void leaveClan(Player player) {
		Clan clan = getClan(player);
		if (clan == null) {
			UtilPlayer.message(player, F.main("Clan", "§cYou are not in a clan."));
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
			int accountId = _clientManager.Get(player).getAccountId();
			
			if (clan.getLeaderId() == accountId) {
				_repository.deleteClan(clan.getId());
				Bukkit.getScheduler().runTask(getPlugin(), () -> {
					UtilPlayer.message(player, F.main("Clan", "§cYou have disbanded your clan."));
					_playerClans.remove(player.getUniqueId());
				});
			} else {
				_repository.leaveClan(accountId);
				Bukkit.getScheduler().runTask(getPlugin(), () -> {
					UtilPlayer.message(player, F.main("Clan", "§cYou have left your clan."));
					_playerClans.remove(player.getUniqueId());
				});
			}
		});
	}
}
