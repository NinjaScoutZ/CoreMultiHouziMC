package com.houzicore.lobby.hub.modules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Color;

import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.lobby.hub.server.ServerManager;
import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.plugin.PluginRegistry;

public class AvatarNpcManager implements org.bukkit.event.Listener {

    private final JavaPlugin _plugin;
    private final HologramManager _hologramManager;
    private final LobbyNpcManager _npcManager;
    
    private final Map<Integer, Location> _slotLocations = new HashMap<>();
    private final Map<Integer, PacketNpc> _activeNpcs = new HashMap<>();

    public AvatarNpcManager(JavaPlugin plugin, HologramManager hologramManager, LobbyNpcManager npcManager) {
        _plugin = plugin;
        _hologramManager = hologramManager;
        _npcManager = npcManager;

        initDatabase();
        
        Bukkit.getPluginManager().registerEvents(this, _plugin);
        Bukkit.getScheduler().runTaskLater(_plugin, this::syncFromDatabase, 60L);
    }

    @org.bukkit.event.EventHandler
    public void onNpcUpdate(com.houzicore.shared.core.npc.event.NpcUpdateEvent event) {
        syncFromDatabase();
    }

    public void registerSlotLocation(int slotId, Location loc) {
        // Auto-aim towards spawn
        org.bukkit.util.Vector faceDir = new Location(loc.getWorld(), 0.5, 128.0, 0.5).toVector().subtract(loc.toVector());
        faceDir.setY(0);
        if (faceDir.lengthSquared() > 0.001) {
            loc.setDirection(faceDir);
        }
        _slotLocations.put(slotId, loc);
    }

