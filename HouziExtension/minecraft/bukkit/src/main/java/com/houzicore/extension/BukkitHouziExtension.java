package com.houzicore.extension;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import com.houzicore.extension.exception.ReloadException;
import com.houzicore.extension.platform.controller.DialogController;
import com.houzicore.extension.platform.controller.InventoryController;
import com.houzicore.extension.processing.resolver.BukkitLibraryResolver;
import com.houzicore.extension.processing.resolver.LibraryResolver;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Singleton
public class BukkitHouziExtension extends JavaPlugin implements HouziExtension {

    private FLogger fLogger;
    private LibraryResolver libraryResolver;
    private Injector injector;

    @Override
    public void onLoad() {
        // initialize custom logger
        fLogger = new FLogger(
                logRecord -> this.getLogger().log(logRecord),
                () -> injector == null ? null : injector.getInstance(FileFacade.class)
        );
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        libraryResolver = new BukkitLibraryResolver(this);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        // configure packetevents api
        System.setProperty("packetevents.nbt.default-max-size", "2097152");
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).checkForUpdates(false).debug(false);

        // create guice injector for dependency injection
        injector = Guice.createInjector(Stage.PRODUCTION, new BukkitInjector(this, this, libraryResolver, fLogger));

        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        if (!isReady()) return;

        get(HouziExtensionAPI.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            terminateFailedPacketAdapter();
            return;
        }

        get(HouziExtensionAPI.class).onDisable();
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        get(HouziExtensionAPI.class).reload();
    }

    @Override
    public void initPacketAdapter() {
        PacketEvents.getAPI().init();
    }

    @Override
    public void terminateFailedPacketAdapter() {
        try {
            PacketEventsAPI<?> packetEventsAPI = PacketEvents.getAPI();
            if (!packetEventsAPI.isInitialized()) {
                packetEventsAPI.getInjector().uninject();
            }
        } catch (Exception ignored) {
            // ignore
        }
    }

    @Override
    public void terminatePacketAdapter() {
        PacketEvents.getAPI().terminate();
    }

    @Override
    public void closeUIs() {
        // close all open inventories
        injector.getInstance(InventoryController.class).closeAll();
        injector.getInstance(DialogController.class).closeAll();
    }

}
