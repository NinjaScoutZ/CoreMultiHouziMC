package com.houzicore.lobby.hub.bootstrap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.PlayerContextService;
import com.houzicore.shared.api.context.TransitionReason;
import com.houzicore.shared.api.snapshot.PlayerSnapshotService;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.mount.MountManager;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.account.CoreClientManager;

public class LobbyTransitionCoordinator implements Listener {

    private enum ActivityKind {
        FISHING(PlayerContextId.LOBBY_FISHING, "fishing"),
        FARM(PlayerContextId.LOBBY_FARM, "farm"),
        PARKOUR(PlayerContextId.LOBBY_PARKOUR, "parkour");

        private final PlayerContextId contextId;
        private final String snapshotKey;

        ActivityKind(PlayerContextId contextId, String snapshotKey) {
            this.contextId = contextId;
            this.snapshotKey = snapshotKey;
        }
    }

    private final PlayerContextService contextService;
    private final PlayerSnapshotService snapshotService;
    private final LobbyPlayerStateApplier stateApplier;

    private final GadgetManager gadgetManager;
    private final PetManager petManager;
    private final MountManager mountManager;
    private final CoreClientManager clientManager;
    private final Map<UUID, ActivityKind> activeActivities = new HashMap<>();

    public LobbyTransitionCoordinator(PlayerContextService contextService, PlayerSnapshotService snapshotService, LobbyPlayerStateApplier stateApplier, GadgetManager gadgetManager, PetManager petManager, MountManager mountManager, CoreClientManager clientManager) {
        this.contextService = contextService;
        this.snapshotService = snapshotService;
        this.stateApplier = stateApplier;
        this.gadgetManager = gadgetManager;
        this.petManager = petManager;
        this.mountManager = mountManager;
        this.clientManager = clientManager;
    }

    /**
     * Called when a player enters the designated Arena Prep zone.
     * Takes an inventory snapshot and applies LOBBY_ARENA_PREP intent.
     */
    public void enterArenaPrep(Player player) {
        // Suspend cosmetic intents explicitly
        suspendCosmetics(player);

        snapshotService.capture(player, "arena");
        contextService.transition(player, PlayerContextId.LOBBY_ARENA_PREP, TransitionReason.GAME_START);
        stateApplier.applyContextState(player, PlayerContextId.LOBBY_ARENA_PREP);
        
        if (com.houzicore.shared.core.combat.legacy.LegacyCombatManager.get() != null) {
            com.houzicore.shared.core.combat.legacy.LegacyCombatManager.get().enableFor(player);
            boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
            com.houzicore.shared.common.util.UtilPlayer.message(player, com.houzicore.shared.common.util.F.main("Arena", 
                isThai ? com.houzicore.shared.common.util.C.cRed + "ระบบเปิดใช้งานโหมด Legacy Combat (1.8 PvP)!" 
                       : com.houzicore.shared.common.util.C.cRed + "Legacy Combat (1.8 PvP) Enabled!"));
        }
    }

    /**
     * Called when the countdown completes and fighting actually begins.
     * Apples LOBBY_ARENA_LIVE iron armor loadout.
     */
    public void enterArenaLive(Player player) {
        contextService.transition(player, PlayerContextId.LOBBY_ARENA_LIVE, TransitionReason.GAME_START);
        stateApplier.applyContextState(player, PlayerContextId.LOBBY_ARENA_LIVE);
    }

    /**
     * Called when a player wins the Arena match and goes back to waiting.
     * Does NOT snapshot them (they already have a Lobby snapshot), just changes context back to Prep.
     */
    public void returnToArenaPrep(Player player) {
        contextService.transition(player, PlayerContextId.LOBBY_ARENA_PREP, TransitionReason.GAME_END);
        stateApplier.applyContextState(player, PlayerContextId.LOBBY_ARENA_PREP);
    }

    /**
     * Called when a player leaves the Arena (death, walked out, quit).
     * Restores snapshot and LOBBY_FREE intent cleanly.
     */
    public void exitArena(Player player) {
        contextService.transition(player, PlayerContextId.LOBBY_FREE, TransitionReason.GAME_END);
        stateApplier.applyContextState(player, PlayerContextId.LOBBY_FREE);
        snapshotService.restore(player, "arena");
        
        if (com.houzicore.shared.core.combat.legacy.LegacyCombatManager.get() != null) {
            com.houzicore.shared.core.combat.legacy.LegacyCombatManager.get().disableFor(player);
        }

        // Resume cosmetic intents explicitly
        resumeCosmetics(player);
    }

    // ─── Fishing Transitions ───

    /**
     * Called when a player enters the Fishing zone.
     * Takes an inventory snapshot and applies LOBBY_ACTIVITY intent (fishing rod only).
     */
    public boolean enterFishing(Player player) {
        return enterActivity(player, ActivityKind.FISHING);
    }