    private void initDatabase() {
        Bukkit.getScheduler().runTaskAsynchronously(_plugin, () -> {
            try (Connection conn = DBPool.ACCOUNT.getConnection()) {
                String createTable = "CREATE TABLE IF NOT EXISTS `server_npc_slots` (" +
                    "`slot_id` INT NOT NULL, " +
                    "`enabled` BOOLEAN DEFAULT 0, " +
                    "`server_key` VARCHAR(100), " +
                    "`display_name` VARCHAR(100), " +
                    "`skin_name` VARCHAR(255), " +
                    "`skin_value` TEXT, " +
                    "`skin_signature` TEXT, " +
                    "`extra_text` VARCHAR(255), " +
                    "PRIMARY KEY (`slot_id`))";
                try (PreparedStatement ps = conn.prepareStatement(createTable)) {
                    ps.executeUpdate();
                }

                Object[][] defaultSlots = {
                    { 1, "Prop Rush", "§aBlock Hunt", "MHF_Pig" },
                    { 2, "MineStrike", "§6MineStrike", "MHF_Skeleton" },
                    { 3, "Survival Primal Games", "§cSurvival Primal Games", "MHF_PigZombie" },
                    { 4, "Hole in the Wall", "§bHole in the Wall", "MHF_Slime" },
                    { 5, "Bomb Lobbers", "§6Bomb Lobbers", "MHF_Blaze" },
                    { 6, "MineWare", "§dMineWare", "MHF_Villager" },
                    { 7, "Sneaky Assassins", "§5Sneaky Assassins", "MHF_Enderman" },
                    { 8, "Castle Siege", "§5Castle Siege", "MHF_Golem" },
                    { 9, "Snow Fight", "§bSnow Fight", "MHF_SnowGolem" },
                    { 10, "Evolution", "§aEvolution", "MHF_Spider" },
                    { 11, "Tug of Wool", "§eTug of Wool", "MHF_Sheep" },
                    { 12, "Super Stacker", "§aSuper Stacker", "MHF_Ocelot" },
                    { 13, "Squid Shooter", "§dSquid Shooter", "MHF_Squid" },
                    { 14, "Search and Destroy", "§eSearch and Destroy", "MHF_Creeper" },
                    { 15, "Wizards", "§5Wizards", "MHF_Ghast" }
                };

                for (Object[] slotData : defaultSlots) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT IGNORE INTO `server_npc_slots` (`slot_id`, `enabled`, `server_key`, `display_name`, `skin_name`) " +
                            "VALUES (?, 1, ?, ?, ?)")) {
                        ps.setInt(1, (Integer) slotData[0]);
                        ps.setString(2, (String) slotData[1]);
                        ps.setString(3, (String) slotData[2]);
                        ps.setString(4, (String) slotData[3]);
                        ps.executeUpdate();
                    }
                }
            } catch (Exception e) {
                _plugin.getLogger().warning("[AvatarNpcManager] Failed to init database: " + e.getMessage());
            }
        });
    }

    private void syncFromDatabase() {
        Bukkit.getScheduler().runTaskAsynchronously(_plugin, () -> {
            try (Connection conn = DBPool.ACCOUNT.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM `server_npc_slots`")) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int slotId = rs.getInt("slot_id");
                        boolean enabled = rs.getBoolean("enabled");
                        String serverKey = rs.getString("server_key");
                        String displayName = rs.getString("display_name");
                        String skinName = rs.getString("skin_name");
                        String extraText = rs.getString("extra_text");
                        String skinValue = rs.getString("skin_value");
                        String skinSignature = rs.getString("skin_signature");
                        
                        Bukkit.getScheduler().runTask(_plugin, () -> updateSlot(slotId, enabled, serverKey, displayName, skinName, skinValue, skinSignature, extraText));
                    }
                }
            } catch (Exception e) {
                _plugin.getLogger().warning("[AvatarNpcManager] DB Sync Error: " + e.getMessage());
            }
        });
    }

    private void updateSlot(int slotId, boolean enabled, String serverKey, String displayName, String skinName, String skinValue, String skinSignature, String extraText) {
        Location loc = _slotLocations.get(slotId);
        if (loc == null) return;

        PacketNpc existing = _activeNpcs.get(slotId);
        
        // Handle Empty Avatar State
        if (!enabled || serverKey == null || serverKey.isEmpty()) {
            if (existing != null) {
                if ("Coming Soon".equals(existing.getProfileName())) return; // Already empty
                _npcManager.removePacketNpc(existing);
                _activeNpcs.remove(slotId);
            }
            
            // Spawn empty avatar
            PacketNpc emptyNpc = _npcManager.spawnPlayerSkinNpc(loc, "§7Coming Soon", null, null, skinName != null && !skinName.isEmpty() ? skinName : "MHF_Question", skinValue, skinSignature, player -> {});
            if (emptyNpc != null) {
                _activeNpcs.put(slotId, emptyNpc);
            }
            return;
        }

        // Check if an update is needed
        if (existing != null) {
            boolean nameMatch = displayName != null && displayName.equals(existing.getProfileName());
            if (nameMatch) {
                if (!"Coming Soon".equals(existing.getProfileName())) {
                    return; 
                }
            }
            _npcManager.removePacketNpc(existing);
            _activeNpcs.remove(slotId);
            loc.getWorld().strikeLightningEffect(loc.clone().subtract(0, 1, 0));
        }

        // Spawn active minigame NPC
        String clickHint = "§e▶ §fClick to Play";
        if (extraText != null && !extraText.isEmpty()) {
            clickHint = extraText.replace("&", "§") + "\n" + clickHint;
        }

        String finalSkin = (skinName == null || skinName.isEmpty()) ? "Steve" : skinName;
        String finalName = (displayName == null || displayName.isEmpty()) ? serverKey : displayName.replace("&", "§");

        PacketNpc activeNpc = _npcManager.spawnPlayerSkinNpc(loc, finalName, serverKey, clickHint, finalSkin, skinValue, skinSignature, player -> {
            try {
                ServerManager sm = PluginRegistry.require(ServerManager.class);
                if (player.isSneaking()) {
                    sm.quickJoin(player, serverKey);
                } else {
                    sm.openServerShop(player, serverKey);
                }
            } catch (Exception e) {
                player.sendMessage("§cServer manager not available.");
            }
        });

        if (activeNpc != null) {
            _activeNpcs.put(slotId, activeNpc);
        }
    }
}
