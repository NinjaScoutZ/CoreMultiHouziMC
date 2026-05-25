package com.houzicore.shared.core.combat.legacy;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;

public class LegacyCombatManager extends MiniPlugin {

    private static LegacyCombatManager _instance;
    private final Set<Player> _activePlayers = new HashSet<>();
    private boolean _global = false;

    private final java.util.Map<java.util.UUID, Long> _lastHealTick = new java.util.HashMap<>();
    private long _tickCounter = 0;

    public LegacyCombatManager(JavaPlugin plugin) {
        super("Legacy Combat", plugin);
        _instance = this;
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> _tickCounter++, 1L, 1L);
    }

    public static LegacyCombatManager get() {
        return _instance;
    }

    public void setGlobal(boolean global) {
        _global = global;
    }

    public void enableFor(Player player) {
        _activePlayers.add(player);
        setAttackSpeed(player, 100.0D);
        player.setMaximumNoDamageTicks(20);
    }

    public void disableFor(Player player) {
        _activePlayers.remove(player);
        setAttackSpeed(player, 4.0D); // Default Minecraft attack speed base
        player.setMaximumNoDamageTicks(20);
    }

    public boolean isEnabledFor(Player player) {
        if (player == null) return false;
        return _global || _activePlayers.contains(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isEnabledFor(event.getPlayer())) {
            setAttackSpeed(event.getPlayer(), 100.0D);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        _activePlayers.remove(event.getPlayer());
        _lastHealTick.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (isEnabledFor(event.getPlayer())) {
            runSyncLater(() -> setAttackSpeed(event.getPlayer(), 100.0D), 1L);
        }
    }

    private void setAttackSpeed(Player player, double speed) {
        AttributeInstance attackSpeed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.setBaseValue(speed);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSweepAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (!isEnabledFor(player)) return;

        if (event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        if (!isEnabledFor(event.getPlayer())) return;

        if (event.getOffHandItem() != null && !event.getOffHandItem().getType().isAir()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onOffhandInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CRAFTING) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!isEnabledFor(player)) return;

        if (event.getClick() != ClickType.SWAP_OFFHAND && event.getSlot() != 40) return;

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWeaponDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        if (!isEnabledFor(player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) return;
        String name = item.getType().name();

        double baseDamage = event.getDamage(); // Default
        boolean modified = false;

        if (name.endsWith("_SWORD")) {
            modified = true;
            switch(name) {
                case "WOODEN_SWORD": case "GOLDEN_SWORD": baseDamage = 4.0; break;
                case "STONE_SWORD": baseDamage = 5.0; break;
                case "IRON_SWORD": baseDamage = 6.0; break;
                case "DIAMOND_SWORD": baseDamage = 8.0; break;
                case "NETHERITE_SWORD": baseDamage = 9.0; break;
                default: baseDamage = 4.0; break;
            }
        } else if (name.endsWith("_AXE")) {
            modified = true;
            switch(name) {
                case "WOODEN_AXE": case "GOLDEN_AXE": baseDamage = 3.0; break;
                case "STONE_AXE": baseDamage = 4.0; break;
                case "IRON_AXE": baseDamage = 5.0; break;
                case "DIAMOND_AXE": baseDamage = 6.0; break;
                case "NETHERITE_AXE": baseDamage = 7.0; break;
                default: baseDamage = 3.0; break;
            }
        } else if (name.endsWith("_SPADE") || name.endsWith("_SHOVEL")) {
            modified = true;
            switch(name) {
                case "WOODEN_SHOVEL": case "GOLDEN_SHOVEL": baseDamage = 1.0; break;
                case "STONE_SHOVEL": baseDamage = 2.0; break;
                case "IRON_SHOVEL": baseDamage = 3.0; break;
                case "DIAMOND_SHOVEL": baseDamage = 4.0; break;
                case "NETHERITE_SHOVEL": baseDamage = 5.0; break;
                default: baseDamage = 1.0; break;
            }
        }

        if (modified) {
            int level = item.getEnchantmentLevel(Enchantment.SHARPNESS);
            if (level > 0) {
                baseDamage += (1.25 * level);
            }

            boolean isCritical = player.getFallDistance() > 0.0F 
                    && !player.isOnGround() 
                    && !player.isInWater()
                    && !player.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS)
                    && player.getVehicle() == null;

            if (isCritical) {
                baseDamage *= 1.5;
            }

            // 1.8 Strength potion logic
            if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH)) {
                int strengthLevel = player.getPotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH).getAmplifier() + 1;
                baseDamage = baseDamage * (1.0 + (1.3 * strengthLevel)); // +130% per level like 1.8
            }

            event.setDamage(baseDamage);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileKnockback(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof org.bukkit.entity.Snowball) && !(event.getDamager() instanceof org.bukkit.entity.Egg)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player victim = (Player) event.getEntity();
        if (!isEnabledFor(victim)) return;

        // Give it a tiny bit of damage so knockback applies
        event.setDamage(0.0001);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGoldenAppleConsume(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!isEnabledFor(player)) return;
        
        ItemStack item = event.getItem();
        if (item.getType() == org.bukkit.Material.GOLDEN_APPLE) {
            runSyncLater(() -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, 100, 1)); // Regen 2 (5s)
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.ABSORPTION, 2400, 0)); // Absorption 1 (2m)
            }, 1L);
        } else if (item.getType().name().equals("ENCHANTED_GOLDEN_APPLE")) {
            runSyncLater(() -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, 600, 4)); // Regen 5 (30s)
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.RESISTANCE, 6000, 0)); // Resistance 1 (5m)
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, 6000, 0)); // Fire Res 1 (5m)
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.ABSORPTION, 2400, 3)); // Absorption 4 (2m)
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKnockback(org.bukkit.event.entity.EntityKnockbackByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!isEnabledFor(player)) return;

        org.bukkit.util.Vector finalKb = event.getFinalKnockback();
        
        // 1.9+ knockback tends to float players too much (Y is too high).
        // Reduce Y to limit floating, and slightly increase horizontal to simulate 1.8 snappiness.
        double newY = Math.min(0.4D, finalKb.getY() * 0.70D);
        double newX = finalKb.getX() * 1.10D;
        double newZ = finalKb.getZ() * 1.10D;
        
        event.setFinalKnockback(new org.bukkit.util.Vector(newX, newY, newZ));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPearlLaunch(org.bukkit.event.entity.ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof org.bukkit.entity.EnderPearl)) return;
        if (!(e.getEntity().getShooter() instanceof Player)) return;
        Player p = (Player) e.getEntity().getShooter();
        if (!isEnabledFor(p)) return;

        runSyncLater(() -> p.setCooldown(org.bukkit.Material.ENDER_PEARL, 0), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFishingRodDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof org.bukkit.entity.FishHook)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!isEnabledFor(player)) return;

        event.setDamage(0.0001); // Give it a tiny bit of damage so knockback applies
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRegen(org.bukkit.event.entity.EntityRegainHealthEvent e) {
        if (e.getEntityType() != org.bukkit.entity.EntityType.PLAYER || 
            e.getRegainReason() != org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED) return;

        Player p = (Player) e.getEntity();
        if (!isEnabledFor(p)) return;

        e.setCancelled(true);
        float prevExh = p.getExhaustion();

        Long lastTick = _lastHealTick.get(p.getUniqueId());
        if (lastTick != null && _tickCounter - lastTick < 80L) { // 80 ticks = 4 seconds
            runSyncLater(() -> p.setExhaustion(prevExh), 1L);
            return;
        }

        double maxHealth = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        if (p.getHealth() < maxHealth) {
            p.setHealth(Math.min(p.getHealth() + 1.0, maxHealth));
            _lastHealTick.put(p.getUniqueId(), _tickCounter);
        }

        runSyncLater(() -> p.setExhaustion(prevExh + 3.0f), 1L); // 1.8 exhaustion rate for healing
    }
}
