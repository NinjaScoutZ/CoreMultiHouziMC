package com.houzicore.shared.core.icon;

public class IconData {
    private final String value;
    private final String signature;
    private final String url;

    public IconData(String value, String signature, String url) {
        this.value = value;
        this.signature = signature;
        this.url = url;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public String getUrl() {
        return url;
    }
}