    /**
     * Called when a player leaves the Fishing zone.
     * Restores snapshot and LOBBY_FREE intent cleanly.
     */
    public void exitFishing(Player player) {
        exitActivity(player, ActivityKind.FISHING);
    }

    /**
     * Helper: check if player is currently in a fishing context.
     */
    public boolean isInFishing(Player player) {
        return getActivityKind(player) == ActivityKind.FISHING;
    }

    public boolean enterFarm(Player player) {
        return enterActivity(player, ActivityKind.FARM);
    }

    public void exitFarm(Player player) {
        exitActivity(player, ActivityKind.FARM);
    }

    public boolean isInFarm(Player player) {
        return getActivityKind(player) == ActivityKind.FARM;
    }

    public boolean enterParkour(Player player) {
        return enterActivity(player, ActivityKind.PARKOUR);
    }

    public void exitParkour(Player player) {
        exitActivity(player, ActivityKind.PARKOUR);
    }

    public boolean isInParkour(Player player) {
        return getActivityKind(player) == ActivityKind.PARKOUR;
    }

    public boolean isInAnyLobbyActivity(Player player) {
        return activeActivities.containsKey(player.getUniqueId());
    }

    public void abandonActivity(Player player) {
        activeActivities.remove(player.getUniqueId());
    }

    private boolean enterActivity(Player player, ActivityKind activity) {
        ActivityKind current = getActivityKind(player);
        if (current == activity) {
            return true;
        }

        PlayerContextId currentContext = contextService.getCurrentContextId(player);
        if (currentContext == PlayerContextId.LOBBY_ARENA_PREP || currentContext == PlayerContextId.LOBBY_ARENA_LIVE) {
            return false;
        }

        if (current != null) {
            exitActivity(player, current);
        }

        suspendCosmetics(player);
        snapshotService.capture(player, activity.snapshotKey);
        contextService.transition(player, activity.contextId, TransitionReason.GAME_START);
        stateApplier.applyContextState(player, activity.contextId);
        activeActivities.put(player.getUniqueId(), activity);
        return true;
    }

    private void exitActivity(Player player, ActivityKind activity) {
        ActivityKind current = getActivityKind(player);
        if (current != activity) {
            return;
        }

        activeActivities.remove(player.getUniqueId());
        contextService.transition(player, PlayerContextId.LOBBY_FREE, TransitionReason.GAME_END);
        stateApplier.applyContextState(player, PlayerContextId.LOBBY_FREE);
        snapshotService.restore(player, activity.snapshotKey);
        resumeCosmetics(player);
    }

    private ActivityKind getActivityKind(Player player) {
        return activeActivities.get(player.getUniqueId());
    }

    private void suspendCosmetics(Player player) {
        gadgetManager.suspend(player);
        petManager.suspend(player);
        mountManager.suspend(player);
    }

    private void resumeCosmetics(Player player) {
        gadgetManager.resume(player);
        petManager.resume(player);
        mountManager.resume(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Enforce explicit quit cleanup without restoring runtime states.
        // If they were in Arena, dropping context and snapshot prevents giving items 
        // to a disconnecting player or spawning cosmetic entities into the world.
        PlayerContextId currentContext = contextService.getCurrentContextId(event.getPlayer());
        if (currentContext == PlayerContextId.LOBBY_ARENA_PREP || currentContext == PlayerContextId.LOBBY_ARENA_LIVE) {
            contextService.transition(event.getPlayer(), PlayerContextId.LOBBY_FREE, TransitionReason.GAME_END);
            // We deliberately skip stateApplier and snapshotService.restore.
            // Cosmetics clean their own suspension states naturally via their quit handlers.
        }
        if (currentContext == PlayerContextId.LOBBY_ACTIVITY
            || currentContext == PlayerContextId.LOBBY_FISHING
            || currentContext == PlayerContextId.LOBBY_FARM
            || currentContext == PlayerContextId.LOBBY_PARKOUR) {
            contextService.transition(event.getPlayer(), PlayerContextId.LOBBY_FREE, TransitionReason.GAME_END);
        }
        activeActivities.remove(event.getPlayer().getUniqueId());
    }

    // Command handling explicitly provided by LobbyBootstrap or a separate command mapping later if needed
    // /ctx reconcile <player> implementation:
    public void reconcile(org.bukkit.command.CommandSender sender, String[] args) {
        if (sender instanceof Player && !clientManager.Get(((Player) sender).getName()).GetRank().Has(Rank.DEVELOPER)) {
            sender.sendMessage("§cInsufficient privileges.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /ctx reconcile <player>");
            return;
        }

        Player target = org.bukkit.Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }

        exitArena(target);
        sender.sendMessage("§aReconciled context transitions for " + target.getName());
    }
}
