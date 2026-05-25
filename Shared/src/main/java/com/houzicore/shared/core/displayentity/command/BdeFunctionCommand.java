package com.houzicore.shared.core.displayentity.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.function.BdeFunctionPack;

public class BdeFunctionCommand extends CommandBase<MiniPlugin> {

    private final DisplayEntityManager _manager;

    public BdeFunctionCommand(DisplayEntityManager manager) {
        super(null, Rank.ADMIN, "bdefunction", "bdefn", "bdanim");
        _manager = manager;
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args.length == 0) {
            sendUsage(caller);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            _manager.reloadRegistry();
            UtilPlayer.message(caller, F.main("BDE", "Reloaded models and BDE function packs."));
            return;
        }

        if (sub.equals("list")) {
            listPacks(caller);
            return;
        }

        if (sub.equals("spawn") && args.length >= 2) {
            Location loc = getPlacementLocation(caller);
            boolean ok = _manager.getFunctionRuntime().spawnAndPlayLoop(args[1], loc);
            UtilPlayer.message(caller, F.main("BDE", ok
                    ? "Spawned animated pack at target: " + ChatColor.YELLOW + args[1]
                    : ChatColor.RED + "Could not spawn pack: " + args[1]));
            return;
        }

        if (sub.equals("run") && args.length >= 2) {
            Location loc = getPlacementLocation(caller);
            boolean ok = _manager.getFunctionRuntime().executeFunction(args[1], loc);
            UtilPlayer.message(caller, F.main("BDE", ok
                    ? "Ran function: " + ChatColor.YELLOW + args[1]
                    : ChatColor.RED + "Function not found: " + args[1]));
            return;
        }

        if (sub.equals("stop") && args.length >= 2) {
            boolean ok = _manager.getFunctionRuntime().stopPackAnimation(args[1], caller.getLocation());
            UtilPlayer.message(caller, F.main("BDE", ok
                    ? "Stopped animation for pack: " + ChatColor.YELLOW + args[1]
                    : ChatColor.RED + "Could not stop pack: " + args[1]));
            return;
        }

        if (sub.equals("delete") && args.length >= 2) {
            boolean ok = _manager.getFunctionRuntime().deletePackEntities(args[1], caller.getLocation());
            UtilPlayer.message(caller, F.main("BDE", ok
                    ? "Deleted entities for pack: " + ChatColor.YELLOW + args[1]
                    : ChatColor.RED + "Could not delete pack: " + args[1]));
            return;
        }

        sendUsage(caller);
    }

    private void listPacks(Player caller) {
        if (_manager.getFunctionRuntime().getPacks().isEmpty()) {
            UtilPlayer.message(caller, F.main("BDE", "No BDE function packs loaded."));
            return;
        }

        UtilPlayer.message(caller, F.main("BDE", "Loaded function packs:"));
        for (BdeFunctionPack pack : _manager.getFunctionRuntime().getPacks()) {
            int loopCount = pack.getLoopAnimationFunctionIds().size();
            UtilPlayer.message(caller, ChatColor.GRAY + "- " + ChatColor.YELLOW + pack.getNamespace()
                    + ChatColor.GRAY + " functions=" + ChatColor.AQUA + pack.getFunctionIds().size()
                    + ChatColor.GRAY + " loops=" + ChatColor.GREEN + loopCount);
        }
    }

    private void sendUsage(Player caller) {
        UtilPlayer.message(caller, F.main("BDE", "Usage: /bdanim list"));
        UtilPlayer.message(caller, F.main("BDE", "Usage: /bdanim spawn <namespace>"));
        UtilPlayer.message(caller, F.main("BDE", "Usage: /bdanim run <namespace:path>"));
        UtilPlayer.message(caller, F.main("BDE", "Usage: /bdanim stop <namespace>"));
        UtilPlayer.message(caller, F.main("BDE", "Usage: /bdanim delete <namespace>"));
    }

    private static Location getPlacementLocation(Player player) {
        Block target = player.getTargetBlockExact(8);
        if (target != null) {
            return target.getLocation().add(0.5, 1.0, 0.5);
        }
        return player.getLocation();
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : List.of("list", "reload", "spawn", "run", "stop", "delete")) {
                if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    result.add(sub);
                }
            }
            return result;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("spawn")
                || args[0].equalsIgnoreCase("stop")
                || args[0].equalsIgnoreCase("delete"))) {
            for (BdeFunctionPack pack : _manager.getFunctionRuntime().getPacks()) {
                if (pack.getNamespace().startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    result.add(pack.getNamespace());
                }
            }
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("run")) {
            for (BdeFunctionPack pack : _manager.getFunctionRuntime().getPacks()) {
                for (String id : pack.getFunctionIds()) {
                    if (id.startsWith(args[1].toLowerCase(Locale.ROOT))) {
                        result.add(id);
                    }
                }
            }
        }
        return result;
    }
}
