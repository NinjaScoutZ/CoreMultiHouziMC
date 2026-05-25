package com.houzicore.extension.module.message.format;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.config.Localization;
import com.houzicore.extension.config.Message;
import com.houzicore.extension.config.Permission;
import com.houzicore.extension.config.setting.PermissionSetting;
import com.houzicore.extension.execution.pipeline.MessagePipeline;
import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.model.event.message.context.MessageContext;
import com.houzicore.extension.module.ModuleLocalization;
import com.houzicore.extension.module.ModuleSimple;
import com.houzicore.extension.module.integration.IntegrationModule;
import com.houzicore.extension.module.message.format.object.ObjectModule;
import com.houzicore.extension.module.message.format.replacement.ReplacementModule;
import com.houzicore.extension.platform.controller.ModuleController;
import com.houzicore.extension.platform.registry.ListenerRegistry;
import com.houzicore.extension.util.checker.PermissionChecker;
import com.houzicore.extension.util.constant.AdventureTag;
import com.houzicore.extension.util.constant.MessageFlag;
import com.houzicore.extension.util.constant.ModuleName;
import com.houzicore.extension.util.file.FileFacade;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FormatModule implements ModuleLocalization<Localization.Message.Format> {

    private final Map<AdventureTag, TagResolver> tagResolverMap = new EnumMap<>(AdventureTag.class);

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;
    private final ModuleController moduleController;

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ModuleLocalization.super.childrenBuilder().add(
                ObjectModule.class,
                ReplacementModule.class
        );
    }

    @Override
    public void onEnable() {
        putAdventureTag(AdventureTag.HOVER, StandardTags.hoverEvent());
        putAdventureTag(AdventureTag.CLICK, StandardTags.clickEvent());
        putAdventureTag(AdventureTag.COLOR, StandardTags.color());
        putAdventureTag(AdventureTag.KEYBIND, StandardTags.keybind());
        putAdventureTag(AdventureTag.TRANSLATABLE, StandardTags.translatable());
        putAdventureTag(AdventureTag.TRANSLATABLE_FALLBACK, StandardTags.translatableFallback());
        putAdventureTag(AdventureTag.INSERTION, StandardTags.insertion());
        putAdventureTag(AdventureTag.FONT, StandardTags.font());
        putAdventureTag(AdventureTag.DECORATION, StandardTags.decorations());
        putAdventureTag(AdventureTag.GRADIENT, StandardTags.gradient());
        putAdventureTag(AdventureTag.RAINBOW, StandardTags.rainbow());
        putAdventureTag(AdventureTag.RESET, StandardTags.reset());
        putAdventureTag(AdventureTag.NEWLINE, StandardTags.newline());
        putAdventureTag(AdventureTag.TRANSITION, StandardTags.transition());
        putAdventureTag(AdventureTag.SELECTOR, StandardTags.selector());
        putAdventureTag(AdventureTag.SCORE, StandardTags.score());
        putAdventureTag(AdventureTag.NBT, StandardTags.nbt());
        putAdventureTag(AdventureTag.PRIDE, StandardTags.pride());
        putAdventureTag(AdventureTag.SHADOW_COLOR, StandardTags.shadowColor());


    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder()
                .add(permission().legacyColors())
                .addAll(permission().adventureTags().values());
    }

    @Override
    public void onDisable() {
        tagResolverMap.clear();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT;
    }

    @Override
    public Message.Format config() {
        return fileFacade.message().format();
    }

    @Override
    public Permission.Message.Format permission() {
        return fileFacade.permission().message().format();
    }

    @Override
    public Localization.Message.Format localization(FEntity sender) {
        return fileFacade.localization(sender).message().format();
    }

    public MessageContext addTags(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        boolean isUserMessage = messageContext.isFlag(MessageFlag.PLAYER_MESSAGE);

        return messageContext.addTagResolvers(tagResolverMap
                .entrySet()
                .stream()
                .filter(entry -> isCorrectTag(entry.getKey(), sender, isUserMessage))
                .map(entry -> {
                    if (entry.getKey() == AdventureTag.GRADIENT
                            && integrationModule.isBedrockPlayer(messageContext.receiver())) {
                        return bedrockGradientTag();
                    }

                    return entry.getValue();
                })
                .toList()
        );
    }

    public boolean isCorrectTag(AdventureTag adventureTag, FEntity sender, boolean needPermission) {
        if (!config().adventureTags().contains(adventureTag)) return false;
        if (!tagResolverMap.containsKey(adventureTag)) return false;

        return !needPermission || permissionChecker.check(sender, permission().adventureTags().get(adventureTag));
    }

    private TagResolver bedrockGradientTag() {
        return TagResolver.resolver("gradient", (argumentQueue, context) -> {
            Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return MessagePipeline.ReplacementTag.emptyTag();

            TextColor textColor = TextColor.fromHexString(argument.value());
            if (textColor == null) return MessagePipeline.ReplacementTag.emptyTag();

            return Tag.styling(textColor);
        });
    }

    private void putAdventureTag(AdventureTag adventureTag, TagResolver tagResolver) {
        if (config().adventureTags().contains(adventureTag)) {
            tagResolverMap.put(adventureTag, tagResolver);
        }
    }
}
