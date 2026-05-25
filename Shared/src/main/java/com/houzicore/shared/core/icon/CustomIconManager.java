package com.houzicore.shared.core.icon;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class CustomIconManager {

    private static CustomIconManager instance;
    private final Map<String, IconData> iconCache = new HashMap<>();
    private final File dataFolder;
    private final java.util.List<File> dataFolders = new java.util.ArrayList<>();
    private final Plugin plugin;

    public CustomIconManager(Plugin plugin) {
        instance = this;
        this.plugin = plugin;
        File sharedIcons = new File(plugin.getDataFolder(), "icons");
        if (!sharedIcons.exists()) {
            sharedIcons.mkdirs();
        }
        dataFolders.add(sharedIcons);

        File houziCoreIcons = new File(plugin.getDataFolder().getParentFile(), "HouziCore/icons");
        if (houziCoreIcons.exists() && houziCoreIcons.isDirectory()) {
            dataFolders.add(houziCoreIcons);
            this.dataFolder = houziCoreIcons;
        } else {
            this.dataFolder = sharedIcons;
        }
        loadAllIcons();
    }

    public static CustomIconManager getInstance() {
        return instance;
    }

    public void loadAllIcons() {
        CompletableFuture.runAsync(() -> {
            for (File folder : dataFolders) {
                File[] files = folder.listFiles((dir, name) -> name.endsWith(".png"));
                if (files == null) continue;

                for (File file : files) {
                    String name = file.getName().replace(".png", "");
                    File jsonCache = new File(folder, name + ".json");

                    if (jsonCache.exists()) {
                        loadFromJson(name, jsonCache);
                    } else {
                        try {
                            plugin.getLogger().info("Uploading " + file.getName() + " to MineSkin...");
                            IconData data = uploadToMineSkin(file);
                            if (data != null) {
                                saveToJson(name, jsonCache, data);
                                iconCache.put(name.toLowerCase(), data);
                                plugin.getLogger().info("Successfully loaded and cached icon: " + name);
                            } else {
                                plugin.getLogger().warning("Failed to generate skin for " + file.getName());
                            }
                            // Sleep to avoid MineSkin rate limiting (approx 6 seconds per request required)
                            Thread.sleep(6000);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error uploading " + file.getName() + ": " + e.getMessage());
                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }
            }
        });
    }

    private void loadFromJson(String name, File file) {
        try (FileReader reader = new FileReader(file)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            String value = obj.get("value").getAsString();
            String signature = obj.has("signature") ? obj.get("signature").getAsString() : "";
            String url = obj.has("url") ? obj.get("url").getAsString() : "";
            
            iconCache.put(name.toLowerCase(), new IconData(value, signature, url));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load cache for " + name + ": " + e.getMessage());
        }
    }

    private void saveToJson(String name, File file, IconData data) {
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("value", data.getValue());
            obj.addProperty("signature", data.getSignature());
            obj.addProperty("url", data.getUrl());
            writer.write(obj.toString());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save cache for " + name + ": " + e.getMessage());
        }
    }

    public IconData getIcon(String name) {
        return iconCache.get(name.toLowerCase());
    }

    public java.util.Set<String> getAllKeys() {
        return java.util.Collections.unmodifiableSet(iconCache.keySet());
    }

    private IconData uploadToMineSkin(File file) throws Exception {
        String boundary = "Boundary" + System.currentTimeMillis() + "Hash";
        URL url = new URL("https://api.mineskin.org/generate/upload?visibility=1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "HouziCore-IconManager");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {
            
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: image/png\r\n\r\n");
            writer.flush();

            Files.copy(file.toPath(), outputStream);
            outputStream.flush();

            writer.append("\r\n").flush();
            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();
        }

        int status = conn.getResponseCode();
        if (status == 200) {
            try (InputStream in = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
                if (response.has("data")) {
                    JsonObject data = response.getAsJsonObject("data");
                    JsonObject texture = data.getAsJsonObject("texture");
                    
                    String value = texture.get("value").getAsString();
                    String signature = texture.get("signature").getAsString();
                    String textureUrl = texture.get("url").getAsString();
                    
                    return new IconData(value, signature, textureUrl);
                }
            }
        } else {
            try (InputStream in = conn.getErrorStream();
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject error = JsonParser.parseReader(reader).getAsJsonObject();
                throw new Exception("MineSkin API Error " + status + ": " + error.toString());
            }
        }
        return null;
    }
}
