package com.houzicore.arcade.nautilus.game.arcade.command;

import com.houzicore.arcade.Arcade;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.core.npc.NpcManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MapBuilderBridgeCommand extends CommandBase<ArcadeManager>
{
    public MapBuilderBridgeCommand(ArcadeManager plugin)
    {
        super(plugin, Rank.ADMIN, new Rank[] { Rank.MAPLEAD, Rank.JNR_DEV }, "mb", "mapbuilder");
    }

    @Override
    public void Execute(Player caller, String[] args)
    {
        Arcade arcade = (Arcade) Plugin.getPlugin();
        NpcManager npcManager = arcade.getNpcManager();

        if (args.length == 0)
        {
            sendHelp(caller);
            return;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("adminbuilder") || sub.equals("sandbox"))
        {
            if (npcManager == null)
            {
                UtilPlayer.message(caller, F.main("MapBuilder", C.cRed + "NPC builder is not available on this Arcade instance."));
                return;
            }

            boolean state = npcManager.toggleAdminBuilder(caller);
            if (state)
            {
                clearBuilderInventory(caller);
                caller.setGameMode(GameMode.CREATIVE);
                caller.setOp(true);
                UtilPlayer.message(caller, F.main("MapBuilder", C.cGreen + "Builder sandbox enabled via Arcade bridge."));
                UtilPlayer.message(caller, F.main("MapBuilder", C.cGray + "Use " + C.cYellow + "/npc add" + C.cGray + " and " + C.cYellow + "/game" + C.cGray + " while arranging the lobby."));
            }
            else
            {
                caller.setGameMode(GameMode.SURVIVAL);
                caller.setOp(false);
                clearBuilderInventory(caller);
                UtilPlayer.message(caller, F.main("MapBuilder", C.cRed + "Builder sandbox disabled."));
                UtilPlayer.message(caller, F.main("MapBuilder", C.cGray + "Rejoin if you need the default lobby items restored."));
            }
            return;
        }

        UtilPlayer.message(caller, F.main("MapBuilder", C.cYellow + "Arcade only supports " + C.cAqua + "/mb adminbuilder" + C.cYellow + " for lobby editing."));
        UtilPlayer.message(caller, F.main("MapBuilder", C.cGray + "Use the dedicated MapBuilder server/plugin for full /mb create/edit/export flows."));
    }

    private void sendHelp(Player caller)
    {
        UtilPlayer.message(caller, F.main("MapBuilder", "Commands List:"));
        UtilPlayer.message(caller, F.help("/mb adminbuilder", "Enter Arcade lobby builder sandbox", Rank.ADMIN));
        UtilPlayer.message(caller, F.help("/game ...", "Arcade game controls for the current lobby/runtime", Rank.ADMIN));
        UtilPlayer.message(caller, F.main("Tip", "Full map authoring still belongs to the dedicated MapBuilder plugin."));
    }

    private void clearBuilderInventory(Player player)
    {
        ItemStack air = new ItemStack(Material.AIR);
        for (int slot = 0; slot < 41; slot++)
        {
            player.getInventory().setItem(slot, air);
        }

        player.getInventory().setHelmet(air);
        player.getInventory().setChestplate(air);
        player.getInventory().setLeggings(air);
        player.getInventory().setBoots(air);
        player.updateInventory();
    }
}
