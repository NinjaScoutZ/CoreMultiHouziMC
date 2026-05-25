package com.houzicore.shared.core.resourcepack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.resourcepack.redis.RedisUnloadResPack;
import com.houzicore.shared.serverdata.commands.CommandCallback;
import com.houzicore.shared.serverdata.commands.ServerCommand;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;

public class ResPackManager implements CommandCallback {
	private final ResUnloadCheck _packUnloadCheck;

	public ResPackManager(ResUnloadCheck packUnloadCheck) {
		_packUnloadCheck = packUnloadCheck;

		ServerCommandManager.getInstance().registerCommandType("RedisUnloadResPack", RedisUnloadResPack.class, this);
	}

	@Override
	public void run(ServerCommand command) {
		if (command instanceof RedisUnloadResPack) {
			final RedisUnloadResPack redisCommand = (RedisUnloadResPack) command;

			final Player player = Bukkit.getPlayerExact(redisCommand.getPlayer());

			if (player != null) {
				if (_packUnloadCheck.canSendUnload(player)) {
					player.setResourcePack("http://www.chivebox.com/file/c/empty.zip");
				}
			}
		}
	}

}
