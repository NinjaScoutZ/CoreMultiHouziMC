package com.houzicore.extension.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import com.houzicore.extension.BuildConfig;
import com.houzicore.extension.annotation.Pulse;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.listener.PulseListener;
import com.houzicore.extension.model.FColor;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.Event;
import com.houzicore.extension.model.event.message.MessageFormattingEvent;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.module.integration.FIntegration;

import com.houzicore.extension.platform.adapter.PlatformPlayerAdapter;
import com.houzicore.extension.platform.adapter.PlatformServerAdapter;
import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.util.checker.PermissionChecker;
import com.houzicore.extension.util.constant.MessageFlag;
import com.houzicore.extension.util.constant.SettingText;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIIntegration extends PlaceholderExpansion implements FIntegration, PulseListener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final PlaceholderAPIModule placeholderAPIModule;
    private final TaskScheduler taskScheduler;
    private final ModuleController moduleController;
    @Getter private final FLogger fLogger;

    @Override
    public @NonNull String getIdentifier() {
        return BuildConfig.PROJECT_NAME;
    }

    @Override
    public @NonNull String getAuthor() {
        return BuildConfig.PROJECT_AUTHOR;
    }

    @Override
    public @NonNull String getVersion() {
        return BuildConfig.PROJECT_VERSION;
    }

    @Override
    public String getIntegrationName() {
        return "PlaceholderAPI";
    }

    @Override
    public void hook() {
        taskScheduler.runSync(this::register);
        logHook();
    }

    @Override
    public void unhook() {
        taskScheduler.runSync(this::unregister);
        logUnhook();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NonNull String params) {
        if (player == null) return null;

        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUniqueId());

        params = params.toLowerCase();
        if (params.startsWith("fcolor")) {

            String number = params.substring(params.lastIndexOf("_") + 1);
            if (!StringUtils.isNumeric(number)) return null;

            Map<Integer, String> colorsMap = new Object2ObjectArrayMap<>(fileFacade.message().format().fcolor().defaultColors());
            if (params.startsWith("fcolor_out")) {
                colorsMap.putAll(fPlayer.getFColors(FColor.Type.OUT));
            } else if (params.startsWith("fcolor_see")) {
                colorsMap.putAll(fPlayer.getFColors(FColor.Type.SEE));
            } else {
                colorsMap.putAll(fPlayer.getFColors(FColor.Type.SEE));
                colorsMap.putAll(fPlayer.getFColors(FColor.Type.OUT));
            }

            return colorsMap.get(Integer.parseInt(number));
        }

        if (params.startsWith("setting_")) {
            String conditionName = params.substring(8);
            if (StringUtils.isEmpty(conditionName)) return null;

            SettingText settingText = SettingText.fromString(conditionName);
            if (settingText != null) {
                String value = fPlayer.getSetting(settingText);
                if (settingText == SettingText.CHAT_NAME && value == null) return "default";

                return StringUtils.defaultString(value);
            }

            return fPlayer.isSetting(params.toUpperCase()) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

        return switch (params) {
            case "player" -> fPlayer.name();
            case "player_head" -> "<white><player_head></white>";
            case "ip" -> fPlayer.ip();
            case "ping" -> String.valueOf(platformPlayerAdapter.getPing(fPlayer));
            case "online" -> String.valueOf(platformServerAdapter.getOnlinePlayerCount());
            case "tps" -> platformServerAdapter.getTPS();
            default -> null;
        };
    }

    @Pulse(priority = Event.Priority.LOW)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(placeholderAPIModule, sender)) return event;

        FPlayer fReceiver = messageContext.receiver();
        boolean isUserMessage = messageContext.isFlag(MessageFlag.PLAYER_MESSAGE);
        if (!permissionChecker.check(sender, placeholderAPIModule.permission().use()) && isUserMessage) return event;
        if (!(sender instanceof FPlayer fPlayer)) return event;

        String message = messageContext.message();

        // switch parsing
        if (!messageContext.isFlag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER)) {
            FPlayer tempFPlayer = fPlayer;
            fPlayer = fReceiver;
            fReceiver = tempFPlayer;
        }

        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(fPlayer.uuid());
            message = PlaceholderAPI.setPlaceholders(offlinePlayer, message);

            if (fPlayer.isOnline()) {
                Player receiver = Bukkit.getPlayer(fReceiver.uuid());
                if (receiver == null) {
                    receiver = offlinePlayer.getPlayer();
                }

                message = PlaceholderAPI.setRelationalPlaceholders(offlinePlayer.getPlayer(), receiver, message);
            }

        } catch (Exception ignored) {
            // ignore placeholderapi exceptions
        }

        return event.withContext(messageContext.withMessage(message));
    }
}
