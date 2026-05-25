package com.houzicore.extension;

import com.google.inject.Injector;
import com.houzicore.extension.exception.ReloadException;

/**
 * Main interface for accessing HouziExtension API functionality.
 * Provides dependency injection capabilities and plugin lifecycle management.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * // Get the HouziExtension instance
 * HouziExtension houzicorePulse = HouziExtensionAPI.getInstance();
 *
 * // Check if the injector is ready
 * if (houzicorePulse.isReady()) {
 *     // Get a dependency
 *     FLogger logger = houzicorePulse.get(FLogger.class);
 *     logger.info("Hello world");
 * }
 * }</pre>
 *
 * @author HouziCore Development
 * @see HouziExtensionAPI#getInstance()
 * @since 0.1.0
 */
public interface HouziExtension {

    /**
     * Gets the Google Guice Injector instance used for dependency injection.
     * This injector is responsible for creating and managing instances of
     * HouziExtension components and services.
     *
     * @return the Injector instance, or {@code null} if not initialized
     * @see #isReady()
     * @see #get(Class)
     */
    Injector getInjector();

    /**
     * Called when the HouziExtension is enabled.
     * <p>
     * This method initializes the dependency injector and prepares the HouziExtension
     * for operation. It should only be called by the HouziExtension itself.
     *
     */
    void onEnable();

    /**
     * Called when the HouziExtension is disabled.
     * <p>
     * This method cleans up resources and shuts down HouziExtension modules.
     * It should only be called by the HouziExtension itself.
     *
     */
    void onDisable();

    /**
     * Reloads the HouziExtension configuration and modules.
     * <p>
     * This method reinitializes the plugin with updated configuration files
     * and should be called when configuration changes are made at runtime.
     *
     * @throws ReloadException if an error occurs during reload
     * @see ReloadException
     */
    void reload() throws ReloadException;

    /**
     * Initialize the PacketAdapter API.
     */
    void initPacketAdapter();

    /**
     * Terminates PacketAdapter API if initialization failed.
     * Prevents errors when HouziExtension fails to start.
     */
    void terminateFailedPacketAdapter();

    /**
     * Terminates the PacketAdapter API and cleans up related resources.
     */
    void terminatePacketAdapter();

    /**
     * Closes all open user interfaces including inventories and dialogs.
     */
    void closeUIs();

    /**
     * Retrieves an instance of the specified class through dependency injection.
     * Uses Google Guice as the underlying dependency injection framework.
     *
     * <p><b>Note:</b> Most HouziExtension classes (except models) are marked with {@code @Singleton}.
     *
     * @param <T> the type of instance to retrieve
     * @param type the class of the instance to retrieve
     * @return an instance of the requested type
     * @throws IllegalStateException if the injector is not ready
     * @see #isReady()
     */
    default <T> T get(Class<T> type) {
        Injector injector = getInjector();
        if (injector == null) {
            throw new IllegalStateException("HouziExtension not initialized yet");
        }

        return injector.getInstance(type);
    }

    /**
     * Checks if the dependency injector is ready to provide instances.
     *
     * <p><b>Important:</b> Always call this method before {@link #get(Class)}
     * to ensure the injector has been properly initialized.
     *
     * @return {@code true} if the injector is ready, {@code false} otherwise
     * @see #get(Class)
     */
    default boolean isReady() {
        return getInjector() != null;
    }

}
