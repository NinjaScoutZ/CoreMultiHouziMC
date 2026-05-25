package com.houzicore.extension.model.event.message;

import com.google.gson.JsonObject;
import lombok.With;
import com.houzicore.extension.model.event.Event;

@With
public record StatusResponseEvent(
        boolean cancelled,
        JsonObject response
) implements Event {

    public StatusResponseEvent(JsonObject response) {
        this(false, response);
    }

}
