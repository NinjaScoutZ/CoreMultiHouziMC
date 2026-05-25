package com.houzicore.extension.module.message.bubble.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import com.houzicore.extension.module.message.bubble.BubbleModule;

@Getter
@SuperBuilder
public class ModernBubble extends Bubble {

    private final boolean hasShadow;
    private final boolean seeThrough;

    private final int background;

    private final float scale;

    private final int animationTime;

    private final BubbleModule.Billboard billboard;

}
