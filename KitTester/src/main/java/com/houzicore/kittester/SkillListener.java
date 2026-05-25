package com.houzicore.kittester;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class SkillListener implements Listener {

    private final KitTester plugin;

    public SkillListener(KitTester plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUseSkill(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Check if right click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_ROD) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().contains("Skill Tester Wand")) {
            return;
        }

        // ==========================================
        // พื้นที่ทดสอบสกิล (Skill Prototyping Area)
        // ==========================================
        
        // ตัวอย่าง: ทดสอบสกิล Falcon (ปล่อยนก + เอฟเฟกต์)
        testFalconSkill(player);
        
        // ยกเลิก Event เพื่อไม่ให้เกิดบล็อกไฟ (ถ้าเผลอคลิกพื้น)
        event.setCancelled(true);
    }

    private void testFalconSkill(Player player) {
        Location spawnLoc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.5));
        
        // 1. เสก Entity (นกแก้ว แทนเหยี่ยว)
        Parrot falcon = (Parrot) player.getWorld().spawnEntity(spawnLoc, EntityType.PARROT);
        falcon.setAdult();
        falcon.setTamed(true);
        falcon.setOwner(player);
        
        // 2. ใส่ความเร็วพุ่งไปข้างหน้า
        Vector velocity = player.getLocation().getDirection().multiply(1.5);
        falcon.setVelocity(velocity);
        
        // 3. ใส่ Sound Effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PARROT_FLY, 1.0f, 1.0f);
        
        // 4. ใส่ Particle Effect
        player.getWorld().spawnParticle(Particle.CLOUD, spawnLoc, 10, 0.2, 0.2, 0.2, 0.1);
        
        player.sendMessage(ChatColor.AQUA + "ทดสอบสกิล: " + ChatColor.YELLOW + "Falcon Launched!");
    }
}
