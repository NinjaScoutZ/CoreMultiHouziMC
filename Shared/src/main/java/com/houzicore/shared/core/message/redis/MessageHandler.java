package com.houzicore.shared.core.message.redis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.message.MessageManager;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;

public class MessageHandler implements CommandCallback {
	private final MessageManager _messageManager;

	public MessageHandler(MessageManager messageManager) {
		_messageManager = messageManager;
	}

	@Override
	public void run(ServerCommand command) {
		if (command instanceof RedisMessage) {
			final RedisMessage message = (RedisMessage) command;

			final Player target = Bukkit.getPlayerExact(message.getTarget());

			if (target != null) {
				_messageManager.receiveMessage(target, message);
			}
		} else if (command instanceof RedisMessageCallback) {

			_messageManager.receiveMessageCallback((RedisMessageCallback) command);
		}
	}
}
