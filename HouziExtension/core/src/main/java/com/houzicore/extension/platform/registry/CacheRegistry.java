package com.houzicore.extension.platform.registry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.houzicore.extension.config.Config;
import com.houzicore.extension.util.constant.CacheName;
import com.houzicore.extension.util.file.FileFacade;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class CacheRegistry {

    private final Map<CacheName, Cache<?, ?>> cacheMap = new EnumMap<>(CacheName.class);

    private final FileFacade fileFacade;

    @Inject
    public CacheRegistry(FileFacade fileFacade) {
        this.fileFacade = fileFacade;

        init();
    }

    public void init() {
        Arrays.stream(CacheName.values()).forEach(this::create);
    }

    public void invalidate() {
        cacheMap.keySet().forEach(this::invalidate);
    }

    public <K, V> void create(CacheName cacheName) {
        if (cacheMap.containsKey(cacheName)) {
            throw new IllegalArgumentException("Cache already created for " + cacheName);
        }

        Config.Cache.CacheSetting cacheSetting = resolveCacheSetting(cacheName);

        Cache<K, V> cache = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheSetting.duration(), cacheSetting.timeUnit())
                .maximumSize(cacheSetting.size())
                .build();

        cacheMap.put(cacheName, cache);
    }

    public void invalidate(CacheName cacheName) {
        if (!cacheMap.containsKey(cacheName)) return;

        Config.Cache.CacheSetting cacheSetting = resolveCacheSetting(cacheName);

        if (cacheSetting.invalidateOnReload()) {
            cacheMap.get(cacheName).invalidateAll();
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(CacheName cacheName) {
        Object cache = cacheMap.get(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("No cache created for " + cacheName);
        }

        return (Cache<K, V>) cache;
    }

    private Config.Cache.CacheSetting resolveCacheSetting(CacheName cacheName) {
        Config.Cache.CacheSetting cacheSetting = fileFacade.config()
                .cache()
                .types()
                .get(cacheName);

        if (cacheSetting != null) {
            return cacheSetting;
        }

        Config.Cache.CacheSetting defaultSetting = defaultCacheSetting(cacheName);
        fileFacade.config().cache().types().put(cacheName, defaultSetting);
        return defaultSetting;
    }

    private Config.Cache.CacheSetting defaultCacheSetting(CacheName cacheName) {
        return switch (cacheName) {
            case COOLDOWN -> new Config.Cache.CacheSetting(false, 5, TimeUnit.HOURS, 5000);
            case DIALOG_CLICK -> new Config.Cache.CacheSetting(false, 1, TimeUnit.SECONDS, 100);
            case OFFLINE_PLAYERS -> new Config.Cache.CacheSetting(false, 1, TimeUnit.HOURS, 1000);
            case MODERATION -> new Config.Cache.CacheSetting(false, 1, TimeUnit.HOURS, 5000);
            case LEGACY_COLOR_MESSAGE -> new Config.Cache.CacheSetting(false, 10, TimeUnit.MINUTES, 100000);
            case MENTION_MESSAGE -> new Config.Cache.CacheSetting(false, 10, TimeUnit.MINUTES, 1000);
            case SWEAR_MESSAGE -> new Config.Cache.CacheSetting(false, 10, TimeUnit.MINUTES, 100000);
            case REPLACEMENT_MESSAGE -> new Config.Cache.CacheSetting(false, 10, TimeUnit.MINUTES, 100000);
            case REPLACEMENT_IMAGE -> new Config.Cache.CacheSetting(false, 10, TimeUnit.MINUTES, 100);
            case TRANSLATE_MESSAGE -> new Config.Cache.CacheSetting(false, 1, TimeUnit.HOURS, 5000);
            case PROFILE_PROPERTY -> new Config.Cache.CacheSetting(false, 1, TimeUnit.HOURS, 1000);
        };
    }
}
