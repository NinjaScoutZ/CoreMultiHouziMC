package com.houzicore.lobby.hub.modules;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PacketNpc {

    private final JavaPlugin plugin;
    private final int entityId;
    private final UUID uuid;
    private final String skinName;
    private final String profileName;
    private final Location location;
    
    private UserProfile profile;
    private boolean spawned = false;
    private org.bukkit.inventory.ItemStack _mainHandItem = null;

    public PacketNpc(JavaPlugin plugin, String profileName, String skinName, Location location) {
        this.plugin = plugin;
        this.skinName = skinName;
        this.profileName = profileName;
        this.location = location;
        this.uuid = UUID.randomUUID();
        this.entityId = 1000000 + (int)(Math.random() * 100000);
        this.profile = new UserProfile(this.uuid, this.profileName);
    }

    public int getEntityId() {
        return entityId;
    }
    
    public String getProfileName() {
        return profileName;
    }
    
    public Location getLocation() {
        return location;
    }

    public CompletableFuture<Void> fetchSkinAndSpawn() {
        return CompletableFuture.runAsync(() -> {
            if (this.profile.getTextureProperties() == null || this.profile.getTextureProperties().isEmpty()) {
                try {
                    // 1. Get UUID from Name
                    URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + skinName);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    
                    if (connection.getResponseCode() == 200) {
                        JsonObject response = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
                        String uuidStr = response.get("id").getAsString();
                        
                        // 2. Get Profile from UUID
                        URL profileUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr + "?unsigned=false");
                        HttpURLConnection profileConnection = (HttpURLConnection) profileUrl.openConnection();
                        if (profileConnection.getResponseCode() == 200) {
                            JsonObject profileResponse = JsonParser.parseReader(new InputStreamReader(profileConnection.getInputStream())).getAsJsonObject();
                            JsonObject properties = profileResponse.getAsJsonArray("properties").get(0).getAsJsonObject();
                            
                            String value = properties.get("value").getAsString();
                            String signature = properties.has("signature") ? properties.get("signature").getAsString() : null;
                            
                            List<TextureProperty> textures = new ArrayList<>();
                            textures.add(new TextureProperty("textures", value, signature));
                            this.profile.setTextureProperties(textures);
                            
                            plugin.getLogger().info("[PacketNpc] Skin loaded for " + skinName + " -> profile " + profileName);
                        } else {
                            plugin.getLogger().warning("[PacketNpc] Session server returned " + profileConnection.getResponseCode() + " for " + skinName);
                        }
                    } else {
                        plugin.getLogger().warning("[PacketNpc] Mojang API returned " + connection.getResponseCode() + " for " + skinName);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("[PacketNpc] Failed to fetch skin for " + skinName + ": " + e.getMessage());
                }
            }

            Bukkit.getScheduler().runTask(plugin, this::spawnForAll);
        });
    }

    public void setRawSkin(String value, String signature) {
        List<TextureProperty> textures = new ArrayList<>();
        textures.add(new TextureProperty("textures", value, signature));
        this.profile.setTextureProperties(textures);
    }

    public void spawnForAll() {
        spawned = true;
        for (Player p : Bukkit.getOnlinePlayers()) {
            showTo(p);
        }
    }

    public void showTo(Player player) {
        if (!spawned) return;

        // 1. Add to Tablist (required for player entity spawn)
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
            profile, true, 1, GameMode.SURVIVAL, null, null
        );
        WrapperPlayServerPlayerInfoUpdate infoPacket = new WrapperPlayServerPlayerInfoUpdate(
            EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER),
            info
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, infoPacket);

        // 2. Spawn Entity
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
            entityId, java.util.Optional.of(uuid), EntityTypes.PLAYER, 
            location.getPosition(), location.getPitch(), location.getYaw(), location.getYaw(), 0, java.util.Optional.empty()
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);

        // 3. Set Head Rotation
        WrapperPlayServerEntityHeadLook headLook = new WrapperPlayServerEntityHeadLook(entityId, location.getYaw());
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, headLook);

        // 4. Hide the nametag using a Team packet with NameTagVisibility=NEVER
        String teamName = "npc_" + entityId;
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);
        
        WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
            Component.empty(),              // displayName
            Component.empty(),              // prefix
            Component.empty(),              // suffix
            WrapperPlayServerTeams.NameTagVisibility.NEVER,
            WrapperPlayServerTeams.CollisionRule.NEVER,
            NamedTextColor.WHITE,
            WrapperPlayServerTeams.OptionData.NONE
        );
        
        WrapperPlayServerTeams teamPacket = new WrapperPlayServerTeams(
            teamName,
            WrapperPlayServerTeams.TeamMode.CREATE,
            teamInfo,
            profileName
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, teamPacket);

        // Send Equipment if exists
        if (_mainHandItem != null && _mainHandItem.getType() != org.bukkit.Material.AIR) {
            com.github.retrooper.packetevents.protocol.item.ItemStack peItem = 
                io.github.retrooper.packetevents.util.SpigotConversionUtil.fromBukkitItemStack(_mainHandItem);
            com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment equipmentPacket = 
                new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment(entityId, 
                java.util.Collections.singletonList(
                    new com.github.retrooper.packetevents.protocol.player.Equipment(
                        com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND, peItem)
                ));
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, equipmentPacket);
        }

        // 5. Remove from Tablist (hide name in player list tab)
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            try {
                WrapperPlayServerPlayerInfoUpdate.PlayerInfo updateInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                    profile, false, 1, GameMode.SURVIVAL, null, null
                );
                WrapperPlayServerPlayerInfoUpdate hidePacket = new WrapperPlayServerPlayerInfoUpdate(
                    EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED),
                    updateInfo
                );
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, hidePacket);
            } catch (Exception e) {
                // Fallback if UPDATE_LISTED is not supported in this PacketEvents version
                // Do not remove the player info to prevent entity despawn in 1.19.3+
            }
        }, 40L); // 2 seconds — enough for client to render skin
    }

    public void hideFrom(Player player) {
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyPacket);
    }

    public void playAnimation(WrapperPlayServerEntityAnimation.EntityAnimationType type) {
        if (!spawned) return;
        WrapperPlayServerEntityAnimation animation = new WrapperPlayServerEntityAnimation(entityId, type);
        for (Player p : Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, animation);
        }
    }

    private static final double NPC_EYE_HEIGHT = 1.62;

    public void lookAt(org.bukkit.Location targetLoc) {
        if (!spawned) return;
        
        double dx = targetLoc.getX() - location.getX();
        double dz = targetLoc.getZ() - location.getZ();
        double dy = targetLoc.getY() - (location.getY() + NPC_EYE_HEIGHT);
        
        double dist = Math.sqrt(dx*dx + dz*dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(-dy, dist));

        // Update internal location cache
        location.setYaw(yaw);
        location.setPitch(pitch);

        WrapperPlayServerEntityHeadLook headLook = new WrapperPlayServerEntityHeadLook(entityId, yaw);
        WrapperPlayServerEntityRotation rotation = new WrapperPlayServerEntityRotation(entityId, yaw, pitch, true);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, headLook);
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, rotation);
        }
    }

    public void setEquipment(org.bukkit.inventory.ItemStack mainHand) {
        this._mainHandItem = mainHand;
        if (!spawned) return;
        
        if (mainHand == null || mainHand.getType() == org.bukkit.Material.AIR) return;
        
        com.github.retrooper.packetevents.protocol.item.ItemStack peItem = 
            io.github.retrooper.packetevents.util.SpigotConversionUtil.fromBukkitItemStack(mainHand);

        com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment equipmentPacket = 
            new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment(entityId, 
            java.util.Collections.singletonList(
                new com.github.retrooper.packetevents.protocol.player.Equipment(
                    com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND, peItem)
            ));
        
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, equipmentPacket);
        }
    }
}
