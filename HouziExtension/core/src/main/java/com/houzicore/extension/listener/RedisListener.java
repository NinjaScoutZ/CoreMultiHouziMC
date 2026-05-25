package com.houzicore.extension.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.lettuce.core.pubsub.RedisPubSubListener;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.platform.handler.ProxyMessageHandler;
import com.houzicore.extension.platform.proxy.RedisProxy;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RedisListener implements RedisPubSubListener<byte[], byte[]> {

    private final RedisProxy redisProxySender;
    private final ProxyMessageHandler proxyMessageHandler;

    @Override
    public void message(byte[] channel, byte[] message) {
        if (!redisProxySender.isEnable()) return;

        proxyMessageHandler.handleProxyMessage(message);
    }

    @Override
    public void message(byte[] bytes, byte[] k1, byte[] bytes2) {
    }

    @Override
    public void subscribed(byte[] bytes, long l) {
    }

    @Override
    public void psubscribed(byte[] bytes, long l) {
    }

    @Override
    public void unsubscribed(byte[] bytes, long l) {
    }

    @Override
    public void punsubscribed(byte[] bytes, long l) {
    }

}
