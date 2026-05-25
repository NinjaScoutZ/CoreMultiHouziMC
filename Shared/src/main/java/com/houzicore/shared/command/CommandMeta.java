package com.houzicore.shared.command;

import com.houzicore.shared.common.Rank;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation-based command metadata.
 * When present on a CommandBase subclass, the framework reads metadata automatically.
 *
 * <pre>
 * {@literal @}CommandMeta(
 *     description = "Teleport to a player",
 *     usage = "/tp <player>",
 *     permission = Rank.MODERATOR,
 *     aliases = {"teleport", "goto"}
 * )
 * public class TeleportCommand extends CommandBase { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandMeta {
    /** Human-readable description of the command. */
    String description() default "";

    /** Usage string shown in help. Example: "/tp <player>" */
    String usage() default "";

    /** Minimum rank required to execute. */
    Rank permission() default Rank.ALL;

    /** Alternative command names. */
    String[] aliases() default {};

    /** Whether console can run this command. */
    boolean allowConsole() default false;
}
