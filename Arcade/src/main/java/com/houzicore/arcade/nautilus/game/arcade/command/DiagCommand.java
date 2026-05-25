package com.houzicore.arcade.nautilus.game.arcade.command;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameLifecycleGuard;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class DiagCommand {

    public static void register(LifecycleEventManager<Plugin> mgr, ArcadeManager plugin) {
        mgr.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                Commands.literal("diag")
                    .requires(source -> source.getSender().hasPermission("houzicore.admin"))
                    .executes(ctx -> {
                        executeDiag(ctx.getSource(), plugin);
                        return 1;
                    })
                    .then(Commands.literal("cleanup")
                        .executes(ctx -> {
                            executeCleanup(ctx.getSource(), plugin);
                            return 1;
                        })
                    )
                    .build()
            );
        });
    }

    private static void executeDiag(CommandSourceStack source, ArcadeManager plugin) {
        if (!(source.getSender() instanceof Player)) return;
        Player caller = (Player) source.getSender();

        GameLifecycleGuard guard = plugin.getLifecycleGuard();
        if (guard == null) {
            UtilPlayer.message(caller, F.main("Diag", "GameLifecycleGuard is not initialized."));
            return;
        }

        List<String> lines = guard.generateDiagnostics();
        for (String line : lines) {
            caller.sendMessage(line);
        }

        caller.sendMessage("");
        UtilPlayer.message(caller, F.main("Tip", "Use " + F.elem("/diag cleanup") + " to force emergency cleanup."));
    }

    private static void executeCleanup(CommandSourceStack source, ArcadeManager plugin) {
        if (!(source.getSender() instanceof Player)) return;
        Player caller = (Player) source.getSender();

        GameLifecycleGuard guard = plugin.getLifecycleGuard();
        if (guard == null) {
            UtilPlayer.message(caller, F.main("Diag", "GameLifecycleGuard is not initialized."));
            return;
        }

        int cleaned = guard.forceEmergencyCleanup();
        UtilPlayer.message(caller, F.main("Diag", "Emergency cleanup complete. Resources cleaned: " + F.elem("" + cleaned)));
    }
}
