package com.houzicore.shared.api.loadout;

import java.util.Objects;

public record LoadoutProfile(String key) {

    public LoadoutProfile {
        Objects.requireNonNull(key, "key");
    }

    public static LoadoutProfile of(String key) {
        return new LoadoutProfile(key);
    }
}
