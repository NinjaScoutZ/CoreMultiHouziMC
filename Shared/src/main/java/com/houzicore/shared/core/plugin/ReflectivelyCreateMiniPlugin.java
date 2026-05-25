package com.houzicore.shared.core.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this MiniPlugin can be reflectively instantiated by the PluginRegistry
 * if an instance has not already been provided manually.
 * The class must declare a public constructor that accepts only a (JavaPlugin plugin) parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReflectivelyCreateMiniPlugin {
}
