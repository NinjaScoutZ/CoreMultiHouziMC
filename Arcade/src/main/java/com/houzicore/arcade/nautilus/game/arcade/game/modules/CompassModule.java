package com.houzicore.arcade.nautilus.game.arcade.game.modules;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;

import java.util.HashSet;

public class CompassModule extends GameModule<Game> {

    private boolean _giveItem = true;

    public CompassModule(Game game) {
        super(game);
    }

    public CompassModule setGiveItem(boolean giveItem) {
        _giveItem = giveItem;
        return this;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;
        if (!_active || !_game.IsLive()) return;

        for (Player player : _game.GetPlayers(true)) {
            // Only provide tracking compass to alive players, spectator compass is global
            if (!_game.IsAlive(player)) continue;

            GameTeam team = _game.GetTeam(player);

            Player target = null;
            GameTeam targetTeam = null;
            double bestDist = 0;

            for (Player other : _game.GetPlayers(true)) {
                if (other.equals(player)) continue;

                GameTeam otherTeam = _game.GetTeam(other);

                // Same Team (Not Solo Game) && Alive
                if (_game.GetTeamList().size() > 1 && (team != null && team.equals(otherTeam)))
                    continue;

                double dist = UtilMath.offset(player, other);

                if (target == null || dist < bestDist) {
                    target = other;
                    targetTeam = otherTeam;
                    bestDist = dist;
                }
            }

            if (target != null) {
                if (_giveItem) {
                    if (!player.getInventory().contains(Material.COMPASS)) {
                        if (player.getOpenInventory() == null || player.getOpenInventory().getCursor() == null || player.getOpenInventory().getCursor().getType() != Material.COMPASS) {
                            ItemStack stack = new ItemStack(Material.COMPASS);
                            ItemMeta itemMeta = stack.getItemMeta();
                            itemMeta.setDisplayName("§b🔮 §7" + com.houzicore.shared.common.util.UtilText.toSmallCaps("Tracking compass"));
                            stack.setItemMeta(itemMeta);
                            player.getInventory().addItem(stack);
                        }
                    }
                }

                player.setCompassTarget(target.getLocation());

                double heightDiff = target.getLocation().getY() - player.getLocation().getY();
                String hFlag = heightDiff > 0 ? "§a+" : "§c";
                String arrow = getDirectionArrow(player.getLocation(), target.getLocation());
                String itemDisplay = "§b🔮 §7Tracking §e" + target.getName() + "  §8|  §7🧭 " + UtilMath.trim(1, bestDist) + "m  §8|  §7" + arrow + " " + hFlag + UtilMath.trim(1, Math.abs(heightDiff)) + "m";

                for (int i : player.getInventory().all(Material.COMPASS).keySet()) {
                    ItemStack stack = player.getInventory().getItem(i);
                    ItemMeta itemMeta = stack.getItemMeta();
                    itemMeta.setDisplayName(itemDisplay);
                    stack.setItemMeta(itemMeta);
                    player.getInventory().setItem(i, stack);
                }
            }
        }
    }

    @EventHandler
    public void DropItem(PlayerDropItemEvent event) {
        if (!_active) return;
        if (!_game.IsAlive(event.getPlayer())) return;

        if (event.getItemDrop().getItemStack() == null || event.getItemDrop().getItemStack().getType() != Material.COMPASS)
            return;

        event.setCancelled(true);
        UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot drop " + F.item("Target Compass") + "."));
    }

    @EventHandler
    public void DeathRemove(PlayerDeathEvent event) {
        if (!_active) return;
        if (!_game.IsAlive(event.getEntity())) return;

        HashSet<ItemStack> remove = new HashSet<>();
        for (ItemStack item : event.getDrops()) {
            if (item != null && item.getType() == Material.COMPASS) {
                remove.add(item);
            }
        }

        for (ItemStack item : remove) {
            event.getDrops().remove(item);
        }
    }

    private String getDirectionArrow(org.bukkit.Location from, org.bukkit.Location to) {
        if (from == null || to == null) return "•";
        org.bukkit.util.Vector direction = to.toVector().subtract(from.toVector());
        if (direction.lengthSquared() == 0) return "•";

        double angle = Math.atan2(direction.getZ(), direction.getX());
        double yaw = (angle * 180 / Math.PI) - 90;
        double relativeYaw = yaw - from.getYaw();
        relativeYaw = (relativeYaw % 360 + 360) % 360;

        if (relativeYaw >= 337.5 || relativeYaw < 22.5) return "↑";
        if (relativeYaw >= 22.5 && relativeYaw < 67.5) return "↗";
        if (relativeYaw >= 67.5 && relativeYaw < 112.5) return "→";
        if (relativeYaw >= 112.5 && relativeYaw < 157.5) return "↘";
        if (relativeYaw >= 157.5 && relativeYaw < 202.5) return "↓";
        if (relativeYaw >= 202.5 && relativeYaw < 247.5) return "↙";
        if (relativeYaw >= 247.5 && relativeYaw < 292.5) return "←";
        if (relativeYaw >= 292.5 && relativeYaw < 337.5) return "↖";

        return "•";
    }

    private boolean shouldDisplayHeldCompassActionBar(Player player) {
        if (_game.isContextRuntime()) return false;
        return UtilPlayer.is1_8(player) && UtilGear.isMat(player.getItemInHand(), Material.COMPASS);
    }
}
