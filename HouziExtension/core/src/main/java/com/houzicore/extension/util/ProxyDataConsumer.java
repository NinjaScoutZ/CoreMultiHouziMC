package com.houzicore.extension.util;

import java.io.IOException;

@FunctionalInterface
public interface ProxyDataConsumer<T> {

    void accept(T t) throws IOException;

}
