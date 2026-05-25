package com.houzicore.shared.core.displayentity.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.DisplayModelImporter;

public class ImportDisplayModelCommand extends CommandBase<MiniPlugin> {

    private final DisplayEntityManager _manager;

    public ImportDisplayModelCommand(DisplayEntityManager manager) {
        super(null, Rank.ADMIN, "importmodel", "bdimport", "importdisplaymodel");
        _manager = manager;
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args.length == 0) {
            sendUsage(caller);
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sendList(caller);
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            _manager.reloadRegistry();
            UtilPlayer.message(caller, F.main("Display", "Reloaded display models. Loaded: " + _manager.getRegistry().getModels().size()));
            return;
        }

        if (args.length < 2) {
            sendUsage(caller);
            return;
        }

        String modelId = args[0];
        String source = args[1];
        boolean force = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--force")) {
                force = true;
                break;
            }
        }
        final boolean forceImport = force;

        UtilPlayer.message(caller, F.main("Display", "Importing model '" + modelId + "'..."));
        _manager.getPlugin().getServer().getScheduler().runTaskAsynchronously(_manager.getPlugin(), () -> {
            try {
                DisplayModelImporter.SourceContent content = DisplayModelImporter.loadSource(_manager, source);
                Bukkit.getScheduler().runTask(_manager.getPlugin(), () -> finishImport(caller, modelId, source, content, forceImport));
            } catch (IOException e) {
                Bukkit.getScheduler().runTask(_manager.getPlugin(), () ->
                        UtilPlayer.message(caller, F.main("Display", ChatColor.RED + e.getMessage())));
            }
        });
    }

    private void finishImport(Player caller, String modelId, String source, DisplayModelImporter.SourceContent content, boolean force) {
        try {
            DisplayModelImporter.ImportResult result = DisplayModelImporter.importModelFromText(
                    _manager, modelId, content.text(), source, content.sourceDescription(), force);

            UtilPlayer.message(caller, F.main("Display", "Imported " + result.modelId()
                    + " (" + result.parts() + " parts)."));
            UtilPlayer.message(caller, F.main("Display", "Saved: " + result.file().getName()));
            UtilPlayer.message(caller, F.main("Display", "Use /spawnmodel " + result.modelId()
                    + " or /givefurniture " + result.modelId()));
        } catch (IOException e) {
            UtilPlayer.message(caller, F.main("Display", ChatColor.RED + e.getMessage()));
        }
    }

    private void sendList(Player caller) {
        List<String> modelIds = _manager.getRegistry().getModels().stream()
                .map(DisplayModel::getId)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        if (modelIds.isEmpty()) {
            UtilPlayer.message(caller, F.main("Display", "No display models loaded."));
            return;
        }

        String preview = modelIds.stream().limit(30).collect(Collectors.joining(", "));
        if (modelIds.size() > 30) {
            preview += " ...";
        }
        UtilPlayer.message(caller, F.main("Display", "Loaded models (" + modelIds.size() + "): " + preview));
    }

    private void sendUsage(Player caller) {
        File dir = _manager.getRegistry().getModelsDirectory();
        String dirName = dir != null ? dir.getAbsolutePath() : new File(_manager.getPlugin().getDataFolder(), "models").getAbsolutePath();
        UtilPlayer.message(caller, F.main("Display", "Usage: /importmodel <id> <url|fileName> [--force]"));
        UtilPlayer.message(caller, F.main("Display", "Also: /importmodel list, /importmodel reload"));
        UtilPlayer.message(caller, F.main("Display", "Local files must be in: " + dirName));
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String option : List.of("list", "reload")) {
                if (option.startsWith(args[0].toLowerCase())) {
                    out.add(option);
                }
            }
        }
        return out;
    }
}
