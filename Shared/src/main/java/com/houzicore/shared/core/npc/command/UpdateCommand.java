package com.houzicore.shared.core.npc.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.npc.NpcManager;
import com.houzicore.shared.core.npc.event.NpcUpdateEvent;

public class UpdateCommand extends CommandBase<NpcManager> {
    public UpdateCommand(NpcManager plugin) {
        super(plugin, Rank.DEVELOPER, "update");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        Bukkit.getPluginManager().callEvent(new NpcUpdateEvent(caller));
        caller.sendMessage(F.main(Plugin.getName(), "Triggered NPC database sync."));
    }
}
