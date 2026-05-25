package com.houzicore.extension.module.message.bubble.render;

import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.module.message.bubble.model.Bubble;

import java.util.function.Predicate;

public interface BubbleRender {

    void renderBubble(Bubble bubble);

    void removeBubbleIf(Predicate<Bubble> bubbleEntityPredicate);

    void removeAllBubbles();

    boolean isCorrectPlayer(FPlayer sender);

    boolean isModern();

    boolean isInteractionRiding();

}
