package com.houzicore.shared;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.houzicore.shared.core.command.CommandCenter;
import com.houzicore.shared.core.command.ICommand;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;

public abstract class MiniPlugin implements Listener {
	protected String _moduleName = "Default";
	protected JavaPlugin _plugin;
	protected NautHashMap<String, ICommand> _commands;
	private final java.util.List<org.bukkit.scheduler.BukkitTask> _tasks = new java.util.ArrayList<>();

	public MiniPlugin(String moduleName, JavaPlugin plugin) {
		_moduleName = moduleName;
		_plugin = plugin;

		com.houzicore.shared.core.plugin.PluginRegistry.register(this);
		
		_commands = new NautHashMap<>();

		onEnable();

		registerEvents(this);
	}

	public final void addCommand(ICommand command) {
		CommandCenter.Instance.AddCommand(command);
	}

	public void addCommands() {
	}

	protected void generatePermissions() {
	}

	public void deregisterSelf() {
		HandlerList.unregisterAll(this);
	}

	public void disable() {
	}

	public void enable() {
	}

	public final String getName() {
		return _moduleName;
	}

	public JavaPlugin getPlugin() {
		return _plugin;
	}

	public PluginManager getPluginManager() {
		return _plugin.getServer().getPluginManager();
	}

	public BukkitScheduler getScheduler() {
		return _plugin.getServer().getScheduler();
	}

	/**
	 * Called to cleanly reset plugin-specific configuration defaults
	 * during round resets or global configuration reloads.
	 */
	public void resetConfiguration() {
	}

	public void log(String message) {
	}

	public final void onDisable() {
		disable();
		
		for (org.bukkit.scheduler.BukkitTask task : _tasks) {
			try { task.cancel(); } catch (Exception e) {}
		}
		_tasks.clear();

		log("Disabled.");
	}

	public final void onEnable() {
		final long epoch = System.currentTimeMillis();
		log("Initializing...");
		enable();
		addCommands();
		generatePermissions();
		log("Enabled in " + UtilTime.convertString(System.currentTimeMillis() - epoch, 1, TimeUnit.FIT) + ".");
	}

	public void registerEvents(Listener listener) {
		_plugin.getServer().getPluginManager().registerEvents(listener, _plugin);
	}

	public void registerSelf() {
		registerEvents(this);
	}

	public final void removeCommand(ICommand command) {
		CommandCenter.Instance.RemoveCommand(command);
	}

	public void runAsync(Runnable runnable) {
		_plugin.getServer().getScheduler().runTaskAsynchronously(_plugin, runnable);
	}

	public void runSync(Runnable runnable) {
		_plugin.getServer().getScheduler().runTask(_plugin, runnable);
	}

	public void runSyncLater(Runnable runnable, long delay) {
		_plugin.getServer().getScheduler().runTaskLater(_plugin, runnable, delay);
	}

	public void runSyncTimer(Runnable runnable, long delay, long period) {
		_tasks.add(_plugin.getServer().getScheduler().runTaskTimer(_plugin, runnable, delay, period));
	}
}
