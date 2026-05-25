package com.houzicore.extension.platform.registry;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.BuildConfig;
import com.houzicore.extension.config.Config;
import com.houzicore.extension.data.database.Database;
import com.houzicore.extension.platform.proxy.Proxy;
import com.houzicore.extension.platform.proxy.RedisProxy;
import com.houzicore.extension.processing.resolver.LibraryResolver;
import com.houzicore.extension.processing.resolver.ReflectionResolver;
import com.houzicore.extension.util.file.FileFacade;
import com.houzicore.extension.util.logging.FLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxyRegistry {

    private final List<Proxy> proxies = new CopyOnWriteArrayList<>();

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final FLogger fLogger;
    private final Injector injector;

    public Collection<Proxy> getProxies() {
        return Collections.unmodifiableList(proxies);
    }

    public boolean hasEnabledProxy() {
        return proxies.stream().anyMatch(Proxy::isEnable);
    }

    public void registry(Proxy proxy) {
        proxies.add(proxy);
    }

    public void onEnable() {
        Config.Proxy.Redis redis = fileFacade.config().proxy().redis();
        if (redis.enable()) {
            warnIfLocalDatabase();

            reflectionResolver.hasClassOrElse("com.houzicore.extension.library.lettuce.core.RedisClient", this::loadLibraries);

            RedisProxy redisProxy = injector.getInstance(RedisProxy.class);
            redisProxy.onEnable();

            registry(redisProxy);
        }
    }

    protected void warnIfLocalDatabase() {
        Config.Database database = fileFacade.config().database();
        if (database.type() == Database.Type.SQLITE || database.type() == Database.Type.H2) {
            fLogger.warning("SQLITE/H2 database and Proxy are incompatible");
        }
    }

    public void onDisable() {
        proxies.forEach(Proxy::onDisable);
        proxies.clear();
    }

    public void reload() {
        onDisable();
        onEnable();
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}lettuce")
                .artifactId("lettuce-core")
                .version(BuildConfig.LETTUCE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("io{}lettuce")
                        .relocatedPattern("com.houzicore.extension.library.lettuce")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("io{}netty")
                        .relocatedPattern("com.houzicore.extension.library.lettuce.netty")
                        .build()
                )
                .build()
        );
    }

}
