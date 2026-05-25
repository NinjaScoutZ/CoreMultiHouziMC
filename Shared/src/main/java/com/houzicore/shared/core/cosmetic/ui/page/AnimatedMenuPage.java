package com.houzicore.shared.core.cosmetic.ui.page;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.AnimationType;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.cosmetic.ui.GuiUtil;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.page.ShopPageBase;

/**
 * Base class for animated cosmetic GUI pages.
 * Runs a BukkitRunnable every 3 ticks to update registered animated item slots.
 * Auto-cancels when the player closes the inventory.
 */
public abstract class AnimatedMenuPage extends ShopPageBase<CosmeticManager, CosmeticShop> {

    /** Holds the base text + animation type for each animated slot */
    private final Map<Integer, AnimationEntry> _animatedSlots = new HashMap<>();
    private BukkitTask _animationTask;
    private int _tick = 0;

    public AnimatedMenuPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
            DonationManager donationManager, String name, Player player) {
        super(plugin, shop, clientManager, donationManager, name, player, 54);
    }

    public AnimatedMenuPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
            DonationManager donationManager, String name, Player player, int slots) {
        super(plugin, shop, clientManager, donationManager, name, player, slots);
    }

    /**
     * Register a slot for animation. The item in this slot will have its display name
     * updated every 3 ticks using the specified animation type.
     */
    protected void registerAnimatedSlot(int slot, String baseText, AnimationType type, ChatColor primaryColor) {
        _animatedSlots.put(slot, new AnimationEntry(baseText, type, primaryColor));
    }

    /**
     * Start the animation loop. Call this AFTER buildPage() in subclass constructors.
     */
    protected void startAnimations() {
        if (_animationTask != null) return;
        if (_animatedSlots.isEmpty()) return;

        _animationTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (player == null || !player.isOnline()) {
                    cancel();
                    return;
                }

                _tick++;

                for (Map.Entry<Integer, AnimationEntry> entry : _animatedSlots.entrySet()) {
                    int slot = entry.getKey();
                    AnimationEntry anim = entry.getValue();
                    ItemStack item = getItem(slot);
                    if (item == null) continue;

                    ItemMeta meta = item.getItemMeta();
                    if (meta == null) continue;

                    String animated = GuiUtil.animateText(anim.baseText, _tick, anim.type, anim.color);
                    meta.setDisplayName(animated);
                    item.setItemMeta(meta);
                }
            }
        }.runTaskTimer(getPlugin().getPlugin(), 3L, 3L);
    }

    @Override
    public void playerClosed() {
        stopAnimations();
        super.playerClosed();
    }

    @Override
    public void dispose() {
        stopAnimations();
        super.dispose();
    }

    private void stopAnimations() {
        if (_animationTask != null) {
            _animationTask.cancel();
            _animationTask = null;
        }
        _animatedSlots.clear();
    }

    protected int getTick() {
        return _tick;
    }

    /** Internal holder for per-slot animation config */
    private static class AnimationEntry {
        final String baseText;
        final AnimationType type;
        final ChatColor color;

        AnimationEntry(String baseText, AnimationType type, ChatColor color) {
            this.baseText = baseText;
            this.type = type;
            this.color = color;
        }
    }
}
