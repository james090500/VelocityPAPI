package com.james090500.VelocityPAPI.shared;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class VPObject {

    @Getter private String messageKey;
    @Getter private UUID uuid;
    @Getter private String message;
    @Getter @Setter private String result;
    @Getter private long createdAt;

    public VPObject(String messageKey, UUID uuid, String message) {
        this.messageKey = messageKey;
        this.uuid = uuid;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "VPObject{" +
                "messageKey='" + messageKey + '\'' +
                ", uuid=" + uuid +
                ", message='" + message + '\'' +
                ", result='" + result + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
