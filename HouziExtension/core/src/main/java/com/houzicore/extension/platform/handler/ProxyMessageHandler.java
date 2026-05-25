package com.houzicore.extension.platform.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Localization;
import com.houzicore.extension.config.Message;
import com.houzicore.extension.data.repository.CooldownRepository;
import com.houzicore.extension.execution.dispatcher.MessageDispatcher;
import com.houzicore.extension.execution.pipeline.MessagePipeline;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.event.EventMetadata;
import com.houzicore.extension.model.event.ModerationMetadata;
import com.houzicore.extension.model.event.UnModerationMetadata;
import com.houzicore.extension.model.util.Destination;
import com.houzicore.extension.model.util.Moderation;
import com.houzicore.extension.model.util.Range;
// Pruned unused command/message module imports
import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.platform.formatter.ModerationMessageFormatter;
import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.service.ModerationService;
import com.houzicore.extension.util.constant.MessageFlag;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxyMessageHandler {

    private final Injector injector;
    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final FLogger fLogger;
    private final ModerationService moderationService;
    private final Gson gson;
    private final TaskScheduler taskScheduler;
    private final CooldownRepository cooldownRepository;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;

    public void handleProxyMessage(byte[] bytes) {
        taskScheduler.runAsync(() -> {
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
                 DataInputStream input = new DataInputStream(byteStream)) {

                ModuleName tag = ModuleName.fromProxyString(input.readUTF());
                if (tag == null) return;

                UUID uuid = UUID.fromString(input.readUTF());

                switch (tag) {
                    case SYSTEM_ONLINE -> handleSystemOnline(uuid);
                    case SYSTEM_OFFLINE -> handleSystemOffline(uuid);
                    default -> handleProxyMessage(input, uuid, tag);
                }
            } catch (IOException e) {
                fLogger.warning(e);
            }
        });
    }

    public void handleSystemOnline(UUID uuid) throws IOException {
        fPlayerService.invalidateOffline(uuid);
    }

    public void handleSystemOffline(UUID uuid) throws IOException {
        fPlayerService.invalidateOnline(uuid);
    }

    public void handleProxyMessage(DataInputStream input, UUID metadataUUID, ModuleName tag) throws IOException {
        Set<String> proxyClusters = gson.fromJson(input.readUTF(), new TypeToken<Set<String>>() {}.getType());

        Optional<FEntity> optionalFEntity = parseFEntity(readAsJsonObject(input));
        if (optionalFEntity.isEmpty()) return;
        if (handleSystemCooldown(tag, input)) return;

        FEntity fEntity = optionalFEntity.get();
        if (handleModerationInvalidation(tag, fEntity)) {
            return;
        }

        Set<String> configClusters = fileFacade.config().proxy().clusters();
        if (!configClusters.isEmpty() && configClusters.stream().noneMatch(proxyClusters::contains)) {
            return;
        }

        handleModuleMessage(input, fEntity, metadataUUID, tag);
    }

    public void handleModuleMessage(DataInputStream input, FEntity fEntity, UUID metadataUUID, ModuleName tag) throws IOException {
        // Handling for stripped modules has been physical deleted from compiling.
    }

    private boolean handleSystemCooldown(ModuleName tag, DataInputStream input) {
        return false;
    }

    private boolean handleModerationInvalidation(ModuleName tag, FEntity fEntity) {
        return false;
    }

    private Map<Integer, Object> parseVanillaArguments(JsonObject jsonObject) {
        Int2ObjectOpenHashMap<Object> result = new Int2ObjectOpenHashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            int key = Integer.parseInt(entry.getKey());
            JsonObject argumentJson = entry.getValue().getAsJsonObject();

            Optional<FEntity> entity = parseFEntity(argumentJson);
            result.put(key, entity.isPresent() ? entity.get() : gson.fromJson(argumentJson, Component.class));
        }

        return result;
    }

    protected JsonObject readAsJsonObject(DataInputStream input) throws IOException {
        return gson.fromJson(input.readUTF(), JsonObject.class);
    }

    protected Optional<FEntity> parseFEntity(JsonObject jsonObject) {
        if (jsonObject.has("name") && jsonObject.has("uuid") && jsonObject.has("type")) {
            boolean isPlayer = jsonObject.has("id");
            return Optional.of(gson.fromJson(jsonObject, isPlayer ? FPlayer.FPlayerImpl.class : FEntity.FEntityImpl.class));
        }

        return Optional.empty();
    }

}
