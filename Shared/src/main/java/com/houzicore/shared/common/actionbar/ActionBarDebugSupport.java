package com.houzicore.shared.common.actionbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class ActionBarDebugSupport
{
	private ActionBarDebugSupport() {}

	public static void execute(Player caller, String[] args, String commandRoot, Rank requiredRank)
	{
		if (caller == null)
			return;

		if (args == null || args.length == 0)
		{
			show(caller, caller);
			return;
		}

		String subcommand = args[0].toLowerCase(Locale.ROOT);
		if (subcommand.equals("show"))
		{
			Player target = resolveTarget(caller, args, 1);
			if (target != null)
				show(caller, target);
			return;
		}

		if (subcommand.equals("clear"))
		{
			Player target = resolveTarget(caller, args, 1);
			if (target == null)
				return;

			ActionBarService.clear(target);
			UtilPlayer.message(caller, F.main("ActionBar", "Cleared lease for " + F.elem(target.getName()) + "."));
			show(caller, target);
			return;
		}

		if (subcommand.equals("send"))
		{
			if (args.length < 4)
			{
				sendUsage(caller, commandRoot, requiredRank);
				return;
			}

			ActionBarChannel channel = parseChannel(args[1]);
			if (channel == null)
			{
				UtilPlayer.message(caller, F.main("ActionBar", "Unknown channel " + F.elem(args[1]) + "."));
				sendUsage(caller, commandRoot, requiredRank);
				return;
			}

			long ttlMs;
			try
			{
				ttlMs = Long.parseLong(args[2]);
			}
			catch (NumberFormatException ex)
			{
				UtilPlayer.message(caller, F.main("ActionBar", "TTL must be a number in milliseconds."));
				sendUsage(caller, commandRoot, requiredRank);
				return;
			}

			if (ttlMs < 0)
			{
				UtilPlayer.message(caller, F.main("ActionBar", "TTL must be zero or greater."));
				return;
			}

			String message = combine(args, 3);
			message = ChatColor.translateAlternateColorCodes('&', message);
			boolean accepted = ActionBarService.display(
				caller,
				channel,
				LegacyComponentSerializer.legacySection().deserialize(message),
				ttlMs);

			if (accepted)
				UtilPlayer.message(caller, F.main("ActionBar", "Sent " + F.elem(channel.name()) + " for " + F.time(ttlMs + " ms") + "."));
			else
				UtilPlayer.message(caller, F.main("ActionBar", "Blocked by a higher-priority active lease."));

			show(caller, caller);
			return;
		}

		sendUsage(caller, commandRoot, requiredRank);
	}

	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		List<String> matches = new ArrayList<String>();
		if (args == null)
			return matches;

		if (args.length <= 1)
		{
			return filter(args.length == 0 ? "" : args[0], Arrays.asList("show", "clear", "send"));
		}

		String subcommand = args[0].toLowerCase(Locale.ROOT);
		if (subcommand.equals("show") || subcommand.equals("clear"))
		{
			if (args.length == 2)
			{
				List<String> players = new ArrayList<String>();
				for (Player player : Bukkit.getOnlinePlayers())
					players.add(player.getName());
				return filter(args[1], players);
			}
			return matches;
		}

		if (subcommand.equals("send"))
		{
			if (args.length == 2)
			{
				List<String> channels = new ArrayList<String>();
				for (ActionBarChannel channel : ActionBarChannel.values())
					channels.add(channel.name());
				return filter(args[1], channels);
			}

			if (args.length == 3)
				return filter(args[2], Arrays.asList("350", "500", "800", "1500", "2500"));
		}

		return matches;
	}

	private static void show(Player caller, Player target)
	{
		ActionBarService.Snapshot snapshot = ActionBarService.snapshot(target);
		if (!snapshot.hasActiveLease())
		{
			UtilPlayer.message(caller, F.main("ActionBar", "No active lease for " + F.elem(target.getName()) + "."));
			return;
		}

		UtilPlayer.message(caller, F.main("ActionBar", "Lease for " + F.elem(target.getName()) + "."));
		UtilPlayer.message(caller, F.value("Channel", snapshot.getChannel().name()));
		UtilPlayer.message(caller, F.value("Priority", Integer.toString(snapshot.getPriority())));
		UtilPlayer.message(caller, F.value("Remaining", snapshot.getRemainingTtlMs() + " ms"));
		UtilPlayer.message(caller, F.value("Preview", snapshot.getPreview().isEmpty() ? "<empty>" : snapshot.getPreview()));
	}

	private static Player resolveTarget(Player caller, String[] args, int targetIndex)
	{
		if (args.length <= targetIndex)
			return caller;

		Player target = Bukkit.getPlayerExact(args[targetIndex]);
		if (target == null)
			UtilPlayer.message(caller, F.main("ActionBar", "Player " + F.elem(args[targetIndex]) + " is not online."));
		return target;
	}

	private static ActionBarChannel parseChannel(String input)
	{
		if (input == null || input.trim().isEmpty())
			return null;

		try
		{
			return ActionBarChannel.valueOf(input.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex)
		{
			return null;
		}
	}

	private static void sendUsage(Player caller, String commandRoot, Rank requiredRank)
	{
		UtilPlayer.message(caller, F.main("ActionBar", "Usage:"));
		UtilPlayer.message(caller, F.help(commandRoot, "Show your active lease", requiredRank));
		UtilPlayer.message(caller, F.help(commandRoot + " show [player]", "Inspect a player's active lease", requiredRank));
		UtilPlayer.message(caller, F.help(commandRoot + " clear [player]", "Clear a player's lease", requiredRank));
		UtilPlayer.message(caller, F.help(commandRoot + " send <channel> <ttlMs> <message...>", "Send a manual test actionbar to yourself", requiredRank));
	}

	private static List<String> filter(String start, List<String> values)
	{
		List<String> matches = new ArrayList<String>();
		String prefix = start == null ? "" : start.toLowerCase(Locale.ROOT);
		for (String value : values)
		{
			if (value.toLowerCase(Locale.ROOT).startsWith(prefix))
				matches.add(value);
		}
		return matches;
	}

	private static String combine(String[] args, int start)
	{
		if (args == null || args.length <= start)
			return "";

		StringBuilder out = new StringBuilder();
		for (int i = start; i < args.length; i++)
		{
			if (out.length() > 0)
				out.append(' ');
			out.append(args[i]);
		}
		return out.toString();
	}
}
