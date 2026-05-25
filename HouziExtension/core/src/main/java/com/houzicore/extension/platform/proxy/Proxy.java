package com.houzicore.extension.platform.proxy;

import com.houzicore.extension.model.entity.FEntity;
import com.houzicore.extension.util.constant.ModuleName;

public interface Proxy {

    boolean isEnable();

    void onEnable();

    void onDisable();

    boolean sendMessage(FEntity sender, ModuleName tag, byte[] message);

}
