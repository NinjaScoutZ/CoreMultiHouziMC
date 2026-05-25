package com.houzicore.shared.core.command;

import java.util.List;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.recharge.Recharge;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandCenter implements Listener {
	public static CommandCenter Instance;

	public static void Initialize(JavaPlugin plugin) {
		if (Instance == null) {
			Instance = new CommandCenter(plugin);
		}
	}

	protected JavaPlugin Plugin;
	protected CoreClientManager ClientManager;

	protected NautHashMap<String, ICommand> Commands;

	private CommandCenter(JavaPlugin instance) {
		Plugin = instance;
		Commands = new NautHashMap<>();
		Plugin.getServer().getPluginManager().registerEvents(this, Plugin);
	}

	public void AddCommand(final ICommand command) {
		for (final String commandRoot : command.Aliases()) {
			Commands.put(commandRoot.toLowerCase(), command);
			command.SetCommandCenter(this);
			
			if (org.bukkit.Bukkit.getServer().getCommandMap().getCommand(commandRoot.toLowerCase()) == null) {
				org.bukkit.Bukkit.getServer().getCommandMap().register(Plugin.getName().toLowerCase(), new org.bukkit.command.Command(commandRoot.toLowerCase()) {
					@Override
					public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
						return true;
					}

					@Override
					public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
						if (sender instanceof org.bukkit.entity.Player) {
							java.util.List<String> suggestions = command.onTabComplete((org.bukkit.entity.Player) sender, alias, args);
							if (suggestions != null) {
								return suggestions;
							}
						}
						return super.tabComplete(sender, alias, args);
					}
				});
			}
		}
	}

	public CoreClientManager GetClientManager() {
		return ClientManager;
	}

	@EventHandler
	public void OnPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String commandName = event.getMessage().substring(1);
		String[] args = new String[0];

		if (commandName.contains(" ")) {
			commandName = commandName.split(" ")[0];
			args = event.getMessage().substring(event.getMessage().indexOf(' ') + 1).split(" ");
		}

		final ICommand command = Commands.get(commandName.toLowerCase());

		if (commandName.equalsIgnoreCase("help") || commandName.equalsIgnoreCase("?")) {
			event.setCancelled(true);
			displayHelpMenu(event.getPlayer());
			return;
		}

		if (command != null) {
			event.setCancelled(true);

			if (ClientManager.Get(event.getPlayer()).GetRank().Has(event.getPlayer(), command.GetRequiredRank(),
					command.GetSpecificRanks(), true)) {
				if (!Recharge.Instance.use(event.getPlayer(), "Command", 500, false, false)) {
					event.getPlayer().sendMessage(F.main("Command Center", "You can't spam commands that fast."));
					return;
				}

				command.SetAliasUsed(commandName.toLowerCase());
				command.Execute(event.getPlayer(), args);
			}
		}
	}

	@EventHandler
	public void onTabComplete(TabCompleteEvent event) {
		final ICommand command = Commands.get(event.getBuffer().split(" ")[0].replace("/", "").toLowerCase());

		if (command != null) {
			final List<String> suggestions = command.onTabComplete(event.getSender(), event.getBuffer().split(" ")[0],
					java.util.Arrays.copyOfRange(event.getBuffer().split(" "), 1, event.getBuffer().split(" ").length));

			if (suggestions != null) {
				event.setCompletions(suggestions);
			}
		}
	}

	public void RemoveCommand(ICommand command) {
		for (final String commandRoot : command.Aliases()) {
			Commands.remove(commandRoot.toLowerCase());
			command.SetCommandCenter(null);
		}
	}

	public void setClientManager(CoreClientManager clientManager) {
		ClientManager = clientManager;
	}

	private void displayHelpMenu(org.bukkit.entity.Player player) {
		player.sendMessage(" ");
		player.sendMessage("§6§l" + com.houzicore.shared.common.util.UtilText.toSmallCaps("Available Commands"));
		player.sendMessage(com.houzicore.shared.common.util.C.cGray + "-----------------------------------------------------");

		boolean hasAny = false;
		java.util.HashSet<Class<?>> processed = new java.util.HashSet<>();

		for (ICommand cmd : Commands.values()) {
			if (!processed.add(cmd.getClass())) continue;

			if (ClientManager != null && !ClientManager.Get(player).GetRank().Has(player, cmd.GetRequiredRank(), cmd.GetSpecificRanks(), false)) {
				continue;
			}

			String usage = cmd.GetUsage() != null && !cmd.GetUsage().isEmpty() ? cmd.GetUsage() : "/" + cmd.Aliases().iterator().next();
			String desc = cmd.GetDescription() != null && !cmd.GetDescription().isEmpty() ? cmd.GetDescription() : "No description provided.";
			
			player.sendMessage(com.houzicore.shared.common.util.C.cGreen + usage + com.houzicore.shared.common.util.C.cGray + " - " + com.houzicore.shared.common.util.C.cWhite + desc);
			hasAny = true;
		}

		if (!hasAny) {
			player.sendMessage(com.houzicore.shared.common.util.C.cRed + "You do not have permission to use any custom commands.");
		}
		player.sendMessage(com.houzicore.shared.common.util.C.cGray + "-----------------------------------------------------");
		player.sendMessage(" ");
	}
}
