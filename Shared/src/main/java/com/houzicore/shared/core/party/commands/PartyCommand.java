package com.houzicore.shared.core.party.commands;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.jsonchat.ChildJsonMessage;
import com.houzicore.shared.common.jsonchat.ClickEvent;
import com.houzicore.shared.common.jsonchat.JsonMessage;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.core.party.PartyManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PartyCommand extends CommandBase<PartyManager> {
	public PartyCommand(PartyManager plugin) {
		super(plugin, Rank.ALL, new String[] { "party", "z" });
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null || args.length == 0) {
			Plugin.openShop(caller);
			return;
		}

		if (args[0].equalsIgnoreCase("kick") && args.length < 2) {
			UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.command_help_title")));
			UtilPlayer.message(caller, F.value(0, "/party <Player>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.command_help_join")));
			UtilPlayer.message(caller, F.value(0, "/party leave", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.command_help_leave")));
			UtilPlayer.message(caller, F.value(0, "/party kick <Player>", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.command_help_kick")));

			return;
		}

		// Callers Party
		Party party = Plugin.getPartyByPlayer(caller);

		// Leave
		if (args[0].equalsIgnoreCase("leave")) {
			if (party == null) {
				UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.not_in_party")));
			} else {
				party.LeaveParty(caller);
			}

			return;
		}

		// Kick
		if (args[0].equalsIgnoreCase("kick")) {
			if (party == null) {
				UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.not_in_party")));
			} else {
				if (party.getLeaderName().equals(caller.getName())) {
					final String target = UtilPlayer.searchCollection(caller, args[1], party.GetPlayers(), "Party ",
							true);
					if (target == null)
						return;

					if (target.equals(caller.getName())) {
						UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.cannot_kick_self")));
						return;
					}

					party.KickParty(target);
				} else {
					UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.not_leader")));
				}
			}

			return;
		}

		// Password
		if (args[0].equalsIgnoreCase("password") && args.length >= 2) {
			if (party == null) {
				UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.not_in_party")));
				return;
			}
			if (!party.getLeaderName().equals(caller.getName())) {
				UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.not_leader")));
				return;
			}
			if (!Plugin.GetClients().Get(caller).GetRank().Has(Rank.SOVEREIGN)) {
				UtilPlayer.message(caller, F.main("Party", "§cRequires SOVEREIGN rank to lock party."));
				return;
			}
			String password = args[1];
			party.setPassword(password);
			UtilPlayer.message(caller, F.main("Party", "§aParty password set. Only players with the password or an invite can join."));
			return;
		}
		
		// Open
		if (args[0].equalsIgnoreCase("open")) {
			if (party == null) {
				UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.not_in_party")));
				return;
			}
			if (!party.getLeaderName().equals(caller.getName())) {
				UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.not_leader")));
				return;
			}
			party.setPassword(null);
			UtilPlayer.message(caller, F.main("Party", "§aParty is now open (no password)."));
			return;
		}

		// Main
		final Player target = UtilPlayer.searchOnline(caller, args[0], true);
		if (target == null)
			return;

		if (target.equals(caller)) {
			UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.cannot_party_self")));
			return;
		}

		if (Plugin.getIgnoreManager().isIgnoring(target, caller)) {
			UtilPlayer.message(caller, F.main(Plugin.getIgnoreManager().getName(), ChatColor.GRAY + "That player is ignoring you"));
			return;
		}

		if (Plugin.getIgnoreManager().isIgnoring(caller, target.getName())) {
			UtilPlayer.message(caller, F.main(Plugin.getIgnoreManager().getName(), ChatColor.GRAY + "You are ignoring that player"));
			return;
		}

		// Preference check
		if (!Plugin.getPreferenceManager().Get(target).PartyRequests) {
			UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.disabled_requests").replace("{0}", F.name(UtilEnt.getName(target)))));
			return;
		}

		// Invite or Suggest
		if (party != null) {
			if (party.GetPlayers().size() + party.GetInvitees().size() >= 16) {
				UtilPlayer.message(caller, com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.party_full"));
				caller.playSound(caller.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1.5f);
			}
			// Decline
			else if (party.getPlayerUuids().contains(target.getUniqueId())) {
				UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.invite_declined").replace("{0}", F.name(target.getName()))));
				caller.playSound(caller.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1.5f);
			}
			// Decline
			else if (party.GetInvitees().contains(target.getUniqueId())) {
				UtilPlayer.message(caller, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(caller, "party.invite_declined_other").replace("{0}", F.name(target.getName()))));
				caller.playSound(caller.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1.5f);
			}
			// Invite
			else if (party.getLeaderUuid().equals(caller.getUniqueId())) {
				party.InviteParty(target, Plugin.getPartyByPlayer(target) != null);
			}
			// Suggest
			else {
				party.Announce("party.suggest_invite", F.name(caller.getName()), F.name(target.getName()));

				final Player leader = Bukkit.getPlayer(party.getLeaderUuid());

				if (leader != null) {
					final ChildJsonMessage message = new JsonMessage("").extra(C.mHead + "Party> " + C.mBody + com.houzicore.shared.core.lang.LangManager.get().get(leader, "party.suggest_invite_cmd").replace("{0}", F.link(target.getName())));

					message.click(ClickEvent.RUN_COMMAND, "/party " + target.getName());

					message.sendToPlayer(leader);
				}
			}
		}
		// Create or Join
		else {
			final Party targetParty = Plugin.getPartyByPlayer(target);

			// Try to Join
			if (targetParty != null) {
				if (targetParty.GetInvitees().contains(caller.getUniqueId())) {
					targetParty.JoinParty(caller);
					return;
				} else if (targetParty.isLocked()) {
					if (args.length >= 2 && args[1].equals(targetParty.getPassword())) {
						targetParty.JoinParty(caller);
						return;
					} else {
						UtilPlayer.message(caller, F.main("Party", "§cThis party requires a password. Usage: /party <Leader> <password>"));
						return;
					}
				}
			}

			// Create
			party = Plugin.CreateParty(caller);
			party.InviteParty(target, Plugin.getPartyByPlayer(target) != null);
		}
	}
}
