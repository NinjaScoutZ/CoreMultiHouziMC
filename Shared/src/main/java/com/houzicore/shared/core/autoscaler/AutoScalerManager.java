package com.houzicore.shared.core.autoscaler;

import org.bukkit.plugin.java.JavaPlugin;
import com.houzicore.shared.serverdata.servers.ServerRepository;
import com.houzicore.shared.serverdata.data.MinecraftServer;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

public class AutoScalerManager implements Runnable {

    private JavaPlugin _plugin;
    private ServerRepository _repository;
    private MCSManagerAPI _api;
    private String[] _arcadePool;
    
    // Track instances we recently requested to start so we don't spam start requests
    private Set<String> _bootingInstances = new HashSet<>();

    public AutoScalerManager(JavaPlugin plugin, ServerRepository repository) {
        _plugin = plugin;
        _repository = repository;
        loadProperties();
        if (_api != null && _arcadePool != null && _arcadePool.length > 0) {
            Bukkit.getScheduler().runTaskTimer(_plugin, this, 200L, 200L); // check every 10 seconds
            _plugin.getLogger().info("[AutoScaler] Initialized. Monitoring " + _arcadePool.length + " standby instances.");
        }
    }

    private void loadProperties() {
        try {
            File propFile = new File("autoscaler.properties");
            if (!propFile.exists()) {
                propFile.createNewFile();
                java.io.FileOutputStream out = new java.io.FileOutputStream(propFile);
                out.write(("mcsmanager.url=http://localhost:23333\n" +
                           "mcsmanager.apikey=d55cae3864564cecfb55e9eec1047ff91d75d7d08e1cb23\n" +
                           "mcsmanager.daemon=1314db0099ad4403a7ffe49772278a78\n" +
                           "mcsmanager.pool.arcade=Arcade1,Arcade2\n").getBytes());
                out.close();
            }

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(propFile)) {
                props.load(fis);
                _api = new MCSManagerAPI(
                        props.getProperty("mcsmanager.url"),
                        props.getProperty("mcsmanager.apikey"),
                        props.getProperty("mcsmanager.daemon")
                );
                _arcadePool = props.getProperty("mcsmanager.pool.arcade").split(",");
            }
        } catch (Exception e) {
            org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        // Example naive scaler: we want at least 1 waiting Arcade server for 'MIN' (Mixed Arcade) group
        int waitingMinCount = 0;
        
        try {
            Collection<MinecraftServer> statuses = _repository.getServerStatuses();
            for (MinecraftServer status : statuses) {
                // Remove from booting tracking if it's already registered in Redis
                _bootingInstances.remove(status.getName());
                
                // Usually Arcade mode states are represented by ServerGroup/Motd. 
                // Let's assume if it has 0 players or is in WAITING state, we count it.
                if (status.getGroup().equalsIgnoreCase("MIN") || status.getName().startsWith("Arcade")) {
                    if (status.getPlayerCount() < 10) { // Naive condition for 'not currently completely full'
                        waitingMinCount++;
                    }
                }
            }
        } catch (Exception ex) {
            org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
        }

        // If no available slots/servers and we aren't currently waiting for one to boot
        if (waitingMinCount == 0 && _bootingInstances.isEmpty()) {
            spinUpNextAvailableArcade();
        }
    }

    private void spinUpNextAvailableArcade() {
        // Iterate over pool and just try to start the first one that is considered "dead" (not in Redis)
        try {
            for (String instanceUuid : _arcadePool) {
                MinecraftServer existing = _repository.getServerStatus(instanceUuid);
                
                // If it's not in Redis, it's stopped/dead
                if (existing == null && !_bootingInstances.contains(instanceUuid)) {
                    _plugin.getLogger().info("[AutoScaler] Insufficient active Arcade servers! Spinning up Standby Instance: " + instanceUuid);
                    
                    if (_api.startServer(instanceUuid)) {
                        _bootingInstances.add(instanceUuid);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
