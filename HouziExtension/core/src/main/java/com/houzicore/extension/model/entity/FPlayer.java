package com.houzicore.extension.model.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Builder;
import lombok.With;
import com.houzicore.extension.model.FColor;

import com.houzicore.extension.service.FPlayerService;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.constant.SettingText;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This is a platform-dynamic, Houzi player. All actions done through Houzi involving a player most likely are done through FPlayer.
 * <hr>
 * <p>
 * For example, plugins using the Bukkit API can get an instance of the {@link FPlayer} object by simply using
 * <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Entity.html#getUniqueId()"><code>Entity.getUniqueId()</code></a>
 * and using {@link FPlayerService}'s <code>{@link UUID} getFPlayer</code> method.
 * </p>
 *
 * @see FPlayerService
 */
public interface FPlayer extends FEntity {

    static FPlayerImpl.FPlayerImplBuilder builder() {
        return new FPlayerImpl.FPlayerImplBuilder();
    }

    String PLAYER_TYPE = "PLAYER";

    String CONSOLE_TYPE = "CONSOLE";

    String INTEGRATION_TYPE = "INTEGRATION";

    FPlayer UNKNOWN = FPlayer.builder().build();

    Integer id();

    boolean isConsole();

    boolean isIntegration();

    boolean isOnline();

    String ip();

    Map<FColor.Type, Set<FColor>> fColors();

    Map<String, Boolean> settingsBoolean();

    Map<SettingText, String> settingsText();


    List<Component> constants();

    FPlayer withOnline(boolean online);

    FPlayer withIp(String ip);


    FPlayer withSetting(String messageType, boolean value);

    FPlayer withSetting(SettingText settingText, String value);


    @Nullable String getSetting(SettingText settingText);

    @NonNull String getSetting(ModuleName moduleName);

    @NonNull String getSetting(String moduleName);

    boolean isSetting(ModuleName moduleName);

    boolean isSetting(String moduleName);

    FPlayer withoutSetting(SettingText settingText);

    FPlayer withoutSetting(String moduleName);

    FPlayer withoutSetting(ModuleName moduleName);


    Map<Integer, String> getFColors(FColor.Type type);

    FPlayer withFColors(FColor.Type type, Set<FColor> fColors);


    FPlayer withConstants(List<Component> constants);

    FPlayerImpl.FPlayerImplBuilder toBuilder();

    @Override
    default boolean isUnknown() {
        return id() == -1;
    }

