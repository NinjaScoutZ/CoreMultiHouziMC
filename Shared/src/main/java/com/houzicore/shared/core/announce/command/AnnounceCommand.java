package com.houzicore.shared.core.announce.command;

import org.bukkit.entity.Player;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.announce.AnnounceManager;

public class AnnounceCommand extends CommandBase<AnnounceManager> {

    public AnnounceCommand(AnnounceManager plugin) {
        super(plugin, Rank.ADMIN, "announce", "bc", "broadcast");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args.length < 2) {
            UtilPlayer.message(caller, F.main("Announce", "Usage: /announce <time_seconds> <message>"));
            return;
        }

        long durationSeconds;
        try {
            durationSeconds = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            UtilPlayer.message(caller, F.main("Announce", "Invalid time. Note: use seconds."));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        long millis = durationSeconds * 1000L;
        String finalMessage = message.toString().trim();

        // Publishes to all servers via Redis (ServerCommandManager → AnnouncementCommand callback in AnnounceManager)
        com.houzicore.shared.serverdata.commands.AnnouncementCommand cmd = new com.houzicore.shared.serverdata.commands.AnnouncementCommand(false, finalMessage, millis);
        cmd.publish();

        UtilPlayer.message(caller, F.main("Announce", "Broadcasted for §e" + durationSeconds + "s §7across all servers."));
    }
}
