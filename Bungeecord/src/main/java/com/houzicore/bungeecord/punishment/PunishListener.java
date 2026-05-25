package com.houzicore.bungeecord.punishment;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.api.chat.TextComponent;

import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PunishListener implements Listener {

    private final Plugin plugin;
    private BasicDataSource dataSource;

    public PunishListener(Plugin plugin) {
        this.plugin = plugin;
        initDatabase();
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    private void initDatabase() {
        dataSource = new BasicDataSource();
        
        String url = System.getenv("HOUZI_ACCOUNT_URL");
        if (url == null) url = "jdbc:mysql://127.0.0.1:3306/account?autoReconnect=true&useSSL=false";
        
        String user = System.getenv("HOUZI_ACCOUNT_USER");
        if (user == null) user = "root";
        
        String pass = System.getenv("HOUZI_ACCOUNT_PASS");
        if (pass == null) pass = "";

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(pass);
        dataSource.setInitialSize(2);
        dataSource.setMaxTotal(5);
    }

    public void shutdown() {
        try {
            if (dataSource != null) dataSource.close();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to close Bungee DB connection: " + e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(LoginEvent event) {
        event.registerIntent(plugin);
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT reason, admin, duration, created FROM punishments WHERE targetName = ? AND sentence = 'Ban' AND active = 1"
                 )) {
                
                ps.setString(1, event.getConnection().getName());
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String reason = rs.getString("reason");
                        long duration = rs.getLong("duration");
                        long created = rs.getTimestamp("created").getTime();
                        
                        long unbanTime = duration == -1 ? -1 : created + (duration * 60L * 60L * 1000L); // duration in hours
                        
                        if (unbanTime == -1 || unbanTime > System.currentTimeMillis()) {
                            String timeStr = unbanTime == -1 ? "Permanent" : new java.util.Date(unbanTime).toString();
                            
                            String banMsg = "§cYou are banned from this server!\n" +
                                            "§7Reason: §f" + reason + "\n" +
                                            "§7Expires: §f" + timeStr;
                            
                            event.setCancelled(true);
                            event.setCancelReason(TextComponent.fromLegacyText(banMsg));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error checking punishments: " + e.getMessage());
                // If DB fails, we probably should let them in or kick them. Usually fallback to allow in or safe-kick.
                // We'll safe-kick to be secure.
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText("§cCould not verify account status. Please try again later."));
            } finally {
                event.completeIntent(plugin);
            }
        });
    }
}
