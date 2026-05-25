package com.houzicore.shared.core.autoscaler;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MCSManagerAPI {

    private String panelUrl;
    private String apiKey;
    private String daemonId;

    public MCSManagerAPI(String panelUrl, String apiKey, String daemonId) {
        this.panelUrl = panelUrl;
        this.apiKey = apiKey;
        this.daemonId = daemonId;
    }

    public boolean sendCommand(String instanceId, String action) {
        try {
            // e.g. /api/protected_instance/open?apikey=xxx&daemonId=yyy&uuid=zzz
            String urlString = String.format("%s/api/protected_instance/%s?apikey=%s&daemonId=%s&uuid=%s",
                    panelUrl, action, apiKey, daemonId, instanceId);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 204) {
                return true;
            } else {
                InputStream is = conn.getErrorStream();
                if (is != null) {
                    byte[] data = new byte[is.available()];
                    is.read(data);
                }
            }
        } catch (Exception e) {
            org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }

    public boolean startServer(String instanceId) {
        return sendCommand(instanceId, "open");
    }

    public boolean stopServer(String instanceId) {
        return sendCommand(instanceId, "stop");
    }
}
