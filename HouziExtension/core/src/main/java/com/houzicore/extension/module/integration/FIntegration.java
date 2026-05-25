package com.houzicore.extension.module.integration;

import com.houzicore.extension.util.logging.FLogger;

public interface FIntegration {

    String getIntegrationName();

    FLogger getFLogger();

    default void hook() {
        logHook();
    }

    default void unhook() {
        logUnhook();
    }

    default void logHook() {
        getFLogger().info("[+] Loaded integration: %s", getIntegrationName());
    }

    default void logUnhook() {
        getFLogger().info("[-] Unloaded integration: %s", getIntegrationName());
    }

}
