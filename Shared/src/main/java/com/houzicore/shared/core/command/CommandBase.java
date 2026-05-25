package com.houzicore.shared.core.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.command.CommandMeta;

public abstract class CommandBase<PluginType extends MiniPlugin> implements ICommand {
	private Rank _requiredRank;
	private Rank[] _specificRank;

	private List<String> _aliases;

    private String _description = "";
    private String _usage = "";
    private boolean _allowConsole = false;

	protected PluginType Plugin;
	protected String AliasUsed;
	protected CommandCenter CommandCenter;

	public CommandBase(PluginType plugin, Rank requiredRank, Rank[] specificRank, String... aliases) {
		Plugin = plugin;
		_requiredRank = requiredRank;
		_specificRank = specificRank;

		_aliases = Arrays.asList(aliases);
        applyCommandMeta();
	}

	public CommandBase(PluginType plugin, Rank requiredRank, String... aliases) {
		Plugin = plugin;
		_requiredRank = requiredRank;
		_aliases = Arrays.asList(aliases);
        applyCommandMeta();
	}

    private void applyCommandMeta() {
        CommandMeta meta = this.getClass().getAnnotation(CommandMeta.class);
        if (meta != null) {
            if (!meta.description().isEmpty()) this._description = meta.description();
            if (!meta.usage().isEmpty()) this._usage = meta.usage();
            if (meta.permission() != Rank.ALL) this._requiredRank = meta.permission();
            if (meta.aliases().length > 0) this._aliases = Arrays.asList(meta.aliases());
            this._allowConsole = meta.allowConsole();
        }
    }

	@Override
	public Collection<String> Aliases() {
		return _aliases;
	}

	@SuppressWarnings("rawtypes")
	protected List<String> getMatches(String start, Enum[] numerators) {
		final List<String> matches = new ArrayList<>();

		for (final Enum e : numerators) {
			final String s = e.toString();
			if (s.toLowerCase().startsWith(start.toLowerCase())) {
				matches.add(s);
			}
		}

		return matches;
	}

	protected List<String> getMatches(String start, List<String> possibleMatches) {
		final List<String> matches = new ArrayList<>();

		for (final String possibleMatch : possibleMatches) {
			if (possibleMatch.toLowerCase().startsWith(start.toLowerCase())) {
				matches.add(possibleMatch);
			}
		}

		return matches;
	}

	protected List<String> getPlayerMatches(Player sender, String start) {
		final List<String> matches = new ArrayList<>();

		for (final Player player : UtilServer.getPlayers()) {
			if (sender.canSee(player) && player.getName().toLowerCase().startsWith(start.toLowerCase())) {
				matches.add(player.getName());
			}
		}

		return matches;
	}

	@Override
	public Rank GetRequiredRank() {
		return _requiredRank;
	}

	@Override
	public Rank[] GetSpecificRanks() {
		return _specificRank;
	}

	@Override
	public String GetDescription() {
		return _description;
	}

	@Override
	public String GetUsage() {
		return _usage;
	}

	@Override
	public boolean AllowConsole() {
		return _allowConsole;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args) {
		return null;
	}

	protected void resetCommandCharge(Player caller) {
		Recharge.Instance.recharge(caller, "Command");
	}

	@Override
	public void SetAliasUsed(String alias) {
		AliasUsed = alias;
	}

	@Override
	public void SetCommandCenter(CommandCenter commandCenter) {
		CommandCenter = commandCenter;
	}

}
