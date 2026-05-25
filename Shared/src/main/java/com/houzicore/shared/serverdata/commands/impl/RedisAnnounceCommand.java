package com.houzicore.shared.serverdata.commands.impl;

import com.houzicore.shared.serverdata.commands.ServerCommand;

public class RedisAnnounceCommand extends ServerCommand {

    private String message;
    private String permission; // If null, announce to all

    public RedisAnnounceCommand(String message, String permission) {
        super();
        this.message = message;
        this.permission = permission;
    }

    public String getMessage() {
        return message;
    }

    public String getPermission() {
        return permission;
    }
}
