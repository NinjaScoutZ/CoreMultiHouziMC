package com.houzicore.mapbuilder;

import com.houzicore.mapbuilder.schema.MapSchema;
import com.houzicore.mapbuilder.schema.MapSchemaExporter;

import org.bukkit.ChatColor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Writes WorldConfig.dat from a MapSchema.
 *
 * The output format is byte/line identical to the legacy implementation —
 * this is enforced by WorldConfigCompatibilityTest. Do not change the
 * serialisation format without updating the golden reference and the
 * Arcade-side ParseData reader simultaneously.
 */
public class WorldConfigExporter {

    public static void export(MapSession session) {
        File dir = session.getWorldConfigFile().getParentFile();
        MapSchema oldSchema = com.houzicore.mapbuilder.schema.MapSchemaRepository.load(dir);

        // Build typed schema first — serialisation is driven from here
        MapSchema schema = MapSchemaExporter.build(session);
        String content = MapSchemaExporter.serializeToString(schema);

        // Diff Reporting
        session.getBuilder().sendMessage(ChatColor.GOLD + "── Export Diff ──");
        if (oldSchema != null) {
            java.util.Set<String> allKeys = new java.util.HashSet<>();
            allKeys.addAll(schema.getDataPoints().keySet());
            allKeys.addAll(oldSchema.getDataPoints().keySet());
            
            boolean changed = false;
            for (String key : allKeys) {
                int oldSize = oldSchema.getDataPoints().containsKey(key) ? oldSchema.getDataPoints().get(key).size() : 0;
                int newSize = schema.getDataPoints().containsKey(key) ? schema.getDataPoints().get(key).size() : 0;
                
                int diff = newSize - oldSize;
                if (diff != 0) {
                    changed = true;
                    String diffStr = diff > 0 ? "+" + diff : String.valueOf(diff);
                    com.houzicore.mapbuilder.domain.MapPointDefinition def = com.houzicore.mapbuilder.domain.MapPointDefinition.fromExportKey(key);
                    String label = def != null ? def.displayName : key;
                    
                    ChatColor color = diff > 0 ? ChatColor.GREEN : ChatColor.RED;
                    session.getBuilder().sendMessage(color + "[" + diffStr + "] " + ChatColor.WHITE + label);
                }
            }
            if (!changed) {
                session.getBuilder().sendMessage(ChatColor.GRAY + "No point changes detected.");
            }
        } else {
            session.getBuilder().sendMessage(ChatColor.GREEN + "New schema generated (First export).");
        }

        File primaryFile = session.getWorldConfigFile();
        File schemaFile = new File(dir, "schema.json");

        // 1. Transactional Backup
        try {
            if (primaryFile.exists()) {
                File bakFile = new File(primaryFile.getPath() + ".bak");
                java.nio.file.Files.copy(primaryFile.toPath(), bakFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                session.getBuilder().sendMessage(ChatColor.GRAY + "Created backup: " + bakFile.getName());
            }
            if (schemaFile.exists()) {
                File bakFile = new File(schemaFile.getPath() + ".bak");
                java.nio.file.Files.copy(schemaFile.toPath(), bakFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                session.getBuilder().sendMessage(ChatColor.GRAY + "Created backup: " + bakFile.getName());
            }
        } catch (IOException e) {
            session.getBuilder().sendMessage(ChatColor.RED + "Failed to create backups. Export aborted.");
            return;
        }

        // 2. Write to Temp Files
        File pluginDataFolder = MapBuilderPlugin.getInstance().getDataFolder();
        File backupFolder = new File(pluginDataFolder, session.getMapName());
        if (!backupFolder.exists()) backupFolder.mkdirs();
        File pluginBackupFile = new File(backupFolder, "WorldConfig.dat");

        File tmpDatFile = new File(primaryFile.getPath() + ".tmp");
        File tmpSchemaFile = new File(schemaFile.getPath() + ".tmp");

        try {
            // Write TMP Dat
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpDatFile))) {
                writer.write(content);
            }
            // Write TMP Schema
            com.houzicore.mapbuilder.schema.MapSchemaRepository.save(new File(tmpSchemaFile.getParent()), tmpSchemaFile.getName(), schema);
            
            // Write to plugin data folder (not transactional, just an extra copy)
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(pluginBackupFile))) {
                writer.write(content);
            }

            // 3. Commit (Atomic Replace)
            java.nio.file.Files.move(tmpDatFile.toPath(), primaryFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            try {
                java.nio.file.Files.move(tmpSchemaFile.toPath(), schemaFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException innerE) {
                // Recover phase: if second file fails, restore the first file from backup to prevent state divergence
                File bakFile = new File(primaryFile.getPath() + ".bak");
                if (bakFile.exists()) {
                    java.nio.file.Files.copy(bakFile.toPath(), primaryFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } else {
                    // First export: restore previous absence
                    primaryFile.delete();
                }
                throw new IOException("Schema commit failed. Reverted dat file to backup/absence.", innerE);
            }

            session.getBuilder().sendMessage(ChatColor.GREEN + "✔ บันทึกข้อมูลแมพสำเร็จ (schema.json + WorldConfig.dat)!");
            session.getBuilder().sendMessage(ChatColor.AQUA + "  → " + primaryFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            session.getBuilder().sendMessage(ChatColor.DARK_RED + "เกิดข้อผิดพลาดในการบันทึกไฟล์ เขียนไฟล์ไม่สำเร็จ!");
            session.getBuilder().sendMessage(ChatColor.RED + "Error: " + e.getMessage());
        } finally {
            // Clean up stranded temp files
            if (tmpDatFile.exists()) tmpDatFile.delete();
            if (tmpSchemaFile.exists()) tmpSchemaFile.delete();
        }
    }
}
