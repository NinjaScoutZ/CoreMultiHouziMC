package com.houzicore.lobby.hub.poll;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Sound;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.poll.command.PollCommand;

public class PollManager extends MiniDbClientPlugin<PlayerPollData>
{
	private PollRepository _repository;
	private DonationManager _donationManager;
	private List<Poll> _polls;

	public PollManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super("PollManager", plugin, clientManager);

		_repository = new PollRepository(plugin);

		_donationManager = donationManager;

		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				_polls = _repository.retrievePolls();
			}
		}, 1L, 1200L);
	}

	@Override
	protected PlayerPollData AddPlayer(String player)
	{
		return new PlayerPollData();
	}

	@EventHandler
	public void join(PlayerJoinEvent event)
	{
		PlayerPollData pollData = Get(event.getPlayer());
		pollData.setPollCooldown(5000);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		if (_polls == null || _polls.size() == 0)
			return;

		for (Player player : _plugin.getServer().getOnlinePlayers())
		{
			PlayerPollData pollData = Get(player);

			if (pollData.shouldPoll())
			{
				Rank playerRank = ClientManager.Get(player).GetRank();
				Poll nextPoll = getNextPoll(pollData, playerRank);
				if (nextPoll != null)
					displayPoll(player, nextPoll);

				// Update the poll cooldown even if there isn't a poll available
				pollData.updatePollCooldown();
			}
		}
	}

	public Poll getNextPoll(PlayerPollData pollData, Rank playerRank)
	{
		for (Poll poll : _polls)
		{
			if (poll.isEnabled() && poll.getDisplayType().shouldDisplay(playerRank) && !pollData.hasAnswered(poll))
				return poll;
		}

		return null;
	}

	public void displayPoll(Player player, Poll poll)
	{
		String[] answers = poll.getAnswers();

		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);

		player.sendMessage("\u00A78\u00A7m                                                    ");
		player.sendMessage(poll.getQuestion());
		player.sendMessage("");
		for (int i = 1; i <= answers.length; i++)
		{
			if (answers[i-1] != null && answers[i-1].length() > 0)
			{
				String message = C.cGreen + i + ". " + answers[i - 1];
				TextComponent component = new TextComponent(message);
				component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/poll " + poll.getId() + " " + i));
				component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(isThai ? "§7คลิกเพื่อเลือก " + F.elem("#" + i) : "§7Click to select " + F.elem("#" + i)).create()));
				player.spigot().sendMessage(component);
			}
		}
		player.sendMessage("");
		player.sendMessage(isThai ? "§7คลิกเลือกคำตอบเพื่อรับ " + C.cGreen + poll.getCoinReward() + " Essence" : "§7Click an answer to receive " + C.cGreen + poll.getCoinReward() + " Essence");
		player.sendMessage("\u00A78\u00A7m                                                    ");

		player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 0f);
	}

	public void answerPoll(final Player player, final Poll poll, final int answer)
	{
		final String name = player.getName();
		final UUID uuid = player.getUniqueId();

		// First update answer locally so we know it was answered
		Get(player).addAnswer(poll.getId(), answer);

		// Here we add the answer into the database, and once that is successful we give the coin reward
		_plugin.getServer().getScheduler().runTaskAsynchronously(_plugin, new Runnable()
		{
			@Override
			public void run()
			{
				if (_repository.addPollAnswer(uuid, poll.getId(), answer))
				{
					// Poll response successful, give coins
					_donationManager.RewardEssence(new Callback<Boolean>()
					{
						@Override
						public void run(Boolean completed)
						{
							if (completed)
							{
								// Need to get out of Async code
								_plugin.getServer().getScheduler().runTask(_plugin, new Runnable()
								{
									@Override
									public void run()
									{
										boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
										UtilPlayer.message(player, F.main("Poll", com.houzicore.shared.core.lang.LangManager.get().get(player, "hub.poll.thanks")));
										player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 0);
										UtilPlayer.message(player, F.main("Essence", com.houzicore.shared.core.lang.LangManager.get().get(player, "hub.poll.reward", F.elem(poll.getCoinReward() + ""))));
									}
								});
							}
						}
					}, "Poll", name, uuid, poll.getCoinReward());
				}
			}
		});
	}

	public Poll getPoll(int pollId)
	{
		for (Poll poll : _polls)
		{
			if (poll.getId() == pollId)
				return poll;
		}
		return null;
	}

	public PollStats getPollStats(int pollId)
	{
		return _repository.getPollStats(pollId);
	}

	public List<Poll> getPolls()
	{
		return _polls;
	}

	@Override
	public void addCommands()
	{
		addCommand(new PollCommand(this));
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException
	{
		Set(playerName, _repository.loadPollData(resultSet));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT pollId, value FROM accountPolls WHERE accountPolls.accountId = '" + accountId + "';";
	}
}
