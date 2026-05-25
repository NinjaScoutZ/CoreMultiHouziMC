package com.houzicore.extension.model;

public record FColor(int number, String name) {

    public enum Type {
        SEE, // always first
        OUT // always second
    }

}