    @Builder(toBuilder = true)
    @With
    record FPlayerImpl(
            String name,
            UUID uuid,
            String type,
            @Nullable Component showEntityName,
            Integer id,
            boolean console,
            boolean integration,
            boolean online,
            String ip,
            Map<FColor.Type, Set<FColor>> fColors,
            Map<String, Boolean> settingsBoolean,
            Map<SettingText, String> settingsText,

            List<Component> constants
    ) implements FPlayer {

        public FPlayerImpl {
            if (name == null) name = FEntity.UNKNOWN_NAME;
            if (uuid == null) uuid = FEntity.UNKNOWN_UUID;
            if (type == null) type = PLAYER_TYPE;
            if (id == null) id = -1;
            if (fColors == null) fColors = Collections.emptyMap();
            if (settingsBoolean == null) settingsBoolean = Collections.emptyMap();
            if (settingsText == null) settingsText = Collections.emptyMap();

            if (constants == null) constants = Collections.emptyList();

            console = console || type.equalsIgnoreCase(CONSOLE_TYPE);
            if (console) type = CONSOLE_TYPE;

            integration = integration || type.equalsIgnoreCase(INTEGRATION_TYPE);
            if (integration) type = INTEGRATION_TYPE;
        }

        @Override
        public boolean isConsole() {
            return console;
        }

        @Override
        public boolean isIntegration() {
            return integration;
        }

        @Override
        public boolean isOnline() {
            return online;
        }

        @Override
        public FPlayer withSetting(@NonNull String messageType, boolean value) {
            Map<String, Boolean> newSettings = new Object2BooleanArrayMap<>(this.settingsBoolean);

            newSettings.put(messageType, value);

            return toBuilder()
                    .settingsBoolean(Collections.unmodifiableMap(newSettings))
                    .build();
        }

        @Override
        public FPlayer withSetting(@NonNull SettingText settingText, @Nullable String value) {
            Map<SettingText, String> newSettings = this.settingsText.isEmpty()
                    ? new EnumMap<>(SettingText.class)
                    : new EnumMap<>(this.settingsText);

            newSettings.put(settingText, value);

            return toBuilder()
                    .settingsText(Collections.unmodifiableMap(newSettings))
                    .build();
        }

        @Override
        public @Nullable String getSetting(@Nullable SettingText settingText) {
            return this.settingsText.get(settingText);
        }

        @Override
        public @NonNull String getSetting(@NonNull ModuleName moduleName) {
            return getSetting(moduleName.name());
        }

        @Override
        public @NonNull String getSetting(@Nullable String moduleName) {
            return isSetting(moduleName) ? "1" : "0";
        }

        @Override
        public boolean isSetting(@NonNull ModuleName messageType) {
            return isSetting(messageType.name());
        }

        @Override
        public boolean isSetting(@Nullable String moduleName) {
            Boolean value = this.settingsBoolean.get(moduleName);
            return value == null || value;
        }

        @Override
        public FPlayer withoutSetting(@Nullable SettingText settingText) {
            if (!this.settingsText.containsKey(settingText)) return this;

            Map<SettingText, String> newSettings = new EnumMap<>(this.settingsText);
            newSettings.remove(settingText);

            return toBuilder()
                    .settingsText(Collections.unmodifiableMap(newSettings))
                    .build();
        }

        @Override
        public FPlayer withoutSetting(@Nullable String moduleName) {
            if (!this.settingsBoolean.containsKey(moduleName)) return this;

            Map<String, Boolean> newSettings = new Object2BooleanArrayMap<>(this.settingsBoolean);

            newSettings.remove(moduleName);

            return toBuilder()
                    .settingsBoolean(Collections.unmodifiableMap(newSettings))
                    .build();
        }

        @Override
        public FPlayer withoutSetting(@NonNull ModuleName moduleName) {
            return withoutSetting(moduleName.name());
        }


        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof FPlayer fPlayer)) return false;

            return Objects.equals(this.id, fPlayer.id());
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }

        @Override
        public Map<Integer, String> getFColors(FColor.@NonNull Type type) {
            Set<FColor> colors = fColors.get(type);
            if (colors == null || colors.isEmpty()) return Collections.emptyMap();

            Map<Integer, String> result = colors.stream()
                    .collect(Collectors.toMap(
                            FColor::number,
                            FColor::name,
                            (v1, v2) -> v1,
                            Int2ObjectArrayMap::new
                    ));

            return Collections.unmodifiableMap(result);
        }

        @Override
        public FPlayer withFColors(FColor.@NonNull Type type, @Nullable Set<FColor> fColors) {
            boolean newFColorsEmpty = fColors == null || fColors.isEmpty();
            boolean oldFColorsEmpty = this.fColors.isEmpty();
            if (newFColorsEmpty && oldFColorsEmpty) return this;

            Map<FColor.Type, Set<FColor>> fColorMap = oldFColorsEmpty
                    ? new EnumMap<>(FColor.Type.class)
                    : new EnumMap<>(this.fColors);

            if (newFColorsEmpty) {
                fColorMap.remove(type);
            } else {
                fColorMap.put(type, Collections.unmodifiableSet(fColors));
            }

            return toBuilder()
                    .fColors(Collections.unmodifiableMap(fColorMap))
                    .build();
        }


        @Override
        public FPlayer withConstants(@Nullable List<Component> constants) {
            if (constants == null || constants.isEmpty()) {
                if (this.constants.isEmpty()) return this;

                return toBuilder()
                        .constants(Collections.emptyList())
                        .build();
            }

            return toBuilder()
                    .constants(Collections.unmodifiableList(constants))
                    .build();
        }

    }
}
