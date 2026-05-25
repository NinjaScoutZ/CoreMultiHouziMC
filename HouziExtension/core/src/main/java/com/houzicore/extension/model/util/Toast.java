package com.houzicore.extension.model.util;

public record Toast(String icon, Type style) {
    public enum Type {
        GOAL,
        TASK,
        CHALLENGE
    }
}
