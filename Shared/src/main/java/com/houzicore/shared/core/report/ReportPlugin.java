package com.houzicore.shared.core.report;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.report.command.ReportCloseCommand;
import com.houzicore.shared.core.report.command.ReportCommand;
import com.houzicore.shared.core.report.command.ReportHandleCommand;

import org.bukkit.plugin.java.JavaPlugin;

public class ReportPlugin extends MiniPlugin {

	private static JavaPlugin instance;

	public static JavaPlugin getPluginInstance() {
		return instance;
	}

	public ReportPlugin(JavaPlugin plugin, String serverName) {
		super("ReportPlugin", plugin);

		instance = plugin;
	}

	@Override
	public void addCommands() {
		addCommand(new ReportCommand(this));
		addCommand(new ReportHandleCommand(this));
		addCommand(new ReportCloseCommand(this));
		// AddCommand(new ReportDebugCommand(this));
	}
}
