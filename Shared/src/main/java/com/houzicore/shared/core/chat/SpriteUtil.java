package com.houzicore.shared.core.chat;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;

/**
 * SpriteUtil — Provides vanilla atlas sprite components for tab/chat/scoreboard.
 *
 * <p>These components rely on the client's native object rendering support
 * (Minecraft 1.21.9+ for object components). They do not require our own
 * resource pack when using vanilla atlases such as {@code blocks} or {@code gui}.</p>
 */
public final class SpriteUtil {

    private SpriteUtil() {
    }

    // Toggle: Enables native inline sprite components.
    public static boolean INLINE_SPRITE_ENABLED = true;

    /**
     * Builds a sprite from the default blocks atlas.
     * Example sprite names: {@code block/oak_log}, {@code block/stone}
     */
    public static Component buildInlineSprite(String sprite) {
        return buildInlineSprite(sprite, true);
    }

    public static Component buildInlineSprite(String sprite, boolean trailingSpace) {
        if (!INLINE_SPRITE_ENABLED || sprite == null || sprite.isEmpty()) return Component.empty();

        try {
            Component component = Component.object()
                    .contents(ObjectContents.sprite(Key.key(sprite)))
                    .build();

            return trailingSpace ? component.append(Component.space()) : component;
        } catch (Throwable ignored) {
            return Component.empty();
        }
    }

    /**
     * Builds a sprite from a specific atlas.
     * Example: atlas {@code gui}, sprite {@code container/slot}
     */
    public static Component buildInlineSprite(String atlas, String sprite, boolean trailingSpace) {
        if (!INLINE_SPRITE_ENABLED || atlas == null || atlas.isEmpty() || sprite == null || sprite.isEmpty()) {
            return Component.empty();
        }

        try {
            Component component = Component.object()
                    .contents(ObjectContents.sprite(Key.key(atlas), Key.key(sprite)))
                    .build();

            return trailingSpace ? component.append(Component.space()) : component;
        } catch (Throwable ignored) {
            return Component.empty();
        }
    }

    public static Component buildInlineSprite(String atlas, String sprite) {
        return buildInlineSprite(atlas, sprite, true);
    }

    public static String getSpriteTag(String sprite) {
        if (!INLINE_SPRITE_ENABLED || sprite == null || sprite.isEmpty()) return "";
        return "<sprite:" + sprite + "> ";
    }

    public static String getSpriteTag(String atlas, String sprite) {
        if (!INLINE_SPRITE_ENABLED || atlas == null || atlas.isEmpty() || sprite == null || sprite.isEmpty()) return "";
        return "<sprite:" + atlas + ":" + sprite + "> ";
    }
}
